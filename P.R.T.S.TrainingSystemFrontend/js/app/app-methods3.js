window._appMethods3 = {
    // ============ 分类、导航、题目操作、辅助方法 ============
    updateCategories() {
        // ...existing code...
        const newCategories = {};
        if (this.practiceMode === 'type') {
            const typeNames = {
                1: '干员调配与特性化决策',
                2: '空间部署与极致化战术',
                3: '效能审计与生态位界定',
                4: '横向分析与竞争力评估',
                5: '作战环境与档案类记录'
            };
            for (let i = 1; i <= 5; i++) {
                const questions = this.rawQuestions.filter(q => q.type === i);
                newCategories[`type_${i}`] = {
                    name: typeNames[i],
                    questions: questions,
                    isOpen: false
                };
            }
        } else {
            const difficultyNames = {
                1: '常识',
                2: '基操',
                3: '娴熟',
                4: '明智',
                5: '深邃'
            };
            for (let i = 1; i <= 5; i++) {
                const questions = this.rawQuestions.filter(q => q.difficulty === i);
                newCategories[`difficulty_${i}`] = {
                    name: difficultyNames[i],
                    questions: questions,
                    isOpen: false
                };
            }
        }
        this.categories = newCategories;
    },
    updateWrongCategories() {
        // ...existing code...
        const newCategories = {};
        const typeNames = {
            1: '干员调配与特性化决策',
            2: '空间部署与极致化战术',
            3: '效能审计与生态位界定',
            4: '横向分析与竞争力评估',
            5: '作战环境与档案类记录'
        };
        for (let i = 1; i <= 5; i++) {
            const questions = this.wrongQuestionsDetail.filter(q => q.type === i);
            if (questions.length > 0) {
                newCategories[`type_${i}`] = {
                    name: typeNames[i],
                    questions: questions,
                    isOpen: false
                };
            }
        }
        this.wrongCategories = newCategories;
    },
    toggleSidebar() {
        this.sidebarOpen = !this.sidebarOpen;
    },
    goToPage(page) {
        // ...existing code...
        this.currentPage = page;
        this.selectedOption = null;
        this.showAnswer = false;
        if (page === 'practice') {
            this.updateCategories();
        } else if (page === 'wrong') {
            this.updateWrongCategories();
        } else if (page === 'search') {
            this.searchKeyword = '';
            this.searchResults = [];
        }
        if (window.innerWidth < 768) {
            this.sidebarOpen = false;
        }
    },
    goToQuestion(id, mode) {
        // ...existing code...
        this.questionMode = mode;
        const question = this.rawQuestions.find(q => q.id === id);
        if (question) {
            this.loadQuestionForDisplay(question, mode);
            if (mode === 'practice') {
                this.currentQuestionIndex = this.rawQuestions.findIndex(q => q.id === id);
            } else if (mode === 'random') {
                this.randomHistory.push(id);
                this.randomCurrentIndex = this.randomHistory.length - 1;
            }
            this.currentPage = 'question';
            this.selectedOption = null;
            this.showAnswer = false;
        } else {
            this.showError('题目不存在');
        }
    },
    goToTrainingQuestion(id) {
        // ...existing code...
        this.questionMode = 'training';
        const question = this.trainingQuestions.find(q => q.id === id);
        if (question) {
            this.loadQuestionForDisplay(question, 'training');
            this.currentPage = 'question';
            this.selectedOption = null;
            this.showAnswer = false;
        } else {
            this.showError('培训题目不存在');
        }
    },
    goToWrongQuestion(id) {
        // ...existing code...
        this.questionMode = 'wrong';
        const question = this.wrongQuestionsDetail.find(q => q.id === id);
        if (question) {
            this.loadQuestionForDisplay(question, 'wrong');
            this.currentPage = 'question';
            this.selectedOption = null;
            this.showAnswer = false;
        } else {
            this.showError('错题不存在');
        }
    },
    loadQuestionForDisplay(question, mode) {
        // ...existing code...
        const fmtText = (str) => (str || '').replace(/\n/g, '<br>').replace(/\r\n/g, '<br>');
        this.currentQuestion = {
            ...question,
            typeText: mode === 'training' ? '入职培训' : this.getTypeText(question.type),
            difficultyText: mode === 'training' ? '入门' : this.getDifficultyText(question.difficulty),
            resource: question.resource || '',
            question: fmtText(question.question),
            options: question.options ? question.options.map(opt => fmtText(opt || '')) : ['', '', '', ''],
            analysis: fmtText(question.analysis),
            picture: question.picture || false
        };
    },
    selectOption(option) {
        if (!this.showAnswer) {
            this.selectedOption = option;
        }
    },
    async checkAnswer() {
        // ...existing code...
        if (!this.selectedOption) {
            this.showError('请先选择一个答案');
            return;
        }
        if (!this.isLoggedIn && this.questionMode !== 'training') {
            this.showError('请先登录以保存答题记录');
            this.showAnswer = true;
            return;
        }
        this.showAnswer = true;
        if (window.answerApi && this.isLoggedIn) {
            await answerApi.submitAnswer(
                this.currentQuestion.id,
                this.questionMode === 'training' ? 'training' : 'normal',
                this.selectedOption
            );
        }
        if (!this.isAnswerCorrect && this.questionMode !== 'training') {
            await this.addToWrongBook(this.currentQuestion.id);
        }
        await this.loadQuestionStats(
            this.currentQuestion.id,
            this.questionMode === 'training' ? 'training' : 'normal'
        );
        this.$nextTick(() => {
            const analysisElement = this.$refs.answerAnalysis;
            if (analysisElement) {
                analysisElement.scrollIntoView({ behavior: 'smooth', block: 'start' });
            }
        });
    },
    async addToWrongBook(questionId) {
        // ...existing code...
        if (!this.isLoggedIn) return;
        if (!this.wrongQuestions.includes(questionId)) {
            this.wrongQuestions.push(questionId);
            const question = this.rawQuestions.find(q => q.id === questionId);
            if (question) {
                this.wrongQuestionsDetail.push(question);
                this.updateWrongCategories();
            }
        }
    },
    async removeFromWrongBook(questionId) {
        // ...existing code...
        if (!this.isLoggedIn) return;
        this.wrongQuestions = this.wrongQuestions.filter(id => id !== questionId);
        this.wrongQuestionsDetail = this.wrongQuestionsDetail.filter(q => q.id !== questionId);
        this.updateWrongCategories();
        if (window.answerApi) {
            await answerApi.removeWrongQuestion(questionId);
        }
    }
};

