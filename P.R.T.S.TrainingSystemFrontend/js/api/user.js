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
    const API_PREFIX = ((window.API_BASE_URL && String(window.API_BASE_URL)) || 'http://localhost:8080').replace(/\/+$/, '') + '/api/v1';

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

            // 优先使用 window.api.auth.register（该方法使用 application/x-www-form-urlencoded 提交）
            if (window.api && api.auth && typeof api.auth.register === 'function') {
                return api.auth.register(username, password, email)
                    .then(res => ({
                        success: true,
                        message: '注册成功',
                        userId: res.userId || res.id,
                        user: res
                    }))
                    .catch(error => ({
                        success: false,
                        message: (error && error.message) || '注册失败，用户名可能已存在'
                    }));
            }

            // 降级：直接以 form 表单格式 POST 到 /register（避免 /auth 前缀）
            const body = new URLSearchParams();
            body.append('username', username);
            body.append('password', password);
            if (email) body.append('email', email);

            return fetch((window.API_BASE_URL || 'http://localhost:8080') + '/api/v1/auth/register', {
                method: 'POST',
                mode: 'cors',
                headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
                body: body.toString()
            }).then(async resp => {
                const text = await resp.text();
                if (!resp.ok) throw new Error(text || resp.statusText);
                try { const json = JSON.parse(text); return { success: true, message: '注册成功', user: json, userId: json.userId || json.id }; }
                catch (e) { return { success: true, message: '注册成功', user: text }; }
            }).catch(error => ({ success: false, message: error.message || '注册失败' }));
        },

        // 【认证模块-2】用户登录
        login: function (username, password, rememberMe = false) {
            // 优先使用 window.api.auth.login（使用 form 提交）
            if (window.api && api.auth && typeof api.auth.login === 'function') {
                return api.auth.login(username, password)
                    .then(res => {
                        if (res.token) {
                            setToken(res.token, rememberMe);
                            if (res.user) setUserInfo(res.user, rememberMe);
                            return {
                                success: true,
                                user: res.user,
                                token: res.token,
                                isAdmin: res.user?.isAdmin || false,
                                message: '登录成功'
                            };
                        }
                        // 有些实现直接返回用户对象
                        if (res.user || res.id || res.username) {
                            if (res.token) setToken(res.token, rememberMe);
                            if (res.user) setUserInfo(res.user, rememberMe);
                            return { success: true, user: res.user || res, message: '登录成功' };
                        }
                        throw new Error('登录失败：服务器返回格式异常');
                    })
                    .catch(error => ({ success: false, message: error.message || '登录失败，请检查用户名和密码' }));
            }

            // 降级：以 form 表单格式 POST 到 /login（避免 /auth 前缀）
            const body = new URLSearchParams();
            body.append('username', username);
            body.append('password', password);

            return fetch((window.API_BASE_URL || 'http://localhost:8080') + '/api/v1/auth/login', {
                method: 'POST',
                mode: 'cors',
                headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
                body: body.toString()
            }).then(async resp => {
                const text = await resp.text();
                if (!resp.ok) throw new Error(text || resp.statusText);
                try {
                    const json = JSON.parse(text);
                    if (json.token) {
                        setToken(json.token, rememberMe);
                        if (json.user) setUserInfo(json.user, rememberMe);
                        return { success: true, user: json.user, token: json.token, isAdmin: json.user?.isAdmin || false, message: '登录成功' };
                    }
                    // 兼容后端直接返回用户对象
                    if (json.user || json.id || json.username) {
                        if (json.token) setToken(json.token, rememberMe);
                        if (json.user) setUserInfo(json.user, rememberMe);
                        return { success: true, user: json.user || json, token: json.token, message: '登录成功' };
                    }
                    throw new Error('登录失败：服务器返回格式异常');
                } catch (e) {
                    // 若非 JSON 返回，视为失败
                    throw new Error(text || '登录失败');
                }
            }).catch(error => ({ success: false, message: error.message || '登录失败，请检查用户名和密码' }));
        },

        // 【认证模块-3】获取当前登录用户信息
        getCurrentUser: function () {
            // 优先使用 api 提供的方法（如果存在）
            if (window.api && api.auth && typeof api.auth.getCurrentUser === 'function') {
                return api.auth.getCurrentUser()
                    .then(res => {
                        setUserInfo(res);
                        return res;
                    })
                    .catch(error => {
                        if (error && error.message && error.message.includes('401')) removeAuthData();
                        return null;
                    });
            }

            // 降级：/api/v1/auth/profile
            const token = getToken();
            if (!token) return Promise.resolve(null);

            // 尝试 /user/me
            const base = (window.API_BASE_URL || 'http://localhost:8080');
            return fetch(base + '/api/v1/auth/profile', { method: 'GET', mode: 'cors', headers: { 'Authorization': 'Bearer ' + token } })
                .then(async resp => {
                    if (!resp.ok) {
                        // 尝试 /user/{id} 需要从本地 userInfo 读取
                        const stored = sessionStorage.getItem('userInfo') || localStorage.getItem('userInfo');
                        if (stored) return JSON.parse(stored);
                        return null;
                    }
                    const json = await resp.json().catch(() => null);
                    if (json) {
                        setUserInfo(json);
                        return json;
                    }
                    return null;
                })
                .catch(() => {
                    // 无法获取，清除本地 auth
                    removeAuthData();
                    return null;
                });
        },

        // 【认证模块-4】用户退出登录
        logout: function () {
            return http.post('/api/v1/auth/logout')
                .then(() => {
                    removeAuthData();
                    console.log('退出登录成功');
                    return { success: true };
                })
                .catch(() => {
                    removeAuthData();
                    console.log('已清除本地登录状态');
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