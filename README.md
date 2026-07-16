# PRTS Training System — 博士业务能力考核系统

> **版本**: 2.0.0 ｜ **Java**: 21 ｜ **框架**: Spring Boot 3.2 ｜ **数据库**: MySQL 8.0 ｜ **前端**: 原生 JS + Vue 2 (CDN)

明日方舟主题的博士业务能力在线考核与培训平台，提供正式考试、入职培训、错题管理、通知公告等完整功能。

---

## 一、技术栈

| 层级 | 技术 | 版本 |
|------|------|------|
| 后端框架 | Spring Boot | 3.2.0 |
| 安全 | Spring Security + JWT (jjwt) + BCrypt | — |
| 持久层 | Spring Data JPA + Hibernate 6.3 | — |
| 数据库 | MySQL | 8.0+ |
| 迁移 | Flyway | 9.x |
| 连接池 | HikariCP (Spring Boot 内置) | — |
| JSON | Jackson (Spring Boot 内置) | — |
| 前端 | 原生 JS + Vue 2.x + Axios 风格 Fetch | CDN |
| 构建 | Maven | 3.x |

---

## 二、目录结构

```
PRTS.TRAININGSYSTEM/
├── pom.xml
├── data/                              # CSV 数据文件 (首次启动自动导入)
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
├── src/main/
│   ├── java/com/hourai/prts/
│   │   ├── PrtsApplication.java
│   │   ├── common/                    # ★ 统一响应标准
│   │   │   ├── Result.java            #   标准响应封装 {code,message,data,success}
│   │   │   ├── ResultCode.java        #   状态码枚举（200/400/401/403/404/409/500）
│   │   │   ├── BusinessException.java #   业务异常（携带语义化状态码）
│   │   │   └── GlobalExceptionHandler.java # 全局异常 -> 标准响应
│   │   ├── config/                    # 安全/CORS/密码/Web 配置
│   │   ├── security/                  # JWT 签发/校验/旧 Token 兼容
│   │   ├── entity/                    # JPA 实体（11 个）
│   │   ├── repository/                # Spring Data JPA 仓库（11 个）
│   │   ├── service/                   # 业务服务（4 个）
│   │   ├── controller/                # REST 控制器（7 个）
│   │   └── dto/                       # 数据传输对象（QuestionDTO 等）
│   └── resources/
│       ├── application.yml
│       └── db/migration/V1__init_schema.sql
└── P.R.T.S.TrainingSystemFrontend/    # 前端（纯静态多页应用）
    ├── index.html                     # 主 SPA 入口
    ├── exam.html                      # 考试页面
    ├── editor.html                    # 题库编辑（正式题库）
    ├── training-editor.html           # 培训题库编辑
    ├── admin_permissions.html         # 权限管理
    ├── announcement-editor.html       # 公告编辑
    ├── css/                           # 样式
    └── js/
        ├── config.js                  # 全局配置（API 基址/版本/端点/存储键）
        ├── api.js                     # 统一接口客户端（window.api + 兼容别名）
        ├── utils/
        │   └── common.js              # ★ 公共工具（window.PRTS: 请求/令牌/格式化/提示）
        ├── components/
        │   └── modal.js               # 公共组件：模态框 & 轻提示
        ├── app/                       # 主应用 Vue 逻辑（data/computed/watch/methods）
        ├── exam.js / exam-frontend.js # 考试页逻辑
        └── question-editor.js         # 题库编辑器逻辑
```

> 说明：`P.R.T.S.TrainingSystemFrontend/dist` 为历史构建产物，当前前端以 `index.html` + `js/` 静态文件方式直接部署，无需构建步骤（见 `package.json` 中 `build` 脚本）。

---

## 三、前后端接口规范（统一标准）

本次重构将**所有** REST 接口统一为同一套请求/响应契约，解决此前响应格式混乱（部分为裸数组/对象、部分用 `success`、部分用 `error` 键、缺少统一状态码）的问题。

### 3.1 基础约定

- **Base URL**: `http://localhost:8080`
- **API 前缀**: `/api/v1`
- **认证**: `Authorization: Bearer <jwt_token>`（公开接口除外）
- **请求体**: 默认 `application/json`；仅 `POST /exam/submit` 与 `POST /admin/user/permission` 因历史原因使用 `application/x-www-form-urlencoded`
- **时间格式**: `yyyy-MM-dd HH:mm:ss`

### 3.2 统一响应格式

所有接口均返回如下 JSON 信封：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": { },
  "success": true
}
```

| 字段 | 类型 | 说明 |
|------|------|------|
| `code` | int | 业务/HTTP 语义状态码（见下表），**唯一判定成功与否的依据** |
| `message` | string | 人类可读的提示信息 |
| `data` | object/array/primitive | 业务数据负载；失败时通常为 `null` |
| `success` | boolean | 由 `code === 200` 派生，便于快速判断 |

> 设计要点：`data` 的内容即此前接口返回的“有效负载”。对于原本返回对象的接口（如题目列表、通知列表），`data` 内保留了原有的字段（如 `questions`、`total`、`notifications`、`unreadCount`）；对于原本返回数组/标量的接口（如试卷、考试历史、错题、关键词），`data` 直接为该数组/标量，调用方无需额外解包。

### 3.3 状态码（ResultCode）

| code | 含义 | 典型场景 |
|------|------|----------|
| 200 | 成功 | 所有成功响应 |
| 400 | 请求参数错误 | 参数缺失/校验失败（如提交考试缺少字段、缺少题目 ID） |
| 401 | 未登录或登录已过期 | 缺少/无效 Token、账号不存在 |
| 403 | 没有权限 | 非管理员操作、`/api/v1/admin/**` 越权、账号被禁用 |
| 404 | 资源不存在 | 题目/培训题目 ID 不存在 |
| 409 | 资源状态冲突 | 并发或状态不一致（预留） |
| 500 | 服务器内部错误 | 未预期异常（由全局异常处理器兜底） |

> 服务端 `GlobalExceptionHandler` 会将上述异常统一转换为对应 `code` 的标准响应，避免散落的 `try/catch` 与不一致的 `{"error": ...}` 结构。

### 3.4 前端解包约定（window.PRTS / api.js）

前端 `js/utils/common.js` 的 `request()` 会自动解包信封，对调用方屏蔽差异：

- `data` 为对象 → 字段提升到顶层，并附带 `code`/`message`/`success` 后返回（兼容既有 `res.success`、`res.questions`、`res.records` 等写法）
- `data` 为数组/标量 → 原样返回（兼容 `Array.isArray(res)` 写法）
- `code !== 200` → 抛出异常（携带 `message`），由调用方 `.catch` 处理
- 登录/注册失败时不抛异常，而是返回 `{ success:false, message }`，与既有交互逻辑保持一致

### 3.5 接口一览

#### 认证 `/api/v1/auth`
| 方法 | 路径 | 说明 | 鉴权 |
|------|------|------|------|
| POST | `/auth/register` | 注册 `{username,password,email?}` | 公开 |
| POST | `/auth/login` | 登录 `{username,password}` → `data:{token,user}` | 公开 |
| POST | `/auth/logout` | 登出 | 公开 |
| GET | `/auth/profile` | 当前用户 `{id,username,isAdmin}` | 需登录 |

#### 题库 `/api/v1/questions` 与 `/api/v1/training/questions`
| 方法 | 路径 | 说明 | 鉴权 |
|------|------|------|------|
| GET | `/questions?page&size&type&difficulty&keyword` | 正式题库列表，`data:{questions,total,page,size,pages}` | 公开 |
| GET | `/questions/{id}` | 题目详情 `QuestionDTO` | 公开 |
| POST | `/questions` | 创建题目 → `data:{id}` | 需登录 |
| PUT | `/questions/{id}` | 更新题目 | 需登录 |
| DELETE | `/questions/{id}` | 删除题目 | 需登录 |
| POST | `/admin/questions/batch-delete` | 批量删除 `{ids:[..]}` | 需登录 |
| GET | `/training/questions` | 培训题库列表（数组） | 公开 |
| GET/POST/PUT/DELETE | `/training/questions[/{id}]` | 培训题目增删改查 | 需登录 |

#### 考试 `/api/v1/exam`
| 方法 | 路径 | 说明 | 鉴权 |
|------|------|------|------|
| GET | `/exam/paper` | 生成试卷（数组 `QuestionDTO`） | 公开 |
| POST | `/exam/submit` | 提交考试 `form: userId,answers,duration` → `data:{examId,score}` | 公开 |
| GET | `/exam/history?page&size` | 考试历史（数组） | 公开/登录 |

#### 错题、通知、公告、用户、系统
| 方法 | 路径 | 说明 | 鉴权 |
|------|------|------|------|
| GET | `/answers/wrong` | 我的错题（数组） | 需登录 |
| DELETE | `/answers/wrong/{questionId}` | 隐藏错题 | 需登录 |
| GET | `/user/{id}/wrong` | 指定用户错题 | 公开 |
| GET | `/notifications?unreadOnly&page&size` | 通知列表 `data:{notifications,unreadCount,hasMore}` | 登录 |
| GET | `/notifications/unread-count` | 未读计数 `data:{unreadCount}` | 登录 |
| PUT | `/notifications/{id}/read` | 标记已读 | 登录 |
| PUT | `/notifications/read-all` | 全部已读 | 登录 |
| DELETE | `/notifications[/{id}]` | 隐藏通知 | 登录 |
| GET | `/announcements` | 公告列表 `data:{announcements}` | 公开 |
| POST | `/admin/announcements` | 发布公告 `{title,content,...}` | 管理员 |
| GET | `/admin/users?q` | 用户列表（数组） | 管理员 |
| POST | `/admin/user/permission` | 设置权限 `form: actor_id,target_id,make_admin` | 管理员 |
| GET/PUT | `/user/answer-settings` | 答题设置 `data:{autoSubmit,autoNextCorrect}` | 登录 |
| GET/POST/PUT/DELETE | `/user/training-records` | 培训记录 | 登录 |
| GET | `/keywords?mode` | 关键词列表（数组） | 公开 |
| GET | `/ping` | 健康检查 `data:{ok:true}` | 公开 |
| GET | `/stats/question/{id}` `/stats/user` `/stats/system` | 统计信息 | 公开 |

---

## 四、数据库表

| 表名 | 说明 |
|------|------|
| `users` | 用户（BCrypt 密码） |
| `questions` | 正式题库 |
| `questions_onboarding` | 入职培训题库 |
| `exam_records` / `exam_detail` | 考试记录 / 详情 |
| `user_answers` | 答题记录 |
| `announcements` | 系统公告 |
| `wrong_visibility` | 错题隐藏状态 |
| `notifications_state` | 通知已读/隐藏状态 |
| `answer_settings` | 答题设置 |
| `training_records` | 培训记录 |

---

## 五、部署方式

### 1. 准备数据库
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

### 3. 构建并启动后端
```bash
mvn clean package -DskipTests
java -jar target/PRTS.TRAININGSYSTEM-2.0.0.jar
```
首次启动：Flyway 自动建表 → `CsvImportService` 从 `data/` 导入 CSV（幂等，仅当表为空时）。默认管理员：`admin / admin`。

### 4. 前端部署
前端为纯静态文件，可用任意静态服务器托管 `P.R.T.S.TrainingSystemFrontend/`：
```bash
cd P.R.T.S.TrainingSystemFrontend
npx http-server -p 3000
```
> 前端通过 `window.API_BASE_URL` 或 `config.js` 自动探测后端地址（localhost → `http://localhost:8080`）。

---

## 六、开发指南

### 后端
- **新增接口**：直接返回 `Result<T>`（`Result.success(data)` / `Result.fail(ResultCode.X, msg)`）；业务校验失败抛 `BusinessException`。
- **统一异常**：勿在控制器中散写 `Map.of("error", ...)`，交由 `GlobalExceptionHandler` 统一处理。
- **状态码**：复用 `ResultCode` 枚举；新增语义请用新枚举值，勿硬编码。

### 前端
- **发起请求**：统一走 `window.api.*`（或兼容别名 `userApi` / `questionApi` / `examApi` 等）。
- **底层能力**：`window.PRTS`（`js/utils/common.js`）提供 `request/get/post/put/del`、令牌存储 `getToken/setToken/clearAuth`、格式化 `format`、提示 `toast`。
- **新增工具/组件**：公共逻辑放入 `js/utils/`，可复用 UI 放入 `js/components/`，并优先挂载到 `window.PRTS` 命名空间，保持 camelCase 命名与单一路由。
- **命名与风格**：目录按 `utils / components / app` 职责划分；常量集中到 `config.js`；避免散落全局变量。

### 代码规范
- 后端：分层清晰 Controller → Service → Repository；所有响应经 `Result` 封装。
- 前端：所有接口调用经 `api.js`；所有弹窗/提示经 `uiModal`（即 `PRTSModal`）；令牌与用户信息统一由 `PRTS.STORAGE` 管理。

---

## 七、开发模式（热更新 / 前后端联动）

日常开发推荐**前后端同时热更新**：改后端 Java 自动重启、改前端静态文件浏览器自动刷新，无需反复打包。

### 7.1 后端热重载（Spring Boot DevTools）

`pom.xml` 已引入 `spring-boot-devtools`（`optional`，仅开发期生效，不会打进生产 jar）。启动后，classpath 任意变动会触发**自动重启**，无需手动停启。

```bash
# 本机 Windows 必须用 mvn.cmd（Unix 的 mvn 启动器会报 ClassNotFoundException）
cd P.R.T.S.TrainingSystem
mvn.cmd spring-boot:run
# 后端监听 8080，控制台出现 Started PrtsApplication 即就绪
```

> 自动重启只重启应用上下文，比冷启动快很多。若想完全关闭热重载，删除 `spring-boot-devtools` 依赖即可。
> 若用 IDE（IntelliJ / Eclipse），直接以 Spring Boot 方式运行 `PrtsApplication` 并开启「自动编译 / Build project automatically」效果相同。

### 7.2 前端热重载（纯静态，无构建步骤）

前端是**无构建多页应用**，API 调用使用绝对地址（默认 `http://localhost:8080`，见 `js/config.js` 的 `getApiBaseUrl()`），而后端 CORS 已放行 `*` 并允许凭据，因此**前端无论跑在哪个端口，都能直连真实后端 8080**，无需反向代理。

**方式 A（推荐，保存即自动刷新浏览器）：**

```bash
cd P.R.T.S.TrainingSystemFrontend
npx live-server . --port=8888 --watch=js,css,*.html
# 或先安装：npm i -D live-server，再执行 npx live-server . -p 8888
```

**方式 B（零安装，手动刷新）：**

```bash
cd P.R.T.S.TrainingSystemFrontend
npx http-server . -p 8888 -c-1      # -c-1 禁用缓存，改完按 F5 即可看到
```

打开浏览器访问 `http://localhost:8888` 即可。后端已在 8080 运行，所有接口请求会直接打到真实后端。

### 7.3 Mock 模式（不需要后端，纯前端联调）

仓库自带 `json-server` 模拟接口，可完全脱离后端做前端开发：

```bash
cd P.R.T.S.TrainingSystemFrontend
npm install
npm run dev:full          # 同时起 http-server(8888) + json-server mock(8889)
```

> 注意：`npm run dev` 内置了把未知请求代理到 `8889` 的 json-server；但本项目前端 API 基址默认指向 `localhost:8080`，故 mock 模式仅在你手动把 `window.API_BASE_URL` 指向 mock 地址时才会真正生效。联调真实后端请使用 7.1 + 7.2。

### 7.4 推荐的一键开发姿势

开两个终端：

| 终端 | 命令 | 作用 |
|------|------|------|
| 终端 1 | `mvn.cmd spring-boot:run` | 后端 8080，改 Java 自动重启 |
| 终端 2 | `npx live-server . -p 8888 --watch=js,css,*.html` | 前端 8888，改文件自动刷新 |

访问 `http://localhost:8888` 即可获得前后端热更新体验。
