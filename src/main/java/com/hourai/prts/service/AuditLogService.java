package com.hourai.prts.service;

import com.hourai.prts.entity.AuditLog;
import com.hourai.prts.entity.User;
import com.hourai.prts.repository.AuditLogRepository;
import com.hourai.prts.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 审计日志服务：记录并查询管理员关键操作。
 *
 * <p>写入使用 {@link Propagation#REQUIRES_NEW}，保证即使主业务事务回滚，
 * 审计记录仍可落库（失败操作的审计同样重要）。
 */
@Service
public class AuditLogService {

    private static final Logger log = LoggerFactory.getLogger(AuditLogService.class);
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final AuditLogRepository auditLogRepository;
    private final UserRepository userRepository;

    public AuditLogService(AuditLogRepository auditLogRepository, UserRepository userRepository) {
        this.auditLogRepository = auditLogRepository;
        this.userRepository = userRepository;
    }

    /**
     * 记录一条审计日志。
     *
     * @param actorId 操作者 ID（可为 null 表示未认证）
     * @param action  动作语义
     * @param target  操作目标描述
     * @param method  HTTP 方法
     * @param path    请求路径
     * @param ip      客户端 IP
     * @param status  SUCCESS / FAIL
     * @param detail  备注/失败原因
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void record(Long actorId, String action, String target, String method, String path,
                       String ip, String status, String detail) {
        try {
            AuditLog entry = new AuditLog();
            entry.setActorId(actorId);
            entry.setActorName(resolveName(actorId));
            entry.setAction(action);
            entry.setTarget(truncate(target, 255));
            entry.setMethod(truncate(method, 10));
            entry.setPath(truncate(path, 255));
            entry.setIp(truncate(ip, 64));
            entry.setStatus(status);
            entry.setDetail(detail);
            entry.setCreatedAt(LocalDateTime.now().format(FMT));
            auditLogRepository.save(entry);
        } catch (Exception e) {
            // 审计失败不应影响主流程
            log.warn("写入审计日志失败 action={} target={}: {}", action, target, e.getMessage());
        }
    }

    public Page<AuditLog> list(int page, int size) {
        int p = Math.max(page, 1);
        int s = Math.min(Math.max(size, 1), 100);
        Pageable pageable = PageRequest.of(p - 1, s);
        return auditLogRepository.findAllByOrderByIdDesc(pageable);
    }

    private String resolveName(Long actorId) {
        if (actorId == null) return "anonymous";
        return userRepository.findById(actorId)
                .map(User::getUsername)
                .orElse("user#" + actorId);
    }

    private String truncate(String value, int max) {
        if (value == null) return null;
        return value.length() > max ? value.substring(0, max) : value;
    }
}
