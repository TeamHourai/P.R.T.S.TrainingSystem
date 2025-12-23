(function () {
    'use strict';

    if (typeof http === 'undefined') {
        console.error('请先加载 request.js');
        return;
    }

    // 统一API前缀
    const BASE = ((window.API_BASE_URL && String(window.API_BASE_URL)) || 'http://localhost:8080').replace(/\/+$/, '');
    const API_PREFIX = BASE + '/api/v1';

    function getToken() {
        return (window.localStorage && (localStorage.getItem('token') || localStorage.getItem('auth_token'))) ||
            (window.sessionStorage && (sessionStorage.getItem('token') || sessionStorage.getItem('auth_token'))) ||
            null;
    }

    function getUserIdFromStorage() {
        // 优先从 authManager 取
        if (window.authManager && typeof window.authManager.getUserId === 'function') {
            const id = window.authManager.getUserId();
            if (id) return id;
        }
        // 兼容直接存 userInfo
        try {
            const raw = (localStorage.getItem('userInfo') || sessionStorage.getItem('userInfo') || localStorage.getItem('user_info') || sessionStorage.getItem('user_info'));
            if (!raw) return null;
            const u = JSON.parse(raw);
            return u && (u.id || u.userId) ? (u.id || u.userId) : null;
        } catch (e) {
            return null;
        }
    }

    // 答题记录 API
    window.answerApi = {
        // 【答题记录模块-14】提交答案
        // submitAnswer(questionId, questionType, selectedOption, examId = null, userId = null, timeSpent = 0)
        submitAnswer: function (questionId, questionType, selectedOption, examId = null, userId = null, timeSpent = 0) {
            // 自动补齐 userId
            if (!userId) {
                userId = getUserIdFromStorage();
            }
            if (!userId) {
                return Promise.reject(new Error('请先登录后提交答案'));
            }

            const answersStr = `${questionId}:${selectedOption}`;
            const body = new URLSearchParams();
            body.append('userId', userId);
            body.append('answers', answersStr);
            if (examId) body.append('examId', examId);
            if (timeSpent) body.append('timeSpent', String(timeSpent));

            return fetch(API_PREFIX + '/exam/submit', {
                method: 'POST',
                mode: 'cors',
                headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
                body: body.toString()
            }).then(async resp => {
                const text = await resp.text();
                if (!resp.ok) throw new Error(text || resp.statusText);
                try { return JSON.parse(text); } catch (e) { return text; }
            });
        },

        // 【答题记录模块-15】获取答题历史
        getAnswerRecords: function (params = {}) {
            const query = {
                page: params.page || 1,
                size: params.size || 20,
                questionType: params.questionType,
                startDate: params.startDate,
                endDate: params.endDate
            };
            Object.keys(query).forEach(k => query[k] === undefined && delete query[k]);

            return http.get(API_PREFIX + '/exam/history', query).catch(() => Promise.resolve([]));
        },

        // 【答题记录模块-16】获取错题本
        getWrongQuestions: function (params = {}) {
            const query = {
                page: params.page || 1,
                size: params.size || 20,
                questionType: params.questionType,
                startDate: params.startDate,
                endDate: params.endDate
            };
            Object.keys(query).forEach(k => query[k] === undefined && delete query[k]);

            // 当前后端实现一般通过 token 解析 userId（Bearer user-{id}）
            const token = getToken();
            const headers = token ? { 'Authorization': 'Bearer ' + token } : undefined;

            // 兼容：若 http.get 不支持 headers third arg，也可直接 fetch；这里优先走 request.js 的 http
            return http.get(API_PREFIX + '/answers/wrong', query, headers ? { headers } : undefined)
                .then(resp => {
                    if (resp && resp.code === 200 && resp.data) return resp.data;
                    if (Array.isArray(resp)) return resp;
                    return { history: [], total: 0, page: 1, size: 20, pages: 1 };
                })
                .catch(() => Promise.resolve({ history: [], total: 0, page: 1, size: 20, pages: 1 }));
        },

        // 【答题记录模块-17】从错题本移除题目
        removeWrongQuestion: function (questionId) {
            const token = getToken();
            if (!token) return Promise.reject(new Error('请先登录'));

            // Prefer shared http client so Authorization header interceptor always applies.
            return http.delete(API_PREFIX + '/answers/wrong/' + questionId)
                .then(resp => {
                    if (resp && resp.success === true) return { success: true };
                    // Some wrappers may return raw text/object
                    if (resp && resp.code === 200) return { success: true };
                    return { success: false, resp };
                })
                .catch(() => ({ success: false }));
        }
    };

})();