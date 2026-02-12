package br.net.dd.netherwingcore.bnetserver.rest.handlers;

import br.net.dd.netherwingcore.common.logging.Log;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;

/**
 * HandlerIndex is responsible for handling requests to the root endpoint ("/") of the REST API.
 * It can be used to provide basic information about the API, such as available endpoints, status, or a welcome message.
 */
public class HandlerIndex implements HttpHandler {

    private static final Log logger = Log.getLogger(HandlerIndex.class.getName());

    @Override
    public void handle(HttpExchange exchange) throws IOException {

        logger.log("HandlerIndex called");

        String requestMethod = exchange.getRequestMethod();
        switch (requestMethod) {
            case "GET": {
                logger.log("Received GET request at /");
                break;
            }
            case "POST": {
                logger.log("Received POST request at /");
                break;
            }
            default: {
                exchange.sendResponseHeaders(405, -1); // Method Not Allowed
                return;
            }
        }

    }

}
