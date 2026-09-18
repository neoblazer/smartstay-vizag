package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class RoomDTO {
    private Long id;
    private String type;
    private double basePrice;
    private String hotelName;
    private Long hotelId;           // ← NEW field (was missing in old version)
    private String imageUrl;        // ← NEW field (was missing in old version)
    private List<String> amenities; // ← NEW field (was missing in old version)
    private Double rating;          // ← NEW field (was missing in old version)
    private Integer capacity;       // ← NEW field (was missing in old version)
    private int totalRooms;         // ← NEW field (was missing in old version)
}