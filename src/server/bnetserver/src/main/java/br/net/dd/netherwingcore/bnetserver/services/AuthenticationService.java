package br.net.dd.netherwingcore.bnetserver.services;

import br.net.dd.netherwingcore.bnetserver.server.Session;
import br.net.dd.netherwingcore.common.logging.Log;
import br.net.dd.netherwingcore.common.utilities.MessageBuffer;

import java.security.SecureRandom;

import static br.net.dd.netherwingcore.proto.client.AuthenticationServiceProto.*;
import static br.net.dd.netherwingcore.proto.BattlenetRpcErrorCode.*;

public class AuthenticationService extends ServiceBase {

    private static final Log logger = Log.getLogger(AuthenticationService.class.getSimpleName());

    private static final int SERVICE_HASH = 0xDECFC01;

    // Method IDs
    private static final int METHOD_LOGON = 1;
    private static final int METHOD_VERIFY_WEB_CREDENTIALS = 2;
    private static final int METHOD_GENERATE_WEB_CREDENTIALS = 3;

    // EntityId constants for generating unique EntityIds for accounts and game accounts
    private static final long ACCOUNT_ENTITY_HIGH = 0x100000000000000L;  // Battle.net account
    private static final long GAME_ACCOUNT_ENTITY_HIGH = 0x200000000000000L; // WoW game account

    private final SecureRandom random = new SecureRandom();

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

            case METHOD_GENERATE_WEB_CREDENTIALS:
                handleGenerateWebCredentials(session, token, buffer);
                break;

            default:
                logger.warn("{} Unknown method ID {} for AuthenticationService",
                        session.getClientInfo(), methodId);
                sendErrorResponse(session, token, ERROR_RPC_NOT_IMPLEMENTED);
        }
    }

    private void handleLogon(Session session, int token, MessageBuffer buffer) {
        logger.debug("{} Logging on", session.getClientInfo());

        LogonRequest request = parseMessage(buffer, LogonRequest.newBuilder(), "Logon");

        logger.debug("Program: {}, Platform: {}, Locale: {}", request.getProgram(), request.getPlatform(), request.getLocale());

        if (!request.getProgram().equals("WoW")) {
            logger.debug("{} Unsupported program {} in LogonRequest", session.getClientInfo(), request.getProgram());
            sendErrorResponse(session, token, ERROR_BAD_PROGRAM);
        }

    }

    private void handleVerifyWebCredentials(Session session, int token, MessageBuffer buffer) {
        logger.debug("{} Verifying Web Credentials", session.getClientInfo());
    }

    private void handleGenerateWebCredentials(Session session, int token, MessageBuffer buffer) {
        logger.debug("{} Generating Web Credentials", session.getClientInfo());
    }

}
