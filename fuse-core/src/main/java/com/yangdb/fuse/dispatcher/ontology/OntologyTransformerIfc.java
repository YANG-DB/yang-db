package com.yangdb.fuse.dispatcher.ontology;

public interface OntologyTransformerIfc<OntIn,OntOut> {
    OntOut transform(OntIn source);
}
