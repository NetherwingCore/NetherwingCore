package br.net.dd.netherwingcore.common.serialization;

import br.net.dd.netherwingcore.common.configuration.fields.Key;
import br.net.dd.netherwingcore.common.configuration.fields.Type;
import br.net.dd.netherwingcore.common.configuration.fields.Value;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Map;

import static br.net.dd.netherwingcore.common.logging.Log.log;

/**
 * The FileManager class provides utility methods for reading and writing configuration data
 * to and from files. It ensures thread-safety by synchronizing write and read operations.
 *
 * <p>This class is designed to support configuration management in a structured and reliable
 * way using key-value pair mappings. The key-value mappings are parsed according to the
 * specified format in the file, and there is built-in handling for text and numeric value types.
 *
 * <p>Usage:
 * <pre>
 *     // Write a line to a file
 *     FileManager.write("exampleKey=exampleValue", Paths.get("config.txt"));
 *
 *     // Read a file and retrieve key-value mappings
 *     Map<Key, Value> settings = FileManager.read(Paths.get("config.txt"));
 * </pre>
 *
 * This class is final and cannot be instantiated.
 */
public class FileManager {

    /**
     * An object lock to guarantee exclusive write operations to the file.
     */
    private static final Object LOCK_WRITE = new Object();

    /**
     * An object lock to guarantee exclusive read operations from the file.
     */
    private static final Object LOCK_READ = new Object();

    // Private constructor to prevent instantiation of the utility class.
    private FileManager() {
    }

    /**
     * Appends a single line of text to the specified file.
     *
     * <p>If the file does not exist, it will be created. If the file already exists, the line
     * will be appended at the end of the file. The file is written using UTF-8 encoding.
     *
     * @param line The line of text to write to the file. This should not be null.
     * @param targetFile The path to the file where the line will be written.
     *                    Must be a valid file path.
     * @throws NullPointerException If {@code line} or {@code targetFile} is null.
     */
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

    /**
     * Reads a configuration file and returns a map of key-value pairs.
     *
     * <p>The input file is expected to have lines in the following format:
     * <pre>
     * key=value
     * </pre>
     * Comments (lines starting with `#`), empty lines, and lines containing `[` are ignored.
     * Keys are wrapped in the {@link Key} class, and values are wrapped in the {@link Value}
     * class, with type information (e.g., {@code Type.TEXT} or {@code Type.NUMBER}).
     *
     * @param sourceFile The path to the configuration file to be read.
     *                   Must be a valid file path and exist.
     * @return A map of key-value pairs parsed from the file, or {@code null} if an error occurs.
     *         Each key is of the {@link Key} type, and the corresponding value is of the {@link Value} type.
     */
    public static Map<Key, Value> read(Path sourceFile){
        synchronized (LOCK_READ) {
            try {
                Map<Key, Value> configurations = new java.util.HashMap<>();

                FileInputStream fis = new FileInputStream(sourceFile.toFile());
                InputStreamReader isr = new InputStreamReader(fis, StandardCharsets.UTF_8);
                BufferedReader bufferedReader = new BufferedReader(isr);
                String line;
                while ((line = bufferedReader.readLine()) != null) {

                    // Skip comments, empty lines, and sections marked by '['
                    if (line.contains("#") || line.trim().isEmpty() || line.contains("[")) {
                        continue;
                    }

                    // Process key-value pairs
                    if (line.contains("=")) {
                        String[] parts = line.split("=", 2);
                        String keyPart = parts[0].trim();
                        String valuePart = parts[1].trim();

                        Key key = new Key(keyPart);
                        Value value = valuePart.contains("\"")
                                ? new Value(valuePart, Type.TEXT)
                                : new Value(valuePart, Type.NUMBER);

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
