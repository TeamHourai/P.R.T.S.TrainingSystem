package com.hourai.prts.controller;

import com.hourai.prts.common.BusinessException;
import com.hourai.prts.common.Result;
import com.hourai.prts.common.ResultCode;
import com.hourai.prts.dto.LoginRequest;
import com.hourai.prts.dto.RegisterRequest;
import com.hourai.prts.entity.User;
import com.hourai.prts.security.JwtTokenProvider;
import com.hourai.prts.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

/**
 * 认证接口：注册、登录、退出和当前用户资料。
 *
 * <p>登录成功后签发 JWT；后续请求通过 Bearer Token 恢复用户身份。
 */
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
    public ResponseEntity<Result<Map<String, Object>>> register(@Valid @RequestBody RegisterRequest request) {
        try {
            User user = userService.register(request.getUsername(), request.getPassword(), request.getEmail());
            Map<String, Object> data = Map.of(
                    "id", user.getId(),
                    "userId", user.getId(),
                    "username", user.getUsername()
            );
            return ResponseEntity.ok(Result.success("注册成功", data));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Result.fail(ResultCode.BAD_REQUEST, e.getMessage()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<Result<Map<String, Object>>> login(@Valid @RequestBody LoginRequest request) {
        Optional<User> userOpt = userService.getByUsername(request.getUsername());
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Result.fail(ResultCode.UNAUTHORIZED, "用户名或密码错误"));
        }
        User user = userOpt.get();
        if (!userService.verifyPassword(request.getPassword(), user.getPassword())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Result.fail(ResultCode.UNAUTHORIZED, "用户名或密码错误"));
        }
        if (!user.getStatus()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Result.fail(ResultCode.FORBIDDEN, "账号已被禁用，请联系管理员"));
        }

        String token = tokenProvider.generateToken(user.getId(), user.getUsername(), user.getIsAdmin());
        Map<String, Object> userMap = Map.of(
                "id", user.getId(),
                "username", user.getUsername(),
                "isAdmin", user.getIsAdmin()
        );
        Map<String, Object> data = Map.of("token", token, "user", userMap);
        return ResponseEntity.ok(Result.success("登录成功", data));
    }

    @PostMapping("/logout")
    public ResponseEntity<Result<Void>> logout() {
        return ResponseEntity.ok(Result.success("已退出登录", null));
    }

    @GetMapping("/profile")
    public ResponseEntity<Result<Map<String, Object>>> profile(Authentication auth) {
        if (auth == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Result.fail(ResultCode.UNAUTHORIZED, "未登录或登录已过期"));
        }
        Long userId = (Long) auth.getPrincipal();
        Optional<User> userOpt = userService.getById(userId);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Result.fail(ResultCode.UNAUTHORIZED, "用户不存在"));
        }
        User user = userOpt.get();
        Map<String, Object> data = Map.of(
                "id", user.getId(),
                "username", user.getUsername(),
                "isAdmin", user.getIsAdmin()
        );
        return ResponseEntity.ok(Result.success(data));
    }
}
