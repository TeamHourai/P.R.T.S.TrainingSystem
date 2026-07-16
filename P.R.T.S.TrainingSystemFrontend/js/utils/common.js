/**
 * PRTS 公共工具模块 (window.PRTS)
 * ------------------------------------------------------------------
 * 统一前端工程化基础能力，供 api.js / config.js / 各页面复用：
 *   - request(method, path, data, opts)  统一请求 + 标准化响应解包
 *   - get/post/put/del                    语义化简写
 *   - 令牌与用户信息存储（单一可信来源）
 *   - 格式化 / 消息提示 工具
 *
 * 统一响应约定（与后端 Result<T> 对齐）：
 *   成功：{ code:200, message, data, success:true }
 *   失败：{ code:4xx/5xx, message, data:null, success:false }
 *
 * request() 会把响应“解包”成调用方最容易消费的形态：
 *   - data 为对象  -> 字段提升到顶层，并附带 code/message/success
 *   - data 为数组/基础类型 -> 原样返回
 *   - code !== 200 -> 抛出异常（携带 message），由调用方 .catch 处理
 */
(function (global) {
    'use strict';

    var PRTS = global.PRTS = global.PRTS || {};

    /* ============ 存储键（单一可信来源，与 api 使用保持一致） ============ */
    var STORAGE = {
        TOKEN: 'token',
        USER_INFO: 'userInfo'
    };

    function getBase() {
        var base = global.API_BASE_URL ||
            (global.AppConfig && global.AppConfig.API && global.AppConfig.API.BASE_URL) ||
            'http://localhost:8080';
        return String(base).replace(/\/+$/, '');
    }

    // 所有接口统一挂载在 /api/v1 之下
    function buildUrl(path) {
        if (/^https?:\/\//i.test(path)) return path;
        var p = path.charAt(0) === '/' ? path : '/' + path;
        return getBase() + '/api/v1' + p;
    }

    /* ============ 令牌 & 用户信息 ============ */
    function getToken() {
        return localStorage.getItem(STORAGE.TOKEN) || sessionStorage.getItem(STORAGE.TOKEN) || null;
    }
    function setToken(t, remember) {
        if (remember) localStorage.setItem(STORAGE.TOKEN, t);
        else sessionStorage.setItem(STORAGE.TOKEN, t);
    }
    function clearAuth() {
        localStorage.removeItem(STORAGE.TOKEN); sessionStorage.removeItem(STORAGE.TOKEN);
        localStorage.removeItem(STORAGE.USER_INFO); sessionStorage.removeItem(STORAGE.USER_INFO);
    }
    function getUserInfo() {
        try {
            return JSON.parse(localStorage.getItem(STORAGE.USER_INFO) ||
                sessionStorage.getItem(STORAGE.USER_INFO) || 'null');
        } catch (e) { return null; }
    }
    function setUserInfo(info, remember) {
        var s = JSON.stringify(info || {});
        if (remember) localStorage.setItem(STORAGE.USER_INFO, s);
        else sessionStorage.setItem(STORAGE.USER_INFO, s);
    }

    /* ============ 消息提示 ============ */
    function toast(type, msg) {
        if (global.uiModal && typeof global.uiModal[type] === 'function') {
            global.uiModal[type](msg);
        } else if (type === 'error' || type === 'warning') {
            console.error('[PRTS]', msg);
        } else {
            console.log('[PRTS]', msg);
        }
    }

    /* ============ 响应解包（标准化 -> 易用形态） ============ */
    function normalize(payload, httpStatus, rawText) {
        // 是否为标准信封（含 code 字段）
        if (payload && typeof payload === 'object' && !Array.isArray(payload) && ('code' in payload)) {
            var code = payload.code;
            if (code !== 200) {
                var err = new Error(payload.message || '请求失败');
                err.code = code;
                err.data = payload.data;
                return { __error: err };
            }
            var data = payload.data;
            if (data && typeof data === 'object' && !Array.isArray(data)) {
                var merged = {};
                for (var k in data) {
                    if (Object.prototype.hasOwnProperty.call(data, k)) merged[k] = data[k];
                }
                merged.code = code;
                merged.message = payload.message || '';
                merged.success = true;
                return merged;
            }
            // data 为数组/基础类型 -> 原样返回；为 null/undefined -> 仍返回成功对象（保留 success 判定）
            if (data === null || data === undefined) {
                return { code: code, message: payload.message || '', success: true };
            }
            return data;
        }
        // 兼容旧式响应（无信封）：HTTP 失败直接抛错
        if (httpStatus && httpStatus >= 400) {
            var msg2 = (typeof rawText === 'string' && rawText) ? rawText : ('HTTP ' + httpStatus);
            return { __error: new Error(msg2) };
        }
        return payload;
    }

    /* ============ 核心请求 ============ */
    function request(method, path, data, opts) {
        opts = opts || {};
        var url = buildUrl(path);
        var headers = { 'Content-Type': 'application/json' };
        var token = getToken();
        if (token) headers['Authorization'] = 'Bearer ' + token;

        var config = { method: method, mode: 'cors', headers: headers, credentials: 'omit' };
        if (data && method !== 'GET') config.body = JSON.stringify(data);
        if (method === 'GET' && data) {
            var qs = new URLSearchParams(data).toString();
            if (qs) url += (url.indexOf('?') === -1 ? '?' : '&') + qs;
            config.body = undefined;
        }
        if (opts.formBody) {
            config.body = opts.formBody;
            config.headers['Content-Type'] = 'application/x-www-form-urlencoded';
        }

        return fetch(url, config).then(function (resp) {
            return resp.text().then(function (text) {
                var payload = null;
                try { payload = text ? JSON.parse(text) : null; } catch (e) { payload = null; }
                if (payload === null) {
                    if (!resp.ok) return { __error: new Error(resp.statusText || '请求失败') };
                    return null;
                }
                var result = normalize(payload, resp.status, text);
                if (result && result.__error) throw result.__error;
                return result;
            });
        });
    }

    /* ============ 格式化工具 ============ */
    var format = {
        pad: function (n) { return n < 10 ? '0' + n : '' + n; },
        dateTime: function (date, fmt) {
            fmt = fmt || 'YYYY-MM-DD HH:mm:ss';
            if (!date) return '';
            var d = (date instanceof Date) ? date : new Date(date);
            if (isNaN(d.getTime())) return '';
            var map = {
                'YYYY': d.getFullYear(),
                'MM': this.pad(d.getMonth() + 1),
                'DD': this.pad(d.getDate()),
                'HH': this.pad(d.getHours()),
                'mm': this.pad(d.getMinutes()),
                'ss': this.pad(d.getSeconds())
            };
            return fmt.replace(/YYYY|MM|DD|HH|mm|ss/g, function (m) { return map[m]; });
        },
        questionText: function (text) {
            if (!text) return '';
            return String(text).replace(/\r\n/g, '\n').replace(/\n/g, '<br>');
        }
    };

    /* ============ 导出 ============ */
    PRTS.STORAGE = STORAGE;
    PRTS.getBase = getBase;
    PRTS.buildUrl = buildUrl;
    PRTS.getToken = getToken;
    PRTS.setToken = setToken;
    PRTS.clearAuth = clearAuth;
    PRTS.getUserInfo = getUserInfo;
    PRTS.setUserInfo = setUserInfo;
    PRTS.toast = toast;
    PRTS.format = format;
    PRTS.normalize = normalize;
    PRTS.request = request;
    PRTS.get = function (p, d) { return request('GET', p, d); };
    PRTS.post = function (p, d, o) { return request('POST', p, d, o); };
    PRTS.put = function (p, d, o) { return request('PUT', p, d, o); };
    PRTS.del = function (p, d) { return request('DELETE', p, d); };

})(window);
