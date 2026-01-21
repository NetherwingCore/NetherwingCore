package br.net.dd.netherwingcore.common.configuration.structs;

import br.net.dd.netherwingcore.common.configuration.fields.Field;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a group of configuration elements in the system. A group can contain
 * a collection of {@link Item} objects and a collection of {@link Field} objects.
 * This class provides methods to add items and fields to the group as well
 * as retrieve them.
 */
public class Group {

    /**
     * A list of items associated with the group.
     */
    private final List<Item> items;

    /**
     * A list of fields associated with the group.
     */
    private final List<Field> fields;

    /**
     * Constructs an empty group with no items or fields.
     * Initializes the lists to hold items and fields.
     */
    public Group() {
        this.items = new ArrayList<>();
        this.fields = new ArrayList<>();
    }

    /**
     * Adds a field to the group.
     *
     * @param field the {@link Field} to be added
     * @return the updated {@code Group} instance
     */
    public Group addField(Field field) {
        this.fields.add(field);
        return this;
    }

    /**
     * Adds an item to the group.
     *
     * @param item the {@link Item} to be added
     * @return the updated {@code Group} instance
     */
    public Group addItem(Item item) {
        this.items.add(item);
        return this;
    }

    /**
     * Retrieves the list of items associated with the group.
     *
     * @return a {@code List} of {@link Item} objects
     */
    public List<Item> getItems() {
        return items;
    }

    /**
     * Retrieves the list of fields associated with the group.
     *
     * @return a {@code List} of {@link Field} objects
     */
    public List<Field> getFields() {
        return fields;
    }

}
