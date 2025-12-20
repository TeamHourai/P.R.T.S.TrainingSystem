(function () {
    'use strict';

    if (typeof http === 'undefined') {
        console.error('请先加载 request.js');
        return;
    }

    const API_PREFIX = ((window.API_BASE_URL && String(window.API_BASE_URL)) || 'http://localhost:8080').replace(/\/+$/, '') + '/api/v1';

    window.keywordApi = {
        // 获取全局关键词列表，可传 mode=onboarding
        getAll: function (params = {}) {
            return http.get(`${API_PREFIX}/keywords`, params).then(resp => resp && resp.data ? resp.data : resp);
        }
    };
})();

