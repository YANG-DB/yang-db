package com.kayhut.fuse.dispatcher.ontology;

import com.kayhut.fuse.model.ontology.Ontology;

import java.util.Optional;

/**
 * Created by liorp on 3/16/2017.
 */
public interface OntologyProvider {
    Optional<Ontology> get(String id);
}
