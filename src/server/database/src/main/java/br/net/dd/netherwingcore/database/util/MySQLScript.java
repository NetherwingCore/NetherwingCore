package br.net.dd.netherwingcore.database.util;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;

public class MySQLScript {

    private final Connection connection;
    private final boolean autoCommit;
    private final boolean stopOnError;

    public MySQLScript(Connection connection, boolean autoCommit, boolean stopOnError) {
        this.connection = connection;
        this.autoCommit = autoCommit;
        this.stopOnError = stopOnError;
    }

    public void run(File script) {
        try (
                InputStream inputStream = new FileInputStream(script);
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
                BufferedReader reader = new BufferedReader(inputStreamReader);
        ) {
            runScript(reader);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void runScript(BufferedReader reader) {
        System.out.println("Running SQL script...");
    }

}
