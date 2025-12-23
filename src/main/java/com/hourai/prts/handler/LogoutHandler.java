package com.hourai.prts.handler;

import com.hourai.prts.utils.Utils;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;

/**
 * POST /api/v1/auth/logout
 *
 * This project uses a lightweight stateless token, so logout is purely client-side.
 * We still provide this endpoint so the frontend can call it without CORS errors.
 */
public class LogoutHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            Utils.send(exchange, 405, "{\"success\":false,\"message\":\"POST required\"}");
            return;
        }
        Utils.send(exchange, 200, "{\"success\":true,\"message\":\"logged out\"}");
    }
}

