package com.zakaflow.zakaflow.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

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

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal targetAmount;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal currentAmount = BigDecimal.ZERO;

    @Column(nullable = false)
    private boolean isCompleted = false;

    @Lob
    @Column(name = "image_data")
    private byte[] imageData;

    @Column(name = "image_content_type", length = 100)
    private String imageContentType;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Category category;

    @OneToMany(mappedBy = "program", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<DonationTransaction> transactions = new ArrayList<>();
}
