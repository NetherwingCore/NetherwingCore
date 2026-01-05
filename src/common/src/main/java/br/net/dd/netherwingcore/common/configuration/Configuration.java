package br.net.dd.netherwingcore.common.configuration;

import br.net.dd.netherwingcore.common.configuration.fields.Description;
import br.net.dd.netherwingcore.common.configuration.fields.Field;
import br.net.dd.netherwingcore.common.configuration.fields.Key;
import br.net.dd.netherwingcore.common.configuration.structs.Item;
import br.net.dd.netherwingcore.common.configuration.structs.Section;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static br.net.dd.netherwingcore.common.configuration.fields.Type.*;

public class Configuration {

    private final Description description;
    private final String serviceName;
    private final List<Section> sections;

    public Configuration() {
        this.description = new Description("");
        this.serviceName = "";
        this.sections = new ArrayList<>();
    }

    public Configuration(Description description, String serviceName) {
        this.description = description;
        this.serviceName = serviceName;
        this.sections = new ArrayList<>();
    }

    public Configuration addSection(Section section) {
        this.sections.add(section);
        return this;
    }

    public Item get(Key key){

        AtomicReference<Item> reference = new AtomicReference<>(null);

        sections.forEach(section -> {
            section.getGroups().forEach(group -> {
                group.getItems().forEach(item -> {
                    List<Field> records = List.of(item.fields());
                    records.forEach(record -> {
                        if (record instanceof Key){
                            if (((Key) record).getValue().equals(key.getValue())){
                                reference.set(item);
                                return;
                            }
                        }
                    });
                });
            });
        });

        return reference.get();
    }

    public Integer get(String key, Integer defaultValue){

        Item item = get(new Key(key));

        if(item == null){
            return defaultValue;
        }

        if (item.getValue() == null){
            return defaultValue;
        }

        if (item.getValue().getType().equals(NUMBER)){
            return Integer.parseInt(item.getValue().getValue());
        }

        return defaultValue;
    }

    public String get(String key, String defaultValue){

        Item item = get(new Key(key));

        if(item == null){
            return defaultValue;
        }

        if (item.getValue() == null){
            return defaultValue;
        }

        if (item.getValue().getType().equals(TEXT)){
            return item.getValue().getValue();
        }

        return defaultValue;
    }

    public Description getDescription() {
        return description;
    }

    public String getServiceName() {
        return serviceName;
    }

    public List<Section> getSections() {
        return sections;
    }

}
