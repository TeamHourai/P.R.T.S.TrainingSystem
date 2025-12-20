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
    // ================= 入职培训相关辅助函数 =================
    // 训练题目格子颜色：如果已答对 -> 绿色，答错 -> 红色，未答过 -> 灰色
    getTrainingQuestionColor(id) {
        const rec = this.trainingRecords[id];
        if (!rec) return '#efefef';
        if (rec.correct) return '#43A047';
        return '#F44336';
    },
    // 清除培训本地记录
    clearTrainingRecords() {
        if (confirm('确定要清除所有入职培训练习记录吗？')) {
            this.trainingRecords = {};
            localStorage.removeItem('trainingRecords');
            this.showSuccess('已清除入职培训记录');
        }
    },
    // 保存单题培训记录到本地
    saveTrainingRecord(questionId, correct) {
        const now = Date.now();
        const prev = this.trainingRecords[questionId] || { attempts: 0, correct: false, lastAt: 0 };
        prev.attempts = (prev.attempts || 0) + 1;
        prev.correct = correct || prev.correct;
        prev.lastAt = now;
        this.$set(this.trainingRecords, questionId, prev);
        try {
            localStorage.setItem('trainingRecords', JSON.stringify(this.trainingRecords));
        } catch (e) {
            console.warn('无法写入 trainingRecords 到 localStorage', e);
        }
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
            // 保持搜索关键字与结果，不在此处清空
            // this.searchKeyword = '';
            // this.searchResults = [];
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
            // 如果是从题库练习进入，构建 practiceContext：按主分类（practiceMode）以及次级分组（另一维度）组织题目
            if (mode === 'practice') {
                try {
                    // 找到被点击题目所在的 category key
                    let foundKey = null;
                    Object.keys(this.categories || {}).forEach(k => {
                        const cat = this.categories[k];
                        if (cat && Array.isArray(cat.questions) && cat.questions.some(q => q.id === id)) {
                            foundKey = k;
                        }
                    });
                    // 如果未找到，则尝试根据 current practiceMode 推断
                    if (!foundKey) {
                        foundKey = this.practiceMode === 'type' ? `type_${question.type}` : `difficulty_${question.difficulty}`;
                    }
                    const groups = [];
                    // 构建 groups：优先保留 category.questions 中子分组出现的顺序
                    const cat = this.categories[foundKey] || { questions: [] };
                    const seen = new Set();
                    const subgroupKeys = [];
                    if (this.practiceMode === 'type') {
                        // 次级维度为 difficulty
                        for (const q of cat.questions) {
                          const k = q.difficulty;
                          if (!seen.has(k)) { seen.add(k); subgroupKeys.push(k); }
                        }
                        // 按 subgroupKeys 顺序收集每组的题，保留 category.questions 的顺序
                        for (const k of subgroupKeys) {
                          const qs = cat.questions.filter(q => q.difficulty === k).slice();
                          if (qs.length > 0) groups.push({ key: k, questions: qs });
                        }
                    } else {
                        // practiceMode === 'difficulty' 次级为 type
                        for (const q of cat.questions) {
                          const k = q.type;
                          if (!seen.has(k)) { seen.add(k); subgroupKeys.push(k); }
                        }
                        for (const k of subgroupKeys) {
                          const qs = cat.questions.filter(q => q.type === k).slice();
                          if (qs.length > 0) groups.push({ key: k, questions: qs });
                        }
                    }
                    // 找到当前题在 groups 中的位置
                    let gIndex = -1, idxInGrp = -1;
                    for (let gi = 0; gi < groups.length; gi++) {
                        const arr = groups[gi].questions;
                        const pos = arr.findIndex(q=>q.id===id);
                        if (pos !== -1) { gIndex = gi; idxInGrp = pos; break; }
                    }
                    if (gIndex === -1) {
                        // 兜底：把题放到第一个分组
                        if (groups.length===0) {
                            groups.push({ key: this.practiceMode==='type'?question.type:question.difficulty, questions:[question] });
                            gIndex = 0; idxInGrp = 0;
                        } else {
                            gIndex = 0; idxInGrp = 0;
                        }
                    }
                    this.practiceContext = {
                        categoryKey: foundKey,
                        groups: groups,
                        currentGroupIndex: gIndex,
                        indexInGroup: idxInGrp
                    };
                } catch (e) {
                    console.warn('构建 practiceContext 失败', e);
                    this.practiceContext = { categoryKey: '', groups: [], currentGroupIndex: 0, indexInGroup: 0 };
                }
                this.currentQuestionIndex = this.rawQuestions.findIndex(q => q.id === id);
            }
            if (mode === 'random') {
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
        // 如果是培训题目，把结果保存到本地 trainingRecords
        const isCorrect = this.currentQuestion && this.selectedOption === this.currentQuestion.answer;
        if (this.questionMode === 'training') {
            try {
                this.saveTrainingRecord(this.currentQuestion.id, !!isCorrect);
            } catch (e) {
                console.warn('保存培训记录失败', e);
            }
        }
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