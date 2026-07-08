package com.hourai.prts.controller;

import com.hourai.prts.entity.User;
import com.hourai.prts.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/admin")
public class AdminController {
    private final UserService userService;

    public AdminController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/users")
    public ResponseEntity<?> listUsers(@RequestParam(defaultValue = "") String q) {
        List<User> users = userService.searchUsers(q);
        List<Map<String, Object>> result = users.stream().map(u -> {
            Map<String, Object> m = new HashMap<>();
            m.put("id", u.getId());
            m.put("username", u.getUsername());
            m.put("isAdmin", u.getIsAdmin());
            m.put("createdAt", u.getCreatedAt() != null ? u.getCreatedAt().toString() : null);
            return m;
        }).collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }

    @PostMapping("/user/permission")
    public ResponseEntity<?> setPermission(@RequestParam Long actorId,
                                           @RequestParam Long targetId,
                                           @RequestParam String makeAdmin,
                                           Authentication auth) {
        try {
            boolean makeAdminBool = "true".equals(makeAdmin) || "1".equals(makeAdmin);
            boolean changed = userService.setAdminStatus(actorId, targetId, makeAdminBool);
            String msg = changed
                    ? (makeAdminBool ? "promoted to admin" : "demoted from admin")
                    : "no change needed";
            return ResponseEntity.ok(Map.of("success", true, "message", msg));
        } catch (RuntimeException e) {
            String msg = e.getMessage();
            if ("not admin".equals(msg)) return ResponseEntity.status(403).body(Map.of("error", msg));
            if (msg.contains("super admin")) return ResponseEntity.status(403).body(Map.of("error", msg));
            return ResponseEntity.internalServerError().body(Map.of("error", msg));
        }
    }
}
