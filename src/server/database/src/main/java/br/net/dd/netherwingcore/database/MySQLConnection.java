package br.net.dd.netherwingcore.database;

import br.net.dd.netherwingcore.common.utilities.Util;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * The {@code MySQLConnection} class serves as an abstract base for managing MySQL database connections.
 * It provides methods for opening and closing connections, executing SQL statements, and handling transactions.
 *
 * <p>This class encapsulates connection information through the nested {@link MySQLConnectionInfo} class,
 * which parses connection details from a formatted string. The class also defines connection flags
 * through the {@link ConnectionFlags} enum to specify synchronous and asynchronous connection types.
 *
 * <p>Subclasses of {@code MySQLConnection} are expected to implement specific database operations
 * such as executing queries and managing transactions.
 *
 * @see MySQLConnection.MySQLConnectionInfo
 * @see MySQLConnection.ConnectionFlags
 */
public abstract class MySQLConnection {

    /**
     * Enumeration representing connection flags for MySQL connections.
     * It defines asynchronous, synchronous, and both connection types.
     */
    public enum ConnectionFlags {
        CONNECTION_ASYNC(0x1),
        CONNECTION_SYNC(0x2),
        CONNECTION_BOTH(CONNECTION_ASYNC.flag | CONNECTION_SYNC.flag);

        final int flag;

        ConnectionFlags(int i) {
            this.flag = i;
        }
    }

    /**
     * The {@code MySQLConnectionInfo} class encapsulates the connection information
     * required to establish a MySQL database connection. It parses a formatted string
     * to extract details such as user, password, database name, host, port or socket,
     * and SSL settings.
     */
    public static class MySQLConnectionInfo {
        public String user;
        public String password;
        public String database;
        public String host;
        public String portOrSocket;
        public String ssl;

        /**
         * Constructs a new {@code MySQLConnectionInfo} instance by parsing the provided
         * connection information string.
         *
         * @param infoString the connection information string in the format:
         *                   "host;portOrSocket;user;password;database[;ssl]"
         */
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

    private final MySQLConnectionInfo connectionInfo;
    private ConnectionPool connectionPool;

    /**
     * Constructs a new {@code MySQLConnection} instance with the specified connection information.
     *
     * @param connectionInfo the {@link MySQLConnectionInfo} object containing connection details
     */
    public MySQLConnection(MySQLConnectionInfo connectionInfo) {
        this.connectionInfo = connectionInfo;
    }

    /**
     * Opens a connection to the MySQL database.
     *
     * @return {@code true} if the connection is successfully opened; {@code false} otherwise
     */
    public boolean open() {
        if (connectionPool != null) {
            return true;
        }

        this.connectionPool = new ConnectionPool(DataSourceFactory.create(connectionInfo), 10);

        return this.connectionPool.getConnection() != null;
    }

    /**
     * Retrieves a connection from the connection pool.
     *
     * @return a {@link Connection} object representing the database connection
     */
    public Connection getConnection(){
        return connectionPool.getConnection();
    }

    /**
     * Closes the connection to the MySQL database.
     */
    public void close() {
        this.connectionPool = null;
    }

}
