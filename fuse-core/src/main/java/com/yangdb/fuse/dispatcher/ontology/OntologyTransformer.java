package com.yangdb.fuse.dispatcher.ontology;

public interface OntologyTransformer<OntIn,OntOut> {
    OntOut transform(OntIn source);
}
