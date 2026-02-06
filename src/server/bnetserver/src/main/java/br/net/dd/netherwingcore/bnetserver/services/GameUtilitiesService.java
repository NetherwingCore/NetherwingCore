package br.net.dd.netherwingcore.bnetserver.services;

import br.net.dd.netherwingcore.proto.client.GameUtilitiesServiceGrpc;
import br.net.dd.netherwingcore.proto.client.GameUtilitiesServiceProto;
import br.net.dd.netherwingcore.proto.client.RpcTypesProto;
import io.grpc.stub.StreamObserver;

public class GameUtilitiesService extends GameUtilitiesServiceGrpc.GameUtilitiesServiceImplBase {
    @Override
    public void processClientRequest(GameUtilitiesServiceProto.ClientRequest request, StreamObserver<GameUtilitiesServiceProto.ClientResponse> responseObserver) {
        super.processClientRequest(request, responseObserver);
    }

    @Override
    public void presenceChannelCreated(GameUtilitiesServiceProto.PresenceChannelCreatedRequest request, StreamObserver<RpcTypesProto.NoData> responseObserver) {
        super.presenceChannelCreated(request, responseObserver);
    }

    @Override
    public void getPlayerVariables(GameUtilitiesServiceProto.GetPlayerVariablesRequest request, StreamObserver<GameUtilitiesServiceProto.GetPlayerVariablesResponse> responseObserver) {
        super.getPlayerVariables(request, responseObserver);
    }

    @Override
    public void processServerRequest(GameUtilitiesServiceProto.ServerRequest request, StreamObserver<GameUtilitiesServiceProto.ServerResponse> responseObserver) {
        super.processServerRequest(request, responseObserver);
    }

    @Override
    public void onGameAccountOnline(GameUtilitiesServiceProto.GameAccountOnlineNotification request, StreamObserver<RpcTypesProto.NO_RESPONSE> responseObserver) {
        super.onGameAccountOnline(request, responseObserver);
    }

    @Override
    public void onGameAccountOffline(GameUtilitiesServiceProto.GameAccountOfflineNotification request, StreamObserver<RpcTypesProto.NO_RESPONSE> responseObserver) {
        super.onGameAccountOffline(request, responseObserver);
    }

    @Override
    public void getAchievementsFile(GameUtilitiesServiceProto.GetAchievementsFileRequest request, StreamObserver<GameUtilitiesServiceProto.GetAchievementsFileResponse> responseObserver) {
        super.getAchievementsFile(request, responseObserver);
    }

    @Override
    public void getAllValuesForAttribute(GameUtilitiesServiceProto.GetAllValuesForAttributeRequest request, StreamObserver<GameUtilitiesServiceProto.GetAllValuesForAttributeResponse> responseObserver) {
        super.getAllValuesForAttribute(request, responseObserver);
    }
}
