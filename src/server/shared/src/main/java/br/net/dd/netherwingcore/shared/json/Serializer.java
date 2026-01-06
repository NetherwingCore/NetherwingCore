package br.net.dd.netherwingcore.shared.json;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.protobuf.Descriptors;
import com.google.protobuf.Message;

import java.util.List;

public class Serializer {

    // Serializes a Protobuf message to JSON.
    public static String serialize(Message message) {
        JsonObject json = new JsonObject();
        serializeMessage(message, json); // Serialize Protobuf fields into JSON object
        return json.toString();
    }

    private static void serializeMessage(Message message, JsonObject json) {
        // Reflection for selecting field descriptors.
        Descriptors.Descriptor descriptor = message.getDescriptorForType();
        List<Descriptors.FieldDescriptor> fields = descriptor.getFields();

        for (Descriptors.FieldDescriptor field : fields) {
            // Check if the field is filled.
            if (field.isRepeated()) {
                serializeRepeatedField(message, field, json);
            } else if (message.hasField(field)) {
                serializeField(message, field, json);
            }
        }
    }

    private static void serializeField(Message message, Descriptors.FieldDescriptor field, JsonObject json) {
        Object value = message.getField(field); // Get value from field

        switch (field.getJavaType()) {
            case INT:
            case LONG:
            case FLOAT:
            case DOUBLE:
            case BOOLEAN, STRING:
                json.addProperty(field.getName(), value.toString());
                break;

            case ENUM:
                json.addProperty(field.getName(), ((Descriptors.EnumValueDescriptor) value).getName());
                break;

            case MESSAGE:
                JsonObject nestedJson = new JsonObject();
                serializeMessage((Message) value, nestedJson);
                json.add(field.getName(), nestedJson);
                break;

            default:
                break;
        }
    }

    private static void serializeRepeatedField(Message message, Descriptors.FieldDescriptor field, JsonObject json) {
        JsonArray array = new JsonArray();
        List<?> repeatedValues = (List<?>) message.getField(field);

        for (Object value : repeatedValues) {
            switch (field.getJavaType()) {
                case INT:
                case LONG:
                case FLOAT:
                case DOUBLE:
                case BOOLEAN:
                case STRING:
                    array.add(value.toString());
                    break;

                case ENUM:
                    array.add(((Descriptors.EnumValueDescriptor) value).getName());
                    break;

                case MESSAGE:
                    JsonObject itemJson = new JsonObject();
                    serializeMessage((Message) value, itemJson);
                    array.add(itemJson);
                    break;

                default:
                    break;
            }
        }

        json.add(field.getName(), array);
    }

}
