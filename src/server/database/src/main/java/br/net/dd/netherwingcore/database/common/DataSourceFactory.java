package br.net.dd.netherwingcore.database.common;

import com.mysql.cj.jdbc.MysqlConnectionPoolDataSource;

public class DataSourceFactory {
    public static MysqlConnectionPoolDataSource createDataSource(ConnectionInfos connectionInfos) {

        MysqlConnectionPoolDataSource dataSource = new MysqlConnectionPoolDataSource();
        if (connectionInfos.getHost().equals(".")) {
            System.out.println("Using named pipe or socket for MySQL connection.");
            dataSource.setUrl(makeUrl(connectionInfos));
        } else {
            dataSource.setServerName(connectionInfos.getHost());
            dataSource.setPort(Integer.parseInt(connectionInfos.getPortOrSocket()));
            dataSource.setDatabaseName(connectionInfos.getDatabase());
        }
        dataSource.setUser(connectionInfos.getUser());
        dataSource.setPassword(connectionInfos.getPassword());

        return dataSource;
    }

    private static String makeUrl(ConnectionInfos connectionInfos) {
        String osName = System.getProperty("os.name").toLowerCase();
        if (osName.contains("win")) {
            return "jdbc:mysql://localhost/" + connectionInfos.getDatabase() + "?socketFactory=com.mysql.cj.protocol.NamedPipeSocketFactory&namedPipePath=\\\\.\\pipe\\MySQL";
        } else {
            return "jdbc:mysql://localhost/" + connectionInfos.getDatabase() + "?socketFactory=com.mysql.cj.protocol.NamedPipeSocketFactory&namedPipePath=/var/run/mysqld/mysqld.sock";
        }
    }
}
