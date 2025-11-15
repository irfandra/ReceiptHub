package com.receipthub.controller;

import java.security.Principal;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.springframework.data.domain.Page;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.receipthub.dto.ReimbursementResponse;
import com.receipthub.model.ReimbursementRequest;
import com.receipthub.model.User;
import com.receipthub.service.ReimbursementService;
import com.receipthub.service.UserService;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class DashboardController {
    
    private final ReimbursementService reimbursementService;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    
    @GetMapping("/")
    public String home(Principal principal) {
        if (principal == null) {
            return "redirect:/login";
        }
        
        User user = userService.getUserByEmail(principal.getName()).orElse(null);
        if (user == null) {
            return "redirect:/login";
        }
        
        if (user.getRole() == User.UserRole.ADMIN) {
            return "redirect:/dashboard";
        } else {
            return "redirect:/my-requests";
        }
    }
    
    @GetMapping("/dashboard")
    public String dashboard(
            @RequestParam(required = false, defaultValue = "ALL") String status,
            @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "10") int size,
        @RequestParam(required = false, defaultValue = "overview") String tab,
        @RequestParam(required = false) String search,
            Principal principal,
            Model model) {
        
        // Spring Security ensures only ADMIN can access (configured in SecurityConfig)
        User user = userService.getUserByEmail(principal.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
        
    model.addAttribute("currentUser", user);
    model.addAttribute("activeTab", tab);
    model.addAttribute("pageTitle", "employees".equalsIgnoreCase(tab) ? "Employee Management" : "Dashboard");
        
        // If employees tab is active, prepare employees data and short-circuit stats table rendering via template
        if ("employees".equalsIgnoreCase(tab)) {
            Page<User> employeesPage;
            if (search != null && !search.trim().isEmpty()) {
                employeesPage = userService.searchUsers(search, page, size);
            } else {
                employeesPage = userService.getAllUsers(page, size);
            }

            model.addAttribute("employees", employeesPage.getContent());
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", employeesPage.getTotalPages());
            model.addAttribute("totalItems", employeesPage.getTotalElements());
            model.addAttribute("pageSize", size);
            model.addAttribute("search", search);

            // Still return dashboard; template will include the employees fragment instead of the default content
            return "main";
        }

        // Get all reimbursements for stats and chart
        List<ReimbursementResponse> allReimbursements = reimbursementService.getAllReimbursements(0, Integer.MAX_VALUE).getContent();
        
        // Get paginated reimbursements based on status filter
        Page<ReimbursementResponse> reimbursementPage;
        if ("ALL".equals(status)) {
            reimbursementPage = reimbursementService.getAllReimbursements(page, size);
        } else {
            try {
                ReimbursementRequest.RequestStatus requestStatus = ReimbursementRequest.RequestStatus.valueOf(status);
                reimbursementPage = reimbursementService.getReimbursementsByStatus(requestStatus, page, size);
            } catch (IllegalArgumentException e) {
                // Invalid status, default to ALL
                reimbursementPage = reimbursementService.getAllReimbursements(page, size);
            }
        }
        
        List<ReimbursementResponse> reimbursements = reimbursementPage.getContent();
        int totalPages = reimbursementPage.getTotalPages();
        long totalItems = reimbursementPage.getTotalElements();
        
        // Calculate stats from all reimbursements
        long pendingCount = allReimbursements.stream()
                .filter(r -> "PENDING".equals(r.getStatus()))
                .count();
        long approvedCount = allReimbursements.stream()
            .filter(r -> "APPROVED".equals(r.getStatus()))
            .count();
        long rejectedCount = allReimbursements.stream()
            .filter(r -> "REJECTED".equals(r.getStatus()))
            .count();
        
        double totalAmount = allReimbursements.stream()
            .filter(r -> "APPROVED".equals(r.getStatus()))
            .map(ReimbursementResponse::getRequestedAmount)
            .filter(Objects::nonNull)
            .reduce(0.0, Double::sum);
        totalAmount = Math.round(totalAmount * 100.0) / 100.0;
        
        double totalPendingAmount = allReimbursements.stream()
            .filter(r -> "PENDING".equals(r.getStatus()))
            .map(ReimbursementResponse::getRequestedAmount)
            .filter(Objects::nonNull)
            .reduce(0.0, Double::sum);
        totalPendingAmount = Math.round(totalPendingAmount * 100.0) / 100.0;
        
        // Calculate monthly data for chart (only months with actual transactions)
        Map<String, Double> monthlyTotalRequested = new LinkedHashMap<>();
        Map<String, Double> monthlyApproved = new LinkedHashMap<>();
        Map<String, Double> monthlyRejected = new LinkedHashMap<>();
        
        DateTimeFormatter monthFormatter = DateTimeFormatter.ofPattern("MMM yyyy");
        
        // Group reimbursements by month - only include months with data
        for (ReimbursementResponse r : allReimbursements) {
            if (r.getSubmittedAt() == null || r.getRequestedAmount() == null) {
                continue; // Skip records with missing data
            }
            
            String monthKey = r.getSubmittedAt().format(monthFormatter);
            
            // Total requested (all statuses)
            monthlyTotalRequested.put(monthKey, 
                monthlyTotalRequested.getOrDefault(monthKey, 0.0) + r.getRequestedAmount());
            
            // Approved
            if ("APPROVED".equals(r.getStatus())) {
                monthlyApproved.put(monthKey, 
                    monthlyApproved.getOrDefault(monthKey, 0.0) + r.getRequestedAmount());
            }
            
            // Rejected
            if ("REJECTED".equals(r.getStatus())) {
                monthlyRejected.put(monthKey, 
                    monthlyRejected.getOrDefault(monthKey, 0.0) + r.getRequestedAmount());
            }
        }
        
        // Ensure all month keys exist in all maps (with 0.0 if no data for that status)
        for (String monthKey : monthlyTotalRequested.keySet()) {
            monthlyApproved.putIfAbsent(monthKey, 0.0);
            monthlyRejected.putIfAbsent(monthKey, 0.0);
        }

        model.addAttribute("totalRequest", allReimbursements.size());
        model.addAttribute("reimbursements", reimbursements);
        model.addAttribute("status", status);
        model.addAttribute("pendingCount", pendingCount);
        model.addAttribute("approvedCount", approvedCount);
        model.addAttribute("rejectedCount", rejectedCount);
        //money
        model.addAttribute("totalAmount", totalAmount);
        model.addAttribute("totalPendingAmount", totalPendingAmount);
        model.addAttribute("totalAmountRequested",totalAmount);
        
        // Pagination attributes
        model.addAttribute("currentPage", page);
        model.addAttribute("pageSize", size);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("totalItems", totalItems);
        
        // Chart data
        model.addAttribute("chartMonths", new ArrayList<>(monthlyTotalRequested.keySet()));
        model.addAttribute("chartTotalRequested", new ArrayList<>(monthlyTotalRequested.values()));
        model.addAttribute("chartApproved", new ArrayList<>(monthlyApproved.values()));
        model.addAttribute("chartRejected", new ArrayList<>(monthlyRejected.values()));
        
        return "main";
    }
    
    // Employee management
    @PostMapping("/dashboard/employees/add")
    public String addEmployee(
            @RequestParam String name,
            @RequestParam String email,
            @RequestParam(required = false) String phoneNumber,
            @RequestParam String role,
            Principal principal,
            Model model) {
        
        // Validate phone number format if provided
        if (phoneNumber != null && !phoneNumber.trim().isEmpty() && !phoneNumber.trim().startsWith("+")) {
            model.addAttribute("error", "Phone number must start with + (country code)");
            return "redirect:/dashboard?tab=employees&error=invalid_phone";
        }
        
        User newUser = new User();
        newUser.setName(name);
        newUser.setEmail(email);
        newUser.setPhoneNumber(phoneNumber != null && !phoneNumber.trim().isEmpty() ? phoneNumber.trim() : null);
        newUser.setRole(User.UserRole.valueOf(role));
        // Set default password "password" for employees added by admin
        newUser.setPassword(passwordEncoder.encode("password"));
        userService.createUser(newUser);
        return "redirect:/dashboard?tab=employees";
    }

    @PostMapping("/dashboard/employees/edit")
    public String editEmployee(
            @RequestParam Long id,
            @RequestParam String name,
            @RequestParam String email,
            @RequestParam(required = false) String phoneNumber,
            @RequestParam String role,
            Principal principal,
            Model model) {
        
        // Validate phone number format if provided
        if (phoneNumber != null && !phoneNumber.trim().isEmpty() && !phoneNumber.trim().startsWith("+")) {
            model.addAttribute("error", "Phone number must start with + (country code)");
            return "redirect:/dashboard?tab=employees&error=invalid_phone";
        }
        
        User user = userService.getUserById(id);
        user.setName(name);
        user.setEmail(email);
        user.setPhoneNumber(phoneNumber != null && !phoneNumber.trim().isEmpty() ? phoneNumber.trim() : null);
        user.setRole(User.UserRole.valueOf(role));
        userService.updateUser(id, user);
        return "redirect:/dashboard?tab=employees";
    }

    @PostMapping("/dashboard/employees/delete")
    public String deleteEmployee(@RequestParam Long id, Principal principal, Model model) {
        userService.deleteUser(id);
        return "redirect:/dashboard?tab=employees";
    }
    
    // Reimbursement management
    @PostMapping("/dashboard/reimbursements/approve")
    public String approveReimbursement(
            @RequestParam Long reimbursementId,
            @RequestParam Long adminId,
            @RequestParam(required = false) String notes,
            Principal principal) {
        reimbursementService.approveReimbursement(reimbursementId, adminId, notes);
        return "redirect:/dashboard";
    }
    
    @PostMapping("/dashboard/reimbursements/reject")
    public String rejectReimbursement(
            @RequestParam Long reimbursementId,
            @RequestParam Long adminId,
            @RequestParam String notes,
            Principal principal) {
        reimbursementService.rejectReimbursement(reimbursementId, adminId, notes);
        return "redirect:/dashboard";
    }
    
    @PostMapping("/dashboard/reimbursements/edit")
    public String editReimbursement(
            @RequestParam Long requestId,
            @RequestParam String merchantName,
            @RequestParam Double amount,
            @RequestParam String transactionDate,
            @RequestParam(required = false) String description,
            Principal principal) {
        reimbursementService.editReimbursement(requestId, merchantName, amount, transactionDate, description);
        return "redirect:/dashboard";
    }
}
