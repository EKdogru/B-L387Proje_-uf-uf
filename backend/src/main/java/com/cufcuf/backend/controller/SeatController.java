package com.cufcuf.backend.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cufcuf.backend.model.Seat;
import com.cufcuf.backend.model.Wagon;
import com.cufcuf.backend.repository.SeatRepository;
import com.cufcuf.backend.repository.WagonRepository;

@RestController
@RequestMapping("/api/seats")
public class SeatController {

    @Autowired
    private WagonRepository wagonRepository;

    @Autowired
    private SeatRepository seatRepository;

    @GetMapping("/trip/{tripId}")
    public ResponseEntity<Map<String, Object>> getSeatsForTrip(@PathVariable Long tripId) {
        System.out.println("=== KOLTUK İSTEĞİ ===");
        System.out.println("Trip ID: " + tripId);

        List<Wagon> wagons = wagonRepository.findByTripId(tripId);
        
        Map<String, Object> response = new HashMap<>();
        
        for (Wagon wagon : wagons) {
            List<Seat> seats = seatRepository.findByWagonId(wagon.getId());
            response.put("wagon_" + wagon.getWagonNumber(), seats);
        }
        
        response.put("wagons", wagons);
        
        System.out.println("Bulunan vagon sayısı: " + wagons.size());
        
        return ResponseEntity.ok(response);
    }
}