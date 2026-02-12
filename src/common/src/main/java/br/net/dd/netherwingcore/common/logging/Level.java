package br.net.dd.netherwingcore.common.logging;

/**
 * Represents the different levels of logging severity that can be used in the application.
 * These levels help categorize and prioritize log entries based on their importance
 * and the nature of the information being logged.
 *
 * <p>Available levels:</p>
 * <ul>
 *   <li>{@link #INFORMATION} - General information about system operations</li>
 *   <li>{@link #WARNING} - Indication of a potential issue or cautionary notice</li>
 *   <li>{@link #ERROR} - An error that impacts the current operation, but the system can continue</li>
 *   <li>{@link #FATAL_ERROR} - A critical error that may cause the system to terminate</li>
 *   <li>{@link #DEBUG} - Detailed information useful for debugging purposes</li>
 * </ul>
 */
public enum Level {
    /**
     * General information about the operation of the system, used for non-critical log messages.
     */
    INFORMATION,

    /**
     * Represents a warning that indicates a potential issue in the system,
     * but does not stop its operation.
     */
    WARNING,

    /**
     * Represents an error that causes an operation to fail, but
     * the system continues running.
     */
    ERROR,

    /**
     * Represents a severe error that affects the system's ability to continue operating.
     * This level is used for critical issues.
     */
    FATAL_ERROR,

    /**
     * Represents debug-level information used for development and troubleshooting purposes.
     */
    DEBUG,

    /**
     * Represents log messages that are intended to be output to the console.
     * This level is used for messages that should be visible in the console output,
     * regardless of the configured log file targets.
     */
    CONSOLE
}
