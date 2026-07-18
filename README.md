# PRTS Training System

> 明日方舟主题的博士业务能力考核与培训系统。用于学习、课程设计和非商业的同人项目交流；与鹰角网络及《明日方舟》官方无关。

PRTS Training System 是一个前后端分离的在线答题系统，包含正式考试、入职培训、题库管理、错题、公告通知和管理员审计等功能。

## 功能

- 用户注册、登录与 JWT 身份验证
- 正式题库浏览、随机组卷、交卷、历史记录与错题管理
- 入职培训题库和培训进度记录
- 公告、未读状态与通知中心
- 管理员题库维护、公告发布、用户权限调整与审计日志
- CSV 示例题库的首次导入，以及 Flyway 数据库迁移

## 技术栈

| 部分 | 技术 |
| --- | --- |
| 后端 | Java 21、Spring Boot 3.2、Spring Security、Spring Data JPA |
| 数据库 | MySQL 8.0+、Flyway |
| 认证 | JWT（jjwt）与 BCrypt |
| 前端 | 原生 JavaScript、Vue 2 CDN、多页面静态站点 |
| 构建/开发 | Maven 3.x、Node.js（仅用于静态服务器和辅助脚本） |

## 项目结构

```text
.
├── src/main/java/                         # Spring Boot 后端
├── src/main/resources/
│   ├── application.yml                    # 后端配置（从根目录 .env 读取敏感变量）
│   └── db/migration/                      # Flyway 迁移脚本
├── P.R.T.S.TrainingSystemFrontend/        # 原生 JS + Vue 2 静态前端
├── data_example/                          # 可安全提交的示例题库/公告 CSV
├── data/                                  # 本地导入数据，已忽略，不提交
├── .env.example                           # 后端环境变量模板
├── .gitignore
└── pom.xml
```

## 快速开始

### 1. 前置条件

- JDK 21
- Maven 3.8+
- MySQL 8.0+
- Node.js 14+（启动前端时需要；也可以使用任意静态文件服务器）

### 2. 创建数据库

```sql
CREATE DATABASE prts_db
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_unicode_ci;
```

### 3. 配置后端环境变量

将模板复制为项目根目录的 `.env`。该文件已被 Git 忽略，绝不能提交。

```powershell
Copy-Item .env.example .env
```

至少填写以下值：

```ini
DB_USERNAME=root
DB_PASSWORD=your_mysql_password
# 32 字节以上随机字节的 Base64 编码，不是普通文本
JWT_SECRET=replace_with_a_long_random_base64_encoded_secret
ADMIN_USERNAME=admin
ADMIN_PASSWORD=use_a_strong_initial_password
```

可用下面的 PowerShell 命令生成 `JWT_SECRET`：

```powershell
[Convert]::ToBase64String([System.Security.Cryptography.RandomNumberGenerator]::GetBytes(32))
```

若未设置 `ADMIN_PASSWORD`，首次导入时应用会生成随机管理员密码并写入启动日志；生产环境请显式设置强密码。

### 4. 准备示例数据（可选）

应用默认从被忽略的 `data/` 目录读取 CSV，而仓库只提交 `data_example/`，以防将实际用户和答题数据公开。需要载入仓库示例题库时，先执行：

```powershell
Copy-Item data_example data -Recurse
```

不复制也可以启动：系统会跳过不存在的 CSV，并按环境变量创建管理员。数据库已有用户时，默认不会重复导入数据。

### 5. 启动后端

```powershell
mvn spring-boot:run
```

或打包运行：

```powershell
mvn clean package -DskipTests
java -jar target/PRTS.TRAININGSYSTEM-2.0.0.jar
```

后端默认监听 `http://127.0.0.1:8080`，健康检查地址为 `http://127.0.0.1:8080/api/v1/ping`。

### 6. 启动前端

前端没有 Vite 等构建步骤，也不读取前端 `.env` 文件。安装依赖并启动静态服务器：

```powershell
Set-Location P.R.T.S.TrainingSystemFrontend
npm install
npm run dev
```

然后打开 `http://localhost:8888`。在 `localhost` 或 `127.0.0.1` 下访问时，前端默认请求 `http://localhost:8080/api/v1`。

> `npm run dev:full` 会额外启动 `json-server` mock 服务，但静态前端默认仍指向真实后端；它不是完整的开箱即用 mock 模式。

## 前端 API 地址配置

API 基址由 `P.R.T.S.TrainingSystemFrontend/js/config.js` 中的 `getApiBaseUrl()` 在运行时决定：

1. 优先使用加载 `config.js` 前定义的 `window.API_BASE_URL`；
2. 本地访问时默认使用 `http://localhost:8080`；
3. 其他域名使用 `config.js` 中配置的测试、预发布或生产地址。

生产部署前应将这些示例域名替换为真实后端地址。也可以在页面加载 `js/config.js` 之前加入：

```html
<script>window.API_BASE_URL = 'https://api.example.com';</script>
```

该值只填写源地址，不包含 `/api/v1`；前端会自动拼接 API 前缀和版本。

## 配置与部署注意事项

- 根目录 `.env` 由 Spring Boot 的 `optional:file:.env[.properties]` 加载；部署时也可直接设置同名系统环境变量。
- `data/` 是本地导入目录，包含用户或运行数据时不要提交。`data_example/` 只应存放可公开的示例内容。
- `app.data.force-import=true` 会清空多个业务表后重新导入 CSV，仅用于本地开发重置，切勿在生产环境使用。
- 后端当前绑定 `127.0.0.1`。若要让其他机器访问，请在部署环境中调整 `server.address`，同时配置反向代理、HTTPS、CORS 和防火墙。
- 前端配置是运行时 JavaScript 配置，不使用 Vite 环境变量，也不需要前端 `.env.example`。

## API 概览

所有主要接口以 `/api/v1` 为前缀。常用端点：

| 模块 | 示例端点 |
| --- | --- |
| 认证 | `POST /auth/register`、`POST /auth/login`、`GET /auth/profile` |
| 题库 | `GET /questions`、`GET /training/questions` |
| 考试 | `GET /exam/paper`、`POST /exam/submit`、`GET /exam/history` |
| 通知 | `GET /announcements`、`GET /notifications` |
| 管理 | `GET /admin/users`、`GET /admin/audit-logs` |

接口响应由后端统一封装为 `code`、`message`、`data` 和 `success` 字段。完整路由以各控制器源码为准。

