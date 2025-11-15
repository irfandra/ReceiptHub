package com.receipthub.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ReimbursementSubmitRequest {
    
    @NotNull(message = "Receipt ID is required")
    private Long receiptId;
    
    @NotNull(message = "User ID is required")
    private Long userId;
    
    @NotNull(message = "Requested amount is required")
    @Positive(message = "Amount must be positive")
    private Double requestedAmount;
    
    @NotBlank(message = "Description is required")
    @Size(max = 500, message = "Description cannot exceed 500 characters")
    private String description;
}
