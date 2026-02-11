package br.net.dd.netherwingcore.bnetserver.services;

import br.net.dd.netherwingcore.bnetserver.server.Session;
import br.net.dd.netherwingcore.common.utilities.MessageBuffer;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static br.net.dd.netherwingcore.common.logging.Log.log;

/**
 * The ServiceDispatcher is responsible for routing incoming RPC calls to the appropriate service based on the service hash.
 * It maintains a mapping of service hashes to ServiceMethod instances, which are functional interfaces that call the appropriate
 * method on the service. When a call is dispatched, it looks up the service method using the service hash and invokes it with
 * the session, token, method ID, and packet buffer.
 */
public class ServiceDispatcher {

    private static final ServiceDispatcher INSTANCE = new ServiceDispatcher();

    private final Map<Integer, ServiceMethod> dispatchers;

    /**
     * The ServiceDispatcher is initialized as a singleton instance. During initialization, it creates a concurrent hash map to store
     * the service dispatchers and registers all available services by calling the registerServices method.
     * Each service is added to the dispatchers map with its unique service hash as the key and a lambda that
     */
    private ServiceDispatcher() {
        this.dispatchers = new ConcurrentHashMap<>();
        registerServices();
    }

    /**
     * Returns the singleton instance of the ServiceDispatcher.
     *
     * @return the singleton instance of the ServiceDispatcher
     */
    public static ServiceDispatcher getInstance() {
        return INSTANCE;
    }

    /**
     * Registers all available services by creating instances of each service and adding them to the dispatchers map.
     * Each service is added with its unique service hash as the key and a lambda that calls the service's callServerMethod
     * as the value. This allows for efficient routing of incoming RPC calls to the appropriate service based on the service hash.
     */
    private void registerServices() {
        addService(new AuthenticationService());
        addService(new ConnectionService());
        addService(new GameUtilitiesService());

        log("ServiceDispatcher initialized with " + dispatchers.size() + " services.");
    }

    /**
     * Adds a service to the dispatchers map. It takes a service instance, retrieves its unique service hash, and adds a lambda
     * that calls the service's callServerMethod to the dispatchers map with the service hash as the key. This allows for efficient
     * routing of incoming RPC calls to the appropriate service based on the service hash.
     *
     * @param service the service instance to add to the dispatchers map
     * @param <T> the type of the service being added
     */
    private <T extends ServiceBase> void addService(T service) {
        int serviceHash = service.getServiceHash();
        dispatchers.put(serviceHash, service::callServerMethod);

        log("Registered service: " + service.getClass().getSimpleName() + " (hash: 0x" + Integer.toHexString(serviceHash) + ")");
    }

    /**
     * Dispatches an incoming RPC call to the appropriate service based on the service hash. It takes the session from which the call
     * originated, the authentication token associated with the call, the method ID of the method being called, and the buffer containing
     * the serialized request data. It looks up the service method using the service hash and invokes it with the session, token, method ID,
     * and packet buffer. If no service is found for the given service hash, it logs an error message.
     *
     * @param session the session from which the call originated
     * @param serviceHash the unique hash of the service being called
     * @param token   the authentication token associated with the call
     * @param methodId the ID of the method being called
     * @param buffer  the buffer containing the serialized request data
     */
    public void dispatch(Session session, int serviceHash, int token, int methodId, MessageBuffer buffer) {
        ServiceMethod method = dispatchers.get(serviceHash);
        if (method != null) {
            method.call(session, token, methodId, buffer);
        } else {
            log("No service found for hash: 0x" + Integer.toHexString(serviceHash));
        }
    }

    /**
     * The ServiceDispatcher is responsible for routing incoming RPC calls to the appropriate service based on the service hash.
     * It maintains a mapping of service hashes to ServiceMethod instances, which are functional interfaces that call the appropriate
     * method on the service. When a call is dispatched, it looks up the service method using the service hash and invokes it with
     * the session, token, method ID, and packet buffer.
     */
    @FunctionalInterface
    private interface ServiceMethod {
        void call(Session session, int token, int methodId, MessageBuffer buffer);
    }
}
