package br.net.dd.netherwingcore.bnetserver.services;

import br.net.dd.netherwingcore.bnetserver.server.Session;
import br.net.dd.netherwingcore.proto.BattlenetRpcErrorCode;
import br.net.dd.netherwingcore.proto.client.AuthenticationServiceProto;
import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;

import static br.net.dd.netherwingcore.common.logging.Log.log;

/**
 * Handler for the Authentication Service, responsible for processing authentication-related requests.
 */
public class AuthenticationServiceHandler implements ServiceHandler {

    private static final int METHOD_LOGON = 1;
    private static final int METHOD_VERIFY_WEB_CREDENTIALS = 7;
    private static final int METHOD_GENERATE_WEB_CREDENTIALS = 8;

    /**
     * Handles incoming RPC method calls for the Authentication Service.
     *
     * @param session  The session associated with the incoming request.
     * @param methodId The ID of the method being called.
     * @param token    The token associated with the request, used for correlating responses.
     * @param data     The raw byte data of the request, which will be parsed according to the method being called.
     * @throws InvalidProtocolBufferException If there is an error parsing the request data.
     */
    @Override
    public void handleMethod(Session session, int methodId, int token, byte[] data) throws InvalidProtocolBufferException {
        int actualMethodId = methodId & 0x3FFFFFFF;

        switch (actualMethodId) {
            case METHOD_LOGON:
                handleLogon(session, token, data);
                break;
            case METHOD_VERIFY_WEB_CREDENTIALS:
                handleVerifyWebCredentials(session, token, data);
                break;
            case METHOD_GENERATE_WEB_CREDENTIALS:
                handleGenerateWebCredentials(session, token, data);
                break;
            default:
                System.out.println("Unknown method ID: " + actualMethodId);
        }

    }

    /**
     * Handles the Logon method, which is responsible for generating a login ticket for authenticated sessions.
     *
     * @param session The session associated with the incoming request.
     * @param token   The token associated with the request, used for correlating responses.
     * @param data    The raw byte data of the request, which will be parsed to extract necessary information.
     * @throws InvalidProtocolBufferException If there is an error parsing the request data.
     */
    private void handleLogon(Session session, int token, byte[] data)
            throws InvalidProtocolBufferException {

        AuthenticationServiceProto.GenerateWebCredentialsRequest request = AuthenticationServiceProto.GenerateWebCredentialsRequest.parseFrom(data);

        if(!session.isAuthenticated()) {
            session.sendError(token, BattlenetRpcErrorCode.ERROR_DENIED.name());
            return;
        }

        log("Received Logon request for account: " + session.getAccountInfo());
        String loginTicket = generateLoginTicket();


    }

    /**
     * Handles the GenerateWebCredentials method, which is responsible for generating web credentials for authenticated sessions.
     *
     * @param session The session associated with the incoming request.
     * @param token   The token associated with the request, used for correlating responses.
     * @param data    The raw byte data of the request, which will be parsed to extract necessary information.
     * @throws InvalidProtocolBufferException If there is an error parsing the request data.
     */
    private void handleGenerateWebCredentials(Session session, int token, byte[] data)
            throws InvalidProtocolBufferException {

        AuthenticationServiceProto.LogonRequest request = AuthenticationServiceProto.LogonRequest.parseFrom(data);

        log("Received GenerateWebCredentials request for account: " + session.getAccountInfo());

        session.sendError(token, BattlenetRpcErrorCode.ERROR_RPC_NOT_IMPLEMENTED.name());
    }

    /**
     * Handles the VerifyWebCredentials method, which is responsible for verifying web credentials for authenticated sessions.
     *
     * @param session The session associated with the incoming request.
     * @param token   The token associated with the request, used for correlating responses.
     * @param data    The raw byte data of the request, which will be parsed to extract necessary information.
     * @throws InvalidProtocolBufferException If there is an error parsing the request data.
     */
    private void handleVerifyWebCredentials(Session session, int token, byte[] data)
            throws InvalidProtocolBufferException {
        AuthenticationServiceProto.VerifyWebCredentialsRequest request = AuthenticationServiceProto.VerifyWebCredentialsRequest.parseFrom(data);
        ByteString webCredentials = request.getWebCredentials();

        log("Received VerifyWebCredentials request for account: " + session.getAccountInfo() + ", webCredentials: " + webCredentials.toStringUtf8());

        session.sendError(token, BattlenetRpcErrorCode.ERROR_RPC_NOT_IMPLEMENTED.name());
    }

    /**
     * Generates a unique login ticket for authenticated sessions. This is a placeholder implementation and should be replaced with a secure token generation mechanism in a production environment.
     *
     * @return A unique login ticket as a string.
     */
    private String generateLoginTicket() {
        return java.util.UUID.randomUUID().toString().replace("-", "").toUpperCase();
    }
}
