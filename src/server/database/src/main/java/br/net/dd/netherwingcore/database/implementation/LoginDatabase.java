package br.net.dd.netherwingcore.database.implementation;

import br.net.dd.netherwingcore.common.cache.Cache;
import br.net.dd.netherwingcore.database.common.GenericDatabase;

import java.sql.ResultSet;


public class LoginDatabase extends GenericDatabase {

    private static LoginDatabase instance;

    private LoginDatabase() {
        String loginDatabaseInfo = Cache.getConfiguration().get("LoginDatabaseInfo", "127.0.0.1;3306;trinity;trinity;auth");
        super(loginDatabaseInfo);
    }

    public static LoginDatabase getInstance() {
        if (instance == null) {
            instance = new LoginDatabase();
        }
        return instance;
    }

    public static boolean execute(LoginDatabaseStatements statement) {
        return getInstance().execute(statement.getQuery(), statement.getConnectionFlag());
    }

    public static ResultSet query(LoginDatabaseStatements statement) {
        return getInstance().guery(statement.getQuery(), statement.getConnectionFlag());
    }

}
