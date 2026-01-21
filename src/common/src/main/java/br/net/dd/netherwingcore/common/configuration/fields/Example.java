package br.net.dd.netherwingcore.common.configuration.fields;

/**
 * Represents an example configuration field in the system.
 *
 * <p>This class is a specialization of the {@link Field} class. It allows the creation
 * of a generic field by passing one or more string values during instantiation.
 * It serves as a base example and can be extended or used directly
 * to represent configuration fields with string values.</p>
 *
 * <p>Usage:</p>
 * <pre>{@code
 * Example exampleField = new Example("value1", "value2");
 * }</pre>
 *
 * @see Field
 */
public class Example extends Field{

    /**
     * Constructs an instance of the {@code Example} class
     * with the given string values.
     *
     * @param value One or more string values associated with this field.
     */
    public Example(String... value) {
        super(value);
    }

}
