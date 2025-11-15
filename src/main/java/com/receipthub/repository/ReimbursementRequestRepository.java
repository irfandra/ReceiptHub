package com.receipthub.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.receipthub.model.ReimbursementRequest;
import com.receipthub.model.User;

@Repository
public interface ReimbursementRequestRepository extends JpaRepository<ReimbursementRequest, Long> {
    
    List<ReimbursementRequest> findByStatus(ReimbursementRequest.RequestStatus status);
    
    Page<ReimbursementRequest> findByStatus(ReimbursementRequest.RequestStatus status, Pageable pageable);
    
    Page<ReimbursementRequest> findBySubmittedBy(User user, Pageable pageable);
    
    Page<ReimbursementRequest> findBySubmittedByAndStatus(User user, ReimbursementRequest.RequestStatus status, Pageable pageable);
    
    List<ReimbursementRequest> findAllByOrderBySubmittedAtDesc();
}
