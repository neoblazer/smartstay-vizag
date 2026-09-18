package com.example.demo.service;

import com.example.demo.dto.RoomDTO;
import com.example.demo.entity.Room;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.repository.BookingRepository;
import com.example.demo.repository.RoomRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class RoomService {

    private final RoomRepository roomRepository;
    private final BookingRepository bookingRepository;

    public RoomService(RoomRepository roomRepository, BookingRepository bookingRepository) {
        this.roomRepository = roomRepository;
        this.bookingRepository = bookingRepository;
    }

    public Room createRoom(Room room) {
        return roomRepository.save(room);
    }

    public List<Room> getAllRooms() {
        return roomRepository.findAll();
    }

    public Room getRoomById(Long id) {
        return roomRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found"));
    }

    public void deleteRoom(Long id) {
        if (!roomRepository.existsById(id)) {
            throw new ResourceNotFoundException("Room not found");
        }
        roomRepository.deleteById(id);
    }

    public List<Room> getAvailableRooms(LocalDate checkIn, LocalDate checkOut) {
        if (!checkOut.isAfter(checkIn)) {
            throw new IllegalArgumentException("Check-out must be after check-in");
        }
        List<Long> bookedRoomIds = bookingRepository.findBookedRoomIds(checkIn, checkOut);
        List<Room> candidates = bookedRoomIds.isEmpty()
                ? roomRepository.findAll()
                : roomRepository.findByIdNotIn(bookedRoomIds);
        return candidates.stream()
                .filter(r -> r.getTotalRooms() > bookingRepository.countBookingsForRoom(r.getId(), checkIn, checkOut))
                .collect(Collectors.toList());
    }

    public List<RoomDTO> getRoomsByHotel(Long hotelId) {
        return roomRepository.findByHotelId(hotelId)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    private RoomDTO convertToDTO(Room room) {
        List<String> amenitiesList = (room.getAmenities() != null && !room.getAmenities().isBlank())
                ? Arrays.asList(room.getAmenities().split(","))
                : Collections.emptyList();

        return new RoomDTO(
                room.getId(),
                room.getType(),
                room.getBasePrice(),
                room.getHotel() != null ? room.getHotel().getName() : "",
                room.getHotel() != null ? room.getHotel().getId() : null,
                room.getImageUrl(),
                amenitiesList,
                room.getRating(),
                room.getCapacity(),
                room.getTotalRooms()
        );
    }
}