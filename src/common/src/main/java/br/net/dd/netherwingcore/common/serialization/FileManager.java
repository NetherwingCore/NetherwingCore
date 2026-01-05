package br.net.dd.netherwingcore.common.serialization;

import br.net.dd.netherwingcore.common.configuration.fields.Key;
import br.net.dd.netherwingcore.common.configuration.fields.Type;
import br.net.dd.netherwingcore.common.configuration.fields.Value;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Map;

import static br.net.dd.netherwingcore.common.logging.Log.log;

public class FileManager {

    private static final Object LOCK_WRITE = new Object();
    private static final Object LOCK_READ = new Object();

    private FileManager() {
    }

    public static void write(String line, Path targetFile){
        synchronized (LOCK_WRITE) {
            try {
                FileOutputStream fos = new FileOutputStream(targetFile.toFile(), true);
                OutputStreamWriter osw = new OutputStreamWriter(fos, StandardCharsets.UTF_8);
                BufferedWriter bufferedWriter = new BufferedWriter(osw);
                bufferedWriter.write(line);
                bufferedWriter.newLine();
                bufferedWriter.close();

            } catch (IOException e) {
                log("An error occurred while writing to the configuration file: " + e.getMessage());
            }
        }
    }

    public static Map<Key, Value> read(Path sourceFile){
        synchronized (LOCK_READ) {
            try {

                Map<Key, Value> configurations = new java.util.HashMap<>();

                FileInputStream fis = new FileInputStream(sourceFile.toFile());
                InputStreamReader isr = new InputStreamReader(fis, StandardCharsets.UTF_8);
                BufferedReader bufferedReader = new BufferedReader(isr);
                String line;
                while ((line = bufferedReader.readLine()) != null) {

                    if (line.contains("#") || line.trim().isEmpty() || line.contains("[")) {
                        continue;
                    }

                    if (line.contains("=")) {
                        String[] parts = line.split("=", 2);
                        String keyPart = parts[0].trim();
                        String valuePart = parts[1].trim();

                        Key key = new Key(keyPart);
                        Value value;
                        if (valuePart.contains("\"")) {
                            value = new Value(valuePart, Type.TEXT);
                        } else {
                            value = new Value(valuePart, Type.NUMBER);
                        }

                        configurations.put(key, value);

                    }

                }
                bufferedReader.close();

                return configurations;
            } catch (Exception e) {
                log("An error occurred while reading the configuration file: " + e.getMessage());
                return null;
            }
        }
    }

}
