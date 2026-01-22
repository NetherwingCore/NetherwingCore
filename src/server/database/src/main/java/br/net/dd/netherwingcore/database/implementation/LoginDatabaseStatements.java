package br.net.dd.netherwingcore.database.implementation;

import br.net.dd.netherwingcore.database.common.ConnectionFlag;

import static br.net.dd.netherwingcore.database.common.ConnectionFlag.*;

public enum LoginDatabaseStatements {

    LOGIN_SEL_REALMLIST("SELECT id, name, address, localAddress, address3, address4, port, icon, flag, timezone, allowedSecurityLevel, population, gamebuild, Region, Battlegroup FROM realmlist WHERE flag <> 3 ORDER BY name", CONNECTION_SYNC),
    LOGIN_UPD_REALM_POPULATION("UPDATE realmlist SET population = ? WHERE id = ?", CONNECTION_SYNC);

    private String query;
    private ConnectionFlag connectionFlag;

    LoginDatabaseStatements(String query, ConnectionFlag connectionFlag) {
        this.query = query;
        this.connectionFlag = connectionFlag;
    }

    public String getQuery() {
        return query;
    }

    public ConnectionFlag getConnectionFlag() {
        return connectionFlag;
    }

}
