package br.net.dd.netherwingcore.shared.json;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.protobuf.Descriptors;
import com.google.protobuf.Message;
import com.google.protobuf.Message.Builder;

public class Deserializer {

    // Deserializes a JSON string into a Protobuf Message object.
    public static void deserialize(String json, Message.Builder builder) throws Exception {
        JsonObject jsonObject = JsonParser.parseString(json).getAsJsonObject();
        deserializeMessage(jsonObject, builder);
    }

    private static void deserializeMessage(JsonObject json, Builder builder) throws Exception {
        Descriptors.Descriptor descriptor = builder.getDescriptorForType();

        for (Descriptors.FieldDescriptor field : descriptor.getFields()) {
            if (json.has(field.getName())) {
                JsonElement jsonElement = json.get(field.getName());
                if (field.isRepeated()) {
                    deserializeRepeatedField(jsonElement, builder, field);
                } else {
                    deserializeField(jsonElement, builder, field);
                }
            }
        }
    }

    private static void deserializeField(JsonElement jsonElement, Builder builder, Descriptors.FieldDescriptor field) throws Exception {
        switch (field.getJavaType()) {
            case INT:
                builder.setField(field, jsonElement.getAsInt());
                break;

            case LONG:
                builder.setField(field, jsonElement.getAsLong());
                break;

            case FLOAT:
                builder.setField(field, jsonElement.getAsFloat());
                break;

            case DOUBLE:
                builder.setField(field, jsonElement.getAsDouble());
                break;

            case BOOLEAN:
                builder.setField(field, jsonElement.getAsBoolean());
                break;

            case STRING:
                builder.setField(field, jsonElement.getAsString());
                break;

            case ENUM:
                Descriptors.EnumDescriptor enumDescriptor = field.getEnumType();
                Descriptors.EnumValueDescriptor valueDescriptor = enumDescriptor.findValueByName(jsonElement.getAsString());

                if (valueDescriptor == null) {
                    throw new Exception("Invalid enum value: " + jsonElement.getAsString());
                }
                builder.setField(field, valueDescriptor);
                break;

            case MESSAGE:
                Builder nestedBuilder = builder.newBuilderForField(field);
                deserializeMessage(jsonElement.getAsJsonObject(), nestedBuilder);
                builder.setField(field, nestedBuilder.build());
                break;

            default:
                throw new UnsupportedOperationException("Unsupported type: " + field.getJavaType());
        }
    }

    private static void deserializeRepeatedField(JsonElement jsonElement, Builder builder, Descriptors.FieldDescriptor field) throws Exception {
        for (JsonElement element : jsonElement.getAsJsonArray()) {
            switch (field.getJavaType()) {
                case INT:
                    builder.addRepeatedField(field, element.getAsInt());
                    break;
                case LONG:
                    builder.addRepeatedField(field, element.getAsLong());
                    break;
                case FLOAT:
                    builder.addRepeatedField(field, element.getAsFloat());
                    break;
                case DOUBLE:
                    builder.addRepeatedField(field, element.getAsDouble());
                    break;
                case BOOLEAN:
                    builder.addRepeatedField(field, element.getAsBoolean());
                    break;
                case STRING:
                    builder.addRepeatedField(field, element.getAsString());
                    break;
                case ENUM:
                    Descriptors.EnumValueDescriptor enumValueDescriptor =
                            field.getEnumType().findValueByName(element.getAsString());

                    if (enumValueDescriptor == null) {
                        throw new Exception("Invalid enum value: " + element.getAsString());
                    }

                    builder.addRepeatedField(field, enumValueDescriptor);
                    break;
                case MESSAGE:
                    Builder nestedBuilder = builder.newBuilderForField(field);
                    deserializeMessage(element.getAsJsonObject(), nestedBuilder);
                    builder.addRepeatedField(field, nestedBuilder.build());
                    break;
                default:
                    throw new UnsupportedOperationException("Unsupported repeated field type: " + field.getJavaType());
            }
        }
    }

}
