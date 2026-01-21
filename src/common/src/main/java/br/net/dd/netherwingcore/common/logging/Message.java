package br.net.dd.netherwingcore.common.logging;

/**
 * Represents a custom exception that can encapsulate additional details, including
 * a logging level. This class extends {@code Exception} and implements the {@code Detail} interface.
 * It can be used to associate specific log levels with exception messages.
 */
public class Message extends Exception implements Detail{

    // Represents the log level associated with this exception.
    Level level;

    /**
     * Constructs a new {@code Message} exception with the specified detail message.
     *
     * @param message The detail message, which is saved for later retrieval by the {@link #getMessage()} method.
     */
    public Message(String message) {
        super(message);
    }

    /**
     * Retrieves the log level associated with this exception.
     *
     * @return The logging level defined in this exception. May be null if it has not been set.
     */
    public Level getLevel() {
        return level;
    }
}
