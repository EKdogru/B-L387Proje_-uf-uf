package com.cufcuf.backend.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cufcuf.backend.model.Station;
import com.cufcuf.backend.repository.StationRepository;

@RestController
@RequestMapping("/api/stations")
public class StationController {

    private final StationRepository stationRepository;

    public StationController(StationRepository stationRepository) {
        this.stationRepository = stationRepository;
    }

    @GetMapping("/all")
    public ResponseEntity<List<Station>> getAllStations() {
        try {
            List<Station> stations = stationRepository.findAll();
            return ResponseEntity.ok(stations);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).build();
        }
    }

    @PostMapping("/create")
    public ResponseEntity<?> createStation(@RequestBody Station station) {
        try {
            Station savedStation = stationRepository.save(station);
            return ResponseEntity.ok(savedStation);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("İstasyon oluşturulurken hata oluştu: " + e.getMessage());
        }
    }
}