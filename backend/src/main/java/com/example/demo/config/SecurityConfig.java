package com.example.demo.config;

import com.example.demo.security.JwtFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtFilter jwtFilter;

    public SecurityConfig(JwtFilter jwtFilter) {
        this.jwtFilter = jwtFilter;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .cors(cors -> {})
            .sessionManagement(session ->
                    session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                    // Public auth endpoints
                    .requestMatchers("/auth/**").permitAll()

                    // Public docs
                    .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/swagger-ui.html").permitAll()

                    // Public read endpoints
                    .requestMatchers(HttpMethod.GET, "/hotels/**").permitAll()
                    .requestMatchers(HttpMethod.GET, "/rooms/**").permitAll()
                    .requestMatchers(HttpMethod.GET, "/ratings/**").permitAll()

                    // Admin-only endpoints
                    .requestMatchers("/admin/**").hasRole("ADMIN")
                    .requestMatchers("/users/**").hasRole("ADMIN")
                    .requestMatchers(HttpMethod.POST, "/hotels").hasRole("ADMIN")
                    .requestMatchers(HttpMethod.PUT, "/hotels/**").hasRole("ADMIN")
                    .requestMatchers(HttpMethod.DELETE, "/hotels/**").hasRole("ADMIN")
                    .requestMatchers(HttpMethod.POST, "/rooms").hasRole("ADMIN")
                    .requestMatchers(HttpMethod.DELETE, "/rooms/**").hasRole("ADMIN")

                    // Authenticated write actions
                    .requestMatchers(HttpMethod.POST, "/bookings").authenticated()
                    .requestMatchers(HttpMethod.GET, "/bookings/my").authenticated()
                    .requestMatchers(HttpMethod.PUT, "/bookings/cancel/**").authenticated()
                    .requestMatchers(HttpMethod.GET, "/bookings/summary").authenticated()
                    .requestMatchers(HttpMethod.POST, "/payment/**").authenticated()
                    .requestMatchers(HttpMethod.GET, "/payment/**").authenticated()
                    .requestMatchers(HttpMethod.POST, "/ratings").authenticated()

                    .requestMatchers(HttpMethod.GET, "/wishlist/**").authenticated()
                    .requestMatchers(HttpMethod.POST, "/wishlist").authenticated()
                    .requestMatchers(HttpMethod.DELETE, "/wishlist/**").authenticated()
                    // Anything else
                    .anyRequest().authenticated()
            )
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}