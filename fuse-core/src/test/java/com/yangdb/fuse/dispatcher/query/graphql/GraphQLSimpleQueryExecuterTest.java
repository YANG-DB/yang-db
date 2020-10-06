package com.yangdb.fuse.dispatcher.query.graphql;

import com.yangdb.fuse.dispatcher.ontology.OntologyProvider;
import com.yangdb.fuse.dispatcher.query.QueryTransformer;
import com.yangdb.fuse.model.execution.plan.descriptors.QueryDescriptor;
import com.yangdb.fuse.model.ontology.Ontology;
import com.yangdb.fuse.model.query.Query;
import com.yangdb.fuse.model.query.QueryInfo;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.InputStream;
import java.util.Collection;
import java.util.Optional;

import static com.yangdb.fuse.model.transport.CreateQueryRequestMetadata.TYPE_GRAPHQL;


public class GraphQLSimpleQueryExecuterTest {
    public static Ontology ontology;
    public static QueryTransformer<QueryInfo<String>, Query> transformer;

    @BeforeClass
    public static void setUp() throws Exception {
        InputStream schemaInput = Thread.currentThread().getContextClassLoader().getResourceAsStream("graphql/starWars.graphql");
        InputStream whereInoput = Thread.currentThread().getContextClassLoader().getResourceAsStream("graphql/whereSchema.graphql");
        GraphQLToOntologyTransformer graphQLToOntologyTransformer = new GraphQLToOntologyTransformer();
        ontology = graphQLToOntologyTransformer.transform(schemaInput,whereInoput);
        transformer = new GraphQL2QueryTransformer(graphQLToOntologyTransformer, new OntologyProvider() {
            @Override
            public Optional<Ontology> get(String id) {
                return Optional.of(ontology);
            }

            @Override
            public Collection<Ontology> getAll() {
                return null;
            }

            @Override
            public Ontology add(Ontology ontology) {
                return null;
            }
        });
        Assert.assertNotNull(ontology);
    }

    @Test
    public void testQuerySingleVertexWithFewProperties() {
        String q = " {\n" +
                "    human {\n" +
                "        name,\n" +
                "        description\n" +
                "    }\n" +
                "}";
        Query query = transformer.transform(new QueryInfo<>(q,"q1", TYPE_GRAPHQL,"test"));
        String expected = "[└── Start, \n" +
                "    ──Typ[Human:1]──Q[2]:{3|4}, \n" +
                "                          └─?[3]:[name<IdentityProjection>], \n" +
                "                          └─?[4]:[description<IdentityProjection>]]";
        Assert.assertEquals(expected, QueryDescriptor.print(query));
    }


    @Test
    public void testConstraintByIdQuerySingleVertexWithFewProperties() {
        String q = "{\n" +
                "    human (where: {\n" +
                "        operator: AND,\n" +
                "        constraints: [{\n" +
                "            operand: \"name\",\n" +
                "            operator: \"like\",\n" +
                "            expression: \"jhone\"\n" +
                "        },\n" +
                "        {\n" +
                "            operand: \"description\",\n" +
                "            operator: \"notEmpty\"\n" +
                "        }]\n" +
                "    }) {\n" +
                "\n" +
                "        name,\n" +
                "        description\n" +
                "    }\n" +
                "}";
        Query query = transformer.transform(new QueryInfo<>(q,"q1", TYPE_GRAPHQL,"test"));
        String expected = "[└── Start, \n" +
                "    ──Typ[Human:1]──Q[2]:{3|4|5}, \n" +
                "                            └─?[3]:[name<like,jhone>, description<notEmpty,null>], \n" +
                "                            └─?[4]:[name<IdentityProjection>], \n" +
                "                            └─?[5]:[description<IdentityProjection>]]";
        Assert.assertEquals(expected, QueryDescriptor.print(query));
    }

    @Test
    public void testQuerySingleVertexWithSinleRelation() {
        String q = " {\n" +
                "    human {\n" +
                "       friends {\n" +
                "            name\n" +
                "        }\n" +
                "    }\n" +
                "}";
        Query query = transformer.transform(new QueryInfo<>(q,"q2", TYPE_GRAPHQL,"test"));
        String expected = "[└── Start, \n" +
                "    ──Typ[Human:1]──Q[2]:{3}, \n" +
                "                        └-> Rel(friends:3)──Typ[Character:4]──Q[5]:{6}, \n" +
                "                                                                  └─?[6]:[name<IdentityProjection>]]";
        Assert.assertEquals(expected, QueryDescriptor.print(query));
    }

    @Test
    public void testQuerySingleVertexWithTwoRelationAndProperties() {
        String q = " {\n" +
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
                "}";
        Query query = transformer.transform(new QueryInfo<>(q,"q3", TYPE_GRAPHQL,"test"));
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
        String q = "{\n" +
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
                "}";
        Query query = transformer.transform(new QueryInfo<>(q,"q4", TYPE_GRAPHQL,"test"));
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
