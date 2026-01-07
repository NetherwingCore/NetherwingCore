package br.net.dd.netherwingcore.shared.json;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.google.protobuf.Descriptors;
import com.google.protobuf.Message;

import java.io.IOException;
import java.util.List;

public class ProtobufJSON {

    static JsonWriter writer;
    static JsonReader reader;

    public static String serialize(Message message) {
        Serializer serializer = new Serializer();
        serializer.writeMessage(message);
        return serializer.getString();
    }

    public static boolean deserialize(String json, Message message) {
        Deserializer deserializer = new Deserializer();
        return deserializer.readMessage(json, message);
    }

    private static class Serializer {

        public void writeMessage(Message message) {

            writer = new JsonWriter(new java.io.StringWriter());

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
                if (field.isRepeated()) {
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
            Object value = message.getField(field);
            writeMessageFieldValue(field, value);
        }

        private void writeRepeatedMessageField(Message message, Descriptors.FieldDescriptor field) {
            Object value = message.getField(field);
            List<?> values = (List<?>) value;
            values.forEach(item -> {
                writeMessageFieldValue(field, item);
            });
        }

        private void writeMessageFieldValue(Descriptors.FieldDescriptor field, Object value) {
            try {
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
                    default:
                        break;
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        public String getString() {
            return writer.toString();
        }

    }

    private static class Deserializer {

        public boolean readMessage(String json, Message message) {

            reader = new JsonReader(new java.io.StringReader(json));
            try {
                reader.beginObject();

                Descriptors.Descriptor descriptor = message.getDescriptorForType();
                List<Descriptors.FieldDescriptor> fields = descriptor.getFields();

                while (reader.hasNext()) {
                    String fieldName = reader.nextName();
                    Descriptors.FieldDescriptor field = descriptor.findFieldByName(fieldName);
                    if (field != null) {
                        Object value = null;
                        switch (field.getJavaType()) {
                            case INT:
                                value = reader.nextInt();
                                break;
                            case LONG:
                                value = reader.nextLong();
                                break;
                            case FLOAT:
                                value = (float) reader.nextDouble();
                                break;
                            case DOUBLE:
                                value = reader.nextDouble();
                                break;
                            case BOOLEAN:
                                value = reader.nextBoolean();
                                break;
                            case STRING:
                                value = reader.nextString();
                                break;
                            case ENUM:
                                String enumName = reader.nextString();
                                value = field.getEnumType().findValueByName(enumName);
                                break;
                            case MESSAGE:
                                break;
                            default:
                                reader.skipValue();
                                break;
                        }

                        if (value != null) {
                            message.toBuilder().setField(field, value);
                        }

                    } else {
                        reader.skipValue();
                    }
                }

                reader.endObject();
                return true;
            } catch (IOException e) {
                return false;
            }

        }

    }

}
