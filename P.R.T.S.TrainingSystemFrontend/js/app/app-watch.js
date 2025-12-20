window._appWatch = {
    practiceMode() {
        this.updateCategories();
    },
    isLoggedIn(newVal) {
        if (newVal) {
            this.loadUserData();
        }
    },
    showSystemNotice(val) {
        if (val) {
            this.loadNotifications();
        }
    },
    systemNoticeTab() {
        this.noticePage = 1;
        this.loadNotifications();
    }
};

