package com.hourai.prts.tool;

import com.hourai.prts.utils.Utils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

/**
 * Simple CSV -> DB importer for data/questions.csv
 * Usage: java -cp ... com.hourai.prts.tool.QuestionsImportTool [path/to/questions.csv]
 */
public class QuestionsImportTool {
    private static final Path DEFAULT = Paths.get("data/questions.csv");
    private static final String URL = "jdbc:mysql://localhost:3306/p.r.t.s?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";
    private static final String USER = "root";
    private static final String PASS = "p.r.t.s.data115";

    public static void main(String[] args) {
        Path csv = args != null && args.length > 0 ? Paths.get(args[0]) : DEFAULT;
        System.out.println("Importing questions from: " + csv.toAbsolutePath());
        try {
            List<String> lines = Files.readAllLines(csv, StandardCharsets.UTF_8);
            int imported = importLines(lines);
            System.out.println("Imported: " + imported + " rows");
        } catch (IOException e) {
            System.err.println("Failed to read CSV: " + e.getMessage());
            System.exit(2);
        } catch (SQLException e) {
            System.err.println("DB error: " + e.getMessage());
            System.exit(3);
        }
    }

    private static int importLines(List<String> lines) throws SQLException {
        if (lines == null || lines.isEmpty()) return 0;
        String sql = "INSERT INTO questions (id, type, difficulty, category, resource, question, options, answer, analysis, has_picture, picture_url, view_count, error_count, created_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP)";
        int count = 0;
        try (Connection conn = DriverManager.getConnection(URL, USER, PASS);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            for (String ln : lines) {
                if (ln == null) continue;
                ln = ln.trim();
                if (ln.isEmpty()) continue;
                try {
                    String[] p = ln.split(",", 10);
                    if (p.length < 9) continue;
                    long id = Long.parseLong(p[0].trim());
                    int type = Integer.parseInt(p[1].trim());
                    int difficulty = Integer.parseInt(p[2].trim());
                    String resource = Utils.unescapeCsv(p[3]);
                    String question = Utils.unescapeCsv(p[4]);
                    boolean hasPicture = !"0".equals(p[5]);
                    String optionsRaw = Utils.unescapeCsv(p[6]);
                    int answer = Integer.parseInt(p[7].trim());
                    String analysis = Utils.unescapeCsv(p[8]);
                    // keywords ignored for DB import (can be stored elsewhere)

                    ps.setLong(1, id);
                    ps.setInt(2, type);
                    ps.setInt(3, difficulty);
                    ps.setString(4, null);
                    ps.setString(5, resource);
                    ps.setString(6, question);
                    ps.setString(7, optionsRaw);
                    ps.setString(8, String.valueOf(answer));
                    ps.setString(9, analysis);
                    ps.setBoolean(10, hasPicture);
                    ps.setString(11, null);
                    ps.setInt(12, 0);
                    ps.setInt(13, 0);
                    try {
                        ps.executeUpdate();
                        count++;
                    } catch (SQLException ex) {
                        // if duplicate key or other error, skip but log
                        System.err.println("Skipped id=" + id + " due to: " + ex.getMessage());
                    }
                } catch (Exception exRow) {
                    System.err.println("Failed to parse line (skipped): " + ln);
                }
            }
        }
        return count;
    }
}
