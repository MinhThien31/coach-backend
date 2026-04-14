package com.minhthien.web.coach.service;

import com.minhthien.web.coach.dto.request.BookingRequest;
import com.minhthien.web.coach.dto.response.BookingResponse;

import java.util.List;

public interface BookingService {
    BookingResponse createBooking(BookingRequest request);
    List<BookingResponse> myBookings();
    BookingResponse completeBooking(Long bookingId);
    void cancelBooking(Long bookingId);
}
