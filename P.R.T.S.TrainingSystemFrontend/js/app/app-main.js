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
            console.log('博士考核系统初始化...');
            await this.checkLoginStatus();
            await this.loadQuestions();
            await this.loadTrainingQuestions();
            // 从 localStorage 恢复入职培训记录（如果有）
            try {
                const raw = localStorage.getItem('trainingRecords');
                if (raw) {
                    this.trainingRecords = JSON.parse(raw);
                }
            } catch (e) {
                console.warn('恢复 trainingRecords 失败', e);
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
            this.showError('系统初始化失败，请刷新页面重试');
        }
    }
});
