package com.cufcuf.backend.service.impl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cufcuf.backend.model.Seat;
import com.cufcuf.backend.model.Trip;
import com.cufcuf.backend.model.Wagon;
import com.cufcuf.backend.repository.SeatRepository;
import com.cufcuf.backend.repository.TripRepository;
import com.cufcuf.backend.repository.WagonRepository;
import com.cufcuf.backend.service.TripService;

@Service
public class TripServiceImpl implements TripService {

    private final TripRepository tripRepository;
    
    @Autowired
    private WagonRepository wagonRepository;
    
    @Autowired
    private SeatRepository seatRepository;

    public TripServiceImpl(TripRepository tripRepository) {
        this.tripRepository = tripRepository;
    }

    @Override
    public List<Trip> searchTrips(Long fromStationId, Long toStationId, LocalDate date) {
        return tripRepository.findByDepartureStationIdAndArrivalStationId(fromStationId, toStationId);
    }

    @Override
    public Trip createTrip(Trip trip) {
        return tripRepository.save(trip);
    }

    @Override
    @Transactional
    public Trip createTripWithSeats(Trip trip) {
        System.out.println("=== YENİ SEFER OLUŞTURULUYOR ===");
        
        // 1. Seferi kaydet
        Trip savedTrip = tripRepository.save(trip);
        System.out.println("✅ Sefer oluşturuldu: " + savedTrip.getId());
        
        // 2. Her sefer için 4 vagon oluştur
        for (int wagonNum = 1; wagonNum <= 4; wagonNum++) {
            Wagon wagon = new Wagon();
            wagon.setTripId(savedTrip.getId());
            wagon.setWagonNumber(wagonNum);
            wagon.setWagonType("ECONOMY"); // Hepsi ekonomi sınıfı
            wagon.setTotalSeats(24);
            wagon.setCreatedAt(LocalDateTime.now());
            
            Wagon savedWagon = wagonRepository.save(wagon);
            System.out.println("✅ Vagon " + wagonNum + " oluşturuldu: " + savedWagon.getId());
            
            // 3. Her vagon için 24 koltuk oluştur
            for (int seatNum = 1; seatNum <= 24; seatNum++) {
                Seat seat = new Seat();
                seat.setWagonId(savedWagon.getId());
                seat.setSeatNumber(seatNum);
                seat.setIsAvailable(true); // Başlangıçta tüm koltuklar boş
                seat.setSeatType("STANDARD");
                seat.setCreatedAt(LocalDateTime.now());
                
                seatRepository.save(seat);
            }
            System.out.println("✅ Vagon " + wagonNum + " için 24 koltuk oluşturuldu");
        }
        
        // 4. Trip'in toplam ve müsait koltuk sayısını güncelle
        savedTrip.setTotalSeats(96); // 4 vagon x 24 koltuk
        savedTrip.setAvailableSeats(96); // Başlangıçta hepsi boş
        tripRepository.save(savedTrip);
        
        System.out.println("🎉 Sefer ve koltuklar başarıyla oluşturuldu!");
        
        return savedTrip;
    }
}