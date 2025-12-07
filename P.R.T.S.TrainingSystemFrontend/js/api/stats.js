(function () {
    'use strict';

    // 检查依赖
    if (typeof http === 'undefined') {
        console.error('请先加载 request.js');
        return;
    }
    // 统计 API
    window.statsApi = {
        // 【统计模块-23】获取用户答题统计
        getUserStats: function () {
            return http.get('/stats/user');
        },

        // 【统计模块-24】获取题目统计
        getQuestionStats: function (questionId) {
            return http.get(`/stats/question/${questionId}`);
        },

        // 【统计模块-25】获取系统统计（管理员操作）
        getSystemStats: function () {
            return http.get('/stats/system');
        }
    };

})();