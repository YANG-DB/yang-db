package com.yangdb.fuse.dispatcher.query.sql;

import com.google.inject.Inject;
import com.yangdb.fuse.dispatcher.ontology.OntologyTransformerIfc;
import com.yangdb.fuse.model.ontology.Ontology;

import java.util.Collections;
import java.util.List;
import static org.jooq.impl.DSL.*;
import org.jooq.*;
import org.jooq.impl.*;
/**
 * convert DDL (SQL Definition Language) structure into V1 ontology
 */
public class DDL2OntologyTransformer implements OntologyTransformerIfc<List<String>, Ontology> {
    private DefaultDSLContext context;

    @Inject
    public DDL2OntologyTransformer() {}

    @Override
    public Ontology transform(List<String> source) {
        return Ontology.OntologyBuilder.anOntology().build();
    }

    @Override
    public List<String> translate(Ontology source) {
        return Collections.emptyList();
    }

    private void init() {
        context = new DefaultDSLContext(SQLDialect.DEFAULT);
    }
}
