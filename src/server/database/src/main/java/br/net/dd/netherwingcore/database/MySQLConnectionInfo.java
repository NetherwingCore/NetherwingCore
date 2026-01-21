package br.net.dd.netherwingcore.database;

import br.net.dd.netherwingcore.common.utilities.Util;

import java.util.List;

/**
 * The {@code MySQLConnectionInfo} class encapsulates information required to set up a MySQL
 * connection. It parses a connection string formatted as a semicolon-delimited series of values
 * and provides getter methods to access the connection parameters.
 *
 * <p>Expected format for the input connection string:</p>
 * <ul>
 *     <li>{@code host};{@code portOrSocket};{@code user};{@code password};{@code database}[;{@code ssl}]</li>
 * </ul>
 * Where the {@code ssl} parameter is optional.
 *
 * <p><b>Example usage:</b></p>
 * <pre>
 * MySQLConnectionInfo connectionInfo = new MySQLConnectionInfo("localhost;3306;root;password;mydb;true");
 * System.out.println(connectionInfo.getHost()); // Outputs: localhost
 * System.out.println(connectionInfo.getPortOrSocket()); // Outputs: 3306
 * </pre>
 *
 * <p>This class uses a utility method {@code Util.tokenize()} to split the input string into
 * tokens. If the number of tokens is not valid (neither 5 nor 6), the object is left uninitialized.</p>
 *
 * <p><b>Note:</b> This class does not enforce validation or type checking on individual parameter values.</p>
 *
 */
public class MySQLConnectionInfo {

    private String user;
    private String password;
    private String database;
    private String host;
    private String portOrSocket;
    private String ssl;

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

    public String getUser() {
        return user;
    }

    public String getPassword() {
        return password;
    }

    public String getDatabase() {
        return database;
    }

    public String getHost() {
        return host;
    }

    public String getPortOrSocket() {
        return portOrSocket;
    }

    public String getSsl() {
        return ssl;
    }
}
