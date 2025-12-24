window._appData = {
    // 当前页面状态
    currentPage: 'index',
    practiceMode: 'type',
    sidebarOpen: false,
    // 数据存储
    categories: {},
    wrongCategories: {},
    rawQuestions: [],
    trainingQuestions: [],
    // 培训答题本地记录： { [id]: { attempts: number, correct: boolean, lastAt: timestamp } }
    trainingRecords: {},
    // 当前答题状态
    currentQuestion: null,
    currentQuestionIndex: 0,
    selectedOption: null,
    showAnswer: false,
    // 题目模式标识
    questionMode: '',
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
    wrongQuestions: [],
    wrongQuestionsDetail: [],
    // 随机题目历史
    randomHistory: [],
    randomCurrentIndex: -1,
    // 搜索相关
    searchKeyword: '',
    searchResults: [],
    // 当前在搜索练习中的索引（-1 表示未在搜索练习中）
    searchCurrentIndex: -1,
    // 题库练习上下文（用于实现按分类/区跳题）
    practiceContext: {
        categoryKey: '', // 例如 'type_1' 或 'difficulty_2'
        groups: [],      // [{ key: subgroupValue, questions: [q,...] }, ...]
        currentGroupIndex: 0,
        indexInGroup: 0
    },
    // 系统公告
    showSystemNotice: false,
    systemNoticeTab: 'unread', // 默认改为 'unread'
    selectedVersion: {},
    updateVersions: [],
    systemTips: '',
    // ================== 系统公告/通知中心相关 ==================
    notifications: [],
    unreadCount: 0,
    noticePage: 1,
    hasMoreNotifications: false,
    loadingNotifications: false,
    showConfirmDialog: false,
    confirmMessage: '',
    confirmAction: null,

    // 公告详情弹窗
    showNoticeDetail: false,
    currentNoticeDetail: null,

    // ================== 用户答题设置 ==================
    showAnswerSettingsModal: false,
    answerSettings: {
        autoSubmit: false,
        autoNextCorrect: false
    },
    _autoAnswerBusy: false,

    // ================== 薄弱练习（推荐） ==================
    weakPractice: {
        queue: [],          // 推荐题目列表（Question[]）
        index: -1,          // 当前在 queue 中的位置
        profile: null,      // { avgDifficulty, dominantType, keywordsTop: [..], scoreByType: {...} }
        minRequiredWrong: 5 // 记录太少时的阈值
    },
};