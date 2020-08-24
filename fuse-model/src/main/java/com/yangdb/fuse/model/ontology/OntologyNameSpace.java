package com.yangdb.fuse.model.ontology;

import org.semanticweb.owlapi.model.IRI;

import java.util.Arrays;

public interface OntologyNameSpace {

    String[] namespaces = new String[] {
            "http://xmlns.com/",
            "http://www.w3.org/",
            "http://yangdb.org/"
    };

    String defaultNameSpace = namespaces[2];

    static boolean inside(String name) {
        return Arrays.stream(namespaces).anyMatch(name::contains);
    }

    static String reminder(String name) {
        return IRI.create(name).getRemainder().or(name);
    }
}
