package com.zakaflow.zakaflow.controller;

import com.zakaflow.zakaflow.model.Category;
import com.zakaflow.zakaflow.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/categories")
@RequiredArgsConstructor
public class AdminCategoryController {

    private final CategoryService categoryService;

    @GetMapping
    public String list(Model model) {
        model.addAttribute("categories", categoryService.findAll());
        return "admin/categories/list";
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("category", new Category());
        model.addAttribute("isEdit", false);
        return "admin/categories/form";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        Category category = categoryService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Kategori tidak ditemukan"));
        model.addAttribute("category", category);
        model.addAttribute("isEdit", true);
        return "admin/categories/form";
    }

    @PostMapping("/save")
    public String save(
            @RequestParam(required = false) Long id,
            @RequestParam String name,
            @RequestParam(required = false) String description,
            RedirectAttributes redirectAttributes) {
        if (name.isBlank()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Nama kategori wajib diisi.");
            return id != null ? "redirect:/admin/categories/" + id + "/edit" : "redirect:/admin/categories/new";
        }

        Category category = id != null
                ? categoryService.findById(id).orElse(new Category())
                : new Category();

        category.setName(name.trim());
        category.setDescription(description != null ? description.trim() : "");

        categoryService.save(category);
        redirectAttributes.addFlashAttribute("successMessage", "Kategori berhasil disimpan.");
        return "redirect:/admin/categories";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            categoryService.deleteById(id);
            redirectAttributes.addFlashAttribute("successMessage", "Kategori berhasil dihapus.");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", "Kategori tidak dapat dihapus (masih digunakan program).");
        }
        return "redirect:/admin/categories";
    }
}
