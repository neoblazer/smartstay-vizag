package com.example.demo.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GoogleClaimsDTO {
    private String sub;
    private String email;
    private String email_verified;
    private String name;
    private String aud;
    private String iss;
    private String exp;
}