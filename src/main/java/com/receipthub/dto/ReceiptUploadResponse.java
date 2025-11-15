package com.receipthub.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ReceiptUploadResponse {
    private Long receiptId;
    private String imageUrl;
    private String message;
    private LocalDateTime uploadedAt;
    private Double amount;
    private String merchantName;
}
