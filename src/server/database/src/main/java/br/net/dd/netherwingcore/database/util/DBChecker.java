package br.net.dd.netherwingcore.database.util;

import br.net.dd.netherwingcore.common.configuration.Config;
import br.net.dd.netherwingcore.database.common.ConnectionInfos;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class DBChecker {

    public static boolean database(ConnectionInfos infos) {

        String rootPassword = Config.get("Updates.RootPassword", "\"\"");

        try (Connection conn = DriverManager.getConnection(infos.getHost(), "root", rootPassword)) {
            String sql = "SELECT SCHEMA_NAME FROM INFORMATION_SCHEMA.SCHEMATA WHERE SCHEMA_NAME = ?";

            try (PreparedStatement statement = conn.prepareStatement(sql)) {

                statement.setString(1, infos.getDatabase());
                return statement.executeQuery().next();

            } catch (SQLException ex) {
                return false;
            }
        } catch (SQLException ex) {
            return false;
        }

    }

    public static boolean user(ConnectionInfos infos) {
        return true;
    }

}
