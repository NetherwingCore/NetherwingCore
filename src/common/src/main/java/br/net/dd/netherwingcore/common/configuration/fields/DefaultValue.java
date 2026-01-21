package br.net.dd.netherwingcore.common.configuration.fields;

/**
 * Represents a default value in the configuration system.
 *
 * <p>This class extends the {@link Field} class, indicating that it is a specialized type of configuration field
 * that represents one or more default values. It allows the specification of default values for associated
 * fields in the configuration.</p>
 *
 * <p>The {@code DefaultValue} constructor accepts one or more string values, which are passed to the superclass
 * {@code Field}. These values can be used to specify default configuration values in the system. This class
 * ensures that default values are treated uniformly throughout the configuration process.</p>
 *
 * Example Usage:
 * <pre>
 * DefaultValue defaultValue = new DefaultValue("value1", "value2");
 * </pre>
 *
 * @see Field
 */
public class DefaultValue extends Field{

    /**
     * Constructs a {@code DefaultValue} instance with the given values.
     *
     * @param value One or more default values to be associated with this field.
     *              These values will be passed to the superclass constructor.
     */
    public DefaultValue(String... value) {
        super(value);
    }

}
