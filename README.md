# PRTS Training System — 博士业务能力考核系统

> **版本**: 2.0.0 | **Java**: 21 | **框架**: Spring Boot 3.2 | **数据库**: MySQL 8.0

明日方舟主题的博士业务能力在线考核与培训平台，提供正式考试、入职培训、错题管理、通知公告等完整功能。

---

## 技术栈

| 层级 | 技术 | 版本 |
|------|------|------|
| 框架 | Spring Boot | 3.2.0 |
| 安全 | Spring Security + JWT (jjwt) + BCrypt | — |
| 持久层 | Spring Data JPA + Hibernate 6.3 | — |
| 数据库 | MySQL | 8.0+ |
| 迁移 | Flyway | 9.x |
| 连接池 | HikariCP (Spring Boot 内置) | — |
| JSON | Jackson (Spring Boot 内置) | — |
| 前端 | Vue.js 2.x + Axios | CDN |
| 构建 | Maven | 3.x |

---

## 项目结构

```
PRTS.TRAININGSYSTEM/
├── pom.xml
├── data/                              # CSV 数据文件 (启动时自动导入)
│   ├── users.csv
│   ├── questions.csv
│   ├── questions_onboarding.csv
│   ├── exam_records.csv
│   ├── user_answers.csv
│   ├── announcements.csv
│   ├── wrong_visibility.csv
│   ├── notifications_state.csv
│   ├── answer_settings.csv
│   └── training_records.csv
├── P.R.T.S.TrainingSystemFrontend/    # 前端 (纯静态)
│   ├── index.html                     # 主 SPA
│   ├── exam.html                      # 考试页面
│   ├── editor.html                    # 题库编辑
│   ├── training-editor.html           # 培训题库编辑
│   ├── admin_permissions.html         # 权限管理
│   ├── announcement-editor.html       # 公告编辑
│   ├── js/
│   │   ├── config.js                  # 全局配置
│   │   ├── apiapp.js                  # API 封装
│   │   ├── api/                       # API 模块
│   │   ├── app/                       # Vue 应用逻辑
│   │   └── vendor/                    # Axios CDN
│   └── css/                           # 样式
└── src/main/
    ├── java/com/hourai/prts/
    │   ├── PrtsApplication.java       # Spring Boot 入口
    │   ├── config/
    │   │   ├── SecurityConfig.java    # Spring Security 配置
    │   │   ├── CorsConfig.java        # CORS 配置
    │   │   └── PasswordConfig.java    # BCrypt 配置
    │   ├── security/
    │   │   ├── JwtTokenProvider.java  # JWT 签发/验证
    │   │   ├── JwtAuthenticationFilter.java
    │   │   └── LegacyTokenFilter.java # 旧 Token 兼容
    │   ├── entity/                    # JPA 实体 (11 个)
    │   ├── repository/                # Spring Data JPA 仓库 (11 个)
    │   ├── service/                   # 业务服务 (4 个)
    │   ├── controller/                # REST 控制器 (7 个)
    │   └── dto/                       # 数据传输对象 (4 个)
    └── resources/
        ├── application.yml            # 应用配置
        └── db/migration/
            └── V1__init_schema.sql    # Flyway 建表迁移
```

---

## 快速开始

### 1. 创建数据库

```sql
CREATE DATABASE IF NOT EXISTS `prts_db`
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_unicode_ci;
```

### 2. 配置数据库连接

编辑 `src/main/resources/application.yml`：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/prts_db?useSSL=false&serverTimezone=Asia/Shanghai&characterEncoding=utf8mb4&allowPublicKeyRetrieval=true
    username: root
    password: your_password
```

### 3. 启动应用

```bash
mvn clean package -DskipTests
java -jar target/PRTS.TRAININGSYSTEM-2.0.0.jar
```

首次启动时，Flyway 自动创建所有表，然后 CsvImportService 从 `data/` 目录导入所有 CSV 数据。密码自动以 BCrypt 加密存储。

### 4. 访问

- 后端 API: `http://localhost:8080`
- H2 控制台: 已禁用 (MySQL 模式)
- 默认管理员: `admin` / `admin`

---

## 数据库表

| 表名 | 说明 | 记录数 |
|------|------|--------|
| `users` | 用户 (BCrypt 密码) | 7 |
| `questions` | 正式题库 | 58 |
| `questions_onboarding` | 入职培训题库 | 15 |
| `exam_records` | 考试记录 | 38 |
| `exam_detail` | 考试详情 | — |
| `user_answers` | 答题记录 | 43 |
| `announcements` | 系统公告 | 1 |
| `wrong_visibility` | 错题隐藏状态 | 20 |
| `notifications_state` | 通知已读状态 | 2 |
| `answer_settings` | 答题设置 | 1 |
| `training_records` | 培训记录 | 3 |

---

## API 接口文档

**基础 URL**: `http://localhost:8080`  
**API 前缀**: `/api/v1`  
**认证方式**: `Authorization: Bearer <jwt_token>`

### 认证模块

#### `POST /api/v1/auth/register` — 注册
```
Content-Type: application/json
Body: {"username":"...", "password":"...", "email":"..."(可选)}
→ 200: {"success":true, "id":1, "userId":1, "username":"..."}
→ 400: {"success":false, "message":"username exists"}
```

#### `POST /api/v1/auth/login` — 登录
```
Body: {"username":"...", "password":"..."}
→ 200: {"success":true, "token":"eyJ...", "user":{"id":1,"username":"admin","isAdmin":true}}
→ 401: {"success":false, "message":"invalid credentials"}
```

#### `POST /api/v1/auth/logout` — 登出
```
→ 200: {"success":true, "message":"logged out"}
```

#### `GET /api/v1/auth/profile` — 当前用户
```
Header: Authorization: Bearer <token>
→ 200: {"id":1, "username":"admin", "isAdmin":true}
→ 401: {"success":false, "message":"missing token"}
```

---

### 题库模块

#### `GET /api/v1/questions` — 题目列表 (公开)
```
Query: ?page=1&size=50&type=1&difficulty=3&keyword=关键词
→ 200: [QuestionDTO, ...]
```

#### `GET /api/v1/questions/{id}` — 题目详情 (公开)
```
→ 200: QuestionDTO | 404
```

#### `POST /api/v1/questions` — 创建题目 (需登录)
```json
{
  "type": 1, "difficulty": 3,
  "question": "题目内容",
  "options": ["A", "B", "C", "D"],
  "answer": 2, "analysis": "解析",
  "keywords": ["关键词1", "关键词2"]
}
→ 200: {"id": 59}
```

#### `PUT /api/v1/questions/{id}` — 更新题目 (需登录)
#### `DELETE /api/v1/questions/{id}` — 删除题目 (需登录)

---

### 培训题库模块

#### `GET /api/v1/training/questions` — 培训题目列表 (公开)
#### `GET /api/v1/training/questions/{id}` — 培训题目详情 (公开)
#### `POST /api/v1/training/questions` — 创建培训题目 (需登录)
#### `PUT /api/v1/training/questions/{id}` — 更新培训题目 (需登录)
#### `DELETE /api/v1/training/questions/{id}` — 删除培训题目 (需登录)

---

### 考试模块

#### `GET /api/v1/exam/paper` — 生成试卷 (公开)
```
→ 200: [QuestionDTO × 25题]
```

#### `POST /api/v1/exam/submit` — 提交考试 (form-urlencoded)
```
Body: userId=1&answers=1:2,3:1,5:3&duration=900
→ 200: {"examId":39, "score":85}
```

#### `GET /api/v1/exam/history` — 考试历史
```
Query: ?page=1&size=10
→ 200: [{"examId":..., "userId":..., "score":..., "createdAt":"..."}]
```

---

### 错题模块

#### `GET /api/v1/answers/wrong` — 错题列表 (需登录)
#### `DELETE /api/v1/answers/wrong/{questionId}` — 隐藏错题 (需登录)
#### `GET /api/v1/user/{userId}/wrong` — 按用户查错题 (公开)

---

### 通知与公告

#### `GET /api/v1/announcements` — 公告列表 (公开)
#### `POST /api/v1/admin/announcements` — 发布公告 (管理员)

#### `GET /api/v1/notifications` — 通知列表
```
Query: ?unreadOnly=false&page=1&size=20
→ 200: {"success":true, "notifications":[...], "unreadCount":5, "hasMore":false}
```
#### `GET /api/v1/notifications/unread-count` — 未读计数
#### `PUT /api/v1/notifications/{id}/read` — 标为已读
#### `PUT /api/v1/notifications/read-all` — 全部已读
#### `DELETE /api/v1/notifications/{id}` — 隐藏单条
#### `DELETE /api/v1/notifications` — 隐藏全部

---

### 管理模块

#### `GET /api/v1/admin/users` — 用户列表 (管理员)
```
Query: ?q=keyword (按用户名/ID搜索)
→ 200: [{"id":1,"username":"admin","isAdmin":true,"createdAt":"..."}]
```

#### `POST /api/v1/admin/user/permission` — 设置权限 (管理员)
```
Body (form): actor_id=1&target_id=2&make_admin=true
→ 200: {"success":true,"message":"promoted to admin"}
→ 403: 非管理员 / only super admin can demote
```

#### `POST /admin/questions/batch-delete` — 批量删除 (需登录)
```
Body: {"ids": [1,2,3]} 或 {"ids": "1,2,3"}
→ 200: {"success":true}
```

---

### 用户设置

#### `GET /api/v1/user/answer-settings` — 获取设置 (需登录)
#### `PUT /api/v1/user/answer-settings` — 更新设置 (需登录)
#### `GET /api/v1/user/training-records` — 培训记录 (需登录)
#### `PUT /api/v1/user/training-records` — 更新培训记录 (需登录)
#### `DELETE /api/v1/user/training-records` — 清除培训记录 (需登录)

---

### 工具接口

#### `GET /api/v1/ping` — 健康检查 (公开)
#### `GET /api/v1/keywords` — 关键词列表 (公开)
#### `GET /api/v1/stats/question/{id}` — 题目统计 (公开)
#### `GET /api/v1/stats/user` — 用户统计 (公开)
#### `GET /api/v1/stats/system` — 系统状态 (公开)

---

## QuestionDTO 数据结构

```json
{
  "id": 1,
  "type": 1,
  "difficulty": 4,
  "category": null,
  "resource": "神奇陆夫人《2022年明日方舟高考》",
  "question": "不好！怎么还有个无人机啊？？...",
  "picture": false,
  "pictureUrl": null,
  "options": ["选项A", "选项B", "选项C", "选项D"],
  "answer": 2,
  "analysis": "A、号角无法对空\nB、琴柳...",
  "keywords": ["干员", "号角", "琴柳"],
  "viewCount": 0,
  "errorCount": 0,
  "createdAt": "2026-07-09T00:31:17",
  "updatedAt": null
}
```

---

## 安全机制

| 特性 | 实现 |
|------|------|
| 密码存储 | BCrypt 哈希 (每次生成随机盐) |
| 认证令牌 | JWT (HMAC-SHA512, 24小时过期) |
| 角色权限 | ROLE_USER / ROLE_ADMIN |
| 公开接口 | GET 类读取操作 |
| 需认证 | POST/PUT/DELETE 写入操作 |
| 管理员操作 | /api/v1/admin/** 仅 ADMIN 角色 |
| 旧 Token 兼容 | `Bearer user-{id}` 自动降级处理 |

---

## 数据导入流程

应用启动时 `CsvImportService` (CommandLineRunner) 自动运行：

1. 检查 `users` 表是否为空 → 避免重复导入
2. 从 `data/` 目录读取 10 个 CSV 文件
3. 解析每行数据，映射到对应实体
4. 密码自动 BCrypt 加密存储
5. 日志输出导入统计

---

## 架构说明

```
请求 → CorsFilter → JwtAuthFilter → LegacyTokenFilter
     → SecurityFilterChain (角色校验)
     → Controller → Service → Repository (JPA) → MySQL
```

- **无状态** — 每个请求独立认证，服务端不存 Session
- **分层清晰** — Controller → Service → Repository，职责分明
- **统一容器** — Spring IoC 管理所有依赖
- **连接池** — HikariCP 自动管理数据库连接复用
