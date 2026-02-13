package br.net.dd.netherwingcore.common.logging;

import br.net.dd.netherwingcore.common.configuration.Config;
import br.net.dd.netherwingcore.common.serialization.FileManager;
import br.net.dd.netherwingcore.common.utilities.DataFormat;
import br.net.dd.netherwingcore.common.utilities.Util;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Provides functionalities for logging messages to various log files with different severity levels.
 * The logging framework supports structured messages with details like logging level and file targets.
 * This class handles the construction, formatting, and writing of log messages.
 */
public class Log {

    private boolean debugEnabled = false;
    private final String className;

    /**
     * Private constructor to prevent instantiation of the Log class.
     * This class is designed to be used as a utility for logging, and instances should be obtained through the getLogger method.
     *
     * @param className The name of the class for which the logger is being created. This is typically used to include the class name in log messages for better traceability.
     */
    private Log(String className) {
        // Private constructor to prevent instantiation
        this.className = className;
    }

    /**
     * Factory method to obtain a logger instance for a specific class.
     * This method creates and returns a new Log instance associated with the provided class name.
     *
     * @param className The name of the class for which the logger is being created. This is typically the simple name of the class (e.g., MyClass.class.getSimpleName()).
     * @return A new Log instance that can be used for logging messages related to the specified class.
     */
    public static Log getLogger(String className) {
        return new Log(className);
    }

    /**
     * Logs a simple informational message.
     * By default, the message is logged with the {@link Level#INFORMATION} severity.
     *
     * @param message A string message to log.
     */
    public void log(String message, Object... parameters) {
        processLog(message, Level.CONSOLE, parameters);
    }

    /**
     * Logs an informational message with optional parameters for formatting.
     * The message is formatted using the provided parameters if any are given.
     *
     * @param message    The informational message to log, which should be an instance of {@link InformationMessage}.
     * @param parameters Optional parameters to format the message. If provided, the message will be formatted
     *                   using these parameters before logging.
     */
    public void info(String message, Object... parameters) {
        processLog(message, Level.INFORMATION, parameters);
    }

    /**
     * Logs a warning message with optional parameters for formatting.
     * The message is formatted using the provided parameters if any are given.
     *
     * @param message    The warning message to log, which should be an instance of {@link WarningMessage}.
     * @param parameters Optional parameters to format the message. If provided, the message will be formatted
     *                   using these parameters before logging.
     */
    public void warn(String message, Object... parameters) {
        processLog(message, Level.WARNING, parameters);
    }

    /**
     * Logs an error message with optional parameters for formatting.
     * The message is formatted using the provided parameters if any are given.
     *
     * @param message    The error message to log, which should be an instance of {@link ErrorMessage}.
     * @param parameters Optional parameters to format the message. If provided, the message will be formatted
     *                   using these parameters before logging.
     */
    public void error(String message, Object... parameters) {
        processLog(message, Level.ERROR, parameters);
    }

    /**
     * Logs a fatal error message with optional parameters for formatting.
     * The message is formatted using the provided parameters if any are given.
     *
     * @param message    The fatal error message to log, which should be an instance of {@link FatalErrorMessage}.
     * @param parameters Optional parameters to format the message. If provided, the message will be formatted
     *                   using these parameters before logging.
     */
    public void fatal(String message, Object... parameters) {
        processLog(message, Level.FATAL_ERROR, parameters);
    }

    /**
     * Logs a debug message with optional parameters for formatting.
     * The message is formatted using the provided parameters if any are given.
     *
     * @param message    The debug message to log, which should be an instance of {@link DebugMessage}.
     * @param parameters Optional parameters to format the message. If provided, the message will be formatted
     *                   using these parameters before logging.
     */
    public void debug(String message, Object... parameters) {
        processLog(message, Level.DEBUG, parameters);
    }

    /**
     * Logs a trace message with optional parameters for formatting.
     * The message is formatted using the provided parameters if any are given.
     *
     * @param message    The trace message to log, which should be an instance of {@link TraceMessage}.
     * @param parameters Optional parameters to format the message. If provided, the message will be formatted
     *                   using these parameters before logging.
     */
    public void trace(String message, Object... parameters) { processLog( message, Level.TRACE, parameters); }

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
    public void log(Detail... details) {
        AtomicReference<String> message = new AtomicReference<>("No message provided.");
        List<String> logFileNames = new ArrayList<>();

        // Process each detail to extract message, logging level, and file paths
        Arrays.stream(details).forEach(detail -> {

            if (detail instanceof Message) {
                message.set(((Message) detail).getMessage());
            }
            if (detail instanceof LogFile) {
                logFileNames.addAll(Arrays.asList(((LogFile) detail).filename()));
            }
        });

        // Log the message to the console (for demonstration purposes)
        System.out.println(message.get());

        // Write the log message to the specified files
        logFileNames.forEach(fileName -> {

            String logsDir = Config.get("LogsDir", "").replace("\"", "");
            if (logsDir.isEmpty()) {
                logsDir = Util.getJarLocation();
            }

            Path filePath = new File(logsDir + File.separator + fileName).toPath();

            FileManager.write(message.get(), filePath);
        });

    }

    /**
     * Processes a log message by determining its severity level, formatting it with provided parameters,
     * and preparing it for logging. This method constructs the final log message based on the input text,
     * logging level, and any additional parameters that may be included in the values array.
     *
     * @param text   The base message text to be logged, which may contain placeholders for parameters.
     * @param level  The severity level of the log message (e.g., ERROR, WARNING, INFO).
     * @param values An array of objects that may include additional details such as log files or parameters
     *               for formatting the message. The method will differentiate between these types to
     *               construct the final log entry appropriately.
     */
    private void processLog(String text, Level level, Object[] values) {

        ArrayList<Detail> detailsList = new ArrayList<>();
        AtomicReference<Message> message = new AtomicReference<>();
        ArrayList<String> parameters = new ArrayList<>();

        Arrays.stream(values).iterator().forEachRemaining(detail -> {
            if (detail instanceof LogFile) {
                detailsList.add((Detail) detail);
            } else {
                parameters.add(String.valueOf(detail));
            }
        });

        String formatMessage = formatMessage(text, level, parameters.toArray());

        if (level == Level.ERROR) {
            detailsList.add(new ErrorMessage(formatMessage));
        } else if (level == Level.FATAL_ERROR) {
            detailsList.add(new FatalErrorMessage(formatMessage));
        } else if (level == Level.WARNING) {
            detailsList.add(new WarningMessage(formatMessage));
        } else if (level == Level.DEBUG) {
            detailsList.add(new DebugMessage(formatMessage));
        } else if (level == Level.TRACE) {
            detailsList.add(new TraceMessage(formatMessage));
        } else {
            detailsList.add(new InformationMessage(formatMessage));
        }

        Detail[] detailsListArray = detailsList.toArray(new Detail[0]);

        log(detailsListArray);
    }

    /**
     * Formats a log message by replacing placeholders with provided parameters and prefixing it with the logging level.
     * The method takes a message template, a logging level, and an array of parameters to replace placeholders in the message.
     *
     * @param message    The message template containing placeholders (e.g., "{}") for parameter substitution.
     * @param level      The logging level to prefix the message with (e.g., "INFO", "ERROR").
     * @param parameters An array of objects to replace the placeholders in the message template.
     *                   Each placeholder will be replaced sequentially with the corresponding parameter value.
     * @return A formatted string that includes the logging level and the message with all placeholders replaced by their respective parameter values.
     */
    private String formatMessage(String message, Level level, Object... parameters) {

        AtomicReference<String> messageRef = new AtomicReference<>(message);

        Integer debugMode = Config.get("Log.Debug", 1);

        String showClassName = "";
        if (Config.get("Log.ShowClassName", 1) == 1) {
            showClassName = "[" + className + "] - ";
        }

        if (level != Level.CONSOLE) {
            String formattedDate = DataFormat.format(new Date(), DataFormat.REGEX_DATE_LOG_EVENT);
            messageRef.set(formattedDate + " - [ " + level + " ]" + showClassName + messageRef.get());
        }

        Arrays.stream(parameters).iterator().forEachRemaining(param -> {
            String value = "";
            if (param instanceof Exception) {
                StackTraceElement[] stackTrace = ((Exception) param).getStackTrace();
                AtomicReference<String> stackTraceRef = new AtomicReference<>("");
                Arrays.stream(stackTrace).iterator().forEachRemaining(stackTraceElement -> {
                    stackTraceRef.set(stackTraceRef.get() + stackTraceElement.toString() + "\n");
                });
                value = stackTraceRef.get();
            } else {
                value = String.valueOf(param);
            }

            messageRef.set(
                    messageRef.get().replaceFirst("\\{}", value)
            );
        });
        return messageRef.get();
    }

    public boolean isDebugEnabled() {
        return debugEnabled;
    }

    public void setDebugEnabled(boolean debugEnabled) {
        this.debugEnabled = debugEnabled;
    }
}
