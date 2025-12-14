package com.cufcuf.backend.service;

import java.time.LocalDate;
import java.util.List;

import com.cufcuf.backend.model.Trip;

public interface TripService {
    List<Trip> searchTrips(Long fromStationId, Long toStationId, LocalDate date);
    Trip createTrip(Trip trip);
    Trip createTripWithSeats(Trip trip);  
}