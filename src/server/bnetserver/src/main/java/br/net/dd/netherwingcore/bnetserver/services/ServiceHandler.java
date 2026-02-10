package br.net.dd.netherwingcore.bnetserver.services;

import br.net.dd.netherwingcore.bnetserver.server.Session;
import com.google.protobuf.InvalidProtocolBufferException;

/**
 * Interface for handling service method calls in the Battle.net server.
 */
public interface ServiceHandler {

    /**
     * Handles a method call for a specific session.
     *
     * @param session  The session for which the method is being called.
     * @param methodId The ID of the method being called.
     * @param token    The token associated with the method call.
     * @param data     The data payload for the method call.
     * @throws InvalidProtocolBufferException If there is an error parsing the data payload.
     */
    void handleMethod(Session session, int methodId, int token, byte[] data)
            throws InvalidProtocolBufferException;

}
