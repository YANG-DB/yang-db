package com.kayhut.fuse.model.transport;

import com.kayhut.fuse.model.ontology.Ontology;

/**
 * Created by lior on 23/02/2017.
 */
public class OntologyResponse implements Response {
    private String id;
    private Ontology ontology;

    public OntologyResponse() {}

    public OntologyResponse(String id, Ontology ontology) {
        this.id = id;
        this.ontology = ontology;
    }

    @Override
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Ontology getOntology() {
        return ontology;
    }

    public void setOntology(Ontology ontology) {
        this.ontology = ontology;
    }
}
