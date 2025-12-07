# 博士考核系统 API 文档

## 📋 接口规范

### 基础信息
- **基础URL**: `http://localhost:8888/api`（开发环境）
- **请求格式**: `application/json`
- **响应格式**: `application/json`
- **字符编码**: `UTF-8`

### 统一响应格式
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {},
  "timestamp": 1630000000000
}
```

**状态码说明**:
- `200`: 成功
- `400`: 请求参数错误
- `401`: 未授权/未登录
- `403`: 权限不足
- `404`: 资源不存在
- `500`: 服务器内部错误

## 🔐 认证模块

### 1. 用户注册
- **接口**: `POST /auth/register`
- **描述**: 新用户注册
- **请求体**:
```json
{
  "username": "doctor123",
  "password": "password123",
  "email": "doctor@rhodes.com"
}
```
- **响应**:
```json
{
  "code": 200,
  "message": "注册成功",
  "data": {
    "userId": 1,
    "username": "doctor123",
    "email": "doctor@rhodes.com",
    "isAdmin": false,
    "createdAt": "2024-01-01T00:00:00Z"
  }
}
```
要求：\
1.用户名至少要3个字符\
2.密码至少要6个字符\
3.用户名不能重复。

### 2. 用户登录
- **接口**: `POST /auth/login`
- **描述**: 用户登录
- **请求体**:
```json
{
  "username": "doctor123",
  "password": "password123"
}
```
- **响应**:
```json
{
  "code": 200,
  "message": "登录成功",
  "data": {
    "userId": 1,
    "username": "doctor123",
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "isAdmin": false,
    "expiresIn": 86400
  }
}
```

### 3. 获取用户信息
- **接口**: `GET /auth/profile`
- **描述**: 获取当前登录用户信息
- **请求头**: `Authorization: Bearer {token}`
- **响应**:
```json
{
  "code": 200,
  "message": "获取成功",
  "data": {
    "userId": 1,
    "username": "doctor123",
    "email": "doctor@rhodes.com",
    "isAdmin": false,
    "createdAt": "2024-01-01T00:00:00Z",
    "totalAnswers": 150,
    "correctRate": 85.5,
    "examCount": 10,
    "bestScore": 95
  }
}
```

### 4. 退出登录
- **接口**: `POST /auth/logout`
- **描述**: 用户退出登录
- **请求头**: `Authorization: Bearer {token}`
- **响应**:
```json
{
  "code": 200,
  "message": "退出成功",
  "data": null
}
```

## 📚 题目管理模块

### 5. 获取所有题目
- **接口**: `GET /questions`
- **描述**: 获取题库所有题目
- **查询参数**:
  - `page`: 页码（可选，默认1）
  - `size`: 每页数量（可选，默认50）
  - `type`: 按类型筛选（1-5）
  - `difficulty`: 按难度筛选（1-5）
  - `keyword`: 关键词搜索
- **响应**:
```json
{
  "code": 200,
  "message": "获取成功",
  "data": {
    "questions": [
      {
        "id": 1,
        "type": 1,
        "typeText": "干员调配与特性化决策",
        "difficulty": 1,
        "difficultyText": "常识",
        "question": "明日方舟一共有几个职业？",
        "options": ["6个", "7个", "8个", "9个"],
        "answer": 3,
        "analysis": "正确答案是8个...",
        "resource": "基础知识",
        "keywords": ["职业", "基础"],
        "picture": false,
        "createdAt": "2024-01-01T00:00:00Z",
        "updatedAt": "2024-01-01T00:00:00Z"
      }
    ],
    "total": 100,
    "page": 1,
    "size": 50,
    "pages": 2
  }
}
```

### 6. 获取单题详情
- **接口**: `GET /questions/{id}`
- **描述**: 获取指定题目详情
- **响应**:
```json
{
  "code": 200,
  "message": "获取成功",
  "data": {
    "id": 1,
    "type": 1,
    "typeText": "干员调配与特性化决策",
    "difficulty": 1,
    "difficultyText": "常识",
    "question": "明日方舟一共有几个职业？",
    "options": ["6个", "7个", "8个", "9个"],
    "answer": 3,
    "analysis": "正确答案是8个...",
    "resource": "基础知识",
    "keywords": ["职业", "基础"],
    "picture": false,
    "pictureUrl": "/api/questions/1/image",
    "createdAt": "2024-01-01T00:00:00Z",
    "updatedAt": "2024-01-01T00:00:00Z",
    "stats": {
      "totalAttempts": 150,
      "correctRate": 85.5,
      "mostCommonWrongOption": 2,
      "avgTimeSpent": 45.2
    }
  }
}
```

### 7. 创建题目（管理员）
- **接口**: `POST /questions`
- **描述**: 创建新题目
- **请求头**: `Authorization: Bearer {token}`（需管理员权限）
- **请求体**:
```json
{
  "type": 1,
  "difficulty": 1,
  "question": "明日方舟一共有几个职业？",
  "options": ["6个", "7个", "8个", "9个"],
  "answer": 3,
  "analysis": "正确答案是8个...",
  "resource": "基础知识",
  "keywords": ["职业", "基础"],
  "picture": false
}
```
- **响应**:
```json
{
  "code": 200,
  "message": "创建成功",
  "data": {
    "id": 101,
    "createdAt": "2024-01-01T00:00:00Z"
  }
}
```

### 8. 更新题目（管理员）
- **接口**: `PUT /questions/{id}`
- **描述**: 更新题目信息
- **请求头**: `Authorization: Bearer {token}`（需管理员权限）
- **请求体**: 同创建接口
- **响应**:
```json
{
  "code": 200,
  "message": "更新成功",
  "data": {
    "id": 1,
    "updatedAt": "2024-01-01T00:00:00Z"
  }
}
```

### 9. 删除题目（管理员）
- **接口**: `DELETE /questions/{id}`
- **描述**: 删除题目
- **请求头**: `Authorization: Bearer {token}`（需管理员权限）
- **响应**:
```json
{
  "code": 200,
  "message": "删除成功",
  "data": null
}
```

### 10. 题目搜索
- **接口**: `GET /questions/search`
- **描述**: 搜索题目
- **查询参数**:
  - `keyword`: 搜索关键词
  - `field`: 搜索字段（question/analysis/keywords，默认question）
- **响应**:
```json
{
  "code": 200,
  "message": "搜索成功",
  "data": {
    "results": [
      {
        "id": 1,
        "question": "明日方舟一共有几个职业？",
        "typeText": "干员调配",
        "difficultyText": "常识",
        "keywords": ["职业", "基础"],
        "resource": "基础知识",
        "matchScore": 0.85
      }
    ],
    "total": 1
  }
}
```

## 🎓 培训题目模块

### 11. 获取培训题目列表
- **接口**: GET /training/questions
- **描述**: 获取培训题目，支持分页
- **查询参数**:
    - page: 页码（可选，默认1）
    - size: 每页数量（可选，默认20）

- **响应**:

```json
{
  "code": 200,
  "message": "获取成功",
  "data": {
    "questions": [
      {
        "id": 1,
        "question": "明日方舟一共有八个职业...",
        "options": ["能天使", "推进之王", "银灰", "夜莺"],
        "answer": 2,
        "analysis": "A、狙击<br>B、先锋...",
        "picture": false,
        "resource": "基础知识",
        "order": 1,
        "createdAt": "2024-01-01T00:00:00Z"
      }
    ],
    "total": 56,        // 总题目数（动态计算）
    "page": 1,          // 当前页码
    "size": 20,         // 每页数量
    "pages": 3          // 总页数
  }
}
```

### 12. 获取培训题目详情
- **接口**: `GET /training/questions/{id}`
- **描述**: 获取指定培训题目
- **响应**: 同普通题目详情

### 13. 管理培训题目（管理员）
- **接口**: `POST /training/questions` - 创建
- **接口**: `PUT /training/questions/{id}` - 更新
- **接口**: `DELETE /training/questions/{id}` - 删除
- **请求头**: `Authorization: Bearer {token}`（需管理员权限）
- **请求/响应格式**: 同普通题目

## 📝 答题记录模块

### 14. 提交答案
- **接口**: `POST /answers`
- **描述**: 提交题目答案
- **请求头**: `Authorization: Bearer {token}`
- **请求体**:
```json
{
  "questionId": 1,
  "questionType": "normal",  // normal/training/exam
  "selectedOption": 3,
  //"timeSpent": 45,          // 单位：秒  有余力再实现
  "examId": null           // 如果是考试中的题目
}
```
- **响应**:
```json
{
  "code": 200,
  "message": "提交成功",
  "data": {
    "isCorrect": true,
    "correctAnswer": 3,
    "explanation": "正确答案是C...",
    "questionStats": {
      "totalAttempts": 151,
      "correctRate": 85.4,
      "mostCommonWrongOption": 2
    },
    "userStats": {
      "totalAnswers": 151,
      "correctRate": 85.4,
      "streak": 5
    }
  }
}
```

### 15. 获取答题历史
- **接口**: `GET /answers/history`
- **描述**: 获取用户的答题历史
- **请求头**: `Authorization: Bearer {token}`
- **查询参数**:
  - `page`: 页码
  - `size`: 每页数量
  - `questionType`: 题目类型
  - `startDate`: 开始日期
  - `endDate`: 结束日期
- **响应**:
```json
{
  "code": 200,
  "message": "获取成功",
  "data": {
    "history": [
      {
        "id": 1001,
        "questionId": 1,
        "questionType": "normal",
        "questionText": "明日方舟一共有几个职业？",
        "selectedOption": 3,
        "correctAnswer": 3,
        "isCorrect": true,
        "timeSpent": 45,
        "answeredAt": "2024-01-01T10:00:00Z",
        "examId": null
      }
    ],
    "total": 150,
    "page": 1,
    "size": 20,
    "pages": 8
  }
}
```

### 16. 获取错题本
- **接口**: `GET /answers/wrong`
- **描述**: 获取用户的错题
- **请求头**: `Authorization: Bearer {token}`
- **查询参数**: 同答题历史
- **响应**: 同答题历史

### 17. 删除错题记录
- **接口**: `DELETE /answers/wrong/{questionId}`
- **描述**: 从错题本中移除题目
- **请求头**: `Authorization: Bearer {token}`
- **响应**:
```json
{
  "code": 200,
  "message": "移除成功",
  "data": null
}
```

## 🏆 考试模块

### 18. 生成考试试卷
- **接口**: `POST /exams/generate`
- **描述**: 生成新的考试试卷
- **请求头**: `Authorization: Bearer {token}`
- **请求体**:
```json
{
  "type": "full",           // full: 全真模拟
  "questionCount": 25,      // 题目数量
  "timeLimit": 900          // 时间限制（秒）
}
```
- **响应**:
```json
{
  "code": 200,
  "message": "生成成功",
  "data": {
    "examId": "EX20240101001",
    "questions": [
      {
        "examQuestionId": 1,
        "questionId": 1,
        "type": 1,
        "difficulty": 1,
        "question": "明日方舟一共有几个职业？",
        "options": ["6个", "7个", "8个", "9个"],
        "order": 1,
        "points": 3
      }
    ],
    "totalQuestions": 25,
    "timeLimit": 900,
    "totalPoints": 100,
    "startTime": "2024-01-01T10:00:00Z",
    "expireTime": "2024-01-01T10:15:00Z"
  }
}
```

### 19. 提交考试答案
- **接口**: `POST /exams/{examId}/submit`
- **描述**: 提交整场考试的答案
- **请求头**: `Authorization: Bearer {token}`
- **请求体**:
```json
{
  "answers": [
    {
      "examQuestionId": 1,
      "selectedOption": 3,
      "timeSpent": 45
    }
  ],
  "submitTime": "2024-01-01T10:14:30Z"
}
```
- **响应**:
```json
{
  "code": 200,
  "message": "提交成功",
  "data": {
    "examId": "EX20240101001",
    "score": 85,
    "totalPoints": 100,
    "correctCount": 21,
    "wrongCount": 4,
    "timeUsed": 870,
    "timeLimit": 900,
    "submittedAt": "2024-01-01T10:14:30Z",
    "details": [
      {
        "questionId": 1,
        "questionText": "明日方舟一共有几个职业？",
        "correctAnswer": 3,
        "selectedOption": 3,
        "isCorrect": true,
        "points": 3,
        "explanation": "正确答案是8个..."
      }
    ],
    "analysis": {
      "byType": {
        "1": {"correct": 5, "total": 5, "accuracy": 100.0},
        "2": {"correct": 4, "total": 5, "accuracy": 80.0}
      },
      "byDifficulty": {
        "1": {"correct": 5, "total": 5, "accuracy": 100.0},
        "2": {"correct": 4, "total": 5, "accuracy": 80.0}
      }
    }
  }
}
```

### 20. 获取考试历史
- **接口**: `GET /exams/history`
- **描述**: 获取用户的考试历史
- **请求头**: `Authorization: Bearer {token}`
- **查询参数**:
  - `page`: 页码
  - `size`: 每页数量
  - `startDate`: 开始日期
  - `endDate`: 结束日期
- **响应**:
```json
{
  "code": 200,
  "message": "获取成功",
  "data": {
    "exams": [
      {
        "examId": "EX20240101001",
        "type": "full",
        "score": 85,
        "totalPoints": 100,
        "correctCount": 21,
        "totalQuestions": 25,
        "timeUsed": 870,
        "timeLimit": 900,
        "startedAt": "2024-01-01T10:00:00Z",
        "submittedAt": "2024-01-01T10:14:30Z",
        "ranking": 150,
        "totalParticipants": 1000
      }
    ],
    "total": 10,
    "page": 1,
    "size": 20,
    "pages": 1,
    "stats": {
      "totalExams": 10,
      "avgScore": 82.5,
      "bestScore": 95,
      "avgTimeUsed": 800,
      "totalQuestionsAnswered": 250,
      "totalCorrectAnswers": 210,
      "overallAccuracy": 84.0
    }
  }
}
```

### 21. 获取考试详情
- **接口**: `GET /exams/{examId}`
- **描述**: 获取考试详情和答案解析
- **请求头**: `Authorization: Bearer {token}`
- **响应**: 同考试提交响应

### 22. 获取考试排行榜
- **接口**: `GET /exams/leaderboard`
- **描述**: 获取考试排行榜
- **查询参数**:
  - `type`: 排行榜类型（daily/weekly/monthly/all，默认all）
  - `limit`: 返回数量（默认10）
- **响应**:
```json
{
  "code": 200,
  "message": "获取成功",
  "data": {
    "leaderboard": [
      {
        "rank": 1,
        "userId": 100,
        "username": "顶级博士",
        "avatar": "/api/avatars/100.jpg",
        "score": 98,
        "timeUsed": 850,
        "examDate": "2024-01-01T10:14:30Z",
        "examCount": 15
      }
    ],
    "myRank": {
      "rank": 150,
      "userId": 1,
      "username": "doctor123",
      "score": 85,
      "timeUsed": 870,
      "examDate": "2024-01-01T10:14:30Z",
      "examCount": 10
    },
    "totalParticipants": 1000,
    "period": "all",
    "generatedAt": "2024-01-01T12:00:00Z"
  }
}
```

## 📊 统计模块

### 23. 获取用户统计
- **接口**: `GET /stats/user`
- **描述**: 获取用户的学习统计
- **请求头**: `Authorization: Bearer {token}`
- **响应**:
```json
{
  "code": 200,
  "message": "获取成功",
  "data": {
    "overview": {
      "totalAnswers": 150,
      "correctAnswers": 128,
      "accuracy": 85.3,
      "totalTimeSpent": 7200,
      "avgTimePerQuestion": 48.0,
      "currentStreak": 7,
      "longestStreak": 15
    },
    "byType": {
      "1": {"total": 30, "correct": 28, "accuracy": 93.3},
      "2": {"total": 30, "correct": 25, "accuracy": 83.3}
    },
    "byDifficulty": {
      "1": {"total": 30, "correct": 29, "accuracy": 96.7},
      "2": {"total": 30, "correct": 26, "accuracy": 86.7}
    },
    "progress": {
      "trainingCompleted": 8,
      "trainingTotal": 12,
      "trainingProgress": 66.7,
      "questionsReviewed": 75,
      "questionsTotal": 100,
      "questionsProgress": 75.0
    },
    "recentActivity": [
      {
        "date": "2024-01-01",
        "answers": 15,
        "accuracy": 86.7,
        "timeSpent": 720
      }
    ]
  }
}
```

### 24. 获取题目统计
- **接口**: `GET /stats/question/{questionId}`
- **描述**: 获取题目的全局统计信息
- **响应**:
```json
{
  "code": 200,
  "message": "获取成功",
  "data": {
    "questionId": 1,
    "totalAttempts": 150,
    "correctAttempts": 128,
    "accuracy": 85.3,
    "avgTimeSpent": 48.2,
    "medianTimeSpent": 45.0,
    "mostCommonWrongOption": 2,
    "wrongOptionStats": {
      "1": {"count": 8, "percentage": 36.4},
      "2": {"count": 12, "percentage": 54.5},
      "4": {"count": 2, "percentage": 9.1}
    },
    "byUserGroup": {
      "new": {"attempts": 50, "accuracy": 70.0},
      "intermediate": {"attempts": 70, "accuracy": 85.7},
      "advanced": {"attempts": 30, "accuracy": 96.7}
    },
    "firstSeen": "2024-01-01T00:00:00Z",
    "lastAttempt": "2024-01-01T10:00:00Z"
  }
}
```

### 25. 获取系统统计（管理员）
- **接口**: `GET /stats/system`
- **描述**: 获取系统全局统计
- **请求头**: `Authorization: Bearer {token}`（需管理员权限）
- **响应**:
```json
{
  "code": 200,
  "message": "获取成功",
  "data": {
    "users": {
      "total": 1000,
      "activeToday": 150,
      "activeWeek": 500,
      "newToday": 10,
      "newWeek": 50
    },
    "questions": {
      "total": 100,
      "training": 12,
      "byType": {"1": 20, "2": 20, "3": 20, "4": 20, "5": 20},
      "byDifficulty": {"1": 20, "2": 20, "3": 20, "4": 20, "5": 20}
    },
    "answers": {
      "total": 15000,
      "today": 150,
      "week": 1500,
      "accuracy": 82.5,
      "avgTimeSpent": 45.2
    },
    "exams": {
      "total": 1000,
      "today": 15,
      "week": 150,
      "avgScore": 78.5,
      "completionRate": 85.3
    },
    "performance": {
      "responseTime": 125,
      "uptime": 99.9,
      "lastUpdated": "2024-01-01T12:00:00Z"
    }
  }
}
```

## 🛠️ 系统管理模块（管理员）

### 26. 获取所有用户
- **接口**: `GET /admin/users`
- **描述**: 获取所有用户列表
- **请求头**: `Authorization: Bearer {token}`（需管理员权限）
- **查询参数**:
  - `page`: 页码
  - `size`: 每页数量
  - `role`: 按角色筛选
  - `status`: 按状态筛选
- **响应**:
```json
{
  "code": 200,
  "message": "获取成功",
  "data": {
    "users": [
      {
        "id": 1,
        "username": "doctor123",
        "email": "doctor@rhodes.com",
        "role": "user",
        "status": "active",
        "createdAt": "2024-01-01T00:00:00Z",
        "lastLogin": "2024-01-01T10:00:00Z",
        "answerCount": 150,
        "examCount": 10,
        "isAdmin": false
      }
    ],
    "total": 1000,
    "page": 1,
    "size": 20,
    "pages": 50
  }
}
```

### 27. 修改用户信息
- **接口**: `PUT /admin/users/{userId}`
- **描述**: 修改用户信息
- **请求头**: `Authorization: Bearer {token}`（需管理员权限）
- **请求体**:
```json
{
  "role": "admin",
  "status": "active"
}
```
- **响应**:
```json
{
  "code": 200,
  "message": "修改成功",
  "data": {
    "userId": 1,
    "updatedAt": "2024-01-01T12:00:00Z"
  }
}
```

### 28. 系统配置管理
- **接口**: `GET /admin/config` - 获取配置
- **接口**: `PUT /admin/config` - 更新配置
- **描述**: 管理系统配置
- **请求头**: `Authorization: Bearer {token}`（需管理员权限）
- **请求体**:
```json
{
  "exam": {
    "questionCount": 25,
    "timeLimit": 900,
    "passingScore": 60
  },
  "training": {
    "requiredCompletion": 80
  },
  "system": {
    "maintenanceMode": false,
    "registrationEnabled": true
  }
}
```

## 📁 文件上传模块

### 29. 上传题目图片
- **接口**: `POST /upload/question-image`
- **描述**: 上传题目图片
- **请求头**: 
  - `Authorization: Bearer {token}`
  - `Content-Type: multipart/form-data`
- **请求参数**:
  - `file`: 图片文件
  - `questionId`: 题目ID
- **响应**:
```json
{
  "code": 200,
  "message": "上传成功",
  "data": {
    "url": "/api/questions/1/image",
    "filename": "question_1.png",
    "size": 102400,
    "uploadedAt": "2024-01-01T12:00:00Z"
  }
}
```

### 30. 上传用户头像
- **接口**: `POST /upload/avatar`
- **描述**: 上传用户头像
- **请求头**: 
  - `Authorization: Bearer {token}`
  - `Content-Type: multipart/form-data`
- **请求参数**:
  - `file`: 头像文件
- **响应**:
```json
{
  "code": 200,
  "message": "上传成功",
  "data": {
    "url": "/api/avatars/1.jpg",
    "filename": "avatar_1.jpg",
    "size": 51200,
    "uploadedAt": "2024-01-01T12:00:00Z"
  }
}
```

## 🔔 通知模块

### 31. 获取通知
- **接口**: `GET /notifications`
- **描述**: 获取用户通知
- **请求头**: `Authorization: Bearer {token}`
- **查询参数**:
  - `unreadOnly`: 是否只获取未读（默认false）
- **响应**:
```json
{
  "code": 200,
  "message": "获取成功",
  "data": {
    "notifications": [
      {
        "id": 1,
        "type": "system",
        "title": "系统更新",
        "content": "系统已更新到v2.0版本",
        "isRead": false,
        "createdAt": "2024-01-01T10:00:00Z",
        "expiresAt": "2024-01-08T10:00:00Z"
      }
    ],
    "unreadCount": 3
  }
}
```

### 32. 标记通知已读
- **接口**: `PUT /notifications/{id}/read`
- **描述**: 标记通知为已读
- **请求头**: `Authorization: Bearer {token}`
- **响应**:
```json
{
  "code": 200,
  "message": "标记成功",
  "data": {
    "notificationId": 1,
    "readAt": "2024-01-01T12:00:00Z"
  }
}
```

## 📄 导出模块

### 33. 导出答题记录
- **接口**: `POST /export/answers`
- **描述**: 导出用户的答题记录
- **请求头**: `Authorization: Bearer {token}`
- **请求体**:
```json
{
  "format": "csv",  // csv/excel/pdf
  "startDate": "2024-01-01",
  "endDate": "2024-01-31",
  "includeQuestions": true
}
```
- **响应**:
```json
{
  "code": 200,
  "message": "导出成功",
  "data": {
    "downloadUrl": "/api/export/download/abc123.csv",
    "filename": "答题记录_202401.csv",
    "size": 102400,
    "expiresAt": "2024-01-01T13:00:00Z"
  }
}
```

### 34. 导出考试报告
- **接口**: `POST /export/exam-report/{examId}`
- **描述**: 导出考试详细报告
- **请求头**: `Authorization: Bearer {token}`
- **请求体**:
```json
{
  "format": "pdf",  // pdf/excel
  "includeAnalysis": true
}
```

## 🚀 工具接口

### 35. 健康检查
- **接口**: `GET /health`
- **描述**: 检查服务是否正常
- **响应**:
```json
{
  "code": 200,
  "message": "服务正常",
  "data": {
    "status": "UP",
    "timestamp": 1630000000000,
    "version": "2.0.0",
    "uptime": 86400
  }
}
```

### 36. 获取系统信息
- **接口**: `GET /system/info`
- **描述**: 获取系统基本信息
- **响应**:
```json
{
  "code": 200,
  "message": "获取成功",
  "data": {
    "appName": "博士考核系统",
    "version": "2.0.0",
    "description": "明日方舟博士业务能力考核平台",
    "author": "罗德岛制药",
    "license": "MIT",
    "buildTime": "2024-01-01T00:00:00Z",
    "apiVersion": "v1",
    "supportEmail": "support@arknights-exam.com"
  }
}
```

## 📌 接口分类总结

| 模块 | 接口数量 | 主要功能 |
|------|----------|----------|
| 认证模块 | 4 | 用户注册、登录、信息获取 |
| 题目管理 | 6 | 题目CRUD、搜索 |
| 培训题目 | 3 | 培训题目管理 |
| 答题记录 | 4 | 答题、历史、错题管理 |
| 考试模块 | 5 | 考试生成、提交、历史、排行 |
| 统计模块 | 3 | 用户、题目、系统统计 |
| 系统管理 | 3 | 用户管理、系统配置 |
| 文件上传 | 2 | 图片、头像上传 |
| 通知模块 | 2 | 通知获取、标记已读 |
| 导出模块 | 2 | 数据导出 |
| 工具接口 | 2 | 健康检查、系统信息 |

**总计：36个接口**

## 🔧 注意事项

1. **接口版本**: 所有接口前缀为 `/api/v1/`
2. **分页参数**: 所有列表接口都支持分页，默认 page=1, size=20
3. **时间格式**: 统一使用 ISO 8601 格式：`YYYY-MM-DDTHH:mm:ssZ`
4. **权限控制**:
   - 公开接口：不需要认证
   - 用户接口：需要有效 token
   - 管理员接口：需要管理员权限
5. **错误处理**: 所有接口都需要有详细的错误信息返回
6. **数据验证**: 所有输入都需要做有效性验证
7. **缓存策略**: 频繁读取但不常变的数据应该缓存
