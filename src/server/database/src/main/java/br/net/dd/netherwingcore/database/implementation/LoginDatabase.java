package br.net.dd.netherwingcore.database.implementation;

import br.net.dd.netherwingcore.common.configuration.Config;
import br.net.dd.netherwingcore.database.common.GenericDatabase;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Map;
import java.util.concurrent.*;

/**
 * LoginDatabase is a singleton class that extends GenericDatabase to handle
 * database operations specific to the login database.
 *
 * @see GenericDatabase
 */
public class LoginDatabase extends GenericDatabase<LoginDatabaseStatements> {

    private Integer syncTheadPoolSize = 4;
    private Integer aSyncTheadPoolSize = 4;

    private static LoginDatabase instance;

    /**
     * Private constructor to enforce singleton pattern.
     * Initializes the LoginDatabase with connection information from the configuration cache.
     */
    private LoginDatabase() {
        String loginDatabaseInfo = Config.get("LoginDatabaseInfo", "127.0.0.1;3306;trinity;trinity;auth");
        super(loginDatabaseInfo);
        this.aSyncTheadPoolSize = Config.get("LoginDatabase.WorkerThreads", 1);
        this.syncTheadPoolSize = Config.get("LoginDatabase.SynchThreads", 1);
    }

    /**
     * Retrieves the singleton instance of LoginDatabase.
     *
     * @return The single instance of LoginDatabase.
     */
    public static LoginDatabase getInstance() {
        if (instance == null) {
            instance = new LoginDatabase();
        }
        return instance;
    }

    /**
     * Executes a database statement with the provided parameters.
     *
     * @param statement The database statement to execute.
     * @param params    The parameters to bind to the statement.
     * @return true if the execution was successful, false otherwise.
     * @see GenericDatabase#execute
     */
    @SafeVarargs
    @Override
    public final boolean execute(LoginDatabaseStatements statement, Map<Integer, String>... params) {
        return switch (statement.getConnectionFlag()) {
            case CONNECTION_SYNC -> syncExecute(statement, params);
            case CONNECTION_ASYNC -> asyncExecute(statement, params);
            default -> throw new IllegalArgumentException("Unknown ConnectionFlag: " + statement.getConnectionFlag());
        };
    }

    /**
     * Synchronous execution of a database statement.
     *
     * @param statement The database statement to execute.
     * @param params    The parameters to bind to the statement.
     * @return true if the execution was successful, false otherwise.
     * @see GenericDatabase#syncExecute
     */
    @SafeVarargs
    @Override
    protected final boolean syncExecute(LoginDatabaseStatements statement, Map<Integer, String>... params) {
        PreparedStatement preparedStatement = getPreparedStatement(statement.getQuery(), params);
        if (preparedStatement != null) {
            try {
                preparedStatement.execute();
                preparedStatement.close();
                return true;
            } catch (Exception e) {
                return false;
            }
        }
        return false;
    }

    /**
     * Asynchronous execution of a database statement.
     *
     * @param statement The database statement to execute.
     * @param params    The parameters to bind to the statement.
     * @return true if the execution was successful, false otherwise.
     * @see GenericDatabase#asyncExecute
     */
    @SafeVarargs
    @Override
    protected final boolean asyncExecute(LoginDatabaseStatements statement, Map<Integer, String>... params) {

        ExecutorService executorService = Executors.newFixedThreadPool(this.aSyncTheadPoolSize);

        CompletableFuture<Boolean> future = CompletableFuture.supplyAsync(() -> {
            PreparedStatement preparedStatement = getPreparedStatement(statement.getQuery(), params);
            if (preparedStatement != null) {
                try {
                    preparedStatement.execute();
                    preparedStatement.close();
                    return true;
                } catch (Exception e) {
                    return false;
                }
            }
            return false;
        }, executorService);

        try {
            return future.get();
        } catch (InterruptedException | ExecutionException e) {
            return false;
        }

    }

    /**
     * Executes a query and returns the result set.
     *
     * @param statement The database statement to query.
     * @param params    The parameters to bind to the statement.
     * @return A ResultSet containing the results of the query.
     * @see GenericDatabase#query
     */
    @SafeVarargs
    @Override
    public final ResultSet query(LoginDatabaseStatements statement, Map<Integer, String>... params) {
        return switch (statement.getConnectionFlag()) {
            case CONNECTION_SYNC -> syncQuery(statement, params);
            case CONNECTION_ASYNC -> asyncQuery(statement, params);
            default -> throw new IllegalArgumentException("Unknown ConnectionFlag: " + statement.getConnectionFlag());
        };
    }

    /**
     * Synchronous query execution.
     *
     * @param statement The database statement to query.
     * @param params    The parameters to bind to the statement.
     * @return A ResultSet containing the results of the query.
     * @see GenericDatabase#syncQuery
     */
    @SafeVarargs
    @Override
    protected final ResultSet syncQuery(LoginDatabaseStatements statement, Map<Integer, String>... params) {
        PreparedStatement preparedStatement = getPreparedStatement(statement.getQuery(), params);
        if (preparedStatement != null) {
            try {
                return preparedStatement.executeQuery();
            } catch (Exception e) {
                return null;
            }
        }
        return null;
    }

    /**
     * Asynchronous query execution.
     *
     * @param statement The database statement to query.
     * @param params    The parameters to bind to the statement.
     * @return A ResultSet containing the results of the query.
     * @see GenericDatabase#asyncQuery
     */
    @SafeVarargs
    @Override
    protected final ResultSet asyncQuery(LoginDatabaseStatements statement, Map<Integer, String>... params) {

        ExecutorService executorService = Executors.newFixedThreadPool(this.aSyncTheadPoolSize);

        CompletableFuture<ResultSet> future = CompletableFuture.supplyAsync(() -> {
            PreparedStatement preparedStatement = getPreparedStatement(statement.getQuery(), params);
            if (preparedStatement != null) {
                try {
                    return preparedStatement.executeQuery();
                } catch (Exception e) {
                    return null;
                }
            }
            return null;
        }, executorService);

        try {
            return future.get();
        } catch (InterruptedException | ExecutionException e) {
            return null;
        }

    }

    /**
     * Retrieves account information from the database based on the provided email.
     *
     * @param email The email address associated with the account to retrieve.
     * @return An AccountInfo object containing the account details, or null if no account is found.
     */
    public static AccountInfo getAccountByEmail(String email) {
        AccountInfo accountInfo = new AccountInfo();
        accountInfo.id = 1;
        accountInfo.email = email;
        accountInfo.locked = false;
        accountInfo.lockCountry = "BR";
        accountInfo.lastIp = "127.0.0.1";
        accountInfo.loginTicketExpiry = 3600;
        accountInfo.isBanned = false;
        accountInfo.isPermanentBan = false;
        accountInfo.gameAccountId = 1;
        accountInfo.gameAccountName = "TestAccount";
        accountInfo.securityLevel = 0;

        return accountInfo;
    }

    /**
     * Data class representing account information retrieved from the database.
     */
    public static class AccountInfo {
        public int id;
        public String email;
        public boolean locked;
        public String lockCountry;
        public String lastIp;
        public int loginTicketExpiry;
        public boolean isBanned;
        public boolean isPermanentBan;
        public int gameAccountId;
        public String gameAccountName;
        public int securityLevel;
    }
}
