package com.hourai.prts.data;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;

import com.hourai.prts.utils.Utils;
import com.hourai.prts.entity.*;

/*
  简单文件数据存取：CSV 风格，线程安全的同步方法
  Files created under ./data/
*/
public class DataStore {
    static final Path DATA_DIR = Paths.get("data");
    static final Path USERS_FILE = DATA_DIR.resolve("users.csv");
    public static final Path QUESTIONS_FILE = DATA_DIR.resolve("questions.csv");
    static final Path ONBOARDING_QUESTIONS_FILE = DATA_DIR.resolve("questions_onboarding.csv");
    static final Path USER_ANSWERS_FILE = DATA_DIR.resolve("user_answers.csv");
    static final Path EXAM_RECORDS_FILE = DATA_DIR.resolve("exam_records.csv");
    static final Path WRONG_VISIBILITY_FILE = DATA_DIR.resolve("wrong_visibility.csv");
    static final Path ANNOUNCEMENTS_FILE = DATA_DIR.resolve("announcements.csv");
    // 新增：用户答题设置
    static final Path ANSWER_SETTINGS_FILE = DATA_DIR.resolve("answer_settings.csv");
    // 新增：入职培训答题记录（按用户维度持久化）
    static final Path TRAINING_RECORDS_FILE = DATA_DIR.resolve("training_records.csv");

    /**
     * Expose the default questions CSV path for handlers that need direct file access.
     */
    public static Path getQuestionsFile() {
        return QUESTIONS_FILE;
    }

    public static Path getWrongVisibilityFile() {
        return WRONG_VISIBILITY_FILE;
    }

    public static Path getAnnouncementsFile() {
        return ANNOUNCEMENTS_FILE;
    }

    /* 方法ensureDataFiles：
     * 确保数据目录和文件存在，如不存在则创建并写入初始数据
     */
    public static void ensureDataFiles() throws IOException {
        if (!Files.exists(DATA_DIR)) Files.createDirectories(DATA_DIR);
        if (!Files.exists(USERS_FILE)) {
            List<String> lines = new ArrayList<>();
            lines.add("1,admin,admin,true," + Utils.now());
            lines.add("2,student1,password,false," + Utils.now());
            Files.write(USERS_FILE, lines, StandardCharsets.UTF_8, StandardOpenOption.CREATE);
        }
        if (!Files.exists(QUESTIONS_FILE)) {
            List<String> qlines = new ArrayList<>();
            qlines.add(Utils.csvQ(1, 1, 2, "", "以下哪个是 Java 的关键字？", false, "function|class|static|define", 3, "static 是关键字"));
            qlines.add(Utils.csvQ(2, 2, 1, "", "2 + 2 = ?", false, "3|4|5|22", 2, "2+2=4"));
            for (int i = 3; i <= 12; i++) {
                qlines.add(Utils.csvQ(i, 1, 1, "", "示例题：" + (i - 2), false, "A|B|C|D", (i % 4 == 0 ? 4 : i % 4), "示例解析"));
            }
            Files.write(QUESTIONS_FILE, qlines, StandardCharsets.UTF_8, StandardOpenOption.CREATE);
        }
        // ensure onboarding file exists (initial content can mirror QUESTIONS_FILE or be left empty)
        if (!Files.exists(ONBOARDING_QUESTIONS_FILE)) {
            List<String> qlines = new ArrayList<>();
            qlines.add(Utils.csvQ(1, 1, 2, "", "以下哪个是 Java 的关键字？", false, "function|class|static|define", 3, "static 是关键字"));
            qlines.add(Utils.csvQ(2, 2, 1, "", "2 + 2 = ?", false, "3|4|5|22", 2, "2+2=4"));
            for (int i = 3; i <= 12; i++) {
                qlines.add(Utils.csvQ(i, 1, 1, "", "示例题：" + (i - 2), false, "A|B|C|D", (i % 4 == 0 ? 4 : i % 4), "示例解析"));
            }
            Files.write(ONBOARDING_QUESTIONS_FILE, qlines, StandardCharsets.UTF_8, StandardOpenOption.CREATE);
        }
        if (!Files.exists(USER_ANSWERS_FILE)) {
            Files.createFile(USER_ANSWERS_FILE);
        }
        if (!Files.exists(EXAM_RECORDS_FILE)) {
            Files.createFile(EXAM_RECORDS_FILE);
        }
        if (!Files.exists(WRONG_VISIBILITY_FILE)) {
            Files.createFile(WRONG_VISIBILITY_FILE);
        }
        if (!Files.exists(ANNOUNCEMENTS_FILE)) {
            Files.createFile(ANNOUNCEMENTS_FILE);
        }
        // 用户答题设置（按用户维度持久化）
        if (!Files.exists(ANSWER_SETTINGS_FILE)) {
            Files.createFile(ANSWER_SETTINGS_FILE);
        }
        // 入职培训答题记录
        if (!Files.exists(TRAINING_RECORDS_FILE)) {
            Files.createFile(TRAINING_RECORDS_FILE);
        }
    }

    // ===================== 用户答题设置（CSV: user_id,auto_submit,auto_next_correct,updated_at） =====================

    /**
     * 简单 DTO：Utils.parseJson 可直接写入布尔字段。
     */
    public static class AnswerSettings {
        public boolean autoSubmit;
        public boolean autoNextCorrect;

        public AnswerSettings() {
        }

        public AnswerSettings(boolean autoSubmit, boolean autoNextCorrect) {
            this.autoSubmit = autoSubmit;
            this.autoNextCorrect = autoNextCorrect;
        }
    }

    public static synchronized AnswerSettings loadAnswerSettings(long userId) throws IOException {
        if (!Files.exists(ANSWER_SETTINGS_FILE)) {
            return new AnswerSettings(false, false);
        }
        List<String> lines = Files.readAllLines(ANSWER_SETTINGS_FILE, StandardCharsets.UTF_8);
        for (String ln : lines) {
            if (ln == null || ln.trim().isEmpty()) continue;
            String[] p = ln.split(",", -1);
            if (p.length < 2) continue;
            // allow optional header, skip non-number id
            if (!p[0].trim().matches("\\d+")) continue;
            long uid;
            try {
                uid = Long.parseLong(p[0].trim());
            } catch (Exception ignored) {
                continue;
            }
            if (uid != userId) continue;

            boolean autoSubmit = p.length > 1 && ("true".equalsIgnoreCase(p[1].trim()) || "1".equals(p[1].trim()));
            boolean autoNextCorrect = p.length > 2 && ("true".equalsIgnoreCase(p[2].trim()) || "1".equals(p[2].trim()));
            return new AnswerSettings(autoSubmit, autoNextCorrect);
        }
        return new AnswerSettings(false, false);
    }

    public static synchronized AnswerSettings upsertAnswerSettings(long userId, boolean autoSubmit, boolean autoNextCorrect) throws IOException {
        if (!Files.exists(ANSWER_SETTINGS_FILE)) {
            Files.createFile(ANSWER_SETTINGS_FILE);
        }

        List<String> lines = Files.readAllLines(ANSWER_SETTINGS_FILE, StandardCharsets.UTF_8);
        List<String> out = new ArrayList<>();
        boolean updated = false;

        for (String ln : lines) {
            if (ln == null || ln.trim().isEmpty()) continue;
            String[] p = ln.split(",", -1);
            // keep header lines or malformed lines as-is
            if (p.length < 1 || !p[0].trim().matches("\\d+")) {
                out.add(ln);
                continue;
            }

            long uid;
            try {
                uid = Long.parseLong(p[0].trim());
            } catch (Exception e) {
                out.add(ln);
                continue;
            }

            if (uid == userId) {
                out.add(userId + "," + autoSubmit + "," + autoNextCorrect + "," + Utils.now());
                updated = true;
            } else {
                out.add(ln);
            }
        }

        if (!updated) {
            out.add(userId + "," + autoSubmit + "," + autoNextCorrect + "," + Utils.now());
        }

        Files.write(ANSWER_SETTINGS_FILE, out, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        return new AnswerSettings(autoSubmit, autoNextCorrect);
    }

    // ===================== 入职培训答题记录（CSV: user_id,question_id,attempts,correct,last_at） =====================

    public static class TrainingRecord {
        public long questionId;
        public int attempts;
        public boolean correct;
        public long lastAt; // epoch millis

        public TrainingRecord() {}

        public TrainingRecord(long questionId, int attempts, boolean correct, long lastAt) {
            this.questionId = questionId;
            this.attempts = attempts;
            this.correct = correct;
            this.lastAt = lastAt;
        }
    }

    /** Load all training records for a given user as map keyed by questionId. */
    public static synchronized Map<Long, TrainingRecord> loadTrainingRecords(long userId) throws IOException {
        Map<Long, TrainingRecord> out = new HashMap<>();
        if (!Files.exists(TRAINING_RECORDS_FILE)) return out;
        List<String> lines = Files.readAllLines(TRAINING_RECORDS_FILE, StandardCharsets.UTF_8);
        for (String ln : lines) {
            if (ln == null) continue;
            String t = ln.trim();
            if (t.isEmpty()) continue;
            if (t.startsWith("#")) continue;
            String[] p = t.split(",", -1);
            if (p.length < 5) continue;
            if (!p[0].trim().matches("\\d+")) continue;
            long uid;
            try { uid = Long.parseLong(p[0].trim()); } catch (Exception e) { continue; }
            if (uid != userId) continue;

            long qid;
            int attempts;
            boolean correct;
            long lastAt;
            try {
                qid = Long.parseLong(p[1].trim());
                attempts = Integer.parseInt(p[2].trim());
                correct = "true".equalsIgnoreCase(p[3].trim()) || "1".equals(p[3].trim());
                lastAt = Long.parseLong(p[4].trim());
            } catch (Exception e) {
                continue;
            }
            out.put(qid, new TrainingRecord(qid, attempts, correct, lastAt));
        }
        return out;
    }

    /** Upsert a single training record line for (userId, questionId). */
    public static synchronized TrainingRecord upsertTrainingRecord(long userId, long questionId, int attempts, boolean correct, long lastAt) throws IOException {
        if (!Files.exists(TRAINING_RECORDS_FILE)) {
            Files.createFile(TRAINING_RECORDS_FILE);
        }

        List<String> lines = Files.readAllLines(TRAINING_RECORDS_FILE, StandardCharsets.UTF_8);
        List<String> out = new ArrayList<>();
        boolean updated = false;

        for (String ln : lines) {
            if (ln == null) continue;
            String t = ln.trim();
            if (t.isEmpty()) continue;
            if (t.startsWith("#")) {
                out.add(ln);
                continue;
            }
            String[] p = t.split(",", -1);
            if (p.length < 2 || !p[0].trim().matches("\\d+")) {
                out.add(ln);
                continue;
            }
            long uid;
            long qid;
            try {
                uid = Long.parseLong(p[0].trim());
                qid = Long.parseLong(p[1].trim());
            } catch (Exception e) {
                out.add(ln);
                continue;
            }

            if (uid == userId && qid == questionId) {
                out.add(userId + "," + questionId + "," + attempts + "," + correct + "," + lastAt);
                updated = true;
            } else {
                out.add(ln);
            }
        }

        if (!updated) {
            out.add(userId + "," + questionId + "," + attempts + "," + correct + "," + lastAt);
        }

        Files.write(TRAINING_RECORDS_FILE, out, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        return new TrainingRecord(questionId, attempts, correct, lastAt);
    }

    /** Clear all training records for a user. */
    public static synchronized void clearTrainingRecords(long userId) throws IOException {
        if (!Files.exists(TRAINING_RECORDS_FILE)) return;
        List<String> lines = Files.readAllLines(TRAINING_RECORDS_FILE, StandardCharsets.UTF_8);
        List<String> out = new ArrayList<>();
        for (String ln : lines) {
            if (ln == null) continue;
            String t = ln.trim();
            if (t.isEmpty()) continue;
            if (t.startsWith("#")) { out.add(ln); continue; }
            String[] p = t.split(",", -1);
            if (p.length < 1 || !p[0].trim().matches("\\d+")) { out.add(ln); continue; }
            long uid;
            try { uid = Long.parseLong(p[0].trim()); } catch (Exception e) { out.add(ln); continue; }
            if (uid != userId) out.add(ln);
        }
        Files.write(TRAINING_RECORDS_FILE, out, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    }

    // ===================== 用户管理相关 =====================

    /* 方法loadUsers:
     * 从用户数据文件中加载所有用户信息，返回用户列表
     */
    public static synchronized List<User> loadUsers() throws IOException {
        if (!Files.exists(USERS_FILE)) return new ArrayList<>();
        List<User> out = new ArrayList<>();
        List<String> lines = Files.readAllLines(USERS_FILE, StandardCharsets.UTF_8);
        // 解析每一行用户数据
        for (String ln : lines) {
            if (ln.trim().isEmpty()) continue;
            String[] p = ln.split(",", 5);
            if (p.length < 5) continue;

            // Skip header if present (simple check: first column is not a number)
            if (!p[0].matches("\\d+")) continue;

            long id = Long.parseLong(p[0]);
            String username = p[1];
            String password = p[2];
            boolean isAdmin = Boolean.parseBoolean(p[3]);
            String createdAt = p[4];
            out.add(new User(id, username, password, isAdmin, createdAt));
        }
        return out;
    }

    // 读取 CSV，返回每一行的列数组（包含表头行）
    public static synchronized List<String[]> readUsersCsv() throws IOException {
        List<String[]> rows = new ArrayList<>();
        if (!Files.exists(USERS_FILE)) return rows;
        try (java.io.BufferedReader br = Files.newBufferedReader(USERS_FILE, StandardCharsets.UTF_8)) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                rows.add(line.split(",", -1));
            }
        }
        return rows;
    }

    // 写回 CSV（覆盖）
    public static synchronized void writeUsersCsv(List<String[]> rows) throws IOException {
        try (java.io.BufferedWriter bw = Files.newBufferedWriter(USERS_FILE, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
            for (String[] cols : rows) {
                bw.write(String.join(",", cols));
                bw.newLine();
            }
        }
    }

    /**
     * 设置用户的管理员标志。
     * @param targetId 要修改的用户 id
     * @param makeAdmin true => 设为管理员；false => 设为非管理员
     * @return true 表示成功并已修改；false 表示未找到用户或没有修改（值已相同）
     */
    public static synchronized boolean setUserAdmin(int targetId, boolean makeAdmin) throws IOException {
        List<String[]> rows = readUsersCsv();
        if (rows.isEmpty()) return false;

        // Check if first row is header
        boolean hasHeader = false;
        String[] firstRow = rows.get(0);
        if (firstRow.length > 0 && !firstRow[0].matches("\\d+")) {
            hasHeader = true;
        }

        int idIdx = 0;
        int adminIdx = 3; // Default for id,username,password,isAdmin,createdAt

        if (hasHeader) {
             String[] header = rows.get(0);
             idIdx = -1;
             adminIdx = -1;
             for (int i = 0; i < header.length; i++) {
                String h = header[i].trim().toLowerCase();
                if (h.equals("id")) idIdx = i;
                if (h.equals("is_admin") || h.equals("admin") || h.equals("isadmin")) adminIdx = i;
            }
            if (idIdx == -1) idIdx = 0;
            if (adminIdx == -1) adminIdx = Math.min(3, header.length - 1);
        }

        boolean changed = false;
        int startRow = hasHeader ? 1 : 0;

        for (int r = startRow; r < rows.size(); r++) {
            String[] cols = rows.get(r);
            if (idIdx >= cols.length) continue;
            String idStr = cols[idIdx].trim();
            if (!idStr.matches("\\d+")) continue;
            int id = Integer.parseInt(idStr);
            if (id == targetId) {
                // 确保 cols 长度足够
                if (adminIdx >= cols.length) {
                    // Extend array
                    String[] newCols = new String[adminIdx + 1];
                    System.arraycopy(cols, 0, newCols, 0, cols.length);
                    // Fill gaps with empty strings if any
                    for(int k=cols.length; k<adminIdx; k++) newCols[k] = "";
                    cols = newCols;
                    rows.set(r, cols);
                }
                String current = cols[adminIdx].trim();
                String want = makeAdmin ? "true" : "false";

                // Normalize current
                boolean currentBool = Boolean.parseBoolean(current) || current.equals("1");

                if (currentBool != makeAdmin) {
                    cols[adminIdx] = want;
                    changed = true;
                }
                break;
            }
        }
        if (changed) {
            writeUsersCsv(rows);
        }
        return changed;
    }

    // 读取某个用户是否为管理员（辅助校验）
    public static synchronized boolean isUserAdmin(int userId) throws IOException {
        List<String[]> rows = readUsersCsv();
        if (rows.isEmpty()) return false;

        boolean hasHeader = false;
        String[] firstRow = rows.get(0);
        if (firstRow.length > 0 && !firstRow[0].matches("\\d+")) {
            hasHeader = true;
        }

        int idIdx = 0;
        int adminIdx = 3;

        if (hasHeader) {
             String[] header = rows.get(0);
             idIdx = -1;
             adminIdx = -1;
             for (int i = 0; i < header.length; i++) {
                String h = header[i].trim().toLowerCase();
                if (h.equals("id")) idIdx = i;
                if (h.equals("is_admin") || h.equals("admin") || h.equals("isadmin")) adminIdx = i;
            }
            if (idIdx == -1) idIdx = 0;
            if (adminIdx == -1) adminIdx = Math.min(3, header.length - 1);
        }

        int startRow = hasHeader ? 1 : 0;
        for (int r = startRow; r < rows.size(); r++) {
            String[] cols = rows.get(r);
            if (idIdx >= cols.length) continue;
            String idStr = cols[idIdx].trim();
            if (!idStr.matches("\\d+")) continue;
            int id = Integer.parseInt(idStr);
            if (id == userId) {
                if (adminIdx >= cols.length) return false;
                String v = cols[adminIdx].trim();
                return Boolean.parseBoolean(v) || v.equals("1");
            }
        }
        return false;
    }

    /* 方法loadQuestions:
     * 从题目数据文件中加载所有题目信息，返回题目列表
     */
    public static synchronized List<Question> loadQuestions() throws IOException {
        return loadQuestions(QUESTIONS_FILE);
    }

    // 新增：根据给定文件加载题目，若目标文件不存在则回退到默认 QUESTIONS_FILE
    public static synchronized List<Question> loadQuestions(Path file) throws IOException {
        Path target = file == null ? QUESTIONS_FILE : file;
        if (!Files.exists(target)) {
            // fallback to default questions file
            if (!Files.exists(QUESTIONS_FILE)) return new ArrayList<>();
            target = QUESTIONS_FILE;
        }
        List<Question> out = new ArrayList<>();
        List<String> lines = Files.readAllLines(target, StandardCharsets.UTF_8);
        // 解析每一行题目数据
        for (String ln : lines) {
            if (ln.trim().isEmpty()) continue;

            // Core format is 9 columns:
            // id,type,difficulty,resource,question,hasPicture,options,answer,analysis
            // Some newer rows may have a 10th column: keywords
            String[] p = ln.split(",", 10);
            if (p.length < 9) continue;

            long id = Long.parseLong(p[0]);
            int type = Integer.parseInt(p[1]);
            int difficulty = Integer.parseInt(p[2]);
            String resource = Utils.unescapeCsv(p[3]);
            String question = Utils.unescapeCsv(p[4]);
            boolean hasPicture = !"0".equals(p[5]);
            String optionsRaw = Utils.unescapeCsv(p[6]);
            List<String> options = Arrays.stream(optionsRaw.split("\\|"))
                    .map(String::trim)
                    .collect(Collectors.toList());
            int answer = Integer.parseInt(p[7]);

            // analysis is always the 9th core column (index 8)
            String analysis = Utils.unescapeCsv(p[8]);

            String keywords = "";
            if (p.length >= 10) {
                keywords = Utils.unescapeCsv(p[9]);
            }

            Question q = new Question(id, type, difficulty, resource, question, hasPicture, options, answer, analysis);
            q.setKeywords(keywords);
            out.add(q);
        }
        return out;
    }

    /* 方法appendQuestion:
     * 将新题目信息追加写入题目数据文件
     */
    public static synchronized List<UserAnswer> loadUserAnswers() throws IOException {
        if (!Files.exists(USER_ANSWERS_FILE)) return new ArrayList<>();
        List<UserAnswer> out = new ArrayList<>();
        List<String> lines = Files.readAllLines(USER_ANSWERS_FILE, StandardCharsets.UTF_8);
        // 解析每一行用户答案数据
        for (String ln : lines) {
            if (ln.trim().isEmpty()) continue;
            try {
                String[] p = ln.split(",", 7);
                if (p.length < 7) continue;

                long id = Long.parseLong(p[0]);
                long userId = Long.parseLong(p[1]);
                long questionId = Long.parseLong(p[2]);
                String qt = p[3];
                boolean isCorrect = Boolean.parseBoolean(p[4]);
                int selected = Integer.parseInt(p[5]);
                String at = p[6];
                out.add(new UserAnswer(id, userId, questionId, qt, isCorrect, selected, at));
            } catch (Exception ignoreBadRow) {
                // Skip legacy/corrupted rows silently
            }
        }
        return out;
    }

    /* 方法appendUserAnswer:
     * 将新用户答案信息追加写入用户答案数据文件
     */
    public static synchronized void appendUserAnswer(UserAnswer ua) throws IOException {
        // Keep CSV format consistent with loadUserAnswers():
        // id,userId,questionId,questionType,isCorrect,selected,answeredAt
        String questionType = "normal";
        String selectedStr = ua.getSelectedAnswer() == null ? "0" : ua.getSelectedAnswer();
        int selected;
        try { selected = Integer.parseInt(selectedStr.trim()); } catch (Exception e) { selected = 0; }
        String answeredAt = (ua.getCreatedAt() == null) ? Utils.now() : ua.getCreatedAt().toString().replace('T', ' ');

        String line = ua.getId() + "," + ua.getUserId() + "," + ua.getQuestionId() + "," + questionType + "," + ua.isCorrect() + "," + selected + "," + answeredAt + System.lineSeparator();
        Files.write(USER_ANSWERS_FILE, line.getBytes(StandardCharsets.UTF_8), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
    }

    /* 方法loadExamRecords:
     * 从考试记录数据文件中加载所有考试记录信息，返回考试记录列表
     */
    public static synchronized List<ExamRecord> loadExamRecords() throws IOException {
        if (!Files.exists(EXAM_RECORDS_FILE)) return new ArrayList<>();
        List<ExamRecord> out = new ArrayList<>();
        List<String> lines = Files.readAllLines(EXAM_RECORDS_FILE, StandardCharsets.UTF_8);
        // 解析每一行考试记录数据
        for (String ln : lines) {
            if (ln.trim().isEmpty()) continue;
            String[] p = ln.split(",", 4);
            if (p.length < 4) continue;
            long id = Long.parseLong(p[0]);
            long userId = Long.parseLong(p[1]);
            int score = Integer.parseInt(p[2]);
            String at = p[3];
            out.add(new ExamRecord(id, userId, score, at));
        }
        return out;
    }

    /* 方法appendExamRecord:
     * 将新考试记录信息追加写入考试记录数据文件
     */
    public static synchronized void appendExamRecord(ExamRecord er) throws IOException {
        String line = er.getId() + "," + er.getUserId() + "," + er.getScore() + "," + er.getCreatedAt() + System.lineSeparator();
        Files.write(EXAM_RECORDS_FILE, line.getBytes(StandardCharsets.UTF_8), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
    }

    /* 方法appendUser:
     * 将新用户信息追加写入用户数据文件
     */
    public static synchronized void appendUser(User u) throws IOException {
        String line = u.getId() + "," + u.getUsername() + "," + u.getPassword() + "," + u.isAdmin() + "," + u.getCreatedAt() + System.lineSeparator();
        Files.write(USERS_FILE, line.getBytes(StandardCharsets.UTF_8), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
    }

    /* 方法nextId:
     * 为实体分配下一个可用ID
     * 遍历列表中的每个实体，获取其ID字段的值
     * 找出最大ID值并返回其加一作为下一个可用ID
     */
    public static synchronized long nextId(List<?> list) {
        long max = 0;
        if (list == null || list.isEmpty()) return 1;

        for (Object o : list) {
            if (o == null) continue;
            try {
                // Prefer getId() if present
                try {
                    java.lang.reflect.Method m = o.getClass().getMethod("getId");
                    Object v = m.invoke(o);
                    if (v instanceof Number) {
                        max = Math.max(max, ((Number) v).longValue());
                        continue;
                    }
                } catch (NoSuchMethodException ignored) {
                    // fall through
                }

                // Fallback to field "id"
                try {
                    java.lang.reflect.Field f = o.getClass().getDeclaredField("id");
                    f.setAccessible(true);
                    Object v = f.get(o);
                    if (v instanceof Number) {
                        max = Math.max(max, ((Number) v).longValue());
                    }
                } catch (NoSuchFieldException ignored) {
                    // ignore
                }
            } catch (Exception ignored) {
            }
        }
        return max + 1;
    }
}
