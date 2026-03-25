package br.net.dd.netherwingcore.database.util;

import br.net.dd.netherwingcore.common.configuration.Config;
import br.net.dd.netherwingcore.database.common.ConnectionInfos;
import br.net.dd.netherwingcore.database.parser.MySqlDumpRunner;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.*;

/**
 * The {@code DBTools} class provides utility methods for managing database users and databases.
 * It includes methods to check for the existence of users and databases, create users and databases,
 * and grant privileges to users. The class uses JDBC to interact with the database and relies on
 * configuration settings for database connection details.
 */
public class DBTools {

    private static final String ROOT_USER = "root";

    private static final String SQL_QUERY_CHECK_USER = "SELECT User FROM mysql.user WHERE User = ?";
    private static final String SQL_CREATE_USER = "CREATE USER '{}'@'{}' IDENTIFIED BY '{}' WITH MAX_QUERIES_PER_HOUR 0 MAX_CONNECTIONS_PER_HOUR 0 MAX_UPDATES_PER_HOUR 0";
    private static final String SQL_GRANT_USAGE = "GRANT USAGE ON * . * TO '{}'@'{}'";

    private static final String SQL_QUERY_CHECK_DATABASE = "SELECT SCHEMA_NAME FROM INFORMATION_SCHEMA.SCHEMATA WHERE SCHEMA_NAME = ?";
    private static final String SQL_CREATE_DATABASE = "CREATE DATABASE `{}` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci";
    private static final String SQL_GRANT_ALL_PRIVILEGES = "GRANT ALL PRIVILEGES ON `{}` . * TO '{}'@'{}' WITH GRANT OPTION";

    /**
     * Private constructor to prevent instantiation of the DBTools class.
     * This class is intended to be used as a utility class with static methods only.
     */
    private DBTools() {
    }

    /**
     * Establishes a connection to the database using the provided ConnectionInfos and root credentials.
     *
     * @param infos   the ConnectionInfos object containing database connection details
     * @param useRoot a boolean indicating whether to use root credentials for the connection
     * @return a Connection object representing the established connection to the database
     * @throws RuntimeException if there is an error during connection establishment
     */
    public static Connection getConnection(ConnectionInfos infos, boolean useRoot) {
        try {

            Connection connection;

            String rootPassword = Config.get("Updates.RootPassword", "\"\"");
            rootPassword = rootPassword.replace("\"", "");

            String url = "jdbc:mysql://" + infos.getHost() + ":3306/" + (useRoot ? "" : infos.getDatabase());

            if (useRoot) {
                connection = DriverManager.getConnection(url, ROOT_USER, rootPassword);
            } else {
                connection = DriverManager.getConnection(url, infos.getUser(), infos.getPassword());
            }

            return connection;

        } catch (SQLException ex) {
            throw new RuntimeException("Failed to connect to the database: " + ex.getMessage(), ex);
        }
    }

    /**
     * Grants USAGE privileges to the user defined in the ConnectionInfos object.
     *
     * @param infos the ConnectionInfos object containing user details
     * @return true if the privileges were granted successfully, false otherwise
     * @throws RuntimeException if there is an error during privilege granting
     */
    public static boolean grantUsage(ConnectionInfos infos) {

        String formatedSQL = SQL_GRANT_USAGE.replaceFirst("\\{}", infos.getUser());

        String host = (infos.getHost().equals("127.0.0.1")) ? "localhost" : infos.getHost();

        formatedSQL = formatedSQL.replaceFirst("\\{}", host);

        return executeStatement(infos, formatedSQL, true);

    }

    /**
     * Creates a new user in the database with the username, host, and password specified in the ConnectionInfos object.
     *
     * @param infos the ConnectionInfos object containing user details
     * @return true if the user was created successfully, false otherwise
     * @throws RuntimeException if there is an error during user creation
     */
    public static boolean createUser(ConnectionInfos infos) {

        String host = (infos.getHost().equals("127.0.0.1")) ? "localhost" : infos.getHost();

        String formatedSQL = SQL_CREATE_USER.replaceFirst("\\{}", infos.getUser());
        formatedSQL = formatedSQL.replaceFirst("\\{}", host);
        formatedSQL = formatedSQL.replaceFirst("\\{}", infos.getPassword());

        return executeStatement(infos, formatedSQL, true);

    }

    /**
     * Grants all privileges on the specified database to the user defined in the ConnectionInfos object.
     *
     * @param infos the ConnectionInfos object containing database and user details
     * @return true if the privileges were granted successfully, false otherwise
     * @throws RuntimeException if there is an error during privilege granting
     */
    public static boolean grantAllPrivileges(ConnectionInfos infos) {

        String host = (infos.getHost().equals("127.0.0.1")) ? "localhost" : infos.getHost();

        String formatedSQL = SQL_GRANT_ALL_PRIVILEGES.replaceFirst("\\{}", infos.getDatabase());
        formatedSQL = formatedSQL.replaceFirst("\\{}", infos.getUser());
        formatedSQL = formatedSQL.replaceFirst("\\{}", host);

        return executeStatement(infos, formatedSQL, true);

    }

    /**
     * Creates a new database with the name specified in the ConnectionInfos object.
     *
     * @param infos the ConnectionInfos object containing database details
     * @return true if the database was created successfully, false otherwise
     * @throws RuntimeException if there is an error during database creation
     */
    public static boolean createDatabase(ConnectionInfos infos) {

        String formatedSQL = SQL_CREATE_DATABASE.replaceFirst("\\{}", infos.getDatabase());

        return executeStatement(infos, formatedSQL, true);
    }

    /**
     * Executes the provided SQL statement using a connection established with the given ConnectionInfos.
     *
     * @param infos       the ConnectionInfos object containing database connection details
     * @param formatedSQL the SQL statement to execute
     * @return true if the statement was executed successfully, false otherwise
     * @throws RuntimeException if there is an error during statement execution or connection handling
     */
    public static boolean executeStatement(ConnectionInfos infos, String formatedSQL, boolean useRoot) {
        try (
                Connection connection = getConnection(infos, useRoot);
                Statement statement = connection.createStatement()
        ) {
            try {

                statement.execute(formatedSQL);

                return true;

            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        } catch (SQLException ex) {
            throw new RuntimeException("Failed to close connection to the database: " + ex.getMessage(), ex);
        }
    }

    /**
     * Loads a SQL dump file into the database specified in the ConnectionInfos object.
     *
     * @param infos    the ConnectionInfos object containing database connection details
     * @param dumpFile the Path to the SQL dump file to be loaded
     * @return true if the dump was loaded successfully, false otherwise
     * @throws RuntimeException if there is an error during dump loading or file handling
     */
    public static boolean loadDump(ConnectionInfos infos, Path dumpFile) {

        if (Files.exists(dumpFile.toAbsolutePath())) {
            System.out.println("Dump file found: " + dumpFile.toString());
        } else {
            throw new RuntimeException("Dump file not found: " + dumpFile.toString());
        }

        try {
            MySqlDumpRunner.runSqlDump(getConnection(infos, false), dumpFile);
            return true;
        } catch (IOException | SQLException e) {
            throw new RuntimeException(e);
        }

    }

    /**
     * Checks if the specified database exists by executing a query against the INFORMATION_SCHEMA.SCHEMATA table.
     *
     * @param infos the ConnectionInfos object containing database details
     * @return true if the database exists, false otherwise
     * @throws RuntimeException if there is an error during the database existence check
     */
    public static boolean checkDatabase(ConnectionInfos infos) {

        Connection connection = getConnection(infos, true);

        try (PreparedStatement statement = connection.prepareStatement(SQL_QUERY_CHECK_DATABASE)) {

            statement.setString(1, infos.getDatabase());

            return statement.executeQuery().next();

        } catch (SQLException ex) {
            throw new RuntimeException("Failed to check database existence: " + ex.getMessage(), ex);
        }

    }

    /**
     * Checks if the specified user exists by executing a query against the mysql.user table.
     *
     * @param infos the ConnectionInfos object containing user details
     * @return true if the user exists, false otherwise
     * @throws RuntimeException if there is an error during the user existence check
     */
    public static boolean checkUser(ConnectionInfos infos) {

        Connection connection = getConnection(infos, true);

        try (PreparedStatement statement = connection.prepareStatement(SQL_QUERY_CHECK_USER)) {

            statement.setString(1, infos.getUser());

            return statement.executeQuery().next();

        } catch (SQLException ex) {
            throw new RuntimeException("Failed to check database existence: " + ex.getMessage(), ex);
        }

    }

    public static boolean updateDatabaseFromFile(ConnectionInfos infos, Path filePath) {
        Connection connection = getConnection(infos, false);

        return false;
    }

}
