package br.net.dd.netherwingcore.common.configuration.structs;

import br.net.dd.netherwingcore.common.configuration.fields.Field;
import br.net.dd.netherwingcore.common.configuration.fields.Value;

/**
 * This class represents an immutable and structured collection of {@link Field} objects.
 * It is implemented as a record, providing a concise representation of data with a fixed set of fields.
 *
 * The {@code Item} class allows accessing an embedded {@link Value} instance, if present,
 * among its collection of {@link Field} objects.
 * <p>
 * Usage example:
 * <pre>
 * {@code
 * Field field1 = new SomeField("example");
 * Value valueField = new Value("value");
 * Item item = new Item(field1, valueField);
 *
 * Value retrievedValue = item.getValue();
 * }
 * </pre>
 */
public record Item(Field... fields) {

    /**
     * Iterates through the fields contained in this {@code Item} and returns the first
     * {@link Value} instance encountered.
     *
     * @return the first {@link Value} field found, or {@code null} if no {@link Value} field exists.
     */
    public Value getValue() {
        for (Field field : fields) {
            if (field instanceof Value) {
                return (Value) field;
            }
        }
        return  null;
    }

}

