package com.receipthub.controller;

import java.security.Principal;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.receipthub.dto.ReimbursementResponse;
import com.receipthub.model.User;
import com.receipthub.service.ReimbursementService;
import com.receipthub.service.UserService;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class EmployeeDashboardController {
    
    private final ReimbursementService reimbursementService;
    private final UserService userService;
    
    @GetMapping("/my-requests")
    public String myRequests(
            @RequestParam(required = false, defaultValue = "ALL") String status,
            @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "10") int size,
            Principal principal,
            Model model) {

        User currentEmployee = userService.getUserByEmail(principal.getName()).orElseThrow();
        model.addAttribute("currentUser", currentEmployee);
        model.addAttribute("pageTitle", "My Requests");

        Page<ReimbursementResponse> reimbursementPage;
        if ("ALL".equals(status)) {
            reimbursementPage = reimbursementService.getReimbursementsByUserId(currentEmployee.getId(), page, size);
        } else {
            reimbursementPage = reimbursementService.getReimbursementsByUserIdAndStatus(
                currentEmployee.getId(), status, page, size);
        }
        

        long pendingCount = reimbursementService.getReimbursementsByUserIdAndStatus(
            currentEmployee.getId(), "PENDING", 0, Integer.MAX_VALUE).getTotalElements();
        long approvedCount = reimbursementService.getReimbursementsByUserIdAndStatus(
            currentEmployee.getId(), "APPROVED", 0, Integer.MAX_VALUE).getTotalElements();
        long rejectedCount = reimbursementService.getReimbursementsByUserIdAndStatus(
            currentEmployee.getId(), "REJECTED", 0, Integer.MAX_VALUE).getTotalElements();
        
        model.addAttribute("reimbursements", reimbursementPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", reimbursementPage.getTotalPages());
        model.addAttribute("totalItems", reimbursementPage.getTotalElements());
        model.addAttribute("pageSize", size);
        model.addAttribute("status", status);
        model.addAttribute("pendingCount", pendingCount);
        model.addAttribute("approvedCount", approvedCount);
        model.addAttribute("rejectedCount", rejectedCount);
        model.addAttribute("totalRequest", reimbursementPage.getTotalElements());
        model.addAttribute("employeeName", currentEmployee.getName());
        
        return "main";
    }
}
