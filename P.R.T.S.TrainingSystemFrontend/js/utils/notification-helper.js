/**
 * 通知/公告数据格式化助手
 * ------------------------------------------------------------
 * 后端 /api/v1/notifications 与 /api/v1/announcements 返回的是原始字段
 * （content / createdAt / type / isImportant / isRead ...），而前端模板
 * 渲染依赖 formatNotification 生成的派生字段：
 *   - formattedContent : 含换行 <br> 的安全 HTML（用于 v-html）
 *   - formattedTime    : 可读发布时间
 *   - typeInfo         : { icon, color, name } 用于列表图标
 * 该助手缺失会导致「公告内容与发布时间无法正常显示」。
 */
(function () {
    'use strict';

    // type -> { icon, color, name }
    var TYPE_MAP = {
        system:   { icon: '📢', color: '#2196F3', name: '系统通知' },
        exam:     { icon: '📝', color: '#4CAF50', name: '考试通知' },
        training: { icon: '🎓', color: '#FF9800', name: '培训通知' },
        update:   { icon: '🆕', color: '#9C27B0', name: '更新公告' },
        notice:   { icon: '📌', color: '#00BCD4', name: '通知' }
    };

    function escapeHtml(s) {
        if (s == null) return '';
        return String(s)
            .replace(/&/g, '&amp;')
            .replace(/</g, '&lt;')
            .replace(/>/g, '&gt;')
            .replace(/"/g, '&quot;')
            .replace(/'/g, '&#39;');
    }

    function formatTime(v) {
        if (!v) return '';
        // 后端 createdAt 已是 "yyyy-MM-dd HH:mm:ss"，直接展示
        return String(v);
    }

    function formatNotification(n) {
        if (!n || typeof n !== 'object') return null;
        var type = n.type || 'system';
        var info = TYPE_MAP[type] || TYPE_MAP.system;
        var content = (n.content != null) ? String(n.content) : '';
        var important = (n.isImportant != null)
            ? n.isImportant
            : (n.important === true);

        return {
            // 透传原始字段
            id: n.id,
            type: type,
            title: (n.title != null) ? String(n.title) : '',
            content: content,
            createdAt: n.createdAt,
            createdBy: n.createdBy,
            expiresAt: n.expiresAt,
            isRead: n.isRead === true,
            isImportant: important === true,
            // 派生展示字段（模板依赖）
            typeInfo: { icon: info.icon, color: info.color, name: info.name },
            formattedContent: escapeHtml(content).replace(/\r?\n/g, '<br>'),
            formattedTime: formatTime(n.createdAt)
        };
    }

    window.notificationHelper = {
        formatNotification: formatNotification,
        formatTime: formatTime
    };
})();
