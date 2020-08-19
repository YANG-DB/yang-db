
package com.yangdb.fuse.asg.translator.sparql;

import com.google.common.collect.Sets;
import com.yangdb.fuse.asg.AsgSparQLTransformer;
import com.yangdb.fuse.asg.strategy.M1SparqlAsgStrategyRegistrar;
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
    /**
     * Projection
     *    ProjectionElemList
     *       ProjectionElem "name"
     *    StatementPattern
     *       Var (name=person)
     *       Var (name=_const_23b7c3b6_uri, value=http://xmlns.com/foaf/0.1/name, anonymous)
     *       Var (name=name)
     */
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

    /**
     * Projection
     *    ProjectionElemList
     *       ProjectionElem "x1"
     *       ProjectionElem "x2"
     *    Filter
     *       Compare (!=)
     *          Var (name=x1)
     *          Var (name=x2)
     *       Join
     *          Join
     *             Join
     *                Join
     *                   Join
     *                      StatementPattern
     *                         Var (name=x1)
     *                         Var (name=_const_da033af8_uri, value=http://xmlns.com/foaf/0.1/acts_in, anonymous)
     *                         Var (name=x3)
     *                      StatementPattern
     *                         Var (name=x1)
     *                         Var (name=_const_f5e5585a_uri, value=http://www.w3.org/1999/02/22-rdf-syntax-ns#type, anonymous)
     *                         Var (name=_const_e1df31e0_uri, value=http://xmlns.com/foaf/0.1/Person, anonymous)
     *                   StatementPattern
     *                      Var (name=x2)
     *                      Var (name=_const_da033af8_uri, value=http://xmlns.com/foaf/0.1/acts_in, anonymous)
     *                      Var (name=x3)
     *                StatementPattern
     *                   Var (name=x2)
     *                   Var (name=_const_f5e5585a_uri, value=http://www.w3.org/1999/02/22-rdf-syntax-ns#type, anonymous)
     *                   Var (name=_const_e1df31e0_uri, value=http://xmlns.com/foaf/0.1/Person, anonymous)
     *             StatementPattern
     *                Var (name=x3)
     *                Var (name=_const_5398fe8d_uri, value=http://xmlns.com/foaf/0.1/title, anonymous)
     *                Var (name=_const_edbb420d_lit_e2eec718_0, value="Unforgiven", anonymous)
     *          StatementPattern
     *             Var (name=x3)
     *             Var (name=_const_f5e5585a_uri, value=http://www.w3.org/1999/02/22-rdf-syntax-ns#type, anonymous)
     *             Var (name=_const_51762b45_uri, value=http://xmlns.com/foaf/0.1/Movie, anonymous)
     */
    @Test
    public void testSimpleSelectAndProjectTriplet() {
        String s =
                "PREFIX foaf:  <http://xmlns.com/foaf/0.1/>\n" +
                "SELECT ?x1 ?x2\n" +
                "WHERE {\n" +
                "    ?x1 foaf:acts_in ?x3 . ?x1 rdf:type foaf:Person .\n" +
                "    ?x2 foaf:acts_in ?x3 . ?x2 rdf:type foaf:Person .\n" +
                "    ?x3 foaf:title \"Unforgiven\" . ?x3 rdf:type foaf:Movie .\n" +
                " FILTER (?x1 != ?x2 )" +
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
    /**
     * Projection
     *    ProjectionElemList
     *       ProjectionElem "person"
     *       ProjectionElem "name"
     *       ProjectionElem "email"
     *    Join
     *       StatementPattern
     *          Var (name=person)
     *          Var (name=_const_23b7c3b6_uri, value=http://xmlns.com/foaf/0.1/name, anonymous)
     *          Var (name=name)
     *       StatementPattern
     *          Var (name=person)
     *          Var (name=_const_23b75369_uri, value=http://xmlns.com/foaf/0.1/mbox, anonymous)
     *          Var (name=email)
     */
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
    /**
     * FROM <http://www.w3.org/People/Berners-Lee/card>
     * Projection
     *    ProjectionElemList
     *       ProjectionElem "homepage"
     *    Join
     *       StatementPattern
     *          Var (name=_const_7a8caa4b_uri, value=http://www.w3.org/People/Berners-Lee/card#i, anonymous)
     *          Var (name=_const_531c5f7d_uri, value=http://xmlns.com/foaf/0.1/knows, anonymous)
     *          Var (name=known)
     *       StatementPattern
     *          Var (name=known)
     *          Var (name=_const_aba78b99_uri, value=http://xmlns.com/foaf/0.1/homepage, anonymous)
     *          Var (name=homepage)
     */
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
    /**
     * Projection
     *    ProjectionElemList
     *       ProjectionElem "country_name"
     *       ProjectionElem "population"
     *    Filter
     *       Compare (>)
     *          Var (name=population)
     *          ValueConstant (value="15000000"^^<http://www.w3.org/2001/XMLSchema#integer>)
     *       Join
     *          Join
     *             StatementPattern
     *                Var (name=country)
     *                Var (name=_const_f5e5585a_uri, value=http://www.w3.org/1999/02/22-rdf-syntax-ns#type, anonymous)
     *                Var (name=_const_e0f732f9_uri, value=http://dbpedia.org/class/yago/LandlockedCountries, anonymous)
     *             StatementPattern
     *                Var (name=country)
     *                Var (name=_const_9285ccfc_uri, value=http://www.w3.org/2000/01/rdf-schema#label, anonymous)
     *                Var (name=country_name)
     *          StatementPattern
     *             Var (name=country)
     *             Var (name=_const_164421cd_uri, value=http://dbpedia.org/property/populationEstimate, anonymous)
     *             Var (name=population)
     */
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
    /**
     * Projection
     *    ProjectionElemList
     *       ProjectionElem "name"
     *       ProjectionElem "img"
     *       ProjectionElem "hp"
     *       ProjectionElem "loc"
     *    LeftJoin
     *       LeftJoin
     *          LeftJoin
     *             Join
     *                StatementPattern
     *                   Var (name=a)
     *                   Var (name=_const_f5e5585a_uri, value=http://www.w3.org/1999/02/22-rdf-syntax-ns#type, anonymous)
     *                   Var (name=_const_a74946f7_uri, value=http://purl.org/ontology/mo/MusicArtist, anonymous)
     *                StatementPattern
     *                   Var (name=a)
     *                   Var (name=_const_23b7c3b6_uri, value=http://xmlns.com/foaf/0.1/name, anonymous)
     *                   Var (name=name)
     *             StatementPattern
     *                Var (name=a)
     *                Var (name=_const_8d89fd38_uri, value=http://xmlns.com/foaf/0.1/img, anonymous)
     *                Var (name=img)
     *          StatementPattern
     *             Var (name=a)
     *             Var (name=_const_aba78b99_uri, value=http://xmlns.com/foaf/0.1/homepage, anonymous)
     *             Var (name=hp)
     *       StatementPattern
     *          Var (name=a)
     *          Var (name=_const_e530421f_uri, value=http://xmlns.com/foaf/0.1/based_near, anonymous)
     *          Var (name=loc)
     */
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

    /**
     * Slice (limit=5000)
     *    Distinct
     *       Projection
     *          ProjectionElemList
     *             ProjectionElem "nom"
     *             ProjectionElem "wikipedia"
     *             ProjectionElem "this"
     *          Order
     *             OrderElem (ASC)
     *                Var (name=label)
     *             Extension
     *                ExtensionElem (nom)
     *                   Str
     *                      Var (name=label)
     *                Filter
     *                   Compare (=)
     *                      Lang
     *                         Var (name=label)
     *                      ValueConstant (value="fr")
     *                   Join
     *                      Join
     *                         Join
     *                            Join
     *                               Join
     *                                  Join
     *                                     StatementPattern
     *                                        Var (name=this)
     *                                        Var (name=_const_f5e5585a_uri, value=http://www.w3.org/1999/02/22-rdf-syntax-ns#type, anonymous)
     *                                        Var (name=_const_2a0bf216_uri, value=http://dbpedia.org/ontology/Artwork, anonymous)
     *                                     StatementPattern
     *                                        Var (name=this)
     *                                        Var (name=_const_86687790_uri, value=http://dbpedia.org/ontology/museum, anonymous)
     *                                        Var (name=Museum1)
     *                                  StatementPattern
     *                                     Var (name=Museum1)
     *                                     Var (name=_const_f5e5585a_uri, value=http://www.w3.org/1999/02/22-rdf-syntax-ns#type, anonymous)
     *                                     Var (name=_const_4fcd63b0_uri, value=http://dbpedia.org/ontology/Museum, anonymous)
     *                               StatementPattern
     *                                  Var (name=this)
     *                                  Var (name=_const_71eecf09_uri, value=http://dbpedia.org/ontology/author, anonymous)
     *                                  Var (name=Person2)
     *                            StatementPattern
     *                               Var (name=Person2)
     *                               Var (name=_const_f5e5585a_uri, value=http://www.w3.org/1999/02/22-rdf-syntax-ns#type, anonymous)
     *                               Var (name=_const_540a34f3_uri, value=http://dbpedia.org/ontology/Person, anonymous)
     *                         StatementPattern
     *                            Var (name=this)
     *                            Var (name=_const_9285ccfc_uri, value=http://www.w3.org/2000/01/rdf-schema#label, anonymous)
     *                            Var (name=label)
     *                      StatementPattern
     *                         Var (name=this)
     *                         Var (name=_const_74b6d379_uri, value=http://xmlns.com/foaf/0.1/isPrimaryTopicOf, anonymous)
     *                         Var (name=wikipedia)
     */
    public void testSimpleSelectMuseumFilterTriplet() {
        String s = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
                "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> \n" +
                "SELECT DISTINCT (STR(?label) AS ?nom) ?wikipedia ?this WHERE {\n" +
                "  ?this rdf:type <http://dbpedia.org/ontology/Artwork>;\n" +
                "    <http://dbpedia.org/ontology/museum> ?Museum1.\n" +
                "  ?Museum1 rdf:type <http://dbpedia.org/ontology/Museum>.\n" +
                "  ?this <http://dbpedia.org/ontology/author> ?Person2.\n" +
                "  ?Person2 rdf:type <http://dbpedia.org/ontology/Person>.\n" +
                "  ?this rdfs:label ?label FILTER(lang(?label) = 'fr') \n" +
                "  ?this <http://xmlns.com/foaf/0.1/isPrimaryTopicOf> ?wikipedia \n" +
                "}\n" +
                "ORDER BY ?label LIMIT 5000";
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
    /**
     * Distinct
     *    Projection
     *       ProjectionElemList
     *          ProjectionElem "label"
     *          ProjectionElem "process"
     *       Join
     *          Union (new scope)
     *             StatementPattern (new scope)
     *                Var (name=process)
     *                Var (name=_const_40e93942_uri, value=http://www.obofoundry.org/ro/ro.owl#part_of, anonymous)
     *                Var (name=_const_28eb1147_uri, value=http://purl.org/obo/owl/GO#GO_0007165, anonymous)
     *             StatementPattern (new scope)
     *                Var (name=process)
     *                Var (name=_const_4592be07_uri, value=http://www.w3.org/2000/01/rdf-schema#subClassOf, anonymous)
     *                Var (name=_const_28eb1147_uri, value=http://purl.org/obo/owl/GO#GO_0007165, anonymous)
     *          StatementPattern
     *             Var (name=process)
     *             Var (name=_const_9285ccfc_uri, value=http://www.w3.org/2000/01/rdf-schema#label, anonymous)
     *             Var (name=label)
     */
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
    /**
     * Distinct
     *    Projection
     *       ProjectionElemList
     *          ProjectionElem "person"
     *       Filter
     *          And
     *             Compare (!=)
     *                Var (name=g1)
     *                Var (name=g2)
     *             And
     *                Compare (!=)
     *                   Var (name=g1)
     *                   Var (name=g3)
     *                Compare (!=)
     *                   Var (name=g2)
     *                   Var (name=g3)
     *          Join
     *             Join
     *                Join
     *                   StatementPattern
     *                      Var (name=person)
     *                      Var (name=_const_23b7c3b6_uri, value=http://xmlns.com/foaf/0.1/name, anonymous)
     *                      Var (name=name)
     *                   StatementPattern FROM NAMED CONTEXT
     *                      Var (name=person)
     *                      Var (name=_const_f5e5585a_uri, value=http://www.w3.org/1999/02/22-rdf-syntax-ns#type, anonymous)
     *                      Var (name=_const_e1df31e0_uri, value=http://xmlns.com/foaf/0.1/Person, anonymous)
     *                      Var (name=g1)
     *                StatementPattern FROM NAMED CONTEXT
     *                   Var (name=person)
     *                   Var (name=_const_f5e5585a_uri, value=http://www.w3.org/1999/02/22-rdf-syntax-ns#type, anonymous)
     *                   Var (name=_const_e1df31e0_uri, value=http://xmlns.com/foaf/0.1/Person, anonymous)
     *                   Var (name=g2)
     *             StatementPattern FROM NAMED CONTEXT
     *                Var (name=person)
     *                Var (name=_const_f5e5585a_uri, value=http://www.w3.org/1999/02/22-rdf-syntax-ns#type, anonymous)
     *                Var (name=_const_e1df31e0_uri, value=http://xmlns.com/foaf/0.1/Person, anonymous)
     *                Var (name=g3)
     */
    public void testQueryNamedGraphs() {
        String s =
                "PREFIX foaf: <http://xmlns.com/foaf/0.1/>\n" +
                "SELECT DISTINCT ?person\n" +
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
    /**
     * Projection
     *    ProjectionElemList
     *       ProjectionElem "lbl"
     *       ProjectionElem "est"
     *    Filter
     *       Compare (<)
     *          Var (name=est)
     *          ValueConstant (value="1920-01-01"^^<http://www.w3.org/2001/XMLSchema#date>)
     *       Filter
     *          FunctionCall (http://www.w3.org/2005/xpath-functions#contains)
     *             Var (name=lbl)
     *             ValueConstant (value="Republic")
     *          Join
     *             Join
     *                StatementPattern
     *                   Var (name=country)
     *                   Var (name=_const_9285ccfc_uri, value=http://www.w3.org/2000/01/rdf-schema#label, anonymous)
     *                   Var (name=lbl)
     *                StatementPattern
     *                   Var (name=country)
     *                   Var (name=_const_f5e5585a_uri, value=http://www.w3.org/1999/02/22-rdf-syntax-ns#type, anonymous)
     *                   Var (name=_const_e46827ee_uri, value=http://dbpedia.org/class/yago/Country108544813, anonymous)
     *             StatementPattern
     *                Var (name=country)
     *                Var (name=_const_40dab42e_uri, value=http://dbpedia.org/property/establishedDate, anonymous)
     *                Var (name=est)
     */
    public void testQueryFreeTextSearch() {
        String s = "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\n" +
                "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>        \n" +
                "PREFIX type: <http://dbpedia.org/class/yago/>\n" +
                "PREFIX prop: <http://dbpedia.org/property/>\n" +
                "SELECT ?lbl ?est\n" +
                "WHERE {\n" +
                "  ?country rdfs:label ?lbl .\n" +
                "  FILTER(contains(?lbl, \"Republic\")) .\n" +
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
    @Ignore
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
    /**
     * Projection
     *    ProjectionElemList
     *       ProjectionElem "ee"
     *    Filter
     *       Compare (=)
     *          Var (name=name)
     *          ValueConstant (value="Emil")
     *       Join
     *          StatementPattern
     *             Var (name=ee)
     *             Var (name=_const_f5e5585a_uri, value=http://www.w3.org/1999/02/22-rdf-syntax-ns#type, anonymous)
     *             Var (name=_const_c02d2b7d_uri, value=http://yangdb.org/Person, anonymous)
     *          StatementPattern
     *             Var (name=ee)
     *             Var (name=_const_4d818093_uri, value=http://yangdb.org/name, anonymous)
     *             Var (name=name)
     */
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
    /**
     * Projection
     *    ProjectionElemList
     *       ProjectionElem "ee"
     *       ProjectionElem "friends"
     *    Join
     *       Join
     *          StatementPattern
     *             Var (name=ee)
     *             Var (name=_const_f5e5585a_uri, value=http://www.w3.org/1999/02/22-rdf-syntax-ns#type, anonymous)
     *             Var (name=_const_c02d2b7d_uri, value=http://yangdb.org/Person, anonymous)
     *          StatementPattern
     *             Var (name=ee)
     *             Var (name=_const_4d818093_uri, value=http://yangdb.org/name, anonymous)
     *             Var (name=_const_2103eb_lit_e2eec718_0, value="Emil", anonymous)
     *       StatementPattern
     *          Var (name=ee)
     *          Var (name=_const_628a3e40_uri, value=http://yangdb.org/knows, anonymous)
     *          Var (name=friends)
     */
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
    @Ignore
    public void testSimpleSelectDistinctPersonWithRelatedOtherSideTriplet() {
        String s = "SELECT DISTINCT ?surfer\n" +
                "    WHERE { ?js a <Person>;\n" +
                "        <name> \"Jhone\";\n" +
                "        <knows>/<knows> ?surfer;\n" +
                "        ?surfer <hobby> \"surfer\". }";
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
    @Ignore
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
    @Ignore
    public void testSimpleSelectMovieWithActorActedInFilterTriplet() {
        String s = "SELECT ?tom ?movies\n" +
                "    WHERE {?tom a <Person>?;\n" +
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
    /**
     * Projection
     *    ProjectionElemList
     *       ProjectionElem "name"
     *    Filter
     *       Compare (!=)
     *          Var (name=tom)
     *          Var (name=co_actor)
     *       Join
     *          Join
     *             Join
     *                StatementPattern
     *                   Var (name=tom)
     *                   Var (name=_const_f5e5585a_uri, value=http://www.w3.org/1999/02/22-rdf-syntax-ns#type, anonymous)
     *                   Var (name=_const_c02d2b7d_uri, value=http://yangdb.org/Person, anonymous)
     *                StatementPattern
     *                   Var (name=tom)
     *                   Var (name=_const_16fd699b_uri, value=http://yangdb.org/ACTED_IN, anonymous)
     *                   Var (name=m)
     *             StatementPattern
     *                Var (name=co_actor)
     *                Var (name=_const_16fd699b_uri, value=http://yangdb.org/ACTED_IN, anonymous)
     *                Var (name=m)
     *          StatementPattern
     *             Var (name=co_actor)
     *             Var (name=_const_4d818093_uri, value=http://yangdb.org/name, anonymous)
     *             Var (name=name)
     */
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