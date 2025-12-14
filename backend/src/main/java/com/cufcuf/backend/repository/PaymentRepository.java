package com.cufcuf.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.cufcuf.backend.model.Payment;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
}