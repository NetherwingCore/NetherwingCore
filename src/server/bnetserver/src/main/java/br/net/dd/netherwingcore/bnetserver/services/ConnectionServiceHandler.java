package br.net.dd.netherwingcore.bnetserver.services;

import br.net.dd.netherwingcore.bnetserver.server.Session;
import br.net.dd.netherwingcore.proto.BattlenetRpcErrorCode;
import br.net.dd.netherwingcore.proto.client.ConnectionServiceProto;
import br.net.dd.netherwingcore.proto.client.RpcTypesProto;
import com.google.protobuf.InvalidProtocolBufferException;

import java.lang.management.ManagementFactory;

import static br.net.dd.netherwingcore.common.logging.Log.log;

/**
 * Handles connection-related RPC methods for the Battle.net server.
 * Manages client connections, disconnections, and keep-alive messages.
 */
public class ConnectionServiceHandler implements ServiceHandler {

    private static final int METHOD_CONNECT = 1;
    private static final int METHOD_BIND = 2;
    private static final int METHOD_ECHO = 3;
    private static final int METHOD_FORCE_DISCONNECT = 4;
    private static final int METHOD_KEEP_ALIVE = 5;
    private static final int METHOD_ENCRYPT = 6;
    private static final int METHOD_REQUEST_DISCONNECT = 7;

    /**
     * Handles incoming RPC method calls for the ConnectionService.
     *
     * @param session  The client session making the request.
     * @param methodId The ID of the method being called.
     * @param token    The token associated with the request for response correlation.
     * @param data     The serialized request data.
     * @throws InvalidProtocolBufferException If the request data cannot be parsed correctly.
     */
    @Override
    public void handleMethod(Session session, int methodId, int token, byte[] data)
            throws InvalidProtocolBufferException {

        int actualMethodId = methodId & 0x3FFFFFFF;

        switch (actualMethodId) {
            case METHOD_CONNECT:
                handleConnect(session, token, data);
                break;
            case METHOD_BIND:
                handleBind(session, token, data);
                break;
            case METHOD_ECHO:
                handleEcho(session, token, data);
                break;
            case METHOD_FORCE_DISCONNECT:
                handleForceDisconnect(session, token, data);
                break;
            case METHOD_KEEP_ALIVE:
                handleKeepAlive(session, token, data);
                break;
            case METHOD_ENCRYPT:
                handleEncrypt(session, token, data);
                break;
            case METHOD_REQUEST_DISCONNECT:
                handleRequestDisconnect(session, token, data);
                break;
            default:
                System.out.println("Unknown method ID: " + actualMethodId);
        }

    }

    /**
     * Handles the Connect RPC method, establishing a connection with the client and sending back connection details.
     *
     * @param session The client session making the request.
     * @param token   The token associated with the request for response correlation.
     * @param data    The serialized ConnectRequest data.
     * @throws InvalidProtocolBufferException If the request data cannot be parsed correctly.
     */
    private void handleConnect(Session session, int token, byte[] data)
            throws InvalidProtocolBufferException {
        ConnectionServiceProto.ConnectRequest request = ConnectionServiceProto.ConnectRequest.parseFrom(data);

        log(String.format(
                "Connecting client - Client ID: %s, Bindless: %s",
                request.hasClientId() ? request.getClientId().getLabel() : "N/A",
                request.getUseBindlessRpc()));

        ConnectionServiceProto.ConnectResponse.Builder response = ConnectionServiceProto.ConnectResponse.newBuilder();
        if (request.hasClientId()) {
            response.setClientId(request.getClientId());
        }

        RpcTypesProto.ProcessId.Builder serverId = RpcTypesProto.ProcessId.newBuilder();
        serverId.setLabel(getProcessId());
        serverId.setEpoch((int) (System.currentTimeMillis() / 1000));
        response.setServerId(serverId);

        response.setServerTime(System.currentTimeMillis());

        response.setUseBindlessRpc(request.getUseBindlessRpc());

        session.sendResponse(token, response.build());

        log("Client connected successfully: " + session.getAccountInfo());
    }

    /**
     * Handles the Bind RPC method, which may be used for associating the session with specific client information or capabilities.
     *
     * @param session The client session making the request.
     * @param token   The token associated with the request for response correlation.
     * @param data    The serialized BindRequest data.
     * @throws InvalidProtocolBufferException If the request data cannot be parsed correctly.
     */
    private void handleBind(Session session, int token, byte[] data)
            throws InvalidProtocolBufferException{
    }

    /**
     * Handles the Echo RPC method, which may be used for testing connectivity and latency between the client and server.
     *
     * @param session The client session making the request.
     * @param token   The token associated with the request for response correlation.
     * @param data    The serialized EchoRequest data.
     * @throws InvalidProtocolBufferException If the request data cannot be parsed correctly.
     */
    private void handleEcho(Session session, int token, byte[] data)
            throws InvalidProtocolBufferException {
    }

    /**
     * Handles the ForceDisconnect RPC method, which may be used by the server to forcibly disconnect a client session for various reasons (e.g., policy violations, maintenance).
     *
     * @param session The client session to be disconnected.
     * @param token   The token associated with the request for response correlation.
     * @param data    The serialized ForceDisconnectRequest data.
     * @throws InvalidProtocolBufferException If the request data cannot be parsed correctly.
     */
    private void handleForceDisconnect(Session session, int token, byte[] data)
            throws InvalidProtocolBufferException{
    }

    /**
     * Handles the KeepAlive RPC method, which is used by the client to maintain an active connection with the server and prevent timeouts.
     *
     * @param session The client session making the request.
     * @param token   The token associated with the request for response correlation.
     * @param data    The serialized KeepAliveRequest data.
     * @throws InvalidProtocolBufferException If the request data cannot be parsed correctly.
     */
    private void handleKeepAlive(Session session, int token, byte[] data)
            throws InvalidProtocolBufferException {
        session.sendResponse(token, null);
    }

    /**
     * Handles the Encrypt RPC method, which may be used for establishing encryption parameters or exchanging keys between the client and server to secure communication.
     *
     * @param session The client session making the request.
     * @param token   The token associated with the request for response correlation.
     * @param data    The serialized EncryptRequest data.
     * @throws InvalidProtocolBufferException If the request data cannot be parsed correctly.
     */
    private void handleEncrypt(Session session, int token, byte[] data)
            throws InvalidProtocolBufferException{
    }

    /**
     * Handles the RequestDisconnect RPC method, which is used by the client to request disconnection from the server, optionally providing an error code for the reason of disconnection.
     *
     * @param session The client session making the request.
     * @param token   The token associated with the request for response correlation.
     * @param data    The serialized DisconnectRequest data.
     * @throws InvalidProtocolBufferException If the request data cannot be parsed correctly.
     */
    private void handleRequestDisconnect(Session session, int token, byte[] data)
            throws InvalidProtocolBufferException{
        ConnectionServiceProto.DisconnectRequest request = ConnectionServiceProto.DisconnectRequest.parseFrom(data);

        log(String.format(
                "Client %s requested disconnection - Code: %d",
                session.getAccountInfo(),
                request.getErrorCode()));

        ConnectionServiceProto.DisconnectNotification.Builder response = ConnectionServiceProto.DisconnectNotification.newBuilder();
        response.setErrorCode(request.getErrorCode());

        session.sendResponse(token, response.build());

        session.close();
    }

    /**
     * Retrieves the current process ID of the server, which can be used for identifying the server instance in client-server communication.
     *
     * @return The process ID of the server.
     */
    private int getProcessId() {
        String processName = ManagementFactory.getRuntimeMXBean().getName();
        return Integer.parseInt(processName.split("@")[0]);
    }

}
