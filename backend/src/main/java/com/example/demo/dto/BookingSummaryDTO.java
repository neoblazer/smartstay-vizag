package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class BookingSummaryDTO {

    private long totalBookings;
    private long activeBookings;
    private long cancelledBookings;
    private double totalSpent;
}