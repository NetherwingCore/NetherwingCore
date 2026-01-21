package br.net.dd.netherwingcore.common.logging;

/**
 * Represents an informational message in the logging system.
 * This class extends the {@link Message} class, adding a predefined log {@link Level}
 * to indicate that the message is informational in nature.
 * <p>
 * Example usage:
 * <pre>
 * InformationMessage infoMessage = new InformationMessage("This is an informational log");
 * </pre>
 */
public class InformationMessage extends Message {

    /**
     * Constructs a new {@code InformationMessage} with the specified message text.
     *
     * @param message the text of the informational message
     */
    public InformationMessage(String message) {
        super(message);
        level = Level.INFORMATION;
    }
}
