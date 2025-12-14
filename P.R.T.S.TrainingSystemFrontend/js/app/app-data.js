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
    // 系统公告
    showSystemNotice: false,
    systemNoticeTab: 'all',
    selectedVersion: {},
    updateVersions: [],
    systemTips: '',
    // ================== 系统公告/通知中心相关 ==================
    notifications: [],
    localNotifications: [],
    unreadCount: 0,
    noticePage: 1,
    hasMoreNotifications: false,
    loadingNotifications: false,
    showConfirmDialog: false,
    confirmMessage: '',
    confirmAction: null,
};
