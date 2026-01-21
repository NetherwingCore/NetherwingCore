package br.net.dd.netherwingcore.common.logging;

/**
 * The {@code ErrorMessage} class represents a specialized type of {@code Message}
 * that is used to log error-level messages in the system. It extends the {@code Message}
 * class and automatically sets the {@code level} to {@code Level.ERROR}.
 *
 * <p>This class can be used whenever an error message needs to be logged or handled,
 * ensuring it is appropriately flagged as an error-level message.
 *
 * Example usage:
 * <pre>
 *     ErrorMessage errorMsg = new ErrorMessage("An error occurred");
 *     System.out.println(errorMsg);
 * </pre>
 *
 * <p>Note: The {@code Level} enum and the {@code Message} class are assumed to be defined elsewhere
 * in the application.
 */
public class ErrorMessage extends Message {

    /**
     * Constructs a new {@code ErrorMessage} instance with the specified message content.
     *
     * @param message The content of the error message.
     */
    public ErrorMessage(String message) {
        super(message);
        level = Level.ERROR;
    }
}
