window._appMethods4 = {
    // ============ 分类折叠/展开（修复 toggleCategory 未定义报错）===========
    toggleCategory(key) {
        if (!this.categories) return;
        const category = this.categories[key];
        if (!category) return;

        // 确保 isOpen 是响应式属性
        if (!Object.prototype.hasOwnProperty.call(category, 'isOpen')) {
            this.$set(category, 'isOpen', true);
        } else {
            category.isOpen = !category.isOpen;
        }
    },

    toggleWrongCategory(key) {
        if (!this.wrongCategories) return;
        const category = this.wrongCategories[key];
        if (!category) return;

        if (!Object.prototype.hasOwnProperty.call(category, 'isOpen')) {
            this.$set(category, 'isOpen', true);
        } else {
            category.isOpen = !category.isOpen;
        }
    },

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
        } else if (this.questionMode === 'weak') {
            const q = this.weakPractice && Array.isArray(this.weakPractice.queue) ? this.weakPractice.queue : [];
            const idx = this.weakPractice && typeof this.weakPractice.index === 'number' ? this.weakPractice.index : -1;
            if (idx > 0) {
                const nextIdx = idx - 1;
                this.weakPractice.index = nextIdx;
                const item = q[nextIdx];
                if (item) {
                    this.loadQuestionForDisplay(item, 'practice');
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
        } else if (this.questionMode === 'weak') {
            const q = this.weakPractice && Array.isArray(this.weakPractice.queue) ? this.weakPractice.queue : [];
            const idx = this.weakPractice && typeof this.weakPractice.index === 'number' ? this.weakPractice.index : -1;
            if (idx >= 0 && idx < q.length - 1) {
                const nextIdx = idx + 1;
                this.weakPractice.index = nextIdx;
                const item = q[nextIdx];
                if (item) {
                    this.loadQuestionForDisplay(item, 'practice');
                    this.resetQuestionState();
                }
            } else {
                this.showInfo('已经是推荐列表中的最后一题了！');
            }
        }
    },
    resetQuestionState() {
        this.selectedOption = null;
        this.showAnswer = false;
        this.questionStats = {};
        // 切换题目后自动滚到页面顶部
        this.$nextTick(() => {
            window.scrollTo({ top: 0, behavior: 'smooth' });
        });
    },
    getPrevTrainingQuestion() {
        if (!this.currentQuestion) return null;
        const ids = this.trainingQuestions.map(q => q.id);
        const idx = ids.indexOf(this.currentQuestion.id);
        return idx > 0 ? ids[idx - 1] : null;
    },
    getNextTrainingQuestion() {
        if (!this.currentQuestion) return null;
        const ids = this.trainingQuestions.map(q => q.id);
        const idx = ids.indexOf(this.currentQuestion.id);
        return idx >= 0 && idx < ids.length - 1 ? ids[idx + 1] : null;
    },
    getPrevWrongQuestion() {
        if (!this.currentQuestion) return null;
        const ids = this.wrongQuestions; // preserve natural order
        const idx = ids.indexOf(this.currentQuestion.id);
        return idx > 0 ? ids[idx - 1] : null;
    },
    getNextWrongQuestion() {
        if (!this.currentQuestion) return null;
        const ids = this.wrongQuestions; // preserve natural order
        const idx = ids.indexOf(this.currentQuestion.id);
        return idx >= 0 && idx < ids.length - 1 ? ids[idx + 1] : null;
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
        // 替换 <br> 为换行符，保留换行格式
        const text = question.replace(/\\n/g, '\n').replace(/<br\s*\/?>/gi, '\n').replace(/<[^>]*>/g, '');
        // 如果需要截断，可以按长度截断，但保留换行符
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
    openSystemNotice() {
        if (!this.isLoggedIn) {
            this.showError('请先登录后查看系统公告');
            return;
        }
        // 已打开：直接刷新当前 tab（避免重复触发 watcher 产生并发竞态）
        if (this.showSystemNotice) {
            this.noticePage = 1;
            this.loadNotifications();
            return;
        }
        // 未打开：先清空旧缓存并置加载态，再打开弹窗；
        // 由 showSystemNotice watcher 触发唯一一次 loadNotifications，避免并发。
        this.noticePage = 1;
        this.notifications = [];
        this.loadingNotifications = true;
        this.showSystemNotice = true;
    },
    // 登录后自动检测未读公告并弹窗（每个浏览器会话仅自动弹一次；
    // 公告标记为已读后 unreadCount 归零，后续登录不再弹出）
    async checkLoginAnnouncements() {
        if (!this.isLoggedIn) return;
        try {
            if (sessionStorage.getItem('__login_announcement_shown__')) return;
        } catch (e) { /* sessionStorage 不可用时忽略 */ }
        if (!window.notificationApi || typeof window.notificationApi.getUnreadCount !== 'function') return;
        try {
            const res = await window.notificationApi.getUnreadCount();
            const count = (res && typeof res.unreadCount === 'number') ? res.unreadCount : 0;
            if (count > 0) {
                try { sessionStorage.setItem('__login_announcement_shown__', '1'); } catch (e) { /* ignore */ }
                // 先重置状态：清空旧缓存 + 置加载态，避免弹窗瞬间展示旧公告；
                // 再打开弹窗，由 showSystemNotice watcher 触发唯一一次 loadNotifications，
                // 不再显式调用，消除 watcher 与显式调用并发导致的竞态。
                this.systemNoticeTab = 'unread';
                this.noticePage = 1;
                this.notifications = [];
                this.loadingNotifications = true;
                this.showSystemNotice = true;
            }
        } catch (e) {
            console.warn('登录未读公告检测失败', e);
        }
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
        } else if (this.questionMode === 'random' || this.questionMode === 'jump' || this.questionMode === 'weak') {
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
    goToAnnouncementEditor() {
        window.location.href = 'announcement-editor.html';
    },
    goToAdminPermissions() {
        window.location.href = 'admin_permissions.html';
    },
    // ============ 通知中心相关方法 ============
    async loadNotifications() {
        if (!this.isLoggedIn) {
            this.notifications = [];
            this.unreadCount = 0;
            this.hasMoreNotifications = false;
            this.loadingNotifications = false;
            return;
        }
        if (!window.notificationApi) {
            this.notifications = [];
            this.unreadCount = 0;
            this.hasMoreNotifications = false;
            this.loadingNotifications = false;
            return;
        }
        // 请求令牌：仅采纳最新一次调用的结果，丢弃过时响应。
        // 修复竞态：showSystemNotice/systemNoticeTab 的 watcher 与显式调用可能并发触发，
        // 后 resolve 的旧响应（或失败的 catch）会覆盖新数据，导致弹窗偶发显示为空/旧公告。
        const reqId = (this.notifReqSeq = (this.notifReqSeq || 0) + 1);
        this.loadingNotifications = true;
        try {
            // 仅在「未读」tab 传 unreadOnly=true；「全部公告」tab 不传该参数，
            // 否则 URLSearchParams 会把 undefined 序列化为 "undefined"，
            // 后端 boolean 解析失败返回 400 -> 列表为空。
            const params = {
                page: this.noticePage,
                size: 10
            };
            if (this.systemNoticeTab === 'unread') {
                params.unreadOnly = true;
            }
            const res = await notificationApi.getNotifications(params);
            if (reqId !== this.notifReqSeq) return; // 已被更新的请求取代，丢弃过时结果
            const raw = res.notifications || [];
            if (window.notificationHelper && typeof window.notificationHelper.formatNotification === 'function') {
                this.notifications = raw.map(n => window.notificationHelper.formatNotification(n)).filter(Boolean);
            } else {
                this.notifications = raw;
            }
            this.unreadCount = res.unreadCount || 0;
            this.hasMoreNotifications = res.hasMore || false;
        } catch (e) {
            if (reqId !== this.notifReqSeq) return; // 丢弃过时请求的错误，不影响最新调用
            this.notifications = [];
            this.unreadCount = 0;
            this.hasMoreNotifications = false;
        } finally {
            // 仅当本请求仍是最新时才复位 loading，避免提前关闭加载态
            if (reqId === this.notifReqSeq) this.loadingNotifications = false;
        }
    },
    switchNoticeTab(tab) {
        this.systemNoticeTab = tab;
        // 切换 tab 立即刷新
        this.noticePage = 1;
        this.loadNotifications();
    },

    // 点击公告查看详情
    openNoticeDetail(notif) {
        if (!notif) return;
        this.currentNoticeDetail = {
            ...notif,
            // 保留 helper 生成的 formattedContent（含换行 <br>）
        };
        this.showNoticeDetail = true;
        // 点击查看时自动标记已读（可选）
        if (!notif.isRead) {
            this.markNotificationAsRead(notif);
        }
    },
    closeNoticeDetail() {
        this.showNoticeDetail = false;
        this.currentNoticeDetail = null;
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
    handleConfirmAction() {
        if (typeof this.confirmAction === 'function') {
            this.confirmAction();
        }
        this.showConfirmDialog = false;
    },

    // ============ 错题辑录操作 ============
    async clearWrongRecords() {
        if (!this.isLoggedIn) {
            this.showError('请先登录');
            return;
        }
        if (!confirm('确定要清除所有错题吗？')) return;

        // 尝试逐个同步到后端删除（如果后端支持）
        const ids = Array.isArray(this.wrongQuestions) ? this.wrongQuestions.slice() : [];
        for (const id of ids) {
            try {
                if (typeof this.removeFromWrongBook === 'function') {
                    await this.removeFromWrongBook(id);
                }
            } catch (e) {
                // ignore single failure; continue
            }
        }

        // 本地兜底清空
        this.wrongQuestions = [];
        this.wrongQuestionsDetail = [];
        this.updateWrongCategories();
        this.showSuccess('已清除所有错题');
    },

    async deleteWrongQuestion(questionId) {
        if (!this.isLoggedIn) {
            this.showError('请先登录');
            return;
        }
        if (!confirm('确定要删除该错题吗？')) return;
        try {
            if (typeof this.removeFromWrongBook === 'function') {
                await this.removeFromWrongBook(questionId);
                return;
            }
        } catch (e) {
            // ignore
        }

        // 本地兜底
        this.wrongQuestions = (this.wrongQuestions || []).filter(id => id !== questionId);
        this.wrongQuestionsDetail = (this.wrongQuestionsDetail || []).filter(q => q.id !== questionId);
        this.updateWrongCategories();
    },

    async deleteWrongCategory(key) {
        if (!this.isLoggedIn) {
            this.showError('请先登录');
            return;
        }
        const cat = (this.wrongCategories || {})[key];
        if (!cat || !Array.isArray(cat.questions) || cat.questions.length === 0) return;
        if (!confirm('确定要删除该分类下的所有错题吗？')) return;

        const ids = cat.questions.map(q => q.id);
        for (const id of ids) {
            try {
                if (typeof this.removeFromWrongBook === 'function') {
                    await this.removeFromWrongBook(id);
                }
            } catch (e) {
                // ignore
            }
        }

        // 本地兜底：再清一次，避免后端失败导致残留
        const toRemove = new Set(ids);
        this.wrongQuestions = (this.wrongQuestions || []).filter(id => !toRemove.has(id));
        this.wrongQuestionsDetail = (this.wrongQuestionsDetail || []).filter(q => !toRemove.has(q.id));
        this.updateWrongCategories();
    },

    // ================== 薄弱练习（推荐） ==================

    // 计算“推荐指数”画像：从错题记录统计易错类型、平均难度、易错关键词
    computeWeakProfile() {
        const wrong = Array.isArray(this.wrongQuestionsDetail) ? this.wrongQuestionsDetail : [];
        if (wrong.length === 0) return null;

        const typeCount = {};
        const keywordCount = {};
        let diffSum = 0;

        wrong.forEach(q => {
            if (!q) return;
            const t = q.type;
            if (t) typeCount[t] = (typeCount[t] || 0) + 1;
            diffSum += (q.difficulty || 0);

            // keywords 字段：可能是数组或字符串
            let kws = q.keywords;
            if (typeof kws === 'string') {
                kws = kws.split(/[,|\s]+/).map(s => s.trim()).filter(Boolean);
            }
            if (Array.isArray(kws)) {
                kws.forEach(k => {
                    if (!k) return;
                    keywordCount[k] = (keywordCount[k] || 0) + 1;
                });
            }
        });

        const avgDifficulty = wrong.length > 0 ? (diffSum / wrong.length) : 0;

        // 主导易错类型
        let dominantType = null;
        let dominantTypeCount = 0;
        Object.keys(typeCount).forEach(k => {
            const c = typeCount[k];
            if (c > dominantTypeCount) {
                dominantTypeCount = c;
                dominantType = parseInt(k);
            }
        });

        const keywordsTop = Object.entries(keywordCount)
            .sort((a, b) => b[1] - a[1])
            .slice(0, 5)
            .map(([k]) => k);

        return {
            wrongCount: wrong.length,
            avgDifficulty,
            dominantType,
            typeCount,
            keywordCount,
            keywordsTop
        };
    },

    // 推荐指数算法：对每道候选题计算一个 score，分数越高越推荐
    // 设计目标：
    //  - 类型匹配更重要
    //  - 与错题平均难度越接近越好（越难也略加分）
    //  - 关键词/Tag 匹配越多越好
    computeRecommendScore(question, profile) {
        if (!question || !profile) return 0;

        const typeWeight = 0.55;
        const diffWeight = 0.25;
        const kwWeight = 0.20;

        // 1) type score
        let typeScore = 0;
        if (profile.dominantType && question.type === profile.dominantType) {
            typeScore = 1;
        } else if (question.type && profile.typeCount && profile.typeCount[question.type]) {
            // 次优：也是常错类型之一
            typeScore = Math.min(0.7, profile.typeCount[question.type] / Math.max(1, profile.wrongCount));
        }

        // 2) difficulty score: closer to avg => higher; also slightly favor >= avg
        const avg = profile.avgDifficulty || 0;
        const d = question.difficulty || 0;
        const dist = Math.abs(d - avg);
        let diffScore = Math.max(0, 1 - dist / 4); // difficulty range 1-5 => dist max 4
        if (d >= avg) diffScore = Math.min(1, diffScore + 0.1);

        // 3) keyword score
        let kwScore = 0;
        const top = new Set(profile.keywordsTop || []);
        let kws = question.keywords;
        if (typeof kws === 'string') {
            kws = kws.split(/[,|\s]+/).map(s => s.trim()).filter(Boolean);
        }
        if (Array.isArray(kws) && kws.length > 0 && top.size > 0) {
            const hit = kws.filter(k => top.has(k)).length;
            kwScore = Math.min(1, hit / Math.min(3, top.size));
        }

        const score = typeWeight * typeScore + diffWeight * diffScore + kwWeight * kwScore;
        return score;
    },

    // 构建推荐队列：只在进入薄弱练习时计算一次
    buildWeakPracticeQueue(profile) {
        const pool = Array.isArray(this.rawQuestions) ? this.rawQuestions : [];
        const wrongSet = new Set((this.wrongQuestions || []).map(Number));

        // 过滤掉培训题、过滤掉 pool 为空
        const candidates = pool.filter(q => q && q.id && !wrongSet.has(Number(q.id)));

        // 计算分数并排序
        const scored = candidates
            .map(q => ({ q, score: this.computeRecommendScore(q, profile) }))
            .filter(x => x.score > 0)
            .sort((a, b) => b.score - a.score);

        // 取前 N；不足则返回空
        const N = 30;
        return scored.slice(0, N).map(x => x.q);
    },

    async startWeakPractice() {
        if (!this.isLoggedIn) {
            this.showError('请先登录');
            this.showAuthModal = true;
            this.authMode = 'login';
            return;
        }

        // 确保最新错题数据
        try {
            if (typeof this.loadWrongQuestions === 'function') {
                await this.loadWrongQuestions();
            }
        } catch (e) {
            // ignore
        }

        const minRequired = (this.weakPractice && this.weakPractice.minRequiredWrong) ? this.weakPractice.minRequiredWrong : 5;
        const wrongCount = Array.isArray(this.wrongQuestionsDetail) ? this.wrongQuestionsDetail.length : 0;
        if (wrongCount < minRequired) {
            this.showInfo('请多做一些题目再来吧');
            return;
        }

        const profile = this.computeWeakProfile();
        if (!profile) {
            this.showInfo('请多做一些题目再来吧');
            return;
        }

        const queue = this.buildWeakPracticeQueue(profile);
        if (!Array.isArray(queue) || queue.length === 0) {
            this.showInfo('暂无可推荐的相似题（可能题库太小或都做过了）');
            return;
        }

        this.weakPractice.profile = profile;
        this.weakPractice.queue = queue;
        this.weakPractice.index = 0;

        // 进入答题页，以 weak 模式导航
        this.questionMode = 'weak';
        const first = queue[0];
        this.loadQuestionForDisplay(first, 'practice');
        this.currentPage = 'question';
        this.resetQuestionState();
    },
};
