package com.yangdb.fuse.pgql;

import com.yangdb.fuse.dispatcher.ontology.OntologyTransformerIfc;
import com.yangdb.fuse.model.ontology.Ontology;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

public class PgqlOntologyParser implements OntologyTransformerIfc<String, Ontology> {

    @Override
    public Ontology transform(String ontologyName, String source) {
        return Ontology.OntologyBuilder.anOntology().build();
    }

    @Override
    public String translate(Ontology source) {
        //Todo
        throw new NotImplementedException();
    }

}
