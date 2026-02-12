package br.net.dd.netherwingcore.bnetserver.rest.handlers;

import br.net.dd.netherwingcore.common.logging.Log;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;

/**
 * HandlerGetGameAccounts is responsible for handling requests to the /bnetserver/gameAccounts/ endpoint.
 * This endpoint is likely used for retrieving information about game accounts associated with a user.
 */
public class HandlerGetGameAccounts implements HttpHandler {

    private static final Log logger = Log.getLogger(HandlerGetGameAccounts.class.getSimpleName());

    @Override
    public void handle(HttpExchange exchange) throws IOException {

        logger.log("HandlerGetGameAccounts called");

    }
}
