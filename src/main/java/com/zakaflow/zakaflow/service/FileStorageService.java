package com.zakaflow.zakaflow.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Set;
import java.util.UUID;

@Service
public class FileStorageService {

    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "image/jpeg", "image/png", "image/webp", "image/gif"
    );

    private final Path uploadRoot;

    public FileStorageService(@Value("${zakaflow.upload.dir:uploads}") String uploadDir) {
        this.uploadRoot = Paths.get(uploadDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.uploadRoot);
        } catch (IOException ex) {
            throw new IllegalStateException("Tidak dapat membuat folder upload: " + this.uploadRoot, ex);
        }
    }

    public String storeProgramImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return null;
        }

        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType)) {
            throw new IllegalArgumentException("Format gambar harus JPG, PNG, WEBP, atau GIF.");
        }

        String extension = extensionFromContentType(contentType);
        String storedName = "program-" + UUID.randomUUID() + extension;
        Path target = uploadRoot.resolve(storedName).normalize();

        if (!target.startsWith(uploadRoot)) {
            throw new IllegalArgumentException("Nama file tidak valid.");
        }

        try {
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException ex) {
            throw new IllegalStateException("Gagal menyimpan gambar program.", ex);
        }

        return storedName;
    }

    public void deleteIfExists(String fileName) {
        if (!StringUtils.hasText(fileName)) {
            return;
        }
        try {
            Path target = uploadRoot.resolve(fileName).normalize();
            if (target.startsWith(uploadRoot)) {
                Files.deleteIfExists(target);
            }
        } catch (IOException ignored) {
            // best-effort cleanup
        }
    }

    private String extensionFromContentType(String contentType) {
        return switch (contentType) {
            case "image/png" -> ".png";
            case "image/webp" -> ".webp";
            case "image/gif" -> ".gif";
            default -> ".jpg";
        };
    }
}
