package br.net.dd.netherwingcore.bnetserver.services;

import br.net.dd.netherwingcore.bnetserver.server.Session;
import br.net.dd.netherwingcore.common.utilities.MessageBuffer;
import br.net.dd.netherwingcore.proto.BattlenetRpcErrorCode;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;

import static br.net.dd.netherwingcore.common.logging.Log.log;

public abstract class ServiceBase {

    /**
     * Base class for all services. Each service must implement the getServiceHash method to return its unique hash,
     * and the callServerMethod to handle incoming RPC calls.
     */
    public abstract int getServiceHash();

    /**
     * Handles an incoming RPC call for this service. The methodId identifies which method is being called,
     * and the buffer contains the serialized request data.
     *
     * @param session the session from which the call originated
     * @param token   the authentication token associated with the call
     * @param methodId the ID of the method being called
     * @param buffer  the buffer containing the serialized request data
     */
    public abstract void callServerMethod(Session session, int token, int methodId, MessageBuffer buffer);

    /**
     * Utility method to parse a protobuf message from a MessageBuffer. It takes a protobuf Message.Builder
     * and attempts to merge the data from the buffer into it. If successful, it builds and returns the message.
     * If parsing fails, it logs an error and returns null.
     *
     * @param buffer the buffer containing the serialized message data
     * @param builder the protobuf Message.Builder to use for parsing
     * @param methodName the name of the method being parsed (for logging purposes)
     * @param <T> the type of protobuf message being parsed
     * @return the parsed protobuf message, or null if parsing failed
     */
    protected <T extends Message> T parseMessage(MessageBuffer buffer, Message.Builder builder, String methodName) {
        try {
            builder.mergeFrom(buffer.toArray());
            @SuppressWarnings("unchecked")
            T message = (T) builder.build();
            return message;
        } catch (InvalidProtocolBufferException e) {
            log("Failed to parse " + methodName + " request: " + e.getMessage());
            return null;
        }
    }

    /**
     * Utility method to send a protobuf message as a response to the client. It takes the session to send the response to,
     * the authentication token, and the protobuf message to send. It serializes the message and sends it using the session's
     * sendResponse method.
     *
     * @param session the session to send the response to
     * @param token   the authentication token associated with the response
     * @param response the protobuf message to send as a response
     */
    protected void sendResponse(Session session, int token, Message response) {
        session.sendResponse(token, response);
    }

    /**
     * Utility method to send an error response to the client. It takes the session to send the response to,
     * the authentication token, and the error code to send. It uses the session's sendResponse method to send the error code.
     *
     * @param session the session to send the error response to
     * @param token   the authentication token associated with the error response
     * @param errorCode the error code to send
     */
    protected void sendErrorResponse(Session session, int token, BattlenetRpcErrorCode errorCode) {
        session.sendResponse(token, errorCode.getValue());
    }

}
