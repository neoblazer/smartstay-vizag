package com.example.demo.controller;

import com.example.demo.dto.*;
import com.example.demo.entity.Booking;
import com.example.demo.service.BookingService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/bookings")
public class BookingController {

    @Autowired
    private BookingService bookingService;

    @PostMapping
    public ResponseEntity<ApiResponse<Booking>> createBooking(
            @Valid @RequestBody BookingRequestDTO request) {
        Booking booking = bookingService.createBooking(request);
        return ResponseEntity.ok(new ApiResponse<>(true, "Booking created successfully", booking));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<AdminBookingDTO>>> getAllBookings() {
        return ResponseEntity.ok(
                new ApiResponse<>(true, "All bookings fetched", bookingService.getAllBookingsForAdmin())
        );
    }

    @GetMapping("/my")
    public ResponseEntity<ApiResponse<List<BookingDTO>>> getMyBookings() {
        return ResponseEntity.ok(new ApiResponse<>(true, "My bookings fetched", bookingService.getMyBookings()));
    }

    @PutMapping("/cancel/{id}")
    public ResponseEntity<ApiResponse<Booking>> cancelBooking(@PathVariable Long id) {
        return ResponseEntity.ok(new ApiResponse<>(true, "Booking cancelled", bookingService.cancelBooking(id)));
    }

    @GetMapping("/summary")
    public ResponseEntity<ApiResponse<BookingSummaryDTO>> getSummary() {
        return ResponseEntity.ok(new ApiResponse<>(true, "Summary fetched", bookingService.getMyBookingSummary()));
    }
}