package br.net.dd.netherwingcore.common.configuration.structs;

import br.net.dd.netherwingcore.common.configuration.fields.Field;
import br.net.dd.netherwingcore.common.configuration.fields.Value;

public record Item(Field... fields) {

    public Value getValue() {
        for (Field field : fields) {
            if (field instanceof Value) {
                return (Value) field;
            }
        }
        return  null;
    }

}

