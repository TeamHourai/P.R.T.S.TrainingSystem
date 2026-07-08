-- Flyway V1: PRTS Training System schema (no FK constraints for CSV import compatibility)
CREATE TABLE IF NOT EXISTS `users` (
    `id`            BIGINT PRIMARY KEY AUTO_INCREMENT,
    `username`      VARCHAR(50)  NOT NULL UNIQUE,
    `password`      VARCHAR(255) NOT NULL,
    `nickname`      VARCHAR(50)  DEFAULT NULL,
    `avatar`        VARCHAR(255) DEFAULT NULL,
    `email`         VARCHAR(100) DEFAULT NULL,
    `is_admin`      TINYINT(1)   DEFAULT 0,
    `status`        TINYINT(1)   DEFAULT 1,
    `register_time` DATETIME     DEFAULT CURRENT_TIMESTAMP,
    `created_at`    DATETIME     DEFAULT CURRENT_TIMESTAMP,
    `updated_at`    DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX `idx_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `questions` (
    `id`          BIGINT PRIMARY KEY AUTO_INCREMENT,
    `type`        INT          NOT NULL DEFAULT 1,
    `difficulty`  INT          NOT NULL DEFAULT 1,
    `category`    VARCHAR(50)  DEFAULT NULL,
    `resource`    VARCHAR(255) DEFAULT NULL,
    `question`    TEXT         NOT NULL,
    `options`     TEXT         NOT NULL,
    `answer`      VARCHAR(255) NOT NULL,
    `analysis`    TEXT         DEFAULT NULL,
    `has_picture` TINYINT(1)   DEFAULT 0,
    `picture_url` VARCHAR(255) DEFAULT NULL,
    `view_count`  INT          DEFAULT 0,
    `error_count` INT          DEFAULT 0,
    `keywords`    VARCHAR(500) DEFAULT NULL,
    `created_at`  DATETIME     DEFAULT CURRENT_TIMESTAMP,
    `updated_at`  DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX `idx_type`       (`type`),
    INDEX `idx_difficulty` (`difficulty`),
    INDEX `idx_category`   (`category`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `questions_onboarding` (
    `id`        INT PRIMARY KEY AUTO_INCREMENT,
    `group_id`  INT          DEFAULT NULL,
    `type_id`   INT          DEFAULT NULL,
    `image_url` VARCHAR(500) DEFAULT NULL,
    `question`  TEXT         NOT NULL,
    `is_multi`  TINYINT(1)   DEFAULT 0,
    `options`   TEXT         NOT NULL,
    `answer`    VARCHAR(255) NOT NULL,
    `analysis`  TEXT         DEFAULT NULL,
    INDEX `idx_group_id` (`group_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `exam_records` (
    `id`              BIGINT PRIMARY KEY AUTO_INCREMENT,
    `user_id`         BIGINT        NOT NULL,
    `exam_name`       VARCHAR(100)  DEFAULT NULL,
    `total_questions` INT           NOT NULL DEFAULT 0,
    `correct_count`   INT           NOT NULL DEFAULT 0,
    `score`           DECIMAL(5,2)  NOT NULL DEFAULT 0.00,
    `duration`        INT           DEFAULT NULL,
    `created_at`      DATETIME      DEFAULT CURRENT_TIMESTAMP,
    INDEX `idx_user_id`    (`user_id`),
    INDEX `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `exam_detail` (
    `id`              BIGINT PRIMARY KEY AUTO_INCREMENT,
    `exam_id`         BIGINT       NOT NULL,
    `question_id`     BIGINT       NOT NULL,
    `selected_answer` VARCHAR(50)  DEFAULT NULL,
    `is_correct`      TINYINT(1)   DEFAULT NULL,
    INDEX `idx_exam_id`     (`exam_id`),
    INDEX `idx_question_id` (`question_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `user_answers` (
    `id`              BIGINT PRIMARY KEY AUTO_INCREMENT,
    `user_id`         BIGINT       NOT NULL,
    `question_id`     BIGINT       NOT NULL,
    `selected_answer` VARCHAR(255) DEFAULT NULL,
    `is_correct`      TINYINT(1)   NOT NULL DEFAULT 0,
    `answer_time`     INT          DEFAULT NULL,
    `created_at`      DATETIME     DEFAULT CURRENT_TIMESTAMP,
    INDEX `idx_user_id`       (`user_id`),
    INDEX `idx_question_id`   (`question_id`),
    INDEX `idx_user_question` (`user_id`, `question_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `announcements` (
    `id`         BIGINT PRIMARY KEY AUTO_INCREMENT,
    `type`       VARCHAR(50)  NOT NULL DEFAULT 'system',
    `title`      VARCHAR(200) NOT NULL,
    `content`    TEXT         NOT NULL,
    `important`  TINYINT(1)   DEFAULT 0,
    `created_at` VARCHAR(50)  DEFAULT NULL,
    `created_by` VARCHAR(100) DEFAULT NULL,
    `expires_at` VARCHAR(50)  DEFAULT NULL,
    INDEX `idx_type` (`type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `wrong_visibility` (
    `id`          BIGINT PRIMARY KEY AUTO_INCREMENT,
    `user_id`     BIGINT      NOT NULL,
    `question_id` BIGINT      NOT NULL,
    `hidden`      TINYINT(1)  DEFAULT 0,
    `updated_at`  VARCHAR(50) DEFAULT NULL,
    UNIQUE KEY `uk_user_question` (`user_id`, `question_id`),
    INDEX `idx_user_id`     (`user_id`),
    INDEX `idx_question_id` (`question_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `notifications_state` (
    `id`              BIGINT PRIMARY KEY AUTO_INCREMENT,
    `user_id`         BIGINT      NOT NULL,
    `notification_id` BIGINT      NOT NULL,
    `is_read`         TINYINT(1)  DEFAULT 0,
    `is_hidden`       TINYINT(1)  DEFAULT 0,
    `read_at`         VARCHAR(50) DEFAULT NULL,
    UNIQUE KEY `uk_user_notification` (`user_id`, `notification_id`),
    INDEX `idx_user_id`         (`user_id`),
    INDEX `idx_notification_id` (`notification_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `answer_settings` (
    `user_id`           BIGINT PRIMARY KEY,
    `auto_submit`       TINYINT(1) DEFAULT 0,
    `auto_next_correct` TINYINT(1) DEFAULT 1,
    `updated_at`        DATETIME   DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `training_records` (
    `user_id`     BIGINT      NOT NULL,
    `question_id` BIGINT      NOT NULL,
    `attempts`    INT         DEFAULT 0,
    `correct`     TINYINT(1)  DEFAULT 0,
    `last_at`     BIGINT      DEFAULT 0,
    PRIMARY KEY (`user_id`, `question_id`),
    INDEX `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
