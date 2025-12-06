package com.hourai.prts.utils;

import com.sun.net.httpserver.HttpExchange;
import com.hourai.prts.entity.Question;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/*
  通用工具：解析参数、生成 JSON（非常简陋）、CSV 辅助、发送响应
*/
public class Utils {
    static final DateTimeFormatter DT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static String now() { return LocalDateTime.now().format(DT); }

    public static Map<String,String> parseQuery(String q) {
        Map<String,String> m = new HashMap<>();
        if (q == null || q.isEmpty()) return m;
        for (String part : q.split("&")) {
            int idx = part.indexOf('=');
            if (idx > 0) {
                String k = urlDecode(part.substring(0, idx));
                String v = urlDecode(part.substring(idx + 1));
                m.put(k, v);
            } else {
                m.put(urlDecode(part), "");
            }
        }
        return m;
    }

    public static Map<String,String> parseForm(HttpExchange ex) throws IOException {
        InputStream is = ex.getRequestBody();
        String body = new String(is.readAllBytes(), StandardCharsets.UTF_8);
        return parseQuery(body);
    }

    public static Map<Long,Integer> parseAnswers(String s) {
        Map<Long,Integer> m = new LinkedHashMap<>();
        if (s == null || s.trim().isEmpty()) return m;
        String[] parts = s.split(",");
        for (String p : parts) {
            String[] kv = p.split(":");
            if (kv.length != 2) continue;
            try {
                long k = Long.parseLong(kv[0].trim());
                int v = Integer.parseInt(kv[1].trim());
                m.put(k, v);
            } catch (Exception ignored) { }
        }
        return m;
    }

    public static String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\","\\\\").replace("\"","\\\"").replace("\n","\\n").replace("\r","\\r");
    }

    public static String csvQ(long id,int type,int difficulty,String resource,String question,boolean hasPicture,String options,int answer,String analysis){
        return id + "," + type + "," + difficulty + "," + csvEscape(resource) + "," + csvEscape(question) + "," + (hasPicture?1:0) + "," + csvEscape(options) + "," + answer + "," + csvEscape(analysis);
    }

    public static String csvEscape(String s){
        if (s==null) return "";
        return s.replace("\n"," ").replace("\r"," ").replace(",", "，").replace("|","¦");
    }

    public static String unescapeCsv(String s){
        if (s==null) return "";
        return s.replace("，",",").replace("¦","|");
    }

    public static void send(HttpExchange ex, int code, String body) throws IOException {
        byte[] b = body.getBytes(StandardCharsets.UTF_8);
        ex.getResponseHeaders().set("Content-Type","application/json; charset=utf-8");
        ex.sendResponseHeaders(code, b.length);
        try (OutputStream os = ex.getResponseBody()) { os.write(b); }
    }

    public static String urlDecode(String s){
        try { return URLDecoder.decode(s, StandardCharsets.UTF_8); } catch (Exception e) { return s; }
    }

    public static String questionsToJson(List<Question> qs) {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        boolean first = true;
        for (Question q : qs) {
            if (!first) sb.append(",");
            first = false;
            sb.append("{");
            sb.append("\"id\":").append(q.id).append(",");
            sb.append("\"type\":").append(q.type).append(",");
            sb.append("\"difficulty\":").append(q.difficulty).append(",");
            sb.append("\"question\":\"").append(escapeJson(q.question)).append("\",");
            sb.append("\"options\":[");
            boolean f2 = true;
            for (String opt : q.options) {
                if (!f2) sb.append(",");
                f2 = false;
                sb.append("\"").append(escapeJson(opt)).append("\"");
            }
            sb.append("],");
            sb.append("\"answer\":").append(q.answer);
            sb.append("}");
        }
        sb.append("]");
        return sb.toString();
    }
}