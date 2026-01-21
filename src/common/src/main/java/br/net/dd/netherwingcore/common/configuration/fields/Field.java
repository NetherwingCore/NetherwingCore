package br.net.dd.netherwingcore.common.configuration.fields;

/**
 * Abstract class representing a generic field configuration.
 * This class serves as a base for creating fields with different values and types.
 * It provides mechanisms to manage single or multiple values and handles field types.
 */
public abstract class Field {

    /**
     * The value of the field when it represents a single entry.
     */
    private String value;

    /**
     * The values of the field when it represents multiple entries.
     */
    private String[] values;

    /**
     * The type of this field.
     */
    private Type type;

    /**
     * Constructor to initialize the field with multiple values.
     *
     * @param values An array of strings representing the field's multiple values.
     */
    public Field(String... values) {
        this.setValues(values);
    }

    /**
     * Constructor to initialize the field with a single value and its type.
     *
     * @param value The single value assigned to the field.
     * @param type  The type of the field.
     */
    public Field(String value, Type type) {
        this.setValue(value);
        this.setType(type);
    }

    /**
     * Retrieves the values of the field.
     *
     * @return An array of strings representing the field's values.
     */
    public String[] getValues() {
        return values;
    }

    /**
     * Sets the values of the field.
     *
     * @param values An array of strings representing the new values for the field.
     */
    public void setValues(String[] values) {
        this.values = values;
    }

    /**
     * Retrieves the single value of the field.
     *
     * @return The string value of the field.
     */
    public String getValue() { return value; }

    /**
     * Sets the single value of the field.
     *
     * @param value The new string value to be assigned to the field.
     */
    public void setValue(String value) { this.value = value; }

    /**
     * Retrieves the type of the field.
     *
     * @return The {@link Type} of the field.
     */
    public Type getType() { return type; }

    /**
     * Sets the type of the field.
     *
     * @param type The new {@link Type} to be assigned to the field.
     */
    public void setType(Type type) { this.type = type; }
}
