package com.example.demo.service;

import org.springframework.stereotype.Service;
import com.example.demo.dto.AdminStats;
import com.example.demo.repository.*;

@Service
public class AdminService {

    private final UserRepository userRepository;
    private final HotelRepository hotelRepository;
    private final RoomRepository roomRepository;
    private final BookingRepository bookingRepository;

    public AdminService(UserRepository userRepository,
                        HotelRepository hotelRepository,
                        RoomRepository roomRepository,
                        BookingRepository bookingRepository) {
        this.userRepository = userRepository;
        this.hotelRepository = hotelRepository;
        this.roomRepository = roomRepository;
        this.bookingRepository = bookingRepository;
    }

    public AdminStats getStats() {
        long users = userRepository.count();
        long hotels = hotelRepository.count();
        long rooms = roomRepository.count();
        long bookings = bookingRepository.count();

        Double revenue = bookingRepository.getTotalRevenue();

        return new AdminStats(
                users,
                hotels,
                rooms,
                bookings,
                revenue != null ? revenue : 0.0
        );
    }
}