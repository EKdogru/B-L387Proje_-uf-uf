package com.cufcuf.backend.service;

import com.cufcuf.backend.model.Booking;

public interface BookingService {
    Booking createBooking(Booking booking);
    Booking getBookingByPnr(String pnr);
}