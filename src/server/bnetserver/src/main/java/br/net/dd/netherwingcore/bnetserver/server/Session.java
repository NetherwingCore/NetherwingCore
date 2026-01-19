package br.net.dd.netherwingcore.bnetserver.server;

import br.net.dd.netherwingcore.bnetserver.server.session.AccountInfo;
import br.net.dd.netherwingcore.bnetserver.server.session.ClientRequestHandler;
import br.net.dd.netherwingcore.bnetserver.server.session.GameAccountInfo;
import br.net.dd.netherwingcore.common.utilities.MessageBuffer;
import br.net.dd.netherwingcore.database.QueryCallbackProcessor;
import br.net.dd.netherwingcore.proto.client.AccountServiceProto.*;
import br.net.dd.netherwingcore.proto.client.AuthenticationServiceProto.*;
import br.net.dd.netherwingcore.proto.client.GameUtilitiesServiceProto.*;
import br.net.dd.netherwingcore.shared.networking.Socket;
import com.google.protobuf.Message;

import java.io.IOException;
import java.nio.channels.AsynchronousSocketChannel;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import static br.net.dd.netherwingcore.common.logging.Log.log;

public class Session extends Socket {

    private MessageBuffer headerLengthBuffer;
    private MessageBuffer readBuffer;
    private MessageBuffer headerBuffer;
    private MessageBuffer packetBuffer;

    private AccountInfo accountInfo;
    private GameAccountInfo gameAccountInfo;

    private String locale;
    private String os;
    private Integer build;
    private String ipCountry;
    private byte[] clientSecret = new byte[32];
    private boolean authed;
    private QueryCallbackProcessor queryProcessor;

    private Map<Integer, Consumer<MessageBuffer>> responseCallbacks;
    private Integer requestToken;

    // Handlers mapeados
    private static final Map<String, ClientRequestHandler> clientRequestHandlers = new HashMap<>();

    public Session() throws IOException {
        super(AsynchronousSocketChannel.open());
        this.accountInfo = new AccountInfo();
        this.gameAccountInfo = new GameAccountInfo();
        this.locale = "";
        this.os = "";
        this.build = 0;
        this.ipCountry = "";
        this.authed = false;
        this.queryProcessor = new QueryCallbackProcessor();
        this.responseCallbacks = new HashMap<>();
        this.requestToken = 1;
        this.headerLengthBuffer = new MessageBuffer(4);
        this.readBuffer = new MessageBuffer(1024);
        this.headerBuffer = new MessageBuffer(128);
        this.packetBuffer = new MessageBuffer(2048);
    }

    public void start() {

        String ipAddress = getRemoteAddress().toString();
        log(getClientInfo() + " Accepted connection");

        /**
        // Verify that this IP is not in the ip_banned table
         LoginDatabase.Execute(LoginDatabase.GetPreparedStatement(LOGIN_DEL_EXPIRED_IP_BANS));

        LoginDatabasePreparedStatement stmt = LoginDatabase.GetPreparedStatement(LOGIN_SEL_IP_INFO);
        stmt.setString(0, ip_address);
         */
    }

    public boolean update() {
        return false;
    }

    public Integer getAccountId() {
        return 0;
    }

    public Integer getGameAccountId() {
        return 0;
    }

    public void sendResponse(Integer token, Message response) {
    }

    public void sendResponse(Integer token, Integer status) {
    }

    public void sendRequest(Integer serviceHash, Integer methodHash, Message request, Consumer<MessageBuffer> callback) {
    }

    public void sendRequest(Integer serviceHash, Integer methodHash, Message request) {
    }

    public Integer handleLogon(LogonRequest logonRequest, Consumer<Message> continuation) {
        return 0;
    }

    public Integer handleVerifyWebCredentials(VerifyWebCredentialsRequest verifyWebCredentialsRequest, Consumer<Message> continuation) {
        return 0;
    }

    public Integer handleGetAccountState(GetAccountStateRequest request, GetAccountStateResponse response) {
        return 0;
    }

    public Integer handleGetGameAccountState(GetGameAccountStateRequest request, GetGameAccountStateResponse response) {
        return 0;
    }

    public Integer handleProcessClientRequest(ClientRequest request, ClientResponse response) {
        return 0;
    }

    public Integer handleGetAllValuesForAttribute(GetAllValuesForAttributeRequest request, GetAllValuesForAttributeResponse response) {
        return 0;
    }

    public String getClientInfo() {
        return "";
    }

    protected void handshakeHandler(Exception error) {
    }

    @Override
    public void readHandler() {
    }

    private boolean readHeaderLengthHandler() {
        return false;
    }

    private boolean readHeaderHandler() {
        return false;
    }

    private boolean readDataHandler() {
        return false;
    }

    private void asyncWrite(MessageBuffer packet) {
    }

    private void asyncHandshake() {
    }

    private void checkIpCallback(ResultSet result) {
    }

    private Integer verifyWebCredentials(String webCredentials, Consumer<Message> continuation) {
        return 0;
    }

}
