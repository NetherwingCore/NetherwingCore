package br.net.dd.netherwingcore.bnetserver.services;

import br.net.dd.netherwingcore.bnetserver.server.Session;
import br.net.dd.netherwingcore.proto.BattlenetRpcErrorCode;
import com.google.protobuf.InvalidProtocolBufferException;

import java.util.HashMap;
import java.util.Map;

import static br.net.dd.netherwingcore.common.logging.Log.log;

/**
 * Singleton dispatcher for routing service method calls to their respective handlers
 * in the Battle.net server.
 */
public class ServiceDispatcher {

    private final Map<Integer, ServiceHandler> handlers;

    private static final ServiceDispatcher INSTANCE = new ServiceDispatcher();

    /**
     * Private constructor to enforce singleton pattern.
     */
    private ServiceDispatcher() {
        this.handlers = new HashMap<>();
        registerServices();
    }

    /**
     * Gets the singleton instance of the ServiceDispatcher.
     *
     * @return The ServiceDispatcher instance.
     */
    public static ServiceDispatcher getInstance() {
        return INSTANCE;
    }

    /**
     * Registers all service handlers with their corresponding service hashes.
     */
    private void registerServices() {

        // Authentication Service - Hash: 0xDECFC01
        registerService(0xDECFC01, new AuthenticationServiceHandler());
        // Connection Service - Hash: 0x65446991
        registerService(0x65446991, new ConnectionServiceHandler());
        // Account Service - Hash: 0x62DA0891
        registerService(0x62DA0891, new AccountServiceHandler());
        // Game Utilities Service - Hash: 0x3FC1274D
        registerService(0x3FC1274D, new GameUtilitiesServiceHandler());
        // Friends Service - Hash: 0xA3DDB1BD
        registerService(0xA3DDB1BD, new FriendsServiceHandler());
        // Notification Service - Hash: 0x7CAF61C9
        registerService(0x7CAF61C9, new NotificationServiceHandler());
        // Presence Service - Hash: 0xFA0796FF
        registerService(0xFA0796FF, new PresenceServiceHandler());
        // Resources Service - Hash: 0xECBE75BA
        registerService(0xECBE75BA, new ResourcesServiceHandler());

        log("Registered: " + handlers.size() + " services");
    }

    /**
     * Registers a service handler for a specific service hash.
     *
     * @param serviceHash The hash of the service.
     * @param handler     The handler for the service.
     */
    private void registerService(int serviceHash, ServiceHandler handler) {
        handlers.put(serviceHash, handler);

        log(String.format("Service registered: 0x%08X -> %s", serviceHash, handler.getClass().getSimpleName()));
    }

    /**
     * Dispatches a service method call to the appropriate handler.
     *
     * @param session     The session for which the method is being called.
     * @param serviceHash The hash of the service being called.
     * @param methodId    The ID of the method being called.
     * @param token       The token associated with the method call.
     * @param data        The data payload for the method call.
     */
    public void dispatch(Session session, int serviceHash, int methodId, int token, byte[] data) {

        ServiceHandler handler = handlers.get(serviceHash);

        if (handler == null) {
            session.sendError(token, BattlenetRpcErrorCode.ERROR_RPC_INVALID_SERVICE.name());
            return;
        }

        try {
            handler.handleMethod(session, methodId, token, data);
        } catch (InvalidProtocolBufferException ex) {
            log("InvalidProtocolBufferException: " + ex.getMessage());
            session.sendError(token, BattlenetRpcErrorCode.ERROR_RPC_MALFORMED_REQUEST.name());

        } catch (Exception e) {
            log("Error handling service method: " + e.getMessage());
            session.sendError(token, BattlenetRpcErrorCode.ERROR_RPC_SERVER_ERROR.name());
        }

    }

}
