package br.net.dd.netherwingcore.bnetserver.rest.handlers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;

import static br.net.dd.netherwingcore.common.logging.Log.log;

/**
 * HandlerLogin is responsible for handling requests to the /bnetserver/login/ endpoint.
 * This endpoint is likely used for processing login requests, which may involve validating user credentials,
 * initiating authentication processes, or providing responses related to login attempts.
 */
public class HandlerLogin implements HttpHandler {

    @Override
    public void handle(HttpExchange exchange) throws IOException {

        log("HandlerLogin called");

    }

}
