package com.example.demo.service;

import com.example.demo.entity.AuthProvider;
import com.example.demo.entity.PhoneOtp;
import com.example.demo.entity.User;
import com.example.demo.exception.BadRequestException;
import com.example.demo.repository.PhoneOtpRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.security.JwtUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

@Service
public class PhoneOtpService {

    private final PhoneOtpRepository phoneOtpRepository;
    private final UserRepository userRepository;
    private final SmsSenderService smsSenderService;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;

    public PhoneOtpService(
            PhoneOtpRepository phoneOtpRepository,
            UserRepository userRepository,
            SmsSenderService smsSenderService,
            JwtUtil jwtUtil,
            PasswordEncoder passwordEncoder
    ) {
        this.phoneOtpRepository = phoneOtpRepository;
        this.userRepository = userRepository;
        this.smsSenderService = smsSenderService;
        this.jwtUtil = jwtUtil;
        this.passwordEncoder = passwordEncoder;
    }

    public void requestOtp(String rawPhone) {
        String phone = normalizePhone(rawPhone);

        String code = String.format("%06d", new Random().nextInt(1000000));

        PhoneOtp otp = new PhoneOtp();
        otp.setPhone(phone);
        otp.setCode(code);
        otp.setExpiresAt(LocalDateTime.now().plusMinutes(5));
        otp.setUsed(false);

        phoneOtpRepository.save(otp);
        smsSenderService.sendOtp(phone, code);
    }

    public Map<String, Object> verifyOtp(String rawPhone, String code, String name) {
        String phone = normalizePhone(rawPhone);

        PhoneOtp otp = phoneOtpRepository.findTopByPhoneAndUsedFalseOrderByIdDesc(phone)
                .orElseThrow(() -> new BadRequestException("OTP not found"));

        if (otp.isUsed()) {
            throw new BadRequestException("OTP already used");
        }

        if (otp.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("OTP expired");
        }

        if (!otp.getCode().equals(code)) {
            throw new BadRequestException("Invalid OTP");
        }

        otp.setUsed(true);
        phoneOtpRepository.save(otp);

        User user = userRepository.findByPhone(phone)
                .orElseGet(() -> {
                    User u = new User();
                    u.setName(name != null && !name.isBlank() ? name.trim() : "Phone User");
                    u.setPhone(phone);
                    u.setPhoneVerified(true);
                    u.setProvider(AuthProvider.PHONE);
                    u.setPassword(passwordEncoder.encode(UUID.randomUUID().toString()));
                    return userRepository.save(u);
                });

        user.setPhone(phone);
        user.setPhoneVerified(true);
        user.setProvider(AuthProvider.PHONE);
        userRepository.save(user);

        String token = jwtUtil.generateToken(user.getEmail() != null ? user.getEmail() : phone);

        return Map.of(
                "token", token,
                "role", user.getRole().name(),
                "email", user.getEmail() != null ? user.getEmail() : phone,
                "name", user.getName()
        );
    }

    private String normalizePhone(String phone) {
        String normalized = phone.replaceAll("\\s+", "").trim();
        if (!normalized.matches("^\\+?[0-9]{10,15}$")) {
            throw new BadRequestException("Invalid phone number");
        }
        return normalized;
    }
}