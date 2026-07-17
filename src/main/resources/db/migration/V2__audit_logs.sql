-- Flyway V2: 管理员关键操作审计日志表
CREATE TABLE IF NOT EXISTS `audit_logs` (
    `id`          BIGINT PRIMARY KEY AUTO_INCREMENT,
    `actor_id`    BIGINT       DEFAULT NULL,
    `actor_name`  VARCHAR(50)  DEFAULT NULL,
    `action`      VARCHAR(50)  NOT NULL,
    `target`      VARCHAR(255) DEFAULT NULL,
    `method`      VARCHAR(10)  DEFAULT NULL,
    `path`        VARCHAR(255) DEFAULT NULL,
    `ip`          VARCHAR(64)  DEFAULT NULL,
    `status`      VARCHAR(20)  DEFAULT NULL,
    `detail`      TEXT         DEFAULT NULL,
    `created_at`  DATETIME     DEFAULT CURRENT_TIMESTAMP,
    INDEX `idx_audit_actor`   (`actor_id`),
    INDEX `idx_audit_created` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
