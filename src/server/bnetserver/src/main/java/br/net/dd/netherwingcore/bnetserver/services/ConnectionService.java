package br.net.dd.netherwingcore.bnetserver.services;

import br.net.dd.netherwingcore.proto.client.ConnectionServiceGrpc;
import br.net.dd.netherwingcore.proto.client.ConnectionServiceProto;
import br.net.dd.netherwingcore.proto.client.RpcTypesProto;
import io.grpc.stub.StreamObserver;

public class ConnectionService extends ConnectionServiceGrpc.ConnectionServiceImplBase {
    @Override
    public void connect(ConnectionServiceProto.ConnectRequest request, StreamObserver<ConnectionServiceProto.ConnectResponse> responseObserver) {
        super.connect(request, responseObserver);
    }

    @Override
    public void bind(ConnectionServiceProto.BindRequest request, StreamObserver<ConnectionServiceProto.BindResponse> responseObserver) {
        super.bind(request, responseObserver);
    }

    @Override
    public void echo(ConnectionServiceProto.EchoRequest request, StreamObserver<ConnectionServiceProto.EchoResponse> responseObserver) {
        super.echo(request, responseObserver);
    }

    @Override
    public void forceDisconnect(ConnectionServiceProto.DisconnectNotification request, StreamObserver<RpcTypesProto.NO_RESPONSE> responseObserver) {
        super.forceDisconnect(request, responseObserver);
    }

    @Override
    public void keepAlive(RpcTypesProto.NoData request, StreamObserver<RpcTypesProto.NO_RESPONSE> responseObserver) {
        super.keepAlive(request, responseObserver);
    }

    @Override
    public void encrypt(ConnectionServiceProto.EncryptRequest request, StreamObserver<RpcTypesProto.NoData> responseObserver) {
        super.encrypt(request, responseObserver);
    }

    @Override
    public void requestDisconnect(ConnectionServiceProto.DisconnectRequest request, StreamObserver<RpcTypesProto.NO_RESPONSE> responseObserver) {
        super.requestDisconnect(request, responseObserver);
    }
}
