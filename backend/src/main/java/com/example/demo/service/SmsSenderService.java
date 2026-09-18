package com.example.demo.service;

public interface SmsSenderService {
    void sendOtp(String phone, String code);
}