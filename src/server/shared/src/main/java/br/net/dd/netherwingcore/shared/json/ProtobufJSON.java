package br.net.dd.netherwingcore.shared.json;

import com.google.gson.stream.JsonWriter;
import com.google.protobuf.Descriptors;
import com.google.protobuf.Message;

import java.io.IOException;
import java.util.List;

public class ProtobufJSON {

    static JsonWriter writer;

    public static String serialize(Message message){
        Serializer serializer = new Serializer();
        serializer.writeMessage(message);
        return serializer.getString();
    }

    public static boolean deserialize(String json, Message message){
        Deserializer deserializer = new Deserializer();
        return deserializer.readMessage(json, message);
    }

    public static class Serializer{

        public void writeMessage(Message message) {
            List<Descriptors.FieldDescriptor> fields = message.getDescriptorForType().getFields();
            try {
                writer.beginObject();

                fields.forEach(field -> {
                    writeMessageField(message, field);
                });

                writer.endObject();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        private void writeMessageField(Message message, Descriptors.FieldDescriptor field) {
            try {
                writer.name(field.getName());
                if(field.isRepeated()){
                    writer.beginArray();
                    writeRepeatedMessageField(message, field);
                    writer.endArray();
                } else {
                    writeSimpleMessageField(message, field);
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        private void writeSimpleMessageField(Message message, Descriptors.FieldDescriptor field) {
            try {
                Object value = message.getField(field);
                switch (field.getJavaType()) {
                    case INT:
                        writer.value((Integer) value);
                        break;
                    case LONG:
                        writer.value((Long) value);
                        break;
                    case FLOAT:
                        writer.value((Float) value);
                        break;
                    case DOUBLE:
                        writer.value((Double) value);
                        break;
                    case BOOLEAN:
                        writer.value((Boolean) value);
                        break;
                    case STRING:
                        writer.value((String) value);
                        break;
                    case BYTE_STRING:
                        writer.value(value.toString());
                        break;
                    case ENUM:
                        writer.value(((Descriptors.EnumValueDescriptor) value).getName());
                        break;
                    case MESSAGE:
                        writeMessage((Message) value);
                        break;
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        private void writeRepeatedMessageField(Message message, Descriptors.FieldDescriptor field) {

        }

        public String getString() {
            return null;
        }
    }

    public static class Deserializer{
        public boolean readMessage(String json, Message message) {
            return false;
        }
    }

}
