package br.net.dd.netherwingcore.bnetserver.services;

import br.net.dd.netherwingcore.bnetserver.server.Session;
import br.net.dd.netherwingcore.common.logging.Log;
import br.net.dd.netherwingcore.common.utilities.MessageBuffer;

import static br.net.dd.netherwingcore.proto.BattlenetRpcErrorCode.*;
import static br.net.dd.netherwingcore.proto.client.ConnectionServiceProto.*;
import static br.net.dd.netherwingcore.proto.client.RpcTypesProto.*;

/**
 * The `ConnectionService` class is responsible for handling client connection requests and maintaining the connection state.
 * It processes incoming connection requests, authenticates clients, and manages keep-alive messages to ensure that the connection remains active.
 * This service is crucial for establishing and maintaining communication between the client and the server.
 */
public class ConnectionService extends ServiceBase {

    private static final Log logger = Log.getLogger(ConnectionService.class.getSimpleName());

    private static final int SERVICE_HASH = 0x65446991;

    private static final int METHOD_CONNECT = 1;
    private static final int METHOD_KEEP_ALIVE = 5;

    /**
     * The `SERVICE_HASH` is a unique identifier for the Connection Service, used to route incoming requests to the correct service handler.
     * The `METHOD_CONNECT` and `METHOD_KEEP_ALIVE` constants represent the method IDs for handling connection requests and keep-alive messages, respectively.
     * These method IDs are used in the `callServerMethod` function to determine which handler to invoke based on the incoming request.
     */
    @Override
    public int getServiceHash() {
        return SERVICE_HASH;
    }

    /**
     * The `callServerMethod` function is the entry point for handling incoming RPC calls to the Connection Service.
     * It takes the session, token, method ID, and message buffer as parameters and routes the call to the appropriate handler based on the method ID.
     * If an unknown method ID is received, it responds with an error indicating that the RPC method is not implemented.
     *
     * @param session The session associated with the client making the request.
     * @param token   The token for correlating requests and responses.
     * @param methodId The ID of the method being called.
     * @param buffer  The message buffer containing the request data.
     */
    @Override
    public void callServerMethod(Session session, int token, int methodId, MessageBuffer buffer) {
        switch (methodId) {
            case METHOD_CONNECT:
                handleConnect(session, token, buffer);
                break;
            case METHOD_KEEP_ALIVE:
                handleKeepAlive(session, token);
                break;
            default:
                sendErrorResponse(session, token, ERROR_RPC_NOT_IMPLEMENTED);
        }
    }

    /**
     * The `handleConnect` method processes incoming connection requests from clients.
     * It parses the request, validates it, and responds with the appropriate connection information.
     * If the request is malformed, it sends an error response back to the client.
     * Upon successful processing, it logs the connection and sends a response containing server information and connection parameters.
     *
     * @param session The session associated with the client making the connection request.
     * @param token   The token for correlating requests and responses.
     * @param buffer  The message buffer containing the connection request data.
     */
    private void handleConnect(Session session, int token, MessageBuffer buffer) {
        ConnectRequest request = parseMessage(buffer, ConnectRequest.newBuilder(), "Connect");
        if (request == null) {
            sendErrorResponse(session, token, ERROR_RPC_MALFORMED_REQUEST);
            return;
        }

        long now = System.currentTimeMillis();

        ConnectResponse.Builder responseBuilder = ConnectResponse.newBuilder();

        if (request.hasClientId()) {
            responseBuilder.setClientId(request.getClientId());
        }

        ProcessId.Builder serverIdBuilder = ProcessId.newBuilder();
        serverIdBuilder.setLabel((int) ProcessHandle.current().pid());
        serverIdBuilder.setEpoch((int) (now / 1000));

        responseBuilder.setServerId(serverIdBuilder.build());
        responseBuilder.setServerTime(now);
        responseBuilder.setUseBindlessRpc(request.getUseBindlessRpc());

        sendResponse(session, token, responseBuilder.build());

        logger.debug("{} Connected successfully", session.getClientInfo());
    }

    /**
     * The `handleConnect` method is called when a client attempts to establish a connection with the server.
     * It should process the connection request, authenticate the client, and configure the session appropriately.
     * However, since the specific implementation depends on your server's requirements and the communication protocol,
     * I've left this method empty so you can fill it in as needed.
     */
    private void handleKeepAlive(Session session, int token) {
        // Simply respond with success to keep the connection alive
        sendErrorResponse(session, token, ERROR_OK);
    }
}
