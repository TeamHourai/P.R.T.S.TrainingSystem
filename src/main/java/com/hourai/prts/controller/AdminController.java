package com.hourai.prts.controller;

import com.hourai.prts.common.Result;
import com.hourai.prts.common.ResultCode;
import com.hourai.prts.entity.AuditLog;
import com.hourai.prts.entity.User;
import com.hourai.prts.service.AuditLogService;
import com.hourai.prts.service.UserService;
import com.hourai.prts.util.IpUtils;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.domain.Page;
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
    private final AuditLogService auditLogService;

    public AdminController(UserService userService, AuditLogService auditLogService) {
        this.userService = userService;
        this.auditLogService = auditLogService;
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
                                                                     Authentication auth,
                                                                     HttpServletRequest request) {
        // 审计以实际登录者为准（auth.principal），而非前端传入的 actorId
        Long realActorId = auth != null ? (Long) auth.getPrincipal() : actorId;
        String ip = IpUtils.getClientIp(request);
        try {
            boolean makeAdminBool = "true".equals(makeAdmin) || "1".equals(makeAdmin);
            boolean changed = userService.setAdminStatus(actorId, targetId, makeAdminBool);
            String msg = changed
                    ? (makeAdminBool ? "已设为管理员" : "已降为普通用户")
                    : "状态未发生变化";
            auditLogService.record(realActorId, "SET_PERMISSION", "user#" + targetId + "=>" + makeAdminBool,
                    request.getMethod(), request.getRequestURI(), ip, "SUCCESS", msg);
            return ResponseEntity.ok(Result.success(Map.of("message", msg)));
        } catch (RuntimeException e) {
            String msg = e.getMessage();
            auditLogService.record(realActorId, "SET_PERMISSION", "user#" + targetId,
                    request.getMethod(), request.getRequestURI(), ip, "FAIL", msg);
            if ("not admin".equals(msg) || (msg != null && msg.contains("super admin"))) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Result.fail(ResultCode.FORBIDDEN, msg));
            }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Result.fail(ResultCode.INTERNAL_ERROR, msg));
        }
    }

    /**
     * 审计日志查询：仅超级管理员（用户 ID = 1）可访问。
     */
    @GetMapping("/audit-logs")
    public ResponseEntity<Result<Map<String, Object>>> auditLogs(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            Authentication auth,
            HttpServletRequest request) {
        if (auth == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Result.fail(ResultCode.UNAUTHORIZED, "未登录或登录已过期"));
        }
        Long userId = (Long) auth.getPrincipal();
        if (!Long.valueOf(1L).equals(userId)) {
            auditLogService.record(userId, "VIEW_AUDIT_LOGS", "denied",
                    request.getMethod(), request.getRequestURI(),
                    IpUtils.getClientIp(request), "FAIL", "非超级管理员");
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Result.fail(ResultCode.FORBIDDEN, "仅超级管理员可查看审计日志"));
        }
        Page<AuditLog> result = auditLogService.list(page, size);
        List<Map<String, Object>> items = result.getContent().stream().map(a -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", a.getId());
            m.put("actorId", a.getActorId());
            m.put("actorName", a.getActorName());
            m.put("action", a.getAction());
            m.put("target", a.getTarget());
            m.put("method", a.getMethod());
            m.put("path", a.getPath());
            m.put("ip", a.getIp());
            m.put("status", a.getStatus());
            m.put("detail", a.getDetail());
            m.put("createdAt", a.getCreatedAt());
            return m;
        }).collect(Collectors.toList());

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("logs", items);
        data.put("total", result.getTotalElements());
        data.put("page", page);
        data.put("size", size);
        data.put("pages", result.getTotalPages());
        return ResponseEntity.ok(Result.success(data));
    }
}
