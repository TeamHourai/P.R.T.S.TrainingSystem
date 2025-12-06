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

    public static String now() {
        return LocalDateTime.now().format(DT);
    }

    /* 方法parseQuery:
     * 解析查询字符串为键值对映射
     * 参数为字符串q，格式为 key1=value1&key2=value2&...
     * 键与值由等号(=)分隔，多个键值对由与号(&)分隔
     * 如果查询字符串为空或为 null，则返回一个空的映射
     */
    public static Map<String, String> parseQuery(String q) {
        Map<String, String> m = new HashMap<>();
        if (q == null || q.isEmpty()) return m;
        // 分割每个键值对
        for (String part : q.split("&")) {
            // 分割键和值
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

    /* 方法parseForm:
     * 解析HTTP请求的表单数据为键值对映射
     * 参数为HttpExchange对象ex，表示HTTP交换信息
     * 从请求体中读取表单数据，并调用parseQuery方法进行解析
     * 返回解析后的键值对映射
     */
    public static Map<String, String> parseForm(HttpExchange ex) throws IOException {
        InputStream is = ex.getRequestBody();
        String body = new String(is.readAllBytes(), StandardCharsets.UTF_8);
        return parseQuery(body);
    }

    /* 方法parseAnswers:
     * 解析答案字符串为映射
     * 参数为字符串s，格式为 id1:answer1,id2:answer2,...
     * 每个答案由题目ID和答案值组成，使用冒号(:)分隔
     * 多个答案之间使用逗号(,)分隔
     * 返回一个映射，键为题目ID（Long类型），值为答案值（Integer类型）
     */
    public static Map<Long, Integer> parseAnswers(String s) {
        Map<Long, Integer> m = new LinkedHashMap<>();
        if (s == null || s.trim().isEmpty()) return m;
        // 解析每个答案对
        String[] parts = s.split(",");
        for (String p : parts) {
            // 分割题目ID和答案值
            String[] kv = p.split(":");
            if (kv.length != 2) continue;
            try {
                long k = Long.parseLong(kv[0].trim());
                int v = Integer.parseInt(kv[1].trim());
                m.put(k, v);
            } catch (Exception ignored) {
            }
        }
        return m;
    }

    /* 方法escapeJson:
     * 转义字符串以适应JSON格式
     * 参数为字符串s，表示要转义的字符串
     * 替换反斜杠(\)为双反斜杠(\\)
     * 替换双引号(")为转义双引号(\")
     * 替换换行符(\n)为转义换行符(\\n)
     * 替换回车符(\r)为转义回车符(\\r)
     * 返回转义后的字符串
     */
    public static String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r");
    }

    /* 方法csvQ:
     * 生成CSV格式的题目字符串
     * 参数包括题目的各个属性，如ID、类型、难度、资源、问题、选项、答案和解析
     * 对字符串属性进行CSV转义，确保特殊字符不会破坏CSV格式
     * 返回一个以逗号分隔的CSV格式字符串，表示题目的各个属性
     */
    public static String csvQ(long id, int type, int difficulty, String resource, String question, boolean hasPicture, String options, int answer, String analysis) {
        return id + "," + type + "," + difficulty + "," + csvEscape(resource) + "," + csvEscape(question) + "," + (hasPicture ? 1 : 0) + "," + csvEscape(options) + "," + answer + "," + csvEscape(analysis);
    }

    /* 方法csvEscape:
     * 转义字符串以适应CSV格式
     * 参数为字符串s，表示要转义的字符串
     * 替换换行符(\n)和回车符(\r)为空格
     * 替换逗号(,)为全角逗号(，)
     * 替换竖线(|)为特殊字符(¦)
     * 返回转义后的字符串
     */
    public static String csvEscape(String s) {
        if (s == null) return "";
        return s.replace("\n", " ").replace("\r", " ").replace(",", "，").replace("|", "¦");
    }

    /* 方法unescapeCsv:
     * 反转义CSV格式的字符串
     * 参数为字符串s，表示要反转义的字符串
     * 替换全角逗号(，)为逗号(,)
     * 替换特殊字符(¦)为竖线(|)
     * 返回反转义后的字符串
     */
    public static String unescapeCsv(String s) {
        if (s == null) return "";
        return s.replace("，", ",").replace("¦", "|");
    }

    /* 方法send:
     * 发送HTTP响应
     * 参数包括HttpExchange对象ex，表示HTTP交换信息；整数code，表示HTTP状态码；字符串body，表示响应体内容
     * 将响应体内容转换为UTF-8字节数组
     * 设置响应头的Content-Type为application/json，字符集为UTF-8
     * 发送响应头，包含状态码和响应体长度
     * 将响应体字节数组写入响应输出流
     */
    public static void send(HttpExchange ex, int code, String body) throws IOException {
        byte[] b = body.getBytes(StandardCharsets.UTF_8);
        ex.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
        ex.sendResponseHeaders(code, b.length);
        try (OutputStream os = ex.getResponseBody()) {
            os.write(b);
        }
    }

    /* 方法urlDecode:
     * 对URL编码的字符串进行解码
     * 参数为字符串s，表示要解码的字符串
     * 使用UTF-8字符集进行解码
     * 如果解码过程中发生异常，则返回原始字符串
     * 返回解码后的字符串
     */
    public static String urlDecode(String s) {
        try {
            return URLDecoder.decode(s, StandardCharsets.UTF_8);
        } catch (Exception e) {
            return s;
        }
    }

    /* 方法questionsToJson:
     * 将题目列表转换为JSON格式字符串
     * 参数为题目列表qs，表示要转换的题目集合
     * 遍历题目列表，将每个题目的属性转换为JSON格式
     * 对字符串属性进行JSON转义，确保特殊字符不会破坏JSON格式
     * 返回一个包含所有题目的JSON数组字符串
     */
    public static String questionsToJson(List<Question> qs) {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        boolean first = true;
        for (Question q : qs) {
            if (!first) sb.append(",");
            first = false;
            sb.append("{");
            sb.append("\"id\":").append(q.getId()).append(",");
            sb.append("\"type\":").append(q.getType()).append(",");
            sb.append("\"difficulty\":").append(q.getDifficulty()).append(",");
            sb.append("\"question\":\"").append(escapeJson(q.getQuestion())).append("\",");
            sb.append("\"options\":[");
            boolean f2 = true;
            String[] optionsArr = q.getOptions() != null ? q.getOptions().split("\\|") : new String[0];
            for (String opt : optionsArr) {
                if (!f2) sb.append(",");
                f2 = false;
                sb.append("\"").append(escapeJson(opt)).append("\"");
            }
            sb.append("],");
            sb.append("\"answer\":\"").append(q.getAnswer()).append("\"");
            sb.append("}");
        }
        sb.append("]");
        return sb.toString();
    }
}