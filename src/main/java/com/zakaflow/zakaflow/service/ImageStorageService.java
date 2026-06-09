package com.zakaflow.zakaflow.service;

import org.springframework.web.multipart.MultipartFile;

/**
 * Interface untuk mendefinisikan operasi penyimpanan dan optimasi gambar.
 * Abstraksi ini memudahkan migrasi dari penyimpanan lokal ke Cloud Storage di masa depan.
 */
public interface ImageStorageService {

    /**
     * Menyimpan file gambar yang diunggah, mengonversinya menjadi format WebP secara otomatis,
     * dan mengembalikan nama file unik hasil konversi tersebut.
     *
     * @param file berkas gambar yang diunggah (JPEG/PNG/WEBP)
     * @return nama berkas unik yang disimpan dengan ekstensi .webp
     * @throws IllegalArgumentException jika berkas kosong atau bukan format gambar yang valid
     * @throws IllegalStateException jika terjadi kesalahan sistem saat memproses atau menulis berkas
     */
    String storeImage(MultipartFile file);

    /**
     * Mengonversi berkas gambar yang diunggah menjadi array of bytes berformat WebP.
     * Berguna untuk penyimpanan di database (BLOB).
     *
     * @param file berkas gambar asli (JPEG/PNG)
     * @return array of bytes dari gambar hasil konversi WebP
     * @throws IllegalArgumentException jika gambar tidak valid
     */
    byte[] convertToWebp(MultipartFile file);
}
