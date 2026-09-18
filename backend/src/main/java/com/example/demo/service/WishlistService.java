package com.example.demo.service;

import com.example.demo.dto.WishlistDTO;
import com.example.demo.entity.Hotel;
import com.example.demo.entity.Room;
import com.example.demo.entity.User;
import com.example.demo.entity.Wishlist;
import com.example.demo.exception.BadRequestException;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.repository.HotelRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.repository.WishlistRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class WishlistService {

    private final WishlistRepository wishlistRepository;
    private final UserRepository userRepository;
    private final HotelRepository hotelRepository;

    public WishlistService(
            WishlistRepository wishlistRepository,
            UserRepository userRepository,
            HotelRepository hotelRepository
    ) {
        this.wishlistRepository = wishlistRepository;
        this.userRepository = userRepository;
        this.hotelRepository = hotelRepository;
    }

    private String getCurrentUserEmail() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof UserDetails ud) return ud.getUsername();
        return principal.toString();
    }

    @Transactional
    public WishlistDTO addToWishlist(Long hotelId) {
        User user = getCurrentUser();

        if (wishlistRepository.existsByUserIdAndHotelId(user.getId(), hotelId)) {
            throw new BadRequestException("Hotel already in wishlist");
        }

        Hotel hotel = hotelRepository.findById(hotelId)
                .orElseThrow(() -> new ResourceNotFoundException("Hotel not found"));

        Wishlist wishlist = new Wishlist();
        wishlist.setUser(user);
        wishlist.setHotel(hotel);

        Wishlist saved = wishlistRepository.save(wishlist);
        return convertToDTO(saved);
    }

    @Transactional(readOnly = true)
    public List<WishlistDTO> getMyWishlist() {
        User user = getCurrentUser();
        return wishlistRepository.findByUserId(user.getId())
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public void removeFromWishlist(Long hotelId) {
        User user = getCurrentUser();

        if (!wishlistRepository.existsByUserIdAndHotelId(user.getId(), hotelId)) {
            throw new ResourceNotFoundException("Hotel not found in wishlist");
        }

        wishlistRepository.deleteByUserIdAndHotelId(user.getId(), hotelId);
    }

    @Transactional(readOnly = true)
    public boolean isInWishlist(Long hotelId) {
        User user = getCurrentUser();
        return wishlistRepository.existsByUserIdAndHotelId(user.getId(), hotelId);
    }

    private WishlistDTO convertToDTO(Wishlist wishlist) {
        Hotel hotel = wishlist.getHotel();

        Double minPrice = null;
        if (hotel.getRooms() != null && !hotel.getRooms().isEmpty()) {
            minPrice = hotel.getRooms().stream()
                    .map(Room::getBasePrice)
                    .min(Double::compareTo)
                    .orElse(null);
        }

        return new WishlistDTO(
                wishlist.getId(),
                hotel.getId(),
                hotel.getName(),
                hotel.getCity(),
                hotel.getState(),
                hotel.getRating(),
                hotel.getImageUrl(),
                hotel.getDescription(),
                minPrice
        );
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
}