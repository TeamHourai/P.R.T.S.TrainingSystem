package com.hourai.prts.entity;

import jakarta.persistence.*;

/**
 * 管理员关键操作审计日志。
 *
 * <p>记录管理员（actor）执行的高权限写操作（如改权限、发公告、批量删除等），
 * 供超级管理员审计追踪。只追加，不修改。
 */
@Entity
@Table(name = "audit_logs")
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 操作者用户 ID */
    @Column(name = "actor_id")
    private Long actorId;

    /** 操作者用户名（冗余，便于审计阅读） */
    @Column(name = "actor_name", length = 50)
    private String actorName;

    /** 动作语义，如 SET_PERMISSION / CREATE_ANNOUNCEMENT / BATCH_DELETE_QUESTIONS */
    @Column(nullable = false, length = 50)
    private String action;

    /** 操作目标描述，如目标用户 ID、创建的公告 ID */
    @Column(length = 255)
    private String target;

    /** HTTP 方法 */
    @Column(length = 10)
    private String method;

    /** 请求路径 */
    @Column(length = 255)
    private String path;

    /** 客户端 IP */
    @Column(length = 64)
    private String ip;

    /** 结果状态：SUCCESS / FAIL */
    @Column(length = 20)
    private String status;

    /** 备注/失败原因 */
    @Column(columnDefinition = "TEXT")
    private String detail;

    @Column(name = "created_at")
    private String createdAt;

    public AuditLog() {}

    // ---- getters / setters ----
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getActorId() { return actorId; }
    public void setActorId(Long actorId) { this.actorId = actorId; }

    public String getActorName() { return actorName; }
    public void setActorName(String actorName) { this.actorName = actorName; }

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }

    public String getTarget() { return target; }
    public void setTarget(String target) { this.target = target; }

    public String getMethod() { return method; }
    public void setMethod(String method) { this.method = method; }

    public String getPath() { return path; }
    public void setPath(String path) { this.path = path; }

    public String getIp() { return ip; }
    public void setIp(String ip) { this.ip = ip; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getDetail() { return detail; }
    public void setDetail(String detail) { this.detail = detail; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}
