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
    }

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

            long id = Long.parseLong(p[0]);
            String username = p[1];
            String password = p[2];
            boolean isAdmin = Boolean.parseBoolean(p[3]);
            String createdAt = p[4];
            out.add(new User(id, username, password, isAdmin, createdAt));
        }
        return out;
    }

    /* 方法appendUser:
     * 将新用户信息追加写入用户数据文件
     */
    public static synchronized void appendUser(User u) throws IOException {
        String line = u.getId() + "," + u.getUsername() + "," + u.getPassword() + "," + u.isAdmin() + "," + u.getCreatedAt() + System.lineSeparator();
        Files.write(USERS_FILE, line.getBytes(StandardCharsets.UTF_8), StandardOpenOption.APPEND);
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
