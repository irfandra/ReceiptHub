package com.receipthub.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import com.receipthub.dto.OcrDataResponse;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OcrService {
    
    private static final Logger log = LoggerFactory.getLogger(OcrService.class);
    
    private static final String OCR_API_URL = "https://api.ocr.space/parse/image";
    
    private final StorageService storageService;
    
    @Value("${ocr.api.key}")
    private String apiKey;
    
private static final Pattern AMOUNT_PATTERN = Pattern.compile(
    "(?:SUB\\s+TOTAL|SUBTOTAL|TOTAL|AMOUNT|GRAND\\s+TOTAL|BALANCE\\s+DUE|TOTAL\\s+DUE|PAID)" + // SUB TOTAL with mandatory space
    "\\s*:?\\s*" +                                                      // Optional colon with spaces
    "\\$?\\s*" +                                                        // Optional $ with spaces
    // Capture Group 1: The Amount - handles numbers with optional spaces before decimal
    "((?:[0-9]{1,3}(?:,\\s?[0-9]{3})*|[0-9]+)\\.?\\s?[0-9]{2})",
    Pattern.CASE_INSENSITIVE
);

    private static final String MONTH_REGEX = "(?:Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec)[a-z]*";

private static final Pattern DATE_PATTERN = Pattern.compile(
    // 1. Matches: dd/mm/yyyy, mm/dd/yyyy, dd-mm-yyyy, dd.mm.yyyy
    "(\\d{1,2}[/.-]\\d{1,2}[/.-]\\d{2,4})|" +  // <-- FIXED
    
    // 2. Matches: yyyy-mm-dd, yyyy/mm/dd, yyyy.mm.dd
    "(\\d{4}[/.-]\\d{1,2}[/.-]\\d{1,2})|" +  // <-- FIXED
    
    // 3. Matches: Jan 01, 2025 or January 1st, 2025
    "(" + MONTH_REGEX + "[\\s,.]+\\d{1,2}(?:st|nd|rd|th)?[\\s,.]+\\d{2,4})|" +
    
    // 4. Matches: 01 Jan 2025 or 1-Jan-2025 (This one was already correct)
    "(\\d{1,2}[\\s,.-]+" + MONTH_REGEX + "[\\s,.-]+\\d{2,4})",
    
    Pattern.CASE_INSENSITIVE
);
    
    @CircuitBreaker(name = "ocrService", fallbackMethod = "ocrFallback")
    public OcrDataResponse extractReceiptData(String objectName) throws Exception {
        log.info("Extracting receipt data from MinIO object: {}", objectName);
        
        // Fetch image from MinIO cloud storage
        byte[] imageBytes = storageService.getFile(objectName);
        
        // Convert to base64
        String base64Image = Base64.getEncoder().encodeToString(imageBytes);
        
        // Call OCR.space API - let exceptions propagate to circuit breaker
        String extractedText = callOcrApi(base64Image);
        
        if (extractedText != null && !extractedText.isEmpty()) {
            // Parse the extracted text
            return parseReceiptText(extractedText);
        } else {
            log.warn("No text extracted from OCR");
            throw new RuntimeException("No text extracted from OCR");
        }
    }

    private String callOcrApi(String base64Image) {
        try {
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("base64Image", "data:image/jpeg;base64," + base64Image);
            body.add("apikey", apiKey);
            body.add("language", "eng");
            body.add("isOverlayRequired", "false");
            body.add("detectOrientation", "true");
            body.add("scale", "true");
            body.add("OCREngine", "2"); // Use OCR Engine 2 for better accuracy

            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

            @SuppressWarnings("unchecked")
            ResponseEntity<Map<String, Object>> response = (ResponseEntity<Map<String, Object>>) (ResponseEntity<?>) restTemplate.exchange(
                OCR_API_URL,
                HttpMethod.POST,
                requestEntity,
                Map.class
            );
            
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                Map<String, Object> responseBody = response.getBody();
                
                if (responseBody != null && responseBody.containsKey("ParsedResults")) {
                    @SuppressWarnings("unchecked")
                    List<Map<String, Object>> parsedResults = (List<Map<String, Object>>) responseBody.get("ParsedResults");
                    
                    if (!parsedResults.isEmpty()) {
                        Map<String, Object> firstResult = parsedResults.getFirst();

                        return (String) firstResult.get("ParsedText");
                    }
                }
                
                // Check for errors
                if (responseBody != null && responseBody.containsKey("ErrorMessage")) {
                    @SuppressWarnings("unchecked")
                    List<String> errors = (List<String>) responseBody.get("ErrorMessage");
                    if (!errors.isEmpty()) {
                        log.error("OCR API Error: {}", errors.getFirst());
                    }
                }
            }

        } catch (org.springframework.web.client.RestClientException | IllegalArgumentException e) {
            log.error("Error calling OCR API: {}", e.getMessage(), e);
        }
        
        return null;
    }

    private OcrDataResponse parseReceiptText(String text) {
        OcrDataResponse response = new OcrDataResponse();
        
        // Extract merchant name (usually in first few lines)
        String merchantName = extractMerchantName(text);
        response.setMerchantName(merchantName);
        
        // Extract amount
        Double amount = extractAmount(text);
        response.setAmount(amount);
        
        // Extract transaction date from receipt
        LocalDateTime date = extractDate(text);
        response.setTransactionDate(date);
        
        log.info("Parsed Receipt - Merchant: {}, Amount: ${}, Date: {}", merchantName, amount, date);
        
        return response;
    }
    
    private String extractMerchantName(String text) {
        String[] lines = text.split("\\r?\\n");
        
        for (String line : lines) {
            line = line.trim();
            
            // Skip empty lines, pure numbers, dates, and very short lines
            if (line.matches("^[0-9\\s]+$") ||
                    line.matches(".*\\d{2}[/-]\\d{2}[/-]\\d{2,4}.*") ||
                    line.length() < 3) {
                continue;
            }
            
            // Look for common merchant indicators
            if (line.toLowerCase().contains("store") || 
                line.toLowerCase().contains("mart") ||
                line.toLowerCase().contains("shop") ||
                line.toLowerCase().contains("restaurant") ||
                line.toLowerCase().contains("cafe") ||
                line.toLowerCase().contains("market") ||
                line.length() > 5) {
                return line;
            }
        }
        
        // If no merchant found, return first non-empty line
        for (String line : lines) {
            line = line.trim();
            if (line.length() > 3) {
                return line;
            }
        }
        
        return "Unknown Merchant";
    }
    
    private Double extractAmount(String text) {
        Matcher matcher = AMOUNT_PATTERN.matcher(text);
        
        double maxAmount = 0.0;
        
        while (matcher.find()) {
            try {
                String amountStr = matcher.group(1)
                    .replace(" ", "")
                    .replace(",", "");
                double amount = Double.parseDouble(amountStr);
                
                if (amount > maxAmount) {
                    maxAmount = amount;
                }
            } catch (NumberFormatException e) {
                // Continue searching
            }
        }
        
        // Simple fallback - find the largest amount
        if (maxAmount == 0.0) {
            Pattern fallbackPattern = Pattern.compile("([0-9]+\\.\\s?[0-9]{2})");
            Matcher fallbackMatcher = fallbackPattern.matcher(text);
            
            while (fallbackMatcher.find()) {
                try {
                    String amountStr = fallbackMatcher.group(1).replace(" ", "");
                    double amount = Double.parseDouble(amountStr);
                    
                    if (amount > maxAmount && amount < 10000) {
                        maxAmount = amount;
                    }
                } catch (NumberFormatException e) {
                    // Continue
                }
            }
        }
        
        return maxAmount > 0 ? maxAmount : 0.0;
    }
    
    private LocalDateTime extractDate(String text) {
        Matcher matcher = DATE_PATTERN.matcher(text);
        
        if (matcher.find()) {
            String dateStr = matcher.group(0);
            
            // Try different date formats
            List<DateTimeFormatter> formatters = List.of(
                DateTimeFormatter.ofPattern("dd/MM/yyyy"),
                DateTimeFormatter.ofPattern("MM/dd/yyyy"),
                DateTimeFormatter.ofPattern("dd-MM-yyyy"),
                DateTimeFormatter.ofPattern("MM-dd-yyyy"),
                DateTimeFormatter.ofPattern("yyyy-MM-dd"),
                DateTimeFormatter.ofPattern("yyyy/MM/dd"),
                DateTimeFormatter.ofPattern("dd/MM/yy"),
                DateTimeFormatter.ofPattern("dd-MM-yy"),
                DateTimeFormatter.ofPattern("MM/dd/yy"),
                DateTimeFormatter.ofPattern("MM-dd-yy")
            );
            
            for (DateTimeFormatter formatter : formatters) {
                try {
                    return LocalDateTime.of(
                        java.time.LocalDate.parse(dateStr, formatter),
                        java.time.LocalTime.now()
                    );
                } catch (Exception e) {
                    // Try next format
                }
            }
        }
        
        // Default to current date if not found
        return LocalDateTime.now();
    }
    
    private OcrDataResponse createFallbackResponse() {
        OcrDataResponse response = new OcrDataResponse();
        response.setMerchantName("OCR Extraction Failed");
        response.setAmount(0.0);
        response.setTransactionDate(LocalDateTime.now());
        return response;
    }
    
    @SuppressWarnings("unused")
    private OcrDataResponse ocrFallback(String imagePath, Exception e) {
        log.warn("OCR Circuit Breaker activated: {}", e.getMessage());
        return createFallbackResponse();
    }
}
