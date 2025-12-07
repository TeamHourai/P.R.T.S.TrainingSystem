package com.hourai.prts.tool;
// 数据导入工具，使用 Service 层进行数据导入，确保业务逻辑一致性。
import com.hourai.prts.data.DataStore;
import com.hourai.prts.entity.*;
import com.hourai.prts.service.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DataImportTool {
    public static void main(String[] args) {
        try {
            // 用户导入，直接用CSV第一列id作为数据库id
            List<User> users = DataStore.loadUsers();
            UserService userService = new UserService();
            Map<Long, User> idToUser = new HashMap<>();
            for (User u : users) {
                userService.register(u); // register会用u.getId()作为主键
                idToUser.put(u.getId(), u);
            }
            System.out.println("用户导入完成: " + users.size());

            // 题目导入，直接用CSV第一列id作为数据库id
            List<Question> questions = DataStore.loadQuestions();
            QuestionService questionService = new QuestionService();
            for (Question q : questions) {
                questionService.addQuestion(q); // addQuestion会用q.getId()作为主键
            }
            System.out.println("题目导入完成: " + questions.size());

            // 答题记录导入，直接用CSV第一列id作为数据库id
            List<UserAnswer> uas = DataStore.loadUserAnswers();
            UserAnswerService uaService = new UserAnswerService();
            for (UserAnswer ua : uas) {
                uaService.addUserAnswer(ua); // addUserAnswer会用ua.getId()作为主键
            }
            System.out.println("答题记录导入完成: " + uas.size());

            // 考试记录导入，直接用CSV第一列id作为数据库id
            List<ExamRecord> ers = DataStore.loadExamRecords();
            ExamRecordService erService = new ExamRecordService();
            for (ExamRecord er : ers) {
                erService.addExamRecord(er); // addExamRecord会用er.getId()作为主键
            }
            System.out.println("考试记录导入完成: " + ers.size());

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("导入失败: " + e.getMessage());
        }
    }
}
