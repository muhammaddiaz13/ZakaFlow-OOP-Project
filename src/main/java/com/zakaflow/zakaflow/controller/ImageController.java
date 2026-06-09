package com.zakaflow.zakaflow.controller;

import com.zakaflow.zakaflow.service.ImageStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Controller untuk menangani halaman optimasi upload gambar.
 */
@Controller
@RequestMapping("/image-upload")
@RequiredArgsConstructor
public class ImageController {

    private final ImageStorageService imageStorageService;

    @Value("${zakaflow.upload.dir:uploads}")
    private String uploadDir;

    /**
     * Merender form upload halaman optimasi gambar.
     */
    @GetMapping
    public String uploadPage(Model model) {
        model.addAttribute("activePage", "upload");
        return "upload-page";
    }

    /**
     * Memproses upload file, mengonversi ke WebP, dan menghitung persentase kompresi.
     */
    @PostMapping
    public String handleUpload(@RequestParam("image") MultipartFile file, Model model) {
        model.addAttribute("activePage", "upload");

        if (file.isEmpty()) {
            model.addAttribute("error", "Harap pilih file gambar JPEG/PNG terlebih dahulu.");
            return "upload-page";
        }

        try {
            long originalSizeBytes = file.getSize();

            // 1. Simpan & Konversi via Service
            String storedFileName = imageStorageService.storeImage(file);

            // 2. Ambil ukuran berkas hasil optimasi untuk kalkulasi statistik
            Path optimizedFilePath = Paths.get(uploadDir).resolve(storedFileName).toAbsolutePath().normalize();
            long optimizedSizeBytes = Files.exists(optimizedFilePath) ? Files.size(optimizedFilePath) : 0;

            // 3. Format ukuran berkas agar mudah dibaca manusia
            String originalSizeFormatted = formatBytes(originalSizeBytes);
            String optimizedSizeFormatted = formatBytes(optimizedSizeBytes);

            // 4. Hitung persentase penghematan storage
            double savingsPercent = 0.0;
            if (originalSizeBytes > 0 && optimizedSizeBytes > 0) {
                long difference = originalSizeBytes - optimizedSizeBytes;
                savingsPercent = ((double) difference / originalSizeBytes) * 100.0;
            }

            model.addAttribute("success", "Gambar berhasil diunggah dan dioptimasi!");
            model.addAttribute("fileName", storedFileName);
            model.addAttribute("fileUrl", "/uploads/" + storedFileName);
            model.addAttribute("originalSize", originalSizeFormatted);
            model.addAttribute("optimizedSize", optimizedSizeFormatted);
            model.addAttribute("savingsPercent", String.format("%.1f", Math.max(0, savingsPercent)) + "%");

        } catch (IllegalArgumentException ex) {
            model.addAttribute("error", "Gagal: " + ex.getMessage());
        } catch (Exception ex) {
            model.addAttribute("error", "Terjadi kesalahan internal saat memproses gambar: " + ex.getMessage());
        }

        return "upload-page";
    }

    /**
     * Helper untuk memformat byte menjadi unit KB / MB yang mudah dibaca.
     */
    private String formatBytes(long bytes) {
        if (bytes < 1024) {
            return bytes + " B";
        }
        int exp = (int) (Math.log(bytes) / Math.log(1024));
        char pre = "KMGTPE".charAt(exp - 1);
        return String.format("%.2f %sB", bytes / Math.pow(1024, exp), pre);
    }
}
