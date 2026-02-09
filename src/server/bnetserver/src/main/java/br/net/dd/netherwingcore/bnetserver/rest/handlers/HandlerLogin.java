package br.net.dd.netherwingcore.bnetserver.rest.handlers;

import br.net.dd.netherwingcore.bnetserver.utilities.SOAPUtil;
import br.net.dd.netherwingcore.common.configuration.Config;
import br.net.dd.netherwingcore.proto.login.LoginProto.FormInputs;
import br.net.dd.netherwingcore.proto.login.LoginProto.FormInput;
import br.net.dd.netherwingcore.proto.login.LoginProto.FormType;
import br.net.dd.netherwingcore.shared.json.ProtobufJSON;
import com.google.gson.Gson;
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

    private FormInputs formInputs;

    /**
     * Handles incoming HTTP requests to the /bnetserver/login/ endpoint.
     * Depending on the HTTP method (GET or POST), it processes the request accordingly.
     * For GET requests, it initializes form inputs and sends a JSON response with the form details.
     * For POST requests, it logs the received request and sends a simple JSON response acknowledging the POST request.
     *
     * @param exchange The HttpExchange object representing the incoming HTTP request and response.
     * @throws IOException If an I/O error occurs while handling the request.
     */
    @Override
    public void handle(HttpExchange exchange) throws IOException {

        switch (exchange.getRequestMethod()) {
            case "GET":
                initializeFormInputs();
                Integer loginRESTPort = Config.get("LoginREST.Port", 8081);
                this.formInputs.toBuilder().setSrpUrl("https://127.0.0.1:" + loginRESTPort + "/bnetserver/login/srp/");
                this.formInputs.toBuilder().build();
                log("Received GET request for /bnetserver/login/ > LoginRESTService::HandleGetForm");
                String serialized = ProtobufJSON.serialize(this.formInputs);
                SOAPUtil.sendJson(exchange, serialized, 200);
                break;
            case "POST":
                log("Received POST request for /bnetserver/login/ > LoginRESTService::HandlePostLogin");
                SOAPUtil.sendJson(exchange, new Gson().toJson("Received POST request for /bnetserver/login/ > LoginRESTService::HandlePostLogin"), 200);
                break;
            default:
                log("Received unsupported HTTP method: " + exchange.getRequestMethod());
                SOAPUtil.sendJson(exchange, new Gson().toJson("Unsupported HTTP method"), 405);
                return;
        }

    }

    /**
     * Initializes the form inputs for the login form. This method creates a FormInputs object and populates it with
     * the necessary input fields for the login form, such as account name, password, and submit button.
     * The form type is set to LOGIN_FORM, and each input field is configured with its respective properties.
     */
    private void initializeFormInputs() {
        this.formInputs = FormInputs.newBuilder().buildPartial();
        this.formInputs.toBuilder().setType(FormType.LOGIN_FORM);

        FormInput input1 = FormInput.newBuilder()
                .setInputId("account_name")
                .setType("text")
                .setLabel("E-mail")
                .setMaxLength(320)
                .build();

        this.formInputs.toBuilder().addInputs(input1);

        FormInput input2 = FormInput.newBuilder()
                .setInputId("password")
                .setType("password")
                .setLabel("Password")
                .setMaxLength(128)
                .build();

        this.formInputs.toBuilder().addInputs(input2);

        FormInput input3 = FormInput.newBuilder()
                .setInputId("log_in_submit")
                .setType("submit")
                .setLabel("Log In")
                .build();

        this.formInputs.toBuilder().addInputs(input3);
    }

}
