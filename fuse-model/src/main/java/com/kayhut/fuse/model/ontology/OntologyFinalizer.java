package com.kayhut.fuse.model.ontology;

import javaslang.collection.Stream;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * Created by moti on 5/14/2017.
 */
public class OntologyFinalizer {

    public static final String ID_FIELD_PTYPE = "id";
    public static final String TYPE_FIELD_PTYPE = "type";

    public static final String ID_FIELD_NAME = "id";
    public static final String TYPE_FIELD_NAME = "type";

    public static Ontology finalize(Ontology ontology) {
        ontology.setProperties(Stream.ofAll(ontology.getProperties())
                .append(Property.Builder.get().withName(ID_FIELD_NAME).withPType(ID_FIELD_PTYPE).withType("string").build())
                .append(Property.Builder.get().withName(TYPE_FIELD_NAME).withPType(TYPE_FIELD_PTYPE).withType("string").build())
                .distinct()
                .toJavaList());

        Stream.ofAll(ontology.getEntityTypes())
                .forEach(entityType -> entityType.setProperties(
                        Stream.ofAll(entityType.getProperties())
                                .appendAll(Arrays.asList(ID_FIELD_PTYPE, TYPE_FIELD_PTYPE))
                                .distinct()
                                .toJavaList()
                ));

        return ontology;
    }
}
