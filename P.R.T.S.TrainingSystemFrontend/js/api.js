/**
 * PRTS 统一接口客户端 (window.api)
 * ------------------------------------------------------------------
 * 所有页面 / 模块的统一 API 入口。底层请求与标准化响应解包由
 * js/utils/common.js (window.PRTS) 提供，本文件只负责：
 *   1. 按业务模块组织接口方法
 *   2. 处理登录态（写入/清除令牌与用户信息）
 *   3. 兼容旧代码使用的全局别名（userApi / questionApi / ...）
 *
 * 依赖加载顺序：js/utils/common.js -> js/api.js
 */
(function () {
    'use strict';

    var PRTS = window.PRTS;

    // ===================== 统一 API =====================
    var api = {
        // ---------- 认证 ----------
        auth: {
            register: function (username, password, email) {
                return PRTS.post('/auth/register', { username: username, password: password, email: email })
                    .catch(function (err) { return { success: false, message: (err && err.message) || '注册失败' }; });
            },
            login: function (username, password, remember) {
                return PRTS.post('/auth/login', { username: username, password: password })
                    .then(function (res) {
                        if (res && res.token) {
                            PRTS.setToken(res.token, remember);
                            if (res.user) {
                                PRTS.setUserInfo(res.user, remember);
                                res.isAdmin = !!res.user.isAdmin;
                                res.username = res.user.username || null;
                            }
                        }
                        return res;
                    })
                    .catch(function (err) { return { success: false, message: (err && err.message) || '登录失败' }; });
            },
            logout: function () {
                // JWT 为无状态令牌，当前后端没有 Token 黑名单。
                // 退出只需清除客户端凭证；清除后再请求受保护的 logout 只会制造 403。
                PRTS.clearAuth();
                return Promise.resolve({ success: true });
            },
            getCurrentUser: function () { return PRTS.get('/auth/profile'); }
        },

        // ---------- 正式题库 ----------
        questions: {
            list: function (params) { return PRTS.get('/questions', params); },
            get: function (id) { return PRTS.get('/questions/' + id); },
            create: function (q) { return PRTS.post('/questions', q); },
            update: function (id, q) { return PRTS.put('/questions/' + id, q); },
            delete: function (id) { return PRTS.del('/questions/' + id); },
            batchDelete: function (ids) { return PRTS.post('/admin/questions/batch-delete', { ids: ids }); }
        },

        // ---------- 培训题库 ----------
        training: {
            list: function (params) {
                var p = Object.assign({ mode: 'onboarding' }, params || {});
                return PRTS.get('/questions', p);
            },
            get: function (id) { return PRTS.get('/questions/' + id, { mode: 'onboarding' }); },
            create: function (q) { return PRTS.post('/training/questions', q); },
            update: function (id, q) { return PRTS.put('/training/questions/' + id, q); },
            delete: function (id) { return PRTS.del('/training/questions/' + id); }
        },

        // ---------- 考试 ----------
        exam: {
            paper: function () { return PRTS.get('/exam/paper'); },
            submit: function (paperId, answers, duration) {
                var body = new URLSearchParams();
                if (typeof answers === 'object' && !Array.isArray(answers)) {
                    answers = Object.keys(answers).map(function (k) { return k + ':' + answers[k]; }).join(',');
                }
                body.append('paperId', paperId);
                body.append('answers', answers);
                if (duration) body.append('duration', duration);
                return PRTS.post('/exam/submit', null, { formBody: body.toString() });
            },
            history: function (params) { return PRTS.get('/exam/history', params); }
        },

        // ---------- 答题 & 错题 ----------
        answers: {
            wrong: function (params) { return PRTS.get('/answers/wrong', params); },
            hideWrong: function (qid) { return PRTS.del('/answers/wrong/' + qid); },
            history: function (params) { return PRTS.get('/exam/history', params); }
        },

        // ---------- 管理 ----------
        admin: {
            users: function (q) { return PRTS.get('/admin/users', q ? { q: q } : {}); },
            setPermission: function (targetId, makeAdmin) {
                var body = new URLSearchParams();
                body.append('targetId', targetId);
                body.append('makeAdmin', makeAdmin ? 'true' : 'false');
                return PRTS.post('/admin/user/permission', null, { formBody: body.toString() });
            },
            auditLogs: function (page, size) {
                return PRTS.get('/admin/audit-logs', { page: page || 1, size: size || 20 });
            }
        },

        // ---------- 公告 & 通知 ----------
        announcements: {
            list: function () { return PRTS.get('/announcements'); },
            create: function (data) { return PRTS.post('/admin/announcements', data); }
        },
        notifications: {
            list: function (params) { return PRTS.get('/notifications', params); },
            markRead: function (id) { return PRTS.put('/notifications/' + id + '/read'); },
            markAllRead: function () { return PRTS.put('/notifications/read-all'); },
            hide: function (id) { return PRTS.del('/notifications/' + id); },
            hideAll: function () { return PRTS.del('/notifications'); }
        },

        // ---------- 用户设置 ----------
        user: {
            answerSettings: function () { return PRTS.get('/user/answer-settings'); },
            updateAnswerSettings: function (s) { return PRTS.put('/user/answer-settings', s); },
            trainingRecords: function () { return PRTS.get('/user/training-records'); },
            saveTrainingRecord: function (r) { return PRTS.put('/user/training-records', r); },
            clearTrainingRecords: function () { return PRTS.del('/user/training-records'); },
        wrongQuestions: function () { return PRTS.get('/answers/wrong'); }
        },

        // ---------- 系统 ----------
        ping: function () { return PRTS.get('/ping'); },
        keywords: function (mode) { return PRTS.get('/keywords', mode ? { mode: mode } : {}); },
        stats: {
            question: function (id) { return PRTS.get('/stats/question/' + id); },
            user: function () { return PRTS.get('/stats/user'); },
            system: function () { return PRTS.get('/stats/system'); }
        },

        // 令牌辅助（向后兼容）
        getToken: function () { return PRTS.getToken(); },
        setToken: function (t, r) { return PRTS.setToken(t, r); },
        clearAuth: function () { return PRTS.clearAuth(); }
    };

    window.api = api;

    // ===================== 向后兼容别名 =====================
    var a = api;
    window.userApi = {
        login: function (u, p, r) { return a.auth.login(u, p, r); },
        register: function (u, p, e) { return a.auth.register(u, p, e); },
        logout: a.auth.logout,
        getCurrentUser: a.auth.getCurrentUser
    };
    window.questionApi = {
        getQuestions: function (p) { return a.questions.list(p); },
        getQuestionDetail: function (id) { return a.questions.get(id); },
        getQuestionById: function (id) { return a.questions.get(id); },
        createQuestion: function (q) { return a.questions.create(q); },
        updateQuestion: function (id, q) { return a.questions.update(id, q); },
        deleteQuestion: function (id) { return a.questions.delete(id); },
        getKeywords: function (m) { return a.keywords(m); }
    };
    window.trainingRecordsApi = {
        get: a.user.trainingRecords,
        upsert: a.user.saveTrainingRecord,
        clear: a.user.clearTrainingRecords
    };
    window.trainingQuestionApi = {
        getTrainingQuestions: a.training.list,
        getTrainingQuestionById: function (id) { return a.training.get(id); }
    };
    window.answerApi = {
        submitAnswer: function (questionId, questionType, selectedOption) {
            var answers = questionId + ':' + selectedOption;
            var body = new URLSearchParams();
            body.append('answers', answers);
            return PRTS.post('/exam/submit', null, { formBody: body.toString() });
        },
        getWrongQuestions: a.answers.wrong,
        removeWrongQuestion: a.answers.hideWrong,
        getAnswerHistory: a.answers.history
    };
    window.examApi = {
        generatePaper: a.exam.paper,
        generateExamPaper: a.exam.paper,
        submitExam: a.exam.submit,
        submitExamAnswers: a.exam.submit,
        getExamHistory: a.exam.history
    };
    window.statsApi = {
        getQuestionStats: a.stats.question,
        getUserStats: a.stats.user
    };
    window.answerSettingsApi = {
        get: a.user.answerSettings,
        update: a.user.updateAnswerSettings
    };
    window.announcementApi = {
        createAnnouncement: function (payload) { return a.announcements.create(payload); }
    };
    window.notificationApi = {
        getNotifications: a.notifications.list,
        markAsRead: a.notifications.markRead,
        markAllAsRead: a.notifications.markAllRead,
        deleteNotification: a.notifications.hide,
        clearAll: a.notifications.hideAll
    };
    window.adminApi = {
        getUsers: a.admin.users,
        setPermission: a.admin.setPermission,
        getAuditLogs: a.admin.auditLogs
    };
})(window);
