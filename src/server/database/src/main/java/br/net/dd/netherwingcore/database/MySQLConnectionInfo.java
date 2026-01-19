package br.net.dd.netherwingcore.database;

import br.net.dd.netherwingcore.common.utilities.Util;

import java.util.List;

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
