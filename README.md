# P.R.T.S. Training System

<div align="center">

![Java](https://img.shields.io/badge/Java-21-orange?logo=openjdk)
![Vue.js](https://img.shields.io/badge/Vue.js-2.6-green?logo=vue.js)
![License](https://img.shields.io/badge/License-Educational-blue)
![Status](https://img.shields.io/badge/Status-Active-success)

**基于明日方舟主题的在线题库训练系统**

**An Arknights-themed Online Training System**

[English](#english) | [中文](#chinese)

</div>

---

## 📋 目录 / Table of Contents

<details>
<summary>点击展开 / Click to expand</summary>

- [项目概述 / Project Overview](#项目概述--project-overview)
- [快速开始 / Quick Start](#快速开始--quick-start)
  - [前置要求 / Prerequisites](#前置要求--prerequisites)
  - [安装步骤 / Installation Steps](#安装步骤--installation-steps)
- [使用指南 / User Guide](#使用指南--user-guide)
  - [学生端功能 / Student Features](#学生端功能--student-features)
  - [管理员功能 / Admin Features](#管理员功能--admin-features)
- [项目结构 / Project Structure](#项目结构--project-structure)
- [开发指南 / Development Guide](#开发指南--development-guide)
- [故障排除 / Troubleshooting](#故障排除--troubleshooting)
- [API 参考文档 / API Reference](#api-参考文档--api-reference)
- [贡献指南 / Contributing](#贡献指南--contributing)
- [许可证 / License](#许可证--license)
- [联系方式 / Contact](#联系方式--contact)

</details>

---

<a name="chinese"></a>

## 项目概述 / Project Overview

<a name="english"></a>

**P.R.T.S. (Rhodes Island Training System)** 是一个基于明日方舟的题库训练系统，用于问答和记录管理。这是一个前后端分离的Web应用，提供完整的在线学习、考试和练习功能。

**P.R.T.S. (Rhodes Island Training System)** is an Arknights-themed training system for question bank management, testing, and learning. It's a full-stack web application with separate frontend and backend, providing comprehensive online learning, examination, and practice features.

### 技术栈 / Tech Stack

**后端 / Backend:**
- **语言 / Language**: Java 17+ (recommended 21)
- **框架 / Framework**: JDK HttpServer (lightweight, no external dependencies)
- **数据存储 / Data Storage**: CSV files (local filesystem)
- **端口 / Port**: 8080

**前端 / Frontend:**
- **框架 / Framework**: Vue.js 2.6
- **构建工具 / Build Tool**: npm
- **UI组件 / UI Components**: Custom CSS with Font Awesome icons
- **端口 / Port**: 8888

### 主要功能 / Key Features

- ✅ **用户管理** / User Management: 注册、登录、权限管理 (Register, Login, Permission Management)
- ✅ **题库系统** / Question Bank: 多种题型（单选、多选、判断）(Multiple question types)
- ✅ **学习模式** / Study Mode: 按难度、类型练习 (Practice by difficulty and type)
- ✅ **考试系统** / Exam System: 随机生成试卷、自动评分 (Random paper generation, auto-grading)
- ✅ **错题本** / Wrong Questions Book: 错题记录、重做 (Track and redo wrong answers)
- ✅ **薄弱练习** / Weak Point Practice: 针对性训练 (Targeted training)
- ✅ **统计分析** / Statistics: 答题记录、正确率分析 (Answer history and accuracy analysis)
- ✅ **管理后台** / Admin Panel: 题目管理、用户管理 (Question and user management)

---

## 快速开始 / Quick Start

### 前置要求 / Prerequisites

1. **Java Development Kit (JDK) 17 or higher**
   - 下载 / Download: [Oracle JDK](https://www.oracle.com/java/technologies/downloads/) or [OpenJDK](https://openjdk.org/)
   - 验证安装 / Verify: `java -version`
   - 注意 / Note: Maven 构建需要 JDK 21（如 pom.xml 中指定），但手动编译可使用 JDK 17+ / Maven build requires JDK 21 (as specified in pom.xml), but manual compilation works with JDK 17+

2. **Node.js 14+ and npm 6+**
   - 下载 / Download: [Node.js Official Site](https://nodejs.org/)
   - 验证安装 / Verify: `node -v` and `npm -v`

3. **Git** (for cloning the repository)
   - 下载 / Download: [Git Official Site](https://git-scm.com/)

### 安装步骤 / Installation Steps

#### 1. 克隆仓库 / Clone Repository

```bash
git clone https://github.com/TeamHourai/P.R.T.S.TrainingSystem.git
cd P.R.T.S.TrainingSystem
```

#### 2. 启动后端服务 / Start Backend Server

**方法一：使用 Maven / Method 1: Using Maven**

```bash
# 编译项目 / Compile project
mvn clean compile

# 运行主程序 / Run main application
mvn exec:java -Dexec.mainClass="com.hourai.prts.Main"
```

**方法二：手动编译和运行 / Method 2: Manual Compilation**

```bash
# 创建输出目录 / Create output directory
mkdir -p classes

# 编译所有 Java 文件 / Compile all Java files
javac -d classes -encoding UTF-8 src/main/java/com/hourai/prts/**/*.java

# 运行应用 / Run application
java -cp classes com.hourai.prts.Main
```

**方法三：使用 PowerShell 构建脚本（Windows）/ Method 3: PowerShell Build Script (Windows)**

```powershell
powershell -ExecutionPolicy Bypass -File .\build_jpackage.ps1
```

后端服务将在 **http://localhost:8080** 启动
Backend server will start at **http://localhost:8080**

#### 3. 安装并启动前端 / Install and Start Frontend

```bash
# 进入前端目录 / Navigate to frontend directory
cd P.R.T.S.TrainingSystemFrontend

# 安装依赖 / Install dependencies
npm install

# 启动开发服务器 / Start development server
npm run dev
```

前端应用将在 **http://localhost:8888** 启动
Frontend application will start at **http://localhost:8888**

#### 4. 访问应用 / Access Application

打开浏览器访问 / Open browser and navigate to:
```
http://localhost:8888
```

**默认管理员账号 / Default Admin Account:**
- 用户名 / Username: `admin`
- 密码 / Password: `admin`

**默认学生账号 / Default Student Account:**
- 用户名 / Username: `student1`
- 密码 / Password: `student1`

### 🎯 快速提示 / Quick Tips

- 🔑 首次使用建议先用管理员账号登录，查看系统配置 / First-time users should log in with admin account to check system configuration
- 📚 题库数据存储在 `data/` 目录的 CSV 文件中 / Question bank data is stored in CSV files in the `data/` directory
- 🚀 后端会在首次启动时自动创建示例数据 / Backend automatically creates sample data on first run
- 💡 前端可以在任何现代浏览器中运行 / Frontend works in any modern browser
- 🔄 修改后端代码后需要重新编译 / Backend code changes require recompilation

### 📸 界面预览 / Interface Preview

> 注：界面截图将在此展示 / Note: Interface screenshots will be displayed here

主页面包含 / Main interface includes:
- 📖 学习模式：浏览和练习题目 / Study Mode: Browse and practice questions
- 📝 考试模式：模拟考试环境 / Exam Mode: Simulated exam environment
- 📊 统计分析：查看学习进度 / Statistics: View learning progress
- ⚙️ 管理后台：题目和用户管理 / Admin Panel: Question and user management

---

## 使用指南 / User Guide

### 学生端功能 / Student Features

1. **注册/登录 / Register/Login**
   - 首次使用需要注册账号 / First-time users need to register
   - 登录后可以开始学习 / After login, start learning

2. **学习模式 / Study Mode**
   - 选择"学习"进入题库 / Select "Study" to enter question bank
   - 可按题型、难度筛选 / Filter by question type and difficulty
   - 查看题目解析 / View question analysis

3. **考试模式 / Exam Mode**
   - 选择"考试"开始测试 / Select "Exam" to start test
   - 系统随机生成试卷 / System generates random paper
   - 提交后自动评分 / Auto-grading after submission

4. **错题本 / Wrong Questions**
   - 查看历史错题 / View historical wrong answers
   - 重做错题加强记忆 / Redo wrong questions to improve

5. **薄弱练习 / Weak Point Practice**
   - 针对错误率高的题目练习 / Practice questions with high error rate
   - 提升薄弱环节 / Improve weak areas

6. **统计分析 / Statistics**
   - 查看答题记录 / View answer history
   - 分析正确率趋势 / Analyze accuracy trends

### 管理员功能 / Admin Features

1. **题目管理 / Question Management**
   - 添加、编辑、删除题目 / Add, edit, delete questions
   - 批量导入题目 / Bulk import questions
   - 设置题目难度和类型 / Set difficulty and type

2. **用户管理 / User Management**
   - 查看所有用户 / View all users
   - 管理用户权限 / Manage user permissions
   - 查看用户统计 / View user statistics

3. **公告管理 / Announcement Management**
   - 发布系统公告 / Post system announcements
   - 编辑通知内容 / Edit notification content

---

## 项目结构 / Project Structure

```
P.R.T.S.TrainingSystem/
├── src/main/java/com/hourai/prts/     # 后端源代码 / Backend Source
│   ├── Main.java                      # 主程序入口 / Main Entry
│   ├── CorsFilter.java                # CORS 跨域过滤器 / CORS Filter
│   ├── entity/                        # 实体类 / Entities
│   │   ├── User.java
│   │   ├── Question.java
│   │   ├── UserAnswer.java
│   │   ├── ExamRecord.java
│   │   ├── Announcement.java
│   │   └── TrainingRecord.java
│   ├── data/                          # 数据访问层 / Data Access
│   │   └── DataStore.java
│   ├── dao/                           # DAO层 / DAO Layer
│   ├── service/                       # 业务逻辑层 / Service Layer
│   ├── handler/                       # HTTP 请求处理器 / HTTP Handlers
│   │   ├── RegisterHandler.java
│   │   ├── LoginHandler.java
│   │   ├── QuestionsHandler.java
│   │   ├── ExamPaperHandler.java
│   │   ├── ExamSubmitHandler.java
│   │   └── UserHandler.java
│   ├── tool/                          # 工具类 / Tools
│   └── utils/                         # 工具方法 / Utilities
├── P.R.T.S.TrainingSystemFrontend/    # 前端代码 / Frontend Code
│   ├── index.html                     # 主页面 / Main Page
│   ├── exam.html                      # 考试页面 / Exam Page
│   ├── editor.html                    # 题目编辑器 / Question Editor
│   ├── admin_permissions.html         # 权限管理 / Permissions
│   ├── training-editor.html           # 训练编辑器 / Training Editor
│   ├── announcement-editor.html       # 公告编辑器 / Announcement Editor
│   ├── css/                           # 样式文件 / Stylesheets
│   ├── js/                            # JavaScript文件 / JavaScript Files
│   │   ├── config.js                  # API配置 / API Config
│   │   ├── api/                       # API调用 / API Calls
│   │   ├── components/                # 组件 / Components
│   │   └── utils/                     # 工具函数 / Utilities
│   ├── images/                        # 图片资源 / Images
│   └── package.json                   # npm配置 / npm Config
├── data/                              # 数据文件 / Data Files
│   ├── users.csv                      # 用户数据 / User Data
│   ├── questions.csv                  # 题目数据 / Question Data
│   ├── user_answers.csv               # 答题记录 / Answer Records
│   ├── exam_records.csv               # 考试记录 / Exam Records
│   ├── training_records.csv           # 训练记录 / Training Records
│   ├── announcements.csv              # 公告 / Announcements
│   └── wrong_visibility.csv           # 错题可见性 / Wrong Q Visibility
├── pom.xml                            # Maven配置 / Maven Config
├── build_jpackage.ps1                 # Windows构建脚本 / Windows Build Script
└── README.md                          # 项目文档 / Documentation
```

---

## 开发指南 / Development Guide

### 后端开发 / Backend Development

#### 添加新的 API 端点 / Adding New API Endpoints

1. 创建 Handler 类实现 `HttpHandler` 接口
2. 在 `Main.java` 中注册路由
3. 使用 `DataStore` 访问数据

示例 / Example:
```java
public class CustomHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        // Your logic here
        Utils.send(exchange, 200, "{\"status\":\"ok\"}");
    }
}

// In Main.java
server.createContext("/custom", new CustomHandler());
```

#### 数据文件格式 / Data File Format

所有数据存储在 `data/` 目录的 CSV 文件中 / All data is stored in CSV files in the `data/` directory:

- **users.csv**: `id,username,password,isAdmin,createdAt`
- **questions.csv**: `id,type,difficulty,resource,question,hasPicture,options,answer,analysis`
- **user_answers.csv**: `id,userId,questionId,questionType,isCorrect,selectedOption,answeredAt`
- **exam_records.csv**: `id,userId,score,examAt`

### 前端开发 / Frontend Development

#### 前端架构 / Frontend Architecture

前端采用多页面应用（MPA）架构，使用 Vue.js 2.6 进行状态管理
Frontend uses Multi-Page Application (MPA) architecture with Vue.js 2.6 for state management

#### 添加新页面 / Adding New Pages

1. 在根目录创建 HTML 文件 / Create HTML file in root directory
2. 创建对应的 CSS 文件在 `css/` 目录 / Create corresponding CSS in `css/` directory
3. 创建 JS 逻辑在 `js/` 目录 / Create JS logic in `js/` directory
4. 在主页面添加导航链接 / Add navigation link in main page

#### API 调用 / API Calls

所有 API 配置在 `js/config.js` 中 / All API configurations are in `js/config.js`:

```javascript
const API_BASE_URL = 'http://localhost:8080';
```

使用 axios 发送请求 / Use axios to send requests:

```javascript
axios.get(`${API_BASE_URL}/questions`)
    .then(response => {
        // Handle response
    })
    .catch(error => {
        // Handle error
    });
```

### 构建和部署 / Build and Deploy

#### 后端打包 / Backend Packaging

**使用 Maven / Using Maven:**
```bash
mvn clean package
java -jar target/PRTS.TRAININGSYSTEM-1.0-SNAPSHOT.jar
```

**使用 PowerShell 脚本 / Using PowerShell Script:**
```powershell
powershell -ExecutionPolicy Bypass -File .\build_jpackage.ps1
```

这将创建 / This will create:
- `homeworkapp.jar` - 可执行 JAR 文件 / Executable JAR
- `dist/` - jpackage 应用程序镜像 / jpackage application image

#### 前端部署 / Frontend Deployment

前端是静态文件，可以直接部署到任何 Web 服务器
Frontend consists of static files that can be deployed to any web server:

```bash
# 使用简单的 HTTP 服务器 / Using simple HTTP server
cd P.R.T.S.TrainingSystemFrontend
npx http-server . -p 8888
```

或使用 nginx、Apache 等 / Or use nginx, Apache, etc.

---

## 故障排除 / Troubleshooting

### 常见问题 / Common Issues

#### 1. 后端无法启动 / Backend Won't Start

**问题**: "Address already in use" 或 端口 8080 被占用
**Problem**: "Address already in use" or Port 8080 is occupied

**解决方案 / Solution**:
```bash
# Windows
netstat -ano | findstr :8080
taskkill /PID <PID> /F

# Linux/Mac
lsof -i :8080
kill -9 <PID>
```

或在 `Main.java` 中修改端口 / Or change port in `Main.java`

#### 2. 前端 API 请求失败 / Frontend API Requests Fail

**问题**: CORS 错误或网络错误
**Problem**: CORS error or network error

**解决方案 / Solution**:
- 确保后端服务正在运行 / Ensure backend is running
- 检查 `js/config.js` 中的 `API_BASE_URL` 配置 / Check `API_BASE_URL` in `js/config.js`
- 后端已包含 CORS 过滤器 / Backend includes CORS filter

#### 3. 编译错误 / Compilation Errors

**问题**: javac 找不到或编译失败
**Problem**: javac not found or compilation fails

**解决方案 / Solution**:
- 确认 JDK 21+ 已安装 / Ensure JDK 21+ is installed
- 检查 JAVA_HOME 环境变量 / Check JAVA_HOME environment variable
- 使用正确的编码: `javac -encoding UTF-8 ...`

#### 4. 数据文件丢失 / Data Files Missing

**问题**: 应用启动时找不到数据文件
**Problem**: Data files not found on startup

**解决方案 / Solution**:
- 数据文件会在首次启动时自动创建 / Data files are created automatically on first run
- 检查 `data/` 目录权限 / Check `data/` directory permissions
- 手动创建 `data/` 目录 / Manually create `data/` directory

#### 5. npm install 失败 / npm install Fails

**问题**: 依赖安装失败
**Problem**: Dependencies installation fails

**解决方案 / Solution**:
```bash
# 清除缓存 / Clear cache
npm cache clean --force

# 使用淘宝镜像（中国用户）/ Use Taobao mirror (China users)
npm install --registry=https://registry.npmmirror.com

# 或使用 cnpm / Or use cnpm
npm install -g cnpm --registry=https://registry.npmmirror.com
cnpm install
```

---

## API 参考文档 / API Reference

以下为完整的 API 接口文档 / Complete API documentation below:

### API 端点概览 / API Endpoints Overview

| 方法 / Method | 路径 / Path | 描述 / Description |
|---------------|-------------|-------------------|
| POST | `/register` | 用户注册 / User Registration |
| POST | `/login` | 用户登录 / User Login |
| GET | `/questions` | 获取所有题目 / Get All Questions |
| GET | `/exam/paper?count=N` | 获取随机试卷 / Get Random Exam Paper |
| POST | `/exam/submit` | 提交考试答案 / Submit Exam Answers |
| GET | `/user/{id}/wrong` | 获取用户错题 / Get User's Wrong Questions |
| GET | `/ping` | 健康检查 / Health Check |

### 详细 API 文档 / Detailed API Documentation

---

## 一、核心类详细文档

### 1. Main. java

**包名**: `com.hourai.prts`

**描述**: 应用程序主入口，负责初始化 HTTP 服务器和路由配置。

#### 类：Main

##### 方法：

| 方法签名 | 返回类型 | 描述 |
|---------|---------|------|
| `main(String[] args)` | `void` | 程序入口，创建并启动 HTTP 服务器 |

##### 功能说明：
- 初始化数据文件（调用 `DataStore.ensureDataFiles()`）
- 创建 HTTP 服务器，监听 8080 端口
- 注册以下路由：
  - `/register` - 用户注册
  - `/login` - 用户登录
  - `/questions` - 获取题目列表
  - `/exam/paper` - 获取随机试卷
  - `/exam/submit` - 提交考试答案
  - `/user` - 用户相关操作（如获取错题）
  - `/ping` - 健康检查

---

## 二、实体类（Entity）

### 2.1 User.java

**包名**: `com.hourai.prts.entity`

**描述**: 用户实体类

#### 成员变量：

| 变量名 | 类型 | 描述 |
|--------|------|------|
| `id` | `long` | 用户唯一标识 |
| `username` | `String` | 用户名 |
| `password` | `String` | 密码（明文存储，仅示例用） |
| `isAdmin` | `boolean` | 是否为管理员 |
| `createdAt` | `String` | 创建时间 |

#### 构造函数：

```java
public User(long id, String username, String password, boolean isAdmin, String createdAt)
```

---

### 2.2 Question.java

**包名**: `com. hourai.prts.entity`

**描述**: 题目实体类

#### 成员变量：

| 变量名 | 类型 | 描述 |
|--------|------|------|
| `id` | `long` | 题目唯一标识 |
| `type` | `int` | 题目类型（1=单选，2=多选等） |
| `difficulty` | `int` | 难度等级（1=简单，2=中等，3=困难） |
| `resource` | `String` | 资源/题目来源 |
| `question` | `String` | 题目内容 |
| `hasPicture` | `boolean` | 是否包含图片 |
| `options` | `List<String>` | 选项列表 |
| `answer` | `int` | 正确答案索引 |
| `analysis` | `String` | 题目解析 |

#### 构造函数：

```java
public Question(long id, int type, int difficulty, String resource, 
                String question, boolean hasPicture, List<String> options, 
                int answer, String analysis)
```

---

### 2.3 UserAnswer.java

**包名**: `com.hourai.prts.entity`

**描述**: 用户答题记录实体类

#### 成员变量：

| 变量名 | 类型 | 描述 |
|--------|------|------|
| `id` | `long` | 记录唯一标识 |
| `userId` | `long` | 用户 ID |
| `questionId` | `long` | 题目 ID |
| `questionType` | `String` | 题目类型 |
| `isCorrect` | `boolean` | 是否答对 |
| `selectedOption` | `int` | 用户选择的选项 |
| `answeredAt` | `String` | 答题时间 |

#### 构造函数：

```java
public UserAnswer(long id, long userId, long questionId, String questionType, 
                  boolean isCorrect, int selectedOption, String answeredAt)
```

---

### 2. 4 ExamRecord.java

**包名**: `com.hourai.prts.entity`

**描述**: 考试记录实体类

#### 成员变量：

| 变量名 | 类型 | 描述 |
|--------|------|------|
| `id` | `long` | 考试记录唯一标识 |
| `userId` | `long` | 用户 ID |
| `score` | `int` | 考试得分 |
| `examAt` | `String` | 考试时间 |

#### 构造函数：

```java
public ExamRecord(long id, long userId, int score, String examAt)
```

---

## 三、数据访问层（Data）

### 3.1 DataStore.java

**包名**: `com.hourai.prts.data`

**描述**: 数据存储和访问管理类，负责 CSV 文件的读写操作

#### 静态常量：

| 常量名 | 类型 | 描述 |
|--------|------|------|
| `DATA_DIR` | `Path` | 数据目录路径（./data/） |
| `USERS_FILE` | `Path` | 用户数据文件路径 |
| `QUESTIONS_FILE` | `Path` | 题目数据文件路径 |
| `USER_ANSWERS_FILE` | `Path` | 答题记录文件路径 |
| `EXAM_RECORDS_FILE` | `Path` | 考试记录文件路径 |

#### 方法列表：

##### 初始化方法：

| 方法签名 | 返回类型 | 描述 |
|---------|---------|------|
| `ensureDataFiles()` | `void` | 确保数据文件存在，不存在则创建并初始化示例数据 |

##### 用户相关方法：

| 方法签名 | 返回类型 | 描述 |
|---------|---------|------|
| `loadUsers()` | `List<User>` | 从 CSV 文件加载所有用户 |
| `appendUser(User u)` | `void` | 添加新用户到 CSV 文件 |
| `nextId(List<? > list)` | `long` | 生成下一个可用 ID |

##### 题目相关方法：

| 方法签名 | 返回类型 | 描述 |
|---------|---------|------|
| `loadQuestions()` | `List<Question>` | 从 CSV 文件加载所有题目 |

##### 答题记录相关方法：

| 方法签名 | 返回类型 | 描述 |
|---------|---------|------|
| `loadUserAnswers()` | `List<UserAnswer>` | 从 CSV 文件加载所有答题记录 |
| `appendUserAnswer(UserAnswer ua)` | `void` | 添加答题记录到 CSV 文件 |

##### 考试记录相关方法：

| 方法签名 | 返回类型 | 描述 |
|---------|---------|------|
| `loadExamRecords()` | `List<ExamRecord>` | 从 CSV 文件加载所有考试记录 |
| `appendExamRecord(ExamRecord er)` | `void` | 添加考试记录到 CSV 文件 |

#### 数据格式说明：

**users.csv 格式：**
```
id,username,password,isAdmin,createdAt
1,admin,admin,true,2025-01-01 00:00:00
```

**questions.csv 格式：**
```
id,type,difficulty,resource,question,hasPicture,options,answer,analysis
1,1,2,"","以下哪个是 Java 的关键字？",0,"function|class|static|define",3,"static 是关键字"
```

**user_answers. csv 格式：**
```
id,userId,questionId,questionType,isCorrect,selectedOption,answeredAt
1,2,1,单选题,true,3,2025-01-01 10:00:00
```

**exam_records.csv 格式：**
```
id,userId,score,examAt
1,2,85,2025-01-01 11:00:00
```

---

## 四、请求处理器（Handler）

### 4.1 RegisterHandler.java

**包名**: `com.hourai.prts. handler`

**接口**: `HttpHandler`

**路由**: `POST /register`

#### 功能：
用户注册

#### 请求参数：

| 参数名 | 类型 | 必填 | 描述 |
|--------|------|------|------|
| `username` | String | 是 | 用户名 |
| `password` | String | 是 | 密码 |

#### 响应示例：

**成功 (200):**
```json
{
  "id": 3,
  "username": "newuser"
}
```

**失败 (400):**
```json
{
  "error": "username exists"
}
```

---

### 4.2 LoginHandler.java

**包名**: `com.hourai.prts. handler`

**接口**: `HttpHandler`

**路由**: `POST /login`

#### 功能：
用户登录

#### 请求参数：

| 参数名 | 类型 | 必填 | 描述 |
|--------|------|------|------|
| `username` | String | 是 | 用户名 |
| `password` | String | 是 | 密码 |

#### 响应示例：

**成功 (200):**
```json
{
  "id": 2,
  "username": "student1"
}
```

**失败 (401):**
```json
{
  "error": "invalid credentials"
}
```

---

### 4.3 QuestionsHandler.java

**包名**: `com.hourai.prts.handler`

**接口**: `HttpHandler`

**路由**: `GET /questions`

#### 功能：
获取所有题目列表

#### 响应示例：

```json
[
  {
    "id": 1,
    "type": 1,
    "difficulty": 2,
    "resource": "",
    "question": "以下哪个是 Java 的关键字？",
    "hasPicture": false,
    "options": ["function", "class", "static", "define"],
    "answer": 3,
    "analysis": "static 是关键字"
  }
]
```

---

### 4.4 ExamPaperHandler.java

**包名**: `com.hourai. prts.handler`

**接口**: `HttpHandler`

**路由**: `GET /exam/paper`

#### 功能：
随机生成考试试卷

#### 请求参数：

| 参数名 | 类型 | 必填 | 默认值 | 描述 |
|--------|------|------|--------|------|
| `count` | int | 否 | 10 | 试卷题目数量 |

#### 示例请求：
```
GET /exam/paper? count=5
```

#### 响应示例：

```json
[
  {
    "id": 3,
    "type": 1,
    "difficulty": 1,
    "question": "示例题：1",
    "options": ["A", "B", "C", "D"],
    "answer": 3
  }
]
```

---

### 4.5 ExamSubmitHandler.java

**包名**: `com.hourai.prts.handler`

**接口**: `HttpHandler`

**路由**: `POST /exam/submit`

#### 功能：
提交考试答案并评分

#### 请求参数：

| 参数名 | 类型 | 必填 | 描述 |
|--------|------|------|------|
| `userId` | long | 是 | 用户 ID |
| `answers` | String | 是 | 答案字符串，格式：`questionId:selectedOption,questionId:selectedOption` |

#### 示例请求：
```
POST /exam/submit
Content-Type: application/x-www-form-urlencoded

userId=2&answers=1:3,2:2,3:1
```

#### 响应示例：

**成功 (200):**
```json
{
  "examId": 1,
  "score": 85
}
```

---

### 4.6 UserHandler.java

**包名**: `com.hourai.prts.handler`

**接口**: `HttpHandler`

**路由**: `GET /user/{id}/wrong`

#### 功能：
获取用户的错题列表

#### 示例请求：
```
GET /user/2/wrong
```

#### 响应示例：

```json
[
  {
    "id": 1,
    "type": 1,
    "difficulty": 2,
    "question": "以下哪个是 Java 的关键字？",
    "options": ["function", "class", "static", "define"],
    "answer": 3,
    "selectedOption": 1,
    "analysis": "static 是关键字"
  }
]
```

---

### 4.7 PingHandler.java

**包名**: `com.hourai.prts.handler`

**接口**: `HttpHandler`

**路由**: `GET /ping`

#### 功能：
健康检查接口

#### 响应示例：

```json
{
  "ok": true
}
```

---

## 五、工具类（Utils）

### 5. 1 Utils.java

**包名**: `com.hourai. prts.utils`

**描述**: 通用工具类，提供参数解析、JSON 生成、CSV 处理等功能

#### 静态常量：

| 常量名 | 类型 | 描述 |
|--------|------|------|
| `DT` | `DateTimeFormatter` | 日期时间格式化器 (yyyy-MM-dd HH:mm:ss) |

#### 方法列表：

##### 日期时间方法：

| 方法签名 | 返回类型 | 描述 |
|---------|---------|------|
| `now()` | `String` | 获取当前时间字符串 |

##### 参数解析方法：

| 方法签名 | 返回类型 | 描述 |
|---------|---------|------|
| `parseQuery(String q)` | `Map<String,String>` | 解析 URL 查询参数 |
| `parseForm(HttpExchange ex)` | `Map<String,String>` | 解析 POST 表单数据 |
| `parseAnswers(String s)` | `Map<Long,Integer>` | 解析答案字符串（格式：1:2,3:1） |
| `urlDecode(String s)` | `String` | URL 解码 |

##### JSON 处理方法：

| 方法签名 | 返回类型 | 描述 |
|---------|---------|------|
| `escapeJson(String s)` | `String` | 转义 JSON 特殊字符 |
| `questionsToJson(List<Question> qs)` | `String` | 将题目列表转换为 JSON 字符串 |

##### CSV 处理方法：

| 方法签名 | 返回类型 | 描述 |
|---------|---------|------|
| `csvQ(...)` | `String` | 生成题目 CSV 行 |
| `csvEscape(String s)` | `String` | CSV 字符转义 |
| `unescapeCsv(String s)` | `String` | CSV 字符反转义 |

##### HTTP 响应方法：

| 方法签名 | 返回类型 | 描述 |
|---------|---------|------|
| `send(HttpExchange ex, int code, String body)` | `void` | 发送 HTTP 响应 |

---

## 六、API 接口汇总

### 接口列表：

| 方法 | 路径 | 描述 | Handler |
|------|------|------|---------|
| POST | `/register` | 用户注册 | RegisterHandler |
| POST | `/login` | 用户登录 | LoginHandler |
| GET | `/questions` | 获取所有题目 | QuestionsHandler |
| GET | `/exam/paper? count=N` | 获取随机试卷 | ExamPaperHandler |
| POST | `/exam/submit` | 提交考试答案 | ExamSubmitHandler |
| GET | `/user/{id}/wrong` | 获取用户错题 | UserHandler |
| GET | `/ping` | 健康检查 | PingHandler |

---

## 七、使用示例

### 启动服务器：

```bash
# 编译
javac -d classes src/main/java/com/hourai/prts/**/*.java

# 运行
java -cp classes com.hourai.prts.Main
```

服务器将在 `http://localhost:8080` 启动。

### cURL 示例：

#### 1. 注册用户
```bash
curl -X POST http://localhost:8080/register \
  -d "username=testuser&password=testpass"
```

#### 2. 登录
```bash
curl -X POST http://localhost:8080/login \
  -d "username=testuser&password=testpass"
```

#### 3.  获取题目列表
```bash
curl http://localhost:8080/questions
```

#### 4. 获取试卷（5道题）
```bash
curl "http://localhost:8080/exam/paper?count=5"
```

#### 5. 提交考试
```bash
curl -X POST http://localhost:8080/exam/submit \
  -d "userId=2&answers=1:3,2:2,3:1,4:4,5:2"
```

#### 6.  查看错题
```bash
curl http://localhost:8080/user/2/wrong
```

#### 7. 健康检查
```bash
curl http://localhost:8080/ping
```

---

## 八、数据流程图

```
用户请求 → HTTP Handler → DataStore → CSV 文件
                ↓
         Utils (解析/格式化)
                ↓
            JSON 响应
```

---

## 九、注意事项

1. **安全性警告**：
   - 密码明文存储，仅供学习示例使用
   - 无认证 token 机制
   - 生产环境请勿使用

2. **数据持久化**：
   - 所有数据存储在 `./data/` 目录的 CSV 文件中
   - 无数据库依赖
   - 数据操作使用同步方法保证线程安全

3.  **依赖要求**：
   - JDK 11+ 或 17+
   - 使用 JDK 自带的 `com.sun.net.httpserver. HttpServer`
   - 无需 Maven 或 Gradle

4. **扩展性**：
   - 可通过添加新的 Handler 扩展 API
   - 可通过修改 DataStore 支持其他存储方式
   - 可通过扩展 Entity 添加新的数据模型

---

## 十、类关系图

```
Main
 ├── DataStore (初始化数据)
 └── Handlers (注册路由)
      ├── RegisterHandler
      ├── LoginHandler
      ├── QuestionsHandler
      ├── ExamPaperHandler
      ├── ExamSubmitHandler
      ├── UserHandler
      └── PingHandler
           ├── 使用 Utils (工具方法)
           ├── 使用 DataStore (数据访问)
           └── 使用 Entity (数据模型)
                ├── User
                ├── Question
                ├── UserAnswer
                └── ExamRecord
```

---

## 十一、编译打包

项目提供了 PowerShell 自动构建脚本：

### 使用 build_jpackage.ps1

```powershell
powershell -ExecutionPolicy Bypass -File .\build_jpackage.ps1
```

**脚本功能**：
1. 递归查找所有 `. java` 文件
2.  编译到 `classes/` 目录
3. 自动检测主类（包含 `public static void main`）
4. 创建可执行 JAR 文件 (`homeworkapp.jar`)
5. 使用 jpackage 生成应用程序镜像到 `dist/` 目录
6. 自动包含 `data/` 目录

---

## 十二、贡献指南

## 贡献指南 / Contributing

欢迎贡献代码！/ Contributions are welcome!

请遵循以下步骤 / Please follow these steps:

1. Fork 本仓库 / Fork this repository
2. 创建特性分支 / Create a feature branch
   ```bash
   git checkout -b feature/AmazingFeature
   ```
3. 提交更改 / Commit your changes
   ```bash
   git commit -m 'Add some AmazingFeature'
   ```
4. 推送到分支 / Push to the branch
   ```bash
   git push origin feature/AmazingFeature
   ```
5. 开启 Pull Request / Open a Pull Request

### 代码规范 / Code Standards

- **Java**: 遵循标准 Java 编码规范 / Follow standard Java coding conventions
- **JavaScript**: 使用 ES6+ 语法 / Use ES6+ syntax
- **提交信息**: 使用清晰的提交信息 / Use clear commit messages

---

## 许可证 / License

本项目仅供学习和演示使用。
This project is for educational and demonstration purposes only.

---

## 联系方式 / Contact

- **项目地址 / Repository**: [https://github.com/TeamHourai/P.R.T.S.TrainingSystem](https://github.com/TeamHourai/P.R.T.S.TrainingSystem)
- **问题反馈 / Issues**: [https://github.com/TeamHourai/P.R.T.S.TrainingSystem/issues](https://github.com/TeamHourai/P.R.T.S.TrainingSystem/issues)
- **团队 / Team**: TeamHourai (蓬莱人形)

---

## 致谢 / Acknowledgments

感谢所有为这个项目做出贡献的开发者！
Thanks to all developers who contributed to this project!

特别感谢明日方舟提供的灵感。
Special thanks to Arknights for the inspiration.

---

## 更新日志 / Changelog

### v2.0.0 (Latest)
- ✨ 新增薄弱练习功能 / Added weak point practice feature
- 🎨 优化 UI 界面 / Improved UI design
- 📝 完善文档 / Enhanced documentation

### v1.0.0
- 🎉 初始版本发布 / Initial release
- ✅ 基础功能实现 / Basic features implemented

---

**最后更新 / Last Updated**: 2025-12-24

**文档版本 / Documentation Version**: 2.0.0
