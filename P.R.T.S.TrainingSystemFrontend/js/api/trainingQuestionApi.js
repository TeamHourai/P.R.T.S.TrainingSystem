(function () {
    'use strict';

    if (typeof http === 'undefined') {
        console.error('请先加载 request.js');
        return;
    }

    // 使用 questionApi 中的 normalizeQuestion 如果存在
    function normalize(q) {
        if (window.questionApi && typeof window.questionApi === 'object' && typeof window.questionApi.getQuestions === 'function') {
            // reuse fetchJsonUtf8 via questionApi.getQuestions isn't exposed; fallback to basic normalization
        }
        // basic normalization similar to question.js
        q = q || {};
        q.options = Array.isArray(q.options) ? q.options : (typeof q.options === 'string' ? q.options.split('|') : []);
        while (q.options.length < 4) q.options.push('');
        q.answer = q.answer ? parseInt(q.answer) : 0;
        q.type = q.type ? Number(q.type) : 0;
        q.difficulty = q.difficulty ? Number(q.difficulty) : 0;
        return q;
    }

    window.trainingQuestionApi = {
        getTrainingQuestions: function (params = {}) {
            const page = params.page || 1;
            const size = params.size || 20;
            const keyword = params.keyword || '';
            // call standard questions endpoint with mode=onboarding
            const url = (window.API_BASE_URL || 'http://localhost:8080').replace(/\/+$/, '') + '/api/v1/questions';
            const fullUrl = new URL(url);
            fullUrl.searchParams.set('page', page);
            fullUrl.searchParams.set('size', size);
            if (keyword) fullUrl.searchParams.set('keyword', keyword);
            fullUrl.searchParams.set('mode', 'onboarding');

            return fetch(fullUrl.toString(), { method: 'GET', mode: 'cors' })
                .then(resp => {
                    if (!resp.ok) return resp.text().then(t => { throw new Error(t || resp.statusText); });
                    return resp.arrayBuffer();
                })
                .then(buf => {
                    const text = new TextDecoder('utf-8').decode(buf);
                    const data = JSON.parse(text);
                    if (Array.isArray(data)) return data.map(normalize);
                    return data;
                });
        }
    };
})();

