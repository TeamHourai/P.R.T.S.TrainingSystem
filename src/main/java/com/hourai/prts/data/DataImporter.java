package com.hourai.prts.data;

import java.io.*;
import java.sql.*;
import java.util.*;

public class DataImporter {
    private final Connection conn;

    public DataImporter(Connection conn) {
        this.conn = conn;
    }

    public void importExamRecords(File csvFile) throws Exception {
        try (BufferedReader reader = new BufferedReader(new FileReader(csvFile))) {
            String line;
            String sql = "INSERT INTO exam_records (user_id, exam_id, score, submit_time) VALUES (?, ?, ?, ?)";
            PreparedStatement ps = conn.prepareStatement(sql);
            while ((line = reader.readLine()) != null) {
                String[] arr = line.split(",");
                ps.setInt(1, Integer.parseInt(arr[0]));
                ps.setInt(2, Integer.parseInt(arr[1]));
                ps.setInt(3, Integer.parseInt(arr[2]));
                ps.setString(4, arr[3]);
                ps.addBatch();
            }
            ps.executeBatch();
            ps.close();
        }
    }

    public void importQuestions(File csvFile) throws Exception {
        try (BufferedReader reader = new BufferedReader(new FileReader(csvFile))) {
            String line;
            String sql = "INSERT INTO questions (id, paper_id, type, img, content, answer_type, options, answer, analysis) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement ps = conn.prepareStatement(sql);
            while ((line = reader.readLine()) != null) {
                String[] arr = line.split(",", -1);
                ps.setInt(1, Integer.parseInt(arr[0]));
                ps.setInt(2, Integer.parseInt(arr[1]));
                ps.setInt(3, Integer.parseInt(arr[2]));
                ps.setString(4, arr[3]);
                ps.setString(5, arr[4]);
                ps.setInt(6, Integer.parseInt(arr[5]));
                ps.setString(7, arr[6]);
                ps.setInt(8, Integer.parseInt(arr[7]));
                ps.setString(9, arr[8]);
                ps.addBatch();
            }
            ps.executeBatch();
            ps.close();
        }
    }

    public void importUserAnswers(File csvFile) throws Exception {
        try (BufferedReader reader = new BufferedReader(new FileReader(csvFile))) {
            String line;
            String sql = "INSERT INTO user_answers (id, exam_id, question_id, mode, correct, answer, submit_time) VALUES (?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement ps = conn.prepareStatement(sql);
            while ((line = reader.readLine()) != null) {
                String[] arr = line.split(",");
                ps.setInt(1, Integer.parseInt(arr[0]));
                ps.setInt(2, Integer.parseInt(arr[1]));
                ps.setInt(3, Integer.parseInt(arr[2]));
                ps.setString(4, arr[3]);
                ps.setBoolean(5, Boolean.parseBoolean(arr[4]));
                if (arr[5].equals("null")) {
                    ps.setNull(6, Types.INTEGER);
                } else {
                    ps.setInt(6, Integer.parseInt(arr[5]));
                }
                ps.setString(7, arr[6]);
                ps.addBatch();
            }
            ps.executeBatch();
            ps.close();
        }
    }

    public void importUsers(File csvFile) throws Exception {
        try (BufferedReader reader = new BufferedReader(new FileReader(csvFile))) {
            String line;
            String sql = "INSERT INTO users (id, username, password, is_admin, register_time) VALUES (?, ?, ?, ?, ?)";
            PreparedStatement ps = conn.prepareStatement(sql);
            while ((line = reader.readLine()) != null) {
                String[] arr = line.split(",");
                ps.setInt(1, Integer.parseInt(arr[0]));
                ps.setString(2, arr[1]);
                ps.setString(3, arr[2]);
                ps.setBoolean(4, Boolean.parseBoolean(arr[3]));
                ps.setString(5, arr[4]);
                ps.addBatch();
            }
            ps.executeBatch();
            ps.close();
        }
    }
}
