package br.net.dd.netherwingcore.common.configuration.fields;

/**
 * Represents a specific type of {@link Field}, called "Detail", which encapsulates
 * one or more values. This class acts as a detailed configuration option within
 * the system.
 *
 * <p>
 * The {@code Detail} class is a specialization of the {@code Field} class,
 * inheriting its ability to store string values and providing a constructor
 * for initialization. Instances of this class are intended to represent
 * configuration fields that require detailed or additional context.
 * </p>
 *
 * Example:
 * <pre>
 *     Detail detail = new Detail("exampleValue1", "exampleValue2");
 * </pre>
 *
 * @see Field
 */
public class Detail extends Field {

    /**
     * Constructs a new {@code Detail} instance with the specified values.
     *
     * @param value one or more string values representing this detail's configuration.
     */
    public Detail(String... value) {
        super(value);
    }

}
