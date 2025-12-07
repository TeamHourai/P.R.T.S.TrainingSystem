// app.js - 博士考核系统前端应用
// 完全基于后端API的数据交互版本
new Vue({
    el: '#app',
    data: {
        // 当前页面状态
        currentPage: 'index',
        practiceMode: 'type',
        sidebarOpen: false,

        // 数据存储
        categories: {},
        wrongCategories: {},
        rawQuestions: [],           // 从后端获取的题库
        trainingQuestions: [],      // 从后端获取的培训题目

        // 当前答题状态
        currentQuestion: null,
        currentQuestionIndex: 0,
        selectedOption: null,
        showAnswer: false,

        // 题目模式标识
        questionMode: '', // 'practice', 'random', 'jump', 'training', 'wrong'

        // 快速跳题
        jumpQuestionId: '',

        // 用户相关
        showAuthModal: false,
        authMode: 'login',
        authUsername: '',
        authPassword: '',
        isLoggedIn: false,
        userInfo: {},
        isAdmin: false,

        // 统计信息
        questionStats: {},
        examStats: { totalAttempts: 0, averageScore: 0 },

        // 错题相关
        wrongQuestions: [],          // 存储错题ID数组
        wrongQuestionsDetail: [],    // 存储错题详情

        // 随机题目历史
        randomHistory: [],
        randomCurrentIndex: -1,

        // 搜索相关
        searchKeyword: '',
        searchResults: [],

        // 系统公告
        showSystemNotice: false,
        systemNoticeTab: 'tips',
        selectedVersion: {},
        updateVersions: [],
        systemTips: ''
    },

    computed: {
        // 是否有上一题
        hasPrevQuestion() {
            if (this.questionMode === 'practice') {
                return this.currentQuestionIndex > 0;
            } else if (this.questionMode === 'random') {
                return this.randomCurrentIndex > 0;
            } else if (this.questionMode === 'jump') {
                return this.currentQuestion && this.currentQuestion.id > 1;
            } else if (this.questionMode === 'training') {
                const prevId = this.getPrevTrainingQuestion();
                return prevId !== null;
            } else if (this.questionMode === 'wrong') {
                const prevId = this.getPrevWrongQuestion();
                return prevId !== null;
            }
            return false;
        },

        // 是否有下一题
        hasNextQuestion() {
            if (this.questionMode === 'practice') {
                return this.currentQuestionIndex < this.rawQuestions.length - 1;
            } else if (this.questionMode === 'random') {
                return true; // 随机模式永远可以有下一题
            } else if (this.questionMode === 'jump') {
                return this.currentQuestion && this.currentQuestion.id < this.rawQuestions.length;
            } else if (this.questionMode === 'training') {
                const nextId = this.getNextTrainingQuestion();
                return nextId !== null;
            } else if (this.questionMode === 'wrong') {
                const nextId = this.getNextWrongQuestion();
                return nextId !== null;
            }
            return false;
        },

        // 答案是否正确
        isAnswerCorrect() {
            return this.selectedOption === this.currentQuestion.answer;
        },

        // 错题平均难度
        averageDifficulty() {
            if (this.wrongQuestionsDetail.length === 0) return 0;
            const sum = this.wrongQuestionsDetail.reduce((total, question) => {
                return total + (question.difficulty || 0);
            }, 0);
            return sum / this.wrongQuestionsDetail.length;
        },

        // 最易错类型
        mostWrongType() {
            if (this.wrongQuestionsDetail.length === 0) return '无';
            const typeCount = {};
            this.wrongQuestionsDetail.forEach(question => {
                const typeText = this.getTypeText(question.type);
                typeCount[typeText] = (typeCount[typeText] || 0) + 1;
            });

            let maxType = '无';
            let maxCount = 0;
            Object.entries(typeCount).forEach(([type, count]) => {
                if (count > maxCount) {
                    maxType = type;
                    maxCount = count;
                }
            });
            return maxType;
        }
    },

    watch: {
        // 监听练习模式变化
        practiceMode() {
            this.updateCategories();
        },

        // 监听登录状态变化
        isLoggedIn(newVal) {
            if (newVal) {
                this.loadUserData();
            }
        }
    },

    async mounted() {
        try {
            console.log('博士考核系统初始化...');

            // 1. 检查用户登录状态
            await this.checkLoginStatus();

            // 2. 加载题库数据（无论是否登录都需要）
            await this.loadQuestions();

            // 3. 加载培训题目
            await this.loadTrainingQuestions();

            // 4. 初始化分类数据
            this.updateCategories();

            // 5. 加载考试统计
            await this.loadExamStats();

            // 6. 加载系统数据
            this.loadSystemData();

            console.log('系统初始化完成');

            // 点击侧边栏外部关闭侧边栏
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
    },

    methods: {
        // ============ 用户认证相关方法 ============

        // 检查登录状态
        async checkLoginStatus() {
            try {
                const token = localStorage.getItem('token') || sessionStorage.getItem('token');
                if (!token) {
                    this.isLoggedIn = false;
                    return;
                }

                // 使用认证API检查登录状态
                if (window.userApi && typeof userApi.checkLoginStatus === 'function') {
                    const isLoggedIn = await userApi.checkLoginStatus();
                    this.isLoggedIn = isLoggedIn;

                    if (isLoggedIn) {
                        // 获取当前用户信息
                        const user = await userApi.getCurrentUser();
                        if (user) {
                            this.userInfo = user;
                            this.isAdmin = user.isAdmin || false;
                        }
                    }
                } else {
                    // 降级方案：检查本地存储
                    const userData = localStorage.getItem('userInfo') || sessionStorage.getItem('userInfo');
                    this.isLoggedIn = !!userData;
                    if (userData) {
                        this.userInfo = JSON.parse(userData);
                        this.isAdmin = this.userInfo.isAdmin || false;
                    }
                }
            } catch (error) {
                console.error('检查登录状态失败:', error);
                this.isLoggedIn = false;
            }
        },

        // 用户登录
        async handleLogin() {
            if (!this.authUsername || !this.authPassword) {
                this.showError('请输入用户名和密码');
                return;
            }

            try {
                const result = await userApi.login(this.authUsername, this.authPassword, true);

                if (result.success) {
                    this.isLoggedIn = true;
                    this.userInfo = result.user || {};
                    this.isAdmin = result.isAdmin || false;
                    this.showAuthModal = false;
                    this.authUsername = '';
                    this.authPassword = '';

                    // 加载用户相关数据
                    await this.loadUserData();

                    this.showSuccess('登录成功！');
                } else {
                    this.showError(result.message || '登录失败');
                }
            } catch (error) {
                console.error('登录失败:', error);
                this.showError('登录失败：' + (error.message || '服务器错误'));
            }
        },

        // 用户注册
        async handleRegister() {
            if (!this.authUsername || !this.authPassword) {
                this.showError('请输入用户名和密码');
                return;
            }

            if (this.authUsername.length < 3) {
                this.showError('用户名至少需要3个字符');
                return;
            }

            if (this.authPassword.length < 6) {
                this.showError('密码至少需要6个字符');
                return;
            }

            try {
                const result = await userApi.register(this.authUsername, this.authPassword, '');

                if (result.success) {
                    this.showSuccess('注册成功！请登录');
                    this.authMode = 'login';
                } else {
                    this.showError(result.message || '注册失败');
                }
            } catch (error) {
                console.error('注册失败:', error);
                this.showError('注册失败：' + (error.message || '服务器错误'));
            }
        },

        // 用户退出
        async handleLogout() {
            try {
                await userApi.logout();
                this.isLoggedIn = false;
                this.userInfo = {};
                this.isAdmin = false;

                // 清除用户相关数据
                this.wrongQuestions = [];
                this.wrongQuestionsDetail = [];

                this.showSuccess('已退出登录');
            } catch (error) {
                console.error('退出登录失败:', error);
            }
        },

        // 加载用户相关数据
        async loadUserData() {
            try {
                // 加载错题本
                await this.loadWrongQuestions();

                // 加载考试统计
                await this.loadExamStats();
            } catch (error) {
                console.error('加载用户数据失败:', error);
            }
        },

        // ============ 数据加载方法 ============

        // 从后端加载题库
        async loadQuestions() {
            try {
                console.log('正在从后端加载题库...');

                if (!window.questionApi) {
                    throw new Error('questionApi 未定义');
                }

                // 使用API获取题目列表
                const response = await questionApi.getQuestions({
                    page: 1,
                    size: 1000, // 获取足够多的题目
                    keyword: ''
                });

                if (response && response.questions) {
                    this.rawQuestions = response.questions;
                    console.log(`题库加载成功，共${this.rawQuestions.length}题`);
                } else if (Array.isArray(response)) {
                    // 如果API直接返回数组
                    this.rawQuestions = response;
                    console.log(`题库加载成功，共${this.rawQuestions.length}题`);
                } else {
                    console.warn('题库数据格式异常:', response);
                    this.rawQuestions = [];
                }
            } catch (error) {
                console.error('加载题库失败:', error);
                this.rawQuestions = [];
                this.showError('加载题库失败，请检查网络连接');
            }
        },

        // 从后端加载培训题目
        async loadTrainingQuestions() {
            try {
                console.log('正在从后端加载培训题目...');

                if (!window.trainingQuestionApi) {
                    throw new Error('trainingQuestionApi 未定义');
                }

                // 使用API获取培训题目列表
                const response = await trainingQuestionApi.getTrainingQuestions({
                    page: 1,
                    size: 100 // 假设培训题目不超过100题
                });

                if (response && response.questions) {
                    this.trainingQuestions = response.questions;
                    console.log(`培训题目加载成功，共${this.trainingQuestions.length}题`);
                } else if (Array.isArray(response)) {
                    // 如果API直接返回数组
                    this.trainingQuestions = response;
                    console.log(`培训题目加载成功，共${this.trainingQuestions.length}题`);
                } else {
                    console.warn('培训题目数据格式异常:', response);
                    this.trainingQuestions = [];
                }
            } catch (error) {
                console.error('加载培训题目失败:', error);
                this.trainingQuestions = [];
                this.showError('加载培训题目失败，请检查网络连接');
            }
        },

        // 从后端加载错题本
        async loadWrongQuestions() {
            if (!this.isLoggedIn) {
                this.wrongQuestions = [];
                this.wrongQuestionsDetail = [];
                return;
            }

            try {
                console.log('正在从后端加载错题本...');

                if (!window.answerApi) {
                    throw new Error('answerApi 未定义');
                }

                // 使用API获取错题本
                const response = await answerApi.getWrongQuestions({
                    page: 1,
                    size: 1000
                });

                if (response && response.history) {
                    // 提取错题ID和详情
                    this.wrongQuestions = response.history
                        .filter(record => !record.isCorrect)
                        .map(record => record.questionId);

                    // 获取错题详情
                    this.wrongQuestionsDetail = [];
                    for (const record of response.history) {
                        if (!record.isCorrect) {
                            // 从本地题库中查找题目详情
                            const question = this.rawQuestions.find(q => q.id === record.questionId);
                            if (question) {
                                this.wrongQuestionsDetail.push(question);
                            }
                        }
                    }

                    console.log(`错题本加载成功，共${this.wrongQuestions.length}题`);
                    this.updateWrongCategories();
                }
            } catch (error) {
                console.error('加载错题本失败:', error);
                this.wrongQuestions = [];
                this.wrongQuestionsDetail = [];
            }
        },

        // 加载题目统计信息
        async loadQuestionStats(questionId, questionType) {
            if (!questionId) return;

            try {
                if (window.statsApi) {
                    this.questionStats = await statsApi.getQuestionStats(questionId);
                } else {
                    // 如果没有statsApi，使用模拟数据
                    this.questionStats = {
                        totalAttempts: Math.floor(Math.random() * 500) + 100,
                        correctRate: Math.random() * 0.3 + 0.6,
                        mostCommonWrongOption: Math.floor(Math.random() * 4) + 1
                    };
                }
            } catch (error) {
                console.error('加载题目统计失败:', error);
                this.questionStats = {};
            }
        },

        // 加载考试统计信息
        async loadExamStats() {
            try {
                // 如果有考试API，使用API获取统计
                if (window.examApi) {
                    // 获取考试历史来统计
                    const history = await examApi.getExamHistory({ page: 1, size: 100 });
                    if (history && history.exams) {
                        const totalAttempts = history.total || history.exams.length;
                        const totalScore = history.exams.reduce((sum, exam) => sum + (exam.score || 0), 0);
                        const averageScore = totalAttempts > 0 ? totalScore / totalAttempts : 0;

                        this.examStats = {
                            totalAttempts: totalAttempts,
                            averageScore: averageScore
                        };
                    }
                } else {
                    // 模拟数据
                    this.examStats = {
                        totalAttempts: 1234,
                        averageScore: 78.5
                    };
                }
            } catch (error) {
                console.error('加载考试统计失败:', error);
                this.examStats = {
                    totalAttempts: 0,
                    averageScore: 0
                };
            }
        },

        // 加载系统数据
        loadSystemData() {
            // 这里可以加载系统公告、更新日志等
            // 目前使用静态数据
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
        },

        // ============ 题目分类和显示方法 ============

        // 更新题目分类
        updateCategories() {
            const newCategories = {};

            if (this.practiceMode === 'type') {
                // 按类型分类
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
                // 按难度分类
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

        // 更新错题分类
        updateWrongCategories() {
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

        // ============ 页面导航方法 ============

        toggleSidebar() {
            this.sidebarOpen = !this.sidebarOpen;
        },

        // 页面跳转
        goToPage(page) {
            this.currentPage = page;
            this.selectedOption = null;
            this.showAnswer = false;

            if (page === 'practice') {
                this.updateCategories();
            } else if (page === 'wrong') {
                this.updateWrongCategories();
            } else if (page === 'search') {
                // 进入搜索页面时重置搜索状态
                this.searchKeyword = '';
                this.searchResults = [];
            }

            // 关闭侧边栏（移动端）
            if (window.innerWidth < 768) {
                this.sidebarOpen = false;
            }
        },

        // ============ 题目操作方法 ============

        // 跳转到题目（普通题库）
        goToQuestion(id, mode) {
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

        // 跳转到培训题目
        goToTrainingQuestion(id) {
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

        // 跳转到错题
        goToWrongQuestion(id) {
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

        // 加载题目并格式化显示
        loadQuestionForDisplay(question, mode) {
            // 格式化题目文本
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

        // 选择答案选项
        selectOption(option) {
            if (!this.showAnswer) {
                this.selectedOption = option;
            }
        },

        // 提交答案
        async checkAnswer() {
            if (!this.selectedOption) {
                this.showError('请先选择一个答案');
                return;
            }

            if (!this.isLoggedIn && this.questionMode !== 'training') {
                this.showError('请先登录以保存答题记录');
                // 继续显示答案，但不保存记录
                this.showAnswer = true;
                return;
            }

            this.showAnswer = true;

            try {
                // 保存答题记录到后端
                if (window.answerApi && this.isLoggedIn) {
                    await answerApi.submitAnswer(
                        this.currentQuestion.id,
                        this.questionMode === 'training' ? 'training' : 'normal',
                        this.selectedOption
                    );
                }

                // 如果是错题，更新错题本
                if (!this.isAnswerCorrect && this.questionMode !== 'training') {
                    await this.addToWrongBook(this.currentQuestion.id);
                }

                // 加载题目统计
                await this.loadQuestionStats(
                    this.currentQuestion.id,
                    this.questionMode === 'training' ? 'training' : 'normal'
                );

                // 滚动到解析部分
                this.$nextTick(() => {
                    const analysisElement = this.$refs.answerAnalysis;
                    if (analysisElement) {
                        analysisElement.scrollIntoView({ behavior: 'smooth', block: 'start' });
                    }
                });

            } catch (error) {
                console.error('提交答案失败:', error);
                // 即使提交失败也显示答案
            }
        },

        // 添加到错题本
        async addToWrongBook(questionId) {
            if (!this.isLoggedIn) return;

            try {
                // 如果错题本中没有这道题，就添加
                if (!this.wrongQuestions.includes(questionId)) {
                    this.wrongQuestions.push(questionId);

                    // 获取题目详情
                    const question = this.rawQuestions.find(q => q.id === questionId);
                    if (question) {
                        this.wrongQuestionsDetail.push(question);
                        this.updateWrongCategories();
                    }

                    // 这里可以调用API同步到服务器
                    // 注意：实际API可能需要单独的错误记录接口
                }
            } catch (error) {
                console.error('添加错题失败:', error);
            }
        },

        // 从错题本移除
        async removeFromWrongBook(questionId) {
            if (!this.isLoggedIn) return;

            try {
                // 本地移除
                this.wrongQuestions = this.wrongQuestions.filter(id => id !== questionId);
                this.wrongQuestionsDetail = this.wrongQuestionsDetail.filter(q => q.id !== questionId);
                this.updateWrongCategories();

                // 调用API从服务器移除
                if (window.answerApi) {
                    await answerApi.removeWrongQuestion(questionId);
                }
            } catch (error) {
                console.error('移除错题失败:', error);
            }
        },

        // ============ 题目导航方法 ============

        // 上一题
        prevQuestion() {
            if (this.questionMode === 'practice') {
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
            }
        },

        // 下一题
        nextQuestion() {
            if (this.questionMode === 'practice') {
                if (this.currentQuestionIndex < this.rawQuestions.length - 1) {
                    this.currentQuestionIndex++;
                    const question = this.rawQuestions[this.currentQuestionIndex];
                    this.loadQuestionForDisplay(question, 'practice');
                    this.resetQuestionState();
                }
            } else if (this.questionMode === 'random') {
                // 随机下一题
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
            }
        },

        // 重置题目状态
        resetQuestionState() {
            this.selectedOption = null;
            this.showAnswer = false;
            this.questionStats = {};
        },

        // ============ 辅助方法 ============

        // 获取前一题培训题目ID
        getPrevTrainingQuestion() {
            if (!this.currentQuestion) return null;
            const currentId = this.currentQuestion.id;
            const prevQuestions = this.trainingQuestions
                .filter(q => q.id < currentId)
                .sort((a, b) => b.id - a.id);
            return prevQuestions.length > 0 ? prevQuestions[0].id : null;
        },

        // 获取后一题培训题目ID
        getNextTrainingQuestion() {
            if (!this.currentQuestion) return null;
            const currentId = this.currentQuestion.id;
            const nextQuestions = this.trainingQuestions
                .filter(q => q.id > currentId)
                .sort((a, b) => a.id - b.id);
            return nextQuestions.length > 0 ? nextQuestions[0].id : null;
        },

        // 获取前一题错题ID
        getPrevWrongQuestion() {
            if (!this.currentQuestion) return null;
            const currentId = this.currentQuestion.id;
            const wrongIds = this.wrongQuestions.sort((a, b) => a - b);
            const currentIndex = wrongIds.indexOf(currentId);
            return currentIndex > 0 ? wrongIds[currentIndex - 1] : null;
        },

        // 获取后一题错题ID
        getNextWrongQuestion() {
            if (!this.currentQuestion) return null;
            const currentId = this.currentQuestion.id;
            const wrongIds = this.wrongQuestions.sort((a, b) => a - b);
            const currentIndex = wrongIds.indexOf(currentId);
            return currentIndex < wrongIds.length - 1 ? wrongIds[currentIndex + 1] : null;
        },

        // ============ 快速跳题方法 ============

        // 开始随机练习
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

        // 跳转到指定题目
        startJump() {
            const input = this.jumpQuestionId.trim();

            // 处理G+题号格式（入职培训）
            if (input.toUpperCase().startsWith('G')) {
                const id = parseInt(input.substring(1));
                if (id >= 1 && id <= this.trainingQuestions.length) {
                    this.questionMode = 'training';
                    this.goToTrainingQuestion(id);
                } else {
                    this.showError(`请输入有效的入职培训题目ID（G1-G${this.trainingQuestions.length}）`);
                }
            }
            // 处理普通题号
            else {
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

        // ============ 搜索功能方法 ============

        // 执行搜索
        performSearch() {
            if (!this.searchKeyword.trim()) {
                this.searchResults = [];
                return;
            }

            const keyword = this.searchKeyword.toLowerCase().trim();

            // 在普通题库中搜索
            this.searchResults = this.rawQuestions.filter(question => {
                // 检查题干
                if (question.question && question.question.toLowerCase().includes(keyword)) {
                    return true;
                }

                // 检查选项
                if (question.options && question.options.some(opt =>
                    opt && opt.toLowerCase().includes(keyword))) {
                    return true;
                }

                // 检查关键词
                if (question.keywords && question.keywords.some(kw =>
                    kw && kw.toLowerCase().includes(keyword))) {
                    return true;
                }

                // 检查解析
                if (question.analysis && question.analysis.toLowerCase().includes(keyword)) {
                    return true;
                }

                return false;
            });
        },

        // 跳转到搜索结果题目
        goToSearchResult(questionId) {
            this.goToQuestion(questionId, 'practice');
        },

        // 截断过长的题干
        truncateQuestion(question) {
            if (!question) return '';
            // 移除HTML标签和换行符
            const text = question.replace(/<br>/g, ' ').replace(/<[^>]*>/g, '');
            return text.length > 120 ? text.substring(0, 120) + '...' : text;
        },

        // ============ 工具方法 ============

        // 获取类型文本
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

        // 获取难度文本
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

        // 获取类型颜色
        getTypeColor(type) {
            const typeColors = {
                1: '#E91E63', // 干员调配 - 粉色
                2: '#9C27B0', // 空间部署 - 深紫
                3: '#3F51B5', // 效能审计 - 靛蓝
                4: '#009688', // 横向分析 - 青绿
                5: '#FF5722'  // 作战环境 - 深橙
            };
            return typeColors[type] || '#666';
        },

        // 获取难度颜色
        getDifficultyColor(difficulty) {
            const difficultyColors = {
                1: '#43A047', // 常识 - 绿色
                2: '#7E57C2', // 基操 - 紫色
                3: '#2196F3', // 娴熟 - 蓝色
                4: '#FF9800', // 明智 - 橙色
                5: '#F44336'  // 深邃 - 红色
            };
            return difficultyColors[difficulty] || '#666';
        },

        // 获取题目颜色（根据当前模式）
        getQuestionColor(question) {
            if (this.practiceMode === 'type') {
                return this.getDifficultyColor(question.difficulty);
            } else {
                return this.getTypeColor(question.type);
            }
        },

        // ============ 消息提示方法 ============

        showError(message) {
            alert(`错误：${message}`);
        },

        showSuccess(message) {
            alert(`成功：${message}`);
        },

        showInfo(message) {
            alert(message);
        },

        // ============ 其他页面方法 ============

        // 开始考试
        startExam() {
            if (!this.isLoggedIn) {
                this.showError('请先登录以参加考试');
                this.showAuthModal = true;
                this.authMode = 'login';
                return;
            }

            window.location.href = 'exam.html';
        },

        // 返回按钮
        goBackFromQuestion() {
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

        // 打开编辑器
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

        // 切换分类展开状态
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

        // 删除错题分类
        deleteWrongCategory(key) {
            if (confirm('确定要删除这个分类的所有错题吗？')) {
                const type = parseInt(key.split('_')[1]);
                this.wrongQuestionsDetail = this.wrongQuestionsDetail.filter(q => q.type !== type);
                this.wrongQuestions = this.wrongQuestionsDetail.map(q => q.id);
                this.updateWrongCategories();
            }
        },

        // 删除单个错题
        deleteWrongQuestion(id) {
            if (confirm('确定要删除这道错题吗？')) {
                this.removeFromWrongBook(id);
            }
        },

        // 清除所有错题
        clearWrongRecords() {
            if (confirm('确定要清除所有错题记录吗？')) {
                this.wrongQuestions = [];
                this.wrongQuestionsDetail = [];
                this.updateWrongCategories();
                this.showSuccess('已清除所有错题记录');
            }
        },

        // 培训相关方法（简化版）
        goToFirstUnansweredTraining() {
            // 这里可以根据后端记录判断哪些题目已做
            // 目前简单跳转到第一题
            if (this.trainingQuestions.length > 0) {
                this.goToTrainingQuestion(this.trainingQuestions[0].id);
            } else {
                this.showError('暂无培训题目');
            }
        }
    }
});
