package com.yangdb.fuse.model.graphql;

import com.yangdb.fuse.model.execution.plan.descriptors.QueryDescriptor;
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
    public void testQuerySingleVertexWithFewProperties() {
        Query.Builder instance = Query.Builder.instance();
        GraphQLSchema schema = createSchema(instance);
        GraphQL graphQL = GraphQL.newGraphQL(schema).build();
        ExecutionResult execute = graphQL.execute(
          " {\n" +
                "    human {\n" +
                "        name,\n" +
                "        description\n" +
                "    }\n" +
                "}");
        Query query = instance.build();
        Assert.assertNotNull(execute);
        String expected = "[└── Start, \n" +
                "    ──Typ[Human:1]──Q[2]:{3|4}, \n" +
                "                          └─?[3]:[name<IdentityProjection>], \n" +
                "                          └─?[4]:[description<IdentityProjection>]]";
        Assert.assertEquals(expected, QueryDescriptor.print(query));
    }
    @Test
    public void testQuerySingleVertexWithSinleRelation() {
        Query.Builder instance = Query.Builder.instance();
        GraphQLSchema schema = createSchema(instance);
        GraphQL graphQL = GraphQL.newGraphQL(schema).build();
        ExecutionResult execute = graphQL.execute(
          " {\n" +
                "    human {\n" +
                "       friends {\n" +
                "            name\n" +
                "        }\n" +
                "    }\n" +
                "}");
        Query query = instance.build();
        Assert.assertNotNull(execute);
        String expected = "[└── Start, \n" +
                "    ──Typ[Human:1]──Q[2]:{3}, \n" +
                "                        └-> Rel(friends:3)──Typ[Character:4]──Q[5]:{6}, \n" +
                "                                                                  └─?[6]:[name<IdentityProjection>]]";
        Assert.assertEquals(expected, QueryDescriptor.print(query));
    }

    @Test
    public void testQuerySingleVertexWithTwoRelationAndProperties() {
        Query.Builder instance = Query.Builder.instance();
        GraphQLSchema schema = createSchema(instance);
        GraphQL graphQL = GraphQL.newGraphQL(schema).build();
        ExecutionResult execute = graphQL.execute("{\n" +
                "    human {\n" +
                "        name,\n" +
                "        friends {\n" +
                "            name\n" +
                "        },\n" +
                "        owns {\n" +
                "            name,\n" +
                "            appearsIn\n" +
                "            }\n" +
                "    }\n" +
                "}");
        Query query = instance.build();
        Assert.assertNotNull(execute);
        String expected = "[└── Start, \n" +
                "    ──Typ[Human:1]──Q[2]:{3|4|8}, \n" +
                "                            └─?[3]:[name<IdentityProjection>], \n" +
                "                            └-> Rel(friends:4)──Typ[Character:5]──Q[6]:{7}, \n" +
                "                                                                      └─?[7]:[name<IdentityProjection>]──Typ[Droid:9]──Q[10]:{11|12}, \n" +
                "                            └-> Rel(owns:8), \n" +
                "                                       └─?[11]:[name<IdentityProjection>], \n" +
                "                                       └─?[12]:[appearsIn<IdentityProjection>]]";
        Assert.assertEquals(expected, QueryDescriptor.print(query));
    }

    @Test
    public void testQuerySingleVertexWithTwoHopesRelationAndProperties() {
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
        String expected = "[└── Start, \n" +
                "    ──Typ[Human:1]──Q[2]:{3|4|8}, \n" +
                "                            └─?[3]:[name<IdentityProjection>], \n" +
                "                            └-> Rel(friends:4)──Typ[Character:5]──Q[6]:{7}, \n" +
                "                                                                      └─?[7]:[name<IdentityProjection>]──Typ[Droid:9]──Q[10]:{11|12|13}, \n" +
                "                            └-> Rel(owns:8), \n" +
                "                                       └─?[11]:[name<IdentityProjection>], \n" +
                "                                       └─?[12]:[appearsIn<IdentityProjection>], \n" +
                "                                       └-> Rel(friends:13)──Typ[Character:14]──Q[15]:{16|17}, \n" +
                "                                                                                        └─?[16]:[name<IdentityProjection>], \n" +
                "                                                                                        └─?[17]:[description<IdentityProjection>]]";
        Assert.assertEquals(expected, QueryDescriptor.print(query));
    }
}
