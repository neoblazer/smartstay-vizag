package com.example.demo.controller;

import com.example.demo.dto.ApiResponse;
import com.example.demo.dto.WishlistDTO;
import com.example.demo.dto.WishlistRequestDTO;
import com.example.demo.service.WishlistService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/wishlist")
public class WishlistController {

    private final WishlistService wishlistService;

    public WishlistController(WishlistService wishlistService) {
        this.wishlistService = wishlistService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<WishlistDTO>> addToWishlist(
            @Valid @RequestBody WishlistRequestDTO request
    ) {
        return ResponseEntity.ok(
                new ApiResponse<>(true, "Added to wishlist", wishlistService.addToWishlist(request.getHotelId()))
        );
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<WishlistDTO>>> getMyWishlist() {
        return ResponseEntity.ok(
                new ApiResponse<>(true, "Wishlist fetched", wishlistService.getMyWishlist())
        );
    }

    @DeleteMapping("/{hotelId}")
    public ResponseEntity<ApiResponse<String>> removeFromWishlist(@PathVariable Long hotelId) {
        wishlistService.removeFromWishlist(hotelId);
        return ResponseEntity.ok(
                new ApiResponse<>(true, "Removed from wishlist", "Removed successfully")
        );
    }

    @GetMapping("/check/{hotelId}")
    public ResponseEntity<ApiResponse<Map<String, Boolean>>> checkWishlist(@PathVariable Long hotelId) {
        boolean exists = wishlistService.isInWishlist(hotelId);
        return ResponseEntity.ok(
                new ApiResponse<>(true, "Wishlist status fetched", Map.of("inWishlist", exists))
        );
    }
}