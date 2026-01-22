package br.net.dd.netherwingcore.database.common;

import br.net.dd.netherwingcore.common.utilities.Util;

import java.util.List;

/**
 * The {@code ConnectionInfos} class is responsible for parsing and storing
 * database connection information from a formatted string.
 *
 * <p>The connection information string should be formatted as follows:
 * <pre>
 *     host;portOrSocket;user;password;database[;ssl]
 * </pre>
 * where the SSL parameter is optional.
 *
 * <p>This class provides getter methods to access individual components
 * of the connection information.
 */
public class ConnectionInfos {

    private String user;
    private String password;
    private String database;
    private String host;
    private String portOrSocket;
    private String ssl;

    /**
     * Constructs a {@code ConnectionInfos} object by parsing the provided
     * connection information string.
     *
     * @param infoString the connection information string to parse
     */
    public ConnectionInfos(String infoString){
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

    /**
     * Returns the username for the database connection.
     *
     * @return the username
     */
    public String getUser() {
        return user;
    }

    /**
     * Returns the password for the database connection.
     *
     * @return the password
     */
    public String getPassword() {
        return password;
    }

    /**
     * Returns the database name for the connection.
     *
     * @return the database name
     */
    public String getDatabase() {
        return database;
    }

    /**
     * Returns the host for the database connection.
     *
     * @return the host
     */
    public String getHost() {
        return host;
    }

    /**
     * Returns the port or socket for the database connection.
     *
     * @return the port or socket
     */
    public String getPortOrSocket() {
        return portOrSocket;
    }

    /**
     * Returns the SSL configuration for the database connection, if provided.
     *
     * @return the SSL configuration, or {@code null} if not provided
     */
    public String getSsl() {
        return ssl;
    }
}
