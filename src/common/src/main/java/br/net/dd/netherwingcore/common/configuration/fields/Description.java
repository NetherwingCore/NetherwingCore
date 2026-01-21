package br.net.dd.netherwingcore.common.configuration.fields;

/**
 * Represents a description field in the configuration system.
 * <p>
 * This class is a specific implementation of the {@link Field} class, and is designed to handle
 * fields that represent descriptions in a flexible and extensible way.
 * </p>
 *
 * <p>
 * The constructor takes one or more {@code String} values, which are passed directly to the
 * superclass {@link Field}.
 * </p>
 *
 * <p>
 * Example usage:
 * <pre>
 *     Description description = new Description("This is the first part.", "This is the second part.");
 * </pre>
 * </p>
 *
 * @see Field
 */
public class Description extends Field{

    /**
     * Constructs a {@code Description} object with the provided value(s).
     *
     * @param value One or more strings representing the description's content.
     */
    public Description(String... value) {
        super(value);
    }

}
