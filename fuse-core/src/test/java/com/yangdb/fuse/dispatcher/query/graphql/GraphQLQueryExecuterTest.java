package com.yangdb.fuse.dispatcher.query.graphql;

import com.yangdb.fuse.dispatcher.query.QueryTransformer;
import com.yangdb.fuse.model.execution.plan.descriptors.QueryDescriptor;
import com.yangdb.fuse.model.ontology.Ontology;
import com.yangdb.fuse.model.query.Query;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.InputStream;


public class GraphQLQueryExecuterTest {
    public static Ontology ontology;
    public static QueryTransformer<String, Query> transformer;

    @BeforeClass
    public static void setUp() throws Exception {
        InputStream resource = Thread.currentThread().getContextClassLoader().getResourceAsStream("graphql/StarWars.graphql");
        GraphQL2OntologyTransformer graphQL2OntologyTransformer = new GraphQL2OntologyTransformer();
        ontology = graphQL2OntologyTransformer.transform(resource);
        transformer = new GraphQL2QueryTransformer(graphQL2OntologyTransformer,ontology);
        Assert.assertNotNull(ontology);
    }

    @Test
    public void testQuerySingleVertexWithFewProperties() {
        Query query = transformer.transform(
                " {\n" +
                        "    human {\n" +
                        "        name,\n" +
                        "        description\n" +
                        "    }\n" +
                        "}");
        String expected = "[└── Start, \n" +
                "    ──Typ[Human:1]──Q[2]:{3|4}, \n" +
                "                          └─?[3]:[name<IdentityProjection>], \n" +
                "                          └─?[4]:[description<IdentityProjection>]]";
        Assert.assertEquals(expected, QueryDescriptor.print(query));
    }

    @Test
    public void testQuerySingleVertexWithSinleRelation() {
        Query query = transformer.transform(
          " {\n" +
                "    human {\n" +
                "       friends {\n" +
                "            name\n" +
                "        }\n" +
                "    }\n" +
                "}");
        String expected = "[└── Start, \n" +
                "    ──Typ[Human:1]──Q[2]:{3}, \n" +
                "                        └-> Rel(friends:3)──Typ[Character:4]──Q[5]:{6}, \n" +
                "                                                                  └─?[6]:[name<IdentityProjection>]]";
        Assert.assertEquals(expected, QueryDescriptor.print(query));
    }

    @Test
    public void testQuerySingleVertexWithTwoRelationAndProperties() {
        Query query = transformer.transform(
            " {\n" +
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
        Query query = transformer.transform(
                "{\n" +
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
