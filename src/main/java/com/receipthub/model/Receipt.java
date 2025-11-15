package com.receipthub.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "receipts")
@Getter
@Setter
public class Receipt {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String imageUrl;
    
    
    private String merchantName;
    
    private Double amount;
    
    private LocalDateTime transactionDate;
    
    @Column(nullable = false)
    private LocalDateTime uploadedAt;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OcrStatus ocrStatus;
    
    @OneToOne(mappedBy = "receipt")
    private ReimbursementRequest reimbursementRequest;
    
    @PrePersist
    protected void onCreate() {
        uploadedAt = LocalDateTime.now();
        if (ocrStatus == null) {
            ocrStatus = OcrStatus.PENDING;
        }
    }
    
    public enum OcrStatus {
        PENDING,
        COMPLETED,
        FAILED
    }
}
