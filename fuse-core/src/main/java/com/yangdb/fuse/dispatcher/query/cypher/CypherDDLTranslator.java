package com.yangdb.fuse.dispatcher.query.cypher;

import com.yangdb.fuse.dispatcher.ontology.OntologyTransformerIfc;
import com.yangdb.fuse.model.ontology.Ontology;

/**
 * translate open cypher DDL - Schema definition language into ontology
 *  ------------------------------------------------------------------------
 *  <b> see https://github.com/opencypher/morpheus/tree/master/graph-ddl/src </b>
 *
 *  Example:
*      CREATE GRAPH TYPE fooSchema (
 *         Person ( name STRING, age INTEGER ),
 *         Book   ( title STRING ) ,
 *         READS  ( rating FLOAT ) ,
 *         (Person),
 *         (Book),
 *         (Person)-[READS]->(Book)
 *     )
 *
 */
public class CypherDDLTranslator implements OntologyTransformerIfc<String, Ontology> {
    @Override
    public Ontology transform(String ontologyName, String source) {
        //todo - implement this transformation
        return Ontology.OntologyBuilder.anOntology().build();
    }

    @Override
    public String translate(Ontology source) {
        //todo - implement this transformation
        return source.toString();
    }
}
