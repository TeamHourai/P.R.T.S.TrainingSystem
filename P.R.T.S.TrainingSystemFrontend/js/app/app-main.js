new Vue({
    el: '#app',
    data: window._appData,
    computed: window._appComputed,
    watch: window._appWatch,
    methods: Object.assign(
        {},
        window._appMethods1,
        window._appMethods2,
        window._appMethods3,
        window._appMethods4
    ),
    async mounted() {
        // ...existing code from mounted...
        try {
            // 防止登录/退出后 reload 标记导致循环：启动后立即清理
            try {
                const k1 = sessionStorage.getItem('__refresh_after_login__');
                const k2 = sessionStorage.getItem('__refresh_after_logout__');
                if (k1 || k2) {
                    sessionStorage.removeItem('__refresh_after_login__');
                    sessionStorage.removeItem('__refresh_after_logout__');
                }
            } catch (e) {
                // ignore
            }

            console.log('博士考核系统初始化...');
            await this.checkLoginStatus();
            // 登录后自动检测未读公告（有未读则弹窗，每会话一次）
            if (this.isLoggedIn && typeof this.checkLoginAnnouncements === 'function') {
                this.checkLoginAnnouncements();
            }
            // 登录后拉取用户答题设置
            if (typeof this.loadAnswerSettings === 'function') {
                await this.loadAnswerSettings();
            }
            await this.loadQuestions();
            await this.loadTrainingQuestions();

            // 从后端加载入职培训记录（替代 localStorage）
            if (this.isLoggedIn && window.trainingRecordsApi && typeof window.trainingRecordsApi.get === 'function') {
                try {
                    const res = await window.trainingRecordsApi.get();
                    if (res && res.success && res.records) {
                        // 前端内部结构为 { [id]: { attempts, correct, lastAt } }
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
                    console.warn('加载 trainingRecords 失败', e);
                }
            }

            this.updateCategories();
            await this.loadExamStats();
            this.loadSystemData();
            console.log('系统初始化完成');
            document.addEventListener('click', (event) => {
                const sidebar = document.querySelector('.sidebar');
                const menuToggle = document.querySelector('.mobile-menu-toggle');
                if (this.sidebarOpen &&
                    sidebar &&
                    menuToggle &&
                    !sidebar.contains(event.target) &&
                    !menuToggle.contains(event.target)) {
                    this.sidebarOpen = false;
                }
            });
        } catch (error) {
            console.error('应用初始化失败:', error);
            // 限流（429）已在请求层给出区分性提示，此处不再重复弹「初始化失败」
            if (error && error.rateLimited) return;
            this.showError('系统初始化失败，请刷新页面重试');
        }
    }
});
