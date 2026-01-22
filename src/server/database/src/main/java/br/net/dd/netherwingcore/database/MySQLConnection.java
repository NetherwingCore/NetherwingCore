package br.net.dd.netherwingcore.database;

import br.net.dd.netherwingcore.common.utilities.Util;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.ExecutorService;

public abstract class MySQLConnection {

    public enum ConnectionFlags {
        CONNECTION_ASYNC(0x1),
        CONNECTION_SYNC(0x2),
        CONNECTION_BOTH(CONNECTION_ASYNC.flag | CONNECTION_SYNC.flag);

        final int flag;

        ConnectionFlags(int i) {
            this.flag = i;
        }
    }

    public static class MySQLConnectionInfo {
        public String user;
        public String password;
        public String database;
        public String host;
        public String portOrSocket;
        public String ssl;

        public MySQLConnectionInfo(String infoString) {
            List<String> tokens = Util.tokenize(infoString, ';', true);

            if (tokens.size() != 5 && tokens.size() != 6) {
                return;
            }

            this.host = tokens.get(0);
            this.portOrSocket = tokens.get(1);
            this.user = tokens.get(2);
            this.password = tokens.get(3);
            this.database = tokens.get(4);

            if (tokens.size() == 6) {
                this.ssl = tokens.get(5);
            }
        }
    }

    private Connection connection;
    private final MySQLConnectionInfo connectionInfo;
    private ExecutorService workerThread;
    private boolean reconnecting;

    public MySQLConnection(MySQLConnectionInfo connectionInfo) {
        this.connectionInfo = connectionInfo;
        this.connection = null;
        this.workerThread = null;
        this.reconnecting = false;
    }

    public boolean open() {
        return false;
    }

    public void close() {}

    public boolean execute(String sql) { return false; }
    public ResultSet query(String sql) { return null; }
    public boolean executeTransaction(List<String> queries) { return false; }
    // Error handling
    private void handleError(SQLException e) {}
    public void beginTransaction() {}
    public void commitTransaction() {}
    public void rollbackTransaction() {}
    public void startWorkerThread() {}

}
