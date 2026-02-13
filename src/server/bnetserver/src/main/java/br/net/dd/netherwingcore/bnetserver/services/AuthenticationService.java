package br.net.dd.netherwingcore.bnetserver.services;

import br.net.dd.netherwingcore.bnetserver.server.Session;
import br.net.dd.netherwingcore.common.logging.Log;
import br.net.dd.netherwingcore.common.utilities.MessageBuffer;
import com.google.protobuf.ByteString;

import java.security.SecureRandom;

import static br.net.dd.netherwingcore.proto.client.EntityTypesProto.*;
import static br.net.dd.netherwingcore.proto.client.AuthenticationServiceProto.*;
import static br.net.dd.netherwingcore.proto.BattlenetRpcErrorCode.*;

public class AuthenticationService extends ServiceBase {

    private static final Log logger = Log.getLogger(AuthenticationService.class.getSimpleName());

    private static final int SERVICE_HASH = 0xDECFC01;

    // Method IDs
    private static final int METHOD_LOGON = 1;
    private static final int METHOD_VERIFY_WEB_CREDENTIALS = 2;
    private static final int METHOD_GENERATE_WEB_CREDENTIALS = 3;
    private static final int METHOD_GENERATE_SSO_TOKEN = 4;

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

            case METHOD_GENERATE_SSO_TOKEN:
                handleGenerateSSOToken(session, token, buffer);
                break;

            default:
                logger.warn("{} Unknown method ID {} for AuthenticationService",
                        session.getClientInfo(), methodId);
                sendErrorResponse(session, token, ERROR_RPC_NOT_IMPLEMENTED);
        }
    }

    private void handleLogon(Session session, int token, MessageBuffer buffer) {
        LogonRequest request = parseMessage(buffer, LogonRequest.newBuilder(), "Logon");

        if (request == null) {
            sendErrorResponse(session, token, ERROR_RPC_MALFORMED_REQUEST);
            return;
        }

        logger.info("╔══════════════════════════════════════════════════════");
        logger.info("║ {} 🔐 WoW Client Logon Request", session.getClientInfo());
        logger.info("╠══════════════════════════════════════════════════════");

        if (!request.getProgram().isEmpty()) {
            logger.info("║ Program:       {}", request.getProgram());
        }

        if (!request.getPlatform().isEmpty()) {
            logger.info("║ Platform:      {}", request.getPlatform());
        }

        if (!request.getLocale().isEmpty()) {
            logger.info("║ Locale:        {}", request.getLocale());
        }

        if (!request.getEmail().isEmpty()) {
            logger.info("║ Email:         {}", request.getEmail());
            session.setAccountName(request.getEmail());
        }

        if (!request.getVersionBytes().isEmpty()) {
            byte[] versionBytes = request.getVersionBytes().toByteArray();
            logger.info("║ Version:       {}", bytesToVersionString(versionBytes));
        }

        if (request.getApplicationVersion() != 0) {
            logger.info("║ App Version:   {}", request.getApplicationVersion());
        }

        logger.info("╚══════════════════════════════════════════════════════");

        // TODO: Validate credentials against database
        // For now, accept any login
        boolean loginSuccessful = authenticateUser(session, request);

        if (loginSuccessful) {
            sendLogonSuccess(session, token, request);
        } else {
            sendLogonFailure(session, token, ERROR_DENIED.getValue());
        }
    }

    private boolean authenticateUser(Session session, LogonRequest request) {
        // TODO: Check database for account
        // TODO: Verify password (SRP6)
        // TODO: Check if banned

        // For now, accept any login with email
        if (request.getEmail().isEmpty()) {
            logger.warn("{} Login failed: no email provided", session.getClientInfo());
            return false;
        }

        logger.info("{} ✅ Login accepted (dev mode - no authentication)",
                session.getClientInfo());

        return true;
    }

    private void sendLogonSuccess(Session session, int token, LogonRequest request) {
        // Generate account ID (for now, random)
        int accountId = 100000 + random.nextInt(900000);
        int gameAccountId = 200000 + random.nextInt(900000);

        // Create Battle.net account EntityId
        EntityId bnetAccountId = EntityId.newBuilder()
                .setHigh(ACCOUNT_ENTITY_HIGH)
                .setLow(accountId)
                .build();

        // Create WoW game account EntityId
        EntityId wowGameAccountId = EntityId.newBuilder()
                .setHigh(GAME_ACCOUNT_ENTITY_HIGH)
                .setLow(gameAccountId)
                .build();

        // Generate session key (64 bytes)
        byte[] sessionKey = generateSessionKey();

        LogonResult.Builder resultBuilder = LogonResult.newBuilder();
        resultBuilder.setErrorCode(ERROR_OK.getValue());
        resultBuilder.setAccountId(bnetAccountId);              // ✅ EntityId
        resultBuilder.addGameAccountId(wowGameAccountId);       // ✅ EntityId

        if (!request.getEmail().isEmpty()) {
            resultBuilder.setEmail(request.getEmail());
            resultBuilder.setBattleTag(generateBattleTag(request.getEmail()));
        }

        // Set regions (1 = US, 2 = EU, 3 = CN, etc)
        resultBuilder.addAvailableRegion(1);  // US
        resultBuilder.setConnectedRegion(1);   // US

        // GeoIP country
        resultBuilder.setGeoipCountry("BRA");

        // Session key for encryption
        resultBuilder.setSessionKey(ByteString.copyFrom(sessionKey));

        LogonResult result = resultBuilder.build();

        // Send response
        sendResponse(session, token, result);

        // Mark session as authenticated
        session.setAuthenticated(true);
        session.setAccountId(accountId);

        logger.info("╔══════════════════════════════════════════════════════");
        logger.info("║ {} 🎉 Logon Successful", session.getClientInfo());
        logger.info("╠══════════════════════════════════════════════════════");
        logger.info("║ BNet Account:  {}.{}",
                Long.toHexString(bnetAccountId.getHigh()).toUpperCase(),
                bnetAccountId.getLow());
        logger.info("║ Game Account:  {}.{}",
                Long.toHexString(wowGameAccountId.getHigh()).toUpperCase(),
                wowGameAccountId.getLow());
        logger.info("║ Battle Tag:    {}", resultBuilder.getBattleTag());
        logger.info("║ Region:        US");
        logger.info("╚══════════════════════════════════════════════════════");
    }

    private void sendLogonFailure(Session session, int token, int errorCode) {
        LogonResult.Builder resultBuilder = LogonResult.newBuilder();
        resultBuilder.setErrorCode(errorCode);

        sendResponse(session, token, resultBuilder.build());

        logger.warn("{} ❌ Logon failed: error code {}",
                session.getClientInfo(), errorCode);
    }

    private void handleVerifyWebCredentials(Session session, int token, MessageBuffer buffer) {
        logger.debug("{} VerifyWebCredentials called", session.getClientInfo());

        // TODO: Implement web credentials verification
        sendErrorResponse(session, token, ERROR_OK);
    }

    private void handleGenerateWebCredentials(Session session, int token, MessageBuffer buffer) {
        logger.debug("{} GenerateWebCredentials called", session.getClientInfo());

        GenerateWebCredentialsResponse.Builder responseBuilder =
                GenerateWebCredentialsResponse.newBuilder();

        // Generate random web credentials
        byte[] webCreds = new byte[128];
        random.nextBytes(webCreds);
        responseBuilder.setWebCredentials(ByteString.copyFrom(webCreds));

        sendResponse(session, token, responseBuilder.build());
    }

    private void handleGenerateSSOToken(Session session, int token, MessageBuffer buffer) {
        logger.debug("{} GenerateSSOToken called", session.getClientInfo());

        GenerateSSOTokenResponse.Builder responseBuilder =
                GenerateSSOTokenResponse.newBuilder();

        // Generate SSO ID and secret
        byte[] ssoId = new byte[32];
        byte[] ssoSecret = new byte[128];
        random.nextBytes(ssoId);
        random.nextBytes(ssoSecret);

        responseBuilder.setSsoId(ByteString.copyFrom(ssoId));
        responseBuilder.setSsoSecret(ByteString.copyFrom(ssoSecret));

        sendResponse(session, token, responseBuilder.build());
    }

    // ========================================================================
    // Helper Methods
    // ========================================================================

    /**
     * Generates a random session key (64 bytes)
     */
    private byte[] generateSessionKey() {
        byte[] key = new byte[64];
        random.nextBytes(key);
        return key;
    }

    /**
     * Generates a battle tag from email
     */
    private String generateBattleTag(String email) {
        String username = email.split("@")[0];
        int tagNumber = 1000 + random.nextInt(9000);
        return username + "#" + tagNumber;
    }

    /**
     * Converts version bytes to string
     */
    private String bytesToVersionString(byte[] bytes) {
        if (bytes.length >= 5) {
            return String.format("%d.%d.%d.%d (build %d)",
                    bytes[0] & 0xFF,
                    bytes[1] & 0xFF,
                    bytes[2] & 0xFF,
                    bytes[3] & 0xFF,
                    ((bytes[4] & 0xFF) << 8) | (bytes[5] & 0xFF));
        }
        return bytesToHex(bytes);
    }

    /**
     * Converts bytes to hex string
     */
    private static String bytesToHex(byte[] bytes) {
        StringBuilder hex = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            hex.append(String.format("%02X", b));
        }
        return hex.toString();
    }

}
