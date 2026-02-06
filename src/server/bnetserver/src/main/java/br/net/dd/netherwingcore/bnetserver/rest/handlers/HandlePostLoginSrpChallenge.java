package br.net.dd.netherwingcore.bnetserver.rest.handlers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;

import static br.net.dd.netherwingcore.common.logging.Log.log;

/**
 * HandlePostLoginSrpChallenge is responsible for handling POST requests to the /bnetserver/login/srp/ endpoint.
 * This endpoint is likely used for processing SRP (Secure Remote Password) challenges during the login process.
 */
public class HandlePostLoginSrpChallenge implements HttpHandler {
    @Override
    public void handle(HttpExchange exchange) throws IOException {

        log("HandlePostLoginSrpChallenge called");

    }
}
