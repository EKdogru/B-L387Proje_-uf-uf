package com.cufcuf.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.cufcuf.backend.model.Station;

@Repository
public interface StationRepository extends JpaRepository<Station, Long> {
}