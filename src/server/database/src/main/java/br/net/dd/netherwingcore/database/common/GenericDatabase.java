package br.net.dd.netherwingcore.database.common;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

/**
 * GenericDatabase is an abstract class that provides a template for database operations.
 * It manages the connection pool and provides methods for connecting, disconnecting,
 * preparing statements, and executing queries.
 *
 * @param <T> The type of database statement used in execute and query methods.
 */
public abstract class GenericDatabase<T> {

    private final ConnectionInfos connectionInfos;
    private ConnectionPool connectionPool;

    /**
     * Constructs a GenericDatabase instance with the provided connection information string.
     *
     * @param infoString A semicolon-separated string containing database connection details.
     *                   Format: "host;port;username;password;databaseName"
     * @throws IllegalArgumentException if the infoString is null or empty.
     */
    public GenericDatabase(String infoString) {
        if (infoString == null || infoString.isEmpty()) {
            throw new IllegalArgumentException("Connection info string cannot be null or empty");
        }
        this.connectionInfos = new ConnectionInfos(infoString);
    }

    /**
     * Establishes a connection to the database by initializing the connection pool.
     *
     * @return true if the connection was successfully established, false if already connected.
     */
    public boolean connect() {
        if (this.connectionPool == null) {
            this.connectionPool = new ConnectionPool(DataSourceFactory.createDataSource(this.connectionInfos), 10);
            return true;
        }
        return false;
    }

    /**
     * Verifies if the database connection is established.
     * Throws an IllegalStateException if the connection pool is null.
     */
    private void verifyConnection() {
        if (this.connectionPool == null) {
            throw new IllegalStateException("Database is not connected. Call connect() before performing database operations.");
        }
    }

    /**
     * Disconnects from the database by closing all connections in the pool.
     *
     * @return true if the disconnection was successful, false if there was no active connection.
     */
    public boolean disconnect() {
        if (this.connectionPool != null) {
            this.connectionPool.closeAllConnections();
            this.connectionPool = null;
            return true;
        }
        return false;
    }

    /**
     * Retrieves a connection from the connection pool.
     *
     * @return A Connection object from the pool.
     */
    protected Connection getConnection() {
        this.verifyConnection();
        return this.connectionPool.getConnection();
    }

    /**
     * Counts the number of parameter placeholders ('?') in the given SQL query.
     *
     * @param query The SQL query string.
     * @return The count of parameter placeholders in the query.
     */
    protected Long paramCount(String query) {
        return query.chars().filter(c -> c == '?').count();
    }

    /**
     * Prepares a SQL statement with the given query and parameters.
     *
     * @param query  The SQL query string with placeholders for parameters.
     * @param params A variable number of maps containing parameter indices and their corresponding values.
     * @return A PreparedStatement object with the parameters set, or null if the parameter count does not match.
     */
    @SafeVarargs
    protected final PreparedStatement getPreparedStatement(String query, Map<Integer, String>... params) {
        Long paramCount = paramCount(query);
        if (paramCount != params[0].size()) {
            return null;
        }
        System.out.println("Preparing statement: " + query);

        try {
            PreparedStatement preparedStatement = getConnection().prepareStatement(query);
            int count = 1;
            while (count < paramCount) {
                preparedStatement.setObject(count, params[count]);
                count++;
            }
            return preparedStatement;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Executes a database statement.
     *
     * @param statement The database statement to execute.
     * @param params    A variable number of maps containing parameter indices and their corresponding values.
     * @return true if the execution was successful, false otherwise.
     */
    public abstract boolean execute(T statement, Map<Integer, String>... params);

    /**
     * Synchronous execution of a database statement.
     *
     * @param statement The database statement to execute.
     * @param params    A variable number of maps containing parameter indices and their corresponding values.
     * @return true if the execution was successful, false otherwise.
     */
    protected abstract boolean syncExecute(T statement, Map<Integer, String>... params);

    /**
     * Asynchronous execution of a database statement.
     *
     * @param statement The database statement to execute.
     * @param params    A variable number of maps containing parameter indices and their corresponding values.
     * @return true if the execution was successful, false otherwise.
     */
    protected abstract boolean asyncExecute(T statement, Map<Integer, String>... params);

    /**
     * Executes a query and returns the result set.
     *
     * @param statement The database statement to query.
     * @param params    A variable number of maps containing parameter indices and their corresponding values.
     * @return A ResultSet containing the results of the query.
     */
    public abstract ResultSet query(T statement, Map<Integer, String>... params);

    /**
     * Synchronous query execution.
     *
     * @param statement The database statement to query.
     * @param params    A variable number of maps containing parameter indices and their corresponding values.
     * @return A ResultSet containing the results of the query.
     */
    protected abstract ResultSet syncQuery(T statement, Map<Integer, String>... params);

    /**
     * Asynchronous query execution.
     *
     * @param statement The database statement to query.
     * @param params    A variable number of maps containing parameter indices and their corresponding values.
     * @return A ResultSet containing the results of the query.
     */
    protected abstract ResultSet asyncQuery(T statement, Map<Integer, String>... params);

}
