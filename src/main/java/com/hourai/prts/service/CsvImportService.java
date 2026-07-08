package com.hourai.prts.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;

@Service
public class CsvImportService implements CommandLineRunner {
    private static final Logger log = LoggerFactory.getLogger(CsvImportService.class);

    private final JdbcTemplate jdbc;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.data.import-on-startup:true}")
    private boolean importOnStartup;

    @Value("${app.data.csv-path:data/}")
    private String csvPath;

    @Value("${app.data.force-import:false}")
    private boolean forceImport;

    public CsvImportService(JdbcTemplate jdbc, PasswordEncoder passwordEncoder) {
        this.jdbc = jdbc;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (!importOnStartup) {
            log.info("CSV import disabled");
            return;
        }

        Integer count = jdbc.queryForObject("SELECT COUNT(*) FROM users", Integer.class);
        if (count != null && count > 0 && !forceImport) {
            log.info("Database already has data ({} users), skipping CSV import. Set app.data.force-import=true to force.", count);
            return;
        }

        if (forceImport) {
            log.info("Force import enabled, clearing existing data...");
            jdbc.execute("SET FOREIGN_KEY_CHECKS = 0");
            for (String table : new String[]{"exam_detail", "user_answers", "exam_records",
                    "wrong_visibility", "notifications_state", "answer_settings", "training_records",
                    "questions_onboarding", "questions", "announcements", "users"}) {
                jdbc.execute("TRUNCATE TABLE " + table);
            }
            jdbc.execute("SET FOREIGN_KEY_CHECKS = 1");
        }

        log.info("Starting CSV data import from path: {}", csvPath);
        try {
            Path base = Paths.get(csvPath);
            importUsers(base.resolve("users.csv"));
            importQuestions(base.resolve("questions.csv"));
            importOnboarding(base.resolve("questions_onboarding.csv"));
            importExamRecords(base.resolve("exam_records.csv"));
            importUserAnswers(base.resolve("user_answers.csv"));
            importAnnouncements(base.resolve("announcements.csv"));
            importWrongVisibility(base.resolve("wrong_visibility.csv"));
            importNotificationStates(base.resolve("notifications_state.csv"));
            importAnswerSettings(base.resolve("answer_settings.csv"));
            importTrainingRecords(base.resolve("training_records.csv"));
            log.info("CSV data import completed successfully");
        } catch (Exception e) {
            log.error("CSV import failed: {}", e.getMessage(), e);
        }
    }

    private void importUsers(Path file) throws Exception {
        if (!Files.exists(file)) return;
        List<String> lines = Files.readAllLines(file, StandardCharsets.UTF_8);
        int count = 0;
        for (String line : lines) {
            line = cleanLine(line);
            if (line.isEmpty()) continue;
            String[] p = parseLine(line);
            if (p.length < 4) continue;
            try {
                jdbc.update("INSERT INTO users (id, username, password, is_admin, status, register_time, created_at) VALUES (?,?,?,?,1,?,NOW())",
                        Long.parseLong(p[0]), p[1], passwordEncoder.encode(p[2]),
                        "true".equalsIgnoreCase(p[3]),
                        p.length >= 5 ? toTimestamp(p[4]) : null);
                count++;
            } catch (Exception e) { log.warn("Skip user {}: {}", p.length > 0 ? p[0] : "?", e.getMessage()); }
        }
        log.info("Imported {} users", count);
    }

    private void importQuestions(Path file) throws Exception {
        if (!Files.exists(file)) return;
        List<String> lines = Files.readAllLines(file, StandardCharsets.UTF_8);
        int count = 0;
        for (String line : lines) {
            line = cleanLine(line);
            if (line.isEmpty()) continue;
            String[] p = parseLine(line);
            if (p.length < 9) continue;
            try {
                jdbc.update("INSERT INTO questions (id, type, difficulty, resource, question, has_picture, options, answer, analysis, keywords, created_at) VALUES (?,?,?,?,?,?,?,?,?,?,NOW())",
                        Long.parseLong(p[0]), Integer.parseInt(p[1]), Integer.parseInt(p[2]),
                        p[3], p[4], "1".equals(p[5]),
                        p[6].replace("¦", "|"), p[7], p.length >= 9 ? p[8] : "",
                        p.length >= 10 ? p[9].replace("¦", "|") : "");
                count++;
            } catch (Exception e) { log.warn("Skip question {}: {}", p.length > 0 ? p[0] : "?", e.getMessage()); }
        }
        log.info("Imported {} questions", count);
    }

    private void importOnboarding(Path file) throws Exception {
        if (!Files.exists(file)) return;
        List<String> lines = Files.readAllLines(file, StandardCharsets.UTF_8);
        int count = 0;
        for (String line : lines) {
            line = cleanLine(line);
            if (line.isEmpty()) continue;
            String[] p = parseLine(line);
            if (p.length < 8) continue;
            try {
                jdbc.update("INSERT INTO questions_onboarding (id, group_id, type_id, image_url, question, is_multi, options, answer, analysis) VALUES (?,?,?,?,?,?,?,?,?)",
                        Integer.parseInt(p[0]),
                        p[1].isEmpty() ? null : Integer.parseInt(p[1]),
                        p[2].isEmpty() ? null : Integer.parseInt(p[2]),
                        p[3].isEmpty() ? null : p[3], p[4],
                        "1".equals(p[5]), p[6].replace("¦", "|"), p[7],
                        p.length >= 9 ? p[8] : "");
                count++;
            } catch (Exception e) { log.warn("Skip onboarding {}: {}", p[0], e.getMessage()); }
        }
        log.info("Imported {} onboarding questions", count);
    }

    private void importExamRecords(Path file) throws Exception {
        if (!Files.exists(file)) return;
        List<String> lines = Files.readAllLines(file, StandardCharsets.UTF_8);
        int count = 0;
        for (String line : lines) {
            line = cleanLine(line);
            if (line.isEmpty()) continue;
            String[] p = parseLine(line);
            if (p.length < 4) continue;
            try {
                jdbc.update("INSERT INTO exam_records (id, user_id, score, created_at) VALUES (?,?,?,?)",
                        Long.parseLong(p[0]), Long.parseLong(p[1]), p[2],
                        p.length >= 4 && !p[3].isEmpty() ? toTimestamp(p[3]) : null);
                count++;
            } catch (Exception e) { log.warn("Skip exam_record {}: {}", p[0], e.getMessage()); }
        }
        log.info("Imported {} exam records", count);
    }

    private void importUserAnswers(Path file) throws Exception {
        if (!Files.exists(file)) return;
        List<String> lines = Files.readAllLines(file, StandardCharsets.UTF_8);
        int count = 0;
        for (String line : lines) {
            line = cleanLine(line);
            if (line.isEmpty()) continue;
            String[] p = parseLine(line);
            if (p.length < 7) continue;
            try {
                jdbc.update("INSERT INTO user_answers (id, user_id, question_id, is_correct, selected_answer, created_at) VALUES (?,?,?,?,?,?)",
                        Long.parseLong(p[0]), Long.parseLong(p[1]), Long.parseLong(p[2]),
                        "true".equalsIgnoreCase(p[4]) || "1".equals(p[4]),
                        p[5], p.length >= 7 ? toTimestamp(p[6]) : null);
                count++;
            } catch (Exception e) { log.warn("Skip user_answer {}: {}", p[0], e.getMessage()); }
        }
        log.info("Imported {} user answers", count);
    }

    private void importAnnouncements(Path file) throws Exception {
        if (!Files.exists(file)) return;
        List<String> lines = Files.readAllLines(file, StandardCharsets.UTF_8);
        int count = 0;
        for (String line : lines) {
            line = cleanLine(line);
            if (line.isEmpty()) continue;
            String[] p = parseLine(line);
            if (p.length < 6) continue;
            try {
                jdbc.update("INSERT INTO announcements (id, type, title, content, important, created_at, created_by, expires_at) VALUES (?,?,?,?,?,?,?,?)",
                        Long.parseLong(p[0]),
                        p[1].isEmpty() ? "system" : p[1], p[2], p[3],
                        "true".equalsIgnoreCase(p[4]) || "1".equals(p[4]),
                        p[5], p.length >= 7 ? p[6] : "",
                        p.length >= 8 ? p[7] : null);
                count++;
            } catch (Exception e) { log.warn("Skip announcement {}: {}", p[0], e.getMessage()); }
        }
        log.info("Imported {} announcements", count);
    }

    private void importWrongVisibility(Path file) throws Exception {
        if (!Files.exists(file)) return;
        List<String> lines = Files.readAllLines(file, StandardCharsets.UTF_8);
        int count = 0;
        for (String line : lines) {
            line = cleanLine(line);
            if (line.isEmpty()) continue;
            String[] p = parseLine(line);
            if (p.length < 5) continue;
            try {
                jdbc.update("INSERT INTO wrong_visibility (id, user_id, question_id, hidden, updated_at) VALUES (?,?,?,?,?)",
                        Long.parseLong(p[0]), Long.parseLong(p[1]), Long.parseLong(p[2]),
                        "true".equalsIgnoreCase(p[3]) || "1".equals(p[3]), p[4]);
                count++;
            } catch (Exception e) { log.warn("Skip wrong_visibility {}: {}", p[0], e.getMessage()); }
        }
        log.info("Imported {} wrong visibility records", count);
    }

    private void importNotificationStates(Path file) throws Exception {
        if (!Files.exists(file)) return;
        List<String> lines = Files.readAllLines(file, StandardCharsets.UTF_8);
        int count = 0;
        for (String line : lines) {
            line = cleanLine(line);
            if (line.isEmpty()) continue;
            String[] p = parseLine(line);
            if (p.length < 4) continue;
            try {
                jdbc.update("INSERT INTO notifications_state (notification_id, user_id, is_read, is_hidden) VALUES (?,?,?,?)",
                        Long.parseLong(p[0]), Long.parseLong(p[1]),
                        "true".equalsIgnoreCase(p[2]) || "1".equals(p[2]),
                        p.length >= 4 && ("true".equalsIgnoreCase(p[3]) || "1".equals(p[3])));
                count++;
            } catch (Exception e) { log.warn("Skip notification_state: {}", e.getMessage()); }
        }
        log.info("Imported {} notification states", count);
    }

    private void importAnswerSettings(Path file) throws Exception {
        if (!Files.exists(file)) return;
        List<String> lines = Files.readAllLines(file, StandardCharsets.UTF_8);
        int count = 0;
        for (String line : lines) {
            line = cleanLine(line);
            if (line.isEmpty()) continue;
            String[] p = parseLine(line);
            if (p.length < 3) continue;
            try {
                jdbc.update("INSERT INTO answer_settings (user_id, auto_submit, auto_next_correct, updated_at) VALUES (?,?,?,?)",
                        Long.parseLong(p[0]),
                        "true".equalsIgnoreCase(p[1]) || "1".equals(p[1]),
                        "true".equalsIgnoreCase(p[2]) || "1".equals(p[2]),
                        p.length >= 4 && !p[3].isEmpty() ? toTimestamp(p[3]) : null);
                count++;
            } catch (Exception e) { log.warn("Skip answer_settings: {}", e.getMessage()); }
        }
        log.info("Imported {} answer settings", count);
    }

    private void importTrainingRecords(Path file) throws Exception {
        if (!Files.exists(file)) return;
        List<String> lines = Files.readAllLines(file, StandardCharsets.UTF_8);
        int count = 0;
        for (String line : lines) {
            line = cleanLine(line);
            if (line.isEmpty()) continue;
            String[] p = parseLine(line);
            if (p.length < 5) continue;
            try {
                jdbc.update("INSERT INTO training_records (user_id, question_id, attempts, correct, last_at) VALUES (?,?,?,?,?)",
                        Long.parseLong(p[0]), Long.parseLong(p[1]), Integer.parseInt(p[2]),
                        "true".equalsIgnoreCase(p[3]) || "1".equals(p[3]),
                        Long.parseLong(p[4]));
                count++;
            } catch (Exception e) { log.warn("Skip training_record: {}", e.getMessage()); }
        }
        log.info("Imported {} training records", count);
    }

    private String cleanLine(String line) {
        if (line == null) return "";
        return line.replace("﻿", "").trim();
    }

    private String[] parseLine(String line) {
        List<String> fields = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        boolean inQuotes = false;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '"') { inQuotes = !inQuotes; }
            else if (c == ',' && !inQuotes) { fields.add(sb.toString()); sb = new StringBuilder(); }
            else { sb.append(c); }
        }
        fields.add(sb.toString());
        return fields.toArray(new String[0]);
    }

    private java.sql.Timestamp toTimestamp(String s) {
        try {
            s = s.trim();
            if (s.contains(".")) s = s.substring(0, s.indexOf('.'));
            return java.sql.Timestamp.valueOf(s);
        } catch (Exception e) { return null; }
    }
}
