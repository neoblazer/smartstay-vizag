package com.example.demo.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.example.demo.entity.Room;

public interface RoomRepository extends JpaRepository<Room, Long> {

    List<Room> findByHotelId(Long hotelId);

    List<Room> findByIdNotIn(List<Long> ids);

    @Query("SELECT MIN(r.basePrice) FROM Room r WHERE r.hotel.id = :hotelId")
    Optional<Double> findMinPriceByHotelId(Long hotelId);
}