package com.cufcuf.backend.service.impl;

import com.cufcuf.backend.model.Booking;
import com.cufcuf.backend.repository.BookingRepository;
import com.cufcuf.backend.service.BookingService;
import org.springframework.stereotype.Service;
import java.util.UUID;

@Service
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;

    public BookingServiceImpl(BookingRepository bookingRepository) {
        this.bookingRepository = bookingRepository;
    }

    @Override
    public Booking createBooking(Booking booking) {
        booking.setPnrCode(UUID.randomUUID().toString().substring(0, 6).toUpperCase());
        booking.setBookingStatus("CONFIRMED");
        return bookingRepository.save(booking);
    }

    @Override
    public Booking getBookingByPnr(String pnr) {
        return bookingRepository.findByPnrCode(pnr)
                .orElseThrow(() -> new RuntimeException("Rezervasyon bulunamadı!"));
    }
}