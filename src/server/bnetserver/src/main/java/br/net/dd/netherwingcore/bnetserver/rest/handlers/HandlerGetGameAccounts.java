package br.net.dd.netherwingcore.bnetserver.rest.handlers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;

import static br.net.dd.netherwingcore.common.logging.Log.log;

/**
 * HandlerGetGameAccounts is responsible for handling requests to the /bnetserver/gameAccounts/ endpoint.
 * This endpoint is likely used for retrieving information about game accounts associated with a user.
 */
public class HandlerGetGameAccounts implements HttpHandler {

    @Override
    public void handle(HttpExchange exchange) throws IOException {

        log("HandlerGetGameAccounts called");

    }
}
