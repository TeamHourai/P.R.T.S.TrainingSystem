window._appWatch = {
    practiceMode() {
        this.updateCategories();
    },
    isLoggedIn(newVal) {
        if (newVal) {
            this.loadUserData();
        }
    }
    // 注：showSystemNotice / systemNoticeTab 不再在此自动 loadNotifications。
    // 弹窗加载统一由 openSystemNotice / checkLoginAnnouncements / switchNoticeTab
    // 显式控制，避免 watcher 与显式调用并发产生竞态或重复请求。
};

