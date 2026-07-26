-- Persist generated papers so unanswered questions remain in the score denominator.
CREATE TABLE IF NOT EXISTS `exam_papers` (
    `id`           BIGINT PRIMARY KEY AUTO_INCREMENT,
    `user_id`      BIGINT      NOT NULL,
    `status`       VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    `created_at`   DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `submitted_at` DATETIME    DEFAULT NULL,
    INDEX `idx_exam_papers_user_status` (`user_id`, `status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `exam_paper_questions` (
    `id`          BIGINT PRIMARY KEY AUTO_INCREMENT,
    `paper_id`    BIGINT NOT NULL,
    `question_id` BIGINT NOT NULL,
    `position`    INT    NOT NULL,
    UNIQUE KEY `uk_exam_paper_question` (`paper_id`, `question_id`),
    UNIQUE KEY `uk_exam_paper_position` (`paper_id`, `position`),
    INDEX `idx_exam_paper_questions_paper` (`paper_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
