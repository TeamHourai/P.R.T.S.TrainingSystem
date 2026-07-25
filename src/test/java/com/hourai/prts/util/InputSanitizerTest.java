package com.hourai.prts.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class InputSanitizerTest {

    @Test
    void sanitizeRemovesScriptsEventHandlersAndDangerousProtocols() {
        String input = "<script>alert(1)</script><a onclick=\"evil()\" href=\"javascript:run()\">safe</a>";

        String sanitized = InputSanitizer.sanitize(input);

        assertFalse(sanitized.toLowerCase().contains("<script"));
        assertFalse(sanitized.toLowerCase().contains("onclick"));
        assertFalse(sanitized.toLowerCase().contains("javascript:"));
    }

    @Test
    void stripAllHtmlKeepsPlainTextOnly() {
        assertEquals("公告标题", InputSanitizer.stripAllHtml("<b>公告</b><i>标题</i>"));
    }
}
