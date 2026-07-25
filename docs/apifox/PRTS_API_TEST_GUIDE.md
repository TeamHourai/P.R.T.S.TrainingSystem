# PRTS 接口测试指南（Apifox）

本指南对应当前后端接口，目标是建立一套可重复执行的鉴权、权限、考试与通知回归测试。

## 1. 准备环境

1. 启动后端，确认 `GET http://127.0.0.1:8080/api/v1/ping` 可访问。
2. 在 Apifox 新建环境“PRTS 本地”，设置前置 URL 为：

   ```text
   http://127.0.0.1:8080/api/v1
   ```

3. 建立以下环境变量：

   | 变量 | 用途 |
   | --- | --- |
   | `normalUsername` | 普通测试用户 |
   | `normalPassword` | 普通测试用户密码 |
   | `adminUsername` | 管理员测试用户 |
   | `adminPassword` | 管理员密码 |
   | `userToken` | 登录后提取的普通用户 JWT |
   | `adminToken` | 登录后提取的管理员 JWT |
   | `userId` | 登录后提取的普通用户 ID |
   | `questionId` | 管理员创建题目后提取的 ID |
   | `announcementId` | 管理员创建公告后提取的 ID |

账号密码只放在“本地值”，不要写入共享值或提交到仓库。

## 2. 保存基础接口用例

请求成功后，在 Apifox 中点击“保存为用例”。每个接口至少保存一个正常用例；鉴权和参数校验接口还应保存异常用例。

### 2.1 存活检查

- 名称：`01-ping`
- 请求：`GET /ping`
- 断言：HTTP 状态码为 `200`、`$.success` 等于 `true`、`$.data.ok` 等于 `true`

### 2.2 登录并提取 JWT

- 名称：`02-user-login`
- 请求：`POST /auth/login`
- Body：

```json
{
  "username": "{{normalUsername}}",
  "password": "{{normalPassword}}"
}
```

- 断言：HTTP 状态码为 `200`、`$.success` 等于 `true`、`$.data.token` 存在
- 后置提取：
  - `$.data.token` -> `userToken`
  - `$.data.user.id` -> `userId`

管理员登录用例相同，将账号改为管理员账号，并把 `$.data.token` 提取到 `adminToken`。

### 2.3 当前用户与伪造 Token

正常资料请求：

- 请求：`GET /auth/profile`
- Header：`Authorization: Bearer {{userToken}}`
- 断言：HTTP `200`，`$.data.id` 等于 `{{userId}}`

再保存两个反向用例：

- 不传 Authorization：断言 HTTP 状态码为当前安全配置返回的 `403`
- `Authorization: Bearer user-1`：断言 HTTP 状态码为 `403`

第二个用例用于防止旧版可伪造 Token 兼容逻辑重新出现。

### 2.4 普通用户不能写题库

- 请求：`POST /questions`
- Header：`Authorization: Bearer {{userToken}}`
- Body：

```json
{
  "type": 1,
  "difficulty": 1,
  "category": "Apifox回归测试",
  "resource": "自动化测试",
  "question": "普通用户不应成功创建此题",
  "picture": false,
  "options": ["A", "B", "C", "D"],
  "answer": 1,
  "analysis": "权限回归测试",
  "keywords": ["apifox", "permission"]
}
```

- 断言：HTTP 状态码为 `403`

对 `/training/questions` 再保存一个同类用例，断言也应为 `403`。

### 2.5 管理员创建、修改和删除题目

创建：

- 请求：`POST /questions`
- Header：`Authorization: Bearer {{adminToken}}`
- Body：复用上一节 JSON，将题目改为“Apifox 自动化测试题”
- 断言：HTTP `200`，`$.success` 等于 `true`
- 提取：`$.data.id` -> `questionId`

修改：

- 请求：`PUT /questions/{{questionId}}`
- Header：`Authorization: Bearer {{adminToken}}`
- Body：使用完整题目 JSON
- 断言：HTTP `200`

删除：

- 请求：`DELETE /questions/{{questionId}}`
- Header：`Authorization: Bearer {{adminToken}}`
- 断言：HTTP `200`

删除用例应放在场景末尾。

### 2.6 正式试卷不得泄露答案

- 请求：`GET /exam/paper`
- Header：`Authorization: Bearer {{userToken}}`
- 基础断言：HTTP `200`
- 后置脚本：

```javascript
pm.test("正式试卷不包含答案和解析", function () {
  const body = pm.response.json();
  pm.expect(body.success).to.eql(true);
  pm.expect(body.data).to.be.an("array");
  body.data.forEach(function (question) {
    pm.expect(question).not.to.have.property("answer");
    pm.expect(question).not.to.have.property("analysis");
  });
});
```

这一条是安全回归测试，不能只断言接口成功。

### 2.7 交卷与考试历史

交卷：

- 请求：`POST /exam/submit`
- Header：`Authorization: Bearer {{userToken}}`
- Body 类型：`x-www-form-urlencoded`
  - `answers`：`{{questionId}}:1`
  - `duration`：`30`
- 断言：HTTP `200`，`$.success` 等于 `true`

请求中不要传 `userId`。后端必须只根据 JWT 确定答题用户。

历史：

- 请求：`GET /exam/history?page=1&size=10`
- Header：`Authorization: Bearer {{userToken}}`
- 断言：HTTP `200`，`$.data` 是数组
- 后置脚本：

```javascript
pm.test("历史记录只属于当前登录用户", function () {
  const body = pm.response.json();
  body.data.forEach(function (record) {
    pm.expect(String(record.userId)).to.eql(String(pm.environment.get("userId")));
  });
});
```

再保存一个不传 Token 的历史用例，断言 HTTP `403`，防止再次返回所有用户记录。

### 2.8 通知和公告

管理员创建公告：

- 请求：`POST /admin/announcements`
- Header：`Authorization: Bearer {{adminToken}}`
- Body：

```json
{
  "type": "system",
  "title": "Apifox 自动化公告",
  "content": "用于通知接口回归测试",
  "important": "false"
}
```

- 提取：`$.data.id` -> `announcementId`

普通用户读取通知：

- 请求：`GET /notifications?unreadOnly=true&page=1&size=20`
- Header：`Authorization: Bearer {{userToken}}`
- 断言：HTTP `200`

标记已读：

- 请求：`PUT /notifications/{{announcementId}}/read`
- Header：`Authorization: Bearer {{userToken}}`
- 断言：HTTP `200`

隐藏通知：

- 请求：`DELETE /notifications/{{announcementId}}`
- Header：`Authorization: Bearer {{userToken}}`
- 断言：HTTP `200`
- 再次查询通知列表，断言结果中不存在该公告 ID

## 3. 组装自动化场景

在“自动化测试”中新建 `PRTS-核心回归`，按顺序添加：

1. ping
2. 普通用户登录并提取 `userToken`、`userId`
3. 管理员登录并提取 `adminToken`
4. 当前用户资料
5. 旧版 `user-1` Token 被拒绝
6. 普通用户创建正式题目被拒绝
7. 普通用户创建培训题目被拒绝
8. 管理员创建题目并提取 `questionId`
9. 管理员修改题目
10. 正式试卷不返回答案
11. 用户交卷
12. 查询当前用户考试历史
13. 未登录查询历史被拒绝
14. 管理员创建公告
15. 用户读取、标记已读并隐藏通知
16. 管理员删除测试题目

接口用例修改后，在场景中执行“立即同步”，否则场景可能仍使用旧快照。

## 4. 参数化测试

将同目录下的 `register-negative-cases.csv` 作为场景测试数据，注册请求使用：

```json
{
  "username": "{{username}}",
  "password": "{{password}}",
  "email": "{{email}}"
}
```

断言 HTTP 状态码等于 `{{expectedStatus}}`。成功注册用例必须使用唯一用户名：

```javascript
pm.environment.set("newUsername", "apifox_" + Date.now());
```

正式回归建议连接专用测试数据库，避免污染开发数据。

## 5. 本地、CLI 与 CI

1. 先在 Apifox 客户端本地运行，确认变量提取和清理步骤正确。
2. 在场景的“CI/CD”页面选择环境和报告格式，复制 Apifox 生成的 CLI 命令。
3. CI 中保留 JUnit 或 HTML 报告，失败时保存请求、响应和断言信息。
4. 管理员密码通过 CI 密钥注入，不写入 CSV、脚本或仓库。

推荐分层：

- 每次提交：`ping + 登录 + 权限矩阵 + 试卷不泄露答案`
- 合并前：完整核心回归
- 定时任务：带注册、交卷、公告写入的全链路场景

