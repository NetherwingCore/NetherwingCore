package br.net.dd.netherwingcore.common.configuration;

import br.net.dd.netherwingcore.common.configuration.fields.*;
import br.net.dd.netherwingcore.common.configuration.structs.Item;
import br.net.dd.netherwingcore.common.logging.Log;
import br.net.dd.netherwingcore.common.utilities.Util;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static br.net.dd.netherwingcore.common.serialization.FileManager.*;

/**
 * The `ConfigurationController` class is a central handler for managing
 * configuration files in the application. It oversees the creation, loading,
 * and modification of configurations defined in a {@link ConfigurationSample}.
 *
 * The controller ensures that the necessary configuration files exist, and if
 * not, it creates a default one based on the provided {@link ConfigurationSample}.
 * It also allows for the configuration settings to be later retrieved and updated
 * as needed.
 *
 * <p>
 * Features of this class:
 * - Reads configuration files and loads key-value pairs for the application.
 * - Automatically creates default configuration files when they do not exist.
 * - Supports validation and formatting of configuration entries.
 * - Provides utilities to print various meta-information about the configuration.
 * <p>
 * It internally utilizes helper classes such as {@link Item}, {@link Field},
 * and utility methods from the {@link Util}, and serialization functionalities
 * from the `FileManager`.
 */
public class ConfigurationController {

    private static final Log logger = Log.getLogger(ConfigurationController.class.getSimpleName());

    /**
     * Holds the configuration object created from the provided {@link ConfigurationSample}.
     */
    private final Configuration configuration;

    /**
     * Initializes the `ConfigurationController` by validating the existence of a configuration file.
     * If a configuration file does not exist, it creates one using the data from the given {@link ConfigurationSample}.
     *
     * @param configurationSample An instance containing the default sample configuration
     *                            and metadata for handling the file.
     */
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

            logger.log("The configuration file was not found.");

            if (!Files.exists(configurationDistFilePath)) {
                createFile(configurationSample.getConfiguration(), configurationDistFilePath);
            }

            logger.log("Rename the file " + configurationSample.getFileName() + ".dist to " + configurationSample.getFileName()
                    + " and change the settings according to your project.");

        }
    }

    /**
     * Retrieves the current `Configuration` stored in this controller.
     *
     * @return The {@link Configuration} instance being handled by this controller.
     */
    public Configuration getConfiguration() {
        return configuration;
    }

    /**
     * Loads a configuration file from a specified path and parses its key-value pairs.
     * The parsed values are then applied to the fields of corresponding configuration items.
     *
     * @param configurationFilePath The {@link Path} to the configuration file to be loaded.
     */
    private void loadConfiguration(Path configurationFilePath) {

        logger.log("Loading configuration file: " + configurationFilePath.toString());

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

    /**
     * Creates a default configuration file at the specified path.
     * The file is built using the fields defined in the given {@link Configuration} object.
     *
     * @param configuration        The {@link Configuration} that contains the default settings.
     * @param configurationFilePath The {@link Path} to create the new configuration file.
     */
    private void createFile(Configuration configuration, Path configurationFilePath) {

        logger.log("Creating default configuration file: " + configurationFilePath.toFile().getName());

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

    /**
     * Prints the description lines for a configuration field, if applicable, to the given file path.
     *
     * @param description           The {@link Description} to be printed, if not null.
     * @param configurationFilePath The {@link Path} file to write the description to.
     */
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

    /**
     * Prints the format lines for a configuration field, if applicable, to the given file path.
     *
     * @param format                The {@link Format} to be printed, if not null.
     * @param configurationFilePath The {@link Path} file to write the format to.
     */
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

    /**
     * Prints important notes for a configuration field, if applicable, to the given file path.
     *
     * @param importantNote         The {@link ImportantNote} to be printed, if not null.
     * @param configurationFilePath The {@link Path} file to write the important notes to.
     */
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

    /**
     * Prints details for a configuration field, if applicable, to the given file path.
     *
     * @param detail                The {@link Detail} to be printed, if not null.
     * @param configurationFilePath The {@link Path} file to write the details to.
     */
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

    /**
     * Prints example lines for a configuration field, if applicable, to the given file path.
     *
     * @param example               The {@link Example} to be printed, if not null.
     * @param configurationFilePath The {@link Path} file to write the examples to.
     */
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

    /**
     * Prints default values for a configuration field, if applicable, to the given file path.
     *
     * @param defaultValue          The {@link DefaultValue} to be printed, if not null.
     * @param configurationFilePath The {@link Path} file to write the default values to.
     */
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

    /**
     * Prints observation lines for a configuration field, if applicable, to the given file path.
     *
     * @param observations          The {@link Observations} to be printed, if not null.
     * @param configurationFilePath The {@link Path} file to write the observations to.
     */
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

    /**
     * Prints developer notes for a configuration field, if applicable, to the given file path.
     *
     * @param developerNote         The {@link DeveloperNote} to be printed, if not null.
     * @param configurationFilePath The {@link Path} file to write the developer notes to.
     */
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
