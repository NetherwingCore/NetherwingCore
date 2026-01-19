package br.net.dd.netherwingcore.bnetserver.server;

import br.net.dd.netherwingcore.common.utilities.MessageBuffer;
import br.net.dd.netherwingcore.database.QueryCallbackProcessor;
import br.net.dd.netherwingcore.proto.client.AccountServiceProto.*;
import br.net.dd.netherwingcore.proto.client.AuthenticationServiceProto.*;
import br.net.dd.netherwingcore.proto.client.GameUtilitiesServiceProto.*;
import br.net.dd.netherwingcore.proto.client.api.client.v2.AttributeTypesProto.*;
import com.google.protobuf.Message;

import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class Battlenet {

    public class Session {

        private MessageBuffer headerLengthBuffer;
        private MessageBuffer headerBuffer;
        private MessageBuffer packetBuffer;

        private AccountInfo accountInfo;
        private GameAccountInfo gameAccountInfo;

        private String locale;
        private String os;
        private int build;
        private String ipCountry;
        private byte[] clientSecret = new byte[32];
        private boolean authed;
        private QueryCallbackProcessor queryProcessor;

        private Map<Integer, Consumer<MessageBuffer>> responseCallbacks;
        private int requestToken;

        // Handlers mapeados
        private static final Map<String, ClientRequestHandler> clientRequestHandlers = new HashMap<>();

        @FunctionalInterface
        private interface ClientRequestHandler {
            int handle(Map<String, Variant> params, ClientResponse response);
        }

        public class LastPlayedCharacterInfo {}
        public class GameAccountInfo {}
        public class AccountInfo {}

        public void start() {}
        public boolean update() { return false; }

        public Integer getAccountId() { return 0; }
        public Integer getGameAccountId() { return 0; }

        public void sendResponse(Integer token, Message response) {}
        public void sendResponse(Integer token, Integer status) {}

        public void sendRequest(Integer serviceHash, Integer methodHash, Message request, Consumer<MessageBuffer> callback) {}
        public void sendRequest(Integer serviceHash, Integer methodHash, Message request) {}

        public Integer handleLogon(LogonRequest logonRequest, Consumer<Message> continuation) { return 0; }
        public Integer handleVerifyWebCredentials(VerifyWebCredentialsRequest verifyWebCredentialsRequest, Consumer<Message> continuation) { return 0; }
        public Integer handleGetAccountState(GetAccountStateRequest request, GetAccountStateResponse response) { return 0; }
        public Integer handleGetGameAccountState(GetGameAccountStateRequest request, GetGameAccountStateResponse response) { return 0; }
        public Integer handleProcessClientRequest(ClientRequest request, ClientResponse response) { return 0; }
        public Integer handleGetAllValuesForAttribute(GetAllValuesForAttributeRequest request, GetAllValuesForAttributeResponse response) { return 0; }

        public String getClientInfo() { return ""; }

        protected void handshakeHandler(Exception error) {}
        protected void readHandler() {}

        private boolean readHeaderLengthHandler() { return  false; }
        private boolean readHeaderHandler() { return false; }
        private boolean readDataHandler() { return false; }
        private void asyncWrite(MessageBuffer packet) {}
        private void asyncHandshake() {}
        private void checkIpCallback(ResultSet result) {}
        private Integer verifyWebCredentials(String webCredentials, Consumer<Message> continuation) { return 0; }


    }
}
