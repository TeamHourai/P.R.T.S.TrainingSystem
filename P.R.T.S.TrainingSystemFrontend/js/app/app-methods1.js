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
            this.showSuccess('登录成功！');
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
        this.showSuccess('已退出登录');
    },
    async loadUserData() {
        // ...existing code...
        await this.loadWrongQuestions();
        await this.loadExamStats();
    }
};

