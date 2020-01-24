package com.yangdb.fuse.model.graphql;

import com.yangdb.fuse.model.graphql.wiring.TraversalWiringFactory;
import com.yangdb.fuse.model.ontology.Ontology;
import com.yangdb.fuse.model.query.Query;
import graphql.ExecutionResult;
import graphql.GraphQL;
import graphql.schema.GraphQLSchema;
import graphql.schema.idl.SchemaGenerator;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.InputStream;


public class GraphQLQueryExecuterTest {
    public static Ontology ontology;

    @BeforeClass
    public static void setUp() throws Exception {
        InputStream resource = Thread.currentThread().getContextClassLoader().getResourceAsStream("graphql/StarWars.graphql");
        ontology = GraphQL2OntologyTransformer.transform(resource);
        Assert.assertNotNull(ontology);
    }

    private GraphQLSchema createSchema(Query.Builder builder) {
        SchemaGenerator schemaGenerator = new SchemaGenerator();
        return schemaGenerator.makeExecutableSchema(GraphQL2OntologyTransformer.typeRegistry, TraversalWiringFactory.newEchoingWiring(ontology,builder));
    }

    @Test
    public void testQuery() {
        Query.Builder instance = Query.Builder.instance();
        GraphQLSchema schema = createSchema(instance);
        GraphQL graphQL = GraphQL.newGraphQL(schema).build();
        ExecutionResult execute = graphQL.execute("{\n" +
                "    human {\n" +
                "        name,\n" +
                "        friends {\n" +
                "            name\n" +
                "        }\n" +
                "        owns {\n" +
                "            name,\n" +
                "            appearsIn,\n" +
                "            friends {\n" +
                "                name,\n" +
                "                description\n" +
                "            }\n" +
                "        }\n" +
                "    }\n" +
                "}");
        Query query = instance.build();
        Assert.assertNotNull(execute);
        Assert.assertNotNull(query);
    }
}
