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
import com.typesafe.config.Config;
import com.yangdb.fuse.dispatcher.ontology.OntologyTransformerIfc;
import com.yangdb.fuse.model.ontology.*;
import com.yangdb.fuse.model.ontology.Ontology.OntologyPrimitiveType;
import com.yangdb.fuse.model.resourceInfo.FuseError;
import org.jooq.*;
import org.jooq.impl.ConstraintStatement;
import org.jooq.impl.CreateTableStatement;
import org.jooq.impl.DefaultConfiguration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.jooq.impl.ConstraintStatement.foreignKey;
import static org.jooq.impl.ConstraintStatement.primaryKey;
import static org.jooq.impl.DSL.using;

/**
 * convert DDL (SQL Definition Language) statement into logical ontology
 */
public class DDLToOntologyTransformer implements OntologyTransformerIfc<List<String>, Ontology> {
    public static final String RELATIONSHIPS = "ddl.schema.relationships";
    public static final String ENTITIES = "ddl.schema.entities";
    public static final String DICTIONARY = "ddl.schema.dictionary";

    private Parser parser;
    // predefined names for user defined schema elements
    private List<String> relationshipNames = new ArrayList<>();
    private List<String> entitiesNames = new ArrayList<>();
    private List<String> dictionaryNames = new ArrayList<>();


    @Inject
    public DDLToOntologyTransformer(Config config) {
        try {
            this.relationshipNames = config.getStringList(String.format("%s.%s", config.getString("assembly"), RELATIONSHIPS))
                    .stream().map(String::toLowerCase).collect(Collectors.toList());
        } catch (Throwable ignore) {}
        try {
            this.entitiesNames = config.getStringList(String.format("%s.%s", config.getString("assembly"), ENTITIES))
                    .stream().map(String::toLowerCase).collect(Collectors.toList());
        } catch (Throwable ignore) {}
        try {
            this.dictionaryNames = config.getStringList(String.format("%s.%s", config.getString("assembly"), DICTIONARY))
                    .stream().map(String::toLowerCase).collect(Collectors.toList());
        } catch (Throwable ignore) {}

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
        } else if (isRelation(table.getName())) {
            createRelation(builder, statement, table);
        }

    }

    /**
     * create relation + pair for direction using the PK pair, FK constraints and the naming convention of the table using
     *  https://github.com/robert-bor/aho-corasick
     * @param ontBuilder
     * @param statement
     * @param table
     */
    private void createRelation(Ontology.OntologyBuilder ontBuilder, CreateTableStatement statement, Table<?> table) {
        if (!foreignKey(statement.getConstraints()).isEmpty()) {
            //build relations
            RelationshipType.Builder builder = RelationshipType.Builder.get()
                    .withName(table.getName().toLowerCase())
                    .withRType(table.getName().toLowerCase())
                    .withDirectional(true);

            //build PK fields constraints
            List<String> mandatory = getPKNames(statement);
            //set mandatory fields
            builder.withMandatory(mandatory);
            //set id field name
            builder.withIdField(mandatory.toArray(new String[0]));

            //build ontology properties (if none exist)
            List<Field<?>> fields = statement.getFields();

            //build entity fields
            fields.forEach(field -> builder.withProperty(field.getName().toLowerCase()));

            //add fields as general properties in ontology
            populateFields(ontBuilder, fields);

            //populate dictionary fields (enum)
            populateEnum(ontBuilder, statement.getConstraints());

            //generate EPairs
            populatePairs(statement, builder);
            //add rel to ontology
            ontBuilder.addRelationshipType(builder.build());
        }
    }

    /**
     * create relation pair
     * @param statement
     * @param builder
     */
    private void populatePairs(CreateTableStatement statement,  RelationshipType.Builder builder) {
        //get primary key names
        List<String> pkNames = getPKNames(statement);
        //only take the FK constraints that are related to the relation's PK since they refer to the entity
        List<ConstraintStatement> foreignKey = foreignKey(statement.getConstraints());
        List<ConstraintStatement> references = foreignKey.stream()
                .filter(fk -> pkNames.contains(fk.getForeignKey()[0].getName().toLowerCase()))
                .collect(Collectors.toList());

        // build the epair from the two sides of the relation collected entities (FK)
        if(references.size() < 2 )
            //we can only manage binary relations and not trinary of such...
            return;

        //todo create the direction according to the table naming convention using
        // https://en.wikipedia.org/wiki/Aho%E2%80%93Corasick_algorithm see https://github.com/robert-bor/aho-corasick
        ConstraintStatement sideA = references.get(0);
        ConstraintStatement sideB = references.get(1);

        //buid the epair according to the pair of FK build on the existance of PK
        builder.withEPair(new EPair(sideA.getName().toLowerCase(),
                sideA.get$referencesTable().getName().toLowerCase(),
                sideA.getForeignKey()[0].getName().toLowerCase(),
                sideB.get$referencesTable().getName().toLowerCase(),
                sideB.getForeignKey()[0].getName().toLowerCase()));
    }

    private void populateEnum(Ontology.OntologyBuilder builder, List<ConstraintStatement> constraints) {
        foreignKey(constraints)
                .stream()
                //enum (dictionary) cant be a relationship table...
                .filter(fk -> dictionaryNames.contains(fk.get$referencesTable().getName().toLowerCase()))
                .forEach(fk ->
                        //get field from properties and change its type to dictionary
                        builder.getProperty(fk.getForeignKey()[0].getName().toLowerCase())
                                .orElseThrow(() -> new FuseError.FuseErrorException(new FuseError("No matching ontology property name found for  " + fk.getForeignKey()[0].getName().toLowerCase(), "No matching ontology property name found ... ")))
                                .setType(fk.get$referencesTable().getName().toLowerCase()));
    }

    private void populateFields(Ontology.OntologyBuilder ontBuilder, List<Field<?>> fields) {
        fields.forEach(f -> {
            String name = f.getName().toLowerCase();
            //add field to properties -
            // todo - alert two fields with same name but different type !!
            ontBuilder.addProperty(new Property(name, name, OntologyPrimitiveType.translate(f.getType().getName()).name().toLowerCase()));
        });
    }

    /**
     * entities do not preserve the FK constraints that their table consisted -
     * @param ontBuilder
     * @param statement
     * @param table
     */
    private void createEntity(Ontology.OntologyBuilder ontBuilder, CreateTableStatement statement, Table<?> table) {
        //build ontology entity
        EntityType.Builder builder = EntityType.Builder.get()
                .withName(table.getName().toLowerCase())
                .withEType(table.getName().toLowerCase());

        //build PK fields constraints
        List<String> mandatory = getPKNames(statement);
        //set mandatory fields
        builder.withMandatory(mandatory);
        //set id field name
        builder.withIdField(mandatory.toArray(new String[0]));

        //build ontology properties (if none exist)
        List<Field<?>> fields = statement.getFields();

        //build entity fields
        fields.forEach(field -> builder.withProperty(field.getName().toLowerCase()));
        //add fields as general properties in ontology
        populateFields(ontBuilder, fields);
        //populate dictionary fields (enum)
        EntityType build = builder.build();
        //compile entity
        ontBuilder.addEntityType(build);
    }

    private List<String> getPKNames(CreateTableStatement statement) {
        return primaryKey(statement.getConstraints()).stream()
                .flatMap(pk -> Arrays.stream(pk.getPrimaryKey()).distinct())
                .map(f -> f.getName().toLowerCase()).collect(Collectors.toList());
    }

    /**
     * see https://www.baeldung.com/string-contains-multiple-words
     *
     * @param name
     * @return
     */
    private boolean isRelation(String name) {
        //todo - improve the functionality of creating a relation by thinking of the following:
        // a) Number of F.K
        // b) Name of table with relation to other tables
        // c) Number of P.K
        return relationshipNames.contains(name.toLowerCase());
    }

    private boolean isEntity(String name) {
        return entitiesNames.contains(name.toLowerCase());
    }

}