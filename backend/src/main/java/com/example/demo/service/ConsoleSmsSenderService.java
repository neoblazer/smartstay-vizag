package com.example.demo.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class ConsoleSmsSenderService implements SmsSenderService {

    private static final Logger log = LoggerFactory.getLogger(ConsoleSmsSenderService.class);

    @Override
    public void sendOtp(String phone, String code) {
        log.info("OTP for {} is {}", phone, code);
    }
}