package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AdminStats {

    private long totalUsers;
    private long totalHotels;
    private long totalRooms;
    private long totalBookings;
    private double totalRevenue;
}