package br.net.dd.netherwingcore.bnetserver.services;

import br.net.dd.netherwingcore.proto.client.AuthenticationServiceGrpc;
import br.net.dd.netherwingcore.proto.client.AuthenticationServiceProto;
import br.net.dd.netherwingcore.proto.client.EntityTypesProto;
import br.net.dd.netherwingcore.proto.client.RpcTypesProto;
import io.grpc.stub.StreamObserver;

public class AuthenticationService extends AuthenticationServiceGrpc.AuthenticationServiceImplBase {
    @Override
    public void logon(AuthenticationServiceProto.LogonRequest request, StreamObserver<RpcTypesProto.NoData> responseObserver) {
        super.logon(request, responseObserver);
    }

    @Override
    public void moduleNotify(AuthenticationServiceProto.ModuleNotification request, StreamObserver<RpcTypesProto.NoData> responseObserver) {
        super.moduleNotify(request, responseObserver);
    }

    @Override
    public void moduleMessage(AuthenticationServiceProto.ModuleMessageRequest request, StreamObserver<RpcTypesProto.NoData> responseObserver) {
        super.moduleMessage(request, responseObserver);
    }

    @Override
    public void selectGameAccountDEPRECATED(EntityTypesProto.EntityId request, StreamObserver<RpcTypesProto.NoData> responseObserver) {
        super.selectGameAccountDEPRECATED(request, responseObserver);
    }

    @Override
    public void generateSSOToken(AuthenticationServiceProto.GenerateSSOTokenRequest request, StreamObserver<AuthenticationServiceProto.GenerateSSOTokenResponse> responseObserver) {
        super.generateSSOToken(request, responseObserver);
    }

    @Override
    public void selectGameAccount(AuthenticationServiceProto.SelectGameAccountRequest request, StreamObserver<RpcTypesProto.NoData> responseObserver) {
        super.selectGameAccount(request, responseObserver);
    }

    @Override
    public void verifyWebCredentials(AuthenticationServiceProto.VerifyWebCredentialsRequest request, StreamObserver<RpcTypesProto.NoData> responseObserver) {
        super.verifyWebCredentials(request, responseObserver);
    }

    @Override
    public void generateWebCredentials(AuthenticationServiceProto.GenerateWebCredentialsRequest request, StreamObserver<AuthenticationServiceProto.GenerateWebCredentialsResponse> responseObserver) {
        super.generateWebCredentials(request, responseObserver);
    }
}
