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
import com.yangdb.fuse.model.ontology.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.Collections.singletonList;
import static org.jooq.impl.ConstraintStatement.*;
import static org.jooq.impl.DSL.*;

import com.yangdb.fuse.model.ontology.Ontology.OntologyPrimitiveType;
import com.yangdb.fuse.model.resourceInfo.FuseError;
import org.jooq.*;
import org.jooq.impl.*;

/**
 * convert DDL (SQL Definition Language) structure into V1 ontology
 */
public class DDLToOntologyTransformer implements OntologyTransformerIfc<List<String>, Ontology> {
    private DefaultDSLContext context;
    private Parser parser;

    @Inject
    public DDLToOntologyTransformer() {
    }

    @Override
    public Ontology transform(String ontologyName, List<String> source) {
        Ontology.OntologyBuilder ontologyBuilder = Ontology.OntologyBuilder.anOntology();
        parser = using(new DefaultConfiguration()).parser();
        source.forEach(s -> parseTable(s, ontologyBuilder));
        ontologyBuilder.withOnt(ontologyName);
        return ontologyBuilder.build();
    }

    @Override
    public List<String> translate(Ontology source) {
        return Collections.emptyList();
    }

    private void parseTable(String table, Ontology.OntologyBuilder builder) throws FuseError.FuseErrorException {
        try {
            context = new DefaultDSLContext(SQLDialect.DEFAULT);
            Queries queries = parser.parse(table);
            Arrays.stream(queries.queries())
                    .filter(q -> q.getClass().getSimpleName().endsWith("CreateTableImpl"))
                    .forEach(q -> parse(q, builder));
        } catch (Throwable t) {
            throw new FuseError.FuseErrorException("Error Parsing DDL file " + table, t);
        }
    }

    private void parse(Query createTable, Ontology.OntologyBuilder builder) {
        CreateTableStatement statement = new CreateTableStatement(createTable);
        //get table entity
        Table<?> table = statement.getTable();

        //build ontology entity
        EntityType.Builder entityTypeBuilder = EntityType.Builder.get()
                .withName(table.getName().toLowerCase())
                .withEType(table.getName().toLowerCase());

        //build PK fields constraints
        List<String> mandatory = primaryKey(statement.getConstraints()).stream().map(pk -> pk.getPrimaryKey()[0].getName().toLowerCase()).collect(Collectors.toList());
        //set mandatory fields
        entityTypeBuilder.withMandatory(mandatory);
        //set id field name
        String idField = String.join("-", mandatory);
        entityTypeBuilder.withIdField(idField);

        //build ontology properties (if none exist)
        List<Field<?>> fields = statement.getFields();

        //build entity fields
        fields.forEach(field -> entityTypeBuilder.withProperty(field.getName().toLowerCase()));
        //add fields as general properties in ontology
        fields.forEach(f -> {
            String name = f.getName().toLowerCase();
            if (!builder.getProperty(name).isPresent()) {
                //add field to properties
                builder.addProperty(new Property(name, name, OntologyPrimitiveType.translate(f.getType().getName()).name().toLowerCase()));
            }
        });

        //build relations
        foreignKey(statement.getConstraints())
                .forEach(fk ->
                        builder.addRelationshipType(
                                RelationshipType.Builder.get()
                                        .withName(fk.getName().toLowerCase())
                                        .withRType(fk.getName().toLowerCase())
                                        .withDirectional(true)
                                        .withEPairs(singletonList(
                                                new EPair(table.getName().toLowerCase(),
                                                        idField,
                                                        fk.get$referencesTable().getName().toLowerCase(),
                                                        fk.getForeignKey()[0].getName().toLowerCase())))
                                        .build()));


        EntityType build = entityTypeBuilder.build();
        //compile entity
        builder.addEntityType(build);
    }


}
