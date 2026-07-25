window._appMethods1 = {
    // ============ 用户认证相关方法 ============
    async checkLoginStatus() {
        const token = localStorage.getItem('token') || sessionStorage.getItem('token');
        if (!token) {
            this.isLoggedIn = false;
            this.userInfo = {};
            this.isAdmin = false;
            return false;
        }

        try {
            if (!window.userApi || typeof userApi.getCurrentUser !== 'function') {
                throw new Error('用户接口未初始化');
            }
            // 只请求一次 profile，同时完成 Token 校验和用户资料恢复。
            const user = await userApi.getCurrentUser();
            if (!user || user.id == null) throw new Error('登录状态无效');
            this.userInfo = user;
            this.isAdmin = user.isAdmin === true;
            this.isLoggedIn = true;
            return true;
        } catch (error) {
            // 失效 Token 必须立即清除，避免后续公共请求继续携带旧身份。
            if (window.api && typeof window.api.clearAuth === 'function') {
                window.api.clearAuth();
            }
            this.isLoggedIn = false;
            this.userInfo = {};
            this.isAdmin = false;
            return false;
        }
    },
    async handleLogin() {
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
            // 登录成功后只加载一次用户私有数据，不再依赖 watcher 或整页刷新。
            await this.loadUserData();
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
        await userApi.logout();
        this.isLoggedIn = false;
        this.userInfo = {};
        this.isAdmin = false;
        this.wrongQuestions = [];
        this.wrongQuestionsDetail = [];
        this.trainingRecords = {};
        this.examStats = { totalAttempts: 0, averageScore: 0 };
        this.answerSettings = { autoSubmit: false, autoNextCorrect: false };
        if (typeof this.resetNotificationUi === 'function') {
            this.resetNotificationUi();
        }
    },
    async loadUserData() {
        if (!this.isLoggedIn) return;
        // 私有数据彼此独立：单项失败只记录日志，不触发全局错误弹窗，
        // 也不能阻断其他数据加载。
        const tasks = [
            this.loadWrongQuestions(),
            this.loadExamStats(),
            this.loadAnswerSettings(),
            this.loadTrainingRecords()
        ];
        const results = await Promise.allSettled(tasks);
        results.forEach((result, index) => {
            if (result.status === 'rejected') {
                const names = ['错题', '考试统计', '答题设置', '培训记录'];
                console.warn(`${names[index]}加载失败`, result.reason);
            }
        });
    },
    async loadTrainingRecords() {
        if (window.trainingRecordsApi && typeof window.trainingRecordsApi.get === 'function') {
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
