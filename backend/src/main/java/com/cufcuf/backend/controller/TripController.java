package com.cufcuf.backend.controller;

import com.cufcuf.backend.model.Booking;
import com.cufcuf.backend.model.Trip;
import com.cufcuf.backend.repository.BookingRepository;
import com.cufcuf.backend.repository.StationRepository;
import com.cufcuf.backend.repository.TripRepository;
import com.cufcuf.backend.service.TripService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/trips")
public class TripController {

    private final TripRepository tripRepository;
    private final TripService tripService;
    private final StationRepository stationRepository;
    private final BookingRepository bookingRepository;

    public TripController(TripRepository tripRepository, 
                         TripService tripService,
                         StationRepository stationRepository,
                         BookingRepository bookingRepository) {
        this.tripRepository = tripRepository;
        this.tripService = tripService;
        this.stationRepository = stationRepository;
        this.bookingRepository = bookingRepository;
    }

    @GetMapping("/search")
    public List<Trip> searchTrips(
            @RequestParam Long fromId,
            @RequestParam Long toId,
            @RequestParam String date) {
        
        System.out.println("=== SEFER ARAMA ===");
        System.out.println("From: " + fromId);
        System.out.println("To: " + toId);
        System.out.println("Date: " + date);
        
        LocalDate tripDate = LocalDate.parse(date);
        
        List<Trip> trips = tripRepository.findByDepartureStationIdAndArrivalStationIdAndTripDate(
            fromId, 
            toId, 
            tripDate
        );
        
        System.out.println("Bulunan sefer sayısı: " + trips.size());
        
        return trips;
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getTripById(@PathVariable Long id) {
        try {
            Trip trip = tripRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Sefer bulunamadı!"));
            return ResponseEntity.ok(trip);
        } catch (Exception e) {
            return ResponseEntity.status(404).body("Sefer bulunamadı!");
        }
    }

    @GetMapping("/all")
    public ResponseEntity<?> getAllTrips(
            @RequestParam(required = false) String date,
            @RequestParam(required = false) Long fromId,
            @RequestParam(required = false) Long toId) {
        try {
            List<Trip> trips;
            
            if (date != null && fromId != null && toId != null) {
                LocalDate tripDate = LocalDate.parse(date);
                trips = tripRepository.findByDepartureStationIdAndArrivalStationIdAndTripDate(
                    fromId, toId, tripDate
                );
            } else if (date != null) {
                LocalDate tripDate = LocalDate.parse(date);
                trips = tripRepository.findByTripDate(tripDate);
            } else {
                trips = tripRepository.findAll();
            }
            
            List<Map<String, Object>> tripDTOs = trips.stream()
                .sorted((t1, t2) -> {
                    int dateCompare = t1.getTripDate().compareTo(t2.getTripDate());
                    if (dateCompare != 0) return dateCompare;
                    return t1.getDepartureTime().compareTo(t2.getDepartureTime());
                })
                .map(trip -> {
                    Map<String, Object> dto = new HashMap<>();
                    dto.put("id", trip.getId());
                    dto.put("tripNumber", trip.getTripNumber());
                    dto.put("departureStationId", trip.getDepartureStationId());
                    dto.put("arrivalStationId", trip.getArrivalStationId());
                    dto.put("departureTime", trip.getDepartureTime());
                    dto.put("arrivalTime", trip.getArrivalTime());
                    dto.put("tripDate", trip.getTripDate());
                    dto.put("basePrice", trip.getBasePrice());
                    dto.put("totalSeats", trip.getTotalSeats());
                    dto.put("availableSeats", trip.getAvailableSeats());
                    
                    stationRepository.findById(trip.getDepartureStationId()).ifPresent(station -> {
                        dto.put("departureStationName", station.getName());
                    });
                    
                    stationRepository.findById(trip.getArrivalStationId()).ifPresent(station -> {
                        dto.put("arrivalStationName", station.getName());
                    });
                    
                    if (trip.getTotalSeats() != null && trip.getTotalSeats() > 0) {
                        int occupiedSeats = trip.getTotalSeats() - (trip.getAvailableSeats() != null ? trip.getAvailableSeats() : 0);
                        double occupancyRate = (occupiedSeats * 100.0) / trip.getTotalSeats();
                        dto.put("occupancyRate", Math.round(occupancyRate));
                    } else {
                        dto.put("occupancyRate", 0);
                    }
                    
                    return dto;
                }).collect(Collectors.toList());
            
            return ResponseEntity.ok(tripDTOs);
            
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Seferler yüklenirken hata oluştu: " + e.getMessage());
        }
    }

    @PostMapping("/create")
    public ResponseEntity<?> createTrip(@RequestBody Map<String, Object> tripData) {
        try {
            System.out.println("=== YENİ SEFER OLUŞTURMA ===");
            System.out.println("Gelen veri: " + tripData);
            
            Trip trip = new Trip();
            trip.setTripNumber(tripData.get("tripNumber").toString());
            trip.setDepartureStationId(Long.valueOf(tripData.get("departureStationId").toString()));
            trip.setArrivalStationId(Long.valueOf(tripData.get("arrivalStationId").toString()));
            trip.setDepartureTime(LocalTime.parse(tripData.get("departureTime").toString()));
            trip.setArrivalTime(LocalTime.parse(tripData.get("arrivalTime").toString()));
            trip.setTripDate(LocalDate.parse(tripData.get("tripDate").toString()));
            trip.setBasePrice(new BigDecimal(tripData.get("basePrice").toString()));
            trip.setTotalSeats(96);
            trip.setAvailableSeats(96);
            
            Trip createdTrip = tripService.createTripWithSeats(trip);
            
            System.out.println("✅ Sefer başarıyla oluşturuldu! ID: " + createdTrip.getId());
            
            return ResponseEntity.ok(createdTrip);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Sefer oluşturulurken hata oluştu: " + e.getMessage());
        }
    }

    @DeleteMapping("/{tripId}")
    public ResponseEntity<?> deleteTrip(@PathVariable Long tripId) {
        try {
            System.out.println("=== SEFER SİLME ===");
            System.out.println("Trip ID: " + tripId);
            
            Trip trip = tripRepository.findById(tripId)
                    .orElseThrow(() -> new RuntimeException("Sefer bulunamadı!"));
            
            // Rezervasyonları iptal et
            List<Booking> bookings = bookingRepository.findByTripId(tripId);
            for (Booking booking : bookings) {
                booking.setBookingStatus("CANCELLED");
                bookingRepository.save(booking);
            }
            System.out.println("İptal edilen rezervasyon sayısı: " + bookings.size());
            
            // Seferi sil (CASCADE ile vagonlar ve koltuklar da silinecek)
            tripRepository.deleteById(tripId);
            
            System.out.println("✅ Sefer başarıyla silindi!");
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Sefer başarıyla silindi!");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Sefer silinirken hata oluştu: " + e.getMessage());
        }
    }
}