(function () {
    'use strict';

    // 如果全局 http 未定义，创建一个最小回退实现，避免中断后续逻辑
    const httpFallback = (function () {
        function buildQuery(params) {
            if (!params) return '';
            return Object.keys(params).map(k => encodeURIComponent(k) + '=' + encodeURIComponent(params[k])).join('&');
        }
        return {
            get: function (url, params) {
                try {
                    let full = url;
                    if (params) {
                        const q = buildQuery(params);
                        if (q) full += (full.indexOf('?') === -1 ? '?' : '&') + q;
                    }
                    return fetch(full, { method: 'GET', mode: 'cors' }).then(async resp => {
                        const text = await resp.text();
                        if (!resp.ok) throw new Error(text || resp.statusText);
                        try { return JSON.parse(text); } catch (e) { return text; }
                    });
                } catch (e) { return Promise.reject(e); }
            },
            post: function (url, bodyObj) {
                try {
                    const body = (bodyObj instanceof URLSearchParams) ? bodyObj.toString() :
                        (typeof bodyObj === 'string' ? bodyObj : new URLSearchParams(bodyObj).toString());
                    return fetch(url, {
                        method: 'POST',
                        mode: 'cors',
                        headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
                        body: body
                    }).then(async resp => {
                        const text = await resp.text();
                        if (!resp.ok) throw new Error(text || resp.statusText);
                        try { return JSON.parse(text); } catch (e) { return text; }
                    });
                } catch (e) { return Promise.reject(e); }
            }
        };
    })();

    // 优先使用全局 http（request.js 提供），否则使用回退
    const _http = (typeof http !== 'undefined') ? http : httpFallback;

    // 统一API前缀
    const BASE = ((window.API_BASE_URL && String(window.API_BASE_URL)) || 'http://localhost:8080').replace(/\/+$/, '');
    const API_PREFIX = BASE + '/api/v1';

    // 考试管理 API
    window.examApi = {
        // 【考试模块-18】生成考试试卷
        generateExamPaper: function (questionCount = 25) {
            // 优先调用后端接口，失败时降级到旧接口，再失败时使用本地题库（回退）
            return _http.get(`${API_PREFIX}/exam/paper`, { count: questionCount }).catch(() => {
                return _http.post(`${BASE}/exams/generate`, { questionCount }).catch(() => {
                    try {
                        const local = window.questions || window.questionBank || window.__questions || window.localQuestions;
                        if (Array.isArray(local) && local.length > 0) {
                            const shuffled = local.slice();
                            for (let i = shuffled.length - 1; i > 0; i--) {
                                const j = Math.floor(Math.random() * (i + 1));
                                [shuffled[i], shuffled[j]] = [shuffled[j], shuffled[i]];
                            }
                            return Promise.resolve(shuffled.slice(0, questionCount));
                        }
                    } catch (e) { /* ignore */ }
                    return Promise.resolve([]);
                });
            });
        },

        // 【考试模块-19】提交考试答案
        submitExamAnswers: function (userId, answers) {
            let answersStr = '';
            if (typeof answers === 'string') answersStr = answers;
            else if (Array.isArray(answers)) answersStr = answers.join(',');
            else if (typeof answers === 'object') answersStr = Object.keys(answers).map(k => `${k}:${answers[k]}`).join(',');

            const body = new URLSearchParams();
            if (userId) body.append('userId', userId);
            body.append('answers', answersStr);

            return _http.post(`${API_PREFIX}/exam/submit`, body).catch(err => {
                console.error('提交试卷失败:', err);
                return Promise.reject(err);
            });
        },

        // 【考试模块-20】获取考试历史
        getExamHistory: function (params = {}) {
            return _http.get(`${API_PREFIX}/exam/history`, { page: params.page || 1, size: params.size || 10 }).catch(() => Promise.resolve([]));
        },

        // 【考试模块-21】获取考试详情
        getExamResult: function (examId) {
            return _http.get(`${API_PREFIX}/exam/${examId}`).catch(() => Promise.resolve(null));
        },

        // 【考试模块-22】获取考试排行榜
        getExamLeaderboard: function (type = 'all', limit = 10) {
            return _http.get(`${API_PREFIX}/exams/leaderboard`, { type, limit }).catch(() => Promise.resolve([]));
        }
    };

})();