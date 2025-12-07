(function () {
    'use strict';

    if (typeof http === 'undefined') {
        console.error('请先加载 request.js');
        return;
    }

    // 系统工具 API
    window.systemApi = {
        // 【工具接口-35】健康检查
        healthCheck: function () {
            return http.get('/health', {}, { showLoading: false })
                .then(data => {
                    console.log('服务健康状态:', data);
                    return {
                        ...data,
                        timestamp: new Date().toISOString(),
                        clientTime: Date.now()
                    };
                })
                .catch(error => {
                    console.error('健康检查失败:', error);
                    return {
                        status: 'DOWN',
                        error: error.message,
                        timestamp: new Date().toISOString()
                    };
                });
        },

        // 【工具接口-36】获取系统信息
        getSystemInfo: function () {
            return http.get('/system/info', {}, { showLoading: false });
        },

        // 检查更新
        checkForUpdates: function () {
            return http.get('/system/updates/check', {}, { showLoading: false });
        },

        // 获取服务器时间
        getServerTime: function () {
            return http.get('/system/time', {}, { showLoading: false });
        },

        // 获取系统配置
        getPublicConfig: function () {
            return http.get('/system/config', {}, { showLoading: false });
        },

        // 发送反馈
        sendFeedback: function (type, content, contact = '') {
            return http.post('/system/feedback', {
                type: type,
                content: content,
                contact: contact,
                userAgent: navigator.userAgent,
                timestamp: new Date().toISOString()
            });
        },

        // 报告问题
        reportIssue: function (title, description, severity = 'medium', steps = [], attachments = []) {
            return http.post('/system/issues', {
                title: title,
                description: description,
                severity: severity,
                steps: steps,
                attachments: attachments,
                url: window.location.href,
                browserInfo: this.getBrowserInfo()
            });
        }
    };

    // 系统工具辅助函数
    window.systemHelper = {
        // 获取浏览器信息
        getBrowserInfo: function () {
            const ua = navigator.userAgent;
            let browser = 'Unknown';
            let version = '';
            let os = 'Unknown';

            // 检测浏览器
            if (ua.includes('Firefox')) {
                browser = 'Firefox';
                version = ua.match(/Firefox\/(\d+\.\d+)/)?.[1] || '';
            } else if (ua.includes('Chrome') && !ua.includes('Edg')) {
                browser = 'Chrome';
                version = ua.match(/Chrome\/(\d+\.\d+)/)?.[1] || '';
            } else if (ua.includes('Safari') && !ua.includes('Chrome')) {
                browser = 'Safari';
                version = ua.match(/Version\/(\d+\.\d+)/)?.[1] || '';
            } else if (ua.includes('Edg')) {
                browser = 'Edge';
                version = ua.match(/Edg\/(\d+\.\d+)/)?.[1] || '';
            } else if (ua.includes('MSIE') || ua.includes('Trident/')) {
                browser = 'Internet Explorer';
                version = ua.match(/(?:MSIE |rv:)(\d+\.\d+)/)?.[1] || '';
            }

            // 检测操作系统
            if (ua.includes('Windows')) {
                os = 'Windows';
                if (ua.includes('Windows NT 10.0')) os = 'Windows 10';
                else if (ua.includes('Windows NT 6.3')) os = 'Windows 8.1';
                else if (ua.includes('Windows NT 6.2')) os = 'Windows 8';
                else if (ua.includes('Windows NT 6.1')) os = 'Windows 7';
            } else if (ua.includes('Mac OS X')) {
                os = 'macOS';
                const match = ua.match(/Mac OS X (\d+[._]\d+(?:[._]\d+)?)/);
                if (match) os = `macOS ${match[1].replace(/_/g, '.')}`;
            } else if (ua.includes('Android')) {
                os = 'Android';
                const match = ua.match(/Android (\d+\.\d+)/);
                if (match) os = `Android ${match[1]}`;
            } else if (ua.includes('Linux')) {
                os = 'Linux';
            }

            return {
                browser: browser,
                version: version,
                os: os,
                userAgent: ua,
                language: navigator.language,
                platform: navigator.platform,
                screenResolution: `${window.screen.width}x${window.screen.height}`,
                colorDepth: window.screen.colorDepth,
                timezone: Intl.DateTimeFormat().resolvedOptions().timeZone
            };
        },

        // 检查网络连接状态
        checkNetworkStatus: function () {
            return new Promise((resolve) => {
                const status = {
                    online: navigator.onLine,
                    timestamp: new Date().toISOString()
                };

                if (navigator.onLine) {
                    // 尝试访问一个快速响应的资源来确认连接
                    const img = new Image();
                    img.onload = () => {
                        status.latency = Date.now() - status.timestamp;
                        resolve({ ...status, connection: 'good' });
                    };
                    img.onerror = () => {
                        resolve({ ...status, connection: 'slow', note: '网络连接不稳定' });
                    };
                    img.src = 'https://www.google.com/favicon.ico?' + Date.now();
                } else {
                    resolve({ ...status, connection: 'offline' });
                }
            });
        },

        // 性能监控
        startPerformanceMonitoring: function () {
            if (!window.performance || !window.performance.timing) {
                console.warn('浏览器不支持性能监控');
                return;
            }

            const perfData = {
                pageLoadTime: 0,
                dnsLookupTime: 0,
                tcpConnectTime: 0,
                requestResponseTime: 0,
                domReadyTime: 0,
                totalLoadTime: 0
            };

            window.addEventListener('load', () => {
                setTimeout(() => {
                    const timing = window.performance.timing;

                    perfData.pageLoadTime = timing.loadEventEnd - timing.navigationStart;
                    perfData.dnsLookupTime = timing.domainLookupEnd - timing.domainLookupStart;
                    perfData.tcpConnectTime = timing.connectEnd - timing.connectStart;
                    perfData.requestResponseTime = timing.responseEnd - timing.requestStart;
                    perfData.domReadyTime = timing.domContentLoadedEventEnd - timing.navigationStart;
                    perfData.totalLoadTime = timing.loadEventEnd - timing.navigationStart;

                    console.log('页面性能数据:', perfData);

                    // 如果加载时间过长，记录到服务器
                    if (perfData.totalLoadTime > 5000) {
                        this.reportPerformanceIssue(perfData);
                    }
                }, 0);
            });

            return perfData;
        },

        // 报告性能问题
        reportPerformanceIssue: function (perfData) {
            const browserInfo = this.getBrowserInfo();
            const issue = {
                type: 'performance',
                severity: perfData.totalLoadTime > 10000 ? 'high' : 'medium',
                data: {
                    ...perfData,
                    ...browserInfo,
                    url: window.location.href,
                    timestamp: new Date().toISOString()
                }
            };

            // 保存到本地存储供后续分析
            this.savePerformanceData(issue);

            // 发送到服务器（如果用户同意）
            if (localStorage.getItem('allowTelemetry') === 'true') {
                systemApi.sendFeedback('performance', JSON.stringify(issue));
            }
        },

        // 保存性能数据
        savePerformanceData: function (data) {
            try {
                const key = 'performance_logs';
                const logs = JSON.parse(localStorage.getItem(key) || '[]');
                logs.push(data);

                // 保留最近100条记录
                if (logs.length > 100) {
                    logs.splice(0, logs.length - 100);
                }

                localStorage.setItem(key, JSON.stringify(logs));
            } catch (error) {
                console.error('保存性能数据失败:', error);
            }
        },

        // 清理本地存储
        cleanupLocalStorage: function () {
            const now = Date.now();
            const oneDay = 24 * 60 * 60 * 1000;
            const oneWeek = 7 * oneDay;

            // 清理过期的临时数据
            Object.keys(localStorage).forEach(key => {
                try {
                    const value = localStorage.getItem(key);
                    const data = JSON.parse(value);

                    if (data && data._expiry && data._expiry < now) {
                        localStorage.removeItem(key);
                    }
                } catch (e) {
                    // 不是 JSON 格式，跳过
                }
            });

            // 清理旧的缓存数据
            const cacheKeys = Object.keys(localStorage).filter(key =>
                key.startsWith('cache_') || key.startsWith('temp_')
            );

            cacheKeys.forEach(key => {
                const value = localStorage.getItem(key);
                try {
                    const data = JSON.parse(value);
                    if (data && data.timestamp) {
                        const age = now - new Date(data.timestamp).getTime();
                        if (age > oneWeek) {
                            localStorage.removeItem(key);
                        }
                    }
                } catch (e) {
                    // 清理无法解析的缓存
                    localStorage.removeItem(key);
                }
            });
        },

        // 自动清理（每小时一次）
        startAutoCleanup: function () {
            setInterval(() => {
                this.cleanupLocalStorage();
            }, 60 * 60 * 1000); // 每小时清理一次
        },

        // 获取客户端信息摘要
        getClientSummary: function () {
            const browserInfo = this.getBrowserInfo();
            const screenInfo = {
                width: window.screen.width,
                height: window.screen.height,
                availWidth: window.screen.availWidth,
                availHeight: window.screen.availHeight,
                pixelRatio: window.devicePixelRatio || 1
            };

            return {
                browser: `${browserInfo.browser} ${browserInfo.version}`,
                os: browserInfo.os,
                screen: `${screenInfo.width}x${screenInfo.height}`,
                language: browserInfo.language,
                timezone: browserInfo.timezone,
                userAgentHash: this.hashString(browserInfo.userAgent)
            };
        },

        // 简单的字符串哈希
        hashString: function (str) {
            let hash = 0;
            for (let i = 0; i < str.length; i++) {
                const char = str.charCodeAt(i);
                hash = ((hash << 5) - hash) + char;
                hash = hash & hash; // 转换为32位整数
            }
            return hash.toString(16);
        }
    };

    // 启动自动清理
    if (typeof window !== 'undefined') {
        window.addEventListener('load', () => {
            setTimeout(() => {
                systemHelper.startAutoCleanup();
            }, 10000); // 延迟10秒启动
        });
    }

})();