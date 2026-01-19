package br.net.dd.netherwingcore.bnetserver.server.session;

import br.net.dd.netherwingcore.proto.client.GameUtilitiesServiceProto.*;
import br.net.dd.netherwingcore.proto.client.api.client.v2.AttributeTypesProto.*;

import java.util.Map;

@FunctionalInterface
public interface ClientRequestHandler {
    Integer handle(Map<String, Variant> params, ClientResponse response);
}
