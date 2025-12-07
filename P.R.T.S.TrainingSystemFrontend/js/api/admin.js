(function () {
    'use strict';

    if (typeof http === 'undefined') {
        console.error('请先加载 request.js');
        return;
    }

    window.adminApi = {
        // 【系统管理模块（管理员）-26】获取所有用户
        getUsers: function (params = {}) {
            return http.get('/admin/users', {
                page: params.page || 1,
                size: params.size || 20,
                role: params.role,
                status: params.status
            });
        },

        // 【系统管理模块（管理员）-27】修改用户信息
        updateUser: function (userId, userData) {
            return http.put(`/admin/users/${userId}`, userData);
        },

        // 【系统管理模块（管理员）-28.1】获取系统配置
        getConfig: function () {
            return http.get('/admin/config');
        },

        // 【系统管理模块（管理员）-28.2】更新系统配置
        updateConfig: function (config) {
            return http.put('/admin/config', config);
        },

        // 批量操作
        batchDeleteQuestions: function (questionIds) {
            return http.post('/admin/questions/batch-delete', { ids: questionIds });
        }
    };
})();