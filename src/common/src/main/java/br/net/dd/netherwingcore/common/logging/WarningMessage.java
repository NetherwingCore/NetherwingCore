package br.net.dd.netherwingcore.common.logging;

/**
 * Represents a warning message in the logging system.
 * This class extends the base {@link Message} class,
 * and it is categorized specifically as a warning by
 * setting the {@link Level} to {@code Level.WARNING}.
 *
 * <p>This class can be used to differentiate between
 * various types of log messages in the application.</p>
 *
 * <h2>Example Usage:</h2>
 * <pre>
 *     WarningMessage warning = new WarningMessage("This is a warning message");
 *     System.out.println(warning.getMessage());
 * </pre>
 *
 * @see Message
 */
public class WarningMessage extends Message {

    /**
     * Constructs a {@code WarningMessage} with the specified message content.
     * The severity level is automatically set to {@code Level.WARNING}.
     *
     * @param message The content of the warning message.
     */
    public WarningMessage(String message) {
        super(message);
        level = Level.WARNING;
    }
}
