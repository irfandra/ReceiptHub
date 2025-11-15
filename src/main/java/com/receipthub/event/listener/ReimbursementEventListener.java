package com.receipthub.event.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import com.receipthub.event.ReimbursementApprovedEvent;
import com.receipthub.event.ReimbursementRejectedEvent;
import com.receipthub.event.ReimbursementSubmittedEvent;
import com.receipthub.service.NotificationService;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ReimbursementEventListener {
    
    private static final Logger log = LoggerFactory.getLogger(ReimbursementEventListener.class);
    
    private final NotificationService notificationService;
    
    @Async
    @EventListener
    public void handleReimbursementSubmitted(ReimbursementSubmittedEvent event) {
        log.info("Processing ReimbursementSubmittedEvent for request ID: {}", 
            event.getReimbursementRequest().getId());
        notificationService.notifyAdmins(event.getReimbursementRequest());
    }
    
    @Async
    @EventListener
    public void handleReimbursementApproved(ReimbursementApprovedEvent event) {
        log.info("Processing ReimbursementApprovedEvent for request ID: {}", 
            event.getReimbursementRequest().getId());
        notificationService.notifyEmployee(event.getReimbursementRequest(), "APPROVED");
    }
    
    @Async
    @EventListener
    public void handleReimbursementRejected(ReimbursementRejectedEvent event) {
        log.info("Processing ReimbursementRejectedEvent for request ID: {}", 
            event.getReimbursementRequest().getId());
        notificationService.notifyEmployee(event.getReimbursementRequest(), "REJECTED");
    }
}
