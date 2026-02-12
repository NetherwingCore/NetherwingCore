package br.net.dd.netherwingcore.bnetserver.services;

import br.net.dd.netherwingcore.bnetserver.server.Session;
import br.net.dd.netherwingcore.common.logging.Log;
import br.net.dd.netherwingcore.common.utilities.MessageBuffer;
import com.google.protobuf.ByteString;

import java.nio.charset.StandardCharsets;
import java.util.zip.Deflater;

import static br.net.dd.netherwingcore.proto.BattlenetRpcErrorCode.*;
import static br.net.dd.netherwingcore.proto.client.GameUtilitiesServiceProto.*;
import static br.net.dd.netherwingcore.proto.client.AttributeTypesProto.*;

/**
 * Service implementation for handling game utility related RPC calls.
 */
public class GameUtilitiesService extends ServiceBase {

    private static final Log logger = Log.getLogger(GameUtilitiesService.class.getSimpleName());

    private static final int SERVICE_HASH = 0x3FC1274D;

    private static final int METHOD_PROCESS_CLIENT_REQUEST = 1;
    private static final int METHOD_GET_ALL_VALUES_FOR_ATTRIBUTE = 10;

    /**
     * Returns the unique service hash for the GameUtilitiesService.
     *
     * @return The service hash as an integer.
     */
    @Override
    public int getServiceHash() {
        return SERVICE_HASH;
    }

    /**
     * Handles incoming RPC calls to the GameUtilitiesService.
     *
     * @param session The session of the client making the call.
     * @param token The unique token for the RPC call.
     * @param methodId The ID of the method being called.
     * @param buffer The message buffer containing any parameters for the call.
     */
    @Override
    public void callServerMethod(Session session, int token, int methodId, MessageBuffer buffer) {
        if (!session.isAuthenticated()) {
            sendErrorResponse(session, token, ERROR_DENIED);
            return;
        }

        switch (methodId) {
            case METHOD_PROCESS_CLIENT_REQUEST:
                handleProcessClientRequest(session, token, buffer);
                break;
            case METHOD_GET_ALL_VALUES_FOR_ATTRIBUTE:
                handleGetAllValuesForAttribute(session, token, buffer);
                break;
            default:
                logger.warn("Unknown method ID {} for GameUtilitiesService", String.valueOf(methodId));
                sendErrorResponse(session, token, ERROR_RPC_NOT_IMPLEMENTED);
        }
    }

    /**
     * Handles the ProcessClientRequest method, which processes various client requests based on the command type and parameters.
     *
     * @param session The session of the client making the request.
     * @param token The unique token for the RPC call.
     * @param buffer The message buffer containing the ClientRequest parameters.
     */
    private void handleProcessClientRequest(Session session, int token, MessageBuffer buffer) {
        ClientRequest request = parseMessage(buffer, ClientRequest.newBuilder(), "ProcessClientRequest");
        if (request == null) {
            sendErrorResponse(session, token, ERROR_RPC_MALFORMED_REQUEST);
            return;
        }

        // TODO: Implement logic to process the client request based on the command type and parameters.
        // Here you would process different commands:
        // - Command_RealmListRequest_v1
        // - Command_RealmJoinRequest_v1
        // - Command_LastCharPlayedRequest_v1

        ClientResponse.Builder responseBuilder = ClientResponse.newBuilder();

        // For demonstration, we will just return a realm list response.
        String realmListJson = buildRealmListJson();
        byte[] compressedData = compressData(realmListJson.getBytes(StandardCharsets.UTF_8));

        Attribute.Builder attributeBuilder = Attribute.newBuilder()
                .setName("Param_RealmList")
                .setValue(
                        Variant.newBuilder().setBlobValue(ByteString.copyFrom(compressedData)).build()
                );

        responseBuilder.addAttribute(attributeBuilder.build());

        sendResponse(session, token, responseBuilder.build());

    }

    /**
     * Handles the GetAllValuesForAttribute method, which retrieves all values for a specified attribute key.
     *
     * @param session The session of the client making the request.
     * @param token The unique token for the RPC call.
     * @param buffer The message buffer containing the GetAllValuesForAttributeRequest parameters.
     */
    private void handleGetAllValuesForAttribute(Session session, int token, MessageBuffer buffer) {
        GetAllValuesForAttributeRequest request = parseMessage(buffer, GetAllValuesForAttributeRequest.newBuilder(), "GetAllValuesForAttribute");
        if (request == null) {
            sendErrorResponse(session, token, ERROR_RPC_MALFORMED_REQUEST);
            return;
        }

        GetAllValuesForAttributeResponse.Builder responseBuilder = GetAllValuesForAttributeResponse.newBuilder();

        // Example: return sub-regions
        if (request.getAttributeKey().contains("Command_RealmListRequest_v1")) {
            // Add available sub-regions
            Variant.Builder variantBuilder = Variant.newBuilder()
                    .setStringValue("1-0-0");  // Example sub-region

            responseBuilder.addAttributeValue(variantBuilder.build());
        }

        sendResponse(session, token, responseBuilder.build());
    }

    private String buildRealmListJson() {
        return """
            {
                "updates": [{
                    "wowRealmAddress": 1,
                    "cfgTimezonesID": 1,
                    "populationState": 1,
                    "cfgCategoriesID": 1,
                    "version": {
                        "versionMajor": 10,
                        "versionMinor": 2,
                        "versionRevision": 7,
                        "versionBuild": 54904
                    },
                    "cfgRealmsID": 1,
                    "flags": 0,
                    "name": "Test Realm",
                    "cfgConfigsID": 1,
                    "cfgLanguagesID": 1
                }]
            }
            """;
    }

    private byte[] compressData(byte[] data) {
        Deflater deflater = new Deflater();
        deflater.setInput(data);
        deflater.finish();

        byte[] buffer = new byte[data.length * 2];
        int compressedSize = deflater.deflate(buffer);
        deflater.end();

        byte[] result = new byte[compressedSize + 4];
        // Add uncompressed size at the beginning.
        result[0] = (byte) (data.length & 0xFF);
        result[1] = (byte) ((data.length >> 8) & 0xFF);
        result[2] = (byte) ((data.length >> 16) & 0xFF);
        result[3] = (byte) ((data.length >> 24) & 0xFF);

        System.arraycopy(buffer, 0, result, 4, compressedSize);

        return result;
    }
}
