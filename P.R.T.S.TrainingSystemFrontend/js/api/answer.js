(function () {
    'use strict';

    if (typeof http === 'undefined') {
        console.error('请先加载 request.js');
        return;
    }

    // 答题记录 API
    window.answerApi = {
        // 【答题记录模块-14】提交答案
        // 新参数说明：submitAnswer(questionId, questionType, selectedOption, examId = null, userId = null, timeSpent = 0)
        submitAnswer: function (questionId, questionType, selectedOption, examId = null, userId = null, timeSpent = 0) {
            // 优先尝试后台 /answers 接口（如果实现）
            const payload = {
                questionId,
                questionType,
                selectedOption,
                timeSpent: timeSpent || 0
            };
            if (examId) payload.examId = examId;
            if (userId) payload.userId = userId;

            return http.post('/answers', payload).catch(async (err) => {
                // 如果 /answers 不可用，尝试将该单题以 exam/submit 格式提交（需要 userId）
                if (!userId) {
                    return Promise.reject(err);
                }
                const answersStr = `${questionId}:${selectedOption}`;
                const body = new URLSearchParams();
                body.append('userId', userId);
                body.append('answers', answersStr);
                const resp = await fetch((window.API_BASE_URL || 'http://localhost:8888') + '/exam/submit', {
                    method: 'POST',
                    mode: 'cors',
                    headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
                    body: body.toString()
                });
                const text = await resp.text();
                try { return JSON.parse(text); } catch (e) { return text; }
            });
        },

        // 【答题记录模块-15】获取答题历史
        getAnswerRecords: function (params = {}) {
            return http.get('/answers/history', {
                page: params.page || 1,
                size: params.size || 20,
                questionType: params.questionType,
                startDate: params.startDate,
                endDate: params.endDate
            }).catch(() => Promise.resolve([]));
        },

        // 【答题记录模块-16】获取错题本
        getWrongQuestions: function (params = {}) {
            return http.get('/answers/wrong', {
                page: params.page || 1,
                size: params.size || 10,
                questionType: params.questionType
            }).catch(() => Promise.resolve([]));
        },

        // 【答题记录模块-17】从错题本移除题目
        removeWrongQuestion: function (questionId) {
            return http.delete(`/answers/wrong/${questionId}`).catch(() => Promise.resolve({ success: false }));
        }
    };

})();