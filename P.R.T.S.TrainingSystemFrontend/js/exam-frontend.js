// exam-frontend.js
// 挂载全真模拟页面的 Vue 实例，实现数据获取与交互

// 依赖 window.examApi

document.addEventListener('DOMContentLoaded', function () {
    new Vue({
        el: '#app',
        data: {
            examPaper: [], // 题目分组
            currentQuestionNumber: 1,
            showResult: false,
            showQuestionDetailModal: false,
            currentDetailQuestion: {},
            answeredCount: 0,
            mobileSheetOpen: false,
            elapsedTime: 0,
            remainingTime: 900, // 15分钟
            timer: null,
            totalScore: 0,
            maxScore: 100,
            questionObserver: null, // 新增：用于同步可见题目（保留）
            // 新增数据
            questionNodes: null,              // 缓存题目节点列表
            scrollRafScheduled: false,        // requestAnimationFrame 限流标志
            _onScrollHandler: null            // 存放绑定的监听函数以便解绑
        },
        computed: {
            formattedTime() {
                const min = Math.floor(this.remainingTime / 60).toString().padStart(2, '0');
                const sec = (this.remainingTime % 60).toString().padStart(2, '0');
                return `${min}:${sec}`;
            }
        },
        methods: {
            // 获取题号
            getQuestionNumber(sectionIndex, qIndex) {
                let num = 1;
                for (let i = 0; i < sectionIndex; i++) {
                    num += this.examPaper[i].questions.length;
                }
                return num + qIndex;
            },
            // 选项选择
            selectOption(qid, opt) {
                for (const section of this.examPaper) {
                    for (const q of section.questions) {
                        if (q.id === qid) {
                            q.userAnswer = opt;
                        }
                    }
                }
                this.answeredCount = this.examPaper.reduce((sum, sec) => sum + sec.questions.filter(q => q.userAnswer).length, 0);
            },
            // 滚动到题目
            scrollToQuestion(sectionIndex, qIndex) {
                const selector = `[data-question-number='${this.getQuestionNumber(sectionIndex, qIndex)}']`;
                const el = document.querySelector(selector);
                if (el) el.scrollIntoView({ behavior: 'smooth', block: 'center' });
                this.currentQuestionNumber = this.getQuestionNumber(sectionIndex, qIndex);
            },
            // 答题卡切换
            toggleMobileSheet() {
                this.mobileSheetOpen = !this.mobileSheetOpen;
            },
            // 格式化时间
            formatTime(sec) {
                const m = Math.floor(sec / 60).toString().padStart(2, '0');
                const s = (sec % 60).toString().padStart(2, '0');
                return `${m}:${s}`;
            },
            // 判断题目是否答对（通过归一化答案为数字后比较，兼容字母/数字/字符串等格式）
            isQuestionCorrect(q) {
                // 原始正确答案可能在 q.answer 或 q.correct
                const rawCorrect = (q.answer !== undefined && q.answer !== null) ? q.answer
                    : (q.correct !== undefined ? q.correct : null);
                // 用户答案可能是数字或字母等
                const rawUser = (q.userAnswer !== undefined && q.userAnswer !== null) ? q.userAnswer : null;

                const correctNum = this.normalizeToNumber(rawCorrect);
                const userNum = this.normalizeToNumber(rawUser);

                // 只有两者都能归一化为数字时才判为相等
                return (typeof correctNum === 'number' && !isNaN(correctNum)
                    && typeof userNum === 'number' && !isNaN(userNum)
                    && userNum === correctNum);
            },
            // 计算分数
            calculateSectionScore(section) {
                const correct = section.questions.filter(q => this.isQuestionCorrect(q)).length;
                return correct * 4; // 假设每题4分
            },
            // 提交试卷
            submitExam() {
                this.showResult = true;
                let total = 0;
                for (const section of this.examPaper) {
                    total += this.calculateSectionScore(section);
                }
                this.totalScore = total;
            },
            // 重新测试
            restartExam() {
                window.location.reload();
            },
            // 返回首页
            goBack() {
                window.location.href = 'index.html';
            },
            // 辅助：把可能为字母或数字的答案归一化为数字（1 -> A, 2 -> B）
            normalizeToNumber(ans) {
                if (ans === null || ans === undefined) return null;
                // 直接是数字或数字字符串
                const n = Number(ans);
                if (!isNaN(n)) return n;
                // 字母形式，如 'A' / 'b'
                if (typeof ans === 'string') {
                    const s = ans.trim().toUpperCase();
                    if (/^[A-Z]$/.test(s)) return s.charCodeAt(0) - 64;
                }
                return null;
            },

            // 辅助：安全地把答案显示为字母（优先使用归一化值，否则使用原始字符串）
            answerLetter(normalized, raw) {
                if (typeof normalized === 'number' && !isNaN(normalized)) {
                    return String.fromCharCode(64 + normalized);
                }
                if (raw !== null && raw !== undefined && raw !== '') return String(raw);
                return '-';
            },

            // 查看题目详情（改为在 nextTick 后显示，避免渲染/事件时序问题）
            showQuestionDetail(q, sectionIndex, qIndex) {
                const section = this.examPaper[sectionIndex] || {};
                const rawAnswer = (q.answer !== undefined && q.answer !== null) ? q.answer : (q.correct !== undefined ? q.correct : null);
                const rawUser = (q.userAnswer !== undefined && q.userAnswer !== null) ? q.userAnswer : null;
                const answerNorm = this.normalizeToNumber(rawAnswer);
                const userNorm = this.normalizeToNumber(rawUser);

                this.currentDetailQuestion = {
                    id: q.id,
                    question: q.question || '',
                    options: Array.isArray(q.options) ? q.options : (q.options ? q.options.split('|') : []),
                    // 归一化后的数值（1表示A，2表示B...），以及保留原始值
                    answerNormalized: answerNorm,
                    rawAnswer: rawAnswer,
                    userAnswerNormalized: userNorm,
                    rawUserAnswer: rawUser,
                    analysis: q.analysis || q.explain || '',
                    picture: q.picture || q.image || null,
                    typeText: section.typeName || q.typeName || q.type || '未知类型',
                    difficultyText: q.difficultyText || (typeof q.difficulty === 'number' ? ['常识','基操','娴熟','明智','深邃'][q.difficulty - 1] : q.difficulty) || '未知'
                };
                // 在 DOM 更新后再打开 modal，防止点击事件与插入时序冲突
                this.$nextTick(() => {
                    setTimeout(() => {
                        this.showQuestionDetailModal = true;
                        document.body.style.overflow = 'hidden';
                        if (this.$refs && this.$refs.modalOverlay) {
                            this.$refs.modalOverlay.style.zIndex = '9999';
                        }
                    }, 30);
                });
            },

            closeQuestionDetail() {
                this.showQuestionDetailModal = false;
                // 恢复 body 滚动
                document.body.style.overflow = '';
                if (this.$refs && this.$refs.modalOverlay) {
                    this.$refs.modalOverlay.style.zIndex = ''; // 恢复
                }
            },
            // 新增：基于视口垂直中心选取最近题号，避免部分显示导致跳动
            updateCurrentByViewportCenter() {
                const nodes = this.questionNodes && this.questionNodes.length ? this.questionNodes : Array.from(document.querySelectorAll('[data-question-number]'));
                if (!nodes || nodes.length === 0) return;
                const viewportCenter = window.innerHeight / 2;
                let best = { dist: Infinity, number: null };
                for (const n of nodes) {
                    const rect = n.getBoundingClientRect();
                    // 若元素完全在视口外可略过，但仍计算中心距离可以更稳健
                    const elCenter = rect.top + rect.height / 2;
                    const dist = Math.abs(elCenter - viewportCenter);
                    if (dist < best.dist) {
                        const numAttr = n.getAttribute('data-question-number');
                        const num = numAttr ? parseInt(numAttr, 10) : NaN;
                        if (!isNaN(num)) best = { dist, number: num };
                    }
                }
                if (best.number !== null && this.currentQuestionNumber !== best.number) {
                    this.currentQuestionNumber = best.number;
                }
            },

            // 新增：使用 requestAnimationFrame 限流更新
            scheduleUpdateCurrentQuestion() {
                if (this.scrollRafScheduled) return;
                this.scrollRafScheduled = true;
                requestAnimationFrame(() => {
                    this.updateCurrentByViewportCenter();
                    this.scrollRafScheduled = false;
                });
            },

            // 新增：绑定/解绑滚动和缩放监听
            bindScrollListener() {
                if (this._onScrollHandler) return;
                this._onScrollHandler = () => { this.scheduleUpdateCurrentQuestion(); };
                window.addEventListener('scroll', this._onScrollHandler, { passive: true });
                window.addEventListener('resize', this._onScrollHandler);
            },
            unbindScrollListener() {
                if (!this._onScrollHandler) return;
                window.removeEventListener('scroll', this._onScrollHandler);
                window.removeEventListener('resize', this._onScrollHandler);
                this._onScrollHandler = null;
            },

            // 原有的 initQuestionObserver 保留，但在渲染后缓存节点并触发视口中心检测
            initQuestionObserver() {
                // 先断开旧的 observer（若存在）
                if (this.questionObserver) {
                    this.questionObserver.disconnect();
                    this.questionObserver = null;
                }
                // 使用阈值数组便于选取最显著的可见项（保留）
                const options = { root: null, rootMargin: '0px', threshold: [0.25, 0.5, 0.75] };
                this.questionObserver = new IntersectionObserver((entries) => {
                    // 仍保留原逻辑作为补充，但不作为唯一可靠来源
                    let best = { ratio: 0, number: null };
                    entries.forEach(entry => {
                        const numAttr = entry.target.getAttribute('data-question-number');
                        if (!numAttr) return;
                        const num = parseInt(numAttr, 10);
                        if (entry.intersectionRatio > best.ratio) {
                            best = { ratio: entry.intersectionRatio, number: num };
                        }
                    });
                    if (best.number !== null && best.ratio >= 0.5) {
                        // 仅在非常显著可见时用 observer 更新（否则以视口中心为准）
                        this.currentQuestionNumber = best.number;
                    }
                }, options);

                // 观察所有题目节点并缓存
                this.$nextTick(() => {
                    const nodes = Array.from(document.querySelectorAll('[data-question-number]'));
                    this.questionNodes = nodes;
                    nodes.forEach(n => this.questionObserver.observe(n));
                    // 启动滚动监听，初始化一次当前题号
                    this.bindScrollListener();
                    this.scheduleUpdateCurrentQuestion();
                });
            },
        },
        mounted() {
            // 获取试卷数据
            if (window.examApi && typeof examApi.generateExamPaper === 'function') {
                examApi.generateExamPaper(25).then(data => {
                    // 假设后端返回一维题目数组，前端分组
                    if (Array.isArray(data)) {
                        // 按 type 分组
                        const typeMap = {
                            1: '干员调配与特性化决策',
                            2: '空间部署与极致化战术',
                            3: '效能审计与生态位界定',
                            4: '横向分析与竞争力评估',
                            5: '作战环境与档案类记录'
                        };
                        const group = {};
                        for (const q of data) {
                            if (!group[q.type]) group[q.type] = [];
                            group[q.type].push({
                                ...q,
                                typeName: typeMap[q.type] || '未知类型',
                                difficultyText: ['常识','基操','娴熟','明智','深邃'][q.difficulty-1] || '未知',
                                userAnswer: null
                            });
                        }
                        this.examPaper = Object.keys(group).sort().map(type => ({
                            typeName: typeMap[type] || '未知类型',
                            questions: group[type]
                        }));
                        // 在 DOM 更新后初始化观察器，确保题目元素已渲染
                        this.$nextTick(() => {
                            this.initQuestionObserver();
                        });
                    }
                });
            }

            // 启动倒计时
            this.timer = setInterval(() => {
                if (this.remainingTime > 0 && !this.showResult) {
                    this.remainingTime--;
                    this.elapsedTime++;
                } else if (this.remainingTime === 0 && !this.showResult) {
                    this.submitExam();
                }
            }, 1000);
        },
        beforeDestroy() {
            if (this.timer) clearInterval(this.timer);
            // 断开 IntersectionObserver
            if (this.questionObserver) {
                this.questionObserver.disconnect();
                this.questionObserver = null;
            }
            // 解绑滚动监听
            this.unbindScrollListener();
        }
    });
});
