package com.hourai.prts.handler;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.hourai.prts.utils.Utils;
import java.io.IOException;

/*
  GET /ping
  用于检查服务器是否在线
*/
public class PingHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        Utils.send(exchange,200,"{\"ok\":true}");
    }
}