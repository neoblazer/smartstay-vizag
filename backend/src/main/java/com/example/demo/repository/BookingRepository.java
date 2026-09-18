package com.example.demo.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.demo.entity.Booking;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    @Query("SELECT COUNT(b) FROM Booking b WHERE b.room.id = :roomId AND " +
           "(:checkIn < b.checkOutDate AND :checkOut > b.checkInDate)")
    int countBookingsForRoom(@Param("roomId") Long roomId,
                             @Param("checkIn") LocalDate checkIn,
                             @Param("checkOut") LocalDate checkOut);

    @Query("SELECT b.room.id FROM Booking b WHERE :checkIn < b.checkOutDate AND :checkOut > b.checkInDate")
    List<Long> findBookedRoomIds(@Param("checkIn") LocalDate checkIn,
                                 @Param("checkOut") LocalDate checkOut);

    List<Booking> findByUserId(Long userId);

    @Query("""
    	    SELECT COALESCE(SUM(b.totalPrice), 0)
    	    FROM Booking b
    	    WHERE b.status <> com.example.demo.entity.BookingStatus.CANCELLED
    	""")
    	Double getTotalRevenue();
}