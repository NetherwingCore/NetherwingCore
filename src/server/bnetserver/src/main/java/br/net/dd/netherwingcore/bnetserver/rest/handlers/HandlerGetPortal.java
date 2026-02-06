package br.net.dd.netherwingcore.bnetserver.rest.handlers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;

import static br.net.dd.netherwingcore.common.logging.Log.log;

/**
 * HandlerGetPortal is responsible for handling requests to the /bnetserver/portal/ endpoint.
 * This endpoint is likely used for retrieving information about the portal, such as available services, status, or other relevant data.
 */
public class HandlerGetPortal implements HttpHandler {

    @Override
    public void handle(HttpExchange exchange) throws IOException {

        log("HandlerGetPortal called");

    }

}
