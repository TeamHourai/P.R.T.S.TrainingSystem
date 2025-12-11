// config.js - 博士考核系统配置文件
(function () {
    'use strict';

    // ==================== 系统配置 ====================
    const CONFIG = {
        // 系统信息
        SYSTEM: {
            NAME: '博士业务能力考核系统',
            VERSION: '2.0.0',
            DESCRIPTION: '明日方舟博士业务能力考核平台',
            AUTHOR: '罗德岛制药',
            COPYRIGHT: '© 2025 罗德岛制药',
            SUPPORT_EMAIL: 'support@arknights-exam.com'
        },

        // API 配置
        API: {
            // 后端API地址
            BASE_URL: getApiBaseUrl(),

            // API版本：针对当前后端设为空（后端已同时支持 /api 与无前缀）
            VERSION: '',

            // API前缀：可为 'api' 或 ''，但实际请求中会使用 FULL_BASE_URL + endpoint
            PREFIX: 'api',

            // 完整API路径（改为更稳健的拼接方式）
            get FULL_BASE_URL() {
                const prefix = this.PREFIX ? (this.PREFIX.startsWith('/') ? this.PREFIX : '/' + this.PREFIX) : '';
                const version = this.VERSION ? (this.VERSION.startsWith('/') ? this.VERSION : '/' + this.VERSION) : '';
                const base = this.BASE_URL.endsWith('/') ? this.BASE_URL.slice(0, -1) : this.BASE_URL;
                return `${base}${prefix}${version}`;
            },

            // API端点（尽量与后端 Main 注册的路径保持一致）
            ENDPOINTS: {
                AUTH: {
                    REGISTER: '/register',
                    LOGIN: '/login',
                    LOGOUT: '/logout',
                    PROFILE: '/profile'
                },
                QUESTIONS: {
                    LIST: '/questions',
                    DETAIL: '/questions/{id}',
                    CREATE: '/questions',
                    UPDATE: '/questions/{id}',
                    DELETE: '/questions/{id}',
                    SEARCH: '/questions/search'
                },
                TRAINING_QUESTIONS: {
                    LIST: '/training/questions',
                    DETAIL: '/training/questions/{id}',
                    CREATE: '/training/questions',
                    UPDATE: '/training/questions/{id}',
                    DELETE: '/training/questions/{id}'
                },
                ANSWERS: {
                    SUBMIT: '/exam/submit',
                    HISTORY: '/answers/history',
                    WRONG: '/user/{userId}/wrong',
                    DELETE_WRONG: '/answers/wrong/{questionId}'
                },
                EXAMS: {
                    GENERATE: '/exam/paper',
                    SUBMIT: '/exam/submit',
                    HISTORY: '/exam/history',
                    DETAIL: '/exam/{examId}',
                    LEADERBOARD: '/exams/leaderboard'
                },
                STATS: {
                    USER: '/stats/user',
                    QUESTION: '/stats/question/{questionId}',
                    SYSTEM: '/stats/system',
                    EXAM: '/stats/exam'
                },
                UPLOAD: {
                    QUESTION_IMAGE: '/upload/question-image',
                    AVATAR: '/upload/avatar'
                },
                SYSTEM: {
                    PING: '/ping',
                    INFO: '/system/info',
                    TIME: '/system/time',
                    FEEDBACK: '/system/feedback'
                },
                EXPORT: {
                    ANSWERS: '/export/answers',
                    EXAM_REPORT: '/export/exam-report/{examId}',
                    USER_STATS: '/export/user-stats'
                },
                ADMIN: {
                    USERS: '/admin/users',
                    USER: '/admin/users/{userId}',
                    CONFIG: '/admin/config'
                }
            },

            // 超时设置（毫秒）
            TIMEOUT: {
                DEFAULT: 15000,
                UPLOAD: 30000,
                DOWNLOAD: 60000
            },

            // 重试配置
            RETRY: {
                MAX_ATTEMPTS: 3,
                DELAY: 1000,
                BACKOFF_MULTIPLIER: 2
            }
        },

        // 题目配置
        QUESTIONS: {
            // 题目类型映射
            TYPES: {
                1: '干员调配与特性化决策',
                2: '空间部署与极致化战术',
                3: '效能审计与生态位界定',
                4: '横向分析与竞争力评估',
                5: '作战环境与档案类记录'
            },

            // 难度映射
            DIFFICULTIES: {
                1: '常识',
                2: '基操',
                3: '娴熟',
                4: '明智',
                5: '深邃'
            },

            // 颜色映射
            COLORS: {
                TYPE: {
                    1: '#E91E63', // 粉色
                    2: '#9C27B0', // 深紫
                    3: '#3F51B5', // 靛蓝
                    4: '#009688', // 青绿
                    5: '#FF5722'  // 深橙
                },
                DIFFICULTY: {
                    1: '#43A047', // 绿色
                    2: '#7E57C2', // 紫色
                    3: '#2196F3', // 蓝色
                    4: '#FF9800', // 橙色
                    5: '#F44336'  // 红色
                }
            },

            // 选项字母
            OPTION_LETTERS: ['A', 'B', 'C', 'D'],

            // 默认选项数量
            DEFAULT_OPTION_COUNT: 4,

            // 验证规则
            VALIDATION: {
                MIN_QUESTION_LENGTH: 5,
                MIN_OPTION_LENGTH: 1,
                MAX_QUESTION_LENGTH: 1000,
                MAX_OPTION_LENGTH: 500
            }
        },

        // 考试配置
        EXAM: {
            // 考试类型
            TYPES: {
                FULL: 'full',      // 全真模拟
                PRACTICE: 'practice', // 练习模式
                TIMED: 'timed'     // 限时练习
            },

            // 默认考试配置
            DEFAULT: {
                QUESTION_COUNT: 25,
                TIME_LIMIT: 900,     // 15分钟（秒）
                PASSING_SCORE: 60    // 及格分数（百分比）
            },

            // 时间配置（毫秒）
            TIMING: {
                WARNING_THRESHOLD: 300000, // 5分钟警告
                SUBMIT_BUFFER: 10000,      // 10秒提交缓冲
                AUTO_SAVE_INTERVAL: 30000  // 30秒自动保存
            }
        },

        // 文件上传配置
        UPLOAD: {
            // 题目图片
            QUESTION_IMAGE: {
                ALLOWED_TYPES: ['image/jpeg', 'image/jpg', 'image/png', 'image/gif'],
                MAX_SIZE: 5 * 1024 * 1024, // 5MB
                MAX_WIDTH: 1200,
                MAX_HEIGHT: 1200,
                COMPRESS_QUALITY: 0.8
            },

            // 用户头像
            AVATAR: {
                ALLOWED_TYPES: ['image/jpeg', 'image/jpg', 'image/png'],
                MAX_SIZE: 2 * 1024 * 1024, // 2MB
                MIN_WIDTH: 100,
                MIN_HEIGHT: 100,
                MAX_WIDTH: 500,
                MAX_HEIGHT: 500
            },

            // 通用配置
            COMMON: {
                CHUNK_SIZE: 1024 * 1024, // 1MB分片
                MAX_RETRIES: 3,
                PARALLEL_UPLOADS: 2
            }
        },

        // 用户配置
        USER: {
            // 验证规则
            VALIDATION: {
                USERNAME: {
                    MIN_LENGTH: 3,
                    MAX_LENGTH: 20,
                    PATTERN: /^[a-zA-Z0-9_\u4e00-\u9fa5]+$/ // 字母数字中文下划线
                },
                PASSWORD: {
                    MIN_LENGTH: 6,
                    MAX_LENGTH: 50
                },
                EMAIL: {
                    PATTERN: /^[^\s@]+@[^\s@]+\.[^\s@]+$/
                }
            },

            // 角色权限
            ROLES: {
                GUEST: 'guest',     // 游客
                USER: 'user',       // 普通用户
                ADMIN: 'admin',     // 管理员
                SUPER_ADMIN: 'super_admin' // 超级管理员
            },

            // 默认设置
            DEFAULTS: {
                AVATAR: '/assets/images/default-avatar.png',
                THEME: 'light',
                LANGUAGE: 'zh-CN'
            }
        },

        // 本地存储配置
        STORAGE: {
            // 键名前缀
            PREFIX: 'ark_exam_',

            // 存储键名
            KEYS: {
                TOKEN: 'auth_token',
                USER_INFO: 'user_info',
                REMEMBER_ME: 'remember_me',
                LAST_LOGIN: 'last_login_time',
                SESSION_EXPIRE: 'session_expire_time',
                SETTINGS: 'user_settings',
                TRAINING_RECORDS: 'training_records',
                WRONG_QUESTIONS: 'wrong_questions',
                EXAM_HISTORY: 'exam_history',
                SEARCH_HISTORY: 'search_history',
                NOTIFICATIONS: 'notifications'
            },

            // 过期时间（毫秒）
            EXPIRY: {
                SESSION: 24 * 60 * 60 * 1000,      // 24小时
                REMEMBER_ME: 30 * 24 * 60 * 60 * 1000, // 30天
                CACHE: 60 * 60 * 1000,             // 1小时
                TEMP: 10 * 60 * 1000               // 10分钟
            }
        },

        // 界面配置
        UI: {
            // 分页配置
            PAGINATION: {
                DEFAULT_PAGE_SIZE: 20,
                PAGE_SIZES: [10, 20, 50, 100],
                MAX_PAGE_BUTTONS: 7
            },

            // 加载配置
            LOADING: {
                MIN_SHOW_TIME: 300,     // 最小显示时间（毫秒）
                DEBOUNCE_DELAY: 200,    // 防抖延迟
                SPINNER_SIZE: '40px'
            },

            // 动画配置
            ANIMATION: {
                DURATION: {
                    FAST: 150,
                    NORMAL: 300,
                    SLOW: 500
                },
                EASING: 'cubic-bezier(0.4, 0, 0.2, 1)'
            },

            // 响应式断点
            BREAKPOINTS: {
                XS: 576,
                SM: 768,
                MD: 992,
                LG: 1200,
                XL: 1400
            },

            // 主题颜色
            THEME: {
                PRIMARY: '#1890ff',
                SUCCESS: '#52c41a',
                WARNING: '#faad14',
                ERROR: '#f5222d',
                INFO: '#1890ff',
                BACKGROUND: '#f0f2f5',
                CARD_BACKGROUND: '#ffffff',
                TEXT_PRIMARY: '#000000',
                TEXT_SECONDARY: '#666666',
                BORDER: '#d9d9d9'
            }
        },

        // 功能开关
        FEATURES: {
            // 是否启用功能
            ENABLED: {
                REGISTRATION: true,
                EXAM: true,
                TRAINING: true,
                SEARCH: true,
                WRONG_BOOK: true,
                NOTIFICATIONS: true,
                UPLOAD: true,
                EXPORT: true,
                OFFLINE_MODE: false
            },

            // 功能限制
            LIMITS: {
                MAX_WRONG_QUESTIONS: 1000,
                MAX_SEARCH_HISTORY: 50,
                MAX_NOTIFICATIONS: 100,
                DAILY_EXAM_ATTEMPTS: 5,
                HOURLY_API_REQUESTS: 1000
            }
        },

        // 调试配置
        DEBUG: {
            // 调试模式
            ENABLED: isDebugMode(),

            // 日志级别
            LOG_LEVEL: getLogLevel(),

            // 调试选项
            OPTIONS: {
                LOG_API_CALLS: true,
                LOG_STATE_CHANGES: false,
                LOG_PERFORMANCE: true,
                MOCK_API_RESPONSES: false
            },

            // 模拟数据
            MOCK_DATA: {
                ENABLED: false,
                DELAY: 500 // 模拟延迟（毫秒）
            }
        },

        // 环境配置
        ENVIRONMENT: getEnvironment()
    };

    // ==================== 工具函数 ====================

    /**
     * 获取API基础URL
     * @returns {string} API基础URL
     */
    function getApiBaseUrl() {
        // 优先级：window.API_BASE_URL > 环境变量 > 默认值
        if (typeof window !== 'undefined' && window.API_BASE_URL) {
            return window.API_BASE_URL;
        }

        // 根据当前域名判断环境
        const hostname = window.location.hostname;

        if (hostname === 'localhost' || hostname === '127.0.0.1') {
            return 'http://localhost:8080';
        } else if (hostname.includes('test.')) {
            return 'https://test-api.arknights-exam.com';
        } else if (hostname.includes('staging.')) {
            return 'https://staging-api.arknights-exam.com';
        } else {
            return 'https://api.arknights-exam.com';
        }
    }

    /**
     * 获取当前环境
     * @returns {string} 环境名称
     */
    function getEnvironment() {
        const hostname = window.location.hostname;

        if (hostname === 'localhost' || hostname === '127.0.0.1') {
            return 'development';
        } else if (hostname.includes('test.')) {
            return 'test';
        } else if (hostname.includes('staging.')) {
            return 'staging';
        } else {
            return 'production';
        }
    }

    /**
     * 判断是否为调试模式
     * @returns {boolean} 是否调试模式
     */
    function isDebugMode() {
        // URL参数优先
        const urlParams = new URLSearchParams(window.location.search);
        if (urlParams.has('debug')) {
            return urlParams.get('debug') === 'true';
        }

        // 其次检查本地存储
        try {
            const debugFlag = localStorage.getItem('debug_mode');
            return debugFlag === 'true';
        } catch (e) {
            console.warn('无法读取本地存储:', e);
        }

        // 最后根据环境判断
        const env = getEnvironment();
        return env === 'development' || env === 'test';
    }

    /**
     * 获取日志级别
     * @returns {string} 日志级别
     */
    function getLogLevel() {
        const env = getEnvironment();

        switch (env) {
            case 'development':
                return 'debug';
            case 'test':
                return 'info';
            case 'staging':
                return 'warn';
            case 'production':
                return 'error';
            default:
                return 'info';
        }
    }

    /**
     * 获取完整的API URL
     * @param {string} endpoint - API端点
     * @param {object} params - 路径参数
     * @returns {string} 完整的URL
     */
    function getApiUrl(endpoint, params = {}) {
        let url = `${CONFIG.API.FULL_BASE_URL}${endpoint}`;

        // 替换路径参数
        Object.keys(params).forEach(key => {
            url = url.replace(`{${key}}`, encodeURIComponent(params[key]));
        });

        return url;
    }

    /**
     * 获取存储键名（带前缀）
     * @param {string} key - 原始键名
     * @returns {string} 带前缀的键名
     */
    function getStorageKey(key) {
        return `${CONFIG.STORAGE.PREFIX}${key}`;
    }

    /**
     * 获取类型文本
     * @param {number} type - 类型编号
     * @returns {string} 类型文本
     */
    function getTypeText(type) {
        return CONFIG.QUESTIONS.TYPES[type] || '未知类型';
    }

    /**
     * 获取难度文本
     * @param {number} difficulty - 难度编号
     * @returns {string} 难度文本
     */
    function getDifficultyText(difficulty) {
        return CONFIG.QUESTIONS.DIFFICULTIES[difficulty] || '未知难度';
    }

    /**
     * 获取类型颜色
     * @param {number} type - 类型编号
     * @returns {string} 颜色值
     */
    function getTypeColor(type) {
        return CONFIG.QUESTIONS.COLORS.TYPE[type] || '#666';
    }

    /**
     * 获取难度颜色
     * @param {number} difficulty - 难度编号
     * @returns {string} 颜色值
     */
    function getDifficultyColor(difficulty) {
        return CONFIG.QUESTIONS.COLORS.DIFFICULTY[difficulty] || '#666';
    }

    /**
     * 获取选项字母
     * @param {number} index - 选项索引（0-based）
     * @returns {string} 选项字母
     */
    function getOptionLetter(index) {
        return CONFIG.QUESTIONS.OPTION_LETTERS[index] || String.fromCharCode(65 + index);
    }

    /**
     * 格式化题目文本（处理换行）
     * @param {string} text - 原始文本
     * @returns {string} 格式化后的HTML
     */
    function formatQuestionText(text) {
        if (!text) return '';
        return text.replace(/\r\n/g, '\n').replace(/\n/g, '<br>');
    }

    /**
     * 格式化日期时间
     * @param {Date|string|number} date - 日期
     * @param {string} format - 格式
     * @returns {string} 格式化后的字符串
     */
    function formatDateTime(date, format = 'YYYY-MM-DD HH:mm:ss') {
        if (!date) return '';

        const d = new Date(date);
        if (isNaN(d.getTime())) return '';

        const pad = (n) => n.toString().padStart(2, '0');

        const replacements = {
            'YYYY': d.getFullYear(),
            'MM': pad(d.getMonth() + 1),
            'DD': pad(d.getDate()),
            'HH': pad(d.getHours()),
            'mm': pad(d.getMinutes()),
            'ss': pad(d.getSeconds())
        };

        return format.replace(/YYYY|MM|DD|HH|mm|ss/g, match => replacements[match]);
    }

    /**
     * 验证用户名
     * @param {string} username - 用户名
     * @returns {object} 验证结果
     */
    function validateUsername(username) {
        const rules = CONFIG.USER.VALIDATION.USERNAME;
        const errors = [];

        if (!username) {
            errors.push('用户名不能为空');
        } else {
            if (username.length < rules.MIN_LENGTH) {
                errors.push(`用户名至少需要${rules.MIN_LENGTH}个字符`);
            }
            if (username.length > rules.MAX_LENGTH) {
                errors.push(`用户名不能超过${rules.MAX_LENGTH}个字符`);
            }
            if (!rules.PATTERN.test(username)) {
                errors.push('用户名只能包含字母、数字、中文和下划线');
            }
        }

        return {
            valid: errors.length === 0,
            errors
        };
    }

    /**
     * 验证密码
     * @param {string} password - 密码
     * @returns {object} 验证结果
     */
    function validatePassword(password) {
        const rules = CONFIG.USER.VALIDATION.PASSWORD;
        const errors = [];

        if (!password) {
            errors.push('密码不能为空');
        } else {
            if (password.length < rules.MIN_LENGTH) {
                errors.push(`密码至少需要${rules.MIN_LENGTH}个字符`);
            }
            if (password.length > rules.MAX_LENGTH) {
                errors.push(`密码不能超过${rules.MAX_LENGTH}个字符`);
            }
        }

        return {
            valid: errors.length === 0,
            errors
        };
    }

    // ==================== 全局导出 ====================

    // 导出配置对象
    window.AppConfig = CONFIG;

    // 导出工具函数
    window.ConfigHelper = {
        getApiUrl,
        getStorageKey,
        getTypeText,
        getDifficultyText,
        getTypeColor,
        getDifficultyColor,
        getOptionLetter,
        formatQuestionText,
        formatDateTime,
        validateUsername,
        validatePassword,

        // 环境信息
        isDevelopment: getEnvironment() === 'development',
        isTest: getEnvironment() === 'test',
        isStaging: getEnvironment() === 'staging',
        isProduction: getEnvironment() === 'production',

        // 功能检查
        isFeatureEnabled: function (feature) {
            return CONFIG.FEATURES.ENABLED[feature] || false;
        },

        // 调试工具
        log: function (message, level = 'info', data = null) {
            if (!CONFIG.DEBUG.ENABLED) return;

            const levels = ['debug', 'info', 'warn', 'error'];
            const currentLevel = levels.indexOf(CONFIG.DEBUG.LOG_LEVEL);
            const messageLevel = levels.indexOf(level);

            if (messageLevel >= currentLevel) {
                const timestamp = formatDateTime(new Date(), 'HH:mm:ss');
                const prefix = `[${timestamp}] [${level.toUpperCase()}]`;

                if (data) {
                    console[level](prefix, message, data);
                } else {
                    console[level](prefix, message);
                }
            }
        },

        // 性能监控
        startTimer: function (label) {
            if (CONFIG.DEBUG.ENABLED && CONFIG.DEBUG.OPTIONS.LOG_PERFORMANCE) {
                console.time(label);
            }
        },

        endTimer: function (label) {
            if (CONFIG.DEBUG.ENABLED && CONFIG.DEBUG.OPTIONS.LOG_PERFORMANCE) {
                console.timeEnd(label);
            }
        }
    };

    // 设置全局API基础URL（供其他模块使用）
    window.API_BASE_URL = CONFIG.API.BASE_URL;

    // 控制台输出配置信息
    if (CONFIG.DEBUG.ENABLED) {
        console.group('博士考核系统配置');
        console.log('系统名称:', CONFIG.SYSTEM.NAME);
        console.log('版本:', CONFIG.SYSTEM.VERSION);
        console.log('环境:', CONFIG.ENVIRONMENT);
        console.log('API地址:', CONFIG.API.BASE_URL);
        console.log('完整API地址:', CONFIG.API.FULL_BASE_URL);
        console.log('调试模式:', CONFIG.DEBUG.ENABLED);
        console.log('日志级别:', CONFIG.DEBUG.LOG_LEVEL);
        console.groupEnd();
    }

    // 添加配置就绪事件
    window.dispatchEvent(new CustomEvent('config:ready'));

    console.log('config.js 加载完成');

})();