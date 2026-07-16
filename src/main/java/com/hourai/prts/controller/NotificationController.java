package com.hourai.prts.controller;

import com.hourai.prts.common.Result;
import com.hourai.prts.common.ResultCode;
import com.hourai.prts.entity.Announcement;
import com.hourai.prts.entity.NotificationState;
import com.hourai.prts.repository.AnnouncementRepository;
import com.hourai.prts.repository.NotificationStateRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1")
public class NotificationController {

    private final AnnouncementRepository announcementRepository;
    private final NotificationStateRepository notificationStateRepository;

    public NotificationController(AnnouncementRepository announcementRepository,
                                   NotificationStateRepository notificationStateRepository) {
        this.announcementRepository = announcementRepository;
        this.notificationStateRepository = notificationStateRepository;
    }

    // ===== 公告 =====
    @GetMapping("/announcements")
    public ResponseEntity<Result<Map<String, Object>>> listAnnouncements() {
        List<Announcement> list = announcementRepository.findAllByOrderByIdDesc();
        List<Map<String, Object>> result = list.stream().map(a -> {
            Map<String, Object> m = new HashMap<>();
            m.put("id", a.getId());
            m.put("type", a.getType());
            m.put("title", a.getTitle());
            m.put("content", a.getContent());
            m.put("important", a.getImportant());
            m.put("createdAt", a.getCreatedAt());
            m.put("createdBy", a.getCreatedBy());
            m.put("expiresAt", a.getExpiresAt());
            return m;
        }).collect(Collectors.toList());
        return ResponseEntity.ok(Result.success(Map.of("announcements", result)));
    }

    @PostMapping("/admin/announcements")
    public ResponseEntity<Result<Map<String, Object>>> createAnnouncement(@RequestBody Map<String, String> body, Authentication auth) {
        if (auth == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Result.fail(ResultCode.UNAUTHORIZED, "未登录或登录已过期"));
        }
        String title = body.get("title");
        String content = body.get("content");
        if (title == null || content == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Result.fail(ResultCode.BAD_REQUEST, "标题和内容不能为空"));
        }
        Announcement a = new Announcement();
        a.setType(body.getOrDefault("type", "system"));
        a.setTitle(title);
        a.setContent(content);
        a.setImportant("true".equals(body.get("important")) || "1".equals(body.get("important")));
        a.setCreatedAt(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        a.setCreatedBy(((Long) auth.getPrincipal()).toString());
        a.setExpiresAt(body.get("expiresAt"));
        a = announcementRepository.save(a);
        return ResponseEntity.ok(Result.success(Map.of("id", a.getId())));
    }

    // ===== 通知 =====
    @GetMapping("/notifications")
    public ResponseEntity<Result<Map<String, Object>>> listNotifications(
            Authentication auth,
            @RequestParam(defaultValue = "false") boolean unreadOnly,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {

        // 未登录时返回公开公告列表
        if (auth == null) {
            return listAnnouncements();
        }
        Long userId = (Long) auth.getPrincipal();

        List<Announcement> announcements = announcementRepository.findAllByOrderByIdDesc();
        List<NotificationState> states = notificationStateRepository.findByUserId(userId);
        Map<Long, NotificationState> stateMap = states.stream()
                .collect(Collectors.toMap(NotificationState::getNotificationId, s -> s));

        List<Map<String, Object>> notifications = announcements.stream()
                .filter(a -> {
                    if (!unreadOnly) return true;
                    NotificationState ns = stateMap.get(a.getId());
                    return ns == null || !ns.getIsRead();
                })
                .skip((long) (page - 1) * size)
                .limit(size)
                .map(a -> {
                    Map<String, Object> m = new HashMap<>();
                    m.put("id", a.getId());
                    m.put("type", a.getType());
                    m.put("title", a.getTitle());
                    m.put("content", a.getContent());
                    NotificationState ns = stateMap.get(a.getId());
                    m.put("isRead", ns != null && ns.getIsRead());
                    m.put("isImportant", a.getImportant());
                    m.put("createdAt", a.getCreatedAt());
                    return m;
                })
                .collect(Collectors.toList());

        long unreadCount = announcements.stream()
                .filter(a -> {
                    NotificationState ns = stateMap.get(a.getId());
                    return ns == null || !ns.getIsRead();
                }).count();

        Map<String, Object> data = Map.of(
                "notifications", notifications,
                "unreadCount", unreadCount,
                "hasMore", (page * size) < announcements.size()
        );
        return ResponseEntity.ok(Result.success(data));
    }

    @GetMapping("/notifications/unread-count")
    public ResponseEntity<Result<Map<String, Object>>> unreadCount(Authentication auth) {
        if (auth == null) {
            return ResponseEntity.ok(Result.success(Map.of("unreadCount", 0)));
        }
        Long userId = (Long) auth.getPrincipal();
        long count = notificationStateRepository.countByUserIdAndIsReadFalse(userId);
        return ResponseEntity.ok(Result.success(Map.of("unreadCount", count)));
    }

    @PutMapping("/notifications/{id}/read")
    @Transactional
    public ResponseEntity<Result<Void>> markRead(@PathVariable Long id, Authentication auth) {
        if (auth == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Result.fail(ResultCode.UNAUTHORIZED, "未登录或登录已过期"));
        }
        Long userId = (Long) auth.getPrincipal();
        NotificationState ns = notificationStateRepository.findByUserIdAndNotificationId(userId, id)
                .orElseGet(() -> {
                    NotificationState n = new NotificationState();
                    n.setUserId(userId);
                    n.setNotificationId(id);
                    return n;
                });
        ns.setIsRead(true);
        ns.setReadAt(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        notificationStateRepository.save(ns);
        return ResponseEntity.ok(Result.success("已标记为已读", null));
    }

    @PutMapping("/notifications/read-all")
    @Transactional
    public ResponseEntity<Result<Void>> markAllRead(Authentication auth) {
        if (auth == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Result.fail(ResultCode.UNAUTHORIZED, "未登录或登录已过期"));
        }
        Long userId = (Long) auth.getPrincipal();
        for (Announcement a : announcementRepository.findAll()) {
            NotificationState ns = notificationStateRepository.findByUserIdAndNotificationId(userId, a.getId())
                    .orElseGet(() -> {
                        NotificationState n = new NotificationState();
                        n.setUserId(userId);
                        n.setNotificationId(a.getId());
                        return n;
                    });
            ns.setIsRead(true);
            ns.setReadAt(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            notificationStateRepository.save(ns);
        }
        return ResponseEntity.ok(Result.success("已全部标记为已读", null));
    }

    @DeleteMapping("/notifications/{id}")
    @Transactional
    public ResponseEntity<Result<Void>> hideNotification(@PathVariable Long id, Authentication auth) {
        if (auth == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Result.fail(ResultCode.UNAUTHORIZED, "未登录或登录已过期"));
        }
        Long userId = (Long) auth.getPrincipal();
        NotificationState ns = notificationStateRepository.findByUserIdAndNotificationId(userId, id)
                .orElseGet(() -> {
                    NotificationState n = new NotificationState();
                    n.setUserId(userId);
                    n.setNotificationId(id);
                    return n;
                });
        ns.setIsHidden(true);
        notificationStateRepository.save(ns);
        return ResponseEntity.ok(Result.success("已隐藏", null));
    }

    @DeleteMapping("/notifications")
    @Transactional
    public ResponseEntity<Result<Void>> hideAllNotifications(Authentication auth) {
        if (auth == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Result.fail(ResultCode.UNAUTHORIZED, "未登录或登录已过期"));
        }
        Long userId = (Long) auth.getPrincipal();
        for (Announcement a : announcementRepository.findAll()) {
            NotificationState ns = notificationStateRepository.findByUserIdAndNotificationId(userId, a.getId())
                    .orElseGet(() -> {
                        NotificationState n = new NotificationState();
                        n.setUserId(userId);
                        n.setNotificationId(a.getId());
                        return n;
                    });
            ns.setIsHidden(true);
            notificationStateRepository.save(ns);
        }
        return ResponseEntity.ok(Result.success("已隐藏全部", null));
    }
}
