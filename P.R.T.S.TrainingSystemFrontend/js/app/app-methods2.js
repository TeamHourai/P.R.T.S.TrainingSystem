window._appMethods2 = {
    // ============ 数据加载方法 ============
    async loadQuestions() {
        // ...existing code...
        if (!window.questionApi) throw new Error('questionApi 未定义');
        const response = await questionApi.getQuestions({ page: 1, size: 1000, keyword: '' });
        if (response && response.questions) {
            this.rawQuestions = response.questions;
        } else if (Array.isArray(response)) {
            this.rawQuestions = response;
        } else {
            this.rawQuestions = [];
        }
    },
    async loadTrainingQuestions() {
        // ...existing code...
        if (!window.trainingQuestionApi) throw new Error('trainingQuestionApi 未定义');
        const response = await trainingQuestionApi.getTrainingQuestions({ page: 1, size: 100 });
        if (response && response.questions) {
            this.trainingQuestions = response.questions;
        } else if (Array.isArray(response)) {
            this.trainingQuestions = response;
        } else {
            this.trainingQuestions = [];
        }
    },
    async loadWrongQuestions() {
        // ...existing code...
        if (!this.isLoggedIn) {
            this.wrongQuestions = [];
            this.wrongQuestionsDetail = [];
            return;
        }
        if (!window.answerApi) throw new Error('answerApi 未定义');
        const response = await answerApi.getWrongQuestions({ page: 1, size: 1000 });

        // Backend currently returns an array of Question objects.
        // Some older frontends expected { history: [...] }.
        const list = Array.isArray(response) ? response : (response && response.history ? response.history : []);

        this.wrongQuestionsDetail = Array.isArray(list) ? list.slice() : [];
        this.wrongQuestions = this.wrongQuestionsDetail.map(q => q.id);
        this.updateWrongCategories();
    },
    async loadQuestionStats(questionId, questionType) {
        // ...existing code...
        if (!questionId) return;
        if (window.statsApi) {
            try {
                this.questionStats = await statsApi.getQuestionStats(questionId);
                return;
            } catch (e) {
                console.warn('获取题目统计失败，使用本地统计兜底', e);
            }
        }
        this.questionStats = {
            totalAttempts: Math.floor(Math.random() * 500) + 100,
            correctRate: Math.random() * 0.3 + 0.6,
            mostCommonWrongOption: Math.floor(Math.random() * 4) + 1
        };
    },
    async loadExamStats() {
        // ...existing code...
        if (window.examApi) {
            const history = await examApi.getExamHistory({ page: 1, size: 100 });
            if (history && history.exams) {
                const totalAttempts = history.total || history.exams.length;
                const totalScore = history.exams.reduce((sum, exam) => sum + (exam.score || 0), 0);
                const averageScore = totalAttempts > 0 ? totalScore / totalAttempts : 0;
                this.examStats = { totalAttempts, averageScore };
            }
        } else {
            this.examStats = { totalAttempts: 1234, averageScore: 78.5 };
        }
    },
    loadSystemData() {
        // ...existing code...
        this.updateVersions = [
            { id: 1, number: 'v2.0.0', date: '2024-01-01', title: '系统全面升级', content: '博士考核系统v2.0正式上线...' },
            { id: 2, number: 'v1.5.0', date: '2023-12-01', title: '新增培训模块', content: '新增入职培训模块...' }
        ];
        this.selectedVersion = this.updateVersions[0] || {};
        this.systemTips = `
            <p>1. 使用前请先登录账号</p>
            <p>2. 建议从入职培训开始学习</p>
            <p>3. 错题本会自动记录答错的题目</p>
            <p>4. 全真模拟考试限时15分钟</p>
        `;
    }
};
