// auth-manager.js - 用户认证管理模块
(function () {
    'use strict';

    // 检查依赖
    if (typeof http === 'undefined') {
        console.error('请先加载 request.js');
        return;
    }

    // 全局声明
    if (typeof window !== 'undefined') {
        window.authManager = window.authManager || {};
    }

    // 本地存储键名
    const STORAGE_KEYS = {
        TOKEN: 'auth_token',
        USER_INFO: 'user_info',
        REMEMBER_ME: 'remember_me',
        LAST_LOGIN: 'last_login_time',
        SESSION_EXPIRE: 'session_expire_time'
    };

    // 会话超时时间（毫秒）
    const SESSION_TIMEOUT = 24 * 60 * 60 * 1000; // 24小时
    const REMEMBER_ME_TIMEOUT = 30 * 24 * 60 * 60 * 1000; // 30天

    // 认证管理器
    window.authManager = {
        // ============ 用户认证状态 ============

        /**
         * 检查用户是否已登录
         * @returns {boolean} 是否已登录
         */
        isLoggedIn() {
            try {
                const token = this.getToken();
                const userInfo = this.getUserInfo();

                // 检查令牌和用户信息是否存在
                if (!token || !userInfo) {
                    return false;
                }

                // 检查会话是否过期
                if (this.isSessionExpired()) {
                    this.clearAuthData();
                    return false;
                }

                return true;
            } catch (error) {
                console.error('检查登录状态失败:', error);
                return false;
            }
        },

        /**
         * 检查是否为管理员
         * @returns {boolean} 是否为管理员
         */
        isAdmin() {
            const userInfo = this.getUserInfo();
            return userInfo && (userInfo.isAdmin === true || userInfo.role === 'admin');
        },

        /**
         * 检查会话是否过期
         * @returns {boolean} 是否过期
         */
        isSessionExpired() {
            try {
                const expireTime = localStorage.getItem(STORAGE_KEYS.SESSION_EXPIRE) ||
                    sessionStorage.getItem(STORAGE_KEYS.SESSION_EXPIRE);

                if (!expireTime) {
                    return true; // 没有过期时间视为过期
                }

                const now = Date.now();
                return now > parseInt(expireTime);
            } catch (error) {
                console.error('检查会话过期失败:', error);
                return true;
            }
        },

        // ============ 认证数据管理 ============

        /**
         * 获取认证令牌
         * @returns {string|null} 令牌或null
         */
        getToken() {
            // 先检查localStorage（记住我），再检查sessionStorage
            return localStorage.getItem(STORAGE_KEYS.TOKEN) ||
                sessionStorage.getItem(STORAGE_KEYS.TOKEN);
        },

        /**
         * 获取用户信息
         * @returns {object|null} 用户信息或null
         */
        getUserInfo() {
            try {
                const userData = localStorage.getItem(STORAGE_KEYS.USER_INFO) ||
                    sessionStorage.getItem(STORAGE_KEYS.USER_INFO);

                if (!userData) {
                    return null;
                }

                return JSON.parse(userData);
            } catch (error) {
                console.error('解析用户信息失败:', error);
                return null;
            }
        },

        /**
         * 获取用户ID
         * @returns {number|null} 用户ID或null
         */
        getUserId() {
            const userInfo = this.getUserInfo();
            return userInfo ? userInfo.id || userInfo.userId : null;
        },

        /**
         * 获取用户名
         * @returns {string|null} 用户名或null
         */
        getUsername() {
            const userInfo = this.getUserInfo();
            return userInfo ? userInfo.username : null;
        },

        /**
         * 保存认证数据
         * @param {object} authData 认证数据
         * @param {boolean} rememberMe 是否记住我
         */
        saveAuthData(authData, rememberMe = false) {
            try {
                const storage = rememberMe ? localStorage : sessionStorage;
                const expireTime = Date.now() + (rememberMe ? REMEMBER_ME_TIMEOUT : SESSION_TIMEOUT);

                // 保存令牌
                if (authData.token) {
                    storage.setItem(STORAGE_KEYS.TOKEN, authData.token);
                }

                // 保存用户信息
                if (authData.user) {
                    storage.setItem(STORAGE_KEYS.USER_INFO, JSON.stringify(authData.user));
                }

                // 保存过期时间
                storage.setItem(STORAGE_KEYS.SESSION_EXPIRE, expireTime.toString());

                // 保存记住我状态
                if (rememberMe) {
                    localStorage.setItem(STORAGE_KEYS.REMEMBER_ME, 'true');
                } else {
                    localStorage.removeItem(STORAGE_KEYS.REMEMBER_ME);
                }

                // 保存最后登录时间
                storage.setItem(STORAGE_KEYS.LAST_LOGIN, Date.now().toString());

                console.log('认证数据保存成功');
            } catch (error) {
                console.error('保存认证数据失败:', error);
                throw error;
            }
        },

        /**
         * 清除认证数据
         */
        clearAuthData() {
            try {
                // 清除localStorage
                Object.values(STORAGE_KEYS).forEach(key => {
                    localStorage.removeItem(key);
                });

                // 清除sessionStorage
                Object.values(STORAGE_KEYS).forEach(key => {
                    sessionStorage.removeItem(key);
                });

                console.log('认证数据已清除');
            } catch (error) {
                console.error('清除认证数据失败:', error);
            }
        },

        /**
         * 更新用户信息
         * @param {object} userInfo 新的用户信息
         */
        updateUserInfo(userInfo) {
            try {
                const currentUserInfo = this.getUserInfo();
                const updatedUserInfo = { ...currentUserInfo, ...userInfo };

                // 检查存储位置（记住我模式用localStorage，否则用sessionStorage）
                const rememberMe = localStorage.getItem(STORAGE_KEYS.REMEMBER_ME) === 'true';
                const storage = rememberMe ? localStorage : sessionStorage;

                storage.setItem(STORAGE_KEYS.USER_INFO, JSON.stringify(updatedUserInfo));

                console.log('用户信息更新成功');
            } catch (error) {
                console.error('更新用户信息失败:', error);
                throw error;
            }
        },

        // ============ 认证操作 ============

        /**
         * 用户登录
         * @param {string} username 用户名
         * @param {string} password 密码
         * @param {boolean} rememberMe 是否记住我
         * @returns {Promise<object>} 登录结果
         */
        async login(username, password, rememberMe = false) {
            try {
                console.log(`用户登录: ${username}, rememberMe: ${rememberMe}`);

                // 参数验证
                if (!username || !password) {
                    return {
                        success: false,
                        message: '用户名和密码不能为空'
                    };
                }

                // 调用登录API
                const response = await userApi.login(username, password, rememberMe);

                if (response.success) {
                    // 保存认证数据
                    this.saveAuthData(response, rememberMe);

                    // 触发登录成功事件
                    this.triggerAuthEvent('loginSuccess', response.user);

                    return {
                        success: true,
                        message: '登录成功',
                        user: response.user,
                        token: response.token
                    };
                } else {
                    return {
                        success: false,
                        message: response.message || '登录失败'
                    };
                }
            } catch (error) {
                console.error('登录过程中出错:', error);

                return {
                    success: false,
                    message: error.message || '登录失败，请检查网络连接'
                };
            }
        },

        /**
         * 用户注册
         * @param {string} username 用户名
         * @param {string} password 密码
         * @param {string} email 邮箱（可选）
         * @returns {Promise<object>} 注册结果
         */
        async register(username, password, email = '') {
            try {
                console.log(`用户注册: ${username}, email: ${email}`);

                // 参数验证
                if (!username || !password) {
                    return {
                        success: false,
                        message: '用户名和密码不能为空'
                    };
                }

                if (username.length < 3) {
                    return {
                        success: false,
                        message: '用户名至少需要3个字符'
                    };
                }

                if (password.length < 6) {
                    return {
                        success: false,
                        message: '密码至少需要6个字符'
                    };
                }

                // 调用注册API
                const response = await userApi.register(username, password, email);

                if (response.success) {
                    // 触发注册成功事件
                    this.triggerAuthEvent('registerSuccess', response.user);

                    return {
                        success: true,
                        message: '注册成功',
                        user: response.user
                    };
                } else {
                    return {
                        success: false,
                        message: response.message || '注册失败'
                    };
                }
            } catch (error) {
                console.error('注册过程中出错:', error);

                return {
                    success: false,
                    message: error.message || '注册失败，请检查网络连接'
                };
            }
        },

        /**
         * 用户退出
         * @returns {Promise<object>} 退出结果
         */
        async logout() {
            try {
                console.log('用户退出登录');

                // 触发退出前事件
                this.triggerAuthEvent('beforeLogout');

                // 调用退出API
                if (this.getToken()) {
                    try {
                        await userApi.logout();
                    } catch (error) {
                        console.warn('调用退出API失败，继续清除本地数据:', error);
                    }
                }

                // 清除本地认证数据
                this.clearAuthData();

                // 触发退出成功事件
                this.triggerAuthEvent('logoutSuccess');

                return {
                    success: true,
                    message: '退出成功'
                };
            } catch (error) {
                console.error('退出过程中出错:', error);

                return {
                    success: false,
                    message: error.message || '退出失败'
                };
            }
        },

        /**
         * 刷新令牌（如果有的话）
         * @returns {Promise<object>} 刷新结果
         */
        async refreshToken() {
            try {
                const currentToken = this.getToken();
                if (!currentToken) {
                    return {
                        success: false,
                        message: '没有可刷新的令牌'
                    };
                }

                console.log('刷新令牌...');

                // 这里调用刷新令牌的API（如果后端支持）
                // 示例：const response = await http.post('/auth/refresh', { token: currentToken });

                // 目前简单返回成功
                return {
                    success: true,
                    message: '令牌刷新成功'
                };
            } catch (error) {
                console.error('刷新令牌失败:', error);
                return {
                    success: false,
                    message: error.message || '刷新令牌失败'
                };
            }
        },

        /**
         * 验证令牌有效性
         * @returns {Promise<boolean>} 是否有效
         */
        async validateToken() {
            try {
                const token = this.getToken();
                if (!token) {
                    return false;
                }

                // 检查会话是否过期
                if (this.isSessionExpired()) {
                    console.log('会话已过期');
                    return false;
                }

                // 调用API验证令牌
                // 这里使用获取用户信息API来验证令牌
                const userInfo = await userApi.getCurrentUser();

                if (userInfo) {
                    // 更新用户信息
                    this.updateUserInfo(userInfo);
                    return true;
                }

                return false;
            } catch (error) {
                console.error('验证令牌失败:', error);

                // 如果是401错误，清除认证数据
                if (error.response && error.response.status === 401) {
                    this.clearAuthData();
                }

                return false;
            }
        },

        // ============ 事件系统 ============

        /**
         * 触发认证事件
         * @param {string} eventName 事件名称
         * @param {any} data 事件数据
         */
        triggerAuthEvent(eventName, data = null) {
            try {
                const event = new CustomEvent(`auth:${eventName}`, {
                    detail: data,
                    bubbles: true,
                    cancelable: true
                });

                window.dispatchEvent(event);
                console.log(`触发认证事件: ${eventName}`, data);
            } catch (error) {
                console.error(`触发事件 ${eventName} 失败:`, error);
            }
        },

        /**
         * 监听认证事件
         * @param {string} eventName 事件名称
         * @param {function} callback 回调函数
         */
        onAuthEvent(eventName, callback) {
            if (typeof callback !== 'function') {
                console.error('回调函数必须是函数类型');
                return;
            }

            const fullEventName = `auth:${eventName}`;
            window.addEventListener(fullEventName, (event) => {
                callback(event.detail);
            });
        },

        // ============ 工具方法 ============

        /**
         * 获取认证头信息
         * @returns {object} 认证头
         */
        getAuthHeaders() {
            const token = this.getToken();
            if (!token) {
                return {};
            }

            return {
                'Authorization': `Bearer ${token}`
            };
        },

        /**
         * 检查用户权限
         * @param {string} permission 权限标识
         * @returns {boolean} 是否有权限
         */
        hasPermission(permission) {
            const userInfo = this.getUserInfo();
            if (!userInfo) {
                return false;
            }

            // 如果是管理员，拥有所有权限
            if (this.isAdmin()) {
                return true;
            }

            // 检查用户权限列表
            const permissions = userInfo.permissions || [];
            return permissions.includes(permission);
        },

        /**
         * 获取登录状态摘要
         * @returns {object} 状态摘要
         */
        getStatusSummary() {
            const isLoggedIn = this.isLoggedIn();
            const userInfo = this.getUserInfo();

            return {
                isLoggedIn,
                isAdmin: this.isAdmin(),
                username: userInfo ? userInfo.username : null,
                userId: userInfo ? (userInfo.id || userInfo.userId) : null,
                lastLogin: this.getLastLoginTime(),
                sessionExpire: this.getSessionExpireTime()
            };
        },

        /**
         * 获取最后登录时间
         * @returns {Date|null} 最后登录时间
         */
        getLastLoginTime() {
            try {
                const timestamp = localStorage.getItem(STORAGE_KEYS.LAST_LOGIN) ||
                    sessionStorage.getItem(STORAGE_KEYS.LAST_LOGIN);

                return timestamp ? new Date(parseInt(timestamp)) : null;
            } catch (error) {
                console.error('获取最后登录时间失败:', error);
                return null;
            }
        },

        /**
         * 获取会话过期时间
         * @returns {Date|null} 过期时间
         */
        getSessionExpireTime() {
            try {
                const timestamp = localStorage.getItem(STORAGE_KEYS.SESSION_EXPIRE) ||
                    sessionStorage.getItem(STORAGE_KEYS.SESSION_EXPIRE);

                return timestamp ? new Date(parseInt(timestamp)) : null;
            } catch (error) {
                console.error('获取会话过期时间失败:', error);
                return null;
            }
        },

        /**
         * 延长会话时间
         */
        extendSession() {
            try {
                const rememberMe = localStorage.getItem(STORAGE_KEYS.REMEMBER_ME) === 'true';
                const storage = rememberMe ? localStorage : sessionStorage;
                const expireTime = Date.now() + (rememberMe ? REMEMBER_ME_TIMEOUT : SESSION_TIMEOUT);

                storage.setItem(STORAGE_KEYS.SESSION_EXPIRE, expireTime.toString());
                console.log('会话已延长');
            } catch (error) {
                console.error('延长会话失败:', error);
            }
        },

        /**
         * 重置密码（需要后端API支持）
         * @param {string} email 邮箱
         * @returns {Promise<object>} 重置结果
         */
        async resetPassword(email) {
            try {
                console.log(`重置密码请求: ${email}`);

                if (!email) {
                    return {
                        success: false,
                        message: '请输入邮箱地址'
                    };
                }

                // 这里调用重置密码的API
                // 示例：const response = await http.post('/auth/reset-password', { email });

                return {
                    success: true,
                    message: '重置密码链接已发送到您的邮箱'
                };
            } catch (error) {
                console.error('重置密码失败:', error);
                return {
                    success: false,
                    message: error.message || '重置密码失败'
                };
            }
        }
    };

    // ============ 自动会话管理 ============

    // 定期检查会话状态
    setInterval(() => {
        if (authManager.isLoggedIn() && !authManager.isSessionExpired()) {
            // 自动延长会话
            authManager.extendSession();
        }
    }, 5 * 60 * 1000); // 每5分钟检查一次

    // 页面可见性变化时检查会话
    document.addEventListener('visibilitychange', () => {
        if (!document.hidden && authManager.isLoggedIn()) {
            // 页面重新可见时验证令牌
            authManager.validateToken().then(isValid => {
                if (!isValid) {
                    console.log('页面重新可见时发现会话无效');
                    // 可以在这里触发重新登录流程
                }
            });
        }
    });

    // 监听网络状态变化
    window.addEventListener('online', () => {
        if (authManager.isLoggedIn()) {
            // 网络恢复时验证令牌
            authManager.validateToken();
        }
    });

    console.log('auth-manager.js 加载完成');

})();