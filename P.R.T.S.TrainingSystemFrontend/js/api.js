/**
 * PRTS Training System — Unified API Client v2.0
 * Replaces: request.js + apiapp.js + all js/api/*.js + user.js
 */
(function () {
    'use strict';

    const BASE = (window.API_BASE_URL || 'http://localhost:8080').replace(/\/+$/, '');
    const API = BASE + '/api/v1';

    // === Token management ===
    function getToken() {
        return localStorage.getItem('token') || sessionStorage.getItem('token') || null;
    }
    function setToken(t, remember) {
        if (remember) localStorage.setItem('token', t);
        else sessionStorage.setItem('token', t);
    }
    function clearAuth() {
        localStorage.removeItem('token'); sessionStorage.removeItem('token');
        localStorage.removeItem('userInfo'); sessionStorage.removeItem('userInfo');
    }

    // === HTTP helpers ===
    async function request(method, path, data, opts) {
        opts = opts || {};
        const url = API + path;
        const headers = { 'Content-Type': 'application/json' };
        const token = getToken();
        if (token) headers['Authorization'] = 'Bearer ' + token;

        const config = { method, mode: 'cors', headers };
        if (data && method !== 'GET') config.body = JSON.stringify(data);
        if (method === 'GET' && data) {
            const qs = new URLSearchParams(data).toString();
            path = path + (qs ? '?' + qs : '');
            config.body = undefined;
        }

        // For form-urlencoded (exam submit)
        if (opts.formBody) {
            config.body = opts.formBody;
            config.headers['Content-Type'] = 'application/x-www-form-urlencoded';
        }

        const resp = await fetch(API + path, config);
        const text = await resp.text();
        if (!resp.ok) {
            const err = new Error(text || resp.statusText);
            err.status = resp.status;
            throw err;
        }
        try { return JSON.parse(text); } catch (e) { return text; }
    }

    function get(path, params) { return request('GET', path, params); }
    function post(path, data, opts) { return request('POST', path, data, opts); }
    function put(path, data, opts) { return request('PUT', path, data, opts); }
    function del(path, data) { return request('DELETE', path, data); }

    // === Unified API ===
    window.api = {
        // Auth
        auth: {
            register: (username, password, email) =>
                post('/auth/register', { username, password, email }),
            login: async (username, password, remember) => {
                const res = await post('/auth/login', { username, password });
                if (res.token) {
                    setToken(res.token, remember);
                    if (res.user) {
                        const info = JSON.stringify(res.user);
                        if (remember) localStorage.setItem('userInfo', info);
                        else sessionStorage.setItem('userInfo', info);
                    }
                }
                return res;
            },
            logout: () => { clearAuth(); return post('/auth/logout', {}).catch(() => ({})); },
            profile: () => get('/auth/profile'),
            checkLogin: () => get('/auth/profile').then(u => !!u).catch(() => false),
            getCurrentUser: () => get('/auth/profile'),
        },

        // Questions
        questions: {
            list: (params) => get('/questions', params),
            get: (id) => get('/questions/' + id),
            create: (q) => post('/questions', q),
            update: (id, q) => put('/questions/' + id, q),
            delete: (id) => del('/questions/' + id),
            batchDelete: (ids) => post('/admin/questions/batch-delete', { ids }),
        },

        // Training (onboarding) questions
        training: {
            list: (params) => {
                // Support mode=onboarding for backward compat
                const p = Object.assign({ mode: 'onboarding' }, params || {});
                return get('/questions', p);
            },
            get: (id) => get('/questions/' + id, { mode: 'onboarding' }),
            create: (q) => post('/training/questions', q),
            update: (id, q) => put('/training/questions/' + id, q),
            delete: (id) => del('/training/questions/' + id),
        },

        // Exam
        exam: {
            paper: () => get('/exam/paper'),
            submit: (userId, answers, duration) => {
                const body = new URLSearchParams();
                body.append('userId', userId);
                if (typeof answers === 'object' && !Array.isArray(answers))
                    answers = Object.entries(answers).map(([k, v]) => k + ':' + v).join(',');
                body.append('answers', answers);
                if (duration) body.append('duration', duration);
                return post('/exam/submit', null, { formBody: body.toString() });
            },
            history: (params) => get('/exam/history', params),
        },

        // Answers & wrong questions
        answers: {
            wrong: (params) => get('/answers/wrong', params),
            hideWrong: (qid) => del('/answers/wrong/' + qid),
            history: (params) => get('/exam/history', params),
        },

        // Admin
        admin: {
            users: (q) => get('/admin/users', q ? { q } : {}),
            setPermission: (actorId, targetId, makeAdmin) => {
                const body = new URLSearchParams();
                body.append('actor_id', actorId);
                body.append('target_id', targetId);
                body.append('make_admin', makeAdmin ? 'true' : 'false');
                return post('/admin/user/permission', null, { formBody: body.toString() });
            },
        },

        // Announcements & notifications
        announcements: {
            list: () => get('/announcements'),
            create: (data) => post('/admin/announcements', data),
        },
        notifications: {
            list: (params) => get('/notifications', params),
            unreadCount: () => get('/notifications/unread-count'),
            markRead: (id) => put('/notifications/' + id + '/read'),
            markAllRead: () => put('/notifications/read-all'),
            hide: (id) => del('/notifications/' + id),
            hideAll: () => del('/notifications'),
        },

        // User settings
        user: {
            answerSettings: () => get('/user/answer-settings'),
            updateAnswerSettings: (s) => put('/user/answer-settings', s),
            trainingRecords: () => get('/user/training-records'),
            saveTrainingRecord: (r) => put('/user/training-records', r),
            clearTrainingRecords: () => del('/user/training-records'),
            wrongQuestions: (userId) => get('/user/' + userId + '/wrong'),
        },

        // System
        ping: () => get('/ping'),
        keywords: (mode) => get('/keywords', mode ? { mode } : {}),
        stats: {
            question: (id) => get('/stats/question/' + id),
            user: () => get('/stats/user'),
            system: () => get('/stats/system'),
        },

        // Token helpers
        getToken, setToken, clearAuth,
    };
    // === Backward-compatible aliases for existing app code ===
    // These map old scattered API names to the new unified api
    const a = window.api;
    window.userApi = {
        checkLoginStatus: a.auth.checkLogin,
        login: (u, p, r) => a.auth.login(u, p, r),
        register: (u, p, e) => a.auth.register(u, p, e),
        logout: a.auth.logout,
        getCurrentUser: a.auth.getCurrentUser,
    };
    window.questionApi = {
        getQuestions: (p) => a.questions.list(p),
        getQuestionDetail: (id) => a.questions.get(id),
        getQuestionById: (id) => a.questions.get(id),
        createQuestion: (q) => a.questions.create(q),
        updateQuestion: (id, q) => a.questions.update(id, q),
        deleteQuestion: (id) => a.questions.delete(id),
        getKeywords: (m) => a.keywords(m),
    };
    window.trainingRecordsApi = {
        get: a.user.trainingRecords,
        upsert: a.user.saveTrainingRecord,
        clear: a.user.clearTrainingRecords,
    };
    window.trainingQuestionApi = {
        getTrainingQuestions: a.training.list,
    };
    window.answerApi = {
        // submitAnswer(questionId, questionType, selectedOption) — individual answer
        submitAnswer: async (questionId, questionType, selectedOption) => {
            const ui = JSON.parse(localStorage.getItem('userInfo') || sessionStorage.getItem('userInfo') || '{}');
            const userId = ui.id || 0;
            const answers = questionId + ':' + selectedOption;
            const body = new URLSearchParams();
            body.append('userId', userId);
            body.append('answers', answers);
            const token = a.getToken();
            const headers = { 'Content-Type': 'application/x-www-form-urlencoded' };
            if (token) headers['Authorization'] = 'Bearer ' + token;
            const resp = await fetch(BASE + '/api/v1/exam/submit', { method: 'POST', headers, body: body.toString() });
            const text = await resp.text();
            try { return JSON.parse(text); } catch (e) { return text; }
        },
        getWrongQuestions: a.answers.wrong,
        removeWrongQuestion: a.answers.hideWrong,
        getAnswerHistory: a.answers.history,
    };
    window.examApi = {
        generatePaper: a.exam.paper,
        generateExamPaper: a.exam.paper,
        submitExam: a.exam.submit,
        submitExamAnswers: a.exam.submit,
        getExamHistory: a.exam.history,
    };
    window.statsApi = {
        getQuestionStats: a.stats.question,
        getUserStats: a.stats.user,
    };
    window.answerSettingsApi = {
        get: a.user.answerSettings,
        update: a.user.updateAnswerSettings,
    };
    window.announcementApi = {
        createAnnouncement: async (payload) => {
            const token = a.getToken();
            const headers = { 'Content-Type': 'application/json' };
            if (token) headers['Authorization'] = 'Bearer ' + token;
            const resp = await fetch(BASE + '/api/v1/admin/announcements', { method: 'POST', headers, body: JSON.stringify(payload) });
            const text = await resp.text();
            try { return JSON.parse(text); } catch (e) { return text; }
        },
    };
    window.notificationApi = {
        getNotifications: a.notifications.list,
        getUnreadCount: a.notifications.unreadCount,
        markAsRead: a.notifications.markRead,
        markAllAsRead: a.notifications.markAllRead,
        deleteNotification: a.notifications.hide,
        clearAll: a.notifications.hideAll,
    };
    window.adminApi = {
        getUsers: a.admin.users,
        setPermission: a.admin.setPermission,
    };
})();
