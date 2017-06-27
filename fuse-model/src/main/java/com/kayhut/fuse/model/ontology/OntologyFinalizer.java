package com.kayhut.fuse.model.ontology;

import javaslang.collection.Stream;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Created by moti on 5/14/2017.
 */
public class OntologyFinalizer {

    public static final String TYPE_FIELD_P_TYPE = String.valueOf(Integer.MAX_VALUE - 1);
    public static final String ID_FIELD_P_TYPE = String.valueOf(Integer.MAX_VALUE);

    public static final String ENTITY_B_ID = "id";
    public static final String ENTITY_B_TYPE = "type";

    public static Ontology finalize(Ontology ontology) {
        List<Property> properties = new ArrayList<>(ontology.getProperties());
        Optional<Property> idField = Stream.ofAll(properties).filter(prop -> prop.getpType().equals(ID_FIELD_P_TYPE)).toJavaOptional();
        if (!idField.isPresent()) {
            properties.add(Property.Builder.get().withName(ENTITY_B_ID).withPType(ID_FIELD_P_TYPE).withType("string").build());
        }

        Optional<Property> typeField = Stream.ofAll(properties).filter(prop -> prop.getpType().equals(TYPE_FIELD_P_TYPE)).toJavaOptional();
        if (!typeField.isPresent()) {
            properties.add(Property.Builder.get().withName(ENTITY_B_TYPE).withPType(TYPE_FIELD_P_TYPE).withType("string").build());
        }
        ontology.setProperties(properties);
        return ontology;
    }
}
