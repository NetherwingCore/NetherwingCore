package br.net.dd.netherwingcore.common.configuration;

import br.net.dd.netherwingcore.common.configuration.fields.*;
import br.net.dd.netherwingcore.common.configuration.structs.Item;
import br.net.dd.netherwingcore.common.utilities.Util;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static br.net.dd.netherwingcore.common.logging.Log.log;
import static br.net.dd.netherwingcore.common.serialization.FileManager.*;

public class ConfigurationController {

    private final Configuration configuration;

    public ConfigurationController(ConfigurationSample configurationSample) {
        String jarLocation = Util.getJarLocation();
        String filePath = jarLocation + File.separator + configurationSample.getFileName();
        String distFilePath = jarLocation + File.separator + configurationSample.getFileName() + ".dist";
        Path configurationFilePath = Path.of(filePath);
        Path configurationDistFilePath = Path.of(distFilePath);

        this.configuration = configurationSample.getConfiguration();

        if (Files.exists(configurationFilePath)) {

            loadConfiguration(configurationFilePath);

        } else {

            log("The configuration file was not found.");

            if (!Files.exists(configurationDistFilePath)) {
                createFile(configurationSample.getConfiguration(), configurationDistFilePath);
            }

            log("Rename the file " + configurationSample.getFileName() + ".dist to " + configurationSample.getFileName()
                    + " and change the settings according to your project.");

        }
    }

    public Configuration getConfiguration() {
        return configuration;
    }

    private void loadConfiguration(Path configurationFilePath) {

        log("Loading configuration file: " + configurationFilePath.toString());

        Map<Key, Value> configurations = read(configurationFilePath);

        if (configurations != null) {

            System.out.println(configurations.size() + " configurations loaded.");

            if (configuration != null) {
                configurations.forEach( (key, value) -> {
                    Item item = configuration.get(key);
                    if (item != null) {
                        List<Field> fields = List.of(item.fields());
                        fields.forEach(field -> {
                            if (field instanceof Value) {
                                ((Value) field).setValue(value.getValue());
                            }
                        });
                    }
                });
            }
        }

    }

    private void createFile(Configuration configuration, Path configurationFilePath) {

        log("Creating default configuration file: " + configurationFilePath.toFile().getName());

        String line = "#".repeat(100);

        write(line, configurationFilePath);

        for (String s : List.of(configuration.getDescription().getValues())) {
            String text = "# " + s;
            text = text + " ".repeat(100 - text.length() - 2) + " #";
            write(text, configurationFilePath);
        }

        write(line, configurationFilePath);
        write("[" + configuration.getServiceName() + "]", configurationFilePath);
        write("", configurationFilePath);
        write(line, configurationFilePath);
        write("# SECTION INDEX", configurationFilePath);
        write("#", configurationFilePath);

        configuration.getSections().forEach(section -> {
            for (String s : List.of(section.getDescription().getValues())) {
                write("#    " + s, configurationFilePath);
            }
        });

        write("#", configurationFilePath);
        write(line, configurationFilePath);

        configuration.getSections().forEach(section -> {

            write("", configurationFilePath);
            write(line, configurationFilePath);
            for (String s : List.of(section.getDescription().getValues())) {
                write("# " + s, configurationFilePath);
            }

            section.getGroups().forEach(group -> {

                List<String> values = new ArrayList<>();

                write("#", configurationFilePath);

                group.getItems().forEach(item -> {
                    AtomicReference<Key> key = new AtomicReference<>(null);
                    AtomicReference<Value> value = new AtomicReference<>(null);
                    AtomicReference<Description> description = new AtomicReference<>(null);
                    AtomicReference<DefaultValue> defaultValue = new AtomicReference<>(null);
                    AtomicReference<Example> example = new AtomicReference<>(null);
                    AtomicReference<Format> format = new AtomicReference<>(null);
                    AtomicReference<Detail> detail = new AtomicReference<>(null);
                    AtomicReference<DeveloperNote> developerNote = new AtomicReference<>(null);
                    AtomicReference<Observations> observations = new AtomicReference<>(null);
                    AtomicReference<ImportantNote> importantNote = new AtomicReference<>(null);

                    List.of(item.fields()).forEach(field -> {
                        if (field instanceof Key) key.set((Key) field);
                        if (field instanceof Value) value.set((Value) field);
                        if (field instanceof Description) description.set((Description) field);
                        if (field instanceof DefaultValue) defaultValue.set((DefaultValue) field);
                        if (field instanceof Example) example.set((Example) field);
                        if (field instanceof Format) format.set((Format) field);
                        if (field instanceof Detail) detail.set((Detail) field);
                        if (field instanceof DeveloperNote) developerNote.set((DeveloperNote) field);
                        if (field instanceof Observations) observations.set((Observations) field);
                        if (field instanceof ImportantNote) importantNote.set((ImportantNote) field);
                    });

                    if((key.get() != null) && (value.get() != null)) {

                        boolean itemDescriptionsFieldsNUll =
                                description.get() == null
                                        && defaultValue.get() == null
                                        && example.get() == null
                                        && format.get() == null
                                        && detail.get() == null
                                        && developerNote.get() == null
                                        && observations.get() == null
                                        && importantNote.get() == null;


                        write("#   " + key.get().getValue(), configurationFilePath);

                        printDescriptions(description.get(), configurationFilePath);

                        printFormats(format.get(), configurationFilePath);

                        printImportantNotes(importantNote.get(), configurationFilePath);

                        printDetails(detail.get(), configurationFilePath);

                        printExamples(example.get(), configurationFilePath);

                        printDefaultValues(defaultValue.get(), configurationFilePath);

                        printObservations(observations.get(), configurationFilePath);

                        printDeveloperNotes(developerNote.get(), configurationFilePath);

                        String keyValue = (key.get().getValue() + " = " + value.get().getValue());
                        if (group.getItems().size() > 1){
                            values.add(keyValue);
                        } else {
                            write("", configurationFilePath);
                            write(keyValue, configurationFilePath);
                        }

                        if (!itemDescriptionsFieldsNUll && group.getItems().size() > 1) {
                            write("#", configurationFilePath);
                        }
                    }

                });

                // Write grouped values
                AtomicReference<Description> description = new AtomicReference<>(null);
                AtomicReference<DefaultValue> defaultValue = new AtomicReference<>(null);
                AtomicReference<Example> example = new AtomicReference<>(null);
                AtomicReference<Format> format = new AtomicReference<>(null);
                AtomicReference<Detail> detail = new AtomicReference<>(null);
                AtomicReference<DeveloperNote> developerNote = new AtomicReference<>(null);
                AtomicReference<Observations> observations = new AtomicReference<>(null);
                AtomicReference<ImportantNote> importantNote = new AtomicReference<>(null);

                group.getFields().forEach(field -> {
                    if (field instanceof Description) description.set((Description) field);
                    if (field instanceof DefaultValue) defaultValue.set((DefaultValue) field);
                    if (field instanceof Example) example.set((Example) field);
                    if (field instanceof Format) format.set((Format) field);
                    if (field instanceof Detail) detail.set((Detail) field);
                    if (field instanceof DeveloperNote) developerNote.set((DeveloperNote) field);
                    if (field instanceof Observations) observations.set((Observations) field);
                    if (field instanceof ImportantNote) importantNote.set((ImportantNote) field);
                });

                printDescriptions(description.get(), configurationFilePath);

                printFormats(format.get(), configurationFilePath);

                printImportantNotes(importantNote.get(), configurationFilePath);

                printDetails(detail.get(), configurationFilePath);

                printExamples(example.get(), configurationFilePath);

                printDefaultValues(defaultValue.get(), configurationFilePath);

                printObservations(observations.get(), configurationFilePath);

                printDeveloperNotes(developerNote.get(), configurationFilePath);

                if (!values.isEmpty()){
                    write("", configurationFilePath);
                    values.forEach(s -> {
                        write(s, configurationFilePath);
                    });
                }

                write("", configurationFilePath);

            });

            write("#", configurationFilePath);
            write(line, configurationFilePath);

        });

    }

    private void printDescriptions(Description description, Path configurationFilePath){
        if (description != null) {
            List<String> descriptionLines =  List.of(description.getValues());
            for(String s : descriptionLines) {
                if (descriptionLines.getFirst().equals(s)) {
                    write("#        Description: " + s, configurationFilePath);
                } else {
                    write("#                     " + s, configurationFilePath);
                }
            }
        }
    }

    private void printFormats(Format format, Path configurationFilePath){
        if (format != null) {
            List<String> formatLines = List.of(format.getValues());
            for(String s : formatLines) {
                if (formatLines.getFirst().equals(s)) {
                    write("#        Format:      " + s, configurationFilePath);
                } else {
                    write("#                     " + s, configurationFilePath);
                }
            }
        }
    }

    private void printImportantNotes(ImportantNote importantNote, Path configurationFilePath){
        if (importantNote != null){
            List<String> importantNoteLines = List.of(importantNote.getValues());
            for(String s : importantNoteLines) {
                if (importantNoteLines.getFirst().equals(s)) {
                    write("#        Important:   " + s, configurationFilePath);
                } else {
                    write("#                     " + s, configurationFilePath);
                }
            }
        }
    }

    private void printDetails(Detail detail, Path configurationFilePath){
        if (detail != null){
            List<String> detailLines = List.of(detail.getValues());
            for (String s : detailLines){
                if (detailLines.getFirst().equals(s)){
                    write("#        Details:     " + s, configurationFilePath);
                } else {
                    write("#                     " + s, configurationFilePath);
                }
            }
        }
    }

    private void printExamples(Example example, Path configurationFilePath){
        if (example != null) {
            List<String> exampleLines = List.of(example.getValues());
            for (String s : exampleLines){
                if (exampleLines.getFirst().equals(s)){
                    write("#        Example:     " + s, configurationFilePath);
                } else {
                    write("#                     " + s, configurationFilePath);
                }
            }
        }
    }

    private void printDefaultValues(DefaultValue defaultValue, Path configurationFilePath){
        if (defaultValue != null) {
            List<String> defaultValueLines = List.of(defaultValue.getValues());
            for (String s : defaultValueLines){
                if (defaultValueLines.getFirst().equals(s)){
                    write("#        Default:     " + s, configurationFilePath);
                } else {
                    write("#                     " + s, configurationFilePath);
                }
            }
        }
    }

    private void printObservations(Observations observations, Path configurationFilePath){
        if (observations != null){
            write("#", configurationFilePath);
            List<String> observationLines = List.of(observations.getValues());
            for (String s : observationLines) {
                if (observationLines.getFirst().equals(s)){
                    write("#        " + s, configurationFilePath);
                } else {
                    write("#           " + s, configurationFilePath);
                }
            }
        }
    }

    private void printDeveloperNotes(DeveloperNote developerNote, Path configurationFilePath){
        if (developerNote != null) {
            write("#", configurationFilePath);
            List<String> developerNoteLines = List.of(developerNote.getValues());
            write("# Note to developers:", configurationFilePath);
            for (String s : developerNoteLines) {
                write("#   " + s, configurationFilePath);
            }
        }
    }

}
