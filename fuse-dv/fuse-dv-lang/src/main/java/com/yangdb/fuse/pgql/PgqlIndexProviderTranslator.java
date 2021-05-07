package com.yangdb.fuse.pgql;

/*-
 * #%L
 * fuse-dv-lang
 * %%
 * Copyright (C) 2016 - 2021 The YangDb Graph Database Project
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

import com.google.common.collect.ImmutableList;
import com.yangdb.fuse.model.ontology.EntityType;
import com.yangdb.fuse.model.ontology.Ontology;
import com.yangdb.fuse.model.ontology.RelationshipType;
import com.yangdb.fuse.model.resourceInfo.FuseError;
import com.yangdb.fuse.model.schema.*;
import oracle.pgql.lang.Pgql;
import oracle.pgql.lang.PgqlException;
import oracle.pgql.lang.PgqlResult;
import oracle.pgql.lang.ddl.propertygraph.CreatePropertyGraph;
import oracle.pgql.lang.ddl.propertygraph.EdgeTable;
import oracle.pgql.lang.ddl.propertygraph.VertexTable;
import oracle.pgql.lang.ir.PgqlStatement;
import oracle.pgql.lang.ir.SchemaQualifiedName;
import oracle.pgql.lang.ir.StatementType;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static com.yangdb.fuse.model.schema.MappingIndexType.STATIC;
import static com.yangdb.fuse.model.schema.MappingIndexType.UNIFIED;
import static java.lang.String.format;

/**
 * translates the PGQL DDL statement into an index provider low level schema used to generate the E/S mapping & indices
 */
public class PgqlIndexProviderTranslator implements IndexProviderTranslator<String> {

    @Override
    public IndexProvider translate(String ontology, String statement) {
        IndexProvider indexProvider = IndexProvider.Builder.generate(ontology);
        try (Pgql pgql = new Pgql()) {
            //parse DDL graph query
            PgqlStatement pgqlStatement = pgql.parse(statement).getPgqlStatement();
            StatementType type = pgqlStatement.getStatementType();
            if (!type.equals(StatementType.CREATE_PROPERTY_GRAPH)) {
                throw new FuseError.FuseErrorException("Pgql Ontology DDL query was not found ",
                        new FuseError("Pgql Ontology Parser Error", "DDL query was not found " + statement));
            }

            CreatePropertyGraph createGraphDDL = (CreatePropertyGraph) pgqlStatement;
            SchemaQualifiedName graphName = createGraphDDL.getGraphName();
            List<VertexTable> vertexTables = createGraphDDL.getVertexTables();
            List<EdgeTable> edgeTables = createGraphDDL.getEdgeTables();
            //transform tables into vertices
            vertexTables.forEach(t -> transform(indexProvider, t));
            //transform tables into vertices
            edgeTables.forEach(t -> transform(indexProvider, t));

            return  indexProvider;
        } catch (PgqlException e) {
            throw new FuseError.FuseErrorException("No valid Pgql statement DDL query ",
                    new FuseError("Pgql Index Parser Error", "No valid Pgql statement " + statement));
        }
    }



    /**
     * adds vertex table to ontology and its related properties
     *
     * @param indexProvider
     * @param table
     */
    private void transform(IndexProvider indexProvider, VertexTable table) {
//        indexProvider.withEntity(new Entity(tableName, STATIC.name(), "Index",
//                new Props(ImmutableList.of(tableName)), Collections.emptyList(), Collections.emptyMap()));
//        indexProvider.withEntity()
//
//        table.getLabels().forEach(label -> {
//            builder.addEntityType(EntityType.Builder.get()
//                    .withEType(label.getName())
//                    .withDBrName(table.getTableName().getName())
//                    .withName(label.getName())
//                    .withIdField(getField(table))
//                    .withProperties(label.getProperties().stream()
//                            .map(p->format("%s_%s", label.getName(), p.getPropertyName())).collect(Collectors.toList()))
//                    .build());
//            //add label related properties to ontology
//            builder.addProperties(label.getProperties().stream()
//                    .map(p -> new com.yangdb.fuse.model.ontology.Property(
//                            format("%s_%s", label.getName(), p.getPropertyName()),
//                            format("%s_%s", label.getName(), p.getPropertyName()),
//                            getType(p.getValueExpression().getExpType())))
//                    .collect(Collectors.toList()));
//        });
    }


    /**
     * adds edge table to ontology and its related properties
     *
     * @param indexProvider
     * @param table
     */
    private void transform(IndexProvider indexProvider, EdgeTable table) {
/*
        table.getLabels().forEach(edge -> {
            builder.addRelationshipType(RelationshipType.Builder.get()
                    .withDBrName(table.getTableName().getName())
                    .withRType(table.getTableAlias())//type would be associated with the table alias
                    .withName(edge.getName())//name would be associated with the label
                    .withIdField(getField(table))
                    .withProperties(edge.getProperties().stream()
                            .map(p->format("%s_%s", edge.getName(), p.getPropertyName())).collect(Collectors.toList()))
                    .withEPair(buildEpair(builder, table))
                    .build());


            //add label related properties to ontology
            builder.addProperties(edge.getProperties().stream()
                    .map(p -> new com.yangdb.fuse.model.ontology.Property(
                            format("%s_%s", edge.getName(), p.getPropertyName()),
                            format("%s_%s", edge.getName(), p.getPropertyName()),
                            getType(p.getValueExpression().getExpType())))
                    .collect(Collectors.toList()));
        });
*/
    }



}
