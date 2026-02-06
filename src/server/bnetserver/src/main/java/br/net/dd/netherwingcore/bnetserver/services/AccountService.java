package br.net.dd.netherwingcore.bnetserver.services;

import br.net.dd.netherwingcore.proto.client.AccountServiceGrpc;
import br.net.dd.netherwingcore.proto.client.AccountServiceProto;
import br.net.dd.netherwingcore.proto.client.RpcTypesProto;
import io.grpc.stub.StreamObserver;

public class AccountService extends AccountServiceGrpc.AccountServiceImplBase {
    @Override
    public void resolveAccount(AccountServiceProto.ResolveAccountRequest request, StreamObserver<AccountServiceProto.ResolveAccountResponse> responseObserver) {
        super.resolveAccount(request, responseObserver);
    }

    @Override
    public void isIgrAddress(AccountServiceProto.IsIgrAddressRequest request, StreamObserver<RpcTypesProto.NoData> responseObserver) {
        super.isIgrAddress(request, responseObserver);
    }

    @Override
    public void subscribe(AccountServiceProto.SubscriptionUpdateRequest request, StreamObserver<AccountServiceProto.SubscriptionUpdateResponse> responseObserver) {
        super.subscribe(request, responseObserver);
    }

    @Override
    public void unsubscribe(AccountServiceProto.SubscriptionUpdateRequest request, StreamObserver<RpcTypesProto.NoData> responseObserver) {
        super.unsubscribe(request, responseObserver);
    }

    @Override
    public void getAccountState(AccountServiceProto.GetAccountStateRequest request, StreamObserver<AccountServiceProto.GetAccountStateResponse> responseObserver) {
        super.getAccountState(request, responseObserver);
    }

    @Override
    public void getGameAccountState(AccountServiceProto.GetGameAccountStateRequest request, StreamObserver<AccountServiceProto.GetGameAccountStateResponse> responseObserver) {
        super.getGameAccountState(request, responseObserver);
    }

    @Override
    public void getLicenses(AccountServiceProto.GetLicensesRequest request, StreamObserver<AccountServiceProto.GetLicensesResponse> responseObserver) {
        super.getLicenses(request, responseObserver);
    }

    @Override
    public void getGameTimeRemainingInfo(AccountServiceProto.GetGameTimeRemainingInfoRequest request, StreamObserver<AccountServiceProto.GetGameTimeRemainingInfoResponse> responseObserver) {
        super.getGameTimeRemainingInfo(request, responseObserver);
    }

    @Override
    public void getGameSessionInfo(AccountServiceProto.GetGameSessionInfoRequest request, StreamObserver<AccountServiceProto.GetGameSessionInfoResponse> responseObserver) {
        super.getGameSessionInfo(request, responseObserver);
    }

    @Override
    public void getCAISInfo(AccountServiceProto.GetCAISInfoRequest request, StreamObserver<AccountServiceProto.GetCAISInfoResponse> responseObserver) {
        super.getCAISInfo(request, responseObserver);
    }

    @Override
    public void getAuthorizedData(AccountServiceProto.GetAuthorizedDataRequest request, StreamObserver<AccountServiceProto.GetAuthorizedDataResponse> responseObserver) {
        super.getAuthorizedData(request, responseObserver);
    }

    @Override
    public void getSignedAccountState(AccountServiceProto.GetSignedAccountStateRequest request, StreamObserver<AccountServiceProto.GetSignedAccountStateResponse> responseObserver) {
        super.getSignedAccountState(request, responseObserver);
    }

    @Override
    public void updateParentalControlsAndCAIS(AccountServiceProto.UpdateParentalControlsAndCAISRequest request, StreamObserver<RpcTypesProto.NoData> responseObserver) {
        super.updateParentalControlsAndCAIS(request, responseObserver);
    }
}
