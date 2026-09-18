package com.example.demo.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class WishlistRequestDTO {
    @NotNull(message = "Hotel ID is required")
    private Long hotelId;
}