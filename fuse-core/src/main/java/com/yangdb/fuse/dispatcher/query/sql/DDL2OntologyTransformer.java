package com.yangdb.fuse.dispatcher.query.sql;

/*-
 * #%L
 * fuse-core
 * %%
 * Copyright (C) 2016 - 2020 The YangDb Graph Database Project
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import com.google.inject.Inject;
import com.yangdb.fuse.dispatcher.ontology.OntologyTransformerIfc;
import com.yangdb.fuse.model.ontology.EntityType;
import com.yangdb.fuse.model.ontology.Ontology;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.jooq.impl.DSL.*;
import org.jooq.*;
import org.jooq.impl.*;
/**
 * convert DDL (SQL Definition Language) structure into V1 ontology
 */
public class DDL2OntologyTransformer implements OntologyTransformerIfc<List<String>, Ontology> {
    private DefaultDSLContext context;
    private Parser parser;

    @Inject
    public DDL2OntologyTransformer() {

    }

    @Override
    public Ontology transform(List<String> source) {
        Ontology.OntologyBuilder ontologyBuilder = Ontology.OntologyBuilder.anOntology();
        parser = using(new DefaultConfiguration()).parser();
        source.forEach(s->parseTable(s,ontologyBuilder));
        return ontologyBuilder.build();
    }

    @Override
    public List<String> translate(Ontology source) {
        return Collections.emptyList();
    }

    private void parseTable(String table,Ontology.OntologyBuilder builder) {
        context = new DefaultDSLContext(SQLDialect.DEFAULT);
        Queries queries = parser.parse(table);
        Arrays.stream(queries.queries())
                .filter(q->q.getClass().getSimpleName().endsWith("CreateTableImpl"))
                .forEach(q->parse(q,builder));
    }

    private void parse(Query createTable, Ontology.OntologyBuilder builder)  {
        CreateTableStatement statement = new CreateTableStatement(createTable);

        Table<?> table = statement.getTable();
        //build relations
        List<ConstraintStatement> constraints = statement.getConstraints();
        List<DataType<?>> dataTypes = statement.getDataTypes();
        //build properties (if none exist)
        List<Field<?>> fields = statement.getFields();
        //build entity
        EntityType entityType = new EntityType(table.getName(),table.getName(),
                fields.stream().map(Field::getName).collect(Collectors.toList()),
                Collections.emptyList());

        builder.addEntityType(entityType);
    }


}
