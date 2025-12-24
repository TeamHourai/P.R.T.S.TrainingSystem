package com.hourai.prts.handler;

import com.hourai.prts.data.DataStore;
import com.hourai.prts.entity.Question;
import com.hourai.prts.utils.Utils;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.stream.Collectors;

/*
  /questions 与 /api/v1/questions
  - GET    /questions               列表（可分页/过滤：page/size/type/difficulty/keyword）
  - GET    /questions/{id}          单题详情
  - POST   /questions               新建题目（JSON）
  - PUT    /questions/{id}          更新题目（JSON）
  - DELETE /questions/{id}          删除题目

  说明：数据存储在 data/questions.csv（与 DataStore.loadQuestions() 同源）。
*/
public class QuestionsHandler implements HttpHandler {
    private static final Path QUESTIONS_FILE = Paths.get("data").resolve("questions.csv");
    private static final Path ONBOARDING_FILE = Paths.get("data").resolve("questions_onboarding.csv");

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();

        // 兼容 /api/v1/questions/...
        int idx = path.indexOf("/questions");
        String sub = idx >= 0 ? path.substring(idx) : path; // /questions or /questions/1

        Long id = null;
        String[] segs = sub.split("/");
        if (segs.length >= 3) {
            try { id = Long.parseLong(segs[2]); } catch (Exception ignored) { }
        }

        if ("GET".equalsIgnoreCase(method)) {
            if (id != null) {
                handleGetOne(exchange, id);
            } else {
                handleGetList(exchange);
            }
            return;
        }

        if ("POST".equalsIgnoreCase(method) && id == null) {
            handleCreate(exchange);
            return;
        }

        if ("PUT".equalsIgnoreCase(method) && id != null) {
            handleUpdate(exchange, id);
            return;
        }

        if ("DELETE".equalsIgnoreCase(method) && id != null) {
            handleDelete(exchange, id);
            return;
        }

        Utils.send(exchange, 405, "{\"error\":\"method not allowed\"}");
    }

    private void handleGetList(HttpExchange exchange) throws IOException {
        Map<String, String> q = Utils.parseQuery(exchange.getRequestURI().getQuery());
        int page = parseIntOrDefault(q.get("page"), 1);
        int size = parseIntOrDefault(q.get("size"), 50);
        Integer type = q.get("type") == null || q.get("type").isEmpty() ? null : parseIntOrNull(q.get("type"));
        Integer difficulty = q.get("difficulty") == null || q.get("difficulty").isEmpty() ? null : parseIntOrNull(q.get("difficulty"));
        String keyword = q.getOrDefault("keyword", "").trim();

        String mode = q.getOrDefault("mode", "");
        String fullPath = exchange.getRequestURI().getPath() == null ? "" : exchange.getRequestURI().getPath();
        boolean useOnboarding = "onboarding".equalsIgnoreCase(mode) || fullPath.toLowerCase().contains("/training/");
        List<Question> all = new ArrayList<>();
        boolean dbOk = true;
        try {
            com.hourai.prts.service.QuestionService questionService = new com.hourai.prts.service.QuestionService();
            all = questionService.getAllQuestions();
        } catch (Exception e) {
            dbOk = false;
        }
        if (!dbOk) {
            if (useOnboarding) {
                try {
                    com.hourai.prts.service.QuestionService questionService = new com.hourai.prts.service.QuestionService();
                    // 约定type=2为onboarding题库
                    all = questionService.getAllQuestionsByType(2);
                } catch (Exception dbEx) {
                    all = DataStore.loadQuestions(ONBOARDING_FILE);
                }
            } else {
                try {
                    com.hourai.prts.service.QuestionService questionService = new com.hourai.prts.service.QuestionService();
                    all = questionService.getAllQuestions();
                } catch (Exception dbEx) {
                    all = DataStore.loadQuestions(QUESTIONS_FILE);
                }
            }
        }
        String keywordLower = keyword == null ? "" : keyword.toLowerCase();
        List<Question> filtered = all.stream().filter(qq -> {
            if (type != null && qq.getType() != type) return false;
            if (difficulty != null && qq.getDifficulty() != difficulty) return false;
            if (!keywordLower.isEmpty()) {
                String kws = qq.getKeywords() == null ? "" : qq.getKeywords();
                if (kws.toLowerCase().contains(keywordLower)) return true;
                String hay = (qq.getQuestion() + " " + qq.getAnalysis()).toLowerCase();
                return hay.contains(keywordLower);
            }
            return true;
        }).collect(Collectors.toList());
        if (!keywordLower.isEmpty() && filtered.size() > 1) {
            List<Question> keywordMatches = new ArrayList<>();
            List<Question> otherMatches = new ArrayList<>();
            for (Question qq : filtered) {
                String kws = qq.getKeywords() == null ? "" : qq.getKeywords();
                if (kws.toLowerCase().contains(keywordLower)) keywordMatches.add(qq);
                else otherMatches.add(qq);
            }
            filtered = new ArrayList<>();
            filtered.addAll(keywordMatches);
            filtered.addAll(otherMatches);
        }
        int from = Math.min((page - 1) * size, filtered.size());
        int to = Math.min(from + size, filtered.size());
        List<Question> pageList = filtered.subList(from, to);
        Utils.send(exchange, 200, Utils.questionsToJson(pageList));
    }

    private void handleGetOne(HttpExchange exchange, long id) throws IOException {
        Map<String, String> q = Utils.parseQuery(exchange.getRequestURI().getQuery());
        String mode = q.getOrDefault("mode", "");
        String fullPath = exchange.getRequestURI().getPath() == null ? "" : exchange.getRequestURI().getPath();
        boolean useOnboarding = "onboarding".equalsIgnoreCase(mode) || fullPath.toLowerCase().contains("/training/");
        boolean dbOk = true;
        try {
            com.hourai.prts.service.QuestionService questionService = new com.hourai.prts.service.QuestionService();
            Question qz = questionService.getQuestionById(id);
            if (qz != null) {
                Utils.send(exchange, 200, oneQuestionToJson(qz));
                return;
            }
        } catch (Exception e) {
            dbOk = false;
        }
        if (!dbOk) {
            List<Question> all;
            if (useOnboarding) {
                try {
                    com.hourai.prts.service.QuestionService questionService = new com.hourai.prts.service.QuestionService();
                    all = questionService.getAllQuestionsByType(2);
                } catch (Exception dbEx) {
                    all = DataStore.loadQuestions(ONBOARDING_FILE);
                }
            } else {
                try {
                    com.hourai.prts.service.QuestionService questionService = new com.hourai.prts.service.QuestionService();
                    all = questionService.getAllQuestions();
                } catch (Exception dbEx) {
                    all = DataStore.loadQuestions(QUESTIONS_FILE);
                }
            }
            for (Question qz : all) {
                if (qz.getId() != null && qz.getId() == id) {
                    Utils.send(exchange, 200, oneQuestionToJson(qz));
                    return;
                }
            }
        }
        Utils.send(exchange, 404, "{\"error\":\"not found\"}");
    }

    private void handleCreate(HttpExchange exchange) throws IOException {
        Map<String, Object> body = readJsonBody(exchange);
        if (body == null) { Utils.send(exchange, 400, "{\"error\":\"invalid json\"}"); return; }

        // 根据请求判断是否写入入职培训题库
        Map<String, String> qparams = Utils.parseQuery(exchange.getRequestURI().getQuery());
        String mode = qparams.getOrDefault("mode", "");
        String fullPath = exchange.getRequestURI().getPath() == null ? "" : exchange.getRequestURI().getPath();
        boolean useOnboarding = "onboarding".equalsIgnoreCase(mode) || fullPath.toLowerCase().contains("/training/");
        Path target = useOnboarding ? ONBOARDING_FILE : QUESTIONS_FILE;

        long newId = -1;
        Question q = null;
        boolean dbOk = true;
        try {
            com.hourai.prts.service.QuestionService questionService = new com.hourai.prts.service.QuestionService();
            // 获取最大id+1
            List<Question> all = questionService.getAllQuestions();
            newId = all.stream().mapToLong(qq -> qq.getId() == null ? 0 : qq.getId()).max().orElse(0L) + 1;
            q = buildQuestionFromBody(newId, body);
            questionService.addQuestion(q);
        } catch (Exception e) {
            dbOk = false;
        }
        if (!dbOk) {
            List<Question> all = DataStore.loadQuestions(target);
            newId = DataStore.nextId(all);
            q = buildQuestionFromBody(newId, body);
            appendQuestionCsv(q, target);
        }
        Utils.send(exchange, 200, "{\"id\":" + newId + "}");
    }

    private void handleUpdate(HttpExchange exchange, long id) throws IOException {
        Map<String, Object> body = readJsonBody(exchange);
        if (body == null) { Utils.send(exchange, 400, "{\"error\":\"invalid json\"}"); return; }

        // Determine which file to update (support training path or mode=onboarding)
        Map<String, String> qparams = Utils.parseQuery(exchange.getRequestURI().getQuery());
        String mode = qparams.getOrDefault("mode", "");
        String fullPath = exchange.getRequestURI().getPath() == null ? "" : exchange.getRequestURI().getPath();
        boolean useOnboarding = "onboarding".equalsIgnoreCase(mode) || fullPath.toLowerCase().contains("/training/");
        Path target = useOnboarding ? ONBOARDING_FILE : QUESTIONS_FILE;

        boolean dbOk = true;
        boolean found = false;
        try {
            com.hourai.prts.service.QuestionService questionService = new com.hourai.prts.service.QuestionService();
            Question q = buildQuestionFromBody(id, body);
            int updated = questionService.updateQuestion(q);
            if (updated > 0) {
                found = true;
            }
        } catch (Exception e) {
            dbOk = false;
        }
        if (!dbOk) {
            List<String> newLines = new ArrayList<>();
            List<String> lines = Files.readAllLines(target, StandardCharsets.UTF_8);
            for (String ln : lines) {
                if (ln.trim().isEmpty()) continue;
                String[] p = ln.split(",", 9);
                if (p.length < 9) continue;
                long qid;
                try { qid = Long.parseLong(p[0]); } catch (Exception ex) { continue; }
                if (qid != id) {
                    newLines.add(ln);
                    continue;
                }
                found = true;
                Question q = buildQuestionFromBody(id, body);
                newLines.add(toCsvLine(q));
            }
            if (!found) { Utils.send(exchange, 404, "{\"error\":\"not found\"}"); return; }
            Files.write(target, newLines, StandardCharsets.UTF_8, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE);
        }
        Utils.send(exchange, 200, "{\"success\":true}");
    }

    private void handleDelete(HttpExchange exchange, long id) throws IOException {
        // Determine target file (training mode or default)
        Map<String, String> qparams = Utils.parseQuery(exchange.getRequestURI().getQuery());
        String mode = qparams.getOrDefault("mode", "");
        String fullPath = exchange.getRequestURI().getPath() == null ? "" : exchange.getRequestURI().getPath();
        boolean useOnboarding = "onboarding".equalsIgnoreCase(mode) || fullPath.toLowerCase().contains("/training/");
        Path target = useOnboarding ? ONBOARDING_FILE : QUESTIONS_FILE;

        boolean dbOk = true;
        boolean found = false;
        try {
            com.hourai.prts.service.QuestionService questionService = new com.hourai.prts.service.QuestionService();
            int deleted = questionService.deleteQuestion(id);
            if (deleted > 0) {
                found = true;
            }
        } catch (Exception e) {
            dbOk = false;
        }
        if (!dbOk) {
            if (!Files.exists(target)) { Utils.send(exchange, 404, "{\"error\":\"not found\"}"); return; }
            List<String> lines = Files.readAllLines(target, StandardCharsets.UTF_8);
            List<String> newLines = new ArrayList<>();
            for (String ln : lines) {
                if (ln.trim().isEmpty()) continue;
                String[] p = ln.split(",", 9);
                if (p.length < 1) continue;
                try {
                    long qid = Long.parseLong(p[0]);
                    if (qid == id) { found = true; continue; }
                } catch (Exception ignored) { }
                newLines.add(ln);
            }
            if (!found) { Utils.send(exchange, 404, "{\"error\":\"not found\"}"); return; }
            Files.write(target, newLines, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE);
        }
        Utils.send(exchange, 200, "{\"success\":true}");
    }

    private static int parseIntOrDefault(String s, int def) {
        if (s == null) return def;
        try { return Integer.parseInt(s); } catch (Exception e) { return def; }
    }

    private static Integer parseIntOrNull(String s) {
        try { return Integer.parseInt(s); } catch (Exception e) { return null; }
    }

    private static String oneQuestionToJson(Question q) {
        // options 在实体里是 "A|B|C|D"
        String[] opts = (q.getOptions() == null ? "" : q.getOptions()).split("\\|");
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"id\":").append(q.getId()).append(",");
        sb.append("\"type\":").append(q.getType()).append(",");
        sb.append("\"difficulty\":").append(q.getDifficulty()).append(",");
        sb.append("\"resource\":\"").append(Utils.escapeJson(q.getResource())).append("\",");
        sb.append("\"question\":\"").append(Utils.escapeJson(q.getQuestion())).append("\",");
        sb.append("\"picture\":").append(q.isHasPicture()).append(",");
        sb.append("\"options\":[");
        for (int i = 0; i < 4; i++) {
            if (i > 0) sb.append(",");
            String v = i < opts.length ? opts[i] : "";
            sb.append("\"").append(Utils.escapeJson(v)).append("\"");
        }
        sb.append("],");
        sb.append("\"answer\":").append(parseIntOrDefault(q.getAnswer(), 0)).append(",");
        sb.append("\"analysis\":\"").append(Utils.escapeJson(q.getAnalysis())).append("\",");
        // keywords: stored in Question.keywords as pipe-separated string
        String kwRaw = q.getKeywords() == null ? "" : q.getKeywords();
        String[] kwArr = kwRaw.isEmpty() ? new String[0] : kwRaw.split("\\|");
        sb.append("\"keywords\":[");
        for (int i = 0; i < kwArr.length; i++) {
            if (i > 0) sb.append(',');
            sb.append("\"").append(Utils.escapeJson(kwArr[i])).append("\"");
        }
        sb.append("]");
        sb.append("}");
        return sb.toString();
    }

    private static Map<String, Object> readJsonBody(HttpExchange exchange) throws IOException {
        String ct = exchange.getRequestHeaders().getFirst("Content-Type");
        if (ct == null || !ct.toLowerCase().contains("application/json")) {
            // 兼容：也允许 x-www-form-urlencoded
            Map<String, String> form = Utils.parseForm(exchange);
            Map<String, Object> out = new HashMap<>();
            out.putAll(form);
            return out;
        }
        String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        return parseJsonToMap(body);
    }

    // 非严格 JSON 解析：支持简单对象、数组
    private static Map<String, Object> parseJsonToMap(String json) {
        if (json == null) return null;
        String s = json.trim();
        if (!s.startsWith("{") || !s.endsWith("}")) return null;
        s = s.substring(1, s.length() - 1).trim();
        Map<String, Object> m = new HashMap<>();
        if (s.isEmpty()) return m;

        // 仅解析本编辑器会提交的字段：type,difficulty,question,options(answer array),answer,analysis,resource,picture
        // 为避免引入依赖，这里做简化解析。
        // 先按顶层逗号拆分（不支持字符串中逗号）
        List<String> parts = splitTopLevel(s);
        for (String part : parts) {
            String[] kv = part.split(":", 2);
            if (kv.length != 2) continue;
            String k = strip(kv[0]);
            String v = kv[1].trim();
            k = stripJsonQuotes(k);
            if (v.startsWith("[")) {
                // options array
                List<String> arr = parseJsonStringArray(v);
                m.put(k, arr);
            } else if (v.startsWith("\"")) {
                m.put(k, stripJsonQuotes(v));
            } else if ("true".equalsIgnoreCase(v) || "false".equalsIgnoreCase(v)) {
                m.put(k, Boolean.parseBoolean(v));
            } else {
                // number or null
                if (v.startsWith("null")) m.put(k, null);
                else m.put(k, v.replaceAll("[^0-9-]", ""));
            }
        }
        return m;
    }

    private static List<String> splitTopLevel(String s) {
        List<String> out = new ArrayList<>();
        int depthArr = 0;
        boolean inStr = false;
        StringBuilder cur = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '"' && (i == 0 || s.charAt(i - 1) != '\\')) inStr = !inStr;
            if (!inStr) {
                if (c == '[') depthArr++;
                if (c == ']') depthArr--;
            }
            if (c == ',' && !inStr && depthArr == 0) {
                out.add(cur.toString());
                cur.setLength(0);
            } else {
                cur.append(c);
            }
        }
        if (cur.length() > 0) out.add(cur.toString());
        return out;
    }

    private static List<String> parseJsonStringArray(String v) {
        String t = v.trim();
        if (!t.startsWith("[") || !t.endsWith("]")) return Collections.emptyList();
        t = t.substring(1, t.length() - 1).trim();
        if (t.isEmpty()) return Collections.emptyList();
        List<String> out = new ArrayList<>();
        List<String> parts = splitTopLevel(t);
        for (String p : parts) {
            out.add(stripJsonQuotes(p.trim()));
        }
        return out;
    }

    private static String strip(String s) { return s == null ? "" : s.trim(); }

    private static String stripJsonQuotes(String s) {
        if (s == null) return "";
        String t = s.trim();
        if (t.startsWith("\"") && t.endsWith("\"") && t.length() >= 2) {
            t = t.substring(1, t.length() - 1);
        }
        return t.replace("\\\\", "\\").replace("\\\"", "\"");
    }

    private static Question buildQuestionFromBody(long id, Map<String, Object> body) {
        int type = parseIntOrDefault(toStr(body.get("type")), 1);
        int difficulty = parseIntOrDefault(toStr(body.get("difficulty")), 1);
        String question = toStr(body.get("question"));
        String analysis = toStr(body.get("analysis"));
        String resource = toStr(body.get("resource"));
        boolean picture = toBool(body.get("picture"));

        List<String> options;
        Object o = body.get("options");
        if (o instanceof List) {
            //noinspection unchecked
            options = ((List<Object>) o).stream().map(QuestionsHandler::toStr).collect(Collectors.toList());
        } else {
            options = Arrays.asList(toStr(body.get("optA")), toStr(body.get("optB")), toStr(body.get("optC")), toStr(body.get("optD")));
        }
        while (options.size() < 4) options.add("");

        int answer = parseIntOrDefault(toStr(body.get("answer")), 0);
        Question q = new Question(id, type, difficulty, resource, question, picture, options, answer, analysis);
        // keywords: can be array or comma-separated string
        Object kw = body.get("keywords");
        String kwRaw = "";
        if (kw instanceof List) {
            //noinspection unchecked
            List<Object> l = (List<Object>) kw;
            List<String> ks = new ArrayList<>();
            for (Object k : l) { if (k != null) ks.add(String.valueOf(k).trim()); }
            kwRaw = String.join("|", ks);
        } else if (kw instanceof String) {
            String ks = ((String) kw).trim();
            if (!ks.isEmpty()) {
                // allow comma separated input
                if (ks.contains(",") || ks.contains("，")) {
                    String[] parts = ks.split("[,，]");
                    List<String> arr = new ArrayList<>();
                    for (String p : parts) { if (!p.trim().isEmpty()) arr.add(p.trim()); }
                    kwRaw = String.join("|", arr);
                } else if (ks.contains("|")) {
                    kwRaw = ks; // already pipe separated
                } else {
                    kwRaw = ks;
                }
            }
        }
        q.setKeywords(kwRaw);
        return q;
    }

    private static boolean toBool(Object v) {
        if (v == null) return false;
        if (v instanceof Boolean) return (Boolean) v;
        String s = String.valueOf(v).trim();
        return "1".equals(s) || "true".equalsIgnoreCase(s);
    }

    private static String toStr(Object v) {
        if (v == null) return "";
        return String.valueOf(v);
    }

    private static void appendQuestionCsv(Question q) throws IOException {
        appendQuestionCsv(q, QUESTIONS_FILE);
    }

    // 新增：向指定文件追加题目行
    private static void appendQuestionCsv(Question q, Path file) throws IOException {
        if (!Files.exists(file)) {
            Files.createDirectories(file.getParent());
            Files.createFile(file);
        }
        Files.write(file,
                (toCsvLine(q) + System.lineSeparator()).getBytes(StandardCharsets.UTF_8),
                StandardOpenOption.APPEND);
    }

    private static String toCsvLine(Question q) {
        // 兼容 DataStore.loadQuestions() 解析：9列
        String options = q.getOptions() == null ? "" : q.getOptions();
        String analysis = q.getAnalysis() == null ? "" : q.getAnalysis();
        String resource = q.getResource() == null ? "" : q.getResource();
        String question = q.getQuestion() == null ? "" : q.getQuestion();
        String keywords = q.getKeywords() == null ? "" : q.getKeywords();
        return q.getId() + "," + q.getType() + "," + q.getDifficulty() + "," + Utils.csvEscape(resource) + "," + Utils.csvEscape(question) + "," + (q.isHasPicture() ? 1 : 0) + "," + Utils.csvEscape(options) + "," + parseIntOrDefault(q.getAnswer(), 0) + "," + Utils.csvEscape(analysis) + "," + Utils.csvEscape(keywords);
    }
}
