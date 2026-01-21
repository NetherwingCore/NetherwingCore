package br.net.dd.netherwingcore.common.logging;

/**
 * Represents a fatal error message in the system logging framework.
 * <p>
 * This class is a specialized {@link Message} that is always associated with the
 * logging level {@link Level#FATAL_ERROR}. It can be used to represent critical
 * errors that require immediate attention, such as system crashes or severe issues.
 * </p>
 */
public class FatalErrorMessage extends Message {

    /**
     * Constructs a {@code FatalErrorMessage} with the specified message content.
     * <p>
     * The message content represents the details of the fatal error, and the
     * logging level is automatically set to {@link Level#FATAL_ERROR}.
     * </p>
     *
     * @param message the details of the fatal error message
     */
    public FatalErrorMessage(String message) {
        super(message);
        level = Level.FATAL_ERROR;
    }
}
