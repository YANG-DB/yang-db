package com.yangdb.fuse.model.graphql;

import com.yangdb.fuse.model.query.Query;
import graphql.ExecutionInput;
import graphql.ExecutionResult;
import graphql.GraphQL;
import graphql.schema.GraphQLSchema;

public class GraphQL2QueryTransformer {

    /**
     * translate graphQL query to V1 Query
     * @param query
     * @return
     */
    public static Query transform(GraphQLSchema schema, String query) {
        ExecutionInput input = ExecutionInput.newExecutionInput().query(query).build();

        GraphQL graphQL = GraphQL.newGraphQL(schema).build();
        ExecutionResult execute = graphQL.execute(query);

        Query.Builder builder = Query.Builder.instance();

        return builder.build();
    }
}
