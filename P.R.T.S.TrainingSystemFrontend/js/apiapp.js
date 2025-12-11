// js/api/index.js - 博士考核系统所有API接口整合
(function () {
    'use strict';

    if (typeof http === 'undefined') {
        console.error('请先加载 request.js');
        return;
    }

    // 统一API前缀，优先使用 window.API_BASE_URL（由 config.js 设置），否则回退到后端默认端口
    const BASE = ((window.API_BASE_URL && String(window.API_BASE_URL)) || 'http://localhost:8080').replace(/\/+$/, '');
    // 后端 Main.java 在根路径注册接口（并兼容 /api 与 /api/v1）
    const API_PREFIX = BASE;

    // 简单表单 POST 助手（application/x-www-form-urlencoded）
    function postForm(url, data = {}) {
        const body = new URLSearchParams();
        Object.keys(data).forEach(k => {
            if (data[k] !== undefined && data[k] !== null) body.append(k, data[k]);
        });
        return fetch(url, {
            method: 'POST',
            mode: 'cors',
            headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
            body: body.toString()
        }).then(async resp => {
            const text = await resp.text();
            try { return JSON.parse(text); } catch (e) { return text; }
        });
    }

    // 整合所有API接口
    window.api = {
        // 认证模块
        auth: {
            // 用户注册（使用 form 表单以兼容后端 parseForm）
            register: function (username, password, email) {
                return postForm(`${API_PREFIX}/register`, { username, password, email });
            },

            // 用户登录（使用 form 表单以兼容后端 parseForm）
            login: function (username, password) {
                return postForm(`${API_PREFIX}/login`, { username, password });
            },

            // 获取当前用户信息（后端未必提供统一 profile，保留为占位）
            getCurrentUser: function () {
                return http.get(`${API_PREFIX}/user/me`).catch(() => http.get(`${API_PREFIX}/user`));
            },

            // 退出登录（占位）
            logout: function () {
                return postForm(`${API_PREFIX}/logout`, {});
            }
        },

        // 题目管理模块
        questions: {
            // 获取题目列表（使用相对路径，request.js 已设置 baseURL）
            getList: function (params = {}) {
                return http.get(`${API_PREFIX}/questions`, params);
            },

            // 获取题目详情
            getDetail: function (id, includeAnalysis = true) {
                return http.get(`${API_PREFIX}/questions/${id}`, { includeAnalysis });
            },

            // 创建题目
            create: function (question) {
                return http.post(`${API_PREFIX}/questions`, question);
            },

            // 更新题目
            update: function (id, question) {
                return http.put(`${API_PREFIX}/questions/${id}`, question);
            },

            // 删除题目
            delete: function (id) {
                return http.delete(`${API_PREFIX}/questions/${id}`);
            },

            // 搜索题目
            search: function (keyword, field = 'question') {
                return http.get(`${API_PREFIX}/questions/search`, { keyword, field });
            },

            // 获取题目统计
            getStats: function (id) {
                return http.get(`${API_PREFIX}/stats/question/${id}`);
            }
        },

        // 培训题目模块
        trainingQuestions: {
            // 获取培训题目列表：先尝试 /training/questions，若失败再降级到 /questions
            getList: function (params = {}) {
                return http.get(`${API_PREFIX}/training/questions`, params)
                    .catch(() => http.get(`${API_PREFIX}/questions`, params));
            },

            // 获取培训题目详情
            getDetail: function (id) {
                return http.get(`${API_PREFIX}/training/questions/${id}`).catch(() => http.get(`${API_PREFIX}/questions/${id}`));
            },

            // 创建培训题目
            create: function (question) {
                return http.post(`${API_PREFIX}/training/questions`, question);
            },

            // 更新培训题目
            update: function (id, question) {
                return http.put(`${API_PREFIX}/training/questions/${id}`, question);
            },

            // 删除培训题目
            delete: function (id) {
                return http.delete(`${API_PREFIX}/training/questions/${id}`);
            }
        },

        // 答题记录模块（保留原定义，具体实现可能降级）
        answers: {
            submit: function (data) {
                // 若已包含 userId 与 answers，优先以 application/x-www-form-urlencoded 提交到 /exam/submit（兼容后端 parseForm）
                try {
                    if (data && (data.userId || data.answers)) {
                        // 标准化 answers：支持字符串/数组/对象
                        let answersStr = '';
                        if (typeof data.answers === 'string') answersStr = data.answers;
                        else if (Array.isArray(data.answers)) answersStr = data.answers.join(',');
                        else if (typeof data.answers === 'object' && data.answers !== null) {
                            answersStr = Object.keys(data.answers).map(k => `${k}:${data.answers[k]}`).join(',');
                        }
                        const body = new URLSearchParams();
                        if (data.userId) body.append('userId', data.userId);
                        body.append('answers', answersStr);
                        return fetch(`${API_PREFIX}/exam/submit`, {
                            method: 'POST',
                            mode: 'cors',
                            headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
                            body: body.toString()
                        }).then(async resp => {
                            const txt = await resp.text();
                            if (!resp.ok) throw new Error(txt || resp.statusText);
                            try { return JSON.parse(txt); } catch (e) { return txt; }
                        });
                    }
                } catch (e) {
                    return Promise.reject(e);
                }
                // 否则尝试 /answers 接口（若实现）
                return http.post(`${API_PREFIX}/answers`, data).catch(err => Promise.reject(err));
            },

            getHistory: function (params = {}) {
                return http.get(`${API_PREFIX}/answers/history`, params).catch(() => Promise.resolve([]));
            },

            getWrongQuestions: function (params = {}) {
                return http.get(`${API_PREFIX}/answers/wrong`, params).catch(() => Promise.resolve([]));
            },

            removeWrongQuestion: function (questionId) {
                return http.delete(`${API_PREFIX}/answers/wrong/${questionId}`);
            }
        },

        // 考试模块（调整以匹配后端）
        exams: {
            // 生成考试试卷：GET /exam/paper?count=XX
            generate: function (questionCount = 25) {
                return http.get(`${API_PREFIX}/exam/paper`, { count: questionCount });
            },

            // 提交考试答案：使用 form 表单提交到 /exam/submit，参数应包含 userId 与 answers（answers 格式： "qid:option,qid:option"）
            submit: function (userId, answers) {
                // 若 answers 是数组或对象，尝试序列化为 qid:option,...
                let answersStr = '';
                if (typeof answers === 'string') answersStr = answers;
                else if (Array.isArray(answers)) answersStr = answers.join(',');
                else if (typeof answers === 'object') {
                    answersStr = Object.keys(answers).map(k => `${k}:${answers[k]}`).join(',');
                }

                const body = new URLSearchParams();
                if (userId) body.append('userId', userId);
                body.append('answers', answersStr);

                return fetch(`${API_PREFIX}/exam/submit`, {
                    method: 'POST',
                    mode: 'cors',
                    headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
                    body: body.toString()
                }).then(async resp => {
                    const text = await resp.text();
                    try { return JSON.parse(text); } catch (e) { return text; }
                });
            },

            getHistory: function (params = {}) {
                return http.get(`${API_PREFIX}/exam/history`, params).catch(() => Promise.resolve([]));
            },

            getDetail: function (examId) {
                return http.get(`${API_PREFIX}/exam/${examId}`).catch(() => Promise.resolve(null));
            },

            getLeaderboard: function (type = 'all', limit = 10) {
                return http.get(`${API_PREFIX}/exams/leaderboard`, { type, limit }).catch(() => Promise.resolve([]));
            }
        },

        // 用户相关接口
        user: {
            getWrongQuestions: function (userId) {
                return http.get(`${API_PREFIX}/user/${userId}/wrong`).catch(() => Promise.resolve([]));
            },

            getStats: function (userId) {
                return http.get(`${API_PREFIX}/stats/user/${userId || ''}`).catch(() => Promise.resolve({}));
            }
        },

        // 系统工具接口
        system: {
            healthCheck: function () {
                return http.get(`${API_PREFIX}/ping`, {}, { showLoading: false }).catch(() => Promise.resolve(null));
            },

            getInfo: function () {
                return http.get(`${API_PREFIX}/system/info`, {}, { showLoading: false }).catch(() => Promise.resolve({}));
            },

            getTime: function () {
                return http.get(`${API_PREFIX}/system/time`, {}, { showLoading: false }).catch(() => Promise.resolve(null));
            },

            sendFeedback: function (type, content, contact = '') {
                return http.post(`${API_PREFIX}/system/feedback`, { type, content, contact }).catch(() => Promise.resolve({ success: false }));
            }
        },

        upload: {
            questionImage: function (file) {
                return http.upload(`${API_PREFIX}/upload/question-image`, file);
            },
            avatar: function (file) {
                return http.upload(`${API_PREFIX}/upload/avatar`, file);
            }
        },

        export: {
            answers: function (params = {}) {
                return http.post(`${API_PREFIX}/export/answers`, params).catch(() => Promise.resolve({}));
            },
            examReport: function (examId) {
                return http.get(`${API_PREFIX}/export/exam-report/${examId}`).catch(() => Promise.resolve(null));
            },
            userStats: function (params = {}) {
                return http.post(`${API_PREFIX}/export/user-stats`, params).catch(() => Promise.resolve({}));
            }
        }
    };

    console.log('API接口已初始化');
})();