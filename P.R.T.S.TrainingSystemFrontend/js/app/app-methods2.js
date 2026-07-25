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
        if (!questionId) return;
        // Prevent infinite re-fetch loops
        if (this._statsLoading === questionId) return;
        this._statsLoading = questionId;
        try {
            if (window.statsApi) {
                try {
                    this.questionStats = await statsApi.getQuestionStats(questionId);
                    return;
                } catch (e) {
                    console.warn('获取题目统计失败', e);
                }
            }
            this.questionStats = {
                totalAttempts: Math.floor(Math.random() * 500) + 100,
                correctRate: Math.random() * 0.3 + 0.6,
                mostCommonWrongOption: Math.floor(Math.random() * 4) + 1
            };
        } finally {
            this._statsLoading = null;
        }
    },
    async loadExamStats() {
        if (!this.isLoggedIn) {
            this.examStats = { totalAttempts: 0, averageScore: 0 };
            return;
        }
        if (window.examApi) {
            try {
                const history = await examApi.getExamHistory({ page: 1, size: 100 });
                // 当前后端直接返回考试记录数组，同时兼容旧版 {exams,total} 结构。
                const exams = Array.isArray(history)
                    ? history
                    : (history && Array.isArray(history.exams) ? history.exams : []);
                const totalAttempts = history && history.total ? history.total : exams.length;
                const totalScore = exams.reduce((sum, exam) => sum + (exam.score || 0), 0);
                const averageScore = totalAttempts > 0 ? totalScore / totalAttempts : 0;
                this.examStats = { totalAttempts, averageScore };
            } catch (error) {
                // 登录状态刚好失效时保持空统计，不把鉴权错误升级成首页初始化弹窗。
                this.examStats = { totalAttempts: 0, averageScore: 0 };
            }
        } else {
            this.examStats = { totalAttempts: 0, averageScore: 0 };
        }
    }
};
