package com.kayhut.fuse.model.results;

import com.kayhut.fuse.model.query.properties.constraint.NamedParameter;

import java.util.*;
import java.util.stream.Collectors;

public abstract class AssignmentUtils {

    public static final String ID = "id";//id projected entityField = "eID"

    /**
     * find entity.field tag name in query result assignments and collect them to an set
     *
     * @param result
     * @param tag
     * @return
     */
    public static NamedParameter collectByTag(AssignmentsQueryResult result, String tag) {
        Set results = new LinkedHashSet<>();
        String[] split = tag.split("[.]");
        //eTag
        String entityTag = split[0];
        if ((split.length == 2) && !split[1].equals(ID)) {
            String entityField = split[1];
            result.getAssignments().forEach(
                    assignment -> results.addAll(assignment.getEntities().stream()
                            .filter(ent -> ent.geteTag().contains(entityTag))
                            .filter(ent -> ent.getProperty(entityField).isPresent())
                            .map(ent -> ent.getProperty(entityField))
                            .filter(Optional::isPresent)
                            .map(p -> p.get().getValue())
                            .collect(Collectors.toList())));
        } else {
            //default id projected entityField = "eID"
            result.getAssignments().forEach(
                    assignment -> results.addAll(assignment.getEntities().stream()
                            .filter(ent -> ent.geteTag().contains(entityTag))
                            .map(Entity::geteID)
                            .collect(Collectors.toList())));
        }
        return new NamedParameter(tag, results);
    }
}
