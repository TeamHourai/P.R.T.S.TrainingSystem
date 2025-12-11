(function () {
    'use strict';

    if (typeof http === 'undefined') {
        console.error('请先加载 request.js');
        return;
    }

    // API 统一前缀
    const API_PREFIX = ((window.API_BASE_URL && String(window.API_BASE_URL)) || 'http://localhost:8080').replace(/\/+$/, '') + '/api/v1';

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

    // Helper：使用 fetch 强制以 UTF-8 解码 JSON 响应，避免后端未声明 charset 导致乱码
    function fetchJsonUtf8(path, params = {}) {
        // 处理 query string（简单处理）
        const urlBase = (window.API_BASE_URL || 'http://localhost:8080').replace(/\/+$/, '')+ '/api/v1';
        const url = new URL(path, urlBase);
        Object.keys(params || {}).forEach(k => {
            if (params[k] !== undefined && params[k] !== null) url.searchParams.set(k, params[k]);
        });
        return fetch(url.toString(), { method: 'GET', mode: 'cors' })
            .then(resp => {
                if (!resp.ok) return resp.text().then(t => { throw new Error(t || resp.statusText); });
                return resp.arrayBuffer();
            })
            .then(buf => {
                const text = new TextDecoder('utf-8').decode(buf);
                try { return JSON.parse(text); } catch (e) { throw new Error('解析后端返回 JSON 失败: ' + e.message); }
            });
    }

    // -------- 新增：规范化后端题目对象，保证 options 为数组、answer 为数字等 --------
    function normalizeQuestion(raw) {
        if (!raw) return raw;
        const q = Object.assign({}, raw);

        // 确保 question/analysis/resource 为字符串
        q.question = (q.question === undefined || q.question === null) ? '' : String(q.question);
        q.analysis = (q.analysis === undefined || q.analysis === null) ? '' : String(q.analysis);
        q.resource = (q.resource === undefined || q.resource === null) ? '' : String(q.resource);

        // options 可能为字符串（"A|B|C|D"）或数组；统一为数组
        if (Array.isArray(q.options)) {
            q.options = q.options.map(o => o === null || o === undefined ? '' : String(o));
        } else if (typeof q.options === 'string') {
            if (q.options.indexOf('|') !== -1) {
                q.options = q.options.split('|').map(s => s.trim());
            } else {
                try {
                    const parsed = JSON.parse(q.options);
                    if (Array.isArray(parsed)) q.options = parsed.map(String);
                    else q.options = [String(q.options)];
                } catch (e) {
                    q.options = [String(q.options)];
                }
            }
        } else {
            q.options = (q.options == null) ? ['', '', '', ''] : [String(q.options)];
        }
        while (q.options.length < 4) q.options.push('');

        // answer 可能为字符串，统一为整数（1-based）
        if (q.answer === undefined || q.answer === null || q.answer === '') {
            q.answer = 0;
        } else {
            const parsed = parseInt(q.answer);
            q.answer = isNaN(parsed) ? 0 : parsed;
        }

        // type / difficulty 也尽量转为数字
        q.type = q.type ? Number(q.type) : 0;
        q.difficulty = q.difficulty ? Number(q.difficulty) : 0;

        // picture 字段兼容
        q.picture = !!q.picture;
        if (q.pictureUrl) q.pictureUrl = String(q.pictureUrl);

        // keywords 兼容
        if (typeof q.keywords === 'string') {
            try {
                q.keywords = JSON.parse(q.keywords);
            } catch {
                q.keywords = q.keywords.split(/[,，]/).map(s => s.trim()).filter(Boolean);
            }
        }
        if (!Array.isArray(q.keywords)) q.keywords = [];

        return q;
    }
    // -------- 规范化结束 --------

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
            // 优先使用强制 UTF-8 的 fetch 实现以避免乱码
            return fetchJsonUtf8('/api/v1/questions', { ...defaultParams, ...params })
                .then(data => {
                    if (!Array.isArray(data)) return data;
                    return data.map(q => normalizeQuestion(q));
                });
        },

        // 【题目管理模块-6】获取单题详情
        getQuestionById: function (id, includeAnalysis = true) {
            // 优先使用强制 UTF-8 的 fetch 实现
            return fetchJsonUtf8(`/api/v1/questions/${id}`, { includeAnalysis })
                .then(q => normalizeQuestion(q));
        },

        // 获取题目统计数据
        getQuestionStats: function (id) {
            return http.get(`${API_PREFIX}/stats/question/${id}`)
                .then(resp => resp && resp.data ? resp.data : resp);
        },

        // 【题目管理模块-7】创建题目（管理员操作）
        createQuestion: function (question) {
            return http.post(`${API_PREFIX}/questions`, question)
                .then(resp => resp && resp.data ? resp.data : resp);
        },

        // 【题目管理模块-8】更新题目信息（管理员操作）
        updateQuestion: function (id, question) {
            return http.put(`${API_PREFIX}/questions/${id}`, question)
                .then(resp => resp && resp.data ? resp.data : resp);
        },

        // 【题目管理模块-9】删除题目（管理员操作）
        deleteQuestion: function (id) {
            return http.delete(`${API_PREFIX}/questions/${id}`)
                .then(resp => resp && resp.data !== undefined ? resp.data : resp);
        },

        // 【题目管理模块-10】搜索题目
        searchQuestions: function (keyword, field = 'question') {
            return http.get(`${API_PREFIX}/questions/search`, { keyword, field }).then(resp => {
                // resp: { code, message, data: { results, total } }
                if (!resp || !resp.data || !Array.isArray(resp.data.results)) return [];
                return {
                    results: resp.data.results.map(q => normalizeQuestion(q)),
                    total: resp.data.total
                };
            });
        }
    };

    // 培训题目管理 API
    window.trainingQuestionApi = {
        // 【培训题目模块-11】获取培训题目列表
        getTrainingQuestions: function (params = {}) {
            // 先尝试 /training/questions（UTF-8 解码），失败降级到 /questions
            return fetchJsonUtf8('/api/v1/training/questions', {
                page: params.page || 1,
                size: params.size || 20
            }).then(data => {
                if (Array.isArray(data)) return data.map(q => normalizeQuestion(q));
                return data;
            }).catch(err => {
                console.warn('/api/v1/training/questions 请求失败或不可用，降级到 /questions', err);
                return fetchJsonUtf8('/api/v1/questions', {
                    page: params.page || 1,
                    size: params.size || 20
                }).then(data => Array.isArray(data) ? data.map(q => normalizeQuestion(q)) : data);
            });
        },

        // 【培训题目模块-12】获取指定培训题目详情
        getTrainingQuestionById: function (id) {
            return fetchJsonUtf8(`/api/v1/training/questions/${id}`).then(q => normalizeQuestion(q)).catch(() => fetchJsonUtf8(`/questions/${id}`).then(q => normalizeQuestion(q)));
        },

        // 【培训题目模块-13.1】创建培训题目（管理员操作）
        createTrainingQuestion: function (question) {
            return http.post('/api/v1/training/questions', question);
        },

        // 【培训题目模块-13.2】更新培训题目（管理员操作)
        updateTrainingQuestion: function (id, question) {
            return http.put(`/api/v1/training/questions/${id}`, question);
        },

        // 【培训题目模块-13.3】删除培训题目（管理员操作)
        deleteTrainingQuestion: function (id) {
            return http.delete(`/api/v1/training/questions/${id}`);
        }
    };

    // 题目数据辅助函数
    window.questionHelper = {
        // 格式化题目显示：转换换行符和添加类型/难度文本
        formatQuestionForDisplay: function (question) {
            if (!question) return null;
            if (question && (question.options === undefined || typeof question.answer === 'string')) {
                question = normalizeQuestion(question);
            }
            const fmtText = (str) => (str || '').replace(/\n/g, '<br>').replace(/\r\n/g, '<br>');
            return {
                ...question,
                typeText: typeNames[question.type] || '未知类型',
                difficultyText: difficultyNames[question.difficulty] || '未知难度',
                question: fmtText(question.question),
                options: (question.options || ['', '', '', '']).map(opt => fmtText(opt)),
                analysis: fmtText(question.analysis),
                keywords: question.keywords || [],
                resource: question.resource || '',
                picture: question.picture || false,
                pictureUrl: question.pictureUrl || ''
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