package br.net.dd.netherwingcore.database;

import com.mysql.cj.jdbc.MysqlConnectionPoolDataSource;

import javax.sql.ConnectionEvent;
import javax.sql.ConnectionEventListener;
import javax.sql.PooledConnection;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.ArrayBlockingQueue;

public class ConnectionPool {

    private final ArrayBlockingQueue<PooledConnection> pool;

    public ConnectionPool(MysqlConnectionPoolDataSource ds, int size) {
        pool = new ArrayBlockingQueue<>(size);

        for ( int i = 0; i < size; i++ ) {
            try {
                PooledConnection conn = ds.getPooledConnection();
                pool.add(conn);
            } catch (Exception e) {
                System.out.println("Failed to create connection for the pool: " + e.getMessage());
            }
        }

    }

    public Connection getConnection() {
        try {
            PooledConnection pc = pool.take();
            Connection connection = pc.getConnection();

            pc.addConnectionEventListener(new ConnectionEventListener() {
                @Override
                public void connectionClosed(ConnectionEvent event) {
                    pool.offer(pc);
                }

                @Override
                public void connectionErrorOccurred(ConnectionEvent event) {
                    System.out.println("Connection error occurred: " + event.getSQLException().getMessage());
                }
            });

            return  connection;
        } catch (InterruptedException | SQLException e) {
            throw new RuntimeException(e);
        }
    }

}
