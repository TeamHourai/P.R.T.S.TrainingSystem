(function () {
    'use strict';

    if (typeof http === 'undefined') {
        console.error('请先加载 request.js');
        return;
    }

    // 题目类型和难度映射
    const typeNames = {
        1: '干员调配与特性化决策',
        2: '空间部署与极致化战术',
        3: '效能审计与生态位界定',
        4: '横向分析与竞争力评估',
        5: '作战环境与档案类记录'
    };

    const difficultyNames = {
        1: '常识', 2: '基操', 3: '娴熟', 4: '明智', 5: '深邃'
    };

    // 题目管理 API
    window.questionApi = {
        // 【题目管理模块-5】获取所有题目
        getQuestions: function (params = {}) {
            const defaultParams = {
                page: 1,
                size: 50,
                type: undefined,
                difficulty: undefined,
                keyword: ''
            };
            return http.get('/questions', { ...defaultParams, ...params }, {
                showLoading: false
            });
        },

        // 【题目管理模块-6】获取单题详情
        getQuestionById: function (id, includeAnalysis = true) {
            return http.get(`/questions/${id}`, { includeAnalysis });
        },

        // 获取题目统计数据
        getQuestionStats: function (id) {
            return http.get(`/stats/question/${id}`);
        },

        // 【题目管理模块-7】创建题目（管理员操作）
        createQuestion: function (question) {
            return http.post('/questions', question);
        },

        // 【题目管理模块-8】更新题目信息（管理员操作）
        updateQuestion: function (id, question) {
            return http.put(`/questions/${id}`, question);
        },

        // 【题目管理模块-9】删除题目（管理员操作）
        deleteQuestion: function (id) {
            return http.delete(`/questions/${id}`);
        },

        // 【题目管理模块-10】搜索题目
        searchQuestions: function (keyword, field = 'question') {
            return http.get('/questions/search', { keyword, field });
        }
    };

    // 培训题目管理 API
    window.trainingQuestionApi = {
        // 【培训题目模块-1】获取培训题目列表
        getTrainingQuestions: function (params = {}) {
            // 优先请求 /training/questions（若后端提供培训题接口）
            return http.get('/training/questions', {
                page: params.page || 1,
                size: params.size || 20
            }).catch(err => {
                // 若 /training/questions 不可用（404 或网络错误），降级到 /questions 获取题库数据
                console.warn('/training/questions 请求失败，尝试降级到 /questions', err);
                return http.get('/questions', {
                    page: params.page || 1,
                    size: params.size || 20
                });
            });
        },

        // 【培训题目模块-2】获取指定培训题目详情
        getTrainingQuestionById: function (id) {
            return http.get(`/training/questions/${id}`);
        },

        // 【培训题目模块-3.1】创建培训题目（管理员操作）
        createTrainingQuestion: function (question) {
            return http.post('/training/questions', question);
        },

        // 【培训题目模块-3.2】更新培训题目（管理员操作）
        updateTrainingQuestion: function (id, question) {
            return http.put(`/training/questions/${id}`, question);
        },

        // 【培训题目模块-3.3】删除培训题目（管理员操作）
        deleteTrainingQuestion: function (id) {
            return http.delete(`/training/questions/${id}`);
        }
    };

    // 题目数据辅助函数
    window.questionHelper = {
        // 格式化题目显示：转换换行符和添加类型/难度文本
        formatQuestionForDisplay: function (question) {
            if (!question) return null;

            const fmtText = (str) => (str || '').replace(/\n/g, '<br>').replace(/\r\n/g, '<br>');

            return {
                ...question,
                typeText: typeNames[question.type] || '未知类型',
                difficultyText: difficultyNames[question.difficulty] || '未知难度',
                question: fmtText(question.question),
                options: (question.options || ['', '', '', '']).map(opt => fmtText(opt)),
                analysis: fmtText(question.analysis),
                keywords: question.keywords || [],
                resource: question.resource || ''
            };
        },

        // 格式化题目提交：HTML转义和字段处理
        formatQuestionForSubmit: function (question) {
            const fmtText = (str) => (str || '').replace(/<br>/g, '\n').replace(/<br\s*\/?>/g, '\n');

            const result = {
                ...question,
                question: fmtText(question.question),
                options: (question.options || []).map(opt => fmtText(opt)),
                analysis: fmtText(question.analysis)
            };

            // 确保关键词是数组格式
            if (question.keywordsInput) {
                result.keywords = question.keywordsInput.split(/[,，]/)
                    .map(k => k.trim())
                    .filter(k => k.length > 0);
            }

            return result;
        },

        // 题目验证：检查必填字段和格式
        validateQuestion: function (question) {
            const errors = [];

            if (!question.question?.trim()) {
                errors.push('题目内容不能为空');
            }

            if (!question.options || question.options.length < 4) {
                errors.push('需要提供至少4个选项');
            } else {
                question.options.forEach((opt, index) => {
                    if (!opt?.trim()) {
                        errors.push(`选项${String.fromCharCode(65 + index)}不能为空`);
                    }
                });
            }

            if (question.answer < 1 || question.answer > 4) {
                errors.push('正确答案必须在1-4之间');
            }

            if (!question.type || question.type < 1 || question.type > 5) {
                errors.push('题目类型必须在1-5之间');
            }

            if (!question.difficulty || question.difficulty < 1 || question.difficulty > 5) {
                errors.push('题目难度必须在1-5之间');
            }

            return {
                valid: errors.length === 0,
                errors: errors
            };
        }
    };

})();