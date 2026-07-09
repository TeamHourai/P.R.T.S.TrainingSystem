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
    // 清除培训记录（后端持久化，替代 localStorage）
    async clearTrainingRecords() {
        if (!confirm('确定要清除所有入职培训练习记录吗？')) return;

        // 登录态：同步后端
        if (this.isLoggedIn && window.trainingRecordsApi && typeof window.trainingRecordsApi.clear === 'function') {
            try {
                await window.trainingRecordsApi.clear();
            } catch (e) {
                console.warn('清除 trainingRecords 失败', e);
            }
        }

        // 本地状态清空
        this.trainingRecords = {};
        this.showSuccess('已清除入职培训记录');
    },

    // 保存单题培训记录（后端持久化，替代 localStorage）
    async saveTrainingRecord(questionId, correct) {
        const now = Date.now();
        const prev = this.trainingRecords[questionId] || { attempts: 0, correct: false, lastAt: 0 };
        const next = {
            attempts: (prev.attempts || 0) + 1,
            correct: !!correct || !!prev.correct,
            lastAt: now
        };
        this.$set(this.trainingRecords, questionId, next);

        // 登录态：写入后端
        if (this.isLoggedIn && window.trainingRecordsApi && typeof window.trainingRecordsApi.upsert === 'function') {
            try {
                await window.trainingRecordsApi.upsert({
                    questionId: Number(questionId),
                    attempts: next.attempts,
                    correct: next.correct,
                    lastAt: next.lastAt
                });
            } catch (e) {
                console.warn('写入 trainingRecords 失败', e);
            }
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
        this.questionMode = 'training';
        const question = this.trainingQuestions.find(q => q.id === id);
        if (question) {
            this.loadQuestionForDisplay(question, 'training');
            this.currentPage = 'question';
            this.$nextTick(() => {
                this.selectedOption = null;
                this.showAnswer = false;
            });
        } else {
            this.showError('培训题目不存在');
        }
    },
    goToWrongQuestion(id) {
        this.questionMode = 'wrong';
        const question = this.wrongQuestionsDetail.find(q => q.id === id);
        if (question) {
            this.loadQuestionForDisplay(question, 'wrong');
            this.currentPage = 'question';
            // Batch reactive changes in nextTick to avoid cascading re-renders
            this.$nextTick(() => {
                this.selectedOption = null;
                this.showAnswer = false;
            });
        } else {
            this.showError('错题不存在');
        }
    },
    loadQuestionForDisplay(question, mode) {
        const fmtText = (str) => (str || '')
            .replace(/\\r\\n/g, '\n')
            .replace(/\\n/g, '\n')
            .replace(/\r\n/g, '\n')
            .replace(/\n/g, '<br>');
        // Normalize options: ensure array of plain strings (no HTML, no fmtText)
        let opts = question.options;
        if (!Array.isArray(opts)) {
            if (typeof opts === 'string') opts = opts.split(/[|¦]/);
            else opts = ['', '', '', ''];
        }
        while (opts.length < 4) opts.push('');
        // Normalize answer to number
        let answer = question.answer;
        if (typeof answer === 'string') answer = parseInt(answer) || 0;
        this.currentQuestion = {
            ...question,
            typeText: mode === 'training' ? '入职培训' : this.getTypeText(question.type),
            difficultyText: mode === 'training' ? '入门' : this.getDifficultyText(question.difficulty),
            resource: question.resource || '',
            question: fmtText(question.question),
            options: opts.map(o => String(o || '')),
            analysis: fmtText(question.analysis) || '暂无解析',
            answer: answer,
            picture: question.picture || false
        };
    },
    selectOption(option) {
        if (!this.showAnswer) {
            this.selectedOption = option;

            // 自动提交答案：避免连点导致重复提交
            if (this.answerSettings && this.answerSettings.autoSubmit) {
                if (this._autoAnswerBusy) return;
                this._autoAnswerBusy = true;
                Promise.resolve()
                    .then(() => this.checkAnswer())
                    .finally(() => { this._autoAnswerBusy = false; });
            }
        }
    },
    async checkAnswer() {
        if (this._checkingAnswer) return;
        this._checkingAnswer = true;
        try {
        if (!this.selectedOption) {
            this.showError('请先选择一个答案');
            return;
        }

        // 先计算正确性（后续会根据设置决定是否展示解析）
        const isCorrectNow = this.currentQuestion && this.selectedOption === this.currentQuestion.answer;
        const autoNextOnCorrect = !!(this.answerSettings && this.answerSettings.autoNextCorrect);
        const shouldAutoAdvanceCorrect = autoNextOnCorrect && isCorrectNow;

        // 显示逻辑：
        // - 普通情况：showAnswer=true 显示解析
        // - 开启“答对自动下一题”且答对：也让 showAnswer=true 以便选项变绿，但解析面板在模板里被隐藏（见 index.html 改动）
        this.showAnswer = true;

        // 如果是培训题目，把结果保存到 trainingRecords
        const isCorrect = isCorrectNow;
        if (this.questionMode === 'training') {
            try {
                await this.saveTrainingRecord(this.currentQuestion.id, !!isCorrect);
            } catch (e) {
                console.warn('保存培训记录失败', e);
            }
        }

        // 非培训模式：已登录才提交做题记录
        if (this.questionMode !== 'training') {
            if (!this.isLoggedIn) {
                this.showError('请先登录以保存答题记录');
            } else if (window.answerApi) {
                try {
                    await answerApi.submitAnswer(
                        this.currentQuestion.id,
                        'normal',
                        this.selectedOption
                    );
                } catch (e) {
                    console.warn('提交答题记录失败', e);
                }
            }

            // 错题本（前端本地维护 + 尝试后端删除/同步）
            if (!this.isAnswerCorrect) {
                await this.addToWrongBook(this.currentQuestion.id);
            }
        }

        await this.loadQuestionStats(
            this.currentQuestion.id,
            this.questionMode === 'training' ? 'training' : 'normal'
        );

        // 普通提交：滚动；自动下一题（答对）不滚动，避免视觉跳动
        if (!shouldAutoAdvanceCorrect) {
            this.$nextTick(() => {
                window.scrollTo({ top: 0, behavior: 'smooth' });
            });
        }

        // 答对自动下一题：停留 1s 展示选项变绿，然后翻页（不显示解析）
        if (shouldAutoAdvanceCorrect) {
            setTimeout(() => {
                try {
                    if (this.hasNextQuestion) {
                        this.nextQuestion();
                    }
                } catch (e) {
                    // ignore
                }
            }, 1000);
        }
        } catch (e) {
            console.error('checkAnswer failed', e);
        } finally {
            this._checkingAnswer = false;
        }
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

        let ok = true;
        if (window.answerApi) {
            try {
                const resp = await answerApi.removeWrongQuestion(questionId);
                ok = !!(resp && (resp.success === true));
            } catch (e) {
                ok = false;
            }
        }

        if (!ok) {
            this.showError('删除错题失败（后端未生效），请重试');
            return;
        }

        // remove locally
        this.wrongQuestions = this.wrongQuestions.filter(id => id !== questionId);
        this.wrongQuestionsDetail = this.wrongQuestionsDetail.filter(q => q.id !== questionId);
        this.updateWrongCategories();

        // refresh from backend to ensure persistence
        if (typeof this.loadWrongQuestions === 'function') {
            await this.loadWrongQuestions();
        }
    },

    // ================= 用户答题设置 =================
    openAnswerSettings() {
        this.showAnswerSettingsModal = true;
        // 打开时尝试刷新一次（已登录时从后端拉）
        this.loadAnswerSettings();
    },
    closeAnswerSettings() {
        this.showAnswerSettingsModal = false;
    },
    async loadAnswerSettings() {
        // 未登录：仅用本地默认值（也可改成 localStorage 临时保存，这里先不扩展）
        if (!this.isLoggedIn) return;

        if (window.answerSettingsApi && typeof window.answerSettingsApi.get === 'function') {
            try {
                const res = await window.answerSettingsApi.get();
                if (res && res.success) {
                    this.answerSettings.autoSubmit = !!res.autoSubmit;
                    this.answerSettings.autoNextCorrect = !!res.autoNextCorrect;
                }
            } catch (e) {
                // 静默失败，不阻断正常答题
                console.warn('loadAnswerSettings failed', e);
            }
        }
    },
    async saveAnswerSettings() {
        if (!this.isLoggedIn) {
            this.showError('请先登录后保存设置');
            return;
        }
        if (window.answerSettingsApi && typeof window.answerSettingsApi.update === 'function') {
            try {
                const payload = {
                    autoSubmit: !!this.answerSettings.autoSubmit,
                    autoNextCorrect: !!this.answerSettings.autoNextCorrect
                };
                const res = await window.answerSettingsApi.update(payload);
                if (res && res.success) {
                    this.answerSettings.autoSubmit = !!res.autoSubmit;
                    this.answerSettings.autoNextCorrect = !!res.autoNextCorrect;
                    this.showSuccess('设置已保存');
                    this.showAnswerSettingsModal = false;
                    return;
                }
            } catch (e) {
                console.warn('saveAnswerSettings failed', e);
                this.showError('保存失败，请稍后重试');
                return;
            }
        }
        this.showError('答题设置接口未初始化');
    },
    onQuestionImageError(e) {
        try {
            if (e && e.target) {
                e.target.onerror = null;
                e.target.style.display = 'none';
            }
        } catch (ignored) {}
        try {
            if (this.currentQuestion) {
                this.$set(this.currentQuestion, 'picture', false);
            }
        } catch (ignored) {}
    },

    // 入职培训：跳转到第一个未正确完成的题目
    goToFirstUnansweredTraining() {
        try {
            const questions = Array.isArray(this.trainingQuestions) ? this.trainingQuestions : [];
            if (questions.length === 0) {
                this.showError('暂无培训题目');
                return;
            }

            // trainingRecords: { [id]: { correct: boolean, ... } }
            const recs = this.trainingRecords || {};
            const firstUnanswered = questions.find(q => {
                const r = recs[q.id];
                return !(r && r.correct === true);
            });

            const target = firstUnanswered || questions[0];
            this.goToTrainingQuestion(target.id);
        } catch (e) {
            console.warn('goToFirstUnansweredTraining failed', e);
            // 兜底：直接进第一题
            if (this.trainingQuestions && this.trainingQuestions[0]) {
                this.goToTrainingQuestion(this.trainingQuestions[0].id);
            }
        }
    },
};