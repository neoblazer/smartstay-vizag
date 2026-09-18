package com.example.demo.service;

import com.example.demo.dto.GoogleClaimsDTO;
import com.example.demo.entity.AuthProvider;
import com.example.demo.entity.User;
import com.example.demo.exception.BadRequestException;
import com.example.demo.repository.UserRepository;
import com.example.demo.security.JwtUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.UUID;

@Service
public class GoogleAuthService {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    @Value("${google.client-id}")
    private String googleClientId;

    public GoogleAuthService(UserRepository userRepository, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
    }

    public Map<String, Object> loginWithGoogle(String credential) {
        RestTemplate restTemplate = new RestTemplate();

        GoogleClaimsDTO claims = restTemplate.getForObject(
                "https://oauth2.googleapis.com/tokeninfo?id_token={token}",
                GoogleClaimsDTO.class,
                credential
        );

        if (claims == null || claims.getEmail() == null) {
            throw new BadRequestException("Invalid Google token");
        }

        if (!googleClientId.equals(claims.getAud())) {
            throw new BadRequestException("Google token audience mismatch");
        }

        if (!"true".equalsIgnoreCase(claims.getEmail_verified())) {
            throw new BadRequestException("Google email is not verified");
        }

        User user = userRepository.findByEmail(claims.getEmail()).orElseGet(() -> {
            User u = new User();
            u.setName(claims.getName() != null ? claims.getName() : "Google User");
            u.setEmail(claims.getEmail());
            u.setPassword(UUID.randomUUID().toString());
            u.setProvider(AuthProvider.GOOGLE);
            u.setProviderId(claims.getSub());
            return userRepository.save(u);
        });

        user.setProvider(AuthProvider.GOOGLE);
        user.setProviderId(claims.getSub());
        userRepository.save(user);

        String token = jwtUtil.generateToken(user.getEmail());

        return Map.of(
                "token", token,
                "role", user.getRole().name(),
                "email", user.getEmail(),
                "name", user.getName()
        );
    }
}