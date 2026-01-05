package br.net.dd.netherwingcore.common.configuration.fields;

public class Key extends Field {

    public Key(String... values) {
        super(values);
    }

    @Override
    public String getValue() {
        return super.getValues()[0];
    }
}
