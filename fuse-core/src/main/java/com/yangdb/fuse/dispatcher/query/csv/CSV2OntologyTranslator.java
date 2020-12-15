package com.yangdb.fuse.dispatcher.query.csv;

import com.yangdb.fuse.dispatcher.ontology.OntologyTransformerIfc;
import com.yangdb.fuse.model.ontology.Ontology;

/**
 * translates csv headers & data files into a single coherent ontology
 *
 * see https://github.com/olehmberg/winter/wiki/SchemaMatching
 *
 * load multiple csv files and fuse them to construct a ontological structure
 */
public class CSV2OntologyTranslator implements OntologyTransformerIfc<String[], Ontology> {

    @Override
    public Ontology transform(String ontologyName, String[] source) {
        //todo - implement this transformation
        return Ontology.OntologyBuilder.anOntology().build();
    }

    @Override
    public String[] translate(Ontology source) {
        //todo - implement this transformation
        return new String[] {source.toString()};
    }
}
