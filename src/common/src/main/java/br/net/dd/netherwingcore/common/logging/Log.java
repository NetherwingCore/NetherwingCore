package br.net.dd.netherwingcore.common.logging;

import br.net.dd.netherwingcore.common.serialization.FileManager;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Provides functionalities for logging messages to various log files with different severity levels.
 * The logging framework supports structured messages with details like logging level and file targets.
 * This class handles the construction, formatting, and writing of log messages.
 */
public class Log {

    /**
     * Logs a simple informational message.
     * By default, the message is logged with the {@link Level#INFORMATION} severity.
     *
     * @param message A string message to log.
     */
    public static void log(String message, String... parameters) {
        log(new InformationMessage(formatMessage(message, parameters)));
    }

    /**
     * Logs an informational message with optional parameters for formatting.
     * The message is formatted using the provided parameters if any are given.
     *
     * @param message    The informational message to log, which should be an instance of {@link InformationMessage}.
     * @param parameters Optional parameters to format the message. If provided, the message will be formatted
     *                   using these parameters before logging.
     */
    public static void info(String message, String... parameters) {
        log(new InformationMessage(formatMessage(message, parameters)));
    }

    /**
     * Logs a warning message with optional parameters for formatting.
     * The message is formatted using the provided parameters if any are given.
     *
     * @param message    The warning message to log, which should be an instance of {@link WarningMessage}.
     * @param parameters Optional parameters to format the message. If provided, the message will be formatted
     *                   using these parameters before logging.
     */
    public static void warn(String message, String... parameters) {
        log(new WarningMessage(formatMessage(message, parameters)));
    }

    /**
     * Logs an error message with optional parameters for formatting.
     * The message is formatted using the provided parameters if any are given.
     *
     * @param message    The error message to log, which should be an instance of {@link ErrorMessage}.
     * @param parameters Optional parameters to format the message. If provided, the message will be formatted
     *                   using these parameters before logging.
     */
    public static void error(String message, String... parameters) {
        log(new ErrorMessage(formatMessage(message, parameters)));
    }

    /**
     * Logs a fatal error message with optional parameters for formatting.
     * The message is formatted using the provided parameters if any are given.
     *
     * @param message    The fatal error message to log, which should be an instance of {@link FatalErrorMessage}.
     * @param parameters Optional parameters to format the message. If provided, the message will be formatted
     *                   using these parameters before logging.
     */
    public static void fatal(String message, String... parameters) {
        log(new FatalErrorMessage(formatMessage(message, parameters)));
    }

    /**
     * Logs a debug message with optional parameters for formatting.
     * The message is formatted using the provided parameters if any are given.
     *
     * @param message    The debug message to log, which should be an instance of {@link DebugMessage}.
     * @param parameters Optional parameters to format the message. If provided, the message will be formatted
     *                   using these parameters before logging.
     */
    public static void debug(String message, String... parameters) {
        log(new DebugMessage(formatMessage(message, parameters)));
    }

    /**
     * Logs a message with additional details such as logging level and target files.
     * This method processes an array of {@link Detail} objects to determine the message,
     * severity level, and the files where the logs will be written.
     * <p>
     * For example:
     * - {@link Message} can specify the log message and its {@link Level}.
     * - {@link LogFile} provides paths to target log files.
     * </p>
     *
     * @param details Varargs of {@link Detail} providing additional context for the log message.
     *                Supported details are {@link Message} and {@link LogFile}.
     */
    public static void log(Detail... details) {
        AtomicReference<Level> level = new AtomicReference<>(Level.INFORMATION);
        AtomicReference<String> message = new AtomicReference<>("No message provided.");
        List<Path> logFiles = new ArrayList<>();

        // Process each detail to extract message, logging level, and file paths
        Arrays.stream(details).forEach(detail -> {
            if (detail instanceof Message){
                level.set(((Message) detail).getLevel());
                message.set(((Message) detail).getMessage());
            }
            if (detail instanceof LogFile){
                Arrays.stream(((LogFile) detail).paths()).iterator().forEachRemaining(logFiles::add);
            }
        });

        // Log the message to the console (for demonstration purposes)
        System.out.println(message.get());

        // Write the log message to the specified files
        logFiles.forEach(path -> {
            System.out.println("Logging to file: " + path.toString());
            FileManager.write("[" + level.get().name() + "] " + message.get(), path);
        });

    }

    /**
     * Formats a message by replacing placeholders with the provided parameters.
     * Placeholders in the message should be denoted by "{}". Each placeholder will be replaced
     * sequentially with the corresponding parameter from the provided array.
     *
     * @param message    The message containing placeholders to be formatted.
     * @param parameters An array of strings to replace the placeholders in the message.
     * @return The formatted message with all placeholders replaced by their corresponding parameters.
     */
    private static String formatMessage(String message, String... parameters) {
        for (String param : parameters) {
            message = message.replaceFirst("\\{}", param);
        }
        return message;
    }
}
