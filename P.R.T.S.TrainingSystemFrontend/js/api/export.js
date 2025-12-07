(function () {
    'use strict';

    if (typeof http === 'undefined') {
        console.error('请先加载 request.js');
        return;
    }

    // 导出格式类型
    const exportFormats = {
        csv: { name: 'CSV文件', mimeType: 'text/csv', extension: 'csv' },
        excel: { name: 'Excel文件', mimeType: 'application/vnd.ms-excel', extension: 'xlsx' },
        pdf: { name: 'PDF文档', mimeType: 'application/pdf', extension: 'pdf' },
        json: { name: 'JSON数据', mimeType: 'application/json', extension: 'json' }
    };

    // 导出管理 API
    window.exportApi = {
        // 【导出模块-33】导出答题记录
        exportAnswers: function (params = {}) {
            const defaultParams = {
                format: 'csv',
                startDate: '',
                endDate: '',
                includeQuestions: true,
                includeAnalysis: false,
                includeStatistics: true
            };

            return http.post('/export/answers', { ...defaultParams, ...params })
                .then(result => {
                    // 自动触发下载
                    if (result.downloadUrl) {
                        this.downloadFile(result.downloadUrl, result.filename || `答题记录_${this.formatDate(new Date())}.${params.format || 'csv'}`);
                    }
                    return result;
                });
        },

        // 【导出模块-34】导出考试报告
        exportExamReport: function (examId, params = {}) {
            const defaultParams = {
                format: 'pdf',
                includeAnalysis: true,
                includeLeaderboard: true,
                includeRecommendations: true
            };

            return http.post(`/export/exam-report/${examId}`, { ...defaultParams, ...params })
                .then(result => {
                    if (result.downloadUrl) {
                        this.downloadFile(result.downloadUrl, result.filename || `考试报告_${examId}.${params.format || 'pdf'}`);
                    }
                    return result;
                });
        },

        // 导出用户统计数据
        exportUserStats: function (params = {}) {
            return http.post('/export/user-stats', {
                format: params.format || 'pdf',
                includeCharts: params.includeCharts !== false,
                includeProgress: true,
                includeRecommendations: true
            }).then(result => {
                if (result.downloadUrl) {
                    this.downloadFile(result.downloadUrl, result.filename || `学习统计_${this.formatDate(new Date())}.${params.format || 'pdf'}`);
                }
                return result;
            });
        },

        // 导出题目数据
        exportQuestions: function (params = {}) {
            return http.post('/export/questions', {
                format: params.format || 'excel',
                type: params.type,
                difficulty: params.difficulty,
                includeAnswers: params.includeAnswers !== false,
                includeStats: params.includeStats !== false,
                batchSize: params.batchSize || 500
            }).then(result => {
                if (result.downloadUrl) {
                    this.downloadFile(result.downloadUrl, result.filename || `题目数据_${this.formatDate(new Date())}.${params.format || 'xlsx'}`);
                }
                return result;
            });
        },

        // 导出错题本
        exportWrongQuestions: function (params = {}) {
            return http.post('/export/wrong-questions', {
                format: params.format || 'pdf',
                includeAnalysis: true,
                includeCorrectAnswers: true,
                includeCategoryStats: true
            }).then(result => {
                if (result.downloadUrl) {
                    this.downloadFile(result.downloadUrl, result.filename || `错题本_${this.formatDate(new Date())}.${params.format || 'pdf'}`);
                }
                return result;
            });
        }
    };

    // 导出辅助函数
    window.exportHelper = {
        // 下载文件
        downloadFile: function (url, filename) {
            // 如果 URL 是相对路径，添加 baseURL
            if (!url.startsWith('http') && !url.startsWith('/')) {
                url = (window.API_BASE_URL || '/api') + (url.startsWith('/') ? url : '/' + url);
            }

            // 创建临时链接
            const link = document.createElement('a');
            link.href = url;
            link.download = filename;
            link.style.display = 'none';
            document.body.appendChild(link);
            link.click();
            document.body.removeChild(link);

            // 清理 Blob URL
            if (url.startsWith('blob:')) {
                URL.revokeObjectURL(url);
            }
        },

        // 将数据导出为 CSV
        exportToCSV: function (data, filename = 'export.csv') {
            if (!data || data.length === 0) {
                console.error('没有数据可以导出');
                return;
            }

            // 获取表头
            const headers = Object.keys(data[0]);

            // 构建 CSV 内容
            let csvContent = headers.join(',') + '\n';

            data.forEach(row => {
                const rowData = headers.map(header => {
                    let value = row[header];

                    // 处理特殊字符
                    if (typeof value === 'string') {
                        // 处理换行和逗号
                        value = value.replace(/"/g, '""');
                        if (value.includes(',') || value.includes('\n') || value.includes('"')) {
                            value = `"${value}"`;
                        }
                    } else if (value === null || value === undefined) {
                        value = '';
                    }

                    return value;
                });

                csvContent += rowData.join(',') + '\n';
            });

            // 创建 Blob 并下载
            const blob = new Blob(['\ufeff' + csvContent], { type: 'text/csv;charset=utf-8;' });
            const url = URL.createObjectURL(blob);
            exportApi.downloadFile(url, filename);
        },

        // 将数据导出为 JSON
        exportToJSON: function (data, filename = 'export.json') {
            const jsonString = JSON.stringify(data, null, 2);
            const blob = new Blob([jsonString], { type: 'application/json' });
            const url = URL.createObjectURL(blob);
            exportApi.downloadFile(url, filename);
        },

        // 格式化日期
        formatDate: function (date, format = 'YYYY-MM-DD') {
            if (!date) return '';

            const d = new Date(date);
            const year = d.getFullYear();
            const month = String(d.getMonth() + 1).padStart(2, '0');
            const day = String(d.getDate()).padStart(2, '0');
            const hours = String(d.getHours()).padStart(2, '0');
            const minutes = String(d.getMinutes()).padStart(2, '0');
            const seconds = String(d.getSeconds()).padStart(2, '0');

            switch (format) {
                case 'YYYY-MM-DD':
                    return `${year}-${month}-${day}`;
                case 'YYYY-MM-DD HH:mm':
                    return `${year}-${month}-${day} ${hours}:${minutes}`;
                case 'YYYY-MM-DD HH:mm:ss':
                    return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`;
                case 'MM-DD HH:mm':
                    return `${month}-${day} ${hours}:${minutes}`;
                default:
                    return `${year}-${month}-${day}`;
            }
        },

        // 生成默认文件名
        generateFilename: function (prefix, format = 'csv') {
            const dateStr = this.formatDate(new Date(), 'YYYY-MM-DD_HH-mm');
            return `${prefix}_${dateStr}.${format}`;
        },

        // 批量导出处理
        createExportJob: function (type, params) {
            return new Promise((resolve, reject) => {
                let jobId = 'export_' + Date.now();
                let progress = 0;

                // 模拟进度更新
                const progressInterval = setInterval(() => {
                    if (progress < 90) {
                        progress += 10;
                        this.updateExportProgress(jobId, progress);
                    }
                }, 300);

                // 实际导出操作
                setTimeout(() => {
                    clearInterval(progressInterval);

                    switch (type) {
                        case 'answers':
                            exportApi.exportAnswers(params)
                                .then(result => {
                                    this.updateExportProgress(jobId, 100, true);
                                    resolve({ jobId, success: true, result });
                                })
                                .catch(error => {
                                    this.updateExportProgress(jobId, 0, false, error.message);
                                    reject(error);
                                });
                            break;

                        case 'exam':
                            exportApi.exportExamReport(params.examId, params)
                                .then(result => {
                                    this.updateExportProgress(jobId, 100, true);
                                    resolve({ jobId, success: true, result });
                                })
                                .catch(error => {
                                    this.updateExportProgress(jobId, 0, false, error.message);
                                    reject(error);
                                });
                            break;

                        default:
                            clearInterval(progressInterval);
                            reject(new Error('不支持的导出类型'));
                    }
                }, 1000);
            });
        },

        // 更新导出进度
        updateExportProgress: function (jobId, progress, completed = false, error = null) {
            const event = new CustomEvent('exportProgress', {
                detail: { jobId, progress, completed, error }
            });
            window.dispatchEvent(event);
        },

        // 获取支持的导出格式
        getSupportedFormats: function (exportType = 'answers') {
            const formats = {
                answers: ['csv', 'excel', 'pdf'],
                exam: ['pdf', 'excel'],
                questions: ['excel', 'csv', 'json'],
                stats: ['pdf', 'excel']
            };

            return formats[exportType] || ['csv', 'excel'];
        }
    };

    // 监听导出进度事件
    window.addEventListener('exportProgress', (event) => {
        const { jobId, progress, completed, error } = event.detail;

        if (error) {
            console.error(`导出任务 ${jobId} 失败:`, error);
        } else if (completed) {
            console.log(`导出任务 ${jobId} 完成`);
        } else {
            console.log(`导出任务 ${jobId} 进度: ${progress}%`);
        }
    });

})();