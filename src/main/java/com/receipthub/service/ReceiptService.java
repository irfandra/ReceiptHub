package com.receipthub.service;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.receipthub.dto.OcrDataResponse;
import com.receipthub.dto.ReceiptUploadResponse;
import com.receipthub.model.Receipt;
import com.receipthub.repository.ReceiptRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReceiptService {
    
    private static final Logger log = LoggerFactory.getLogger(ReceiptService.class);
    
    private final ReceiptRepository receiptRepository;
    private final OcrService ocrService;
    private final StorageService storageService;
    
    /**
     * Upload receipt from Telegram bot (byte array).
     * Processes OCR synchronously to return merchant name and amount immediately.
     */
    public ReceiptUploadResponse uploadReceiptFromBytes(byte[] fileData, String fileName) throws IOException {
        // Upload file to MinIO cloud storage
        String objectName = storageService.uploadFile(fileData, fileName);
        
        Receipt receipt = new Receipt();
        receipt.setImageUrl(objectName);  // Store object name (e.g., "abc-123.jpg")
        receipt.setOcrStatus(Receipt.OcrStatus.PENDING);
        
        receipt = receiptRepository.save(receipt);
        
        // Process OCR synchronously for Telegram uploads to get the amount
        processOcrSync(receipt);
        
        return new ReceiptUploadResponse(
            receipt.getId(),
            receipt.getImageUrl(),
            "Receipt uploaded successfully. OCR processing completed.",
            receipt.getUploadedAt(),
            receipt.getAmount(),
            receipt.getMerchantName()
        );
    }
    
    /**
     * Process OCR synchronously for Telegram uploads to extract merchant, amount, and date.
     */
    private void processOcrSync(Receipt receipt) {
        try {
            OcrDataResponse ocrData = ocrService.extractReceiptData(receipt.getImageUrl());
            
            receipt.setMerchantName(ocrData.getMerchantName());
            receipt.setAmount(ocrData.getAmount());
            receipt.setTransactionDate(ocrData.getTransactionDate());
            receipt.setOcrStatus(Receipt.OcrStatus.COMPLETED);
            
            receiptRepository.save(receipt);
            log.info("OCR processing completed for receipt {}: merchant={}, amount={}", 
                receipt.getId(), receipt.getMerchantName(), receipt.getAmount());
        } catch (Exception e) {
            receipt.setOcrStatus(Receipt.OcrStatus.FAILED);
            receiptRepository.save(receipt);
            log.error("OCR processing failed for receipt {}: {}", receipt.getId(), e.getMessage());
        }
    }
    
    public Receipt getReceiptById(Long id) {
        return receiptRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Receipt not found with id: " + id));
    }
    
    public void deleteReceipt(Long id) {
        try {
            Receipt receipt = receiptRepository.findById(id).orElse(null);
            if (receipt != null) {
                // Delete from database
                receiptRepository.deleteById(id);
                log.info("Receipt deleted: {}", id);
            }
        } catch (Exception e) {
            log.error("Error deleting receipt {}: {}", id, e.getMessage(), e);
        }
    }
}
