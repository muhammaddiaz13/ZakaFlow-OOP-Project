package com.zakaflow.zakaflow.controller;

import com.zakaflow.zakaflow.model.Category;
import com.zakaflow.zakaflow.model.DonationProgram;
import com.zakaflow.zakaflow.service.CategoryService;
import com.zakaflow.zakaflow.service.DonationProgramService;
import com.zakaflow.zakaflow.service.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;

@Controller
@RequestMapping("/admin/programs")
@RequiredArgsConstructor
public class AdminProgramController {

    private final DonationProgramService donationProgramService;
    private final CategoryService categoryService;
    private final FileStorageService fileStorageService;

    @GetMapping
    public String list(Model model) {
        model.addAttribute("programs", donationProgramService.findAll());
        return "admin/programs/list";
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("program", new DonationProgram());
        model.addAttribute("categories", categoryService.findAll());
        model.addAttribute("isEdit", false);
        return "admin/programs/form";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        DonationProgram program = donationProgramService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Program tidak ditemukan"));
        model.addAttribute("program", program);
        model.addAttribute("categories", categoryService.findAll());
        model.addAttribute("isEdit", true);
        return "admin/programs/form";
    }

    @PostMapping("/save")
    public String save(
            @RequestParam(required = false) Long id,
            @RequestParam String title,
            @RequestParam String description,
            @RequestParam BigDecimal targetAmount,
            @RequestParam Long categoryId,
            @RequestParam(value = "image", required = false) MultipartFile image,
            @RequestParam(value = "removeImage", defaultValue = "false") boolean removeImage,
            RedirectAttributes redirectAttributes) {
        if (title.isBlank()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Judul program wajib diisi.");
            return id != null ? "redirect:/admin/programs/" + id + "/edit" : "redirect:/admin/programs/new";
        }
        if (targetAmount == null || targetAmount.signum() <= 0) {
            redirectAttributes.addFlashAttribute("errorMessage", "Target donasi harus lebih dari 0.");
            return id != null ? "redirect:/admin/programs/" + id + "/edit" : "redirect:/admin/programs/new";
        }

        Category category = categoryService.findById(categoryId)
                .orElseThrow(() -> new IllegalArgumentException("Kategori tidak ditemukan"));

        DonationProgram program = id != null
                ? donationProgramService.findById(id).orElse(new DonationProgram())
                : new DonationProgram();

        if (id == null) {
            program.setCurrentAmount(BigDecimal.ZERO);
            program.setCompleted(false);
        }

        program.setTitle(title.trim());
        program.setDescription(description != null ? description.trim() : "");
        program.setTargetAmount(targetAmount);
        program.setCategory(category);

        try {
            if (removeImage) {
                fileStorageService.deleteIfExists(program.getImagePath());
                program.setImagePath(null);
            }
            if (image != null && !image.isEmpty()) {
                String storedName = fileStorageService.storeProgramImage(image);
                if (storedName != null) {
                    fileStorageService.deleteIfExists(program.getImagePath());
                    program.setImagePath(storedName);
                }
            }
        } catch (IllegalArgumentException | IllegalStateException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            return id != null ? "redirect:/admin/programs/" + id + "/edit" : "redirect:/admin/programs/new";
        }

        donationProgramService.save(program);
        redirectAttributes.addFlashAttribute("successMessage", "Program berhasil disimpan.");
        return "redirect:/admin/programs";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            donationProgramService.findById(id).ifPresent(p -> fileStorageService.deleteIfExists(p.getImagePath()));
            donationProgramService.deleteById(id);
            redirectAttributes.addFlashAttribute("successMessage", "Program berhasil dihapus.");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", "Program tidak dapat dihapus (mungkin masih memiliki transaksi).");
        }
        return "redirect:/admin/programs";
    }
}
