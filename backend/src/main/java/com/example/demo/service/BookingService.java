package com.example.demo.service;

import com.example.demo.dto.AdminBookingDTO;
import com.example.demo.dto.BookingDTO;
import com.example.demo.dto.BookingSummaryDTO;
import com.example.demo.dto.BookingRequestDTO;
import com.example.demo.entity.*;
import com.example.demo.exception.*;
import com.example.demo.repository.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class BookingService {

    private final BookingRepository bookingRepository;
    private final RoomRepository roomRepository;
    private final UserRepository userRepository;

    public BookingService(BookingRepository bookingRepository,
                          RoomRepository roomRepository,
                          UserRepository userRepository) {
        this.bookingRepository = bookingRepository;
        this.roomRepository = roomRepository;
        this.userRepository = userRepository;
    }

    private String getCurrentPrincipal() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof UserDetails ud) return ud.getUsername();
        return principal.toString();
    }

    private User getCurrentUser() {
        String principal = getCurrentPrincipal();
        return userRepository.findByEmail(principal)
                .or(() -> userRepository.findByPhone(principal))
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    @Transactional
    public Booking createBooking(BookingRequestDTO request) {
        User user = getCurrentUser();
        LocalDate checkIn = request.getCheckInDate();
        LocalDate checkOut = request.getCheckOutDate();

        if (checkIn == null || checkOut == null) {
            throw new BadRequestException("Check-in and check-out dates are required");
        }
        if (!checkOut.isAfter(checkIn)) {
            throw new BadRequestException("Check-out must be after check-in");
        }
        if (checkIn.isBefore(LocalDate.now())) {
            throw new BadRequestException("Check-in date cannot be in the past");
        }

        Room room = roomRepository.findById(request.getRoomId())
                .orElseThrow(() -> new ResourceNotFoundException("Room not found"));

        int count = bookingRepository.countBookingsForRoom(room.getId(), checkIn, checkOut);
        if (count >= room.getTotalRooms()) {
            throw new BadRequestException("Room is not available for the selected dates");
        }

        long nights = ChronoUnit.DAYS.between(checkIn, checkOut);

        Booking booking = new Booking();
        booking.setUser(user);
        booking.setRoom(room);
        booking.setHotel(room.getHotel());
        booking.setCheckInDate(checkIn);
        booking.setCheckOutDate(checkOut);
        booking.setTotalPrice(room.getBasePrice() * nights);
        booking.setStatus(BookingStatus.PENDING);

        return bookingRepository.save(booking);
    }

    @Transactional(readOnly = true)
    public List<AdminBookingDTO> getAllBookingsForAdmin() {
        return bookingRepository.findAll()
                .stream()
                .map(b -> new AdminBookingDTO(
                        b.getId(),
                        b.getUser() != null ? b.getUser().getName() : "—",
                        b.getHotel() != null ? b.getHotel().getName() : "—",
                        b.getRoom() != null ? b.getRoom().getType() : "—",
                        b.getCheckInDate(),
                        b.getCheckOutDate(),
                        b.getTotalPrice(),
                        b.getStatus().name()
                ))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<BookingDTO> getMyBookings() {
        User user = getCurrentUser();

        return bookingRepository.findByUserId(user.getId())
                .stream()
                .map(b -> new BookingDTO(
                        b.getId(),
                        b.getHotel() != null ? b.getHotel().getName() : "N/A",
                        b.getHotel() != null ? b.getHotel().getImageUrl() : null,
                        b.getRoom() != null ? b.getRoom().getType() : "N/A",
                        b.getCheckInDate(),
                        b.getCheckOutDate(),
                        b.getTotalPrice(),
                        b.getStatus().name()
                ))
                .collect(Collectors.toList());
    }

    @Transactional
    public Booking cancelBooking(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));

        String currentPrincipal = getCurrentPrincipal();
        String ownerPrincipal = booking.getUser().getEmail() != null && !booking.getUser().getEmail().isBlank()
                ? booking.getUser().getEmail()
                : booking.getUser().getPhone();

        boolean isAdmin = SecurityContextHolder.getContext().getAuthentication()
                .getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (!isAdmin && !ownerPrincipal.equals(currentPrincipal)) {
            throw new UnauthorizedException("You cannot cancel this booking");
        }

        if (booking.getStatus() == BookingStatus.CANCELLED) {
            throw new BadRequestException("Booking is already cancelled");
        }

        booking.setStatus(BookingStatus.CANCELLED);
        return bookingRepository.save(booking);
    }

    @Transactional(readOnly = true)
    public BookingSummaryDTO getMyBookingSummary() {
        User user = getCurrentUser();

        List<Booking> bookings = bookingRepository.findByUserId(user.getId());

        long total = bookings.size();
        long cancelled = bookings.stream()
                .filter(b -> b.getStatus() == BookingStatus.CANCELLED)
                .count();
        long active = total - cancelled;
        double spent = bookings.stream()
                .filter(b -> b.getStatus() != BookingStatus.CANCELLED)
                .mapToDouble(Booking::getTotalPrice)
                .sum();

        return new BookingSummaryDTO(total, active, cancelled, spent);
    }
}