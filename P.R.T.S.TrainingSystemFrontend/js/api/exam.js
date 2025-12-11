(function () {
    'use strict';

    if (typeof http === 'undefined') {
        console.error('请先加载 request.js');
        return;
    }

    // 统一API前缀
    const BASE = ((window.API_BASE_URL && String(window.API_BASE_URL)) || 'http://localhost:8080').replace(/\/+$/, '');
    const API_PREFIX = BASE + '/api/v1';

    // 考试管理 API
    window.examApi = {
        // 【考试模块-18】生成考试试卷
        generateExamPaper: function (questionCount = 25) {
            return http.get(`${API_PREFIX}/exam/paper`, { count: questionCount }).catch(() => {
                // 兼容旧接口
                return http.post('/exams/generate', { questionCount }).catch(() => Promise.resolve([]));
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

            return fetch(`${API_PREFIX}/exam/submit`, {
                method: 'POST',
                mode: 'cors',
                headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
                body: body.toString()
            }).then(async resp => {
                const text = await resp.text();
                if (!resp.ok) throw new Error(text || resp.statusText);
                try { return JSON.parse(text); } catch (e) { return text; }
            }).catch(err => {
                console.error('提交试卷失败:', err);
                return Promise.reject(err);
            });
        },

        // 【考试模块-20】获取考试历史
        getExamHistory: function (params = {}) {
            return http.get(`${API_PREFIX}/exam/history`, { page: params.page || 1, size: params.size || 10 }).catch(() => Promise.resolve([]));
        },

        // 【考试模块-21】获取考试详情
        getExamResult: function (examId) {
            return http.get(`${API_PREFIX}/exam/${examId}`).catch(() => Promise.resolve(null));
        },

        // 【考试模块-22】获取考试排行榜
        getExamLeaderboard: function (type = 'all', limit = 10) {
            return http.get(`${API_PREFIX}/exams/leaderboard`, { type, limit }).catch(() => Promise.resolve([]));
        }
    };

})();