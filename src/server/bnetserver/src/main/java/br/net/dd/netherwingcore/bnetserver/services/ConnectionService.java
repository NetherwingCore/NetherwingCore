package br.net.dd.netherwingcore.bnetserver.services;

import br.net.dd.netherwingcore.bnetserver.server.Session;
import br.net.dd.netherwingcore.common.logging.Log;
import br.net.dd.netherwingcore.common.utilities.MessageBuffer;
import br.net.dd.netherwingcore.common.utilities.Util;

import static br.net.dd.netherwingcore.proto.BattlenetRpcErrorCode.*;
import static br.net.dd.netherwingcore.proto.client.ConnectionServiceProto.*;
import static br.net.dd.netherwingcore.proto.client.RpcTypesProto.*;

public class ConnectionService extends ServiceBase {

    private static final Log logger = Log.getLogger(ConnectionService.class.getSimpleName());

    private static final int SERVICE_HASH = 0x65446991;

    private static final int METHOD_CONNECT = 1;
    private static final int METHOD_KEEP_ALIVE = 5;
    private static final int METHOD_REQUEST_DISCONNECT = 7;

    @Override
    public int getServiceHash() {
        return SERVICE_HASH;
    }

    @Override
    public void callServerMethod(Session session, int token, int methodId, MessageBuffer buffer) {
        switch (methodId) {
            case METHOD_CONNECT:
                handleConnect(session, token, buffer);
                break;

            case METHOD_KEEP_ALIVE:
                handleKeepAlive(session, token);
                break;

            case METHOD_REQUEST_DISCONNECT:
                handleRequestDisconnect(session, token, buffer);
                break;

            default:
                logger.warn("{} Unknown method ID {} for ConnectionService",
                        session.getClientInfo(), methodId);
                sendErrorResponse(session, token, ERROR_RPC_NOT_IMPLEMENTED);
        }
    }

    private void handleConnect(Session session, int token, MessageBuffer buffer) {
        ConnectRequest request = parseMessage(buffer, ConnectRequest.newBuilder(), "Connect");

        ConnectResponse.Builder response = ConnectResponse.newBuilder();

        if (request.hasClientId()){
            response.setClientId(request.getClientId());
        }

        response.getServerId().toBuilder().setLabel((int) Util.getPID());
        response.getServerId().toBuilder().setEpoch((int) (System.currentTimeMillis() / 1000));
        response.getServerId().toBuilder().build();
        response.setServerTime(System.currentTimeMillis());

        response.setUseBindlessRpc(request.getUseBindlessRpc());

        sendResponse(session, token, response.build());

        logger.info("{} ✅ Connect response sent (bindless: {}, server time: {})",
                session.getClientInfo(), request.getUseBindlessRpc(), response.getServerTime());
    }

    private void handleKeepAlive(Session session, int token) {
        logger.trace("{} Keep-alive received", session.getClientInfo());

    }

    private void handleRequestDisconnect(Session session, int token, MessageBuffer buffer) {
        DisconnectRequest request = parseMessage(buffer,
                DisconnectRequest.newBuilder(), "RequestDisconnect");

        if (request != null && request.getErrorCode() != 0) {
            logger.info("{} Client requested disconnect with error code: {}",
                    session.getClientInfo(), request.getErrorCode());
        } else {
            logger.info("{} Client requested disconnect", session.getClientInfo());
        }

        // NO_RESPONSE method - don't send response
        session.closeSocket();
    }

}
