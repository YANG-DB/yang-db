package com.yangdb.fuse.dispatcher.query.rdf;

import com.google.common.collect.Sets;
import com.yangdb.fuse.dispatcher.ontology.SimpleOntologyProvider;
import com.yangdb.fuse.dispatcher.query.QueryTransformer;
import com.yangdb.fuse.model.execution.plan.descriptors.QueryDescriptor;
import com.yangdb.fuse.model.ontology.Ontology;
import com.yangdb.fuse.model.query.Query;
import com.yangdb.fuse.model.query.QueryInfo;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;
import java.net.URL;
import java.nio.file.Files;

import static com.yangdb.fuse.model.transport.CreateQueryRequestMetadata.TYPE_SPARQL;


/**
 * https://dzone.com/articles/sparql-and-cypher
 * Addapting the example queries for test purpose
 */
@Ignore
public class SPARQLOntologyToV1QLExecuterTest {
    public static Ontology ontology;
    public static QueryTransformer<QueryInfo<String>, Query> queryTransformer;

    @BeforeClass
    @Ignore("Todo fix")
    public static void setUp() throws Exception {
        URL personas = Thread.currentThread().getContextClassLoader().getResource("rdf/personasonto.owl");
        OWL2OntologyTransformer transformer = new OWL2OntologyTransformer();
        //load owl ontologies - the order of the ontologies is important in regards with the owl dependencies
        ontology = transformer.transform(Sets.newHashSet(
                new String(Files.readAllBytes(new File(personas.toURI()).toPath()))));

        queryTransformer = new SparQL2QueryTransformer(new SimpleOntologyProvider(ontology));
        // transformer
        Assert.assertNotNull(ontology);
    }

    @Test
    @Ignore
    public void testQuerySingleVertexWithFewProperties() {
        Query query = queryTransformer.transform(new QueryInfo<>(
                "SELECT ?ee WHERE { ?ee a <Person>;<name> ?name. FILTER(?name = \"Emil\")}",
                "q1", TYPE_SPARQL, ontology.getOnt()));
        String expected = "[└── Start, \n" +
                "    ──Typ[Human:1]──Q[2]:{3|4}, \n" +
                "                          └─?[3]:[name<IdentityProjection>], \n" +
                "                          └─?[4]:[description<IdentityProjection>]]";
        Assert.assertEquals(expected, QueryDescriptor.print(query));
    }


}
