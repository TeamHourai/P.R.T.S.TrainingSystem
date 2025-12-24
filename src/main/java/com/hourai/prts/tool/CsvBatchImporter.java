package com.hourai.prts.tool;

import java.io.*;
import java.sql.*;
import java.util.*;

public class CsvBatchImporter {
    // 配置数据库连接
    static final String url = "jdbc:mysql://localhost:3306/p.r.t.s?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";
    static final String user = "root";
    static final String password = "p.r.t.s.data115";

    public static void main(String[] args) throws Exception {
        // 需要导入的表和csv文件名
        Map<String, String[]> tableMap = new LinkedHashMap<>();
        tableMap.put("announcements", new String[]{"data/announcements.csv", "id,type,title,content,visible,create_time,creator,publish_time"});
        tableMap.put("exam_records", new String[]{"data/exam_records.csv", "id,user_id,exam_id,submit_time"});
        tableMap.put("notifications_state", new String[]{"data/notifications_state.csv", "id,user_id,read,deleted"});
        tableMap.put("questions_onboarding", new String[]{"data/questions_onboarding.csv", "id,group_id,type_id,image_url,question,is_multi,options,answer,analysis"});
        tableMap.put("questions", new String[]{"data/questions.csv", "id,group_id,type_id,title,question,is_multi,options,answer,analysis"});
        tableMap.put("user_answers", new String[]{"data/user_answers.csv", "id,user_id,question_id,mode,correct,answer,answer_time"});
        tableMap.put("wrong_visibility", new String[]{"data/wrong_visibility.csv", "id,user_id,question_id,visible,update_time"});
        tableMap.put("users", new String[]{"data/users.csv", "id,username,password,is_admin,register_time"});

        try (Connection conn = DriverManager.getConnection(url, user, password)) {
            for (Map.Entry<String, String[]> entry : tableMap.entrySet()) {
                String table = entry.getKey();
                String csvFile = entry.getValue()[0];
                String[] fields = entry.getValue()[1].split(",");
                importCsv(conn, table, csvFile, fields);
            }
        }
        System.out.println("全部导入完成！");
    }

    private static void importCsv(Connection conn, String table, String csvFile, String[] fields) throws Exception {
        File file = new File(csvFile);
        if (!file.exists()) {
            System.out.println(csvFile + " 不存在，跳过。");
            return;
        }
        StringBuilder sql = new StringBuilder("INSERT INTO " + table + " (");
        sql.append(String.join(",", fields)).append(") VALUES (");
        sql.append("?,".repeat(fields.length));
        sql.setLength(sql.length() - 1);
        sql.append(")");
        try (BufferedReader br = new BufferedReader(new FileReader(file));
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            String line;
            br.readLine(); // 跳过表头
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue; // 跳过空行
                String[] arr = line.split(",", -1);
                if (arr.length < fields.length) {
                    System.out.println("跳过字段数不足的行: " + line);
                    continue;
                }
                for (int i = 0; i < fields.length; i++) {
                    String val = arr[i].trim();
                    if (val.isEmpty()) {
                        ps.setObject(i + 1, null);
                    } else if (fields[i].endsWith("_time") || fields[i].endsWith("date")) {
                        ps.setTimestamp(i + 1, Timestamp.valueOf(val.replace('T', ' ')));
                    } else if (fields[i].equalsIgnoreCase("visible") || fields[i].equalsIgnoreCase("is_admin") || fields[i].equalsIgnoreCase("read") || fields[i].equalsIgnoreCase("deleted") || fields[i].equalsIgnoreCase("correct")) {
                        ps.setBoolean(i + 1, "1".equals(val) || "true".equalsIgnoreCase(val));
                    } else if (fields[i].endsWith("id")) {
                        ps.setLong(i + 1, Long.parseLong(val));
                    } else {
                        ps.setString(i + 1, val);
                    }
                }
                ps.executeUpdate();
            }
            System.out.println(table + " 导入完成");
        }
    }
}
