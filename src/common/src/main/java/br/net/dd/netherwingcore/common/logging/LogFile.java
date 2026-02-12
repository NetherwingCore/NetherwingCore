package br.net.dd.netherwingcore.common.logging;

import java.nio.file.Path;

/**
 * Represents a log file target for logging messages. This record encapsulates the filename(s) of the log file(s)
 * where log messages should be written. It implements the {@link Detail} interface, allowing it to be used as
 * a detail in log messages to specify the target log file(s).
 *
 * <p>Example usage:</p>
 * <pre>
 *     Log.log("This is an informational message.", new LogFile("application.log"));
 * </pre>
 *
 * @param filename The name(s) of the log file(s) where messages should be logged.
 */
public record LogFile(String... filename) implements Detail {
}
