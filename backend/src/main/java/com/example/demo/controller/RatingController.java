package com.example.demo.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.demo.dto.ApiResponse;
import com.example.demo.entity.Rating;
import com.example.demo.repository.RatingRepository;

@RestController
@RequestMapping("/ratings")
@CrossOrigin("*")
public class RatingController {

    @Autowired
    private RatingRepository ratingRepository;

    @PostMapping
    public ResponseEntity<ApiResponse<Rating>> addRating(@RequestBody Rating rating) {
        Rating savedRating = ratingRepository.save(rating);
        return ResponseEntity.ok(
                new ApiResponse<Rating>(true, "Rating added successfully", savedRating)
        );
    }

    @GetMapping("/{hotelId}")
    public ResponseEntity<ApiResponse<List<Rating>>> getRatings(@PathVariable Long hotelId) {
        List<Rating> ratings = ratingRepository.findByHotelId(hotelId);
        return ResponseEntity.ok(
                new ApiResponse<List<Rating>>(true, "Ratings fetched successfully", ratings)
        );
    }
}