package br.net.dd.netherwingcore.bnetserver.services;

import br.net.dd.netherwingcore.proto.client.GameUtilitiesServiceGrpc;
import br.net.dd.netherwingcore.proto.client.GameUtilitiesServiceProto;
import br.net.dd.netherwingcore.proto.client.RpcTypesProto;
import io.grpc.stub.StreamObserver;

import static br.net.dd.netherwingcore.common.logging.Log.log;

public class GameUtilitiesService extends GameUtilitiesServiceGrpc.GameUtilitiesServiceImplBase {
    @Override
    public void processClientRequest(GameUtilitiesServiceProto.ClientRequest request, StreamObserver<GameUtilitiesServiceProto.ClientResponse> responseObserver) {
        log("Received ClientRequest: " + request);
        super.processClientRequest(request, responseObserver);
    }

    @Override
    public void presenceChannelCreated(GameUtilitiesServiceProto.PresenceChannelCreatedRequest request, StreamObserver<RpcTypesProto.NoData> responseObserver) {
        log("Received PresenceChannelCreated: " + request);
        super.presenceChannelCreated(request, responseObserver);
    }

    @Override
    public void getPlayerVariables(GameUtilitiesServiceProto.GetPlayerVariablesRequest request, StreamObserver<GameUtilitiesServiceProto.GetPlayerVariablesResponse> responseObserver) {
        log("Received GetPlayerVariables: " + request);
        super.getPlayerVariables(request, responseObserver);
    }

    @Override
    public void processServerRequest(GameUtilitiesServiceProto.ServerRequest request, StreamObserver<GameUtilitiesServiceProto.ServerResponse> responseObserver) {
        log("Received ServerRequest: " + request);
        super.processServerRequest(request, responseObserver);
    }

    @Override
    public void onGameAccountOnline(GameUtilitiesServiceProto.GameAccountOnlineNotification request, StreamObserver<RpcTypesProto.NO_RESPONSE> responseObserver) {
        log("Received onGameAccountOnline: " + request);
        super.onGameAccountOnline(request, responseObserver);
    }

    @Override
    public void onGameAccountOffline(GameUtilitiesServiceProto.GameAccountOfflineNotification request, StreamObserver<RpcTypesProto.NO_RESPONSE> responseObserver) {
        log("Received onGameAccountOffline: " + request);
        super.onGameAccountOffline(request, responseObserver);
    }

    @Override
    public void getAchievementsFile(GameUtilitiesServiceProto.GetAchievementsFileRequest request, StreamObserver<GameUtilitiesServiceProto.GetAchievementsFileResponse> responseObserver) {
        log("Received GetAchievementsFile: " + request);
        super.getAchievementsFile(request, responseObserver);
    }

    @Override
    public void getAllValuesForAttribute(GameUtilitiesServiceProto.GetAllValuesForAttributeRequest request, StreamObserver<GameUtilitiesServiceProto.GetAllValuesForAttributeResponse> responseObserver) {
        log("Received GetAllValuesForAttribute: " + request);
        super.getAllValuesForAttribute(request, responseObserver);
    }
}
