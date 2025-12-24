package com.hourai.prts.data;

import com.hourai.prts.entity.*;
import com.hourai.prts.dao.*;
import java.util.List;

public class DataImportMain {
    public static void main(String[] args) {
        try {
            // 1. 先导入用户表
            List<User> users = DataStore.loadUsers();
            UserDao userDao = new UserDao();
            for (User user : users) {
                userDao.insert(user);
            }

            // 2. 再导入试题表（如有 user_id 字段则过滤无效 user_id）
            List<Question> questions = DataStore.loadQuestions();
            QuestionDao questionDao = new QuestionDao();
            for (Question question : questions) {
                // 如果 Question 有 userId 字段且需要外键约束，可加如下判断：
                // if (question.getUserId() == null || validUserIds.contains(question.getUserId())) {
                //     questionDao.insert(question);
                // }
                // 否则直接插入：
                questionDao.insert(question);
            }

            // 3. 再导入考试记录表（过滤无效 user_id）
            List<ExamRecord> examRecords = DataStore.loadExamRecords();
            ExamRecordDao examRecordDao = new ExamRecordDao();
            java.util.Set<Long> validUserIds = new java.util.HashSet<>();
            for (User user : users) {
                validUserIds.add(user.getId());
            }
            for (ExamRecord record : examRecords) {
                if (validUserIds.contains(record.getUserId())) {
                    examRecordDao.insert(record);
                }
            }

            // 4. 最后导入用户答题表（过滤无效 user_id）
            List<UserAnswer> userAnswers = DataStore.loadUserAnswers();
            UserAnswerDao userAnswerDao = new UserAnswerDao();
            for (UserAnswer answer : userAnswers) {
                if (validUserIds.contains(answer.getUserId())) {
                    userAnswerDao.insert(answer);
                }
            }

            System.out.println("全部数据导入完成！");
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("导入失败！");
        }
    }
}
