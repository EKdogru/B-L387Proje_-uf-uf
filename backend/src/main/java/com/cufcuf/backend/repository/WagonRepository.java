package com.cufcuf.backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.cufcuf.backend.model.Wagon;

@Repository
public interface WagonRepository extends JpaRepository<Wagon, Long> {
    List<Wagon> findByTripId(Long tripId);
}