package com.hourai.prts.controller;

import com.hourai.prts.service.AuditLogService;
import com.hourai.prts.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AdminControllerSecurityTest {

    @Test
    void permissionChangeUsesAuthenticatedActor() {
        UserService userService = mock(UserService.class);
        AuditLogService auditLogService = mock(AuditLogService.class);
        AdminController controller = new AdminController(userService, auditLogService);
        Authentication authentication = mock(Authentication.class);
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(authentication.getPrincipal()).thenReturn(7L);
        when(request.getMethod()).thenReturn("POST");
        when(request.getRequestURI()).thenReturn("/api/v1/admin/user/permission");
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");
        when(userService.setAdminStatus(7L, 9L, true)).thenReturn(true);

        controller.setPermission(9L, "true", authentication, request);

        verify(userService).setAdminStatus(7L, 9L, true);
    }
}
