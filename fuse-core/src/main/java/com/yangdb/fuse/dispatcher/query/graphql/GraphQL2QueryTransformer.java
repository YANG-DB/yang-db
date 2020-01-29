package com.yangdb.fuse.dispatcher.query.graphql;

/*-
 * #%L
 * fuse-model
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
import com.yangdb.fuse.dispatcher.ontology.OntologyProvider;
import com.yangdb.fuse.dispatcher.query.QueryTransformer;
import com.yangdb.fuse.dispatcher.query.graphql.wiring.TraversalWiringFactory;
import com.yangdb.fuse.model.ontology.Ontology;
import com.yangdb.fuse.model.query.Query;
import com.yangdb.fuse.model.query.QueryInfo;
import com.yangdb.fuse.model.resourceInfo.FuseError;
import graphql.ExecutionResult;
import graphql.GraphQL;
import graphql.schema.GraphQLSchema;
import graphql.schema.idl.SchemaGenerator;

import java.util.Optional;

public class GraphQL2QueryTransformer implements QueryTransformer<QueryInfo<String>, Query>  {

    private final GraphQLSchemaUtils schemaUtils;
    private OntologyProvider ontologyProvider;

    @Inject
    public GraphQL2QueryTransformer(GraphQLSchemaUtils schemaUtils, OntologyProvider ontologyProvider) {
        this.schemaUtils = schemaUtils;
        this.ontologyProvider = ontologyProvider;
    }


    public Query transform(QueryInfo<String> query) {
        Optional<Ontology> ontology =  ontologyProvider.get(query.getOntology());
        if(!ontology.isPresent())
            throw new FuseError.FuseErrorException(new FuseError("No ontology was found","Ontology not found "+query.getOntology()));

        return transform(schemaUtils,ontology.get(),query.getQuery());
    }

    /**
     * translate graphQL query to V1 Query
     * @param query
     * @return
     */
    public static Query transform(GraphQLSchemaUtils schemaUtils,Ontology ontology, String query) {
        Query.Builder instance = Query.Builder.instance();
        GraphQLSchema schema = createSchema(schemaUtils,ontology,instance);
        GraphQL graphQL = GraphQL.newGraphQL(schema).build();
        ExecutionResult execute = graphQL.execute(query);
        if(execute.getErrors().isEmpty())
            return instance.build();

        throw new IllegalArgumentException(execute.getErrors().toString());
    }

    private static GraphQLSchema createSchema(GraphQLSchemaUtils schemaUtils,Ontology ontology,Query.Builder builder) {
        SchemaGenerator schemaGenerator = new SchemaGenerator();
        return schemaGenerator.makeExecutableSchema(
                schemaUtils.getTypeRegistry(),
                TraversalWiringFactory.newEchoingWiring(schemaUtils,ontology,builder));
    }

}
