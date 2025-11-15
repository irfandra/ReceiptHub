package com.receipthub.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.receipthub.model.Receipt;

@Repository
public interface ReceiptRepository extends JpaRepository<Receipt, Long> {
}
