/* 后续修改指南：
 * notifications.js 中的接口还未整合到 apiapp.js，请参考API规范。
 */


(function () {
    'use strict';

    if (typeof http === 'undefined') {
        console.error('请先加载 request.js');
        return;
    }

    // 统一API前缀，优先使用 window.API_BASE_URL（由 config.js 设置），否则回退到后端默认端口
    const BASE = ((window.API_BASE_URL && String(window.API_BASE_URL)) || 'http://localhost:8080').replace(/\/+$/, '');
    // API 规范要求所有接口前缀为 /api/v1
    const API_PREFIX = BASE + '/api/v1';

    // 简单JSON POST助手
    function postJson(url, data = {}) {
        return fetch(url, {
            method: 'POST',
            mode: 'cors',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(data)
        }).then(async resp => {
            const text = await resp.text();
            try { return JSON.parse(text); } catch (e) { return text; }
        });
    }

    // 整合所有API接口
    window.api = {
        // 认证模块
        auth: {
            // 【认证模块-1】用户注册
            register: function (username, password, email) {
                return postJson(`${API_PREFIX}/auth/register`, { username, password, email });
            },

            // 【认证模块-2】用户登录
            login: function (username, password) {
                return postJson(`${API_PREFIX}/auth/login`, { username, password });
            },

            // 【认证模块-3】获取当前登录用户信息
            getCurrentUser: function () {
                return http.get(`${API_PREFIX}/auth/profile`);
            },

            // 【认证模块-4】用户退出登录
            logout: function () {
                return postJson(`${API_PREFIX}/auth/logout`, {});
            }
        },

        // 题目管理模块
        questions: {
            // 【题目管理模块-5】获取所有题目
            getList: function (params = {}) {
                return http.get(`${API_PREFIX}/questions`, params);
            },

            // 【题目管理模块-6】获取单题详情
            getDetail: function (id, includeAnalysis = true) {
                return http.get(`${API_PREFIX}/questions/${id}`, { includeAnalysis });
            },

            // 【题目管理模块-7】创建题目（管理员操作）
            create: function (question) {
                return http.post(`${API_PREFIX}/questions`, question);
            },

            // 【题目管理模块-8】更新题目信息（管理员操作）
            update: function (id, question) {
                return http.put(`${API_PREFIX}/questions/${id}`, question);
            },

            // 【题目管理模块-9】删除题目（管理员操作）
            delete: function (id) {
                return http.delete(`${API_PREFIX}/questions/${id}`);
            },

            // 【题目管理模块-10】搜索题目
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

            // 【培训题目模块-12】获取培训题目详情
            getDetail: function (id) {
                return http.get(`${API_PREFIX}/training/questions/${id}`).catch(() => http.get(`${API_PREFIX}/questions/${id}`));
            },

            // 【培训题目模块-13.1】创建培训题目（管理员操作）
            create: function (question) {
                return http.post(`${API_PREFIX}/training/questions`, question);
            },

            // 【培训题目模块-13.2】更新培训题目（管理员操作）
            update: function (id, question) {
                return http.put(`${API_PREFIX}/training/questions/${id}`, question);
            },

            // 【培训题目模块-13.3】删除培训题目（管理员操作）
            delete: function (id) {
                return http.delete(`${API_PREFIX}/training/questions/${id}`);
            }
        },

        // 答题记录模块
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
            // 【考试模块-18】生成考试试卷
            generate: function (questionCount = 25) {
                return http.get(`${API_PREFIX}/exam/paper`, { count: questionCount });
            },

            // 【考试模块-19】提交考试答案
            submit: function (userId, answers) {
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

            // 【考试模块-20】获取考试历史
            getHistory: function (params = {}) {
                return http.get(`${API_PREFIX}/exam/history`, params).catch(() => Promise.resolve([]));
            },

            // 【考试模块-21】获取考试详情
            getDetail: function (examId) {
                return http.get(`${API_PREFIX}/exam/${examId}`).catch(() => Promise.resolve(null));
            },

            // 【考试模块-22】获取考试排行榜
            getLeaderboard: function (type = 'all', limit = 10) {
                return http.get(`${API_PREFIX}/exams/leaderboard`, { type, limit }).catch(() => Promise.resolve([]));
            }
        },

        // 用户相关接口
        user: {
            // API规范中尚未规定
            getWrongQuestions: function (userId) {
                return http.get(`${API_PREFIX}/user/${userId}/wrong`).catch(() => Promise.resolve([]));
            },

            // 【统计模块-23】获取用户统计数据
            getStats: function (userId) {
                return http.get(`${API_PREFIX}/stats/user/${userId || ''}`).catch(() => Promise.resolve({}));
            }
        },

        // 系统工具接口
        system: {
            // 【工具接口-35】系统健康检查
            healthCheck: function () {
                return http.get(`${API_PREFIX}/ping`, {}, { showLoading: false })
                    .then(data => {
                        return {
                            status: data.status || 'UP',
                            timestamp: new Date().toISOString(),
                            clientTime: Date.now()
                        };
                    })
                    .catch(() => Promise.resolve({
                        status: 'DOWN',
                        timestamp: new Date().toISOString()
                    }));
            },

            // 【工具接口-36】获取系统信息
            getInfo: function () {
                return http.get(`${API_PREFIX}/system/info`, {}, { showLoading: false }).catch(() => Promise.resolve({}));
            },

            // API规范中尚未规定
            getTime: function () {
                return http.get(`${API_PREFIX}/system/time`, {}, { showLoading: false }).catch(() => Promise.resolve(null));
            },

            // API规范中尚未规定
            sendFeedback: function (type, content, contact = '') {
                return http.post(`${API_PREFIX}/system/feedback`, { type, content, contact }).catch(() => Promise.resolve({ success: false }));
            }
        },

        upload: {
            // 【文件上传模块-29】上传题目图片
            questionImage: function (file) {
                return http.upload(`${API_PREFIX}/upload/question-image`, file);
            },
            // 【文件上传模块-30】上传用户头像
            avatar: function (file) {
                return http.upload(`${API_PREFIX}/upload/avatar`, file);
            }
        },

        export: {
            // 【导出模块-33】导出答题记录
            answers: function (params = {}) {
                return http.post(`${API_PREFIX}/export/answers`, params).catch(() => Promise.resolve({}));
            },
            // 【导出模块-34】导出考试报告
            examReport: function (examId) {
                return http.get(`${API_PREFIX}/export/exam-report/${examId}`).catch(() => Promise.resolve(null));
            },
            // API规范中尚未规定
            userStats: function (params = {}) {
                return http.post(`${API_PREFIX}/export/user-stats`, params).catch(() => Promise.resolve({}));
            }
        }
    };

    console.log('API接口已初始化');
})();