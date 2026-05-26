package com.zakaflow.zakaflow.service.impl;

import com.zakaflow.zakaflow.dto.ProgramCreateForm;
import com.zakaflow.zakaflow.model.Category;
import com.zakaflow.zakaflow.model.DonationProgram;
import com.zakaflow.zakaflow.model.FundingType;
import com.zakaflow.zakaflow.model.ProgramStatus;
import com.zakaflow.zakaflow.repository.CategoryRepository;
import com.zakaflow.zakaflow.repository.DonationProgramRepository;
import com.zakaflow.zakaflow.repository.DonationTransactionRepository;
import com.zakaflow.zakaflow.service.DonationProgramService;
import com.zakaflow.zakaflow.service.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DonationProgramServiceImpl implements DonationProgramService {

    private final DonationProgramRepository donationProgramRepository;
    private final DonationTransactionRepository donationTransactionRepository;
    private final CategoryRepository categoryRepository;
    private final FileStorageService fileStorageService;

    @Override
    public List<DonationProgram> findAll() {
        return donationProgramRepository.findAll();
    }

    @Override
    public List<DonationProgram> findAllActive() {
        return donationProgramRepository.findAll().stream()
                .filter(DonationProgram::isPubliclyVisible)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<DonationProgram> findById(Long id) {
        return donationProgramRepository.findById(id);
    }

    @Override
    @Transactional
    public DonationProgram save(DonationProgram program) {
        return donationProgramRepository.save(program);
    }

    @Override
    @Transactional
    public DonationProgram createProgram(ProgramCreateForm form, MultipartFile thumbnail, MultipartFile[] gallery) {
        validateForm(form);

        Category category = resolveCategory(form.getFundingType());

        DonationProgram program = new DonationProgram();
        applyFormFields(program, form, category);
        applyMedia(program, thumbnail, gallery, false);

        return donationProgramRepository.save(program);
    }

    @Override
    @Transactional
    public DonationProgram updateProgram(Long id, ProgramCreateForm form, MultipartFile thumbnail, MultipartFile[] gallery) {
        validateForm(form);

        DonationProgram program = donationProgramRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Program tidak ditemukan"));

        Category category = resolveCategory(form.getFundingType());
        applyFormFields(program, form, category);
        applyMedia(program, thumbnail, gallery, true);

        return donationProgramRepository.save(program);
    }

    @Override
    @Transactional
    public void deleteProgram(Long id) {
        DonationProgram program = donationProgramRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Program tidak ditemukan"));

        long txCount = donationTransactionRepository.countByProgram_Id(id);
        if (txCount > 0) {
            throw new IllegalArgumentException(
                    "Program tidak dapat dihapus karena memiliki " + txCount + " transaksi donasi.");
        }

        donationProgramRepository.delete(program);
    }

    private Category resolveCategory(FundingType fundingType) {
        return categoryRepository.findByName(fundingType.getLabel())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Kategori pendanaan tidak ditemukan: " + fundingType.getLabel()));
    }

    private void applyFormFields(DonationProgram program, ProgramCreateForm form, Category category) {
        program.setTitle(form.getTitle().trim());
        program.setExcerpt(form.getExcerpt().trim());
        program.setDescription(form.getFullDescription());
        program.setBeneficiary(form.getBeneficiary().trim());
        program.setFundingType(form.getFundingType());
        program.setSpecificCategory(form.getSpecificCategory());
        program.setAsnafList(form.getAsnaf());
        program.setZakatType(form.getFundingType() == FundingType.ZAKAT ? form.getZakatType() : null);
        program.setOpenEnded(form.isOpenEnded());
        program.setTargetAmount(form.isOpenEnded() ? null : form.getTargetAmount());
        program.setStartDate(form.getStartDate());
        program.setEndDate(form.getEndDate());
        program.setVideoUrl(blankToNull(form.getVideoUrl()));
        program.setMinDonationAmount(form.getMinDonationAmount());
        program.setQuickAmounts(normalizeQuickAmounts(form.getQuickAmounts()));
        ProgramStatus status = form.getStatus() != null ? form.getStatus() : ProgramStatus.DRAFT;
        program.setStatus(status);
        program.setPersonInCharge(blankToNull(form.getPersonInCharge()));
        program.setCategory(category);

        if (status == ProgramStatus.ACTIVE && program.getStartDate() == null) {
            program.setStartDate(LocalDate.now());
        }

        program.setCompleted(resolveCompleted(program, status));
    }

    private boolean resolveCompleted(DonationProgram program, ProgramStatus status) {
        if (status == ProgramStatus.CLOSED) {
            return true;
        }
        if (status == ProgramStatus.ACTIVE) {
            return !program.isOpenEnded()
                    && program.getTargetAmount() != null
                    && program.getCurrentAmount().compareTo(program.getTargetAmount()) >= 0;
        }
        return false;
    }

    private void applyMedia(DonationProgram program, MultipartFile thumbnail, MultipartFile[] gallery, boolean appendGallery) {
        String newThumbnail = fileStorageService.store(thumbnail, "programs/thumbnails");
        if (newThumbnail != null) {
            program.setThumbnailPath(newThumbnail);
        }

        List<String> newGallery = fileStorageService.storeAll(gallery, "programs/gallery");
        if (!newGallery.isEmpty()) {
            if (appendGallery && program.getGalleryPaths() != null && !program.getGalleryPaths().isBlank()) {
                List<String> merged = new ArrayList<>(program.getGalleryPathList());
                merged.addAll(newGallery);
                program.setGalleryPaths(String.join(",", merged));
            } else {
                program.setGalleryPaths(String.join(",", newGallery));
            }
        }
    }

    private void validateForm(ProgramCreateForm form) {
        if (form.getTitle() == null || form.getTitle().isBlank()) {
            throw new IllegalArgumentException("Judul program wajib diisi");
        }
        if (form.getExcerpt() == null || form.getExcerpt().isBlank()) {
            throw new IllegalArgumentException("Deskripsi singkat wajib diisi");
        }
        if (form.getFullDescription() == null || form.getFullDescription().isBlank()) {
            throw new IllegalArgumentException("Deskripsi lengkap wajib diisi");
        }
        if (form.getBeneficiary() == null || form.getBeneficiary().isBlank()) {
            throw new IllegalArgumentException("Penerima manfaat wajib diisi");
        }
        if (form.getFundingType() == null) {
            throw new IllegalArgumentException("Tipe pendanaan wajib dipilih");
        }
        if (form.getSpecificCategory() == null) {
            throw new IllegalArgumentException("Kategori spesifik wajib dipilih");
        }
        if (!form.isOpenEnded()) {
            if (form.getTargetAmount() == null || form.getTargetAmount().signum() <= 0) {
                throw new IllegalArgumentException("Target dana wajib diisi jika program tidak open-ended");
            }
        }
        if (form.getStartDate() != null && form.getEndDate() != null
                && form.getEndDate().isBefore(form.getStartDate())) {
            throw new IllegalArgumentException("Tanggal berakhir tidak boleh sebelum tanggal mulai");
        }
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private String normalizeQuickAmounts(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        return raw.replaceAll("\\s+", "");
    }
}
