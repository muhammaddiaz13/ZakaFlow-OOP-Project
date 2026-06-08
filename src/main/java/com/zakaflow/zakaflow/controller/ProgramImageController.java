package com.zakaflow.zakaflow.controller;

import com.zakaflow.zakaflow.model.DonationProgram;
import com.zakaflow.zakaflow.service.DonationProgramService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/programs/image")
@RequiredArgsConstructor
public class ProgramImageController {

    private final DonationProgramService donationProgramService;

    @GetMapping("/{id}")
    @Transactional(readOnly = true)
    public ResponseEntity<byte[]> getImage(@PathVariable Long id) {
        DonationProgram program = donationProgramService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Program tidak ditemukan"));

        if (program.getImageData() == null || program.getImageContentType() == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(program.getImageContentType()))
                .body(program.getImageData());
    }
}

