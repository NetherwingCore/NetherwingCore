package br.net.dd.netherwingcore.database;

import com.mysql.cj.jdbc.MysqlConnectionPoolDataSource;

/**
 * The {@code DataSourceFactory} class is responsible for creating and configuring
 * instances of {@link MysqlConnectionPoolDataSource} based on the provided
 * {@link MySQLConnection.MySQLConnectionInfo}.
 *
 * <p>This factory class encapsulates the logic for setting up the data source,
 * including handling different connection scenarios such as socket connections
 * based on the operating system.
 */
public class DataSourceFactory {

    /**
     * Creates and configures a {@link MysqlConnectionPoolDataSource} instance
     * based on the provided {@link MySQLConnection.MySQLConnectionInfo}.
     *
     * @param mysqlConnectionInfo the connection information used to configure the data source
     * @return a configured {@link MysqlConnectionPoolDataSource} instance
     */
    public static MysqlConnectionPoolDataSource create(MySQLConnection.MySQLConnectionInfo mysqlConnectionInfo) {
        MysqlConnectionPoolDataSource dataSource = new MysqlConnectionPoolDataSource();

        if (mysqlConnectionInfo.host.equals(".")) {
            String osName = System.getProperty("os.name").toLowerCase();
            String url = getUrl(mysqlConnectionInfo, osName);

            dataSource.setUrl(url + mysqlConnectionInfo.portOrSocket);
        } else {
            dataSource.setDatabaseName(mysqlConnectionInfo.database);
            dataSource.setUser(mysqlConnectionInfo.user);
            dataSource.setPassword(mysqlConnectionInfo.password);
            dataSource.setServerName(mysqlConnectionInfo.host);
            dataSource.setPort(Integer.parseInt(mysqlConnectionInfo.portOrSocket));
        }

        return dataSource;
    }

    /**
     * Constructs the JDBC URL for socket connections based on the operating system.
     *
     * @param mysqlConnectionInfo the connection information used to build the URL
     * @param osName              the name of the operating system
     * @return the constructed JDBC URL for socket connections
     */
    private static String getUrl(MySQLConnection.MySQLConnectionInfo mysqlConnectionInfo, String osName) {

        String url = "jdbc:mysql://localhost/" + mysqlConnectionInfo.database +
                "?user="+ mysqlConnectionInfo.user +
                "&password=" + mysqlConnectionInfo.password;

        if (osName.contains("win")) {
            url = url + "&socketFactory=com.mysql.cj.jdbc.NamedPipeSocketFactory&unixSocket=";
        } else {
            url = url + "&socketFactory=com.mysql.cj.jdbc.UnixSocketFactory&pipe=";
        }
        return url;
    }

}
