package br.net.dd.netherwingcore.common.configuration.fields;

/**
 * The {@code Type} enumeration represents the different kinds of configuration field types
 * that can exist in the application. This serves to categorize the fields based on their
 * content or expected value format, making it easier to interpret and validate data.
 *
 * <ul>
 *   <li>{@link #TEXT}: Denotes a field that contains textual data.</li>
 *   <li>{@link #NUMBER}: Represents a field that stores numeric data.</li>
 *   <li>{@link #UNDEFINED}: Used for fields where the type is unspecified or unknown.</li>
 * </ul>
 *
 * This enum is designed to enable strict type checking and improve code readability
 * when dealing with configuration fields.
 * @author Netherwing Dev Team
 */
public enum Type {
    /**
     * Indicates a field that contains textual data.
     */
    TEXT,

    /**
     * Indicates a field that contains numeric data.
     */
    NUMBER,

    /**
     * Indicates a field whose type is unspecified or not yet defined.
     */
    UNDEFINED
}
