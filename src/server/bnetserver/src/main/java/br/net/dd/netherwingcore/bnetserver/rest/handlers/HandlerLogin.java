package br.net.dd.netherwingcore.bnetserver.rest.handlers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;

import static br.net.dd.netherwingcore.common.logging.Log.log;

public class HandlerLogin implements HttpHandler {

    @Override
    public void handle(HttpExchange exchange) throws IOException {

        log("HandlerLogin called");

    }

}
