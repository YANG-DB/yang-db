package com.kayhut.fuse.model.ontology;

import javaslang.collection.Stream;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Created by moti on 5/14/2017.
 */
public class OntologyFinalizer {
    public static Ontology finalize(Ontology ontology){
        List<Property> properties = new ArrayList<>(ontology.getProperties());
        Optional<Property> idField = Stream.ofAll(properties).filter(prop -> prop.getpType() == Integer.MAX_VALUE).toJavaOptional();
        if(!idField.isPresent()){
            properties.add(Property.Builder.get().withName("id").withPType(Integer.MAX_VALUE).withType("string").build());
        }

        Optional<Property> typeField = Stream.ofAll(properties).filter(prop -> prop.getpType() == Integer.MAX_VALUE-1).toJavaOptional();
        if(!typeField.isPresent()){
            properties.add(Property.Builder.get().withName("type").withPType(Integer.MAX_VALUE - 1).withType("string").build());
        }
        ontology.setProperties(properties);
        return ontology;
    }
}
