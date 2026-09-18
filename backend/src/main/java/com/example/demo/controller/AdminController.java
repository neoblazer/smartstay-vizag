package com.example.demo.controller;

import com.example.demo.dto.AdminStats;
import com.example.demo.dto.ApiResponse;
import com.example.demo.service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    @Autowired
    private AdminService adminService;

    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<AdminStats>> getStats() {
        return ResponseEntity.ok(new ApiResponse<>(true, "Stats fetched", adminService.getStats()));
    }
}