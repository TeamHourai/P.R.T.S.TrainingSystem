window._appMethods4 = {
    // ============ 题目导航、搜索、工具、通知等方法 ============
    prevQuestion() {
        // ...existing code...
        if (this.questionMode === 'practice') {
            // 如果已建立 practiceContext，则在分组内/组间导航
            const ctx = this.practiceContext || {};
            if (ctx && Array.isArray(ctx.groups) && ctx.groups.length > 0) {
                let g = ctx.currentGroupIndex || 0;
                let i = ctx.indexInGroup || 0;
                if (i > 0) {
                    i--;
                } else if (g > 0) {
                    g--;
                    i = (ctx.groups[g].questions.length || 1) - 1;
                } else {
                    // 已经是该分类的第一题，保持不变
                    this.showInfo('已经是本分类的第一题了');
                    return;
                }
                const q = ctx.groups[g].questions[i];
                if (q) {
                    this.practiceContext.currentGroupIndex = g;
                    this.practiceContext.indexInGroup = i;
                    this.loadQuestionForDisplay(q, 'practice');
                    // 更新 global index
                    this.currentQuestionIndex = this.rawQuestions.findIndex(r=>r.id===q.id);
                    this.resetQuestionState();
                }
                return;
            }
            // 兜底回退到原有行为
            if (this.currentQuestionIndex > 0) {
                this.currentQuestionIndex--;
                const question = this.rawQuestions[this.currentQuestionIndex];
                this.loadQuestionForDisplay(question, 'practice');
                this.resetQuestionState();
            }
        } else if (this.questionMode === 'random') {
            if (this.randomCurrentIndex > 0) {
                this.randomCurrentIndex--;
                const id = this.randomHistory[this.randomCurrentIndex];
                const question = this.rawQuestions.find(q => q.id === id);
                if (question) {
                    this.loadQuestionForDisplay(question, 'random');
                    this.resetQuestionState();
                }
            }
        } else if (this.questionMode === 'training') {
            const prevId = this.getPrevTrainingQuestion();
            if (prevId !== null) {
                this.goToTrainingQuestion(prevId);
            }
        } else if (this.questionMode === 'wrong') {
            const prevId = this.getPrevWrongQuestion();
            if (prevId !== null) {
                this.goToWrongQuestion(prevId);
            }
        } else if (this.questionMode === 'search') {
            // 在搜索练习模式下使用 searchResults 和 searchCurrentIndex
            if (Array.isArray(this.searchResults) && this.searchCurrentIndex > 0) {
                this.searchCurrentIndex--;
                const q = this.searchResults[this.searchCurrentIndex];
                if (q) {
                    this.loadQuestionForDisplay(q, 'practice');
                    this.resetQuestionState();
                }
            }
        }
    },
    nextQuestion() {
        // ...existing code...
        if (this.questionMode === 'practice') {
            // 使用 practiceContext（若存在）进行分组内/组间导航
            const ctx = this.practiceContext || {};
            if (ctx && Array.isArray(ctx.groups) && ctx.groups.length > 0) {
                let g = ctx.currentGroupIndex || 0;
                let i = ctx.indexInGroup || 0;
                if (i < (ctx.groups[g].questions.length - 1)) {
                    i++;
                } else if (g < ctx.groups.length - 1) {
                    // 移动到下一组的第一题
                    g++;
                    i = 0;
                } else {
                    this.showInfo('已经是本分类的最后一题了');
                    return;
                }
                const q = ctx.groups[g].questions[i];
                if (q) {
                    this.practiceContext.currentGroupIndex = g;
                    this.practiceContext.indexInGroup = i;
                    this.loadQuestionForDisplay(q, 'practice');
                    // 更新 global index
                    this.currentQuestionIndex = this.rawQuestions.findIndex(r=>r.id===q.id);
                    this.resetQuestionState();
                }
                return;
            }
            // 兜底：按全局ID顺序导航
            if (this.currentQuestionIndex < this.rawQuestions.length - 1) {
                this.currentQuestionIndex++;
                const question = this.rawQuestions[this.currentQuestionIndex];
                this.loadQuestionForDisplay(question, 'practice');
                this.resetQuestionState();
            }
        } else if (this.questionMode === 'random') {
             const doneQuestions = [...this.randomHistory];
             const availableQuestions = this.rawQuestions.filter(
                 q => !doneQuestions.includes(q.id)
             );
             if (availableQuestions.length > 0) {
                 const randomIndex = Math.floor(Math.random() * availableQuestions.length);
                 const nextQuestion = availableQuestions[randomIndex];
                 this.randomHistory.push(nextQuestion.id);
                 this.randomCurrentIndex = this.randomHistory.length - 1;
                 this.loadQuestionForDisplay(nextQuestion, 'random');
                 this.resetQuestionState();
             } else {
                 this.showInfo('所有题目都已练习过！');
             }
         } else if (this.questionMode === 'training') {
            const nextId = this.getNextTrainingQuestion();
            if (nextId !== null) {
                this.goToTrainingQuestion(nextId);
            } else {
                this.showInfo('所有题目都已练习过！');
            }
        } else if (this.questionMode === 'wrong') {
            const nextId = this.getNextWrongQuestion();
            if (nextId !== null) {
                this.goToWrongQuestion(nextId);
            } else {
                this.showInfo('已经是最后一题了！');
            }
        } else if (this.questionMode === 'search') {
            // 搜索练习：移动到 searchResults 的下一题
            if (Array.isArray(this.searchResults) && this.searchCurrentIndex < this.searchResults.length - 1) {
                this.searchCurrentIndex++;
                const q = this.searchResults[this.searchCurrentIndex];
                if (q) {
                    this.loadQuestionForDisplay(q, 'practice');
                    this.resetQuestionState();
                }
            } else {
                this.showInfo('已经是搜索结果中的最后一题了！');
            }
        }
    },
    resetQuestionState() {
        this.selectedOption = null;
        this.showAnswer = false;
        this.questionStats = {};
    },
    getPrevTrainingQuestion() {
        if (!this.currentQuestion) return null;
        const currentId = this.currentQuestion.id;
        const prevQuestions = this.trainingQuestions
            .filter(q => q.id < currentId)
            .sort((a, b) => b.id - a.id);
        return prevQuestions.length > 0 ? prevQuestions[0].id : null;
    },
    getNextTrainingQuestion() {
        if (!this.currentQuestion) return null;
        const currentId = this.currentQuestion.id;
        const nextQuestions = this.trainingQuestions
            .filter(q => q.id > currentId)
            .sort((a, b) => a.id - b.id);
        return nextQuestions.length > 0 ? nextQuestions[0].id : null;
    },
    getPrevWrongQuestion() {
        if (!this.currentQuestion) return null;
        const currentId = this.currentQuestion.id;
        const wrongIds = this.wrongQuestions.sort((a, b) => a - b);
        const currentIndex = wrongIds.indexOf(currentId);
        return currentIndex > 0 ? wrongIds[currentIndex - 1] : null;
    },
    getNextWrongQuestion() {
        if (!this.currentQuestion) return null;
        const currentId = this.currentQuestion.id;
        const wrongIds = this.wrongQuestions.sort((a, b) => a - b);
        const currentIndex = wrongIds.indexOf(currentId);
        return currentIndex < wrongIds.length - 1 ? wrongIds[currentIndex + 1] : null;
    },
    startRandom() {
        if (this.rawQuestions.length === 0) {
            this.showError('题库为空，无法开始随机练习');
            return;
        }
        const randomIndex = Math.floor(Math.random() * this.rawQuestions.length);
        const question = this.rawQuestions[randomIndex];
        this.questionMode = 'random';
        this.randomHistory = [question.id];
        this.randomCurrentIndex = 0;
        this.goToQuestion(question.id, 'random');
    },
    startJump() {
        const input = this.jumpQuestionId.trim();
        if (input.toUpperCase().startsWith('G')) {
            const id = parseInt(input.substring(1));
            if (id >= 1 && id <= this.trainingQuestions.length) {
                this.questionMode = 'training';
                this.goToTrainingQuestion(id);
            } else {
                this.showError(`请输入有效的入职培训题目ID（G1-G${this.trainingQuestions.length}）`);
            }
        } else {
            const id = parseInt(input);
            if (id >= 1 && id <= this.rawQuestions.length) {
                this.questionMode = 'jump';
                const question = this.rawQuestions.find(q => q.id === id);
                if (question) {
                    this.loadQuestionForDisplay(question, 'jump');
                    this.currentPage = 'question';
                    this.selectedOption = null;
                    this.showAnswer = false;
                }
            } else {
                this.showError(`请输入有效的题目ID（1-${this.rawQuestions.length}）`);
            }
        }
    },
    performSearch() {
        if (!this.searchKeyword.trim()) {
            this.searchResults = [];
            return;
        }
        const keyword = this.searchKeyword.toLowerCase().trim();
        this.searchResults = this.rawQuestions.filter(question => {
            if (question.question && question.question.toLowerCase().includes(keyword)) {
                return true;
            }
            if (question.options && question.options.some(opt =>
                opt && opt.toLowerCase().includes(keyword))) {
                return true;
            }
            if (question.keywords && question.keywords.some(kw =>
                kw && kw.toLowerCase().includes(keyword))) {
                return true;
            }
            if (question.analysis && question.analysis.toLowerCase().includes(keyword)) {
                return true;
            }
            return false;
        });
        // 当搜索结果产生变化且当前处于 search 模式时，保持 searchCurrentIndex 在合理范围
        if (this.questionMode === 'search') {
            if (!Array.isArray(this.searchResults) || this.searchResults.length === 0) {
                this.searchCurrentIndex = -1;
            } else if (this.searchCurrentIndex >= this.searchResults.length) {
                this.searchCurrentIndex = this.searchResults.length - 1;
            }
        }
    },
    goToSearchResult(questionId) {
        // 通过搜索结果进入：启用 special search 模式，该模式下 prev/next/返回行为不同
        const idx = this.searchResults.findIndex(q => q.id === questionId);
        if (idx === -1) {
            // 兜底：如果未在当前 searchResults 中，尝试在 rawQuestions 中查找
            this.goToQuestion(questionId, 'practice');
            return;
        }
        this.questionMode = 'search';
        this.searchCurrentIndex = idx;
        const q = this.searchResults[idx];
        this.loadQuestionForDisplay(q, 'practice');
        this.currentPage = 'question';
        this.selectedOption = null;
        this.showAnswer = false;
    },
    truncateQuestion(question) {
        if (!question) return '';
        const text = question.replace(/<br>/g, ' ').replace(/<[^>]*>/g, '');
        return text.length > 120 ? text.substring(0, 120) + '...' : text;
    },
    getTypeText(type) {
        const typeMap = {
            1: '干员调配与特性化决策',
            2: '空间部署与极致化战术',
            3: '效能审计与生态位界定',
            4: '横向分析与竞争力评估',
            5: '作战环境与档案类记录'
        };
        return typeMap[type] || '未知类型';
    },
    getDifficultyText(difficulty) {
        const difficultyMap = {
            1: '常识',
            2: '基操',
            3: '娴熟',
            4: '明智',
            5: '深邃'
        };
        return difficultyMap[difficulty] || '未知难度';
    },
    getTypeColor(type) {
        const typeColors = {
            1: '#E91E63',
            2: '#9C27B0',
            3: '#3F51B5',
            4: '#009688',
            5: '#FF5722'
        };
        return typeColors[type] || '#666';
    },
    getDifficultyColor(difficulty) {
        const difficultyColors = {
            1: '#43A047',
            2: '#7E57C2',
            3: '#2196F3',
            4: '#FF9800',
            5: '#F44336'
        };
        return difficultyColors[difficulty] || '#666';
    },
    getQuestionColor(question) {
        if (this.practiceMode === 'type') {
            return this.getDifficultyColor(question.difficulty);
        } else {
            return this.getTypeColor(question.type);
        }
    },
    showError(message) {
        if (window.uiModal && typeof window.uiModal.error === 'function') {
            window.uiModal.error(message);
            return;
        }
        // fallback
        console.error('错误：' + message);
    },
    showSuccess(message) {
        if (window.uiModal && typeof window.uiModal.success === 'function') {
            window.uiModal.success(message);
            return;
        }
        console.log('成功：' + message);
    },
    showInfo(message) {
        if (window.uiModal && typeof window.uiModal.info === 'function') {
            window.uiModal.info(message);
            return;
        }
        console.log(String(message));
    },
    startExam() {
        if (!this.isLoggedIn) {
            this.showError('请先登录以参加考试');
            this.showAuthModal = true;
            this.authMode = 'login';
            return;
        }
        window.location.href = 'exam.html';
    },
    goBackFromQuestion() {
        // 如果当前是从搜索结果进入的练习（search 模式），返回搜索页面并保留搜索关键字与结果
        if (this.questionMode === 'search') {
            this.currentPage = 'search';
            // 不重置 searchResults 或 searchKeyword；保留 searchCurrentIndex
            // 清除 questionMode，防止返回后误以为仍在搜索答题模式
            this.questionMode = '';
            return;
        }
        if (this.questionMode === 'practice') {
            this.currentPage = 'practice';
        } else if (this.questionMode === 'random' || this.questionMode === 'jump') {
            this.currentPage = 'quickjump';
        } else if (this.questionMode === 'training') {
            this.currentPage = 'training';
        } else if (this.questionMode === 'wrong') {
            this.currentPage = 'wrong';
        } else {
            this.currentPage = 'index';
        }
    },
    goToEditor(type) {
        if (!this.isAdmin) {
            this.showError('需要管理员权限');
            return;
        }
        const map = {
            questions: 'editor.html',
            training: 'training-editor.html'
        };
        window.open(map[type], '_blank');
    },
    toggleCategory(key) {
        const updatedCategories = { ...this.categories };
        updatedCategories[key].isOpen = !updatedCategories[key].isOpen;
        Object.keys(updatedCategories).forEach(k => {
            if (k !== key) {
                updatedCategories[k].isOpen = false;
            }
        });
        this.categories = updatedCategories;
    },
    toggleWrongCategory(key) {
        const updatedCategories = { ...this.wrongCategories };
        updatedCategories[key].isOpen = !updatedCategories[key].isOpen;
        Object.keys(updatedCategories).forEach(k => {
            if (k !== key) {
                updatedCategories[k].isOpen = false;
            }
        });
        this.wrongCategories = updatedCategories;
    },
    deleteWrongCategory(key) {
        if (confirm('确定要删除这个分类的所有错题吗？')) {
            const type = parseInt(key.split('_')[1]);
            const idsToDelete = this.wrongQuestionsDetail.filter(q => q.type === type).map(q => q.id);

            // hide on backend (one by one)
            Promise.all(idsToDelete.map(id => this.removeFromWrongBook(id)))
                .then(() => {
                    // then update UI
                    this.wrongQuestionsDetail = this.wrongQuestionsDetail.filter(q => q.type !== type);
                    this.wrongQuestions = this.wrongQuestionsDetail.map(q => q.id);
                    this.updateWrongCategories();
                });
        }
    },
    deleteWrongQuestion(id) {
        if (confirm('确定要删除这道错题吗？')) {
            this.removeFromWrongBook(id);
        }
    },
    clearWrongRecords() {
        if (confirm('确定要清除所有错题记录吗？')) {
            const idsToDelete = (this.wrongQuestionsDetail || []).map(q => q.id);
            Promise.all(idsToDelete.map(id => this.removeFromWrongBook(id)))
                .then(() => {
                    this.wrongQuestions = [];
                    this.wrongQuestionsDetail = [];
                    this.updateWrongCategories();
                    this.showSuccess('已清除所有错题记录');
                });
        }
    },
    goToFirstUnansweredTraining() {
        if (this.trainingQuestions.length > 0) {
            this.goToTrainingQuestion(this.trainingQuestions[0].id);
        } else {
            this.showError('暂无培训题目');
        }
    },
    async loadNotifications() {
        if (this.systemNoticeTab === 'local') {
            this.loadLocalNotifications();
            return;
        }
        this.loadingNotifications = true;
        try {
            if (!window.notificationApi) {
                this.notifications = [];
                this.unreadCount = 0;
                this.hasMoreNotifications = false;
                this.loadingNotifications = false;
                return;
            }
            const params = {
                page: this.noticePage,
                size: 10,
                unread: this.systemNoticeTab === 'unread' ? true : undefined
            };
            const res = await notificationApi.getNotifications(params);
            this.notifications = res.notifications || [];
            this.unreadCount = res.unreadCount || 0;
            this.hasMoreNotifications = res.hasMore || false;
        } catch (e) {
            this.notifications = [];
            this.unreadCount = 0;
            this.hasMoreNotifications = false;
        } finally {
            this.loadingNotifications = false;
        }
    },
    loadLocalNotifications() {
        this.localNotifications = JSON.parse(localStorage.getItem('localNotifications') || '[]');
    },
    switchNoticeTab(tab) {
        this.systemNoticeTab = tab;
    },
    changeNoticePage(page) {
        if (page < 1) return;
        this.noticePage = page;
        this.loadNotifications();
    },
    async markNotificationAsRead(notif) {
        if (!notif || notif.isRead) return;
        if (window.notificationApi) {
            await notificationApi.markAsRead(notif.id);
        }
        notif.isRead = true;
        this.unreadCount = Math.max(0, this.unreadCount - 1);
    },
    async markAllNotificationsAsRead() {
        if (window.notificationApi) {
            await notificationApi.markAllAsRead();
        }
        this.notifications.forEach(n => n.isRead = true);
        this.unreadCount = 0;
    },
    async deleteNotification(notif) {
        if (!notif) return;
        if (window.notificationApi) {
            await notificationApi.deleteNotification(notif.id);
        }
        this.notifications = this.notifications.filter(n => n.id !== notif.id);
    },
    async clearAllNotifications() {
        if (window.notificationApi) {
            await notificationApi.clearAll();
        }
        this.notifications = [];
        this.unreadCount = 0;
    },
    deleteLocalNotification(notif) {
        this.localNotifications = this.localNotifications.filter(n => n.id !== notif.id);
        localStorage.setItem('localNotifications', JSON.stringify(this.localNotifications));
    },
    clearAllLocalNotifications() {
        this.localNotifications = [];
        localStorage.removeItem('localNotifications');
    },
    confirmMarkAllRead() {
        this.confirmMessage = '确定要将全部通知标记为已读吗？';
        this.confirmAction = this.markAllNotificationsAsRead;
        this.showConfirmDialog = true;
    },
    confirmClearAllNotifications() {
        this.confirmMessage = '确定要清空全部通知吗？';
        this.confirmAction = this.clearAllNotifications;
        this.showConfirmDialog = true;
    },
    confirmClearAllLocalNotifications() {
        this.confirmMessage = '确定要清空本地通知吗？';
        this.confirmAction = this.clearAllLocalNotifications;
        this.showConfirmDialog = true;
    },
    handleConfirmAction() {
        if (typeof this.confirmAction === 'function') {
            this.confirmAction();
        }
        this.showConfirmDialog = false;
    }
};
