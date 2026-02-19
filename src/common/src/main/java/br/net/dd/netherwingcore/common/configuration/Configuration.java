package br.net.dd.netherwingcore.common.configuration;

import br.net.dd.netherwingcore.common.configuration.fields.Description;
import br.net.dd.netherwingcore.common.configuration.fields.Key;
import br.net.dd.netherwingcore.common.configuration.structs.Item;
import br.net.dd.netherwingcore.common.configuration.structs.Section;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static br.net.dd.netherwingcore.common.configuration.fields.Type.*;

/**
 * The Configuration class represents a structure for managing configuration data.
 * It organizes the data into sections and items, supporting the retrieval of specific
 * configuration values by keys.
 * <p>
 * This class is designed to encapsulate a configuration's metadata such as a description
 * and a service name, while also storing the configuration's hierarchical structure in
 * sections and groups.
 */
public class Configuration {

    /**
     * A brief description of the configuration.
     */
    private final Description description;

    /**
     * The name of the service associated with this configuration.
     */
    private final String serviceName;

    /**
     * A list of sections that organize the configuration data.
     */
    private final List<Section> sections;

    /**
     * Constructs an empty Configuration object with default values:
     * - an empty description
     * - an empty service name
     * - an empty list of sections.
     */
    public Configuration() {
        this.description = new Description("");
        this.serviceName = "";
        this.sections = new ArrayList<>();
    }

    /**
     * Constructs a Configuration object with the specified description and service name,
     * initializing the sections list as empty.
     *
     * @param description A Description object representing the configuration description.
     * @param serviceName A String representing the service name associated with the configuration.
     */
    public Configuration(Description description, String serviceName) {
        this.description = description;
        this.serviceName = serviceName;
        this.sections = new ArrayList<>();
    }

    /**
     * Adds a new section to the configuration.
     *
     * @param section A Section object to be added.
     * @return The Configuration object to allow method chaining.
     */
    public Configuration addSection(Section section) {
        this.sections.add(section);
        return this;
    }

    /**
     * Checks if the given item contains a key that matches the specified key.
     *
     * @param item The Item to be checked for a matching key.
     * @param key  The Key to be matched against the item's fields.
     * @return true if a matching key is found in the item's fields; false otherwise.
     */
    private boolean matches(Item item, Key key) {
        return Arrays.stream(
                item.fields()
        ).anyMatch(
                field -> field instanceof Key && field.getValue().equals(key.getValue())
        );
    }

    /**
     * Retrieves an Item from the configuration that matches the specified key.
     *
     * @param key The Key to be matched against the items in the configuration.
     * @return The first Item that matches the key, or null if no matching item is found.
     */
    public Item get(Key key) {
        return sections.stream()
                .flatMap(section -> section.getGroups().stream())
                .flatMap(group -> group.getItems().stream())
                .filter(item -> matches(item, key))
                .findFirst() .orElse(null);
    }

    /**
     * Retrieves an integer value from the configuration based on the given key.
     * Returns a default value if the item is not found or if the type does not match.
     *
     * @param key          A String representing the key of the desired value.
     * @param defaultValue The default integer value to return if the key is not found.
     * @return The integer value associated with the key, or the default value if not found.
     */
    public Integer get(String key, Integer defaultValue) {

        Item item = get(new Key(key));

        if (item == null) {
            return defaultValue;
        }

        if (item.getValue() == null) {
            return defaultValue;
        }

        if (item.getValue().getType().equals(NUMBER)) {
            return Integer.parseInt(item.getValue().getValue());
        }

        return defaultValue;
    }

    /**
     * Retrieves a string value from the configuration based on the given key.
     * Returns a default value if the item is not found or if the type does not match.
     *
     * @param key          A String representing the key of the desired value.
     * @param defaultValue The default string value to return if the key is not found.
     * @return The string value associated with the key, or the default value if not found.
     */
    public String get(String key, String defaultValue) {

        Item item = get(new Key(key));

        if (item == null) {
            return defaultValue;
        }

        if (item.getValue() == null) {
            return defaultValue;
        }

        if (item.getValue().getType().equals(TEXT)) {
            return item.getValue().getValue();
        }

        return defaultValue;
    }

    /**
     * Gets the description of the configuration.
     *
     * @return A Description object containing the configuration description.
     */
    public Description getDescription() {
        return description;
    }

    /**
     * Gets the name of the service associated with the configuration.
     *
     * @return A String representing the service name.
     */
    public String getServiceName() {
        return serviceName;
    }

    /**
     * Gets the list of sections in the configuration.
     *
     * @return A list of Section objects representing the structure of the configuration.
     */
    public List<Section> getSections() {
        return sections;
    }

}
