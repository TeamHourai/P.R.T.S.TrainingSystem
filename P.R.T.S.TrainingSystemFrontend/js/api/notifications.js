(function () {
    'use strict';

    if (typeof http === 'undefined') {
        console.error('请先加载 request.js');
        return;
    }

    // 通知类型映射
    const notificationTypes = {
        system: { name: '系统通知', icon: '🔔', color: '#2196F3' },
        exam: { name: '考试通知', icon: '📝', color: '#4CAF50' },
        answer: { name: '答题反馈', icon: '✅', color: '#FF9800' },
        warning: { name: '系统提醒', icon: '⚠️', color: '#FF5722' },
        update: { name: '更新通知', icon: '🔄', color: '#9C27B0' },
        reward: { name: '奖励通知', icon: '🎁', color: '#E91E63' }
    };

    // 通知管理 API
    window.notificationApi = {
        // 【通知模块-31】获取通知
        getNotifications: function (params = {}) {
            return http.get('/notifications', {
                // 新参数名：unread=true 表示仅未读
                unread: !!params.unread,
                page: params.page || 1,
                size: params.size || 20,
                type: params.type
            });
        },

        // 【通知模块-32】标记通知已读
        markAsRead: function (notificationId) {
            return http.put(`/notifications/${notificationId}/read`);
        },

        // 【UPD1.1-1】标记所有通知已读
        markAllAsRead: function () {
            return http.put('/notifications/read-all');
        },

        // 【UPD1.1-2】删除单个通知
        deleteNotification: function (notificationId) {
            return http.delete(`/notifications/${notificationId}`);
        },

        // 【UPD1.1-3】清空所有通知
        clearAll: function () {
            return http.delete('/notifications');
        },

        // 【UPD1.1-4】获取未读通知数量
        getUnreadCount: function () {
            return http.get('/notifications/unread-count');
        }
    };

    // 通知辅助函数
    window.notificationHelper = {
        // 格式化通知显示
        formatNotification: function (notification) {
            if (!notification) return null;

            const typeInfo = notificationTypes[notification.type] || notificationTypes.system;

            return {
                ...notification,
                typeInfo: typeInfo,
                formattedTime: this.formatTime(notification.createdAt),
                formattedContent: this.formatContent(notification.content, notification.type),
                badgeColor: typeInfo.color,
                badgeIcon: typeInfo.icon
            };
        },

        // 格式化时间
        formatTime: function (timestamp) {
            if (!timestamp) return '';

            const date = new Date(timestamp);
            const now = new Date();
            const diffMs = now - date;
            const diffMins = Math.floor(diffMs / (1000 * 60));
            const diffHours = Math.floor(diffMs / (1000 * 60 * 60));
            const diffDays = Math.floor(diffMs / (1000 * 60 * 60 * 24));

            if (diffMins < 1) return '刚刚';
            if (diffMins < 60) return `${diffMins}分钟前`;
            if (diffHours < 24) return `${diffHours}小时前`;
            if (diffDays < 7) return `${diffDays}天前`;

            // 超过7天显示具体日期
            const month = (date.getMonth() + 1).toString().padStart(2, '0');
            const day = date.getDate().toString().padStart(2, '0');
            return `${month}-${day}`;
        },

        // 格式化内容
        formatContent: function (content, type) {
            if (!content) return '';

            // 根据类型添加样式
            let formatted = content.replace(/\n/g, '<br>');

            switch (type) {
                case 'exam':
                    formatted = formatted.replace(/(考试|成绩|分数|排名)/g, '<strong>$1</strong>');
                    break;
                case 'reward':
                    formatted = formatted.replace(/(奖励|积分|勋章|成就)/g, '<strong class="text-reward">$1</strong>');
                    break;
                case 'warning':
                    formatted = `<span class="text-warning">${formatted}</span>`;
                    break;
            }

            return formatted;
        },

        // 创建本地通知
        createLocalNotification: function (title, content, type = 'system', options = {}) {
            const notification = {
                id: 'local_' + Date.now() + '_' + Math.random().toString(36).substr(2, 9),
                type: type,
                title: title,
                content: content,
                isRead: false,
                createdAt: new Date().toISOString(),
                expiresAt: options.expiresAt,
                actionUrl: options.actionUrl,
                actionText: options.actionText,
                priority: options.priority || 'normal',
                isLocal: true
            };

            // 保存到本地存储
            this.saveLocalNotification(notification);

            // 显示桌面通知（如果浏览器支持且用户已授权）
            this.showDesktopNotification(title, content);

            return notification;
        },

        // 保存到本地存储
        saveLocalNotification: function (notification) {
            try {
                const key = 'local_notifications';
                const notifications = JSON.parse(localStorage.getItem(key) || '[]');

                // 保留最新的50条
                notifications.unshift(notification);
                if (notifications.length > 50) {
                    notifications.splice(50);
                }

                localStorage.setItem(key, JSON.stringify(notifications));
            } catch (error) {
                console.error('保存本地通知失败:', error);
            }
        },

        // 获取本地通知
        getLocalNotifications: function () {
            try {
                const key = 'local_notifications';
                return JSON.parse(localStorage.getItem(key) || '[]');
            } catch (error) {
                console.error('获取本地通知失败:', error);
                return [];
            }
        },

        // 清除过期的本地通知
        clearExpiredNotifications: function () {
            try {
                const key = 'local_notifications';
                const notifications = JSON.parse(localStorage.getItem(key) || '[]');
                const now = new Date();

                const validNotifications = notifications.filter(notif => {
                    if (!notif.expiresAt) return true;
                    return new Date(notif.expiresAt) > now;
                });

                localStorage.setItem(key, JSON.stringify(validNotifications));
            } catch (error) {
                console.error('清除过期通知失败:', error);
            }
        },

        // 显示桌面通知
        showDesktopNotification: function (title, body) {
            // 检查浏览器支持
            if (!('Notification' in window)) {
                return;
            }

            // 检查权限
            if (Notification.permission === 'granted') {
                new Notification(title, {
                    body: body,
                    icon: '/favicon.ico'
                });
            } else if (Notification.permission !== 'denied') {
                // 请求权限
                Notification.requestPermission().then(permission => {
                    if (permission === 'granted') {
                        new Notification(title, {
                            body: body,
                            icon: '/favicon.ico'
                        });
                    }
                });
            }
        },

        // 合并服务器和本地通知
        mergeNotifications: function (serverNotifications, localNotifications) {
            const allNotifications = [...serverNotifications];

            // 添加本地通知，避免重复
            localNotifications.forEach(localNotif => {
                if (!allNotifications.some(serverNotif => serverNotif.id === localNotif.id)) {
                    allNotifications.push(localNotif);
                }
            });

            // 按时间排序
            allNotifications.sort((a, b) => {
                return new Date(b.createdAt) - new Date(a.createdAt);
            });

            return allNotifications;
        }
    };

    // 自动清除过期通知
    setInterval(() => {
        window.notificationHelper.clearExpiredNotifications();
    }, 3600000); // 每小时检查一次

})();