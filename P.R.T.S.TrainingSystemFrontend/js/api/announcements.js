(function () {
    'use strict';

    if (typeof http === 'undefined') {
        console.error('请先加载 request.js');
        return;
    }

    // 系统公告 API
    window.announcementApi = {
        // 公告列表（所有用户可读）
        getAnnouncements(params = {}) {
            return http.get('/api/v1/announcements', {
                page: params.page || 1,
                size: params.size || 50
            });
        },

        // 发布公告（管理员）
        createAnnouncement(payload) {
            return http.post('/api/v1/admin/announcements', payload);
        }
    };
})();

