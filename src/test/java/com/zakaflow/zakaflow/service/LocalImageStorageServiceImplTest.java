package com.zakaflow.zakaflow.service;

import com.zakaflow.zakaflow.service.impl.LocalImageStorageServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class LocalImageStorageServiceImplTest {

    @TempDir
    Path tempDir;

    private LocalImageStorageServiceImpl imageStorageService;

    @BeforeEach
    void setUp() {
        // Inisialisasi service dengan direktori sementara JUnit untuk isolasi test I/O
        imageStorageService = new LocalImageStorageServiceImpl(tempDir.toString());
    }

    @Test
    void whenFileIsEmpty_thenThrowException() {
        MultipartFile emptyFile = new MockMultipartFile("image", "empty.jpg", "image/jpeg", new byte[0]);

        Exception exception = assertThrows(IllegalArgumentException.class, () -> 
                imageStorageService.storeImage(emptyFile)
        );
        assertTrue(exception.getMessage().contains("kosong") || exception.getMessage().contains("tidak ditemukan"));
    }

    @Test
    void whenContentTypeNotSupported_thenThrowException() {
        MultipartFile textFile = new MockMultipartFile("image", "test.txt", "text/plain", "Hello World".getBytes());

        Exception exception = assertThrows(IllegalArgumentException.class, () -> 
                imageStorageService.storeImage(textFile)
        );
        assertTrue(exception.getMessage().contains("tidak didukung") || exception.getMessage().contains("format"));
    }

    @Test
    void whenValidImage_thenConvertAndSaveAsWebP() throws IOException {
        // 1. Buat data gambar PNG dummy di memori (gambar merah ukuran 100x100px)
        BufferedImage bufferedImage = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
        for (int x = 0; x < 100; x++) {
            for (int y = 0; y < 100; y++) {
                bufferedImage.setRGB(x, y, 0xFF0000); // warna merah
            }
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(bufferedImage, "png", baos);
        byte[] imageBytes = baos.toByteArray();

        MultipartFile validFile = new MockMultipartFile("image", "red.png", "image/png", imageBytes);

        // 2. Eksekusi metode penyimpanan & optimasi
        String savedName = imageStorageService.storeImage(validFile);

        // 3. Verifikasi hasil konversi
        assertNotNull(savedName);
        assertTrue(savedName.endsWith(".webp"));

        Path savedFile = tempDir.resolve(savedName);
        assertTrue(Files.exists(savedFile), "File WebP hasil konversi harus ada di disk");
        assertTrue(Files.size(savedFile) > 0, "Ukuran file WebP harus lebih dari 0");
    }
}
