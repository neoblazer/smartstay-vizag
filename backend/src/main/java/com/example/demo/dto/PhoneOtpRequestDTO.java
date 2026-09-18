package com.example.demo.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PhoneOtpRequestDTO {
    @NotBlank(message = "Phone number is required")
    private String phone;
}