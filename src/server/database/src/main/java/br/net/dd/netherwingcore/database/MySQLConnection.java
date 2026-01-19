package br.net.dd.netherwingcore.database;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.ExecutorService;

public class MySQLConnection {

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
