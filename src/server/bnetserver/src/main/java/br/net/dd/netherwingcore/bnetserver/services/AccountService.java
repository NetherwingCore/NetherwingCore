package br.net.dd.netherwingcore.bnetserver.services;

import br.net.dd.netherwingcore.proto.client.AccountServiceGrpc;
import br.net.dd.netherwingcore.proto.client.AccountServiceProto;
import br.net.dd.netherwingcore.proto.client.RpcTypesProto;
import io.grpc.stub.StreamObserver;

import static br.net.dd.netherwingcore.common.logging.Log.log;

public class AccountService extends AccountServiceGrpc.AccountServiceImplBase {
    @Override
    public void resolveAccount(AccountServiceProto.ResolveAccountRequest request, StreamObserver<AccountServiceProto.ResolveAccountResponse> responseObserver) {
        log("Received ResolveAccount request: " + request);
        super.resolveAccount(request, responseObserver);
    }

    @Override
    public void isIgrAddress(AccountServiceProto.IsIgrAddressRequest request, StreamObserver<RpcTypesProto.NoData> responseObserver) {
        log("Received IsIgrAddress request: " + request);
        super.isIgrAddress(request, responseObserver);
    }

    @Override
    public void subscribe(AccountServiceProto.SubscriptionUpdateRequest request, StreamObserver<AccountServiceProto.SubscriptionUpdateResponse> responseObserver) {
        log("Received SubscriptionUpdateRequest request: " + request);
        super.subscribe(request, responseObserver);
    }

    @Override
    public void unsubscribe(AccountServiceProto.SubscriptionUpdateRequest request, StreamObserver<RpcTypesProto.NoData> responseObserver) {
        log("Received SubscriptionUpdateRequest request: " + request);
        super.unsubscribe(request, responseObserver);
    }

    @Override
    public void getAccountState(AccountServiceProto.GetAccountStateRequest request, StreamObserver<AccountServiceProto.GetAccountStateResponse> responseObserver) {
        log("Received GetAccountStateRequest request: " + request);
        super.getAccountState(request, responseObserver);
    }

    @Override
    public void getGameAccountState(AccountServiceProto.GetGameAccountStateRequest request, StreamObserver<AccountServiceProto.GetGameAccountStateResponse> responseObserver) {
        log("Received GetGameAccountStateRequest request: " + request);
        super.getGameAccountState(request, responseObserver);
    }

    @Override
    public void getLicenses(AccountServiceProto.GetLicensesRequest request, StreamObserver<AccountServiceProto.GetLicensesResponse> responseObserver) {
        log("Received GetLicensesRequest request: " + request);
        super.getLicenses(request, responseObserver);
    }

    @Override
    public void getGameTimeRemainingInfo(AccountServiceProto.GetGameTimeRemainingInfoRequest request, StreamObserver<AccountServiceProto.GetGameTimeRemainingInfoResponse> responseObserver) {
        log("Received GetGameTimeRemainingInfoRequest request: " + request);
        super.getGameTimeRemainingInfo(request, responseObserver);
    }

    @Override
    public void getGameSessionInfo(AccountServiceProto.GetGameSessionInfoRequest request, StreamObserver<AccountServiceProto.GetGameSessionInfoResponse> responseObserver) {
        log("Received GetGameSessionInfoRequest request: " + request);
        super.getGameSessionInfo(request, responseObserver);
    }

    @Override
    public void getCAISInfo(AccountServiceProto.GetCAISInfoRequest request, StreamObserver<AccountServiceProto.GetCAISInfoResponse> responseObserver) {
        log("Received GetCAISInfoRequest request: " + request);
        super.getCAISInfo(request, responseObserver);
    }

    @Override
    public void getAuthorizedData(AccountServiceProto.GetAuthorizedDataRequest request, StreamObserver<AccountServiceProto.GetAuthorizedDataResponse> responseObserver) {
        log("Received GetAuthorizedDataRequest request: " + request);
        super.getAuthorizedData(request, responseObserver);
    }

    @Override
    public void getSignedAccountState(AccountServiceProto.GetSignedAccountStateRequest request, StreamObserver<AccountServiceProto.GetSignedAccountStateResponse> responseObserver) {
        log("Received GetSignedAccountStateRequest request: " + request);
        super.getSignedAccountState(request, responseObserver);
    }

    @Override
    public void updateParentalControlsAndCAIS(AccountServiceProto.UpdateParentalControlsAndCAISRequest request, StreamObserver<RpcTypesProto.NoData> responseObserver) {
        log("Received UpdateParentalControlsAndCAIS request: " + request);
        super.updateParentalControlsAndCAIS(request, responseObserver);
    }
}
