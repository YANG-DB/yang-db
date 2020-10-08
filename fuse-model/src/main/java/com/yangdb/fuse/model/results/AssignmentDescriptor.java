package com.yangdb.fuse.model.results;

import com.yangdb.fuse.model.descriptors.Descriptor;

import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.StringJoiner;

public class AssignmentDescriptor implements Descriptor<Assignment<Entity, Relationship>> {
    @Override
    public String describe(Assignment<Entity, Relationship> item) {
        return patternValue(item);
    }

    //region Private Methods
    private String patternValue(Assignment<Entity, Relationship> assignment) {
        StringJoiner joiner = new StringJoiner("-", "", "");

        assignment.getRelationships().forEach(rel -> {
            joiner.add(print(getEntityById(rel.geteID1(), assignment).orElseGet(() ->
                    new Entity(Collections.emptySet(), rel.geteID1(),"???", Collections.emptyMap() ))));
            joiner.add(print(rel));
            joiner.add(print(getEntityById(rel.geteID2(), assignment).orElseGet(() ->
                    new Entity(Collections.emptySet(), rel.geteID1(),"???", Collections.emptyMap() ))));
        });
        return (joiner.toString().toCharArray()[joiner.length() - 1] == '-') ?
                joiner.toString().substring(0, joiner.length() - 1) : joiner.toString();

    }

    private Optional<Entity> getEntityById(String id, Assignment<Entity, Relationship> assignment) {
        return assignment.getEntities().stream().filter(e -> e.geteID().equals(id)).findFirst();
    }

    public static String print(Assignment<Entity, Relationship> item) {
        return new AssignmentDescriptor().patternValue(item);
    }

    private static String print(Entity entity) {
        return String.format("[%s,%s, %s]", entity.geteID(), strip(String.join(",", entity.geteTag())), entity.geteType());
    }

    private static String print(Relationship relationship) {
        return String.format("(%s, %s)", relationship.getrID(), relationship.getrType());
    }

    private static String strip(String value) {
        return value.replace('[', ' ').replace(']', ' ');
    }
}

