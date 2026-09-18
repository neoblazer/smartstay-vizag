package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class WishlistDTO {
    private Long id;
    private Long hotelId;
    private String name;
    private String city;
    private String state;
    private Double rating;
    private String imageUrl;
    private String description;
    private Double minPrice;
}