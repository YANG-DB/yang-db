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

        //todo - improve the functionality of creating a relation by thinking of the following:
        // a) Number of F.K
        // b) Name of table with relation to other tables
        // c) Number of P.K
        if (!foreignKey(statement.getConstraints()).isEmpty()) {
            //build relations
            RelationshipType.Builder relBuilder = RelationshipType.Builder.get()
                    .withName(table.getName().toLowerCase())
                    .withRType(table.getName().toLowerCase())
                    .withDirectional(true);

            foreignKey(statement.getConstraints())
                    .forEach(fk -> relBuilder.withEPairs(singletonList(
                            new EPair(fk.getName().toLowerCase(),
                                    table.getName().toLowerCase(),
                                    idField,
                                    fk.get$referencesTable().getName().toLowerCase(),
                                    fk.getForeignKey()[0].getName().toLowerCase())))
                    );
            builder.addRelationshipType(relBuilder.build());
        }

        EntityType build = entityTypeBuilder.build();
        //compile entity
        builder.addEntityType(build);
    }


}
