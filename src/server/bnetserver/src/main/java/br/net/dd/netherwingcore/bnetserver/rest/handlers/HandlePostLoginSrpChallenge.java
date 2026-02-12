package br.net.dd.netherwingcore.bnetserver.rest.handlers;

import br.net.dd.netherwingcore.common.logging.Log;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;

/**
 * HandlePostLoginSrpChallenge is responsible for handling POST requests to the /bnetserver/login/srp/ endpoint.
 * This endpoint is likely used for processing SRP (Secure Remote Password) challenges during the login process.
 */
public class HandlePostLoginSrpChallenge implements HttpHandler {

    private static final Log logger = Log.getLogger(HandlePostLoginSrpChallenge.class.getSimpleName());

    @Override
    public void handle(HttpExchange exchange) throws IOException {

        logger.log("HandlePostLoginSrpChallenge called");

    }
}
