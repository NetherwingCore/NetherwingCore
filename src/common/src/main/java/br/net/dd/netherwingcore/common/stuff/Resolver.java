package br.net.dd.netherwingcore.common.stuff;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.Optional;

public class Resolver {

    public Resolver() {
        // No specific implementation is required in the constructor for this example
    }

    /**
     * Resolves a host and service (port) into an endpoint.
     *
     * @param protocol The protocol (not directly used here, as Java doesn't differentiate at this level).
     * @param host The hostname or IP address to be resolved.
     * @param service The port number in String format.
     * @return An Optional containing the InetSocketAddress (endpoint) if the resolution is successful, or an empty Optional otherwise.
     */
    public Optional<InetSocketAddress> resolve(String protocol, String host, String service) {
        try {
            // Convert the service (port) to an integer
            int port = Integer.parseInt(service);

            // Resolve the address using the hostname
            InetAddress address = InetAddress.getByName(host);

            // Return the result as an InetSocketAddress
            return Optional.of(new InetSocketAddress(address, port));
        } catch (UnknownHostException | NumberFormatException e) {
            // Error resolving the hostname or converting the port, logging or handling can be done here
            return Optional.empty();
        }
    }

}
