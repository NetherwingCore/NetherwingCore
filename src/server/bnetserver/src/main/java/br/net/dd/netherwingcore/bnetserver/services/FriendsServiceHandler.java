package br.net.dd.netherwingcore.bnetserver.services;

import br.net.dd.netherwingcore.bnetserver.server.Session;
import br.net.dd.netherwingcore.proto.BattlenetRpcErrorCode;
import com.google.protobuf.InvalidProtocolBufferException;

public class FriendsServiceHandler implements ServiceHandler {
    @Override
    public void handleMethod(Session session, int methodId, int token, byte[] data)
            throws InvalidProtocolBufferException {
        session.sendError(token, BattlenetRpcErrorCode.ERROR_RPC_NOT_IMPLEMENTED.name());

    }
}
