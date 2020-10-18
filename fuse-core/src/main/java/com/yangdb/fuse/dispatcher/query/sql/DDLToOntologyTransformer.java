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
import com.yangdb.fuse.model.ontology.Ontology.OntologyPrimitiveType;
import com.yangdb.fuse.model.resourceInfo.FuseError;
import org.jooq.*;
import org.jooq.impl.CreateTableStatement;
import org.jooq.impl.DefaultConfiguration;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Collections.singletonList;
import static org.jooq.impl.ConstraintStatement.foreignKey;
import static org.jooq.impl.ConstraintStatement.primaryKey;
import static org.jooq.impl.DSL.using;

/**
 * convert DDL (SQL Definition Language) structure into V1 ontology
 */
public class DDLToOntologyTransformer implements OntologyTransformerIfc<List<String>, Ontology> {
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
        if (isEntity(table.getName())) {
            createEntity(builder, statement, table);
        } else if(isRelation(table.getName())) {
            createRelation(builder,statement,table);
        }

    }

    private void createRelation(Ontology.OntologyBuilder ontBuilder, CreateTableStatement statement, Table<?> table) {
        if (!foreignKey(statement.getConstraints()).isEmpty()) {
            //build relations
            RelationshipType.Builder builder = RelationshipType.Builder.get()
                    .withName(table.getName().toLowerCase())
                    .withRType(table.getName().toLowerCase())
                    .withDirectional(true);

            //build PK fields constraints
            List<String> mandatory = primaryKey(statement.getConstraints()).stream()
                    .flatMap(pk -> Arrays.stream(pk.getPrimaryKey()).distinct())
                    .map(f->f.getName().toLowerCase()).collect(Collectors.toList());
            //set mandatory fields
            builder.withMandatory(mandatory);
            //set id field name
            builder.withIdField(mandatory.toArray(new String[0]));

            //build ontology properties (if none exist)
            List<Field<?>> fields = statement.getFields();

            //build entity fields
            fields.forEach(field -> builder.withProperty(field.getName().toLowerCase()));

            //add fields as general properties in ontology
            fields.forEach(f -> {
                String name = f.getName().toLowerCase();
                if (!ontBuilder.getProperty(name).isPresent()) {
                    //add field to properties
                    ontBuilder.addProperty(new Property(name, name, OntologyPrimitiveType.translate(f.getType().getName()).name().toLowerCase()));
                }
            });
            //generate EPairs
            foreignKey(statement.getConstraints())
                    .forEach(fk -> builder.withEPairs(singletonList(
                            new EPair(fk.getName().toLowerCase(),
                                    table.getName().toLowerCase(),
                                    BaseElement.idFieldName(mandatory),
                                    fk.get$referencesTable().getName().toLowerCase(),
                                    fk.getForeignKey()[0].getName().toLowerCase())))
                    );
            //add rel to ontology
            ontBuilder.addRelationshipType(builder.build());
        }
    }

    private void createEntity(Ontology.OntologyBuilder ontBuilder, CreateTableStatement statement, Table<?> table) {
            //build ontology entity
            EntityType.Builder builder = EntityType.Builder.get()
                    .withName(table.getName().toLowerCase())
                    .withEType(table.getName().toLowerCase());

            //build PK fields constraints
        List<String> mandatory = primaryKey(statement.getConstraints()).stream()
                .flatMap(pk -> Arrays.stream(pk.getPrimaryKey()).distinct())
                .map(f->f.getName().toLowerCase()).collect(Collectors.toList());
            //set mandatory fields
            builder.withMandatory(mandatory);
            //set id field name
            builder.withIdField(mandatory.toArray(new String[0]));

            //build ontology properties (if none exist)
            List<Field<?>> fields = statement.getFields();

            //build entity fields
            fields.forEach(field -> builder.withProperty(field.getName().toLowerCase()));
            //add fields as general properties in ontology
            fields.forEach(f -> {
                String name = f.getName().toLowerCase();
                if (!ontBuilder.getProperty(name).isPresent()) {
                    //add field to properties
                    ontBuilder.addProperty(new Property(name, name, OntologyPrimitiveType.translate(f.getType().getName()).name().toLowerCase()));
                }
            });

            EntityType build = builder.build();
            //compile entity
            ontBuilder.addEntityType(build);
    }

    /**
     * see https://www.baeldung.com/string-contains-multiple-words
     * @param name
     * @return
     */
    private boolean isRelation(String name) {
        //todo - improve the functionality of creating a relation by thinking of the following:
        // a) Number of F.K
        // b) Name of table with relation to other tables
        // c) Number of P.K
        return Stream.of("AlertsToBehaviors","behavior_to_behavior","BehaviorEntities","BehaviorEvents","TraceEntities","TraceEvents","TracesToBehaviors").map(String::toLowerCase).anyMatch(v->v.equals(name.toLowerCase()));
    }

    private boolean isEntity(String name) {
        return !isRelation(name);
    }

}