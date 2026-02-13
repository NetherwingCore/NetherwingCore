package br.net.dd.netherwingcore.common.logging;

/**
 * Represents a trace message in the logging system.
 * This class extends the base {@link Message} class,
 * and it is categorized specifically as a trace message by
 * setting the {@link Level} to {@code Level.TRACE}.
 *
 * <p>This class can be used to differentiate between
 * various types of log messages in the application, particularly for very fine-grained logging.</p>
 *
 * <h2>Example Usage:</h2>
 * <pre>
 *     TraceMessage trace = new TraceMessage("This is a trace message");
 *     System.out.println(trace.getMessage());
 * </pre>
 *
 * @see Message
 */
public class TraceMessage extends Message {

    /**
     * Constructs a {@code TraceMessage} with the specified message content.
     * The severity level is automatically set to {@code Level.TRACE}.
     *
     * @param message The content of the trace message.
     */
    public TraceMessage(String message) {
        super(message);
        level = Level.TRACE;
    }
}
