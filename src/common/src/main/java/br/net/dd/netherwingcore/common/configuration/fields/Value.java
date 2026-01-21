package br.net.dd.netherwingcore.common.configuration.fields;

/**
 * The {@code Value} class represents a specific implementation of a {@code Field}
 * that allows for the configuration of values within the NetherwingCore framework.
 *
 * <p>This class extends the {@code Field} class, inheriting its functionality
 * and providing constructors for different ways of initializing {@code Value} objects.
 * It is primarily used to encapsulate and handle value-related configurations in the system.</p>
 *
 * @see Field
 */
public class Value extends Field {

    /**
     * Constructs a {@code Value} object with a single value part and a specified type.
     *
     * @param valuePart a {@code String} representing the value part.
     * @param type the {@code Type} of the value, indicating its configuration type.
     */
    public Value(String valuePart, Type type) {
        super(valuePart, type);
    }

    /**
     * Constructs a {@code Value} object with multiple value parts.
     *
     * <p>This constructor can be used when the value comprises multiple parts
     * to be handled as a unit.</p>
     *
     * @param value a varargs parameter representing one or more value parts as strings.
     */
    public Value(String... value) {
        super(value);
    }

}
