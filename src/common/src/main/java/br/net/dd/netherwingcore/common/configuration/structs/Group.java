package br.net.dd.netherwingcore.common.configuration.structs;

import br.net.dd.netherwingcore.common.configuration.fields.Field;

import java.util.ArrayList;
import java.util.List;

public class Group {

    private final List<Item> items;
    private final List<Field> fields;

    public Group() {
        this.items = new ArrayList<>();
        this.fields = new ArrayList<>();
    }

    public Group addField(Field field) {
        this.fields.add(field);
        return this;
    }

    public Group addItem(Item item) {
        this.items.add(item);
        return this;
    }

    public List<Item> getItems() {
        return items;
    }

    public List<Field> getFields() {
        return fields;
    }

}
