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
            // 弹窗属于瞬时 UI，不能跨页面导航或 BFCache 恢复。
            // pagehide 在页面进入往返缓存前执行；pageshow.persisted 则覆盖浏览器
            // 直接恢复旧 Vue 实例、不会重新执行 mounted 的情况。
            const resetNotificationUi = () => {
                if (typeof this.resetNotificationUi === 'function') {
                    this.resetNotificationUi();
                }
            };
            resetNotificationUi();
            window.addEventListener('pagehide', resetNotificationUi);
            window.addEventListener('pageshow', event => {
                if (event.persisted) resetNotificationUi();
            });

            console.log('博士考核系统初始化...');
            await this.checkLoginStatus();
            // 公共题库互不依赖，单项失败不能触发全局模态框或阻断首页。
            const publicResults = await Promise.allSettled([
                this.loadQuestions(),
                this.loadTrainingQuestions()
            ]);
            publicResults.forEach((result, index) => {
                if (result.status === 'rejected') {
                    console.warn(index === 0 ? '正式题库加载失败' : '培训题库加载失败', result.reason);
                }
            });

            this.updateCategories();
            if (this.isLoggedIn) {
                await this.loadUserData();
            } else {
                this.examStats = { totalAttempts: 0, averageScore: 0 };
            }
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
            // 初始化失败仅记录日志。登录、退出和页面导航不应产生全局错误弹窗；
            // 具体功能在用户主动操作时再显示针对性的错误。
        }
    }
});
