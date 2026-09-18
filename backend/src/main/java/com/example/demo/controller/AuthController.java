package com.example.demo.controller;

import com.example.demo.dto.ApiResponse;
import com.example.demo.dto.LoginRequestDTO;
import com.example.demo.dto.LoginResponseDTO;
import com.example.demo.dto.RegisterRequestDTO;
import com.example.demo.dto.UserResponseDTO;
import com.example.demo.entity.User;
import com.example.demo.exception.BadRequestException;
import com.example.demo.exception.UnauthorizedException;
import com.example.demo.repository.UserRepository;
import com.example.demo.security.JwtUtil;
import com.example.demo.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import com.example.demo.dto.GoogleAuthRequestDTO;
import com.example.demo.dto.PhoneOtpRequestDTO;
import com.example.demo.dto.PhoneOtpVerifyDTO;
import com.example.demo.service.GoogleAuthService;
import com.example.demo.service.PhoneOtpService;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/auth")
@Tag(name = "Authentication", description = "Register and Login endpoints")
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private GoogleAuthService googleAuthService;

    @Autowired
    private PhoneOtpService phoneOtpService;

    @PostMapping("/register")
    @Operation(summary = "Register a new user")
    public ResponseEntity<ApiResponse<UserResponseDTO>> register(
            @Valid @RequestBody RegisterRequestDTO request) {

        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(request.getPassword());

        UserResponseDTO response = userService.registerUser(user);
        return ResponseEntity.ok(new ApiResponse<>(true, "User registered successfully", response));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponseDTO>> login(
            @RequestBody LoginRequestDTO request) {

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UnauthorizedException("Invalid email or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new UnauthorizedException("Invalid email or password");
        }

        String token = jwtUtil.generateToken(user.getEmail());

        LoginResponseDTO response = new LoginResponseDTO(
                token,
                user.getRole().name(),
                user.getEmail(),
                user.getName()
        );

        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "Login successful",
                        response
                )
        );
    }
    @PostMapping("/google")
    public ResponseEntity<ApiResponse<Map<String, Object>>> googleLogin(
            @Valid @RequestBody GoogleAuthRequestDTO request
    ) {
        return ResponseEntity.ok(
                new ApiResponse<>(true, "Google login successful", googleAuthService.loginWithGoogle(request.getCredential()))
        );
    }

    @PostMapping("/phone/request-otp")
    public ResponseEntity<ApiResponse<String>> requestPhoneOtp(
            @Valid @RequestBody PhoneOtpRequestDTO request
    ) {
        phoneOtpService.requestOtp(request.getPhone());
        return ResponseEntity.ok(
                new ApiResponse<>(true, "OTP sent successfully", "OTP sent")
        );
    }

    @PostMapping("/phone/verify-otp")
    public ResponseEntity<ApiResponse<Map<String, Object>>> verifyPhoneOtp(
            @Valid @RequestBody PhoneOtpVerifyDTO request
    ) {
        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "Phone login successful",
                        phoneOtpService.verifyOtp(request.getPhone(), request.getOtp(), request.getName())
                )
        );
    }
    
    @PostMapping("/phone/firebase")
    public ResponseEntity<ApiResponse<Map<String, Object>>> firebasePhoneLogin(
            @RequestBody Map<String, String> body
    ) {
        String phone = body.get("phone");
        String name = body.getOrDefault("name", "").trim();

        if (phone == null || phone.isBlank()) {
            throw new BadRequestException("Phone number is required");
        }

        User user = userRepository.findByPhone(phone).orElseGet(() -> {
            User u = new User();
            u.setPhone(phone);
            u.setName(!name.isBlank() ? name : "User");
            u.setPassword(passwordEncoder.encode(UUID.randomUUID().toString()));
            u.setRole(com.example.demo.entity.Role.USER);
            return userRepository.save(u);
        });

        if ((user.getName() == null || user.getName().isBlank()) && !name.isBlank()) {
            user.setName(name);
            userRepository.save(user);
        }

        String token = jwtUtil.generateToken(
                user.getEmail() != null && !user.getEmail().isBlank() ? user.getEmail() : phone
        );

        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "Phone login successful",
                        Map.of(
                                "token", token,
                                "role", user.getRole().name(),
                                "name", user.getName(),
                                "email", user.getEmail() != null ? user.getEmail() : phone
                        )
                )
        );
    }
}