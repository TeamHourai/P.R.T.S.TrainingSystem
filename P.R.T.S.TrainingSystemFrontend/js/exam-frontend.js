// exam-frontend.js
// 挂载全真模拟页面的 Vue 实例，实现数据获取与交互

// 依赖 window.examApi

document.addEventListener('DOMContentLoaded', function () {
    new Vue({
        el: '#app',
        data: {
            paperId: null, // 服务端试卷快照 ID，决定交卷时的完整题目集合
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
            async submitExam() {
                // 先展示提交状态；最终分数和答案以后端判分结果为准。
                this.showResult = true;
                this.totalScore = 0;

                // 如果后端支持提交（用于回填正确答案/解析/记录），则提交一次。
                // 不阻塞 UI：失败也不影响本地展示。
                try {
                    if (window.examApi && typeof examApi.submitExamAnswers === 'function') {
                        // collect answers: {questionId: userAnswer}
                        const answers = {};
                        for (const sec of this.examPaper) {
                            for (const q of sec.questions) {
                                if (q && q.id != null && q.userAnswer != null) {
                                    answers[q.id] = q.userAnswer;
                                }
                            }
                        }
                        // 用户身份由后端从 JWT 中获取，客户端不再提交 userId。
                        const res = await examApi.submitExamAnswers(
                            this.paperId, answers, this.elapsedTime);
                        // 若后端返回了纠正后的题目数据（例如 correctAnswer/analysis），可在这里合并回 examPaper
                        // 保持兼容：仅在字段存在时覆盖
                        if (res && Array.isArray(res.questions)) {
                            const byId = new Map(res.questions.map(x => [x.id, x]));
                            for (const sec of this.examPaper) {
                                for (const q of sec.questions) {
                                    const serverQ = byId.get(q.id);
                                    if (!serverQ) continue;
                                    // merge common fields
                                    if (serverQ.answer !== undefined) q.answer = serverQ.answer;
                                    if (serverQ.analysis !== undefined) q.analysis = serverQ.analysis;
                                    if (serverQ.options !== undefined) q.options = serverQ.options;
                                    if (serverQ.question !== undefined) q.question = serverQ.question;
                                    if (serverQ.picture !== undefined) q.picture = serverQ.picture;
                                    if (serverQ.difficulty !== undefined) q.difficulty = serverQ.difficulty;
                                    if (serverQ.type !== undefined) q.type = serverQ.type;
                                }
                            }
                        }
                        if (res && res.score !== undefined) {
                            this.totalScore = Number(res.score) || 0;
                        }
                    }
                } catch (e) {
                    console.warn('提交考试到后端失败（已忽略，不影响前端结果显示）', e);
                }
            },

            // 查看题目详情：如果当前题缺少详情（尤其 analysis/options/question），则从后端按 id 拉取再展示
            async showQuestionDetail(q, sectionIndex, qIndex) {
                const section = this.examPaper[sectionIndex] || {};

                // 若本地没有解析/选项/题干，尝试从后端 /api/v1/questions/{id} 拉取完整详情
                try {
                    const needsFetch = !q || !q.id || !q.question || !q.options || (Array.isArray(q.options) && q.options.length === 0) || (q.analysis == null || q.analysis === '');
                    if (needsFetch && window.questionApi && typeof questionApi.getQuestionById === 'function') {
                        const full = await questionApi.getQuestionById(q.id);
                        if (full) {
                            // 合并回原对象，保证后续再次点不用再请求
                            Object.assign(q, full);
                        }
                    } else if (needsFetch) {
                        // fallback：直接 fetch（兼容统一响应体 {code,message,data,success}）
                        const base = (window.API_BASE_URL || 'http://localhost:8080').replace(/\/+$/, '');
                        const resp = await fetch(base + '/api/v1/questions/' + encodeURIComponent(q.id));
                        if (resp.ok) {
                            const payload = await resp.json();
                            const full = (payload && payload.data !== undefined) ? payload.data : payload;
                            if (full) Object.assign(q, full);
                        }
                    }
                } catch (e) {
                    console.warn('拉取题目详情失败（将用本地数据展示）', e);
                }

                const rawAnswer = (q.answer !== undefined && q.answer !== null) ? q.answer : (q.correct !== undefined ? q.correct : null);
                const rawUser = (q.userAnswer !== undefined && q.userAnswer !== null) ? q.userAnswer : null;
                const answerNorm = this.normalizeToNumber(rawAnswer);
                const userNorm = this.normalizeToNumber(rawUser);

                this.currentDetailQuestion = {
                    id: q.id,
                    question: this.fmtHtmlText(q.question || ''),
                    options: Array.isArray(q.options)
                        ? q.options.map(opt => this.fmtHtmlText(opt))
                        : (q.options ? String(q.options).split('|').map(s => this.fmtHtmlText(s)) : []),
                    answer: answerNorm || this.normalizeToNumber(q.answer) || 0,
                    userAnswer: userNorm || this.normalizeToNumber(rawUser) || null,
                    analysis: this.fmtHtmlText(q.analysis || q.explain || '暂无解析'),
                    picture: q.picture || q.image || false,
                    typeText: section.typeName || q.typeName || q.type || '未知类型',
                    difficultyText: q.difficultyText || (typeof q.difficulty === 'number' ? ['常识','基操','娴熟','明智','深邃'][q.difficulty - 1] : q.difficulty) || '未知'
                };

                this.$nextTick(() => {
                    this.showQuestionDetailModal = true;
                    document.body.style.overflow = 'hidden';
                });
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


            // 图片加载失败时隐藏图片，避免控制台大量 404 + Vue 报错
            onQuestionImageError(question) {
                try {
                    if (question) question.picture = false;
                } catch (e) { /* ignore */ }
            },

            onDetailImageError() {
                try {
                    if (this.currentDetailQuestion) this.currentDetailQuestion.picture = false;
                } catch (e) { /* ignore */ }
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

            restartExam() {
                // 已提交的 paperId 不能重复使用，刷新后向服务端申请新试卷。
                window.location.reload();
            },

            goBack() {
                window.location.href = 'index.html';
            },
            // 文本格式化：支持真实换行与转义换行(\\n / \\r\\n)，并用于 v-html 渲染
            fmtHtmlText(str) {
                return (str || '')
                    .replace(/\\r\\n/g, '\n')
                    .replace(/\\n/g, '\n')
                    .replace(/\r\n/g, '\n')
                    .replace(/\n/g, '<br>');
            },
        },
        mounted() {
            // 获取试卷数据
            if (window.examApi && typeof examApi.generateExamPaper === 'function') {
                examApi.generateExamPaper().then(data => {
                    const questions = data && Array.isArray(data.questions) ? data.questions : [];
                    this.paperId = data ? data.paperId : null;
                    if (questions.length > 0 && this.paperId != null) {
                        // 按 type 分组
                        const typeMap = {
                            1: '干员调配与特性化决策',
                            2: '空间部署与极致化战术',
                            3: '效能审计与生态位界定',
                            4: '横向分析与竞争力评估',
                            5: '作战环境与档案类记录'
                        };
                        const group = {};
                        for (const q of questions) {
                            if (!group[q.type]) group[q.type] = [];
                            group[q.type].push({
                                ...q,
                                // 题干/选项/解析在页面上用 v-html 显示，因此这里提前把换行统一成 <br>
                                question: this.fmtHtmlText(q.question),
                                options: Array.isArray(q.options) ? q.options.map(opt => this.fmtHtmlText(opt)) : q.options,
                                analysis: this.fmtHtmlText(q.analysis),
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
