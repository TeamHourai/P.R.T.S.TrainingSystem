```markdown
# 极简本地作业系统（多文件、无依赖）

这是一个极简、阉割、仅用于学习/作业演示的作业系统实现，分成多个 Java 源文件，依赖 JDK 自带的 HttpServer（com.sun.net.httpserver）。

特点
- 无外部库、无需 Maven/Gradle
- 数据保存在本地 data/ 目录的 CSV 文件中
- 提供简单的 REST 风格接口（JSON 响应，手工构造）
- 极简权限：无认证（仅用户名/密码匹配登录），明文密码，仅示例用途

要求
- JDK 11+ 或 17+

编译与运行
1. 将所有 .java 文件放在同一目录（例如 project/）。
2. 运行：
   javac *.java
   java Main
3. 访问服务：http://localhost:8080

主要 API
- POST /register
  - body: application/x-www-form-urlencoded
  - params: username, password
  - 返回: { "id":..., "username":"..." }

- POST /login
  - body: username, password
  - 返回: { "id":..., "username":"..." } or { "error":... }

- GET /questions
  - 返回题目列表（包括 options，answer 也会返回）

- GET /exam/paper?count=10
  - 随机抽取 count 道题（默认 10）

- POST /exam/submit
  - body: userId=...&answers=1:2,3:1
    (answers 格式 questionId:selectedOption，多个用逗号分隔)
  - 返回: { "examId":..., "score":... }

- GET /user/{id}/wrong
  - 返回该用户错题列表

示例 (curl)
- 注册：curl -X POST -d "username=stu&password=123" http://localhost:8080/register
- 登录：curl -X POST -d "username=stu&password=123" http://localhost:8080/login
- 题库：curl http://localhost:8080/questions
- 生成卷：curl "http://localhost:8080/exam/paper?count=5"
- 提交：curl -X POST -d "userId=2&answers=1:3,2:2" http://localhost:8080/exam/submit
- 查错题：curl http://localhost:8080/user/2/wrong

注意与限制
- 明文密码、无认证、输入几乎不校验 —— 仅作教学/作业用途。
- JSON 构造非常简陋，不保证对复杂字符完全鲁棒。
- 可以按需扩展：把存储改为 JSON 文件或 SQLite、改用更好的 JSON 库或加入简单前端。

想要的下一步
- 如果你希望我把“答案”字段从题目列表中隐藏（只返回给管理员或考试评分时使用），我可以修改。
- 或者把数据文件改为 JSON 格式、或把服务端改为控制台交互、或生成一个单文件 HTML 前端（fetch）——告诉我你的选择，我会给出修改。
```