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
public class ReimbursementResponse {
    private Long id;
    private Long receiptId;
    private String receiptImageUrl;
    private String merchantName;
    private Double requestedAmount;
    private String description;
    private String status;
    private String submittedByName;
    private String submittedByEmail;
    private LocalDateTime submittedAt;
    private LocalDateTime transactionDate;
    private LocalDateTime reviewedAt;
    private String reviewedByName;
    private String reviewNotes;
    
    public String getImageUrl() {
        return receiptImageUrl;
    }
}
