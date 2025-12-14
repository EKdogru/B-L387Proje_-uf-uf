package com.cufcuf.backend.controller;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cufcuf.backend.model.Booking;
import com.cufcuf.backend.model.Payment;
import com.cufcuf.backend.model.Seat;
import com.cufcuf.backend.model.Session;
import com.cufcuf.backend.model.Trip;
import com.cufcuf.backend.model.Wagon;
import com.cufcuf.backend.repository.BookingRepository;
import com.cufcuf.backend.repository.PaymentRepository;
import com.cufcuf.backend.repository.SeatRepository;
import com.cufcuf.backend.repository.SessionRepository;
import com.cufcuf.backend.repository.TripRepository;
import com.cufcuf.backend.repository.WagonRepository;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    private final BookingRepository bookingRepository;
    private final PaymentRepository paymentRepository;
    private final SeatRepository seatRepository;
    private final SessionRepository sessionRepository;
    private final WagonRepository wagonRepository;
    private final TripRepository tripRepository;

    public BookingController(BookingRepository bookingRepository, 
                            PaymentRepository paymentRepository,
                            SeatRepository seatRepository,
                            SessionRepository sessionRepository,
                            WagonRepository wagonRepository,
                            TripRepository tripRepository) {
        this.bookingRepository = bookingRepository;
        this.paymentRepository = paymentRepository;
        this.seatRepository = seatRepository;
        this.sessionRepository = sessionRepository;
        this.wagonRepository = wagonRepository;
        this.tripRepository = tripRepository;
    }

    @PostMapping("/create")
    public ResponseEntity<?> createBooking(
            @RequestHeader(value = "Session-Token", required = false) String sessionToken,
            @RequestBody Map<String, Object> bookingData) {
        try {
            System.out.println("=== REZERVASYON OLUŞTURMA ===");
            System.out.println("Gelen veri: " + bookingData);
            System.out.println("Session Token: " + sessionToken);

            Long tripId = Long.valueOf(bookingData.get("tripId").toString());
            Long seatId = Long.valueOf(bookingData.get("seatId").toString());
            String passengerName = bookingData.get("passengerName").toString();
            String passengerSurname = bookingData.get("passengerSurname").toString();
            Integer wagonNumber = Integer.valueOf(bookingData.get("wagonNumber").toString());
            Integer seatNumber = Integer.valueOf(bookingData.get("seatNumber").toString());

            Long userId = null;
            if (sessionToken != null && !sessionToken.isEmpty()) {
                Session session = sessionRepository.findBySessionToken(sessionToken)
                        .orElse(null);
                if (session != null) {
                    userId = session.getUserId();
                    System.out.println("Session'dan alınan User ID: " + userId);
                }
            }

            @SuppressWarnings("unchecked")
            Map<String, String> paymentDetails = (Map<String, String>) bookingData.get("paymentDetails");

            Seat seat = seatRepository.findById(seatId)
                    .orElseThrow(() -> new RuntimeException("Koltuk bulunamadı!"));

            if (!seat.getIsAvailable()) {
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("message", "Bu koltuk dolu! Lütfen başka bir koltuk seçin.");
                return ResponseEntity.badRequest().body(errorResponse);
            }

            String pnrCode = UUID.randomUUID().toString().substring(0, 6).toUpperCase();

            Booking booking = new Booking();
            booking.setPnrCode(pnrCode);
            booking.setUserId(userId);
            booking.setTripId(tripId);
            booking.setWagonId(seat.getWagonId());
            booking.setSeatId(seatId);
            booking.setPassengerName(passengerName);
            booking.setPassengerSurname(passengerSurname);
            booking.setTravelDate(LocalDate.now());
            booking.setBookingStatus("CONFIRMED");
            booking.setTotalPrice(new BigDecimal("450.00"));
            
            
            booking = bookingRepository.save(booking);

            System.out.println("Booking kaydı oluşturuldu: " + booking.getId());
            System.out.println("User ID: " + userId);
            System.out.println("Vagon: " + wagonNumber + ", Koltuk: " + seatNumber);

            String cardNumber = paymentDetails.get("cardNumber");
            String cardLastFour = cardNumber.substring(cardNumber.length() - 4);

            Payment payment = new Payment();
            payment.setBookingId(booking.getId());
            payment.setAmount(new BigDecimal("450.00"));
            payment.setPaymentMethod("CREDIT_CARD");
            payment.setCardLastFour(cardLastFour);
            payment.setTransactionId(UUID.randomUUID().toString());
            payment.setPaymentStatus("COMPLETED");
            payment.setPaidAt(LocalDateTime.now());
            
            payment = paymentRepository.save(payment);

            System.out.println("Payment kaydı oluşturuldu: " + payment.getId());

            seat.setIsAvailable(false);
            seatRepository.save(seat);

            System.out.println("Koltuk dolu yapıldı: " + seatId);
            System.out.println("✅ Rezervasyon başarıyla tamamlandı!");

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("pnrCode", pnrCode);
            response.put("bookingId", booking.getId());
            response.put("message", "Rezervasyon başarıyla oluşturuldu!");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            e.printStackTrace();
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", "Rezervasyon oluşturulurken bir hata oluştu: " + e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    @GetMapping("/{pnr}")
    public ResponseEntity<?> getBookingByPnr(@PathVariable String pnr) {
        try {
            Booking booking = bookingRepository.findByPnrCode(pnr)
                    .orElseThrow(() -> new RuntimeException("Rezervasyon bulunamadı!"));
            return ResponseEntity.ok(booking);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(404).body(errorResponse);
        }
    }

    @GetMapping("/user/my-tickets")
    public ResponseEntity<?> getMyBookings(@RequestHeader("Session-Token") String sessionToken) {
        try {
            System.out.println("=== KULLANICI BİLETLERİ SORGUSU ===");
            System.out.println("Session Token: " + sessionToken);
            
            Session session = sessionRepository.findBySessionToken(sessionToken)
                    .orElseThrow(() -> new RuntimeException("Geçersiz session!"));
            
            Long userId = session.getUserId();
            System.out.println("User ID: " + userId);
            
            List<Booking> bookings = bookingRepository.findByUserId(userId);
            
            List<Map<String, Object>> bookingDTOs = bookings.stream().map(booking -> {
                Map<String, Object> dto = new HashMap<>();
                dto.put("id", booking.getId());
                dto.put("pnrCode", booking.getPnrCode());
                dto.put("passengerName", booking.getPassengerName());
                dto.put("passengerSurname", booking.getPassengerSurname());
                dto.put("travelDate", booking.getTravelDate());
                dto.put("bookingStatus", booking.getBookingStatus());
                dto.put("totalPrice", booking.getTotalPrice());
                dto.put("createdAt", booking.getCreatedAt());
                
                Wagon wagon = wagonRepository.findById(booking.getWagonId()).orElse(null);
                if (wagon != null) {
                    dto.put("wagonNo", wagon.getWagonNumber());
                } else {
                    dto.put("wagonNo", "?");
                }
                
                Seat seat = seatRepository.findById(booking.getSeatId()).orElse(null);
                if (seat != null) {
                    dto.put("seatNo", seat.getSeatNumber());
                } else {
                    dto.put("seatNo", "?");
                }
                
                // Trip bilgisini ekle (sefer tarihi için)
                Trip trip = tripRepository.findById(booking.getTripId()).orElse(null);
                if (trip != null) {
                    dto.put("tripDate", trip.getTripDate());
                    dto.put("tripNumber", trip.getTripNumber());
                    dto.put("departureTime", trip.getDepartureTime());
                    dto.put("arrivalTime", trip.getArrivalTime());
                }
                
                return dto;
            }).collect(Collectors.toList());
            
            System.out.println("Bulunan bilet sayısı: " + bookings.size());
            
            return ResponseEntity.ok(bookingDTOs);
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", "Biletler yüklenemedi: " + e.getMessage());
            return ResponseEntity.status(401).body(errorResponse);
        }
    }

    @DeleteMapping("/{bookingId}/cancel")
    public ResponseEntity<?> cancelBooking(
            @RequestHeader("Session-Token") String sessionToken,
            @PathVariable Long bookingId) {
        try {
            System.out.println("=== BİLET İPTALİ ===");
            System.out.println("Booking ID: " + bookingId);
            
            Session session = sessionRepository.findBySessionToken(sessionToken)
                    .orElseThrow(() -> new RuntimeException("Geçersiz session!"));
            
            Long userId = session.getUserId();
            
            Booking booking = bookingRepository.findById(bookingId)
                    .orElseThrow(() -> new RuntimeException("Rezervasyon bulunamadı!"));
            
            if (!booking.getUserId().equals(userId)) {
                return ResponseEntity.status(403).body("Bu işlem için yetkiniz yok!");
            }
            
            if ("CANCELLED".equals(booking.getBookingStatus())) {
                return ResponseEntity.badRequest().body("Bu bilet zaten iptal edilmiş!");
            }
            
            Seat seat = seatRepository.findById(booking.getSeatId())
                    .orElse(null);
            if (seat != null) {
                seat.setIsAvailable(true);
                seatRepository.save(seat);
                System.out.println("Koltuk boşaltıldı: " + seat.getId());
            }
            
            booking.setBookingStatus("CANCELLED");
            bookingRepository.save(booking);
            
            System.out.println("✅ Bilet iptal edildi!");
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Biletiniz başarıyla iptal edildi!");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", "Bilet iptal edilirken hata oluştu: " + e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    @PutMapping("/{bookingId}/change")
    public ResponseEntity<?> changeBooking(
            @RequestHeader("Session-Token") String sessionToken,
            @PathVariable Long bookingId,
            @RequestBody Map<String, Object> changeData) {
        try {
            System.out.println("=== BİLET DEĞİŞİKLİĞİ ===");
            System.out.println("Booking ID: " + bookingId);
            System.out.println("Değişiklik verisi: " + changeData);
            
            Session session = sessionRepository.findBySessionToken(sessionToken)
                    .orElseThrow(() -> new RuntimeException("Geçersiz session!"));
            
            Long userId = session.getUserId();
            
            Booking oldBooking = bookingRepository.findById(bookingId)
                    .orElseThrow(() -> new RuntimeException("Rezervasyon bulunamadı!"));
            
            if (!oldBooking.getUserId().equals(userId)) {
                return ResponseEntity.status(403).body("Bu işlem için yetkiniz yok!");
            }
            
            if ("CANCELLED".equals(oldBooking.getBookingStatus())) {
                return ResponseEntity.badRequest().body("İptal edilmiş bilet değiştirilemez!");
            }
            
            Long newTripId = Long.valueOf(changeData.get("newTripId").toString());
            Long newSeatId = Long.valueOf(changeData.get("newSeatId").toString());
            
            Seat newSeat = seatRepository.findById(newSeatId)
                    .orElseThrow(() -> new RuntimeException("Yeni koltuk bulunamadı!"));
            
            if (!newSeat.getIsAvailable()) {
                return ResponseEntity.badRequest().body("Seçtiğiniz koltuk dolu!");
            }
            
            Seat oldSeat = seatRepository.findById(oldBooking.getSeatId())
                    .orElse(null);
            if (oldSeat != null) {
                oldSeat.setIsAvailable(true);
                seatRepository.save(oldSeat);
                System.out.println("Eski koltuk boşaltıldı: " + oldSeat.getId());
            }
            
            newSeat.setIsAvailable(false);
            seatRepository.save(newSeat);
            System.out.println("Yeni koltuk dolu yapıldı: " + newSeat.getId());
            
            oldBooking.setTripId(newTripId);
            oldBooking.setWagonId(newSeat.getWagonId());
            oldBooking.setSeatId(newSeatId);
            oldBooking.setUpdatedAt(LocalDateTime.now());
            
            bookingRepository.save(oldBooking);
            
            System.out.println("✅ Bilet değiştirildi!");
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Biletiniz başarıyla değiştirildi!");
            response.put("newBooking", oldBooking);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", "Bilet değiştirilirken hata oluştu: " + e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }
}