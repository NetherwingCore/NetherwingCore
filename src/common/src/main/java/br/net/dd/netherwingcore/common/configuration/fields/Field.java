package br.net.dd.netherwingcore.common.configuration.fields;

public abstract class Field {

    private String value;

    private String[] values;

    private Type type;

    public Field(String... values) {
        this.setValues(values);
    }

    public Field(String value, Type type) {
        this.setValue(value);
        this.setType(type);
    }

    public String[] getValues() {
        return values;
    }

    public void setValues(String[] values) {
        this.values = values;
    }

    public String getValue() { return value; }

    public void setValue(String value) { this.value = value; }

    public Type getType() { return type; }

    public void setType(Type type) { this.type = type; }
}
