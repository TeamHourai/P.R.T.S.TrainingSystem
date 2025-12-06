package com.hourai.prts.handler;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.hourai.prts.utils.Utils;
import com.hourai.prts.entity.*;
import java.io.IOException;

/*
  GET /ping
*/
public class PingHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        Utils.send(exchange,200,"{\"ok\":true}");
    }
}