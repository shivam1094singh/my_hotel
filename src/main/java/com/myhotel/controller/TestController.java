package com.myhotel.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/test")
public class TestController {

    @GetMapping("/public")
    public ResponseEntity<String> publicEndpoint() {
        return ResponseEntity.ok("This is a public endpoint - accessible to everyone");
    }

    @GetMapping("/user")
    public ResponseEntity<String> userEndpoint() {
        return ResponseEntity.ok("This is a user endpoint - accessible to authenticated users");
    }

    @GetMapping("/admin")
    public ResponseEntity<String> adminEndpoint() {
        return ResponseEntity.ok("This is an admin endpoint - accessible to ADMIN users only");
    }

    @GetMapping("/manager")
    public ResponseEntity<String> managerEndpoint() {
        return ResponseEntity.ok("This is a manager endpoint - accessible to HOTEL_MANAGER users only");
    }
}
