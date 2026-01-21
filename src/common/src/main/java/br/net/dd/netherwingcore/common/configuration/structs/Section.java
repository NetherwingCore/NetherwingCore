package br.net.dd.netherwingcore.common.configuration.structs;

import br.net.dd.netherwingcore.common.configuration.fields.Description;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a configuration section that holds a description and a collection of groups.
 * This class provides a structure for grouping configuration-related data together
 * and organizing it hierarchically.
 */
public class Section {

    /**
     * The description of the section.
     */
    private final Description description;

    /**
     * The list of groups associated with the section.
     */
    private final List<Group> groups;

    /**
     * Constructs a new Section with the specified description.
     *
     * @param description The description of the section.
     */
    public Section(Description description) {
        this.description = description;
        this.groups = new ArrayList<>();
    }

    /**
     * Adds a new group to this section.
     *
     * @param group The group to add.
     * @return The current Section instance, for method chaining.
     */
    public Section addGroup(Group group) {
        this.groups.add(group);
        return this;
    }

    /**
     * Gets the description of this section.
     *
     * @return The description of the section.
     */
    public Description getDescription() {
        return description;
    }

    /**
     * Gets the list of groups associated with this section.
     *
     * @return The list of groups.
     */
    public List<Group> getGroups() {
        return groups;
    }

}
