package com.yangdb.fuse.model.graphql;

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

import com.yangdb.fuse.model.graphql.wiring.TraversalWiringFactory;
import com.yangdb.fuse.model.ontology.Ontology;
import com.yangdb.fuse.model.query.Query;
import graphql.ExecutionResult;
import graphql.GraphQL;
import graphql.schema.GraphQLSchema;
import graphql.schema.idl.SchemaGenerator;
import graphql.schema.idl.TypeDefinitionRegistry;

public class GraphQL2QueryTransformer {

    /**
     * translate graphQL query to V1 Query
     * @param query
     * @return
     */
    public static Query transform(TypeDefinitionRegistry typeRegistry,Ontology ontology, String query) {
        Query.Builder instance = Query.Builder.instance();
        GraphQLSchema schema = createSchema(typeRegistry,ontology,instance);
        GraphQL graphQL = GraphQL.newGraphQL(schema).build();
        ExecutionResult execute = graphQL.execute(query);
        if(execute.getErrors().isEmpty())
            return instance.build();
        throw new IllegalArgumentException(execute.getErrors().toString());
    }

    private static GraphQLSchema createSchema(TypeDefinitionRegistry typeRegistry,Ontology ontology,Query.Builder builder) {
        SchemaGenerator schemaGenerator = new SchemaGenerator();
        return schemaGenerator.makeExecutableSchema(typeRegistry, TraversalWiringFactory.newEchoingWiring(ontology,builder));
    }

}
