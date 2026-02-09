package br.net.dd.netherwingcore.bnetserver.server;

import io.grpc.*;

import java.util.concurrent.atomic.AtomicInteger;

import static br.net.dd.netherwingcore.common.logging.Log.log;

/**
 * ConnectionInterceptor is a gRPC server interceptor that tracks the number of active client connections.
 * It increments the active client count when a new connection is established and decrements it when the connection is completed or canceled.
 */
public class ConnectionInterceptor implements ServerInterceptor {

    private final AtomicInteger activeClients = new AtomicInteger(0);

    /**
     * Intercepts incoming gRPC calls to track active client connections.
     * Increments the active client count when a new connection is established and decrements it when the connection is completed or canceled.
     *
     * @param call    The gRPC server call being intercepted.
     * @param headers The metadata headers of the gRPC call.
     * @param next    The next server call handler in the interceptor chain.
     * @return A ServerCall.Listener that handles the gRPC call and tracks active client connections.
     */
    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(ServerCall<ReqT, RespT> call, Metadata headers, ServerCallHandler<ReqT, RespT> next) {

        activeClients.incrementAndGet();

        log("New client connected. Total active clients: " + (activeClients.get()));

        ServerCall.Listener<ReqT> listener = next.startCall(call, headers);

        return new ForwardingServerCallListener.SimpleForwardingServerCallListener<>(listener) {
            @Override
            public void onComplete() {
                super.onComplete();
                activeClients.decrementAndGet();
            }

            @Override
            public void onCancel() {
                super.onCancel();
                activeClients.decrementAndGet();
            }
        };
    }

    /**
     * Retrieves the current number of active client connections.
     *
     * @return The number of active clients currently connected to the server.
     */
    public int getActiveClients() {
        return activeClients.get();
    }

}
