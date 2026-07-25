# PRTS Training System — 技术规范与建表手册

> **版本**: 2.0.0 | **更新**: 2026-07-09  
> **框架**: Spring Boot 3.2 + MySQL 8.0 | **Java**: 21

---

## 一、技术架构

```
┌─────────────────────────────────────────────┐
│                  前端 (SPA)                  │
│         Vue.js 2.x + Axios (CDN)            │
└─────────────────┬───────────────────────────┘
                  │ HTTP REST (JSON)
┌─────────────────▼───────────────────────────┐
│              Spring Boot 3.2                 │
│  ┌──────────────────────────────────────┐   │
│  │        Security Layer                │   │
│  │  RateLimit → XSS → JwtAuthFilter     │   │
│  │  BCrypt + HMAC-signed JWT            │   │
│  └──────────────────────────────────────┘   │
│  ┌──────────────────────────────────────┐   │
│  │        Controller Layer (7)          │   │
│  └──────────────────────────────────────┘   │
│  ┌──────────────────────────────────────┐   │
│  │        Service Layer (4)             │   │
│  └──────────────────────────────────────┘   │
│  ┌──────────────────────────────────────┐   │
│  │        Repository Layer (11)         │   │
│  │        Spring Data JPA               │   │
│  └──────────────────────────────────────┘   │
└─────────────────┬───────────────────────────┘
                  │ JDBC (HikariCP 连接池)
┌─────────────────▼───────────────────────────┐
│              MySQL 8.0                       │
│  11 tables | utf8mb4 | InnoDB               │
└─────────────────────────────────────────────┘
```

---

## 二、完整建表语句

> 以下 SQL 由 Flyway 在首次启动时自动执行 (`V1__init_schema.sql`)。  
> 也可手动执行以初始化数据库。

```sql
CREATE DATABASE IF NOT EXISTS `prts_db`
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_unicode_ci;
USE `prts_db`;

-- 1. 用户表
CREATE TABLE IF NOT EXISTS `users` (
    `id`            BIGINT PRIMARY KEY AUTO_INCREMENT,
    `username`      VARCHAR(50)  NOT NULL UNIQUE COMMENT '用户名',
    `password`      VARCHAR(255) NOT NULL COMMENT 'BCrypt加密密码',
    `nickname`      VARCHAR(50)  DEFAULT NULL COMMENT '昵称',
    `avatar`        VARCHAR(255) DEFAULT NULL COMMENT '头像URL',
    `email`         VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
    `is_admin`      TINYINT(1)   DEFAULT 0 COMMENT '是否管理员',
    `status`        TINYINT(1)   DEFAULT 1 COMMENT '账号状态: 0-禁用 1-启用',
    `register_time` DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '注册时间',
    `created_at`    DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`    DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX `idx_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- 2. 正式题库
CREATE TABLE IF NOT EXISTS `questions` (
    `id`          BIGINT PRIMARY KEY AUTO_INCREMENT,
    `type`        INT          NOT NULL DEFAULT 1 COMMENT '题型: 1-5',
    `difficulty`  INT          NOT NULL DEFAULT 1 COMMENT '难度: 1-5',
    `category`    VARCHAR(50)  DEFAULT NULL COMMENT '分类',
    `resource`    VARCHAR(255) DEFAULT NULL COMMENT '来源',
    `question`    TEXT         NOT NULL COMMENT '题目内容',
    `options`     TEXT         NOT NULL COMMENT '选项(竖线分隔: A|B|C|D)',
    `answer`      VARCHAR(255) NOT NULL COMMENT '正确答案(数字)',
    `analysis`    TEXT         DEFAULT NULL COMMENT '解析',
    `has_picture` TINYINT(1)   DEFAULT 0 COMMENT '是否有配图',
    `picture_url` VARCHAR(255) DEFAULT NULL COMMENT '配图URL',
    `view_count`  INT          DEFAULT 0 COMMENT '查看次数',
    `error_count` INT          DEFAULT 0 COMMENT '错误次数',
    `keywords`    VARCHAR(500) DEFAULT NULL COMMENT '关键词(竖线分隔)',
    `created_at`  DATETIME     DEFAULT CURRENT_TIMESTAMP,
    `updated_at`  DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX `idx_type`       (`type`),
    INDEX `idx_difficulty` (`difficulty`),
    INDEX `idx_category`   (`category`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='正式考试题库';

-- 3. 入职培训题库
CREATE TABLE IF NOT EXISTS `questions_onboarding` (
    `id`        INT PRIMARY KEY AUTO_INCREMENT,
    `group_id`  INT          DEFAULT NULL COMMENT '分组ID',
    `type_id`   INT          DEFAULT NULL COMMENT '题型ID',
    `image_url` VARCHAR(500) DEFAULT NULL COMMENT '配图URL',
    `question`  TEXT         NOT NULL COMMENT '题目内容',
    `is_multi`  TINYINT(1)   DEFAULT 0 COMMENT '是否多选',
    `options`   TEXT         NOT NULL COMMENT '选项',
    `answer`    VARCHAR(255) NOT NULL COMMENT '正确答案',
    `analysis`  TEXT         DEFAULT NULL COMMENT '解析',
    INDEX `idx_group_id` (`group_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='入职培训题库';

-- 4. 考试记录
CREATE TABLE IF NOT EXISTS `exam_records` (
    `id`              BIGINT PRIMARY KEY AUTO_INCREMENT,
    `user_id`         BIGINT        NOT NULL COMMENT '用户ID',
    `exam_name`       VARCHAR(100)  DEFAULT NULL COMMENT '考试名称',
    `total_questions` INT           NOT NULL DEFAULT 0 COMMENT '总题数',
    `correct_count`   INT           NOT NULL DEFAULT 0 COMMENT '正确题数',
    `score`           DECIMAL(5,2)  NOT NULL DEFAULT 0.00 COMMENT '得分',
    `duration`        INT           DEFAULT NULL COMMENT '用时(秒)',
    `created_at`      DATETIME      DEFAULT CURRENT_TIMESTAMP COMMENT '提交时间',
    INDEX `idx_user_id`    (`user_id`),
    INDEX `idx_created_at` (`created_at`),
    CONSTRAINT `fk_exam_records_user` FOREIGN KEY (`user_id`) REFERENCES `users`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='考试记录表';

-- 5. 考试详情
CREATE TABLE IF NOT EXISTS `exam_detail` (
    `id`              BIGINT PRIMARY KEY AUTO_INCREMENT,
    `exam_id`         BIGINT       NOT NULL COMMENT '考试记录ID',
    `question_id`     BIGINT       NOT NULL COMMENT '题目ID',
    `selected_answer` VARCHAR(50)  DEFAULT NULL COMMENT '选择的答案',
    `is_correct`      TINYINT(1)   DEFAULT NULL COMMENT '是否正确',
    INDEX `idx_exam_id`     (`exam_id`),
    INDEX `idx_question_id` (`question_id`),
    CONSTRAINT `fk_exam_detail_record`   FOREIGN KEY (`exam_id`)     REFERENCES `exam_records`(`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_exam_detail_question` FOREIGN KEY (`question_id`) REFERENCES `questions`(`id`)   ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='考试详情表';

-- 6. 答题记录
CREATE TABLE IF NOT EXISTS `user_answers` (
    `id`              BIGINT PRIMARY KEY AUTO_INCREMENT,
    `user_id`         BIGINT       NOT NULL COMMENT '用户ID',
    `question_id`     BIGINT       NOT NULL COMMENT '题目ID',
    `selected_answer` VARCHAR(255) DEFAULT NULL COMMENT '选择的答案',
    `is_correct`      TINYINT(1)   NOT NULL DEFAULT 0 COMMENT '是否正确',
    `answer_time`     INT          DEFAULT NULL COMMENT '用时(秒)',
    `created_at`      DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '答题时间',
    INDEX `idx_user_id`       (`user_id`),
    INDEX `idx_question_id`   (`question_id`),
    INDEX `idx_user_question` (`user_id`, `question_id`),
    CONSTRAINT `fk_user_answers_user`     FOREIGN KEY (`user_id`)     REFERENCES `users`(`id`)     ON DELETE CASCADE,
    CONSTRAINT `fk_user_answers_question` FOREIGN KEY (`question_id`) REFERENCES `questions`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户答题记录表';

-- 7. 公告
CREATE TABLE IF NOT EXISTS `announcements` (
    `id`         BIGINT PRIMARY KEY AUTO_INCREMENT,
    `type`       VARCHAR(50)  NOT NULL DEFAULT 'system' COMMENT '类型: system/exam/answer/warning/update/reward',
    `title`      VARCHAR(200) NOT NULL COMMENT '标题',
    `content`    TEXT         NOT NULL COMMENT '内容',
    `important`  TINYINT(1)   DEFAULT 0 COMMENT '是否重要',
    `created_at` VARCHAR(50)  DEFAULT NULL COMMENT '发布时间',
    `created_by` VARCHAR(100) DEFAULT NULL COMMENT '发布人ID',
    `expires_at` VARCHAR(50)  DEFAULT NULL COMMENT '过期时间',
    INDEX `idx_type` (`type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统公告表';

-- 8. 错题可见性
CREATE TABLE IF NOT EXISTS `wrong_visibility` (
    `id`          BIGINT PRIMARY KEY AUTO_INCREMENT,
    `user_id`     BIGINT      NOT NULL COMMENT '用户ID',
    `question_id` BIGINT      NOT NULL COMMENT '题目ID',
    `hidden`      TINYINT(1)  DEFAULT 0 COMMENT '是否隐藏',
    `updated_at`  VARCHAR(50) DEFAULT NULL COMMENT '更新时间',
    UNIQUE KEY `uk_user_question` (`user_id`, `question_id`),
    INDEX `idx_user_id`     (`user_id`),
    INDEX `idx_question_id` (`question_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='错题可见性表';

-- 9. 通知状态
CREATE TABLE IF NOT EXISTS `notifications_state` (
    `id`              BIGINT PRIMARY KEY AUTO_INCREMENT,
    `user_id`         BIGINT      NOT NULL COMMENT '用户ID',
    `notification_id` BIGINT      NOT NULL COMMENT '公告ID',
    `is_read`         TINYINT(1)  DEFAULT 0 COMMENT '是否已读',
    `is_hidden`       TINYINT(1)  DEFAULT 0 COMMENT '是否隐藏',
    `read_at`         VARCHAR(50) DEFAULT NULL COMMENT '阅读时间',
    UNIQUE KEY `uk_user_notification` (`user_id`, `notification_id`),
    INDEX `idx_user_id`         (`user_id`),
    INDEX `idx_notification_id` (`notification_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='通知状态表';

-- 10. 答题设置
CREATE TABLE IF NOT EXISTS `answer_settings` (
    `user_id`           BIGINT PRIMARY KEY COMMENT '用户ID',
    `auto_submit`       TINYINT(1) DEFAULT 0 COMMENT '自动提交',
    `auto_next_correct` TINYINT(1) DEFAULT 1 COMMENT '答对自动下一题',
    `updated_at`        DATETIME   DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT `fk_answer_settings_user` FOREIGN KEY (`user_id`) REFERENCES `users`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户答题设置表';

-- 11. 培训记录
CREATE TABLE IF NOT EXISTS `training_records` (
    `user_id`     BIGINT      NOT NULL COMMENT '用户ID',
    `question_id` BIGINT      NOT NULL COMMENT '题目ID',
    `attempts`    INT         DEFAULT 0 COMMENT '尝试次数',
    `correct`     TINYINT(1)  DEFAULT 0 COMMENT '是否答对',
    `last_at`     BIGINT      DEFAULT 0 COMMENT '最后答题时间(ms)',
    PRIMARY KEY (`user_id`, `question_id`),
    INDEX `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='培训答题记录表';
```

---

## 三、ER 关系图

```
users ────< exam_records ────< exam_detail >──── questions
  │              │
  │              └── user_answers ────> questions
  │
  ├──< wrong_visibility ────> questions
  ├──< notifications_state
  ├──< answer_settings (1:1)
  ├──< training_records ────> questions_onboarding
  └──< announcements (via notifications_state)
```

---

## 四、CSV 数据导入规范

应用首次启动时，`CsvImportService` 从 `data/` 目录读取以下 CSV 文件并导入数据库。

### CSV 格式说明

| 文件 | 格式 (无表头，逗号分隔) |
|------|------------------------|
| `users.csv` | `id,username,password,isAdmin,registerTime` |
| `questions.csv` | `id,type,difficulty,resource,question,hasPicture,options(piped),answer,analysis,keywords(piped)` |
| `questions_onboarding.csv` | `id,group_id,type_id,imageUrl,question,isMulti,options(piped),answer,analysis` |
| `exam_records.csv` | `id,userId,score,createdAt` |
| `user_answers.csv` | `id,userId,questionId,questionType,isCorrect,selected,answeredAt` |
| `announcements.csv` | `id,type,title,content,important,createdAt,createdBy,expiresAt` |
| `wrong_visibility.csv` | `id,userId,questionId,hidden,updatedAt` |
| `notifications_state.csv` | `notificationId,userId,isRead,isHidden` |
| `answer_settings.csv` | `userId,autoSubmit,autoNextCorrect,updatedAt` |
| `training_records.csv` | `userId,questionId,attempts,correct,lastAt` |

### 导入特点

- **幂等** — users 表为空时才导入
- **密码加密** — 明文密码自动转换为 BCrypt 哈希
- **容错** — 单行解析失败不影响整体导入

---

## 五、API 响应格式规范

### 成功
```json
{"success":true, "message":"操作成功", "data":{...}}
```

### 失败
```json
{"success":false, "message":"错误描述"}
```

### QuestionDTO (统一题目格式)
```json
{
  "id": 1, "type": 1, "difficulty": 4,
  "resource": "来源", "question": "题目",
  "picture": false, "pictureUrl": null,
  "options": ["A", "B", "C", "D"],
  "answer": 2, "analysis": "解析",
  "keywords": ["kw1", "kw2"],
  "viewCount": 0, "errorCount": 0
}
```

### 提醒格式 (答案提交)
```
questionId:selectedAnswer,questionId:selectedAnswer,...
例: 1:2,3:1,5:3
```

---

## 六、部署清单

| 项目 | 值 |
|------|-----|
| 端口 | 8080 |
| 绑定 | 127.0.0.1 |
| 数据库 | MySQL 8.0, 库名 `prts_db` |
| JDK | 21 |
| Maven | 3.x |
| 前端 | 纯静态文件，任何 HTTP 服务器 |

### 启动步骤

```bash
# 1. 创建数据库
mysql -u root -p -e "CREATE DATABASE IF NOT EXISTS prts_db DEFAULT CHARACTER SET utf8mb4;"

# 2. 更新 application.yml 中的数据库密码

# 3. 构建并运行
mvn clean package -DskipTests
java -jar target/PRTS.TRAININGSYSTEM-2.0.0.jar

# 4. 前端可通过任意静态服务器访问
# 开发: cd P.R.T.S.TrainingSystemFrontend && npx http-server -p 3000
```
