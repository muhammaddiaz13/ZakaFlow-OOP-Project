package com.zakaflow.zakaflow.service.impl;

import com.zakaflow.zakaflow.service.ImageStorageService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;
import java.util.UUID;

/**
 * Implementasi ImageStorageService untuk penyimpanan lokal.
 * Mengonversi gambar JPEG/PNG menjadi WebP menggunakan Java ImageIO + TwelveMonkeys.
 */
@Service
public class LocalImageStorageServiceImpl implements ImageStorageService {

    static {
        try {
            // Registrasi SPI secara manual untuk mengatasi classloader issue pada Spring Boot DevTools
            javax.imageio.spi.IIORegistry registry = javax.imageio.spi.IIORegistry.getDefaultInstance();
            registry.registerServiceProvider(new com.luciad.imageio.webp.WebPImageReaderSpi());
            registry.registerServiceProvider(new com.luciad.imageio.webp.WebPImageWriterSpi());
        } catch (Throwable t) {
            System.err.println("Peringatan: Gagal registrasi WebP ImageIO SPI secara manual. " + t.getMessage());
        }
    }

    // Hanya menerima format standard gambar untuk dikompresi
    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "image/jpeg", "image/jpg", "image/png", "image/webp"
    );

    private final Path uploadRoot;

    public LocalImageStorageServiceImpl(@Value("${zakaflow.upload.dir:uploads}") String uploadDir) {
        this.uploadRoot = Paths.get(uploadDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.uploadRoot);
        } catch (IOException ex) {
            throw new IllegalStateException("Tidak dapat menginisialisasi direktori penyimpanan: " + this.uploadRoot, ex);
        }
    }

    @Override
    public String storeImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Berkas gambar kosong atau tidak ditemukan.");
        }

        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType.toLowerCase())) {
            throw new IllegalArgumentException("Format berkas tidak didukung. Silakan unggah gambar berformat JPEG atau PNG.");
        }

        try {
            // 1. Baca berkas input ke dalam BufferedImage
            BufferedImage image = ImageIO.read(file.getInputStream());
            if (image == null) {
                throw new IllegalArgumentException("Berkas rusak atau bukan merupakan gambar yang valid.");
            }

            // 2. Hasilkan nama berkas unik berbasis UUID dengan ekstensi .webp
            String uniqueFileName = UUID.randomUUID().toString() + ".webp";
            Path targetPath = this.uploadRoot.resolve(uniqueFileName).normalize();

            // Keamanan: Validasi path agar tidak keluar dari direktori root (mencegah Directory Traversal)
            if (!targetPath.startsWith(this.uploadRoot)) {
                throw new IllegalArgumentException("Nama file tidak aman.");
            }

            // 3. Konversi dan simpan gambar ke format WebP menggunakan ImageIO.write
            // formatName "webp" didukung secara langsung setelah TwelveMonkeys didaftarkan sebagai dependensi
            boolean writeSuccess = ImageIO.write(image, "webp", targetPath.toFile());
            if (!writeSuccess) {
                throw new IllegalStateException("Gagal menyimpan gambar. Format WebP tidak didukung oleh sistem (pastikan dependensi TwelveMonkeys aktif).");
            }

            return uniqueFileName;
        } catch (IOException ex) {
            throw new IllegalStateException("Terjadi kesalahan I/O saat memproses konversi gambar.", ex);
        }
    }

    @Override
    public byte[] convertToWebp(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Berkas gambar kosong atau tidak ditemukan.");
        }

        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType.toLowerCase())) {
            throw new IllegalArgumentException("Format berkas tidak didukung. Silakan unggah gambar berformat JPEG atau PNG.");
        }

        try {
            // Membaca file gambar ke dalam BufferedImage
            BufferedImage image = ImageIO.read(file.getInputStream());
            if (image == null) {
                throw new IllegalArgumentException("Berkas rusak atau bukan merupakan gambar yang valid.");
            }

            // Konversi gambar ke format WebP di dalam memori
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            boolean success = ImageIO.write(image, "webp", baos);
            if (!success) {
                throw new IllegalStateException("Gagal melakukan konversi. Format WebP tidak didukung.");
            }

            return baos.toByteArray();
        } catch (IOException ex) {
            throw new IllegalStateException("Terjadi kesalahan I/O saat mengonversi gambar ke WebP.", ex);
        }
    }
}


