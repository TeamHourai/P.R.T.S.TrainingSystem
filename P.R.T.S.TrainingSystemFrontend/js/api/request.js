(function () {
    'use strict';

    // 创建 axios 实例
    const service = axios.create({
        // 将 baseURL 改为后端地址优先（避免默认 '/api' 指向静态服务器）
        baseURL: (window.API_BASE_URL || 'http://localhost:8080'),
        timeout: 15000,
        headers: {
            'Content-Type': 'application/json;charset=UTF-8'
        }
    });

    // 请求计数器
    let requestCount = 0;

    // 显示加载动画
    const showLoading = () => {
        if (requestCount === 0) {
            const loadingDiv = document.createElement('div');
            loadingDiv.id = 'global-loading';
            loadingDiv.innerHTML = `
                <div style="position: fixed; top: 0; left: 0; width: 100%; height: 100%; 
                            background: rgba(0,0,0,0.5); z-index: 9999; display: flex; 
                            align-items: center; justify-content: center;">
                    <div style="background: white; padding: 20px; border-radius: 8px;">
                        加载中...
                    </div>
                </div>
            `;
            document.body.appendChild(loadingDiv);
        }
        requestCount++;
    };

    // 隐藏加载动画
    const hideLoading = () => {
        requestCount--;
        if (requestCount <= 0) {
            const loadingDiv = document.getElementById('global-loading');
            if (loadingDiv) loadingDiv.remove();
            requestCount = 0;
        }
    };

    // 显示消息提示
    const showMessage = (message, type = 'error') => {
        const msgDiv = document.createElement('div');
        msgDiv.innerHTML = `
            <div style="position: fixed; top: 20px; right: 20px; 
                        background: ${type === 'error' ? '#f44336' : '#4caf50'}; 
                        color: white; padding: 12px 24px; border-radius: 4px; 
                        z-index: 10000; box-shadow: 0 2px 8px rgba(0,0,0,0.2);">
                ${message}
            </div>
        `;
        document.body.appendChild(msgDiv);
        setTimeout(() => msgDiv.remove(), 3000);
    };

    // 获取 token
    const getToken = () => {
        return localStorage.getItem('token') || sessionStorage.getItem('token');
    };

    // 清除认证信息
    const clearAuth = () => {
        localStorage.removeItem('token');
        sessionStorage.removeItem('token');
        localStorage.removeItem('userInfo');
        sessionStorage.removeItem('userInfo');
    };

    // 请求拦截器：添加认证令牌和全局 loading
    service.interceptors.request.use(
        config => {
            // 配置是否显示 loading
            const showLoadingDefault = ['post', 'put', 'delete'].includes(config.method?.toLowerCase());
            config.showLoading = config.showLoading ?? showLoadingDefault;

            if (config.showLoading) showLoading();

            // 添加认证头
            const token = getToken();
            if (token) config.headers['Authorization'] = 'Bearer ' + token;

            // GET 请求防缓存
            if (config.method?.toLowerCase() === 'get') {
                config.params = { ...config.params, _t: Date.now() };
            }

            return config;
        },
        error => {
            if (error.config?.showLoading) hideLoading();
            return Promise.reject(error);
        }
    );

    // 响应拦截器：统一处理响应数据和错误
    service.interceptors.response.use(
        response => {
            if (response.config?.showLoading) hideLoading();

            const res = response.data;

            // 处理标准响应格式
            if (res.code !== undefined) {
                if (res.code === 200) {
                    return res.data || res;
                }

                // 错误处理
                const errorMessage = res.message || res.msg || '操作失败';
                showMessage(errorMessage, 'error');

                // 401 未授权处理
                if (res.code === 401) {
                    clearAuth();
                    if (!window.location.href.includes('login')) {
                        window.location.href = 'index.html#login';
                    }
                }

                return Promise.reject(new Error(errorMessage));
            }

            return res;
        },
        error => {
            if (error.config?.showLoading) hideLoading();

            let errorMessage = '';
            if (error.response) {
                switch (error.response.status) {
                    case 400: errorMessage = error.response.data?.message || '请求参数错误'; break;
                    case 401:
                        errorMessage = '登录已过期，请重新登录';
                        clearAuth();
                        if (!window.location.href.includes('login')) {
                            window.location.href = 'index.html#login';
                        }
                        break;
                    case 403: errorMessage = '没有权限执行此操作'; break;
                    case 404: errorMessage = '请求的资源不存在'; break;
                    case 422: errorMessage = error.response.data?.message || '数据验证失败'; break;
                    case 500: errorMessage = '服务器内部错误'; break;
                    default: errorMessage = `请求失败 (${error.response.status})`;
                }
            } else if (error.request) {
                errorMessage = '网络连接异常，请检查网络设置';
            } else {
                errorMessage = error.message || '请求配置错误';
            }

            if (errorMessage) showMessage(errorMessage, 'error');
            console.error('API请求失败:', errorMessage, error);
            return Promise.reject(error);
        }
    );

    // HTTP 方法封装
    window.http = {
        // GET 请求：获取资源
        get(url, params = {}, config = {}) {
            return service.get(url, { params, showLoading: config.showLoading || false, ...config });
        },

        // POST 请求：创建资源
        post(url, data = {}, config = {}) {
            return service.post(url, data, { showLoading: config.showLoading ?? true, ...config });
        },

        // PUT 请求：更新资源
        put(url, data = {}, config = {}) {
            return service.put(url, data, { showLoading: config.showLoading ?? true, ...config });
        },

        // DELETE 请求：删除资源
        delete(url, params = {}, config = {}) {
            return service.delete(url, { params, showLoading: config.showLoading ?? true, ...config });
        },

        // 文件上传
        upload(url, file, config = {}) {
            const formData = new FormData();
            formData.append('file', file);
            return service.post(url, formData, {
                headers: { 'Content-Type': 'multipart/form-data' },
                showLoading: config.showLoading ?? true,
                ...config
            });
        }
    };

    // 全局访问
    window.ApiRequest = service;

})();