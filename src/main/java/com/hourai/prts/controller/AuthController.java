package com.hourai.prts.controller;

import com.hourai.prts.dto.ApiResponse;
import com.hourai.prts.dto.LoginRequest;
import com.hourai.prts.dto.RegisterRequest;
import com.hourai.prts.entity.User;
import com.hourai.prts.security.JwtTokenProvider;
import com.hourai.prts.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
    private final UserService userService;
    private final JwtTokenProvider tokenProvider;

    public AuthController(UserService userService, JwtTokenProvider tokenProvider) {
        this.userService = userService;
        this.tokenProvider = tokenProvider;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
        try {
            User user = userService.register(request.getUsername(), request.getPassword(), request.getEmail());
            return ResponseEntity.ok(Map.of(
                "success", true,
                "id", user.getId(),
                "userId", user.getId(),
                "username", user.getUsername()
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        Optional<User> userOpt = userService.getByUsername(request.getUsername());
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(401).body(Map.of("success", false, "message", "invalid credentials"));
        }
        User user = userOpt.get();
        if (!userService.verifyPassword(request.getPassword(), user.getPassword())) {
            return ResponseEntity.status(401).body(Map.of("success", false, "message", "invalid credentials"));
        }
        if (!user.getStatus()) {
            return ResponseEntity.status(403).body(Map.of("success", false, "message", "account disabled"));
        }

        String token = tokenProvider.generateToken(user.getId(), user.getUsername(), user.getIsAdmin());
        return ResponseEntity.ok(Map.of(
            "success", true,
            "message", "登录成功",
            "token", token,
            "user", Map.of(
                "id", user.getId(),
                "username", user.getUsername(),
                "isAdmin", user.getIsAdmin()
            )
        ));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        return ResponseEntity.ok(Map.of("success", true, "message", "logged out"));
    }

    @GetMapping("/profile")
    public ResponseEntity<?> profile(Authentication auth) {
        if (auth == null) {
            return ResponseEntity.status(401).body(Map.of("success", false, "message", "missing token"));
        }
        Long userId = (Long) auth.getPrincipal();
        Optional<User> userOpt = userService.getById(userId);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(401).body(Map.of("success", false, "message", "user not found"));
        }
        User user = userOpt.get();
        return ResponseEntity.ok(Map.of(
            "id", user.getId(),
            "username", user.getUsername(),
            "isAdmin", user.getIsAdmin()
        ));
    }
}
