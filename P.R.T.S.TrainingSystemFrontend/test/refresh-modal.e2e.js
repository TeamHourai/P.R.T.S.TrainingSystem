const assert = require('node:assert/strict');
const { spawn } = require('node:child_process');
const fs = require('node:fs');
const path = require('node:path');

const EDGE_PATH = process.env.EDGE_PATH ||
    'C:\\Program Files (x86)\\Microsoft\\Edge\\Application\\msedge.exe';
const PAGE_URL = process.env.E2E_PAGE_URL || 'http://127.0.0.1:8888/index.html';
const DEBUG_PORT = Number(process.env.E2E_DEBUG_PORT || 9333);
const PROFILE_PATH = path.resolve(__dirname, '.edge-e2e-profile');

function delay(ms) {
    return new Promise(resolve => setTimeout(resolve, ms));
}

async function waitForJson(url, attempts = 60) {
    for (let i = 0; i < attempts; i += 1) {
        try {
            const response = await fetch(url);
            if (response.ok) return response.json();
        } catch {
            // Edge is still starting.
        }
        await delay(100);
    }
    throw new Error(`Edge debugging endpoint did not start: ${url}`);
}

class CdpClient {
    constructor(url) {
        this.socket = new WebSocket(url);
        this.nextId = 1;
        this.pending = new Map();
        this.ready = new Promise((resolve, reject) => {
            this.socket.addEventListener('open', resolve, { once: true });
            this.socket.addEventListener('error', reject, { once: true });
        });
        this.socket.addEventListener('message', event => {
            const message = JSON.parse(event.data);
            if (!message.id || !this.pending.has(message.id)) return;
            const { resolve, reject } = this.pending.get(message.id);
            this.pending.delete(message.id);
            if (message.error) reject(new Error(message.error.message));
            else resolve(message.result);
        });
    }

    async send(method, params = {}) {
        await this.ready;
        const id = this.nextId++;
        return new Promise((resolve, reject) => {
            this.pending.set(id, { resolve, reject });
            this.socket.send(JSON.stringify({ id, method, params }));
        });
    }

    close() {
        this.socket.close();
    }
}

async function readPageState(client) {
    const expression = `(() => {
        const app = document.getElementById('app');
        const notice = document.querySelector('.notification-modal');
        const generic = document.querySelector('.ui-modal');
        return {
            readyState: document.readyState,
            vueLoaded: typeof window.Vue === 'function',
            vueMounted: !!(app && app.__vue__),
            appStillCloaked: !!(app && app.hasAttribute('v-cloak')),
            noticeModalExists: !!notice,
            genericModalExists: !!generic,
            noticeDisplay: notice ? getComputedStyle(notice).display : null,
            genericText: generic ? generic.textContent.trim() : null
        };
    })()`;
    const result = await client.send('Runtime.evaluate', {
        expression,
        returnByValue: true,
        awaitPromise: true
    });
    return result.result.value;
}

async function main() {
    if (!fs.existsSync(EDGE_PATH)) {
        throw new Error(`Edge not found: ${EDGE_PATH}`);
    }
    fs.rmSync(PROFILE_PATH, { recursive: true, force: true });

    const edge = spawn(EDGE_PATH, [
        '--headless=new',
        '--disable-gpu',
        '--no-first-run',
        '--no-default-browser-check',
        `--remote-debugging-port=${DEBUG_PORT}`,
        `--user-data-dir=${PROFILE_PATH}`,
        PAGE_URL
    ], { stdio: 'ignore' });

    let client;
    try {
        const targets = await waitForJson(`http://127.0.0.1:${DEBUG_PORT}/json/list`);
        const page = targets.find(target => target.type === 'page');
        assert.ok(page && page.webSocketDebuggerUrl, 'No debuggable page target');
        client = new CdpClient(page.webSocketDebuggerUrl);
        await client.send('Runtime.enable');
        await client.send('Page.enable');
        await delay(2500);

        const initial = await readPageState(client);
        assert.equal(initial.vueLoaded, true, 'local Vue did not load');
        assert.equal(initial.vueMounted, true, 'Vue app did not mount');
        assert.equal(initial.appStillCloaked, false, 'v-cloak was not removed');
        assert.equal(initial.noticeModalExists, false, 'notification modal opened on initial load');
        assert.equal(initial.genericModalExists, false, `generic modal opened: ${initial.genericText}`);

        await client.send('Page.reload', { ignoreCache: true });
        await delay(2500);
        const refreshed = await readPageState(client);
        assert.equal(refreshed.vueLoaded, true, 'local Vue did not load after refresh');
        assert.equal(refreshed.vueMounted, true, 'Vue app did not mount after refresh');
        assert.equal(refreshed.appStillCloaked, false, 'v-cloak remained after refresh');
        assert.equal(refreshed.noticeModalExists, false, 'notification modal opened after refresh');
        assert.equal(refreshed.genericModalExists, false, `generic modal opened after refresh: ${refreshed.genericText}`);

        // 过期或历史 Token 是刷新页面的常见状态，必须被静默清理，
        // 不能产生通知中心或通用错误弹窗。
        await client.send('Runtime.evaluate', {
            expression: `localStorage.setItem('token', 'expired-test-token')`
        });
        await client.send('Page.reload', { ignoreCache: true });
        await delay(2500);
        const expiredSessionRefresh = await readPageState(client);
        assert.equal(expiredSessionRefresh.vueMounted, true, 'Vue app did not mount with an expired session');
        assert.equal(expiredSessionRefresh.noticeModalExists, false, 'notification modal opened for an expired session');
        assert.equal(
            expiredSessionRefresh.genericModalExists,
            false,
            `generic modal opened for an expired session: ${expiredSessionRefresh.genericText}`
        );

        process.stdout.write(`${JSON.stringify({ initial, refreshed, expiredSessionRefresh }, null, 2)}\n`);
    } finally {
        if (client) client.close();
        edge.kill();
        await delay(500);
        fs.rmSync(PROFILE_PATH, { recursive: true, force: true, maxRetries: 5, retryDelay: 200 });
    }
}

main().catch(error => {
    process.stderr.write(`${error.stack || error}\n`);
    process.exitCode = 1;
});
