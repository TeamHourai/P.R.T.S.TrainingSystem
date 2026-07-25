const assert = require('node:assert/strict');
const fs = require('node:fs');
const path = require('node:path');
const test = require('node:test');
const vm = require('node:vm');

function loadMethods(...files) {
    const sessionData = new Map();
    const localData = new Map();
    const context = {
        console: {
            log: console.log.bind(console),
            warn: console.warn.bind(console),
            error: console.error.bind(console)
        },
        setTimeout,
        clearTimeout,
        sessionStorage: {
            getItem: key => sessionData.has(key) ? sessionData.get(key) : null,
            setItem: (key, value) => sessionData.set(key, String(value)),
            removeItem: key => sessionData.delete(key)
        },
        localStorage: {
            getItem: key => localData.has(key) ? localData.get(key) : null,
            setItem: (key, value) => localData.set(key, String(value)),
            removeItem: key => localData.delete(key)
        },
        reloadCount: 0
    };
    context.location = { reload: () => { context.reloadCount += 1; } };
    context.window = context;
    files.forEach(file => {
        const source = fs.readFileSync(path.resolve(__dirname, '..', file), 'utf8');
        vm.runInNewContext(source, context, { filename: file });
    });
    return context;
}

test('logout closes notification UI and invalidates in-flight notification requests', async () => {
    const context = loadMethods('js/app/app-methods1.js', 'js/app/app-methods4.js');
    context.userApi = { logout: async () => ({}) };
    const state = {
        isLoggedIn: true,
        userInfo: { id: 1 },
        isAdmin: true,
        wrongQuestions: [1],
        wrongQuestionsDetail: [{}],
        trainingRecords: { 1: {} },
        showSystemNotice: true,
        showNoticeDetail: true,
        currentNoticeDetail: { id: 1 },
        notifications: [{ id: 1 }],
        unreadCount: 1,
        hasMoreNotifications: true,
        loadingNotifications: true,
        notifReqSeq: 3,
        resetNotificationUi() {
            return context.window._appMethods4.resetNotificationUi.call(this);
        }
    };

    await context.window._appMethods1.handleLogout.call(state);

    assert.equal(state.isLoggedIn, false);
    assert.equal(state.showSystemNotice, false);
    assert.equal(state.showNoticeDetail, false);
    assert.equal(state.notifications.length, 0);
    assert.equal(state.unreadCount, 0);
    assert.equal(state.notifReqSeq, 4);
    assert.equal(context.reloadCount, 0);
});

test('login loads private user data once without reloading the page', async () => {
    const context = loadMethods('js/app/app-methods1.js');
    context.userApi = {
        login: async () => ({
            success: true,
            user: { id: 1, username: 'admin', isAdmin: true },
            isAdmin: true
        })
    };
    let loadCount = 0;
    const state = {
        authUsername: 'admin',
        authPassword: 'secret',
        showAuthModal: true,
        async loadUserData() { loadCount += 1; },
        showError() {}
    };

    await context.window._appMethods1.handleLogin.call(state);

    assert.equal(state.isLoggedIn, true);
    assert.equal(state.isAdmin, true);
    assert.equal(loadCount, 1);
    assert.equal(context.reloadCount, 0);
});

test('stored token is validated with one profile request', async () => {
    const context = loadMethods('js/app/app-methods1.js');
    context.localStorage.setItem('token', 'jwt');
    let profileCount = 0;
    context.userApi = {
        getCurrentUser: async () => {
            profileCount += 1;
            return { id: 7, username: 'doctor', isAdmin: false };
        }
    };
    context.api = { clearAuth() {} };
    const state = { isLoggedIn: false, userInfo: {}, isAdmin: false };

    const loggedIn = await context.window._appMethods1.checkLoginStatus.call(state);

    assert.equal(loggedIn, true);
    assert.equal(profileCount, 1);
    assert.equal(state.userInfo.id, 7);
});

test('private session data loads once per source and isolates failures', async () => {
    const context = loadMethods('js/app/app-methods1.js');
    context.console.warn = () => {};
    const calls = { wrong: 0, exam: 0, settings: 0, training: 0 };
    const state = {
        isLoggedIn: true,
        async loadWrongQuestions() {
            calls.wrong += 1;
            throw new Error('temporary failure');
        },
        async loadExamStats() { calls.exam += 1; },
        async loadAnswerSettings() { calls.settings += 1; },
        async loadTrainingRecords() { calls.training += 1; }
    };

    await context.window._appMethods1.loadUserData.call(state);

    assert.deepEqual(calls, { wrong: 1, exam: 1, settings: 1, training: 1 });
});

test('only an explicit notice action opens the modal and starts one request', () => {
    const context = loadMethods('js/app/app-methods4.js');
    let requestCount = 0;
    const state = {
        isLoggedIn: true,
        noticePage: 8,
        notifications: [{ id: 9 }],
        loadingNotifications: false,
        showSystemNotice: false,
        loadNotifications() {
            requestCount += 1;
        }
    };

    context.window._appMethods4.openSystemNotice.call(state);

    assert.equal(state.showSystemNotice, true);
    assert.equal(state.noticePage, 1);
    assert.equal(state.notifications.length, 0);
    assert.equal(requestCount, 1);
});

test('navigation reset clears modal state and invalidates old responses', () => {
    const context = loadMethods('js/app/app-methods4.js');
    const state = {
        showSystemNotice: true,
        showNoticeDetail: true,
        currentNoticeDetail: { id: 1 },
        notifications: [{ id: 1 }],
        unreadCount: 1,
        hasMoreNotifications: true,
        loadingNotifications: true,
        notifReqSeq: 6
    };

    context.window._appMethods4.resetNotificationUi.call(state);

    assert.equal(state.showSystemNotice, false);
    assert.equal(state.showNoticeDetail, false);
    assert.equal(state.currentNoticeDetail, null);
    assert.equal(state.notifications.length, 0);
    assert.equal(state.unreadCount, 0);
    assert.equal(state.notifReqSeq, 7);
});

test('homepage lifecycle and watchers cannot open or reload notifications', () => {
    const mainSource = fs.readFileSync(
        path.resolve(__dirname, '..', 'js/app/app-main.js'),
        'utf8'
    );
    const watchSource = fs.readFileSync(
        path.resolve(__dirname, '..', 'js/app/app-watch.js'),
        'utf8'
    );
    const methodsSource = fs.readFileSync(
        path.resolve(__dirname, '..', 'js/app/app-methods4.js'),
        'utf8'
    );
    const authSource = fs.readFileSync(
        path.resolve(__dirname, '..', 'js/app/app-methods1.js'),
        'utf8'
    );
    const apiSource = fs.readFileSync(
        path.resolve(__dirname, '..', 'js/api.js'),
        'utf8'
    );

    assert.equal(mainSource.includes('this.checkLoginAnnouncements('), false);
    assert.equal(methodsSource.includes('checkLoginAnnouncements'), false);
    assert.equal(mainSource.includes('__refresh_after_'), false);
    assert.equal(mainSource.includes('window.location.reload'), false);
    assert.equal(authSource.includes('__refresh_after_'), false);
    assert.equal(authSource.includes('window.location.reload'), false);
    assert.equal(watchSource.includes('isLoggedIn'), false);
    assert.equal(apiSource.includes("PRTS.post('/auth/logout'"), false);
    assert.equal(mainSource.includes("window.addEventListener('pagehide'"), true);
    assert.equal(mainSource.includes("window.addEventListener('pageshow'"), true);
    assert.equal(watchSource.includes('this.loadNotifications('), false);
    assert.equal(
        (methodsSource.match(/this\.showSystemNotice\s*=\s*true/g) || []).length,
        1
    );
});

test('Vue is local and modal templates are cloaked before mounting', () => {
    const indexHtml = fs.readFileSync(path.resolve(__dirname, '..', 'index.html'), 'utf8');
    const examHtml = fs.readFileSync(path.resolve(__dirname, '..', 'exam.html'), 'utf8');

    for (const html of [indexHtml, examHtml]) {
        assert.equal(html.includes('cdn.jsdelivr.net/npm/vue'), false);
        assert.equal(html.includes('js/vendor/vue-2.6.14.min.js'), true);
        assert.equal(html.includes('[v-cloak]'), true);
        assert.match(html, /id="app"[^>]*v-cloak|v-cloak[^>]*id="app"/);
    }
});
