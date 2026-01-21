package br.net.dd.netherwingcore.common.configuration.fields;

/**
 * The {@code Key} class represents a specific type of {@link Field}, which is used to handle configuration fields.
 * This class is responsible for encapsulating values where the first value is considered the key.
 *
 * <p>
 * It extends the functionality of the {@code Field} class by providing direct access to the first value
 * supplied during instantiation through the {@code getValue()} method.
 * </p>
 *
 * <h2>Usage</h2>
 * <pre>{@code
 * Key key = new Key("myKey", "otherValue");
 * String value = key.getValue(); // Returns "myKey"
 * }</pre>
 *
 * @see Field
 */
public class Key extends Field {

    /**
     * Constructs a new {@code Key} instance with the provided values.
     *
     * @param values The values associated with this {@code Key}.
     *               The first value is treated as the key.
     */
    public Key(String... values) {
        super(values);
    }

    /**
     * Retrieves the first value of this field, which is treated as the key.
     *
     * @return The first value of the {@code Key}.
     */
    @Override
    public String getValue() {
        return super.getValues()[0];
    }
}
