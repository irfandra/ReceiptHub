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
public class OcrDataResponse {
    private String merchantName;
    private Double amount;
    private LocalDateTime transactionDate;
}
