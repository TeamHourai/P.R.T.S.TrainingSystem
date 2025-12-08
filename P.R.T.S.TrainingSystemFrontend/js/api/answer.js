(function () {
    'use strict';

    if (typeof http === 'undefined') {
        console.error('请先加载 request.js');
        return;
    }

    // 统一API前缀
    const BASE = ((window.API_BASE_URL && String(window.API_BASE_URL)) || 'http://localhost:8888').replace(/\/+$/, '');
    const API_PREFIX = BASE + '/api/v1';

    // 答题记录 API
    window.answerApi = {
        // 【答题记录模块-14】提交答案
        submitAnswer: function (questionId, questionType, selectedOption, examId = null, timeSpent = 0) {
            const token = window.localStorage && window.localStorage.getItem('token');
            if (!token) return Promise.reject(new Error('请先登录后提交答案'));

            const body = {
                questionId,
                questionType,
                selectedOption,
                examId: examId || null
                // timeSpent 可选，API规范建议有余力再实现
            };
            if (typeof timeSpent === 'number' && timeSpent > 0) body.timeSpent = timeSpent;

            return fetch(`${API_PREFIX}/answers`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': 'Bearer ' + token
                },
                body: JSON.stringify(body)
            }).then(async resp => {
                const text = await resp.text();
                if (!resp.ok) throw new Error(text || resp.statusText);
                try { return JSON.parse(text); } catch (e) { return text; }
            });
        },

        // 【答题记录模块-15】获取答题历史
        getAnswerRecords: function (params = {}) {
            const token = window.localStorage && window.localStorage.getItem('token');
            if (!token) return Promise.reject(new Error('请先登录'));

            // 支持分页及筛选参数
            const query = {
                page: params.page || 1,
                size: params.size || 20,
                questionType: params.questionType,
                startDate: params.startDate,
                endDate: params.endDate
            };
            // 移除未定义参数
            Object.keys(query).forEach(k => query[k] === undefined && delete query[k]);

            return http.get(`${API_PREFIX}/answers/history`, query, {
                headers: { 'Authorization': 'Bearer ' + token }
            }).then(resp => {
                // 规范返回 data.history
                if (resp && resp.code === 200 && resp.data) return resp.data;
                return { history: [], total: 0, page: 1, size: 20, pages: 1 };
            }).catch(() => Promise.resolve({ history: [], total: 0, page: 1, size: 20, pages: 1 }));
        },

        // 【答题记录模块-16】获取错题本
        getWrongQuestions: function (params = {}) {
            const token = window.localStorage && window.localStorage.getItem('token');
            if (!token) return Promise.reject(new Error('请先登录'));

            const query = {
                page: params.page || 1,
                size: params.size || 20,
                questionType: params.questionType,
                startDate: params.startDate,
                endDate: params.endDate
            };
            Object.keys(query).forEach(k => query[k] === undefined && delete query[k]);

            return http.get(`${API_PREFIX}/answers/wrong`, query, {
                headers: { 'Authorization': 'Bearer ' + token }
            }).then(resp => {
                if (resp && resp.code === 200 && resp.data) return resp.data;
                return { history: [], total: 0, page: 1, size: 20, pages: 1 };
            }).catch(() => Promise.resolve({ history: [], total: 0, page: 1, size: 20, pages: 1 }));
        },

        // 【答题记录模块-17】从错题本移除题目
        removeWrongQuestion: function (questionId) {
            const token = window.localStorage && window.localStorage.getItem('token');
            if (!token) return Promise.reject(new Error('请先登录'));

            return fetch(`${API_PREFIX}/answers/wrong/${questionId}`, {
                method: 'DELETE',
                headers: { 'Authorization': 'Bearer ' + token }
            }).then(async resp => {
                const text = await resp.text();
                if (!resp.ok) throw new Error(text || resp.statusText);
                try { return JSON.parse(text); } catch (e) { return text; }
            }).then(resp => {
                if (resp && resp.code === 200) return { success: true };
                return { success: false };
            }).catch(() => ({ success: false }));
        }
    };

})();