window._appComputed = {
    // 是否有上一题
    hasPrevQuestion() {
        if (this.questionMode === 'practice') {
            return this.currentQuestionIndex > 0;
        } else if (this.questionMode === 'random') {
            return this.randomCurrentIndex > 0;
        } else if (this.questionMode === 'jump') {
            return this.currentQuestion && this.currentQuestion.id > 1;
        } else if (this.questionMode === 'training') {
            const prevId = this.getPrevTrainingQuestion();
            return prevId !== null;
        } else if (this.questionMode === 'wrong') {
            const prevId = this.getPrevWrongQuestion();
            return prevId !== null;
        }
        return false;
    },

    // 是否有下一题
    hasNextQuestion() {
        if (this.questionMode === 'practice') {
            return this.currentQuestionIndex < this.rawQuestions.length - 1;
        } else if (this.questionMode === 'random') {
            return true; // 随机模式永远可以有下一题
        } else if (this.questionMode === 'jump') {
            return this.currentQuestion && this.currentQuestion.id < this.rawQuestions.length;
        } else if (this.questionMode === 'training') {
            const nextId = this.getNextTrainingQuestion();
            return nextId !== null;
        } else if (this.questionMode === 'wrong') {
            const nextId = this.getNextWrongQuestion();
            return nextId !== null;
        }
        return false;
    },

    // 答案是否正确
    isAnswerCorrect() {
        return this.selectedOption === this.currentQuestion.answer;
    },

    // 错题平均难度
    averageDifficulty() {
        if (this.wrongQuestionsDetail.length === 0) return 0;
        const sum = this.wrongQuestionsDetail.reduce((total, question) => {
            return total + (question.difficulty || 0);
        }, 0);
        return sum / this.wrongQuestionsDetail.length;
    },

    // 最易错类型
    mostWrongType() {
        if (this.wrongQuestionsDetail.length === 0) return '无';
        const typeCount = {};
        this.wrongQuestionsDetail.forEach(question => {
            const typeText = this.getTypeText(question.type);
            typeCount[typeText] = (typeCount[typeText] || 0) + 1;
        });

        let maxType = '无';
        let maxCount = 0;
        Object.entries(typeCount).forEach(([type, count]) => {
            if (count > maxCount) {
                maxType = type;
                maxCount = count;
            }
        });
        return maxType;
    }
};
