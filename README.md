# P.R. T.S.  Training System - 完整 API 文档

## 项目概述

P.R.T.S.  (Rhodes Island Training System) 是一个基于明日方舟的题库训练系统，用于问答和记录管理。

- **语言**: Java (86.4%), PowerShell (13.6%)
- **架构**: 基于 JDK 自带的 HttpServer，无外部依赖
- **数据存储**: 本地 CSV 文件
- **端口**: 8080

## 项目结构

```
src/main/java/com/hourai/prts/
├── Main.java                 # 主程序入口
├── entity/                   # 实体类
│   ├── User.java
│   ├── Question.java
│   ├── UserAnswer.java
│   └── ExamRecord.java
├── data/                     # 数据访问层
│   └── DataStore.java
├── handler/                  # HTTP 请求处理器
│   ├── RegisterHandler.java
│   ├── LoginHandler.java
│   ├── QuestionsHandler.java
│   ├── ExamPaperHandler.java
│   ├── ExamSubmitHandler.java
│   ├── UserHandler.java
│   └── PingHandler.java
└── utils/                    # 工具类
    └── Utils.java
```

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

欢迎贡献代码！请遵循以下步骤：

1. Fork 本仓库
2. 创建特性分支 (`git checkout -b feature/AmazingFeature`)
3.  提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 开启 Pull Request

---

## 十三、许可证

本项目仅供学习和演示使用。

---

## 联系方式

- 项目地址: https://github.com/TeamHourai/P. R.T.S. TrainingSystem
- 问题反馈: https://github.com/TeamHourai/P.R.T.S. TrainingSystem/issues

---

**最后更新**: 2025-12-04
