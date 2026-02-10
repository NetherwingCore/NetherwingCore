package br.net.dd.netherwingcore.bnetserver.services;

import br.net.dd.netherwingcore.bnetserver.server.Session;
import br.net.dd.netherwingcore.proto.BattlenetRpcErrorCode;
import br.net.dd.netherwingcore.proto.client.AccountServiceProto;
import br.net.dd.netherwingcore.proto.client.AccountTypesProto;
import com.google.protobuf.InvalidProtocolBufferException;

/**
 * Handler for the Account Service, responsible for managing account-related requests and responses.
 * This service provides functionalities such as retrieving account state and game account state.
 */
public class AccountServiceHandler implements ServiceHandler {

    private static final int METHOD_GET_ACCOUNT_STATE = 30;
    private static final int METHOD_GET_GAME_ACCOUNT_STATE = 31;

    /**
     * Handles incoming RPC method calls for the Account Service.
     *
     * @param session  The session associated with the client making the request.
     * @param methodId The ID of the method being called.
     * @param token    The token associated with the RPC call for response correlation.
     * @param data     The serialized request data in bytes.
     * @throws InvalidProtocolBufferException If there is an error parsing the request data.
     */
    @Override
    public void handleMethod(Session session, int methodId, int token, byte[] data)
            throws InvalidProtocolBufferException {

        if (!session.isAuthenticated()) {
            session.sendError(token, BattlenetRpcErrorCode.ERROR_DENIED.name());
            return;
        }

        int actualMethodId = methodId & 0x3FFFFFFF;

        switch (actualMethodId) {
            case METHOD_GET_ACCOUNT_STATE:
                handleGetAccountState(session, token, data);
                break;
            case METHOD_GET_GAME_ACCOUNT_STATE:
                handleGetGameAccountState(session, token, data);
                break;
            default:
                session.sendError(token, BattlenetRpcErrorCode.ERROR_RPC_NOT_IMPLEMENTED.name());
                break;
        }

    }

    /**
     * Handles the GetAccountState RPC method, which retrieves the current state of the account.
     *
     * @param session The session associated with the client making the request.
     * @param token   The token associated with the RPC call for response correlation.
     * @param data    The serialized request data in bytes.
     * @throws InvalidProtocolBufferException If there is an error parsing the request data.
     */
    private void handleGetAccountState(Session session, int token, byte[] data)
            throws InvalidProtocolBufferException {
        AccountServiceProto.GetAccountStateRequest request = AccountServiceProto.GetAccountStateRequest.parseFrom(data);
        AccountServiceProto.GetAccountStateResponse.Builder response = AccountServiceProto.GetAccountStateResponse.newBuilder();

        AccountTypesProto.AccountState.Builder state = AccountTypesProto.AccountState.newBuilder();
        state.setAccountLevelInfo(buildAccountLevelInfo(session));
        state.setPrivacyInfo(buildPrivacyInfo());

        state.addGameLevelInfo(buildGameLevelInfo(null));

        response.setState(state);
        response.setTags(buildAccountFieldTag("AccountLevelInfo"));
        response.setTags(buildAccountFieldTag("PrivacyInfo"));

        session.sendResponse(token, response.build());
    }

    /**
     * Handles the GetGameAccountState RPC method, which retrieves the current state of the game account.
     *
     * @param session The session associated with the client making the request.
     * @param token   The token associated with the RPC call for response correlation.
     * @param data    The serialized request data in bytes.
     * @throws InvalidProtocolBufferException If there is an error parsing the request data.
     */
    private void handleGetGameAccountState(Session session, int token, byte[] data)
            throws InvalidProtocolBufferException {

        AccountServiceProto.GetGameAccountStateRequest request = AccountServiceProto.GetGameAccountStateRequest.parseFrom(data);
        AccountServiceProto.GetGameAccountStateResponse.Builder response = AccountServiceProto.GetGameAccountStateResponse.newBuilder();
        AccountTypesProto.GameAccountState.Builder state = AccountTypesProto.GameAccountState.newBuilder();

        state.setGameLevelInfo(buildGameLevelInfo(null));
        state.setGameStatus(buildGameStatus(null));

        response.setState(state);

        session.sendResponse(token, response.build());

    }

    /**
     * Builds the AccountLevelInfo object for the account state response.
     *
     * @param session The session associated with the client making the request.
     * @return An instance of AccountLevelInfo with the relevant data populated.
     */
    private AccountTypesProto.AccountLevelInfo buildAccountLevelInfo(Session session) {
        return AccountTypesProto.AccountLevelInfo.newBuilder().build();
    }

    /**
     * Builds the PrivacyInfo object for the account state response.
     *
     * @return An instance of PrivacyInfo with the relevant data populated.
     */
    private AccountTypesProto.PrivacyInfo buildPrivacyInfo() {
        return AccountTypesProto.PrivacyInfo.newBuilder().build();
    }

    /**
     * Builds the GameLevelInfo object for the game account state response.
     *
     * @param gameAccountInfo The game account information used to populate the GameLevelInfo.
     * @return An instance of GameLevelInfo with the relevant data populated.
     */
    private AccountTypesProto.GameLevelInfo buildGameLevelInfo(Object gameAccountInfo) {
        return AccountTypesProto.GameLevelInfo.newBuilder().build();
    }

    /**
     * Builds the GameStatus object for the game account state response.
     *
     * @param gameAccountInfo The game account information used to populate the GameStatus.
     * @return An instance of GameStatus with the relevant data populated.
     */
    private AccountTypesProto.GameStatus buildGameStatus(Object gameAccountInfo) {
        return AccountTypesProto.GameStatus.newBuilder().build();
    }

    /**
     * Builds the AccountFieldTags object for the account state response.
     *
     * @param field The name of the field for which to build the tags.
     * @return An instance of AccountFieldTags with the relevant data populated.
     */
    private AccountTypesProto.AccountFieldTags buildAccountFieldTag(String field) {
        return AccountTypesProto.AccountFieldTags.newBuilder().build();
    }
}
