package com.example.demo.repository;

import com.example.demo.dto.HotelDTO;
import com.example.demo.entity.Hotel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;

public interface HotelRepository extends JpaRepository<Hotel, Long> {

    @Query("""
        SELECT new com.example.demo.dto.HotelDTO(
            h.id, h.name, h.city, h.state, h.rating,
            h.imageUrl, h.description,
            MIN(r.basePrice)
        )
        FROM Hotel h
        LEFT JOIN h.rooms r
        GROUP BY h.id, h.name, h.city, h.state, h.rating, h.imageUrl, h.description
    """)
    Page<HotelDTO> findAllWithMinPrice(Pageable pageable);

    @Query("""
        SELECT new com.example.demo.dto.HotelDTO(
            h.id, h.name, h.city, h.state, h.rating,
            h.imageUrl, h.description,
            MIN(r.basePrice)
        )
        FROM Hotel h
        LEFT JOIN h.rooms r
        WHERE LOWER(h.city) LIKE LOWER(CONCAT('%', :city, '%'))
        GROUP BY h.id, h.name, h.city, h.state, h.rating, h.imageUrl, h.description
    """)
    Page<HotelDTO> searchByCity(@Param("city") String city, Pageable pageable);

    @Query("""
        SELECT new com.example.demo.dto.HotelDTO(
            h.id, h.name, h.city, h.state, h.rating,
            h.imageUrl, h.description,
            MIN(r.basePrice)
        )
        FROM Hotel h
        JOIN h.rooms r
        WHERE r.basePrice BETWEEN :min AND :max
        GROUP BY h.id, h.name, h.city, h.state, h.rating, h.imageUrl, h.description
    """)
    Page<HotelDTO> findByPriceRange(
            @Param("min") double min,
            @Param("max") double max,
            Pageable pageable
    );

    @Query("""
        SELECT new com.example.demo.dto.HotelDTO(
            h.id, h.name, h.city, h.state, h.rating,
            h.imageUrl, h.description,
            MIN(r.basePrice)
        )
        FROM Hotel h
        LEFT JOIN h.rooms r
        GROUP BY h.id, h.name, h.city, h.state, h.rating, h.imageUrl, h.description
        ORDER BY h.rating DESC
    """)
    Page<HotelDTO> findTopHotels(Pageable pageable);

    @Query("""
        SELECT new com.example.demo.dto.HotelDTO(
            h.id, h.name, h.city, h.state, h.rating,
            h.imageUrl, h.description,
            MIN(r.basePrice)
        )
        FROM Hotel h
        JOIN h.rooms r
        WHERE (:city IS NULL OR LOWER(h.city) LIKE LOWER(CONCAT('%', :city, '%')))
          AND (:min IS NULL OR r.basePrice >= :min)
          AND (:max IS NULL OR r.basePrice <= :max)
          AND (:rating IS NULL OR h.rating >= :rating)
          AND (:capacity IS NULL OR r.capacity >= :capacity)
          AND (
                :checkIn IS NULL OR :checkOut IS NULL OR
                (
                  SELECT COUNT(b)
                  FROM Booking b
                  WHERE b.room.id = r.id
                    AND b.status <> com.example.demo.entity.BookingStatus.CANCELLED
                    AND (:checkIn < b.checkOutDate AND :checkOut > b.checkInDate)
                ) < r.totalRooms
              )
        GROUP BY h.id, h.name, h.city, h.state, h.rating, h.imageUrl, h.description
    """)
    Page<HotelDTO> searchAdvanced(
            @Param("city") String city,
            @Param("min") Double min,
            @Param("max") Double max,
            @Param("rating") Double rating,
            @Param("capacity") Integer capacity,
            @Param("checkIn") LocalDate checkIn,
            @Param("checkOut") LocalDate checkOut,
            Pageable pageable
    );
}