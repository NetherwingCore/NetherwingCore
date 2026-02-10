package br.net.dd.netherwingcore.bnetserver.services;

import br.net.dd.netherwingcore.bnetserver.server.Session;
import br.net.dd.netherwingcore.proto.BattlenetRpcErrorCode;
import br.net.dd.netherwingcore.proto.client.GameUtilitiesServiceProto;
import br.net.dd.netherwingcore.proto.client.api.client.v2.AttributeTypesProto;
import com.google.gson.JsonObject;
import com.google.protobuf.InvalidProtocolBufferException;

import static br.net.dd.netherwingcore.common.logging.Log.log;

public class GameUtilitiesServiceHandler implements ServiceHandler {

    private static final int METHOD_PROCESS_CLIENT_REQUEST = 1;
    private static final int METHOD_GET_ALL_VALUES_FOR_ATTRIBUTE = 10;

    @Override
    public void handleMethod(Session session, int methodId, int token, byte[] data)
            throws InvalidProtocolBufferException {

        int actualMethodId = methodId & 0x3FFFFFFF;

        switch (actualMethodId) {
            case METHOD_PROCESS_CLIENT_REQUEST:
                handleProcessClientRequest(session, token, data);
                break;
            case METHOD_GET_ALL_VALUES_FOR_ATTRIBUTE:
                handleGetAllValuesForAttribute(session, token, data);
                break;
            default:
                session.sendError(token, BattlenetRpcErrorCode.ERROR_RPC_NOT_IMPLEMENTED.name());
                break;
        }

    }

    private void handleProcessClientRequest(Session session, int token, byte[] data)
            throws InvalidProtocolBufferException {

        GameUtilitiesServiceProto.ClientRequest request = GameUtilitiesServiceProto.ClientRequest.parseFrom(data);

        br.net.dd.netherwingcore.proto.client.AttributeTypesProto.Attribute attribute = request.getAttribute(0);
        String command = attribute.getName();

        log(String.format(
                "ProcessClientRequest from %s: command=%s",
                session.getAccountInfo(), command
        ));

        JsonObject responseData = processCommand(session, command, request);

        GameUtilitiesServiceProto.ClientResponse.Builder response = GameUtilitiesServiceProto.ClientResponse.newBuilder();

        if (responseData != null) {
            AttributeTypesProto.Attribute.Builder attr = AttributeTypesProto.Attribute.newBuilder();
            attr.setName("Param_RealmListTicket");
            AttributeTypesProto.Variant.Builder variant = AttributeTypesProto.Variant.newBuilder();
            variant.setStringValue(response.toString());
            attr.setValue(variant.build());
        }

        session.sendResponse(token, response.build());

    }

    private void handleGetAllValuesForAttribute(Session session, int token, byte[] data)
            throws InvalidProtocolBufferException {

        GameUtilitiesServiceProto.GetAllValuesForAttributeRequest request = GameUtilitiesServiceProto.GetAllValuesForAttributeRequest.parseFrom(data);

        String attributeKey = request.getAttributeKey();

        log("GetAllValuesForAttribute from " + session.getAccountInfo() + ": attributeKey=" + attributeKey);

        GameUtilitiesServiceProto.GetAllValuesForAttributeResponse.Builder response = GameUtilitiesServiceProto.GetAllValuesForAttributeResponse.newBuilder();

        session.sendResponse(token, response.build());

    }

    private JsonObject processCommand(Session session, String command, GameUtilitiesServiceProto.ClientRequest request) {

        log(String.format("ProcessCommand from %s: command=%s", session.getAccountInfo(), command));

        return switch (command) {
            case "Command_RealmListTicketRequest_v1_b9" -> handleRealmListRequest(session);
            case "Command_LastCharPlayedRequest_v1_b9" -> handleLastCharPlayedRequest(session);
            case "Command_RealmJoinRequest_v1_b9" -> handleRealmJoinRequest(session, request);
            default -> {
                log("Unknown command: " + command);
                yield null;
            }
        };
    }

    private JsonObject handleRealmListRequest(Session session) {
        JsonObject response = new JsonObject();

        // TODO: Implement realm list retrieval logic and populate the response object

        return response;
    }

    private JsonObject handleLastCharPlayedRequest(Session session) {
        JsonObject response = new JsonObject();

        // TODO: Implement last character played retrieval logic and populate the response object

        return response;
    }

    private JsonObject handleRealmJoinRequest(Session session, GameUtilitiesServiceProto.ClientRequest request) {
        JsonObject response = new JsonObject();

        // TODO: Implement realm join logic based on the request data and populate the response object

        return response;
    }
}
