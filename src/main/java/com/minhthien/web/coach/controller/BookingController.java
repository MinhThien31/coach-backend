package com.minhthien.web.coach.controller;


import com.minhthien.web.coach.dto.request.BookingRequest;
import com.minhthien.web.coach.dto.response.*;
import com.minhthien.web.coach.service.BookingService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class BookingController {

    private final BookingService bookingService;

    @PostMapping
    public ApiResponse<BookingResponse> createBooking(
            @RequestBody BookingRequest request
    ) {

        return ApiResponse.<BookingResponse>builder()
                .data(bookingService.createBooking(request))
                .build();
    }

    @GetMapping("/my")
    public ApiResponse<List<BookingResponse>> myBookings() {

        return ApiResponse.<List<BookingResponse>>builder()
                .data(bookingService.myBookings())
                .build();
    }

    @PutMapping("/{id}/complete")
    public ApiResponse<BookingResponse> completeBooking(
            @PathVariable Long id
    ) {

        return ApiResponse.<BookingResponse>builder()
                .data(bookingService.completeBooking(id))
                .build();
    }

    @PutMapping("/{id}/cancel")
    public ApiResponse<String> cancelBooking(
            @PathVariable Long id
    ) {

        bookingService.cancelBooking(id);

        return ApiResponse.<String>builder()
                .data("Booking cancelled")
                .build();
    }
}
