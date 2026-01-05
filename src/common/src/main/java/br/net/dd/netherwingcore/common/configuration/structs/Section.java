package br.net.dd.netherwingcore.common.configuration.structs;

import br.net.dd.netherwingcore.common.configuration.fields.Description;

import java.util.ArrayList;
import java.util.List;

public class Section {

    private final Description description;
    private final List<Group> groups;

    public Section(Description description) {
        this.description = description;
        this.groups = new ArrayList<>();
    }

    public Section addGroup(Group group) {
        this.groups.add(group);
        return this;
    }

    public Description getDescription() {
        return description;
    }

    public List<Group> getGroups() {
        return groups;
    }

}
