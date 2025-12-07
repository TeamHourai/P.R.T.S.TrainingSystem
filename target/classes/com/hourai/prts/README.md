

### 技术栈

#### 后端
- **框架**: Spring Boot 3.2+
- **ORM**: MyBatis-Plus 3.5+
- **数据库**: MySQL 8.0+
- **API 文档**: Knife4j (Swagger) 4.x
- **构建工具**: Maven 3. 8+
- **工具库**: Hutool 5.8+

#### 前端
- **框架**: Vue 3. x
- **构建工具**: Vite 5.x
- **UI 组件**: Element Plus 2.x
- **路由**: Vue Router 4.x
- **状态管理**: Pinia 2.x
- **HTTP**: Axios 1.x

---

## 🎯 功能规划

### ✅ 保留功能（核心）

#### 学生端
- [x] 用户登录
- [x] 题库浏览（搜索）
- [x] 单题练习（答题 + 查看解析）
- [x] 模拟考试（随机N题 + 提交评分）
- [x] 成绩查看
- [x] 简单统计（答题数、正确率）

#### 管理员端
- [x] 用户登录
- [x] 题目管理（增删改查）

### ❌ 删减功能（时间限制）

- ❌ 用户注册（管理员直接添加账号）
- ❌ 个人中心/修改资料
- ❌ 错题本
- ❌ 数据统计图表（改为文字统计）
- ❌ 富文本编辑器（纯文本即可）
- ❌ 题目分类筛选（仅保留搜索）
- ❌ 头像上传
- ❌ 考试倒计时
- ❌ 用户管理页面
- ❌ 云服务器部署（本地演示）

---

## 📅 14天开发计划

### Week 1：框架搭建 + 核心功能

| 天数 | 主要任务 | 验收标准 |
|------|---------|---------|
| **Day 1-2** | **环境搭建** | |
| | • 安装 MySQL 8.0、JDK 17、Node.js 18+ | 环境正常运行 |
| | • 创建 Spring Boot 项目 | 后端能启动 |
| | • 创建 Vue 3 项目（Vite） | 前端能访问 |
| | • 配置数据库连接 | 能连接数据库 |
| | • 创建 5 张表 + 初始化数据 | 数据库有表和初始账号 |
| **Day 3** | **用户登录** | |
| | • 后端：登录接口 `POST /user/login` | 接口能返回用户信息 |
| | • 前端：登录页面 + 路由守卫 | 能用 admin/student1 登录 |
| | • 用户信息存储（localStorage + Pinia） | 刷新页面保持登录状态 |
| **Day 4-5** | **题目管理（管理员）** | |
| | • 后端：题目 CRUD 接口 | 增删改查接口正常 |
| | • 前端：题目管理页面（表格 + 表单） | 能在页面上添加题目 |
| | • 添加、编辑、删除功能 | 管理员能维护题库 |
| **Day 6** | **题库展示（学生）** | |
| | • 后端：题目列表接口（分页 + 搜索） | 接口返回题目列表 |
| | • 前端：题库列表页（卡片展示） | 学生能看到所有题目 |
| | • 搜索功能 | 能按关键词搜索 |
| **Day 7** | **练习功能** | |
| | • 后端：答题提交接口 | 能判断对错并返回解析 |
| | • 答题记录存储 | 数据保存到数据库 |
| | • 前端：练习页面（题目 + 选项 + 提交） | 能答题并看到结果 |
| | • 答题反馈（对错 + 解析） | 完整答题流程 |

### Week 2：考试功能 + 优化上线

| 天数 | 主要任务 | 验收标准 |
|------|---------|---------|
| **Day 8-9** | **考试功能** | |
| | • 后端：生成试卷接口 | 能随机抽取N道题 |
| | • 后端：考试提交接口（批量评分） | 能计算总分并保存 |
| | • 前端：考试配置页 | 能设置题目数量 |
| | • 前端：考试页面（答题卡 + 交卷） | 能完成答题 |
| | • 前端：成绩页面 | 显示得分和答题详情 |
| **Day 10** | **简单统计** | |
| | • 后端：用户统计接口 | 返回答题数、正确率等 |
| | • 前端：统计数据展示（卡片） | 显示个人统计 |
| | • 考试记录列表 | 能查看历史考试 |
| **Day 11** | **前端优化** | |
| | • 统一页面布局（导航 + 侧边栏） | UI 统一美观 |
| | • UI 美化（颜色、间距、图标） | 视觉效果良好 |
| | • Loading 效果 + 错误提示 | 用户体验优化 |
| | • 简单响应式适配 | 手机端能基本使用 |
| **Day 12** | **后端优化** | |
| | • 统一异常处理 | 错误信息规范 |
| | • 统一响应格式 Result<T> | 接口格式一致 |
| | • 添加日志 | 关键操作有日志 |
| | • 参数校验 | 输入验证完善 |
| | • Knife4j 接口文档 | API 文档完整 |
| **Day 13** | **联调测试** | |
| | • 全流程测试 | 无阻断性 Bug |
| | • 边界测试 | 异常情况处理正常 |
| | • Bug 修复 | 已知问题全部解决 |
| | • 准备演示数据 | 数据库有 50 道题 |
| **Day 14** | **准备答辩** | |
| | • 编写项目文档 | README 完整 |
| | • 录制演示视频 | 5 分钟演示视频 |
| | • 准备 PPT | 答辩 PPT 完成 |
| | • 本地部署测试 | 能完整演示 |

---

## 👥 四人小组分工

### 团队角色

| 成员 | 角色 | 主要职责 |
|------|------|---------|
| **成员 A** | 后端负责人 | Spring Boot 架构、核心接口开发 |
| **成员 B** | 后端开发 | 数据库设计、业务接口开发 |
| **成员 C** | 前端负责人 | Vue 架构、核心页面开发 |
| **成员 D** | 前端开发 | UI 组件、功能页面开发 |

---

### Week 1 详细分工

#### Day 1-2：环境搭建

| 成员 A（后端负责人） | 成员 B（后端开发） | 成员 C（前端负责人） | 成员 D（前端开发） |
|-------------------|------------------|-------------------|------------------|
| 搭建 Spring Boot 项目 | 设计数据库表结构 | 搭建 Vue 3 项目 | 安装 Element Plus |
| 配置 application.yml | 编写建表 SQL 脚本 | 配置 Vite | 配置 Vue Router |
| 集成 MyBatis-Plus | 执行 SQL 初始化数据库 | 创建基础布局 | 安装 Axios + Pinia |
| 集成 Knife4j | 准备测试账号 | 配置路由结构 | 封装 HTTP 请求 |

**验收**: 后端能启动，前端能访问，数据库有表

---

#### Day 3：用户登录

| 成员 A（后端负责人） | 成员 B（后端开发） | 成员 C（前端负责人） | 成员 D（前端开发） |
|-------------------|------------------|-------------------|------------------|
| 创建 User 实体类 | 协助 A 编写 Mapper | 创建登录页面 | 完善 Axios 封装 |
| 编写 UserService | 准备测试数据 | 表单验证 | 请求/响应拦截器 |
| 开发登录接口 | 用 Postman 测试接口 | 登录成功跳转 | 用户信息存储 |
| 统一响应格式 Result | 在数据库添加学生账号 | 配置路由守卫 | Pinia 状态管理 |

**验收**: 能用 admin/student1 登录，未登录自动跳转

---

#### Day 4-5：题目管理

| 成员 A（后端负责人） | 成员 B（后端开发） | 成员 C（前端负责人） | 成员 D（前端开发） |
|-------------------|------------------|-------------------|------------------|
| Question 实体类 | 分页查询接口 | 题目管理页面布局 | 题目表单组件 |
| 题目增加接口 | 搜索功能实现 | 题目列表表格 | 表单验证 |
| 题目修改接口 | 测试所有接口 | 添加按钮 + 弹窗 | 动态选项输入 |
| 题目删除接口 | 编写接口文档 | 编辑/删除功能 | 选项增删功能 |

**验收**: 管理员能在页面上添加/修改/删除题目，数据库有 20 条测试题

---

#### Day 6：题库展示

| 成员 A（后端负责人） | 成员 B（后端开发） | 成员 C（前端负责人） | 成员 D（前端开发） |
|-------------------|------------------|-------------------|------------------|
| 优化题目列表接口 | 添加 20 条测试题目 | 题库列表页面 | 题目卡片组件 |
| 添加搜索参数 | 测试搜索功能 | 卡片式布局 | 搜索框组件 |
| 返回数据优化 | 数据格式验证 | 分页组件 | 题目类型/难度标签 |
| 接口文档更新 | 准备多类型题目 | 页面美化 | 响应式适配 |

**验收**: 学生能看到所有题目，能搜索

---

#### Day 7：练习功能

| 成员 A（后端负责人） | 成员 B（后端开发） | 成员 C（前端负责人） | 成员 D（前端开发） |
|-------------------|------------------|-------------------|------------------|
| 答题提交接口 | UserAnswer 实体类 | 练习页面布局 | 答题组件（单选/多选） |
| 判断答案逻辑 | 答题记录存储 | 题目展示区域 | 提交按钮 |
| 返回正确答案和解析 | 测试答题流程 | 答题反馈展示 | 对错提示（✅/❌） |
| 接口优化 | 数据验证 | 下一题按钮 | 解析展示区域 |

**验收**: 能答题并立即看到对错和解析

---

### Week 2 详细分工

#### Day 8-9：考试功能

| 成员 A（后端负责人） | 成员 B（后端开发） | 成员 C（前端负责人） | 成员 D（前端开发） |
|-------------------|------------------|-------------------|------------------|
| 生成试卷接口 | ExamRecord 实体类 | 考试配置页 | 考试答题页面 |
| 随机抽题逻辑 | 考试提交接口 | 开始考试按钮 | 题目列表展示 |
| 试卷返回优化 | 批量评分逻辑 | 答题进度显示 | 答题卡组件 |
| 接口测试 | ExamDetail 存储 | 交卷确认弹窗 | 选项选择交互 |
| | 考试记录查询接口 | 成绩页面 | 答题详情展示 |
| | 测试评分准确性 | 得分展示 | 对错统计 |

**验收**: 能完成完整考试流程（配置→答题→交卷→查看成绩）

---

#### Day 10：简单统计

| 成员 A（后端负责人） | 成员 B（后端开发） | 成员 C（前端负责人） | 成员 D（前端开发） |
|-------------------|------------------|-------------------|------------------|
| 用户统计接口 | 考试历史查询接口 | 统计页面布局 | 统计卡片组件 |
| 计算总答题数 | 答题记录统计 | 数据展示（卡片） | 考试记录表格 |
| 计算正确率 | SQL 优化 | 页面美化 | 详情查看功能 |
| 最高分查询 | 接口测试 | 响应式适配 | 数据格式化 |

**验收**: 能看到个人统计数据和考试历史

---

#### Day 11：前端优化

| 成员 A（后端负责人） | 成员 B（后端开发） | 成员 C（前端负责人） | 成员 D（前端开发） |
|-------------------|------------------|-------------------|------------------|
| 协助测试接口 | 协助测试接口 | 统一页面布局 | 移动端适配 |
| 性能优化建议 | 性能优化建议 | 顶部导航栏 | Loading 组件 |
| | | 侧边栏菜单 | 错误提示优化 |
| | | 整体 UI 美化 | 图标添加 |
| | | 颜色主题统一 | 动画效果 |

**验收**: UI 美观统一，移动端基本可用

---

#### Day 12：后端优化

| 成员 A（后端负责人） | 成员 B（后端开发） | 成员 C（前端负责人） | 成员 D（前端开发） |
|-------------------|------------------|-------------------|------------------|
| 统一异常处理 | 参数校验 @Valid | 协助后端测试 | 协助后端测试 |
| @ControllerAdvice | 接口文档完善 | 前端错误处理 | 前端优化 |
| 添加日志 Logback | Knife4j 注解 | | |
| 统一响应格式检查 | 接口测试 | | |

**验收**: 异常处理规范，API 文档完整

---

#### Day 13：联调测试

| 全员任务 |
|---------|
| • 全流程测试（登录→练习→考试→查看成绩） |
| • 边界测试（空数据、错误输入、网络异常） |
| • 性能测试（大量数据、并发请求） |
| • Bug 修复（按优先级处理） |
| • 准备演示数据（管理员添加 50 道完整题目） |
| • 代码整理（删除无用代码、统一格式） |

**验收**: 无阻断性 Bug，能流畅演示

---

#### Day 14：准备答辩

| 成员 A | 成员 B | 成员 C | 成员 D |
|--------|--------|--------|--------|
| 编写后端文档 | 整理接口文档 | 录制演示视频 | 制作答辩 PPT |
| 代码注释完善 | 数据库文档 | 视频剪辑 | PPT 美化 |
| 部署说明 | 环境配置文档 | 准备演示环境 | 演讲稿准备 |
| 答辩准备 | 答辩准备 | 答辩准备 | 答辩彩排 |

**验收**: 文档齐全，演示视频完成，PPT 完成

---

## 📁 项目结构

### 后端结构（精简版）

```
prts-backend/
├── pom. xml
└── src/main/
    ├── java/com/hourai/prts/
    │   ├── PrtsApplication.java          # 启动类
    │   ├── controller/                    # 控制器
    │   │   ├── UserController.java       # 用户登录
    │   │   ├── QuestionController.java   # 题目 CRUD
    │   │   ├── AnswerController.java     # 答题提交
    │   │   └── ExamController.java       # 考试
    │   ├── service/                       # 业务逻辑
    │   │   ├── IUserService.java
    │   │   ├── IQuestionService.java
    │   │   ├── IAnswerService.java
    │   │   ├── IExamService.java
    │   │   └── impl/
    │   ├── mapper/                        # 数据访问
    │   │   ├── UserMapper.java
    │   │   ├── QuestionMapper. java
    │   │   ├── UserAnswerMapper.java
    │   │   └── ExamRecordMapper.java
    │   ├── entity/                        # 实体类
    │   │   ├── User.java
    │   │   ├── Question.java
    │   │   ├── UserAnswer.java
    │   │   ├── ExamRecord.java
    │   │   └── ExamDetail.java
    │   ├── dto/                           # 数据传输对象
    │   │   ├── LoginDTO.java
    │   │   └── ExamSubmitDTO.java
    │   ├── vo/                            # 视图对象
    │   │   ├── UserVO.java
    │   │   └── ExamResultVO.java
    │   ├── common/                        # 公共类
    │   │   ├── Result.java               # 统一响应
    │   │   └── PageResult.java           # 分页响应
    │   └── config/                        # 配置类
    │       ├── MyBatisPlusConfig.java
    │       ├── CorsConfig.java
    │       └── Knife4jConfig.java
    └── resources/
        ├── application.yml
        └── db/
            ├── schema.sql                 # 建表脚本
            └── data. sql                   # 初始数据
```

---

### 前端结构（精简版）

```
prts-frontend/
├── package.json
├── vite. config.js
├── index.html
└── src/
    ├── main. js                           # 入口文件
    ├── App.vue
    ├── views/                            # 页面
    │   ├── Login. vue                     # 登录页
    │   ├── QuestionManage.vue            # 题目管理（管理员）
    │   ├── QuestionList.vue              # 题库列表（学生）
    │   ├── Practice.vue                  # 练习页面
    │   ├── Exam.vue                      # 考试页面
    │   ├── ExamResult.vue                # 成绩页面
    │   └── Statistics.vue                # 统计页面
    ├── components/                       # 组件
    │   ├── QuestionCard.vue              # 题目卡片
    │   ├── AnswerSheet.vue               # 答题卡
    │   └── Layout.vue                    # 布局组件
    ├── router/                           # 路由
    │   └── index.js
    ├── store/                            # 状态管理
    │   ├── index.js
    │   └── modules/
    │       └── user.js
    ├── api/                              # API 接口
    │   ├── user.js
    │   ├── question.js
    │   ├── answer.js
    │   └── exam.js
    ├── utils/                            # 工具函数
    │   ├── request.js                    # Axios 封装
    │   └── storage.js                    # localStorage 封装
    └── assets/                           # 静态资源
        └── styles/
            └── main.css
```

---

## 🗄️ 数据库设计

### 表结构（5张表）

```sql
-- 1. 用户表
CREATE TABLE `user` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `username` VARCHAR(50) UNIQUE NOT NULL,
  `password` VARCHAR(255) NOT NULL COMMENT '明文密码',
  `nickname` VARCHAR(50),
  `is_admin` TINYINT(1) DEFAULT 0 COMMENT '0-学生 1-管理员',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 2. 题目表
CREATE TABLE `question` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `type` TINYINT NOT NULL COMMENT '1-单选 2-多选',
  `difficulty` TINYINT NOT NULL COMMENT '1-简单 2-中等 3-困难',
  `question` TEXT NOT NULL COMMENT '题目内容',
  `options` JSON NOT NULL COMMENT '选项数组',
  `answer` VARCHAR(50) NOT NULL COMMENT '正确答案',
  `analysis` TEXT COMMENT '题目解析',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_type (`type`),
  INDEX idx_difficulty (`difficulty`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='题目表';

-- 3. 答题记录表
CREATE TABLE `user_answer` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL,
  `question_id` BIGINT NOT NULL,
  `selected_answer` VARCHAR(50),
  `is_correct` TINYINT(1),
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_user_id (`user_id`),
  INDEX idx_question_id (`question_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='答题记录表';

-- 4. 考试记录表
CREATE TABLE `exam_record` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL,
  `total_questions` INT NOT NULL COMMENT '总题数',
  `correct_count` INT NOT NULL COMMENT '正确题数',
  `score` DECIMAL(5,2) NOT NULL COMMENT '得分',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_user_id (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='考试记录表';

-- 5. 考试详情表
CREATE TABLE `exam_detail` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `exam_id` BIGINT NOT NULL,
  `question_id` BIGINT NOT NULL,
  `selected_answer` VARCHAR(50),
  `is_correct` TINYINT(1),
  INDEX idx_exam_id (`exam_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='考试详情表';

-- 初始化数据
INSERT INTO `user` (username, password, nickname, is_admin) VALUES 
('admin', 'admin123', '系统管理员', 1),
('student1', '123456', '学生1号', 0),
('student2', '123456', '学生2号', 0);
```

---

## 🔧 关键配置

### 后端配置（application.yml）

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
  configuration:
    log-impl: org.apache.ibatis.logging.stdout.StdOutImpl
  global-config:
    db-config:
      id-type: auto
```

---

### 前端配置（package.json）

```json
{
  "name": "prts-frontend",
  "version": "1.0.0",
  "scripts": {
    "dev": "vite",
    "build": "vite build",
    "preview": "vite preview"
  },
  "dependencies": {
    "vue": "^3.4.0",
    "vue-router": "^4.2.5",
    "pinia": "^2.1. 7",
    "element-plus": "^2.5.0",
    "axios": "^1.6.0",
    "@element-plus/icons-vue": "^2.3.1"
  },
  "devDependencies": {
    "@vitejs/plugin-vue": "^5.0.0",
    "vite": "^5.0.0"
  }
}
```

---

### 后端 pom.xml 关键依赖

```xml
<dependencies>
    <!-- Spring Boot Web -->
    <dependency>
        <groupId>org.springframework. boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    
    <!-- MyBatis-Plus -->
    <dependency>
        <groupId>com.baomidou</groupId>
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
    
    <!-- Hutool -->
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

---

## 🚀 快速开始

### 环境要求
- JDK 17+
- Node.js 18+
- MySQL 8.0+
- Maven 3.8+

### 后端启动

```bash
# 1. 克隆项目
git clone <repository-url>
cd prts-backend

# 2. 创建数据库
mysql -u root -p
CREATE DATABASE prts_db DEFAULT CHARSET utf8mb4;

# 3. 执行 SQL 脚本
mysql -u root -p prts_db < src/main/resources/db/schema.sql
mysql -u root -p prts_db < src/main/resources/db/data.sql

# 4. 修改配置
# 编辑 src/main/resources/application.yml，修改数据库密码

# 5. 启动项目
mvn spring-boot:run

# 访问: http://localhost:8888
# API 文档: http://localhost:8888/doc.html
```

---

### 前端启动

```bash
# 1. 进入前端目录
cd prts-frontend

# 2.  安装依赖
npm install

# 3. 启动开发服务器
npm run dev

# 访问: http://localhost:5173
```

---

### 默认账号

| 用户名 | 密码 | 角色 |
|--------|------|------|
| admin | admin123 | 管理员 |
| student1 | 123456 | 学生 |
| student2 | 123456 | 学生 |

---

## 📊 API 接口汇总

### 用户相关
| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/user/login` | 用户登录 |

### 题目相关
| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/question/list` | 分页查询题目 |
| POST | `/question` | 添加题目 |
| PUT | `/question` | 修改题目 |
| DELETE | `/question/{id}` | 删除题目 |

### 答题相关
| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/answer/submit` | 提交单题答案 |

### 考试相关
| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/exam/paper? count=10` | 生成试卷 |
| POST | `/exam/submit` | 提交考试 |
| GET | `/exam/record/{userId}` | 考试历史 |
| GET | `/exam/detail/{examId}` | 考试详情 |

### 统计相关
| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/user/{id}/stats` | 用户统计 |

