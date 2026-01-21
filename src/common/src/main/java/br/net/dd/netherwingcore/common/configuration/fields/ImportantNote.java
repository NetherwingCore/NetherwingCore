package br.net.dd.netherwingcore.common.configuration.fields;

/**
 * Represents an important note within the configuration fields.
 *
 * <p>
 * This class extends the {@link Field} class and allows the creation of a field
 * that can store one or more values representing an important note. It can be
 * used to highlight critical messages or instructions that should be emphasized
 * within the configuration.
 * </p>
 *
 * <p>
 * Example usage:
 * </p>
 * <pre>
 *     ImportantNote note = new ImportantNote("This is an important note.");
 * </pre>
 *
 * @see Field
 */
public class ImportantNote extends Field {

    /**
     * Constructs a new {@code ImportantNote} instance with the specified value(s).
     *
     * @param value one or more string values representing the content of the important note
     */
    public ImportantNote(String... value) {
        super(value);
    }

}
