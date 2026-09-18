package com.example.demo.service;

import com.example.demo.dto.HotelDTO;
import com.example.demo.entity.Hotel;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.repository.HotelRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public class HotelService {

    @Autowired
    private HotelRepository hotelRepository;

    public Hotel createHotel(Hotel hotel) {
        return hotelRepository.save(hotel);
    }

    public Hotel updateHotel(Long id, Hotel updated) {
        Hotel hotel = hotelRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Hotel not found"));

        hotel.setName(updated.getName());
        hotel.setCity(updated.getCity());
        hotel.setState(updated.getState());
        hotel.setRating(updated.getRating());
        hotel.setImageUrl(updated.getImageUrl());
        hotel.setDescription(updated.getDescription());

        return hotelRepository.save(hotel);
    }

    public void deleteHotel(Long id) {
        if (!hotelRepository.existsById(id)) {
            throw new ResourceNotFoundException("Hotel not found");
        }
        hotelRepository.deleteById(id);
    }

    public Hotel getHotelById(Long id) {
        return hotelRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Hotel not found"));
    }

    public Page<HotelDTO> getAllHotels(Pageable pageable) {
        return hotelRepository.findAllWithMinPrice(pageable);
    }

    public Page<HotelDTO> searchHotels(String city, Pageable pageable) {
        return hotelRepository.searchByCity(city, pageable);
    }

    public Page<HotelDTO> findHotelsByPriceRange(double min, double max, Pageable pageable) {
        return hotelRepository.findByPriceRange(min, max, pageable);
    }

    public Page<HotelDTO> getTopHotels(Pageable pageable) {
        return hotelRepository.findTopHotels(pageable);
    }

    public Page<HotelDTO> searchAdvanced(
            String city,
            Double min,
            Double max,
            Double rating,
            Integer capacity,
            LocalDate checkIn,
            LocalDate checkOut,
            Pageable pageable
    ) {
        return hotelRepository.searchAdvanced(city, min, max, rating, capacity, checkIn, checkOut, pageable);
    }
}