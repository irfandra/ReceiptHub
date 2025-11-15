package com.receipthub.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.receipthub.dto.ReimbursementResponse;
import com.receipthub.dto.ReimbursementSubmitRequest;
import com.receipthub.event.ReimbursementApprovedEvent;
import com.receipthub.event.ReimbursementRejectedEvent;
import com.receipthub.event.ReimbursementSubmittedEvent;
import com.receipthub.model.Receipt;
import com.receipthub.model.ReimbursementRequest;
import com.receipthub.model.User;
import com.receipthub.repository.ReimbursementRequestRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReimbursementService {
    
    private final ReimbursementRequestRepository reimbursementRequestRepository;
    private final ReceiptService receiptService;
    private final UserService userService;
    private final ApplicationEventPublisher eventPublisher;
    
    @Transactional
    public void submitReimbursement(ReimbursementSubmitRequest request) {
        Receipt receipt = receiptService.getReceiptById(request.getReceiptId());
        User user = userService.getUserById(request.getUserId());
        
        ReimbursementRequest reimbursement = new ReimbursementRequest();
        reimbursement.setReceipt(receipt);
        reimbursement.setSubmittedBy(user);
        reimbursement.setRequestedAmount(request.getRequestedAmount());
        reimbursement.setDescription(request.getDescription());
        reimbursement.setStatus(ReimbursementRequest.RequestStatus.PENDING);
        
        reimbursement = reimbursementRequestRepository.save(reimbursement);
        
        eventPublisher.publishEvent(new ReimbursementSubmittedEvent(this, reimbursement));

        convertToResponse(reimbursement);
    }
    
    @Transactional
    public void approveReimbursement(Long reimbursementId, Long adminId, String notes) {
        ReimbursementRequest reimbursement = reimbursementRequestRepository.findById(reimbursementId)
            .orElseThrow(() -> new RuntimeException("Reimbursement Request not found with id: " + reimbursementId));
        
        User admin = userService.getUserById(adminId);
        
        reimbursement.setStatus(ReimbursementRequest.RequestStatus.APPROVED);
        reimbursement.setReviewedBy(admin);
        reimbursement.setReviewedAt(LocalDateTime.now());
        reimbursement.setReviewNotes(notes);
        
        reimbursement = reimbursementRequestRepository.save(reimbursement);
        
        log.info("Reimbursement {} approved by admin {}", reimbursementId, adminId);
        
        eventPublisher.publishEvent(new ReimbursementApprovedEvent(this, reimbursement));

        convertToResponse(reimbursement);
    }
    
    @Transactional
    public void rejectReimbursement(Long reimbursementId, Long adminId, String notes) {
        ReimbursementRequest reimbursement = reimbursementRequestRepository.findById(reimbursementId)
            .orElseThrow(() -> new RuntimeException("Reimbursement Request not found with id: " + reimbursementId));
        
        User admin = userService.getUserById(adminId);
        
        reimbursement.setStatus(ReimbursementRequest.RequestStatus.REJECTED);
        reimbursement.setReviewedBy(admin);
        reimbursement.setReviewedAt(LocalDateTime.now());
        reimbursement.setReviewNotes(notes);
        
        reimbursement = reimbursementRequestRepository.save(reimbursement);
        
        log.info("Reimbursement {} rejected by admin {}", reimbursementId, adminId);
        
        eventPublisher.publishEvent(new ReimbursementRejectedEvent(this, reimbursement));

        convertToResponse(reimbursement);
    }
    
    @Transactional
    public void editReimbursement(Long requestId, String merchantName, Double amount, String transactionDate, String description) {
        ReimbursementRequest reimbursement = reimbursementRequestRepository.findById(requestId)
            .orElseThrow(() -> new RuntimeException("Reimbursement Request not found with id: " + requestId));
        
        // Update amount
        reimbursement.setRequestedAmount(amount);
        
        // Update description
        if (description != null && !description.trim().isEmpty()) {
            reimbursement.setDescription(description);
        }
        
        // Update merchant name and transaction date in the associated receipt
        Receipt receipt = reimbursement.getReceipt();
        
        if (merchantName != null && !merchantName.trim().isEmpty()) {
            receipt.setMerchantName(merchantName);
        }
        
        if (transactionDate != null && !transactionDate.trim().isEmpty()) {
            LocalDate date = LocalDate.parse(transactionDate, DateTimeFormatter.ISO_LOCAL_DATE);
            receipt.setTransactionDate(LocalDateTime.of(date, java.time.LocalTime.MIDNIGHT));
        }
        
        reimbursement = reimbursementRequestRepository.save(reimbursement);

        convertToResponse(reimbursement);
    }
    
    public Page<ReimbursementResponse> getAllReimbursements(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "submittedAt"));
        Page<ReimbursementRequest> reimbursementPage = reimbursementRequestRepository.findAll(pageable);
        return reimbursementPage.map(this::convertToResponse);
    }
    
    public Page<ReimbursementResponse> getReimbursementsByStatus(ReimbursementRequest.RequestStatus status, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "submittedAt"));
        return reimbursementRequestRepository.findByStatus(status, pageable).map(this::convertToResponse);
    }
    
    public Page<ReimbursementResponse> getReimbursementsByUserId(Long userId, int page, int size) {
        User user = userService.getUserById(userId);
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "submittedAt"));
        return reimbursementRequestRepository.findBySubmittedBy(user, pageable).map(this::convertToResponse);
    }
    
    public Page<ReimbursementResponse> getReimbursementsByUserIdAndStatus(Long userId, String statusStr, int page, int size) {
        User user = userService.getUserById(userId);
        ReimbursementRequest.RequestStatus status = ReimbursementRequest.RequestStatus.valueOf(statusStr);
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "submittedAt"));
        return reimbursementRequestRepository.findBySubmittedByAndStatus(user, status, pageable).map(this::convertToResponse);
    }
    
    private ReimbursementResponse convertToResponse(ReimbursementRequest reimbursement) {
        ReimbursementResponse response = new ReimbursementResponse();
        response.setId(reimbursement.getId());
        response.setReceiptId(reimbursement.getReceipt().getId());
        response.setReceiptImageUrl(reimbursement.getReceipt().getImageUrl());
        response.setMerchantName(reimbursement.getReceipt().getMerchantName());
        response.setRequestedAmount(reimbursement.getRequestedAmount());
        response.setDescription(reimbursement.getDescription());
        response.setStatus(reimbursement.getStatus().name());
        response.setSubmittedByName(reimbursement.getSubmittedBy().getName());
        response.setSubmittedByEmail(reimbursement.getSubmittedBy().getEmail());
        response.setSubmittedAt(reimbursement.getSubmittedAt());
        response.setTransactionDate(reimbursement.getReceipt().getTransactionDate());
        response.setReviewedAt(reimbursement.getReviewedAt());
        if (reimbursement.getReviewedBy() != null) {
            response.setReviewedByName(reimbursement.getReviewedBy().getName());
        }
        
        response.setReviewNotes(reimbursement.getReviewNotes());
        
        return response;
    }
}

