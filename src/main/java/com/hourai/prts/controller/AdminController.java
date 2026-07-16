package com.hourai.prts.controller;

import com.hourai.prts.common.Result;
import com.hourai.prts.common.ResultCode;
import com.hourai.prts.entity.User;
import com.hourai.prts.service.UserService;
import org.springframework.http.HttpStatus;
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
    public ResponseEntity<Result<List<Map<String, Object>>>> listUsers(@RequestParam(defaultValue = "") String q) {
        List<User> users = userService.searchUsers(q);
        List<Map<String, Object>> result = users.stream().map(u -> {
            Map<String, Object> m = new HashMap<>();
            m.put("id", u.getId());
            m.put("username", u.getUsername());
            m.put("isAdmin", u.getIsAdmin());
            m.put("createdAt", u.getCreatedAt() != null ? u.getCreatedAt().toString() : null);
            return m;
        }).collect(Collectors.toList());
        return ResponseEntity.ok(Result.success(result));
    }

    @PostMapping("/user/permission")
    public ResponseEntity<Result<Map<String, Object>>> setPermission(@RequestParam Long actorId,
                                                                     @RequestParam Long targetId,
                                                                     @RequestParam String makeAdmin,
                                                                     Authentication auth) {
        try {
            boolean makeAdminBool = "true".equals(makeAdmin) || "1".equals(makeAdmin);
            boolean changed = userService.setAdminStatus(actorId, targetId, makeAdminBool);
            String msg = changed
                    ? (makeAdminBool ? "已设为管理员" : "已降为普通用户")
                    : "状态未发生变化";
            return ResponseEntity.ok(Result.success(Map.of("message", msg)));
        } catch (RuntimeException e) {
            String msg = e.getMessage();
            if ("not admin".equals(msg) || (msg != null && msg.contains("super admin"))) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Result.fail(ResultCode.FORBIDDEN, msg));
            }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Result.fail(ResultCode.INTERNAL_ERROR, msg));
        }
    }
}
