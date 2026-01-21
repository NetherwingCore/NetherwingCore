package br.net.dd.netherwingcore.common.configuration.fields;

/**
 * Represents a developer note field in the configuration. This class is a specialized
 * extension of the {@code Field} class, allowing developers to include additional
 * notes or metadata as needed.
 *
 * <p>
 * Instances of this class can be initialized with one or more string values, which are
 * passed to the superclass {@code Field} for further handling.
 * </p>
 *
 * <h3>Usage Example:</h3>
 * <pre>
 * {@code
 * DeveloperNote note = new DeveloperNote("Important info", "Review before release");
 * }
 * </pre>
 *
 * @see Field
 */
public class DeveloperNote extends Field{

    /**
     * Constructs a new {@code DeveloperNote} with the given value(s).
     *
     * @param value One or more string values representing the developer note contents.
     */
    public DeveloperNote(String... value) {
        super(value);
    }

}
