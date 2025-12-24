package com.hourai.prts.data;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;

public class DataImporterTest {
    public static void main(String[] args) {
        String url = "jdbc:mysql://localhost:3306/p.r.t.s?useSSL=false&serverTimezone=UTC";
        String user = "root";
        String password = "p.r.t.s.data115";
        try (Connection conn = DriverManager.getConnection(url, user, password)) {
            DataImporter importer = new DataImporter(conn);
            importer.importUsers(new File("data/users.csv"));
            importer.importExamRecords(new File("data/exam_records.csv"));
            importer.importQuestions(new File("data/questions.csv"));
            importer.importUserAnswers(new File("data/user_answers.csv"));
            System.out.println("数据导入成功！");
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("数据导入失败！");
        }
    }
}
