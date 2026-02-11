package br.net.dd.netherwingcore.bnetserver.services;

import br.net.dd.netherwingcore.bnetserver.server.Session;
import br.net.dd.netherwingcore.common.logging.Log;
import br.net.dd.netherwingcore.common.utilities.MessageBuffer;
import br.net.dd.netherwingcore.database.implementation.LoginDatabase;
import br.net.dd.netherwingcore.proto.client.RpcTypesProto;

import static br.net.dd.netherwingcore.proto.client.EntityTypesProto.*;
import static br.net.dd.netherwingcore.proto.client.AuthenticationServiceProto.*;
import static br.net.dd.netherwingcore.proto.BattlenetRpcErrorCode.*;

public class AuthenticationService extends ServiceBase {

    private static final int SERVICE_HASH = 0xDECFC01;  // Hash do Authentication Service

    // Method IDs
    private static final int METHOD_LOGON = 1;
    private static final int METHOD_VERIFY_WEB_CREDENTIALS = 7;

    @Override
    public int getServiceHash() {
        return SERVICE_HASH;
    }

    @Override
    public void callServerMethod(Session session, int token, int methodId, MessageBuffer buffer) {
        switch (methodId) {
            case METHOD_LOGON:
                handleLogon(session, token, buffer);
                break;
            case METHOD_VERIFY_WEB_CREDENTIALS:
                handleVerifyWebCredentials(session, token, buffer);
                break;
            default:
                Log.warn("Unknown method ID: {} for Authentication Service", String.valueOf(methodId));
                sendErrorResponse(session, token, ERROR_RPC_NOT_IMPLEMENTED);
        }
    }

    private void handleLogon(Session session, int token, MessageBuffer buffer) {

        LogonResult request = parseMessage(buffer, LogonResult.newBuilder(), "Logon");

        if (request == null) {
            sendErrorResponse(session, token, ERROR_RPC_MALFORMED_REQUEST);
            return;
        }

        Log.info("{} Logon request for program: {}", session.getClientInfo(), request.getBattleTag());

        // TODO: Implement the actual authentication logic here, verifying the user's credentials and generating a valid session token.
        String email = request.getEmail();
        LoginDatabase.AccountInfo accountInfo = LoginDatabase.getAccountByEmail(email);

        if (accountInfo.id == 0) {
            Log.warn("No account found for email: {}", email);
            sendErrorResponse(session, token, ERROR_DENIED);
            return;
        }

        if (accountInfo.isBanned) {
            Log.warn("Account with email {} is banned", email);
            sendErrorResponse(session, token, ERROR_DENIED);
            return;
        }

        LogonResult.Builder responseBuilder = LogonResult.newBuilder();
        responseBuilder.setAccountId(EntityId.newBuilder().setHigh(accountInfo.id).setLow(0));
        responseBuilder.addGameAccountId(EntityId.newBuilder().setHigh(accountInfo.gameAccountId).setLow(0));

        session.setAuthenticated(true);
        session.setAccountName(email);
        session.setAccountId(accountInfo.id);

        sendResponse(session, token, responseBuilder.build());

        Log.info("Account {} successfully authenticated", email);
    }

    private void handleVerifyWebCredentials(Session session, int token, MessageBuffer buffer) {
        VerifyWebCredentialsRequest request = parseMessage(buffer, VerifyWebCredentialsRequest.newBuilder(), "VerifyWebCredentials");

            if (request == null) {
                sendErrorResponse(session, token, ERROR_RPC_MALFORMED_REQUEST);
                return;
            }

            String webCredentials = request.getWebCredentials().toStringUtf8();

            // TODO: Implement the actual verification logic for the web credentials here, checking them against the database or an external service as needed.

            Log.info("{} VerifyWebCredentials request with credentials: {}", session.getClientInfo(), webCredentials);

            // For demonstration purposes, we'll just accept any non-empty credentials and return a success response.
            if (webCredentials.isEmpty()) {
                Log.warn("Empty web credentials provided");
                sendErrorResponse(session, token, ERROR_DENIED);
                return;
            }

            sendResponse(session, token, RpcTypesProto.NoData.newBuilder().build());
    }
}
