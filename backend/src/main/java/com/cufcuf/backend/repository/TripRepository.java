package com.cufcuf.backend.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.cufcuf.backend.model.Trip;

@Repository
public interface TripRepository extends JpaRepository<Trip, Long> {
    List<Trip> findByDepartureStationIdAndArrivalStationId(Long departureStationId, Long arrivalStationId);
    
    List<Trip> findByDepartureStationIdAndArrivalStationIdAndTripDate(
        Long departureStationId, 
        Long arrivalStationId, 
        LocalDate tripDate
    );
    
    List<Trip> findByTripDate(LocalDate tripDate);
}