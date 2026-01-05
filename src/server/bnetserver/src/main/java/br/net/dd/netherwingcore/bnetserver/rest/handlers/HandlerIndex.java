package br.net.dd.netherwingcore.bnetserver.rest.handlers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;

import static br.net.dd.netherwingcore.common.logging.Log.log;

public class HandlerIndex implements HttpHandler {

    @Override
    public void handle(HttpExchange exchange) throws IOException {

        log("HandlerIndex called");

        String requestMethod = exchange.getRequestMethod();
        switch (requestMethod) {
            case "GET": {
                log("Received GET request at /");
                break;
            }
            case "POST": {
                log("Received POST request at /");
                break;
            }
            default: {
                exchange.sendResponseHeaders(405, -1); // Method Not Allowed
                return;
            }
        }

    }

}
