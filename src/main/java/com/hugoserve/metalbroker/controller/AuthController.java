package com.hugoserve.metalbroker.controller;

import com.hugoserve.metalbroker.security.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody String body) {
        return ResponseEntity.ok(authService.register(body));
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody String body) {
        return ResponseEntity.ok(authService.login(body));
    }

    @PostMapping("/refresh")
    public ResponseEntity<String> refresh(@RequestBody String body) {
        return ResponseEntity.ok(authService.refresh(body));
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(@RequestBody String body) {
        return ResponseEntity.ok(authService.logout(body));
    }
}