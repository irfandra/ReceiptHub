package com.receipthub.service;

import java.util.List;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.receipthub.model.ReimbursementRequest;
import com.receipthub.model.User;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NotificationService {
    
    private final UserService userService;
    private final TelegramBotService telegramBotService;
    
    @Async
    public void notifyAdmins(ReimbursementRequest reimbursement) {
        List<User> admins = userService.getAdminUsers();
        
        String message = """
            🔔 New Reimbursement Request
            
            Employee: %s
            Merchant: %s
            Amount: $%.2f
            Description: %s
            
            Please review in the admin dashboard."""
            .formatted(
                reimbursement.getSubmittedBy().getName(),
                reimbursement.getReceipt().getMerchantName(),
                reimbursement.getRequestedAmount(),
                reimbursement.getDescription()
            );
        
        for (User admin : admins) {
            try {
                telegramBotService.sendMessage(admin.getTelegramChatId(), message);
            } catch (Exception e) {
                // Silently fail if notification fails
            }
        }
    }
    
    @Async
    public void notifyEmployee(ReimbursementRequest reimbursement, String decision) {
        User employee = reimbursement.getSubmittedBy();
        
        String emoji = decision.equals("APPROVED") ? "✅" : "❌";
        String message = """
            %s Reimbursement Request %s
            
            Request ID: %d
            Merchant: %s
            Amount: $%.2f
            Reviewed by: %s
            Notes: %s"""
            .formatted(
                emoji,
                decision,
                reimbursement.getId(),
                reimbursement.getReceipt().getMerchantName(),
                reimbursement.getRequestedAmount(),
                reimbursement.getReviewedBy().getName(),
                reimbursement.getReviewNotes() != null ? reimbursement.getReviewNotes() : "No notes provided"
            );
        
        try {
            telegramBotService.sendMessage(employee.getTelegramChatId(), message);
        } catch (Exception e) {
            // Silently fail if notification fails
        }
    }
}
