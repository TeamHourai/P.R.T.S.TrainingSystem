(function () {
    'use strict';

    if (typeof http === 'undefined') {
        console.error('请先加载 request.js');
        return;
    }

    // 本地存储操作
    const getToken = () => localStorage.getItem('token') || sessionStorage.getItem('token');
    const setToken = (token, remember = false) => {
        if (remember) localStorage.setItem('token', token);
        else sessionStorage.setItem('token', token);
    };
    const setUserInfo = (userInfo, remember = false) => {
        const data = JSON.stringify(userInfo);
        if (remember) localStorage.setItem('userInfo', data);
        else sessionStorage.setItem('userInfo', data);
    };
    const removeAuthData = () => {
        localStorage.removeItem('token');
        sessionStorage.removeItem('token');
        localStorage.removeItem('userInfo');
        sessionStorage.removeItem('userInfo');
    };

    // 统一API前缀
    const API_PREFIX = ((window.API_BASE_URL && String(window.API_BASE_URL)) || 'http://localhost:8888').replace(/\/+$/, '') + '/api/v1';

    // 用户认证和资料管理 API
    window.userApi = {
        // 【认证模块-1】用户注册
        register: function (username, password, email) {
            if (!username || username.length < 3) {
                return Promise.resolve({ success: false, message: '用户名至少需要3个字符' });
            }
            if (!password || password.length < 6) {
                return Promise.resolve({ success: false, message: '密码至少需要6个字符' });
            }

            // 优先使用 window.api.auth.register（已按规范实现）
            if (window.api && api.auth && typeof api.auth.register === 'function') {
                return api.auth.register(username, password, email)
                    .then(res => {
                        // 兼容API规范响应结构
                        if (res && typeof res === 'object' && 'code' in res) {
                            if (res.code === 200) {
                                return {
                                    success: true,
                                    message: res.message || '注册成功',
                                    userId: res.data?.userId,
                                    user: res.data
                                };
                            } else {
                                return { success: false, message: res.message || '注册失败' };
                            }
                        }
                        return { success: false, message: '注册失败' };
                    })
                    .catch(error => ({
                        success: false,
                        message: (error && error.message) || '注册失败，用户名可能已存在'
                    }));
            }

            // 降级：直接以 JSON 格式 POST 到 /api/v1/auth/register
            return fetch(API_PREFIX + '/auth/register', {
                method: 'POST',
                mode: 'cors',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ username, password, email })
            }).then(async resp => {
                const text = await resp.text();
                let json;
                try { json = JSON.parse(text); } catch (e) { json = null; }
                if (!resp.ok || !json || json.code !== 200) {
                    throw new Error((json && json.message) || text || resp.statusText);
                }
                return {
                    success: true,
                    message: json.message || '注册成功',
                    user: json.data,
                    userId: json.data?.userId
                };
            }).catch(error => ({ success: false, message: error.message || '注册失败' }));
        },

        // 【认证模块-2】用户登录
        login: function (username, password, rememberMe = false) {
            // 优先使用 window.api.auth.login（已按规范实现）
            if (window.api && api.auth && typeof api.auth.login === 'function') {
                return api.auth.login(username, password)
                    .then(res => {
                        // 兼容API规范响应结构
                        if (res && typeof res === 'object' && 'code' in res) {
                            if (res.code === 200 && res.data && res.data.token) {
                                setToken(res.data.token, rememberMe);
                                setUserInfo(res.data, rememberMe);
                                return {
                                    success: true,
                                    user: res.data,
                                    token: res.data.token,
                                    isAdmin: res.data.isAdmin || false,
                                    message: res.message || '登录成功'
                                };
                            } else {
                                return { success: false, message: res.message || '登录失败' };
                            }
                        }
                        return { success: false, message: '登录失败' };
                    })
                    .catch(error => ({ success: false, message: error.message || '登录失败，请检查用户名和密码' }));
            }

            // 降级：以 JSON 格式 POST 到 /api/v1/auth/login
            return fetch(API_PREFIX + '/auth/login', {
                method: 'POST',
                mode: 'cors',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ username, password })
            }).then(async resp => {
                const text = await resp.text();
                let json;
                try { json = JSON.parse(text); } catch (e) { json = null; }
                if (!resp.ok || !json || json.code !== 200 || !json.data?.token) {
                    throw new Error((json && json.message) || text || resp.statusText);
                }
                setToken(json.data.token, rememberMe);
                setUserInfo(json.data, rememberMe);
                return {
                    success: true,
                    user: json.data,
                    token: json.data.token,
                    isAdmin: json.data.isAdmin || false,
                    message: json.message || '登录成功'
                };
            }).catch(error => ({ success: false, message: error.message || '登录失败，请检查用户名和密码' }));
        },

        // 【认证模块-3】获取当前登录用户信息
        getCurrentUser: function () {
            // 优先使用 api 提供的方法（如果存在）
            if (window.api && api.auth && typeof api.auth.getCurrentUser === 'function') {
                return api.auth.getCurrentUser()
                    .then(res => {
                        // 兼容API规范响应结构
                        if (res && typeof res === 'object' && 'code' in res) {
                            if (res.code === 200 && res.data) {
                                setUserInfo(res.data);
                                return res.data;
                            }
                            return null;
                        }
                        return null;
                    })
                    .catch(error => {
                        if (error && error.message && error.message.includes('401')) removeAuthData();
                        return null;
                    });
            }

            // 降级：/api/v1/auth/profile
            const token = getToken();
            if (!token) return Promise.resolve(null);

            return fetch(API_PREFIX + '/auth/profile', {
                method: 'GET',
                mode: 'cors',
                headers: { 'Authorization': 'Bearer ' + token }
            }).then(async resp => {
                const text = await resp.text();
                let json;
                try { json = JSON.parse(text); } catch (e) { json = null; }
                if (!resp.ok || !json || json.code !== 200 || !json.data) {
                    // token失效
                    removeAuthData();
                    return null;
                }
                setUserInfo(json.data);
                return json.data;
            }).catch(() => {
                removeAuthData();
                return null;
            });
        },

        // 【认证模块-4】用户退出登录
        logout: function () {
            const token = getToken();
            return fetch(API_PREFIX + '/auth/logout', {
                method: 'POST',
                mode: 'cors',
                headers: { 'Content-Type': 'application/json', ...(token ? { 'Authorization': 'Bearer ' + token } : {}) },
                body: '{}'
            }).then(async resp => {
                removeAuthData();
                return { success: true };
            }).catch(() => {
                removeAuthData();
                return { success: true };
            });
        },

        // 检查登录状态：验证令牌有效性
        checkLoginStatus: function () {
            const token = getToken();
            if (!token) return Promise.resolve(false);

            return this.getCurrentUser()
                .then(userInfo => !!userInfo)
                .catch(() => false);
        }
    };

})();