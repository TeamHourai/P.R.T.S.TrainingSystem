package com.hourai.prts.util;

import java.util.regex.Pattern;

/**
 * 输入清洗工具：防御性剥离常见 XSS / 注入向量。
 *
 * <p>用于在用户输入被持久化（尤其是之后会以 HTML 形式回显的文本，如公告内容、题目题干）
 * 之前做一道防线，移除脚本块、事件处理器、javascript: 协议、危险标签等。
 *
 * <p>注意：JPA 参数化查询已天然防 SQL 注入，本类聚焦 XSS 维度。
 * 前端回显亦应做转义（见 notification-helper.js 的 escapeHtml），本类为纵深防御。
 */
public final class InputSanitizer {

    private InputSanitizer() {}

    // <script ...>...</script>（含大小写、换行）
    private static final Pattern SCRIPT_BLOCK = Pattern.compile(
            "<\\s*script[^>]*>[\\s\\S]*?<\\s*/\\s*script\\s*>", Pattern.CASE_INSENSITIVE);
    // 自闭合 <script ... />
    private static final Pattern SCRIPT_TAG = Pattern.compile(
            "<\\s*script[^>]*>", Pattern.CASE_INSENSITIVE);
    // 危险标签：<iframe> <object> <embed> <svg> <style> <link> <meta> <base> <form>
    private static final Pattern DANGEROUS_TAGS = Pattern.compile(
            "<\\s*/?(iframe|object|embed|svg|style|link|meta|base|form|input|button)[^>]*>",
            Pattern.CASE_INSENSITIVE);
    // 事件处理器：on*="..." / on*='...' / on*=value
    private static final Pattern EVENT_HANDLERS = Pattern.compile(
            "\\son\\w+\\s*=\\s*(\"[^\"]*\"|'[^']*'|[^\\s>]+)", Pattern.CASE_INSENSITIVE);
    // javascript: / vbscript: / data:text/html 协议
    private static final Pattern DANGEROUS_PROTOCOLS = Pattern.compile(
            "(?i)(javascript|vbscript|data\\s*:\\s*text/html)\\s*:");

    /**
     * 清洗字符串：剥离脚本块、危险标签、事件处理器与危险协议。
     *
     * @param input 原始输入，可为 null
     * @return 清洗后的字符串；null 入参返回 null
     */
    public static String sanitize(String input) {
        if (input == null) return null;
        String s = input;
        s = SCRIPT_BLOCK.matcher(s).replaceAll("");
        s = SCRIPT_TAG.matcher(s).replaceAll("");
        s = DANGEROUS_TAGS.matcher(s).replaceAll("");
        s = EVENT_HANDLERS.matcher(s).replaceAll("");
        s = DANGEROUS_PROTOCOLS.matcher(s).replaceAll("$1");
        return s;
    }

    /**
     * 清洗并裁剪到最大长度，防超长输入。
     */
    public static String sanitize(String input, int maxLength) {
        String s = sanitize(input);
        if (s != null && maxLength > 0 && s.length() > maxLength) {
            s = s.substring(0, maxLength);
        }
        return s;
    }

    /**
     * 严格清洗：用于不需要任何 HTML 的纯文本字段（如标题、用户名），
     * 移除全部尖括号转义后的标签。
     */
    public static String stripAllHtml(String input) {
        if (input == null) return null;
        String s = sanitize(input);
        // 去除剩余所有标签
        return s.replaceAll("<[^>]*>", "");
    }
}
