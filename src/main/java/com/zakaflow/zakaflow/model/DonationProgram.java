package com.zakaflow.zakaflow.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Entity
@Table(name = "donation_programs")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DonationProgram {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(length = 500)
    private String excerpt;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(columnDefinition = "TEXT")
    private String beneficiary;

    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private FundingType fundingType;

    @Enumerated(EnumType.STRING)
    @Column(length = 40)
    private SpecificCategory specificCategory;

    @Column(length = 500)
    private String asnafValues;

    @Enumerated(EnumType.STRING)
    @Column(length = 40)
    private ZakatType zakatType;

    @Column(precision = 19, scale = 2)
    private BigDecimal targetAmount;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal currentAmount = BigDecimal.ZERO;

    @Column(nullable = false)
    private boolean openEnded = false;

    private LocalDate startDate;

    private LocalDate endDate;

    @Column(length = 500)
    private String thumbnailPath;

    @Column(columnDefinition = "TEXT")
    private String galleryPaths;

    @Column(length = 500)
    private String videoUrl;

    @Column(precision = 19, scale = 2)
    private BigDecimal minDonationAmount;

    @Column(length = 200)
    private String quickAmounts;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ProgramStatus status = ProgramStatus.DRAFT;

    @Column(length = 150)
    private String personInCharge;

    @Column(nullable = false)
    private boolean isCompleted = false;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Category category;

    @OneToMany(mappedBy = "program", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<DonationTransaction> transactions = new ArrayList<>();

    public List<Asnaf> getAsnafList() {
        if (asnafValues == null || asnafValues.isBlank()) {
            return List.of();
        }
        return Arrays.stream(asnafValues.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(Asnaf::valueOf)
                .collect(Collectors.toList());
    }

    public void setAsnafList(List<Asnaf> asnafList) {
        if (asnafList == null || asnafList.isEmpty()) {
            this.asnafValues = null;
        } else {
            this.asnafValues = asnafList.stream()
                    .map(Enum::name)
                    .collect(Collectors.joining(","));
        }
    }

    public List<String> getGalleryPathList() {
        if (galleryPaths == null || galleryPaths.isBlank()) {
            return List.of();
        }
        return Arrays.stream(galleryPaths.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }

    public List<BigDecimal> getQuickAmountList() {
        if (quickAmounts == null || quickAmounts.isBlank()) {
            return List.of();
        }
        return Arrays.stream(quickAmounts.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(BigDecimal::new)
                .collect(Collectors.toList());
    }

    /**
     * Program tampil di beranda/katalog jika status ACTIVE dan dalam periode tanggal (jika diisi).
     * Target tercapai (isCompleted) tidak menyembunyikan program — donasi saja yang ditutup.
     */
    public boolean isPubliclyVisible() {
        return getPublicVisibilityBlockReason().isEmpty();
    }

    public Optional<String> getPublicVisibilityBlockReason() {
        if (status != ProgramStatus.ACTIVE) {
            return Optional.of("Status harus Published / Active (saat ini: "
                    + (status != null ? status.getLabel() : "tidak diatur") + ")");
        }
        LocalDate today = LocalDate.now();
        if (startDate != null && today.isBefore(startDate)) {
            return Optional.of("Belum mulai (tanggal mulai: " + startDate + ")");
        }
        if (endDate != null && today.isAfter(endDate)) {
            return Optional.of("Sudah berakhir (deadline: " + endDate + ")");
        }
        return Optional.empty();
    }
}
