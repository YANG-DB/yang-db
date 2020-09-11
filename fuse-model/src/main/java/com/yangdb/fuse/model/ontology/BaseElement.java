package com.yangdb.fuse.model.ontology;

import java.util.List;

/**
 * common attributed element shared by any properties enabled element
 */
public interface BaseElement {
    List<String> getMetadata();
    List<String> getProperties();
}
