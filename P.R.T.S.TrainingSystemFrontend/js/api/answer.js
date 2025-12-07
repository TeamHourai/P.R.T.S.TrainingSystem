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
            // 直接以 form 表单提交到 /exam/submit，后端 parseForm 接受该格式
            if (!userId) {
                return Promise.reject(new Error('请先登录后提交答案'));
            }
            const answersStr = `${questionId}:${selectedOption}`;
            const body = new URLSearchParams();
            body.append('userId', userId);
            body.append('answers', answersStr);
            // 若有 examId 或 timeSpent 可附加
            if (examId) body.append('examId', examId);
            if (timeSpent) body.append('timeSpent', String(timeSpent));

            return fetch((window.API_BASE_URL || 'http://localhost:8888') + '/exam/submit', {
                method: 'POST',
                mode: 'cors',
                headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
                body: body.toString()
            }).then(async resp => {
                const text = await resp.text();
                if (!resp.ok) throw new Error(text || resp.statusText);
                try { return JSON.parse(text); } catch (e) { return text; }
            }).catch(err => Promise.reject(err));
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