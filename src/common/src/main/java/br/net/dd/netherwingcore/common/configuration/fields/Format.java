package br.net.dd.netherwingcore.common.configuration.fields;

/**
 * The {@code Format} class represents a specific type of configuration field
 * that accepts one or more string values.
 * This class extends the {@code Field} class, inheriting its behavior
 * and adding its own specialized functionality for handling format configurations.
 *
 * <p>
 * Use this class when you need to define a configuration field that expects
 * a format-related value(s). The format values are passed as strings during
 * the instantiation.
 * </p>
 *
 * <p>
 * Example usage:
 * <pre>
 *     Format formatField = new Format("value1", "value2");
 * </pre>
 * </p>
 *
 * @see Field
 */
public class Format extends Field {

    /**
     * Constructs a {@code Format} object with the specified values.
     *
     * @param value one or more string values to define the format configuration field.
     */
    public Format(String... value) {
        super(value);
    }

}
