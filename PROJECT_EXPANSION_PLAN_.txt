# P.R.T.S. 训练系统扩展计划

## 项目目标
将现有的极简 Java HTTP 服务扩展为一个功能完整的前后端分离 Web 应用系统。

---

## 技术栈选型

### 后端技术栈
| 组件 | 技术选型 | 版本 | 说明 |
|------|---------|------|------|
| **框架** | Spring Boot | 3.2+ | 主流 Java Web 框架，简化开发 |
| **Web 层** | Spring MVC | - | RESTful API 开发 |
| **数据库** | MySQL | 8. 0+ | 关系型数据库 |
| **ORM** | MyBatis-Plus | 3.5+ | 简化数据库操作，提供代码生成 |
| **连接池** | HikariCP | - | Spring Boot 默认，高性能 |
| **API 文档** | Knife4j (Swagger) | 4.x | 自动生成 API 文档和测试界面 |
| **JSON** | Jackson | - | Spring Boot 默认 |
| **工具类** | Hutool | 5.8+ | Java 工具类库 |
| **构建工具** | Maven | 3.8+ | 依赖管理和构建 |

### 前端技术栈
| 组件 | 技术选型 | 版本 | 说明 |
|------|---------|------|------|
| **框架** | Vue. js | 3.x | 渐进式前端框架 |
| **构建工具** | Vite | 5.x | 快速的前端构建工具 |
| **UI 组件库** | Element Plus | 2.x | 成熟的 Vue 3 UI 组件库 |
| **路由** | Vue Router | 4.x | 官方路由管理 |
| **状态管理** | Pinia | 2.x | Vue 3 官方推荐状态管理 |
| **HTTP 客户端** | Axios | 1.x | HTTP 请求库 |
| **Markdown 渲染** | Marked. js | - | 题目解析展示 |

### 开发工具
- **IDE**: IntelliJ IDEA (后端) + VS Code (前端)
- **API 测试**: Postman / Apifox
- **版本控制**: Git
- **数据库工具**: MySQL Workbench / DataGrip

---

## 数据库设计

### 表结构设计

```sql
-- 用户表
CREATE TABLE `user` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `username` VARCHAR(50) UNIQUE NOT NULL,
  `password` VARCHAR(255) NOT NULL COMMENT '明文密码',
  `nickname` VARCHAR(50),
  `avatar` VARCHAR(255),
  `email` VARCHAR(100),
  `is_admin` TINYINT(1) DEFAULT 0,
  `status` TINYINT(1) DEFAULT 1 COMMENT '0-禁用 1-启用',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 题目表
CREATE TABLE `question` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `type` TINYINT NOT NULL COMMENT '1-单选 2-多选 3-判断 4-填空',
  `difficulty` TINYINT NOT NULL COMMENT '1-简单 2-中等 3-困难',
  `category` VARCHAR(50) COMMENT '题目分类',
  `resource` VARCHAR(255) COMMENT '资源来源',
  `question` TEXT NOT NULL COMMENT '题目内容',
  `options` JSON COMMENT '选项数组',
  `answer` VARCHAR(255) NOT NULL COMMENT '正确答案',
  `analysis` TEXT COMMENT '题目解析',
  `has_picture` TINYINT(1) DEFAULT 0,
  `picture_url` VARCHAR(255),
  `view_count` INT DEFAULT 0,
  `error_count` INT DEFAULT 0,
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX idx_type (`type`),
  INDEX idx_difficulty (`difficulty`),
  INDEX idx_category (`category`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 答题记录表
CREATE TABLE `user_answer` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL,
  `question_id` BIGINT NOT NULL,
  `selected_answer` VARCHAR(255) NOT NULL,
  `is_correct` TINYINT(1) NOT NULL,
  `answer_time` INT COMMENT '答题用时（秒）',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_user_id (`user_id`),
  INDEX idx_question_id (`question_id`),
  INDEX idx_user_question (`user_id`, `question_id`),
  FOREIGN KEY (`user_id`) REFERENCES `user`(`id`) ON DELETE CASCADE,
  FOREIGN KEY (`question_id`) REFERENCES `question`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 考试记录表
CREATE TABLE `exam_record` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL,
  `exam_name` VARCHAR(100),
  `total_questions` INT NOT NULL,
  `correct_count` INT NOT NULL,
  `score` DECIMAL(5,2) NOT NULL,
  `duration` INT COMMENT '考试用时（秒）',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_user_id (`user_id`),
  INDEX idx_created_at (`created_at`),
  FOREIGN KEY (`user_id`) REFERENCES `user`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 考试详情表
CREATE TABLE `exam_detail` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `exam_id` BIGINT NOT NULL,
  `question_id` BIGINT NOT NULL,
  `selected_answer` VARCHAR(255),
  `is_correct` TINYINT(1),
  FOREIGN KEY (`exam_id`) REFERENCES `exam_record`(`id`) ON DELETE CASCADE,
  FOREIGN KEY (`question_id`) REFERENCES `question`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

---

## 项目结构规划

### 后端项目结构
```
prts-backend/
├── pom. xml
├── src/main/java/com/hourai/prts/
│   ├── PrtsApplication.java          # 启动类
│   ├── controller/                    # 控制器层
│   │   ├── UserController.java
│   │   ├── QuestionController.java
│   │   ├── ExamController. java
│   │   └── StatisticsController.java
│   ├── service/                       # 业务逻辑层
│   │   ├── IUserService.java
│   │   ├── impl/
│   │   │   └── UserServiceImpl.java
│   │   ├── IQuestionService.java
│   │   └── IExamService.java
│   ├── mapper/                        # 数据访问层
│   │   ├── UserMapper.java
│   │   ├── QuestionMapper.java
│   │   └── ExamRecordMapper.java
│   ├── entity/                        # 实体类
│   │   ├── User.java
│   │   ├── Question.java
│   │   ├── UserAnswer.java
│   │   └── ExamRecord.java
│   ├── dto/                           # 数据传输对象
│   │   ├── LoginDTO.java
│   │   ├── ExamSubmitDTO.java
│   │   └── QuestionQueryDTO.java
│   ├── vo/                            # 视图对象
│   │   ├── UserVO.java
│   │   ├── QuestionVO.java
│   │   └── ExamResultVO.java
│   ├── common/                        # 公共模块
│   │   ├── Result.java               # 统一响应
│   │   ├── PageResult.java           # 分页响应
│   │   └── ErrorCode.java            # 错误码
│   ├── config/                        # 配置类
│   │   ├── MyBatisPlusConfig.java
│   │   ├── CorsConfig.java
│   │   └── Knife4jConfig.java
│   └── utils/                         # 工具类
│       └── DataMigrationUtil.java    # CSV 迁移工具
└── src/main/resources/
    ├── application.yml
    ├── application-dev.yml
    ├── mapper/                        # MyBatis XML
    └── db/migration/                  # 数据库脚本
        └── V1__init. sql
```

### 前端项目结构
```
prts-frontend/
├── package.json
├── vite.config.js
├── index.html
├── src/
│   ├── main.js                       # 入口文件
│   ├── App.vue
│   ├── views/                        # 页面组件
│   │   ├── Login.vue
│   │   ├── Register.vue
│   │   ├── Home.vue
│   │   ├── QuestionBank.vue         # 题库
│   │   ├── Practice.vue             # 练习模式
│   │   ├── Exam.vue                 # 考试模式
│   │   ├── WrongQuestions.vue       # 错题本
│   │   ├── Statistics.vue           # 统计分析
│   │   └── Admin/                   # 管理后台
│   │       ├── QuestionManage.vue
│   │       └── UserManage.vue
│   ├── components/                   # 公共组件
│   │   ├── QuestionCard.vue
│   │   ├── AnswerSheet.vue
│   │   └── Chart.vue
│   ├── router/                       # 路由配置
│   │   └── index.js
│   ├── store/                        # 状态管理
│   │   ├── index.js
│   │   └── modules/
│   │       ├── user.js
│   │       └── exam.js
│   ├── api/                          # API 接口
│   │   ├── user.js
│   │   ├── question.js
│   │   └── exam.js
│   ├── utils/                        # 工具函数
│   │   ├── request.js               # axios 封装
│   │   └── storage.js               # localStorage 封装
│   └── assets/                       # 静态资源
│       ├── styles/
│       └── images/
```

---

## 实施步骤与时间规划

### 阶段一：环境搭建与基础架构（1 周）

**时间**: 第 1-7 天

#### Day 1-2: 环境准备
- [ ] 安装 MySQL 8.0，创建数据库 `prts_db`
- [ ] 安装 Node.js 18+ 和 npm/pnpm
- [ ] 配置 IntelliJ IDEA 和 VS Code
- [ ] 创建 Git 仓库，建立 `backend` 和 `frontend` 分支

#### Day 3-4: 后端框架搭建
- [ ] 使用 Spring Initializr 创建 Spring Boot 项目
  - 依赖：Web, MyBatis-Plus, MySQL Driver, Lombok
- [ ] 配置 `application.yml`（数据库连接、端口 8888）
- [ ] 集成 MyBatis-Plus，配置分页插件
- [ ] 集成 Knife4j，访问 `http://localhost:8888/doc. html`
- [ ] 创建统一响应类 `Result<T>`

#### Day 5-6: 前端框架搭建
- [ ] 使用 Vite 创建 Vue 3 项目：`npm create vite@latest`
- [ ] 安装 Element Plus：`npm install element-plus`
- [ ] 配置 Vue Router 和 Pinia
- [ ] 封装 Axios（请求/响应拦截器）
- [ ] 创建基础布局（顶部导航、侧边栏）

#### Day 7: 数据迁移
- [ ] 执行数据库初始化脚本
- [ ] 编写 CSV 导入工具类，将现有 CSV 数据导入 MySQL
- [ ] 验证数据迁移完整性

---

### 阶段二：核心功能开发（2 周）

**时间**: 第 8-21 天

#### Week 2: 用户模块 + 题库模块

**Day 8-10: 用户模块**
- [ ] **后端**:
  - 实体类、Mapper、Service、Controller
  - API: 注册、登录、获取用户信息
  - 用户信息修改（昵称、头像）
- [ ] **前端**:
  - 登录页面（表单验证）
  - 注册页面
  - 用户信息存储（Pinia + localStorage）
  - 路由守卫（登录拦截）

**Day 11-14: 题库模块**
- [ ] **后端**:
  - Question CRUD 接口
  - 分页查询（支持类型、难度、分类筛选）
  - 随机抽题接口（考试用）
- [ ] **前端**:
  - 题库列表页（表格 + 分页）
  - 题目详情展示（Markdown 渲染）
  - 筛选器组件（类型、难度）
  - 管理员题目增删改（表单 + 富文本）

#### Week 3: 练习模块 + 考试模块

**Day 15-17: 练习模块**
- [ ] **后端**:
  - 提交答案接口（单题练习）
  - 记录答题历史
  - 获取错题列表接口
- [ ] **前端**:
  - 题目练习页面（题卡 + 选项）
  - 答题反馈（对错提示 + 解析）
  - 错题本页面（列表 + 重做）

**Day 18-21: 考试模块**
- [ ] **后端**:
  - 生成试卷接口（随机 N 题）
  - 提交试卷接口（批量评分）
  - 考试记录查询接口
  - 考试详情（答题情况）
- [ ] **前端**:
  - 考试配置页（题目数量、时长）
  - 考试页面（倒计时、答题卡）
  - 交卷确认（二次确认弹窗）
  - 成绩报告页（分数、正确率、用时）

---

### 阶段三：数据统计与优化（1 周）

**时间**: 第 22-28 天

#### Day 22-24: 统计分析
- [ ] **后端**:
  - 用户统计接口（答题数、正确率、考试次数）
  - 题目统计接口（正确率排行、高频错题）
  - 趋势分析接口（每日答题量）
- [ ] **前端**:
  - 个人中心页面（统计卡片）
  - 使用 ECharts 绘制图表（折线图、饼图、柱状图）
  - 错题分析（按难度、类型分布）

#### Day 25-26: 管理后台
- [ ] **前端**:
  - 用户管理页面（列表、禁用/启用）
  - 题目管理页面（批量导入、导出）
  - 数据概览仪表盘

#### Day 27-28: 优化与测试
- [ ] 前端路由懒加载
- [ ] 后端接口性能测试（JMeter）
- [ ] 添加日志（Logback）
- [ ] 异常统一处理（@ControllerAdvice）
- [ ] 前端错误处理（全局错误提示）

---

### 阶段四：部署与上线（3 天）

**时间**: 第 29-31 天

#### Day 29: 项目打包
- [ ] 后端打包：`mvn clean package`（生成 JAR）
- [ ] 前端打包：`npm run build`（生成 dist 目录）
- [ ] 编写 Dockerfile（可选）

#### Day 30: 服务器部署
- [ ] 云服务器准备（阿里云/腾讯云）
- [ ] 安装 JDK 17、MySQL、Nginx
- [ ] 上传后端 JAR，使用 nohup 或 systemd 运行
- [ ] Nginx 配置静态文件代理 + API 反向代理
- [ ] 配置域名和 HTTPS（Let's Encrypt）

#### Day 31: 测试与文档
- [ ] 全流程功能测试
- [ ] 编写部署文档
- [ ] 更新 README.md
- [ ] 录制演示视频

---

## 功能清单

### 学生端功能
- [x] 用户注册/登录
- [x] 题库浏览（分类、筛选）
- [x] 单题练习模式
- [x] 模拟考试模式
- [x] 错题本（重做、收藏）
- [x] 个人统计（答题记录、正确率）
- [x] 成绩报告查看

### 管理员功能
- [x] 题目管理（增删改查）
- [x] 用户管理（查看、禁用）
- [x] 题目批量导入（Excel/CSV）
- [x] 数据统计仪表盘

---

## 依赖清单

### 后端 pom.xml 关键依赖

```xml
<dependencies>
    <!-- Spring Boot Web -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    
    <!-- MyBatis-Plus -->
    <dependency>
        <groupId>com. baomidou</groupId>
        <artifactId>mybatis-plus-boot-starter</artifactId>
        <version>3.5.5</version>
    </dependency>
    
    <!-- MySQL Driver -->
    <dependency>
        <groupId>com.mysql</groupId>
        <artifactId>mysql-connector-j</artifactId>
    </dependency>
    
    <!-- Knife4j (Swagger UI) -->
    <dependency>
        <groupId>com.github.xiaoymin</groupId>
        <artifactId>knife4j-openapi3-jakarta-spring-boot-starter</artifactId>
        <version>4.3.0</version>
    </dependency>
    
    <!-- Hutool 工具类 -->
    <dependency>
        <groupId>cn.hutool</groupId>
        <artifactId>hutool-all</artifactId>
        <version>5.8.25</version>
    </dependency>
    
    <!-- Lombok -->
    <dependency>
        <groupId>org.projectlombok</groupId>
        <artifactId>lombok</artifactId>
    </dependency>
</dependencies>
```

### 前端 package.json 关键依赖

```json
{
  "dependencies": {
    "vue": "^3.4.0",
    "vue-router": "^4.2.5",
    "pinia": "^2.1. 7",
    "element-plus": "^2.5.0",
    "axios": "^1.6.0",
    "@element-plus/icons-vue": "^2.3.1",
    "echarts": "^5.4. 3",
    "marked": "^11.0.0"
  },
  "devDependencies": {
    "@vitejs/plugin-vue": "^5.0.0",
    "vite": "^5.0.0"
  }
}
```

---

## 关键配置文件

### application.yml

```yaml
spring:
  application:
    name: prts-backend
  datasource:
    driver-class-name: com. mysql.cj.jdbc.Driver
    url: jdbc:mysql://localhost:3306/prts_db?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai
    username: root
    password: your_password
  jackson:
    date-format: yyyy-MM-dd HH:mm:ss
    time-zone: GMT+8

server:
  port: 8888

mybatis-plus:
  mapper-locations: classpath:mapper/**/*.xml
  configuration:
    log-impl: org.apache. ibatis.logging.stdout.StdOutImpl
  global-config:
    db-config:
      id-type: auto
      logic-delete-field: deleted
```

### Nginx 配置示例

```nginx
server {
    listen 80;
    server_name your-domain.com;
    
    # 前端静态文件
    location / {
        root /var/www/prts-frontend/dist;
        try_files $uri $uri/ /index.html;
    }
    
    # 后端 API 代理
    location /api/ {
        proxy_pass http://localhost:8888/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }
}
```

---

## 总时间估算

| 阶段 | 内容 | 时间 |
|------|------|------|
| 阶段一 | 环境搭建 + 基础架构 | 7 天 |
| 阶段二 | 核心功能开发 | 14 天 |
| 阶段三 | 数据统计 + 优化 | 7 天 |
| 阶段四 | 部署上线 | 3 天 |
| **总计** | - | **31 天（约 1 个月）** |

---

## 学习资源

### 官方文档
- Spring Boot: https://spring.io/projects/spring-boot
- MyBatis-Plus: https://baomidou.com/
- Vue 3: https://vuejs. org/
- Element Plus: https://element-plus.org/
- Vite: https://vitejs.dev/

### 推荐教程
- 黑马程序员《瑞吉外卖》（Spring Boot + MyBatis-Plus）
- 尚硅谷《Vue3 全家桶》
- B 站搜索"Spring Boot + Vue 前后端分离"

---

## 注意事项

1. **密码保留明文**：本项目为学习目的，密码仍使用明文存储（生产环境请使用 BCrypt 加密）
2. **CORS 配置**：后端需配置跨域支持
3. **数据备份**：定期备份 MySQL 数据库
4.  **版本控制**：频繁提交代码，使用有意义的 commit message
5. **循序渐进**：建议先完成最小可用版本（MVP），再逐步添加功能

---

**祝开发顺利！** 🚀