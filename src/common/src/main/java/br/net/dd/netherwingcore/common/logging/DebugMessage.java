package br.net.dd.netherwingcore.common.logging;

/**
 * A class representing a debug message within the application.
 * <p>
 * This class extends the {@code Message} class and is specifically used
 * to handle messages at the debug level. It helps differentiate
 * debug-related logs from other log levels in the system.
 * </p>
 *
 * <h2>Usage example:</h2>
 * <pre>{@code
 * DebugMessage debugMessage = new DebugMessage("Debugging application...");
 * System.out.println(debugMessage.toString());
 * }</pre>
 *
 * @see Message
 */
public class DebugMessage extends Message {

    /**
     * Constructs a new DebugMessage with the provided message content.
     * The log level is automatically set to {@code Level.DEBUG}.
     *
     * @param message The debug message content to be logged or processed.
     */
    public DebugMessage(String message) {
        super(message);
        level = Level.DEBUG;
    }
}
