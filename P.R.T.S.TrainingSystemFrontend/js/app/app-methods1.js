window._appMethods1 = {
    // ============ 用户认证相关方法 ============
    async checkLoginStatus() {
        // ...existing code...
        const token = localStorage.getItem('token') || sessionStorage.getItem('token');
        if (!token) {
            this.isLoggedIn = false;
            return;
        }
        if (window.userApi && typeof userApi.checkLoginStatus === 'function') {
            const isLoggedIn = await userApi.checkLoginStatus();
            this.isLoggedIn = isLoggedIn;
            if (isLoggedIn) {
                const user = await userApi.getCurrentUser();
                if (user) {
                    this.userInfo = user;
                    this.isAdmin = user.isAdmin || false;
                }
            }
        } else {
            const userData = localStorage.getItem('userInfo') || sessionStorage.getItem('userInfo');
            this.isLoggedIn = !!userData;
            if (userData) {
                this.userInfo = JSON.parse(userData);
                this.isAdmin = this.userInfo.isAdmin || false;
            }
        }
    },
    async handleLogin() {
        // ...existing code...
        if (!this.authUsername || !this.authPassword) {
            this.showError('请输入用户名和密码');
            return;
        }
        const result = await userApi.login(this.authUsername, this.authPassword, true);
        if (result.success) {
            this.isLoggedIn = true;
            this.userInfo = result.user || {};
            this.isAdmin = result.isAdmin || false;
            this.showAuthModal = false;
            this.authUsername = '';
            this.authPassword = '';
            await this.loadUserData();

            // 登录后拉取入职培训记录
            if (window.trainingRecordsApi && typeof window.trainingRecordsApi.get === 'function') {
                try {
                    const res = await window.trainingRecordsApi.get();
                    if (res && res.success && res.records) {
                        const mapped = {};
                        Object.keys(res.records).forEach(k => {
                            const r = res.records[k];
                            mapped[k] = {
                                attempts: r.attempts || 0,
                                correct: !!r.correct,
                                lastAt: r.lastAt || 0
                            };
                        });
                        this.trainingRecords = mapped;
                    }
                } catch (e) {
                    console.warn('登录后加载 trainingRecords 失败', e);
                }
            }

            // 登录后自动刷新页面（防止状态不同步）
            try {
                sessionStorage.setItem('__refresh_after_login__', String(Date.now()));
                window.location.reload();
                return;
            } catch (e) {
                // ignore
            }

            // 登录成功不弹窗
            return;
        } else {
            this.showError(result.message || '登录失败');
        }
    },
    async handleRegister() {
        // ...existing code...
        if (!this.authUsername || !this.authPassword) {
            this.showError('请输入用户名和密码');
            return;
        }
        if (this.authUsername.length < 3) {
            this.showError('用户名至少需要3个字符');
            return;
        }
        if (this.authPassword.length < 6) {
            this.showError('密码至少需要6个字符');
            return;
        }
        const result = await userApi.register(this.authUsername, this.authPassword, '');
        if (result.success) {
            this.showSuccess('注册成功！请登录');
            this.authMode = 'login';
        } else {
            this.showError(result.message || '注册失败');
        }
    },
    async handleLogout() {
        // ...existing code...
        await userApi.logout();
        this.isLoggedIn = false;
        this.userInfo = {};
        this.isAdmin = false;
        this.wrongQuestions = [];
        this.wrongQuestionsDetail = [];
        this.trainingRecords = {};

        // 清除登录公告弹窗标记，使下次登录重新检测未读公告
        try { sessionStorage.removeItem('__login_announcement_shown__'); } catch (e) { /* ignore */ }

        // 退出登录成功不弹窗

        // 退出登录后自动刷新页面
        try {
            sessionStorage.setItem('__refresh_after_logout__', String(Date.now()));
            window.location.reload();
        } catch (e) {
            // ignore
        }
    },
    async loadUserData() {
        // ...existing code...
        await this.loadWrongQuestions();
        await this.loadExamStats();

        // 登录后也刷新一次培训记录（防止外部调用 loadUserData 时遗漏）
        if (window.trainingRecordsApi && typeof window.trainingRecordsApi.get === 'function') {
            try {
                const res = await window.trainingRecordsApi.get();
                if (res && res.success && res.records) {
                    const mapped = {};
                    Object.keys(res.records).forEach(k => {
                        const r = res.records[k];
                        mapped[k] = {
                            attempts: r.attempts || 0,
                            correct: !!r.correct,
                            lastAt: r.lastAt || 0
                        };
                    });
                    this.trainingRecords = mapped;
                }
            } catch (e) {
                console.warn('loadUserData: load trainingRecords failed', e);
            }
        }
    },
    // 新增：登录弹窗输入体验
    focusAuthPassword() {
        this.$nextTick(() => {
            const el = this.$refs && this.$refs.authPasswordInput;
            if (el && typeof el.focus === 'function') el.focus();
        });
    },
};
