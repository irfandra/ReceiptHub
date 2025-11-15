package com.receipthub.event;

import org.springframework.context.ApplicationEvent;

import com.receipthub.model.ReimbursementRequest;

import lombok.Getter;

@Getter
public class ReimbursementApprovedEvent extends ApplicationEvent {
    
    private final ReimbursementRequest reimbursementRequest;
    
    public ReimbursementApprovedEvent(Object source, ReimbursementRequest reimbursementRequest) {
        super(source);
        this.reimbursementRequest = reimbursementRequest;
    }
}
