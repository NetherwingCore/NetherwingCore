package br.net.dd.netherwingcore.database.common;

import java.sql.Connection;
import java.sql.ResultSet;

public abstract class GenericDatabase {

    private final ConnectionInfos connectionInfos;
    private ConnectionPool connectionPool;

    public GenericDatabase(String infoString) {
        if (infoString == null || infoString.isEmpty()) {
            throw new IllegalArgumentException("Connection info string cannot be null or empty");
        }
        this.connectionInfos = new ConnectionInfos(infoString);
    }

    public boolean connect() {
        if (this.connectionPool == null) {
            this.connectionPool = new ConnectionPool(DataSourceFactory.createDataSource(this.connectionInfos), 10);
            return true;
        }
        return false;
    }

    private void verifyConnection() {
        if (this.connectionPool == null) {
            throw new IllegalStateException("Database is not connected. Call connect() before performing database operations.");
        }
    }

    public boolean disconnect() {
        if (this.connectionPool != null) {
            this.connectionPool.closeAllConnections();
            this.connectionPool = null;
            return true;
        }
        return false;
    }

    protected Connection getConnection() {
        this.verifyConnection();
        return this.connectionPool.getConnection();
    }

    protected boolean execute(String query, ConnectionFlag connectionFlag) {
        this.verifyConnection();
        return false;
    }

    protected ResultSet guery(String query, ConnectionFlag connectionFlag) {
        this.verifyConnection();
        return null;
    }

}
