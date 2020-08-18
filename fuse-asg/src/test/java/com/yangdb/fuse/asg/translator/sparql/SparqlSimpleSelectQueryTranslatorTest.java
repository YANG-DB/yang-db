
package com.yangdb.fuse.asg.translator.sparql;

import com.google.common.collect.Sets;
import com.yangdb.fuse.asg.AsgSparQLTransformer;
import com.yangdb.fuse.asg.strategy.M1SparqlAsgStrategyRegistrar;
import com.yangdb.fuse.asg.strategy.SparqlAsgStrategyRegistrar;
import com.yangdb.fuse.asg.translator.sparql.strategies.SparqlTranslatorStrategy;
import com.yangdb.fuse.dispatcher.ontology.SimpleOntologyProvider;
import com.yangdb.fuse.dispatcher.query.rdf.OWL2OntologyTransformer;
import com.yangdb.fuse.model.asgQuery.AsgQuery;
import com.yangdb.fuse.model.ontology.Ontology;
import com.yangdb.fuse.model.query.QueryInfo;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.semanticweb.owlapi.model.IRI;

import java.io.File;
import java.net.URL;
import java.nio.file.Files;
import java.util.Collections;

import static com.yangdb.fuse.model.execution.plan.descriptors.AsgQueryDescriptor.print;
import static com.yangdb.fuse.model.ontology.Ontology.OntologyBuilder.YANGDB_ORG;
import static com.yangdb.fuse.model.transport.CreateQueryRequestMetadata.TYPE_SPARQL;
import static org.junit.Assert.assertEquals;

/**
 * Implemented the next sample queries
 * https://dzone.com/articles/sparql-and-cypher
 * https://www.w3.org/2009/Talks/0615-qbe/
 */
@Ignore
public class SparqlSimpleSelectQueryTranslatorTest {
    private AsgSparQLTransformer sparQLTransformer;
    private SparqlTranslatorStrategy match;


    @Before
    public void setUp() throws Exception {
        URL personas = Thread.currentThread().getContextClassLoader().getResource("sparql/foaf.owl");
        OWL2OntologyTransformer transformer = new OWL2OntologyTransformer();
        //load owl ontologies - the order of the ontologies is important in regards with the owl dependencies
        Ontology ontology = transformer.transform(Sets.newHashSet(
                new String(Files.readAllBytes(new File(personas.toURI()).toPath()))));

        ontology.setOnt(IRI.create(ontology.getOnt()+"/foaf").toString());
        sparQLTransformer = new AsgSparQLTransformer(new SparqlTranslator(new M1SparqlAsgStrategyRegistrar(new SimpleOntologyProvider(ontology))));
        // transformer
        Assert.assertNotNull(ontology);
    }

    @Test
    public void testSimpleSelectVarsAndPatternTriplet() {
        String s = "PREFIX foaf:  <http://xmlns.com/foaf/0.1/>\n" +
                "SELECT ?name\n" +
                "WHERE {\n" +
                "    ?person foaf:name ?name .\n" +
                "}";
        final AsgQuery query = sparQLTransformer.transform(new QueryInfo<>(s, "q", TYPE_SPARQL, YANGDB_ORG +"/foaf"));

        String expected = "[└── Start, \n" +
                "    ──Typ[:Entity person#1]──Q[100:all]:{2|4}, \n" +
                "                                         └-> Rel(:hasEvalue Rel_#2#2)──Typ[:Evalue personName#3]──Q[300:all]:{301}, \n" +
                "                                                                                                              └─?[..][301]──Typ[:Entity m1#5]──Q[800:all]:{6|801}, \n" +
                "                                                                                                                      └─?[301]:[stringValue<eq,Tom Hanks>], \n" +
                "                                         └-> Rel(:relatedEntity tomActedIn#4), \n" +
                "                                                                                                              └─?[..][400], \n" +
                "                                                                                                                      └─?[401]:[category<eq,ACTED_IN>], \n" +
                "                                                                                                                                                  └─Typ[:Entity otherPerson#6]──Q[600:all]:{7}, \n" +
                "                                                                                                                                                                                          └-> Rel(:relatedEntity othersActedIn#7)──Typ[:Entity m2#8], \n" +
                "                                                                                                                                                                                                                             └─?[..][700], \n" +
                "                                                                                                                                                                                                                                     └─?[701]:[category<eq,ACTED_IN>], \n" +
                "                                                                                                                                                  └─?[..][801], \n" +
                "                                                                                                                                                          └─?[801]:[name<eq,m2.name>]]";
        assertEquals(expected, print(query));

    }

    @Test
    public void testSimpleSelectVarsAndMulriPatternTriplet() {
        String s = "PREFIX foaf:  <http://xmlns.com/foaf/0.1/>\n" +
                "SELECT *\n" +
                "WHERE {\n" +
                "    ?person foaf:name ?name .\n" +
                "    ?person foaf:mbox ?email .\n" +
                "}";
        final AsgQuery query = sparQLTransformer.transform(new QueryInfo<>(s, "q", TYPE_SPARQL, YANGDB_ORG +"/foaf"));

        String expected = "[└── Start, \n" +
                "    ──Typ[:Entity person#1]──Q[100:all]:{2|4}, \n" +
                "                                         └-> Rel(:hasEvalue Rel_#2#2)──Typ[:Evalue personName#3]──Q[300:all]:{301}, \n" +
                "                                                                                                              └─?[..][301]──Typ[:Entity m1#5]──Q[800:all]:{6|801}, \n" +
                "                                                                                                                      └─?[301]:[stringValue<eq,Tom Hanks>], \n" +
                "                                         └-> Rel(:relatedEntity tomActedIn#4), \n" +
                "                                                                                                              └─?[..][400], \n" +
                "                                                                                                                      └─?[401]:[category<eq,ACTED_IN>], \n" +
                "                                                                                                                                                  └─Typ[:Entity otherPerson#6]──Q[600:all]:{7}, \n" +
                "                                                                                                                                                                                          └-> Rel(:relatedEntity othersActedIn#7)──Typ[:Entity m2#8], \n" +
                "                                                                                                                                                                                                                             └─?[..][700], \n" +
                "                                                                                                                                                                                                                                     └─?[701]:[category<eq,ACTED_IN>], \n" +
                "                                                                                                                                                  └─?[..][801], \n" +
                "                                                                                                                                                          └─?[801]:[name<eq,m2.name>]]";
        assertEquals(expected, print(query));

    }

    @Test
    public void testSimpleSelectVarsAndMulriPatternTraversingGraphTriplet() {
        String s = "PREFIX foaf:  <http://xmlns.com/foaf/0.1/>\n" +
                "PREFIX card: <http://www.w3.org/People/Berners-Lee/card#>\n" +
                "SELECT ?homepage\n" +
                "FROM <http://www.w3.org/People/Berners-Lee/card>\n" +
                "WHERE {\n" +
                "    card:i foaf:knows ?known .\n" +
                "    ?known foaf:homepage ?homepage .\n" +
                "}";
        final AsgQuery query = sparQLTransformer.transform(new QueryInfo<>(s, "q", TYPE_SPARQL, YANGDB_ORG +"/foaf"));

        String expected = "[└── Start, \n" +
                "    ──Typ[:Entity person#1]──Q[100:all]:{2|4}, \n" +
                "                                         └-> Rel(:hasEvalue Rel_#2#2)──Typ[:Evalue personName#3]──Q[300:all]:{301}, \n" +
                "                                                                                                              └─?[..][301]──Typ[:Entity m1#5]──Q[800:all]:{6|801}, \n" +
                "                                                                                                                      └─?[301]:[stringValue<eq,Tom Hanks>], \n" +
                "                                         └-> Rel(:relatedEntity tomActedIn#4), \n" +
                "                                                                                                              └─?[..][400], \n" +
                "                                                                                                                      └─?[401]:[category<eq,ACTED_IN>], \n" +
                "                                                                                                                                                  └─Typ[:Entity otherPerson#6]──Q[600:all]:{7}, \n" +
                "                                                                                                                                                                                          └-> Rel(:relatedEntity othersActedIn#7)──Typ[:Entity m2#8], \n" +
                "                                                                                                                                                                                                                             └─?[..][700], \n" +
                "                                                                                                                                                                                                                                     └─?[701]:[category<eq,ACTED_IN>], \n" +
                "                                                                                                                                                  └─?[..][801], \n" +
                "                                                                                                                                                          └─?[801]:[name<eq,m2.name>]]";
        assertEquals(expected, print(query));

    }
    @Test
    public void testSimpleSelectVarsAndMulriPatternWithFilterTriplet() {
        String s = "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>        \n" +
                "PREFIX type: <http://dbpedia.org/class/yago/>\n" +
                "PREFIX prop: <http://dbpedia.org/property/>\n" +
                "SELECT ?country_name ?population\n" +
                "WHERE {\n" +
                "    ?country a type:LandlockedCountries ;\n" +
                "             rdfs:label ?country_name ;\n" +
                "             prop:populationEstimate ?population .\n" +
                "    FILTER (?population > 15000000) .\n" +
                "}";
        final AsgQuery query = sparQLTransformer.transform(new QueryInfo<>(s, "q", TYPE_SPARQL, YANGDB_ORG +"/foaf"));

        String expected = "[└── Start, \n" +
                "    ──Typ[:Entity person#1]──Q[100:all]:{2|4}, \n" +
                "                                         └-> Rel(:hasEvalue Rel_#2#2)──Typ[:Evalue personName#3]──Q[300:all]:{301}, \n" +
                "                                                                                                              └─?[..][301]──Typ[:Entity m1#5]──Q[800:all]:{6|801}, \n" +
                "                                                                                                                      └─?[301]:[stringValue<eq,Tom Hanks>], \n" +
                "                                         └-> Rel(:relatedEntity tomActedIn#4), \n" +
                "                                                                                                              └─?[..][400], \n" +
                "                                                                                                                      └─?[401]:[category<eq,ACTED_IN>], \n" +
                "                                                                                                                                                  └─Typ[:Entity otherPerson#6]──Q[600:all]:{7}, \n" +
                "                                                                                                                                                                                          └-> Rel(:relatedEntity othersActedIn#7)──Typ[:Entity m2#8], \n" +
                "                                                                                                                                                                                                                             └─?[..][700], \n" +
                "                                                                                                                                                                                                                                     └─?[701]:[category<eq,ACTED_IN>], \n" +
                "                                                                                                                                                  └─?[..][801], \n" +
                "                                                                                                                                                          └─?[801]:[name<eq,m2.name>]]";
        assertEquals(expected, print(query));

    }

    @Test
    public void testSimpleSelectVarsAndMulriPatternWithOptionalFilterTriplet() {
        String s = "PREFIX mo: <http://purl.org/ontology/mo/>\n" +
                "PREFIX foaf:  <http://xmlns.com/foaf/0.1/>\n" +
                "SELECT ?name ?img ?hp ?loc\n" +
                "WHERE {\n" +
                "  ?a a mo:MusicArtist ;\n" +
                "     foaf:name ?name .\n" +
                "  OPTIONAL { ?a foaf:img ?img }\n" +
                "  OPTIONAL { ?a foaf:homepage ?hp }\n" +
                "  OPTIONAL { ?a foaf:based_near ?loc }\n" +
                "}";
        final AsgQuery query = sparQLTransformer.transform(new QueryInfo<>(s, "q", TYPE_SPARQL, YANGDB_ORG +"/foaf"));

        String expected = "[└── Start, \n" +
                "    ──Typ[:Entity person#1]──Q[100:all]:{2|4}, \n" +
                "                                         └-> Rel(:hasEvalue Rel_#2#2)──Typ[:Evalue personName#3]──Q[300:all]:{301}, \n" +
                "                                                                                                              └─?[..][301]──Typ[:Entity m1#5]──Q[800:all]:{6|801}, \n" +
                "                                                                                                                      └─?[301]:[stringValue<eq,Tom Hanks>], \n" +
                "                                         └-> Rel(:relatedEntity tomActedIn#4), \n" +
                "                                                                                                              └─?[..][400], \n" +
                "                                                                                                                      └─?[401]:[category<eq,ACTED_IN>], \n" +
                "                                                                                                                                                  └─Typ[:Entity otherPerson#6]──Q[600:all]:{7}, \n" +
                "                                                                                                                                                                                          └-> Rel(:relatedEntity othersActedIn#7)──Typ[:Entity m2#8], \n" +
                "                                                                                                                                                                                                                             └─?[..][700], \n" +
                "                                                                                                                                                                                                                                     └─?[701]:[category<eq,ACTED_IN>], \n" +
                "                                                                                                                                                  └─?[..][801], \n" +
                "                                                                                                                                                          └─?[801]:[name<eq,m2.name>]]";
        assertEquals(expected, print(query));

    }

    @Test
    public void testSimpleSelectVarsAndMulriPatternWithUnionTriplet() {
        String s = "PREFIX go: <http://purl.org/obo/owl/GO#>\n" +
                "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" +
                "PREFIX obo: <http://www.obofoundry.org/ro/ro.owl#>\n" +
                "SELECT DISTINCT ?label ?process\n" +
                "WHERE {\n" +
                "  { ?process obo:part_of go:GO_0007165 } # integral to\n" +
                "      UNION\n" +
                "  { ?process rdfs:subClassOf go:GO_0007165 } # refinement of\n" +
                "  ?process rdfs:label ?label\n" +
                "}";
        final AsgQuery query = sparQLTransformer.transform(new QueryInfo<>(s, "q", TYPE_SPARQL, YANGDB_ORG +"/foaf"));

        String expected = "[└── Start, \n" +
                "    ──Typ[:Entity person#1]──Q[100:all]:{2|4}, \n" +
                "                                         └-> Rel(:hasEvalue Rel_#2#2)──Typ[:Evalue personName#3]──Q[300:all]:{301}, \n" +
                "                                                                                                              └─?[..][301]──Typ[:Entity m1#5]──Q[800:all]:{6|801}, \n" +
                "                                                                                                                      └─?[301]:[stringValue<eq,Tom Hanks>], \n" +
                "                                         └-> Rel(:relatedEntity tomActedIn#4), \n" +
                "                                                                                                              └─?[..][400], \n" +
                "                                                                                                                      └─?[401]:[category<eq,ACTED_IN>], \n" +
                "                                                                                                                                                  └─Typ[:Entity otherPerson#6]──Q[600:all]:{7}, \n" +
                "                                                                                                                                                                                          └-> Rel(:relatedEntity othersActedIn#7)──Typ[:Entity m2#8], \n" +
                "                                                                                                                                                                                                                             └─?[..][700], \n" +
                "                                                                                                                                                                                                                                     └─?[701]:[category<eq,ACTED_IN>], \n" +
                "                                                                                                                                                  └─?[..][801], \n" +
                "                                                                                                                                                          └─?[801]:[name<eq,m2.name>]]";
        assertEquals(expected, print(query));

    }
    @Test
    public void testQueryNamedGraphs() {
        String s = "SELECT DISTINCT ?person\n" +
                "WHERE {\n" +
                "    ?person foaf:name ?name .\n" +
                "    GRAPH ?g1 { ?person a foaf:Person }\n" +
                "    GRAPH ?g2 { ?person a foaf:Person }\n" +
                "    GRAPH ?g3 { ?person a foaf:Person }\n" +
                "    FILTER(?g1 != ?g2 && ?g1 != ?g3 && ?g2 != ?g3) .\n" +
                "}     ";
        final AsgQuery query = sparQLTransformer.transform(new QueryInfo<>(s, "q", TYPE_SPARQL, YANGDB_ORG +"/foaf"));

        String expected = "[└── Start, \n" +
                "    ──Typ[:Entity person#1]──Q[100:all]:{2|4}, \n" +
                "                                         └-> Rel(:hasEvalue Rel_#2#2)──Typ[:Evalue personName#3]──Q[300:all]:{301}, \n" +
                "                                                                                                              └─?[..][301]──Typ[:Entity m1#5]──Q[800:all]:{6|801}, \n" +
                "                                                                                                                      └─?[301]:[stringValue<eq,Tom Hanks>], \n" +
                "                                         └-> Rel(:relatedEntity tomActedIn#4), \n" +
                "                                                                                                              └─?[..][400], \n" +
                "                                                                                                                      └─?[401]:[category<eq,ACTED_IN>], \n" +
                "                                                                                                                                                  └─Typ[:Entity otherPerson#6]──Q[600:all]:{7}, \n" +
                "                                                                                                                                                                                          └-> Rel(:relatedEntity othersActedIn#7)──Typ[:Entity m2#8], \n" +
                "                                                                                                                                                                                                                             └─?[..][700], \n" +
                "                                                                                                                                                                                                                                     └─?[701]:[category<eq,ACTED_IN>], \n" +
                "                                                                                                                                                  └─?[..][801], \n" +
                "                                                                                                                                                          └─?[801]:[name<eq,m2.name>]]";
        assertEquals(expected, print(query));

    }

    @Test
    public void testQueryFreeTextSearch() {
        String s = "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\n" +
                "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>        \n" +
                "PREFIX type: <http://dbpedia.org/class/yago/>\n" +
                "PREFIX prop: <http://dbpedia.org/property/>\n" +
                "SELECT ?lbl ?est\n" +
                "WHERE {\n" +
                "  ?country rdfs:label ?lbl .\n" +
                "  FILTER(bif:contains(?lbl, \"Republic\")) .\n" +
                "  ?country a type:Country108544813 ;\n" +
                "      prop:establishedDate ?est .\n" +
                "  FILTER(?est < \"1920-01-01\"^^xsd:date) .\n" +
                "}";
        final AsgQuery query = sparQLTransformer.transform(new QueryInfo<>(s, "q", TYPE_SPARQL, YANGDB_ORG +"/foaf"));

        String expected = "[└── Start, \n" +
                "    ──Typ[:Entity person#1]──Q[100:all]:{2|4}, \n" +
                "                                         └-> Rel(:hasEvalue Rel_#2#2)──Typ[:Evalue personName#3]──Q[300:all]:{301}, \n" +
                "                                                                                                              └─?[..][301]──Typ[:Entity m1#5]──Q[800:all]:{6|801}, \n" +
                "                                                                                                                      └─?[301]:[stringValue<eq,Tom Hanks>], \n" +
                "                                         └-> Rel(:relatedEntity tomActedIn#4), \n" +
                "                                                                                                              └─?[..][400], \n" +
                "                                                                                                                      └─?[401]:[category<eq,ACTED_IN>], \n" +
                "                                                                                                                                                  └─Typ[:Entity otherPerson#6]──Q[600:all]:{7}, \n" +
                "                                                                                                                                                                                          └-> Rel(:relatedEntity othersActedIn#7)──Typ[:Entity m2#8], \n" +
                "                                                                                                                                                                                                                             └─?[..][700], \n" +
                "                                                                                                                                                                                                                                     └─?[701]:[category<eq,ACTED_IN>], \n" +
                "                                                                                                                                                  └─?[..][801], \n" +
                "                                                                                                                                                          └─?[801]:[name<eq,m2.name>]]";
        assertEquals(expected, print(query));

    }
    @Test
    public void testQueryWithAggregation() {
        String s = "PREFIX foaf: <http://xmlns.com/foaf/0.1/>\n" +
                "SELECT ?interest COUNT(*) AS ?count where\n" +
                "  {\n" +
                "    ?p foaf:interest <http://www.livejournal.com/interests.bml?int=harry+potter> .\n" +
                "    ?p foaf:interest ?interest\n" +
                "  }\n" +
                "GROUP BY ?interest ORDER BY DESC(COUNT(*)) LIMIT 10";
        final AsgQuery query = sparQLTransformer.transform(new QueryInfo<>(s, "q", TYPE_SPARQL, YANGDB_ORG +"/foaf"));

        String expected = "[└── Start, \n" +
                "    ──Typ[:Entity person#1]──Q[100:all]:{2|4}, \n" +
                "                                         └-> Rel(:hasEvalue Rel_#2#2)──Typ[:Evalue personName#3]──Q[300:all]:{301}, \n" +
                "                                                                                                              └─?[..][301]──Typ[:Entity m1#5]──Q[800:all]:{6|801}, \n" +
                "                                                                                                                      └─?[301]:[stringValue<eq,Tom Hanks>], \n" +
                "                                         └-> Rel(:relatedEntity tomActedIn#4), \n" +
                "                                                                                                              └─?[..][400], \n" +
                "                                                                                                                      └─?[401]:[category<eq,ACTED_IN>], \n" +
                "                                                                                                                                                  └─Typ[:Entity otherPerson#6]──Q[600:all]:{7}, \n" +
                "                                                                                                                                                                                          └-> Rel(:relatedEntity othersActedIn#7)──Typ[:Entity m2#8], \n" +
                "                                                                                                                                                                                                                             └─?[..][700], \n" +
                "                                                                                                                                                                                                                                     └─?[701]:[category<eq,ACTED_IN>], \n" +
                "                                                                                                                                                  └─?[..][801], \n" +
                "                                                                                                                                                          └─?[801]:[name<eq,m2.name>]]";
        assertEquals(expected, print(query));

    }

    @Test
    public void testSimpleSelectPersonWithSingleTriplet() {
        String s = "SELECT ?ee WHERE { ?ee a <Person>;<name> ?name. FILTER(?name = \"Emil\")}";
        final AsgQuery query = sparQLTransformer.transform(new QueryInfo<>(s, "q", TYPE_SPARQL, YANGDB_ORG +"/foaf"));

        String expected = "[└── Start, \n" +
                "    ──Typ[:Entity person#1]──Q[100:all]:{2|4}, \n" +
                "                                         └-> Rel(:hasEvalue Rel_#2#2)──Typ[:Evalue personName#3]──Q[300:all]:{301}, \n" +
                "                                                                                                              └─?[..][301]──Typ[:Entity m1#5]──Q[800:all]:{6|801}, \n" +
                "                                                                                                                      └─?[301]:[stringValue<eq,Tom Hanks>], \n" +
                "                                         └-> Rel(:relatedEntity tomActedIn#4), \n" +
                "                                                                                                              └─?[..][400], \n" +
                "                                                                                                                      └─?[401]:[category<eq,ACTED_IN>], \n" +
                "                                                                                                                                                  └─Typ[:Entity otherPerson#6]──Q[600:all]:{7}, \n" +
                "                                                                                                                                                                                          └-> Rel(:relatedEntity othersActedIn#7)──Typ[:Entity m2#8], \n" +
                "                                                                                                                                                                                                                             └─?[..][700], \n" +
                "                                                                                                                                                                                                                                     └─?[701]:[category<eq,ACTED_IN>], \n" +
                "                                                                                                                                                  └─?[..][801], \n" +
                "                                                                                                                                                          └─?[801]:[name<eq,m2.name>]]";
        assertEquals(expected, print(query));

    }

    @Test
    public void testSimpleSelectPersonWithFriendTriplet() {
        String s = "SELECT ?ee ?friends\n" +
                "    WHERE {?ee a <Person>;\n" +
                "        <name> \"Emil\";\n" +
                "        <knows> ?friends }\n";
        final AsgQuery query = sparQLTransformer.transform(new QueryInfo<>(s, "q", TYPE_SPARQL, YANGDB_ORG +"/foaf"));

        String expected = "[└── Start, \n" +
                "    ──Typ[:Entity person#1]──Q[100:all]:{2|4}, \n" +
                "                                         └-> Rel(:hasEvalue Rel_#2#2)──Typ[:Evalue personName#3]──Q[300:all]:{301}, \n" +
                "                                                                                                              └─?[..][301]──Typ[:Entity m1#5]──Q[800:all]:{6|801}, \n" +
                "                                                                                                                      └─?[301]:[stringValue<eq,Tom Hanks>], \n" +
                "                                         └-> Rel(:relatedEntity tomActedIn#4), \n" +
                "                                                                                                              └─?[..][400], \n" +
                "                                                                                                                      └─?[401]:[category<eq,ACTED_IN>], \n" +
                "                                                                                                                                                  └─Typ[:Entity otherPerson#6]──Q[600:all]:{7}, \n" +
                "                                                                                                                                                                                          └-> Rel(:relatedEntity othersActedIn#7)──Typ[:Entity m2#8], \n" +
                "                                                                                                                                                                                                                             └─?[..][700], \n" +
                "                                                                                                                                                                                                                                     └─?[701]:[category<eq,ACTED_IN>], \n" +
                "                                                                                                                                                  └─?[..][801], \n" +
                "                                                                                                                                                          └─?[801]:[name<eq,m2.name>]]";
        assertEquals(expected, print(query));

    }
    @Test
    public void testSimpleSelectDistinctPersonWithRelatedOtherSideTriplet() {
        String s = "SELECT DISTINCT ?surfer\n" +
                "    WHERE {?js a <Person>;\n" +
                "        <name> \"Jhone\";\n" +
                "        <knows>/<knows> ?surfer;\n" +
                "        ?surfer <hobby> \"surfer\".}";
        final AsgQuery query = sparQLTransformer.transform(new QueryInfo<>(s, "q", TYPE_SPARQL, YANGDB_ORG +"/foaf"));

        String expected = "[└── Start, \n" +
                "    ──Typ[:Entity person#1]──Q[100:all]:{2|4}, \n" +
                "                                         └-> Rel(:hasEvalue Rel_#2#2)──Typ[:Evalue personName#3]──Q[300:all]:{301}, \n" +
                "                                                                                                              └─?[..][301]──Typ[:Entity m1#5]──Q[800:all]:{6|801}, \n" +
                "                                                                                                                      └─?[301]:[stringValue<eq,Tom Hanks>], \n" +
                "                                         └-> Rel(:relatedEntity tomActedIn#4), \n" +
                "                                                                                                              └─?[..][400], \n" +
                "                                                                                                                      └─?[401]:[category<eq,ACTED_IN>], \n" +
                "                                                                                                                                                  └─Typ[:Entity otherPerson#6]──Q[600:all]:{7}, \n" +
                "                                                                                                                                                                                          └-> Rel(:relatedEntity othersActedIn#7)──Typ[:Entity m2#8], \n" +
                "                                                                                                                                                                                                                             └─?[..][700], \n" +
                "                                                                                                                                                                                                                                     └─?[701]:[category<eq,ACTED_IN>], \n" +
                "                                                                                                                                                  └─?[..][801], \n" +
                "                                                                                                                                                          └─?[801]:[name<eq,m2.name>]]";
        assertEquals(expected, print(query));

    }

    @Test
    public void testSimpleSelectMovieWithFilterTriplet() {
        String s = "SELECT ?title\n" +
                "    WHERE {?ninties a <Movie>; <released>\n" +
                "        ?released; <title> ?title.\n" +
                "        FILTER(?released > 1990 & ?released > 2000}\n";
        final AsgQuery query = sparQLTransformer.transform(new QueryInfo<>(s, "q", TYPE_SPARQL, YANGDB_ORG +"/foaf"));

        String expected = "[└── Start, \n" +
                "    ──Typ[:Entity person#1]──Q[100:all]:{2|4}, \n" +
                "                                         └-> Rel(:hasEvalue Rel_#2#2)──Typ[:Evalue personName#3]──Q[300:all]:{301}, \n" +
                "                                                                                                              └─?[..][301]──Typ[:Entity m1#5]──Q[800:all]:{6|801}, \n" +
                "                                                                                                                      └─?[301]:[stringValue<eq,Tom Hanks>], \n" +
                "                                         └-> Rel(:relatedEntity tomActedIn#4), \n" +
                "                                                                                                              └─?[..][400], \n" +
                "                                                                                                                      └─?[401]:[category<eq,ACTED_IN>], \n" +
                "                                                                                                                                                  └─Typ[:Entity otherPerson#6]──Q[600:all]:{7}, \n" +
                "                                                                                                                                                                                          └-> Rel(:relatedEntity othersActedIn#7)──Typ[:Entity m2#8], \n" +
                "                                                                                                                                                                                                                             └─?[..][700], \n" +
                "                                                                                                                                                                                                                                     └─?[701]:[category<eq,ACTED_IN>], \n" +
                "                                                                                                                                                  └─?[..][801], \n" +
                "                                                                                                                                                          └─?[801]:[name<eq,m2.name>]]";
        assertEquals(expected, print(query));

    }
    @Test
    public void testSimpleSelectMovieWithActorActedInFilterTriplet() {
        String s = "SELECT ?tom ?movies\n" +
                "    WHERE {?tom a <Person?;\n" +
                "        <name> \"Tom Hanks\";\n" +
                "        <ACTED_IN> ?movies}\n";
        final AsgQuery query = sparQLTransformer.transform(new QueryInfo<>(s, "q", TYPE_SPARQL, YANGDB_ORG +"/foaf"));

        String expected = "[└── Start, \n" +
                "    ──Typ[:Entity person#1]──Q[100:all]:{2|4}, \n" +
                "                                         └-> Rel(:hasEvalue Rel_#2#2)──Typ[:Evalue personName#3]──Q[300:all]:{301}, \n" +
                "                                                                                                              └─?[..][301]──Typ[:Entity m1#5]──Q[800:all]:{6|801}, \n" +
                "                                                                                                                      └─?[301]:[stringValue<eq,Tom Hanks>], \n" +
                "                                         └-> Rel(:relatedEntity tomActedIn#4), \n" +
                "                                                                                                              └─?[..][400], \n" +
                "                                                                                                                      └─?[401]:[category<eq,ACTED_IN>], \n" +
                "                                                                                                                                                  └─Typ[:Entity otherPerson#6]──Q[600:all]:{7}, \n" +
                "                                                                                                                                                                                          └-> Rel(:relatedEntity othersActedIn#7)──Typ[:Entity m2#8], \n" +
                "                                                                                                                                                                                                                             └─?[..][700], \n" +
                "                                                                                                                                                                                                                                     └─?[701]:[category<eq,ACTED_IN>], \n" +
                "                                                                                                                                                  └─?[..][801], \n" +
                "                                                                                                                                                          └─?[801]:[name<eq,m2.name>]]";
        assertEquals(expected, print(query));

    }
    @Test
    public void testSimpleSelectMovieWithCoAutorFilterTriplet() {
        String s = "SELECT ?name\n" +
                "    WHERE {?tom a <Person>;<ACTED_IN> ?m.\n" +
                "            ?co_actor <ACTED_IN> ?m; <name> ?name.\n" +
                "        FILTER(?tom != ?co_actor)}";
        final AsgQuery query = sparQLTransformer.transform(new QueryInfo<>(s, "q", TYPE_SPARQL, YANGDB_ORG +"/foaf"));

        String expected = "[└── Start, \n" +
                "    ──Typ[:Entity person#1]──Q[100:all]:{2|4}, \n" +
                "                                         └-> Rel(:hasEvalue Rel_#2#2)──Typ[:Evalue personName#3]──Q[300:all]:{301}, \n" +
                "                                                                                                              └─?[..][301]──Typ[:Entity m1#5]──Q[800:all]:{6|801}, \n" +
                "                                                                                                                      └─?[301]:[stringValue<eq,Tom Hanks>], \n" +
                "                                         └-> Rel(:relatedEntity tomActedIn#4), \n" +
                "                                                                                                              └─?[..][400], \n" +
                "                                                                                                                      └─?[401]:[category<eq,ACTED_IN>], \n" +
                "                                                                                                                                                  └─Typ[:Entity otherPerson#6]──Q[600:all]:{7}, \n" +
                "                                                                                                                                                                                          └-> Rel(:relatedEntity othersActedIn#7)──Typ[:Entity m2#8], \n" +
                "                                                                                                                                                                                                                             └─?[..][700], \n" +
                "                                                                                                                                                                                                                                     └─?[701]:[category<eq,ACTED_IN>], \n" +
                "                                                                                                                                                  └─?[..][801], \n" +
                "                                                                                                                                                          └─?[801]:[name<eq,m2.name>]]";
        assertEquals(expected, print(query));

    }
}