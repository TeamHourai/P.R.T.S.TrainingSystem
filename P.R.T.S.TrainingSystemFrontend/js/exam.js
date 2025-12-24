// exam.js
// 负责考试页面的主要逻辑，包括题目加载、答题、提交、结果显示等

new Vue({
    el: '#app',
    data: {
        examPaper: [], // 题目结构
        currentQuestionNumber: 1,
        showResult: false,
        totalScore: 0,
        maxScore: 100,
        elapsedTime: 0,
        remainingTime: 3600, // 例：60分钟
        timer: null,
        answeredCount: 0,
        mobileSheetOpen: false,
        showQuestionDetailModal: false,
        currentDetailQuestion: {},
    },
    computed: {
        formattedTime() {
            const min = Math.floor(this.remainingTime / 60).toString().padStart(2, '0');
            const sec = (this.remainingTime % 60).toString().padStart(2, '0');
            return `${min}:${sec}`;
        }
    },
    created() {
        this.loadExamPaper();
        this.startTimer();
    },
    methods: {
        loadExamPaper() {
            // 假设 window.questionsData 由 questions.js 提供
            if (window.questionsData) {
                this.examPaper = JSON.parse(JSON.stringify(window.questionsData));
                this.maxScore = this.examPaper.length * 20; // 假设每部分20分
            }
        },
        selectOption(questionId, optionIndex) {
            for (const section of this.examPaper) {
                for (const q of section.questions) {
                    if (q.id === questionId) {
                        q.userAnswer = optionIndex;
                        this.updateAnsweredCount();
                        return;
                    }
                }
            }
        },
        updateAnsweredCount() {
            let count = 0;
            for (const section of this.examPaper) {
                for (const q of section.questions) {
                    if (q.userAnswer) count++;
                }
            }
            this.answeredCount = count;
        },
        submitExam() {
            this.showResult = true;
            this.calculateTotalScore();
            clearInterval(this.timer);
        },
        calculateTotalScore() {
            let score = 0;
            for (const section of this.examPaper) {
                score += this.calculateSectionScore(section);
            }
            this.totalScore = score;
        },
        calculateSectionScore(section) {
            let sectionScore = 0;
            for (const q of section.questions) {
                if (q.userAnswer === q.answer) sectionScore += Math.floor(20 / section.questions.length);
            }
            return sectionScore;
        },
        isQuestionCorrect(q) {
            return q.userAnswer === q.answer;
        },
        getQuestionNumber(sectionIndex, qIndex) {
            let num = 1;
            for (let i = 0; i < sectionIndex; i++) {
                num += this.examPaper[i].questions.length;
            }
            return num + qIndex;
        },
        scrollToQuestion(sectionIndex, qIndex) {
            // 可选：实现平滑滚动到指定题目
        },
        formatTime(sec) {
            const min = Math.floor(sec / 60).toString().padStart(2, '0');
            const s = (sec % 60).toString().padStart(2, '0');
            return `${min}:${s}`;
        },
        startTimer() {
            this.timer = setInterval(() => {
                this.elapsedTime++;
                this.remainingTime--;
                if (this.remainingTime <= 0) {
                    clearInterval(this.timer);
                    this.submitExam();
                }
            }, 1000);
        },
        toggleMobileSheet() {
            this.mobileSheetOpen = !this.mobileSheetOpen;
        },
        showQuestionDetail(q, sectionIndex, qIndex) {
            this.currentDetailQuestion = q;
            this.showQuestionDetailModal = true;
        },
        closeQuestionDetail() {
            this.showQuestionDetailModal = false;
        },
        restartExam() {
            window.location.reload();
        },
        goBack() {
            window.location.href = 'index.html';
        }
    }
});
