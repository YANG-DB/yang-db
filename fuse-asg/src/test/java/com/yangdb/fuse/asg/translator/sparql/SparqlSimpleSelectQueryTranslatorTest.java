
package com.yangdb.fuse.asg.translator.sparql;

import com.yangdb.fuse.dispatcher.ontology.SimpleOntologyProvider;
import com.yangdb.fuse.dispatcher.query.rdf.OWLToOntologyTransformer;
import com.yangdb.fuse.model.asgQuery.AsgQuery;
import com.yangdb.fuse.model.asgQuery.AsgQueryUtil;
import com.yangdb.fuse.model.ontology.Ontology;
import com.yangdb.fuse.model.ontology.OntologyNameSpace;
import com.yangdb.fuse.model.query.QueryInfo;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.semanticweb.owlapi.model.IRI;

import java.io.File;
import java.net.URL;
import java.nio.file.Files;
import java.util.Arrays;

import static com.yangdb.fuse.model.execution.plan.descriptors.AsgQueryDescriptor.print;
import static com.yangdb.fuse.model.transport.CreateQueryRequestMetadata.TYPE_SPARQL;
import static org.junit.Assert.assertEquals;

/**
 * Implemented the next sample queries
 * https://dzone.com/articles/sparql-and-cypher
 * https://www.w3.org/2009/Talks/0615-qbe/
 */
public class SparqlSimpleSelectQueryTranslatorTest {
    private static AsgSparQLTransformer sparQLTransformer;


    @BeforeClass
    public static void setUp() throws Exception {
        URL rdf_ns = Thread.currentThread().getContextClassLoader().getResource("sparql/rdf-namespace.rdf");
        URL personas = Thread.currentThread().getContextClassLoader().getResource("sparql/foaf.owl");
        URL dbpedia = Thread.currentThread().getContextClassLoader().getResource("sparql/dbpedia.owl");

        OWLToOntologyTransformer transformer = new OWLToOntologyTransformer();
        //load owl ontologies - the order of the ontologies is important in regards with the owl dependencies
        assert rdf_ns != null;
        assert personas != null;
        assert dbpedia != null;

        Ontology ontology = transformer.transform(IRI.create( OntologyNameSpace.defaultNameSpace +"foaf").toString(), Arrays.asList(
                new String(Files.readAllBytes(new File(rdf_ns.toURI()).toPath())),
                new String(Files.readAllBytes(new File(personas.toURI()).toPath())),
                new String(Files.readAllBytes(new File(dbpedia.toURI()).toPath()))));

        sparQLTransformer = new AsgSparQLTransformer(new SparqlTranslator(new SimpleOntologyProvider(ontology), new M1SparqlAsgStrategyRegistrar()));
        // transformer
        Assert.assertNotNull(ontology);
    }

    @Test
    /**
     * Projection
     *    ProjectionElemList
     *       ProjectionElem "x"
     *       ProjectionElem "y"
     *    ArbitraryLengthPath
     *       Var (name=x)
     *       Join
     *          StatementPattern
     *             Var (name=x)
     *             Var (name=_const_65a0fb1e_uri, value=http://xmlns.com/foaf/0.1/acted_in, anonymous)
     *             Var (name=_anon_8a454038_62b9_4e1e_a53b_5b2b8ebf090e, anonymous)
     *          StatementPattern
     *             Var (name=_anon_8a454038_62b9_4e1e_a53b_5b2b8ebf090e, anonymous)
     *             Var (name=_const_65a0fb1e_uri, value=http://xmlns.com/foaf/0.1/acted_in, anonymous)
     *             Var (name=y)
     *       Var (name=y)
     */

    /**
     * Projection
     *    ProjectionElemList
     *       ProjectionElem "x"
     *       ProjectionElem "y"
     *    ArbitraryLengthPath
     *       Var (name=x)
     *       Join
     *          StatementPattern
     *             Var (name=x)
     *             Var (name=_const_65a0fb1e_uri, value=http://xmlns.com/foaf/0.1/acted_in, anonymous)
     *             Var (name=_anon_46f01c96_c07f_4c42_a154_1e9873dac6db, anonymous)
     *          StatementPattern
     *             Var (name=y)
     *             Var (name=_const_65a0fb1e_uri, value=http://xmlns.com/foaf/0.1/acted_in, anonymous)
     *             Var (name=_anon_46f01c96_c07f_4c42_a154_1e9873dac6db, anonymous)
     *       Var (name=y)
     */
    @Ignore
    public void testSelectPatternWithDistanceTriplet() {
        String s1 = "PREFIX foaf:  <http://xmlns.com/foaf/0.1/>\n" +
                "SELECT ?x ?y\n" +
                "WHERE {\n" +
                "    ?x (foaf:acted_in/foaf:acted_in)* ?y .\n" +
                "}";
        final AsgQuery query1 = sparQLTransformer.transform(new QueryInfo<>(s1, "q", TYPE_SPARQL, OntologyNameSpace.defaultNameSpace + "foaf"));

        String expected1 = "[└── Start, \n" +
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
        assertEquals(expected1, print(query1));

        String s2 = "PREFIX foaf:  <http://xmlns.com/foaf/0.1/>\n" +
                "SELECT ?x ?y\n" +
                "WHERE {\n" +
                "    ?x (foaf:acted_in/^foaf:acted_in)* ?y .\n" +
                "}";
        final AsgQuery query2 = sparQLTransformer.transform(new QueryInfo<>(s2, "q", TYPE_SPARQL, OntologyNameSpace.defaultNameSpace + "foaf"));
        String expected2 = "[└── Start, \n" +
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
        assertEquals(expected2, print(query2));

    }

    @Test
    /**
     * Projection
     *    ProjectionElemList
     *       ProjectionElem "name"
     *    Join
     *       StatementPattern
     *          Var (name=person)
     *          Var (name=_const_23b7c3b6_uri, value=http://xmlns.com/foaf/0.1/name, anonymous)
     *          Var (name=name)
     *       StatementPattern
     *          Var (name=person)
     *          Var (name=_const_f5e5585a_uri, value=http://www.w3.org/1999/02/22-rdf-syntax-ns#type, anonymous)
     *          Var (name=_const_e1df31e0_uri, value=http://xmlns.com/foaf/0.1/Person, anonymous)
     */
    public void testSimpleSelectVarsAndPatternTriplet() {
        String s = "PREFIX foaf:  <http://xmlns.com/foaf/0.1/>\n" +
                "SELECT ?name\n" +
                "WHERE {\n" +
                "    ?person foaf:firstName ?name .\n" +
                "    ?person rdf:type foaf:Person .\n" +
                "}";
        final AsgQuery query = sparQLTransformer.transform(new QueryInfo<>(s, "q", TYPE_SPARQL, OntologyNameSpace.defaultNameSpace + "foaf"));

        String expected = "Projected fields:name\n" +
                "[└── Start, \n" +
                "    ──Typ[:http://www.w3.org/2002/07/owl#Thing person#1]──Q[2:all]:{4}, \n" +
                "                                                                  └─?[..][4], \n" +
                "                                                                        └─?[3]:[http://xmlns.com/foaf/0.1/firstName<IdentityProjection>], \n" +
                "                                                                        └─?[5]:[type<eq,http://xmlns.com/foaf/0.1/Person>]]";
        assertEquals(expected, print(query));

    }

    /**
     * Projection
     * ProjectionElemList
     * ProjectionElem "x1"
     * ProjectionElem "x2"
     * Filter
     * Compare (!=)
     * Var (name=x1)
     * Var (name=x2)
     * Join
     * Join
     * Join
     * Join
     * Join
     * StatementPattern
     * Var (name=x1)
     * Var (name=_const_da033af8_uri, value=http://xmlns.com/foaf/0.1/acts_in, anonymous)
     * Var (name=x3)
     * StatementPattern
     * Var (name=x1)
     * Var (name=_const_f5e5585a_uri, value=http://www.w3.org/1999/02/22-rdf-syntax-ns#type, anonymous)
     * Var (name=_const_e1df31e0_uri, value=http://xmlns.com/foaf/0.1/Person, anonymous)
     * StatementPattern
     * Var (name=x2)
     * Var (name=_const_da033af8_uri, value=http://xmlns.com/foaf/0.1/acts_in, anonymous)
     * Var (name=x3)
     * StatementPattern
     * Var (name=x2)
     * Var (name=_const_f5e5585a_uri, value=http://www.w3.org/1999/02/22-rdf-syntax-ns#type, anonymous)
     * Var (name=_const_e1df31e0_uri, value=http://xmlns.com/foaf/0.1/Person, anonymous)
     * StatementPattern
     * Var (name=x3)
     * Var (name=_const_5398fe8d_uri, value=http://xmlns.com/foaf/0.1/title, anonymous)
     * Var (name=_const_edbb420d_lit_e2eec718_0, value="Unforgiven", anonymous)
     * StatementPattern
     * Var (name=x3)
     * Var (name=_const_f5e5585a_uri, value=http://www.w3.org/1999/02/22-rdf-syntax-ns#type, anonymous)
     * Var (name=_const_51762b45_uri, value=http://xmlns.com/foaf/0.1/Movie, anonymous)
     */
    @Test
    @Ignore
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
        final AsgQuery query = sparQLTransformer.transform(new QueryInfo<>(s, "q", TYPE_SPARQL, OntologyNameSpace.defaultNameSpace + "foaf"));

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
                "    ?person foaf:name ?firstName .\n" +
                "    ?person foaf:mbox ?email .\n" +
                "}";
        final AsgQuery query = sparQLTransformer.transform(new QueryInfo<>(s, "q", TYPE_SPARQL, OntologyNameSpace.defaultNameSpace + "foaf"));
        AsgQueryUtil.replaceTagsStartingWith(query,"_anon_",eBase -> "_anon_" + eBase.geteNum());

        String expected = "Projected fields:person|firstName|email\n" +
                "[└── Start, \n" +
                "    ──Typ[:http://www.w3.org/2002/07/owl#Thing person#1]──Q[2:all]:{4|5}, \n" +
                "                                                                    └─?[..][4], \n" +
                "                                                                          └─?[3]:[http://xmlns.com/foaf/0.1/name<IdentityProjection>]──Typ[:http://www.w3.org/2002/07/owl#Thing email#6], \n" +
                "                                                                    └-> Rel(:http://xmlns.com/foaf/0.1/mbox _const_23b75369_uri#5)]";
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
        final AsgQuery query = sparQLTransformer.transform(new QueryInfo<>(s, "q", TYPE_SPARQL, OntologyNameSpace.defaultNameSpace + "foaf"));

        String expected = "Projected fields:homepage\n" +
                "[└── Start, \n" +
                "    ──Conc[:http://www.w3.org/2002/07/owl#Thing http://www.w3.org/People/Berners-Lee/card#i#1]──Q[2:all]:{3}, \n" +
                "                                                                                                        └-> Rel(:http://xmlns.com/foaf/0.1/knows _const_531c5f7d_uri#3)──Typ[:http://www.w3.org/2002/07/owl#Thing known#4]──Q[5:all]:{6}, \n" +
                "                                                                                                                                                                                                                                    └-> Rel(:http://xmlns.com/foaf/0.1/homepage _const_aba78b99_uri#6)──Typ[:http://www.w3.org/2002/07/owl#Thing homepage#7]]";
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
        String s = "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> \n" +
                "PREFIX type: <http://dbpedia.org/class/yago/>\n" +
                "PREFIX prop: <http://dbpedia.org/ontology/>\n" +
                "SELECT ?country_name ?population\n" +
                "WHERE {\n" +
                "    ?country a type:LandlockedCountries ;\n" +
                "             rdfs:name ?country_name ;\n" +
                "             prop:populationTotalRanking ?population .\n" +
                "    FILTER (?population > 15000000) .\n" +
                "}";
        final AsgQuery query = sparQLTransformer.transform(new QueryInfo<>(s, "q", TYPE_SPARQL, OntologyNameSpace.defaultNameSpace + "foaf"));

        AsgQueryUtil.replaceTagsStartingWith(query,"_anon_",eBase -> "_anon_" + eBase.geteNum());
        String expected = "Projected fields:country_name|population\n" +
                "[└── Start, \n" +
                "    ──Typ[:http://www.w3.org/2002/07/owl#Thing country#1]──Q[2:all]:{4}, \n" +
                "                                                                   └─?[..][4], \n" +
                "                                                                         └─?[3]:[type<eq,http://dbpedia.org/class/yago/LandlockedCountries>], \n" +
                "                                                                         └─?[5]:[http://xmlns.com/foaf/0.1/name<IdentityProjection>], \n" +
                "                                                                         └─?[5]:[http://dbpedia.org/ontology/populationTotalRanking<gt,15000000>]]";
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
    @Ignore
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
        final AsgQuery query = sparQLTransformer.transform(new QueryInfo<>(s, "q", TYPE_SPARQL, OntologyNameSpace.defaultNameSpace + "foaf"));

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
    @Ignore
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
        final AsgQuery query = sparQLTransformer.transform(new QueryInfo<>(s, "q", TYPE_SPARQL, OntologyNameSpace.defaultNameSpace + "foaf"));

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
     *       ProjectionElem "desc"
     *       ProjectionElem "date"
     *    Join
     *       Join
     *          Join
     *             StatementPattern
     *                Var (name=person)
     *                Var (name=_const_f5e5585a_uri, value=http://www.w3.org/1999/02/22-rdf-syntax-ns#type, anonymous)
     *                Var (name=_const_e1df31e0_uri, value=http://xmlns.com/foaf/0.1/Person, anonymous)
     *             StatementPattern
     *                Var (name=person)
     *                Var (name=_const_a437b411_uri, value=http://purl.org/dc/elements/1.1/description, anonymous)
     *                Var (name=desc)
     *          StatementPattern
     *             Var (name=person)
     *             Var (name=_const_dd5cdacf_uri, value=http://dbpedia.org/ontology/birthDate, anonymous)
     *             Var (name=date)
     *       Union (new scope)
     *          StatementPattern (new scope)
     *             Var (name=person)
     *             Var (name=_const_a437b411_uri, value=http://purl.org/dc/elements/1.1/description, anonymous)
     *             Var (name=_const_658d2cee_lit_9902a4bf_ca9, value="Novelist"@en, anonymous)
     *          StatementPattern (new scope)
     *             Var (name=person)
     *             Var (name=_const_a437b411_uri, value=http://purl.org/dc/elements/1.1/description, anonymous)
     *             Var (name=_const_75920dab_lit_9902a4bf_ca9, value="Author"@en, anonymous)
     */
    @Ignore
    public void testSimpleSelectVarsPatternWithUnionInPropertiesTriplet() {
        String s = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
                "PREFIX foaf: <http://xmlns.com/foaf/0.1/>\n" +
                "PREFIX onto: <http://dbpedia.org/ontology/>\n" +
                "\n" +
                "SELECT  ?person ?desc ?date\n" +
                "WHERE { ?person rdf:type foaf:Person .\n" +
                "      ?person onto:description ?desc .\n" +
                "      ?person onto:birthDate ?date .\n" +
                "\n" +
                "      { ?person onto:description \"Novelist\"@en . }\n" +
                "UNION\n" +
                "      { ?person onto:description \"Author\"@en . }\n" +
                "      } ";
        final AsgQuery query = sparQLTransformer.transform(new QueryInfo<>(s, "q", TYPE_SPARQL, OntologyNameSpace.defaultNameSpace + "foaf"));

        AsgQueryUtil.replaceTagsStartingWith(query,"_anon_",eBase -> "_anon_" + eBase.geteNum());
        String expected = "Projected fields:person|desc|date\n" +
                "[└── Start, \n" +
                "    ──Typ[:http://www.w3.org/2002/07/owl#Thing person#1]──Q[2:all]:{4|6}, \n" +
                "                                                                    └─?[..][4], \n" +
                "                                                                          └─?[3]:[type<eq,http://xmlns.com/foaf/0.1/Person>], \n" +
                "                                                                          └─?[5]:[http://dbpedia.org/ontology/description<IdentityProjection>], \n" +
                "                                                                          └─?[5]:[http://dbpedia.org/ontology/birthDate<IdentityProjection>], \n" +
                "                                                                    └─?[..][6], \n" +
                "                                                                          └─?[5]:[http://dbpedia.org/ontology/description<eq,Novelist>], \n" +
                "                                                                          └─?[7]:[http://dbpedia.org/ontology/description<eq,Author>]]";
        assertEquals(expected, print(query));

    }

    @Test
    /**
     * Projection
     *    ProjectionElemList
     *       ProjectionElem "person"
     *       ProjectionElem "desc"
     *       ProjectionElem "date"
     *       ProjectionElem "interests"
     *    Join
     *       Join
     *          Join
     *             Join
     *                Join
     *                   StatementPattern
     *                      Var (name=person)
     *                      Var (name=_const_f5e5585a_uri, value=http://www.w3.org/1999/02/22-rdf-syntax-ns#type, anonymous)
     *                      Var (name=_const_e1df31e0_uri, value=http://xmlns.com/foaf/0.1/Person, anonymous)
     *                   StatementPattern
     *                      Var (name=person)
     *                      Var (name=_const_517a953e_uri, value=http://dbpedia.org/ontology/description, anonymous)
     *                      Var (name=desc)
     *                StatementPattern
     *                   Var (name=person)
     *                   Var (name=_const_dd5cdacf_uri, value=http://dbpedia.org/ontology/birthDate, anonymous)
     *                   Var (name=date)
     *             StatementPattern
     *                Var (name=person)
     *                Var (name=_const_ea9562d5_uri, value=http://xmlns.com/foaf/0.1/interest, anonymous)
     *                Var (name=interests)
     *          StatementPattern
     *             Var (name=person)
     *             Var (name=_const_26291f2_uri, value=http://xmlns.com/foaf/0.1/publications, anonymous)
     *             Var (name=_const_c55e3bb9_uri, value=https://patents.google.com/patent/US9264505B2/en, anonymous)
     *       Union (new scope)
     *          StatementPattern (new scope)
     *             Var (name=person)
     *             Var (name=_const_531c5f7d_uri, value=http://xmlns.com/foaf/0.1/knows, anonymous)
     *             Var (name=_const_658d2cee_lit_9902a4bf_ca9, value="Novelist"@en, anonymous)
     *          StatementPattern (new scope)
     *             Var (name=person)
     *             Var (name=_const_531c5f7d_uri, value=http://xmlns.com/foaf/0.1/knows, anonymous)
     *             Var (name=_const_75920dab_lit_9902a4bf_ca9, value="Author"@en, anonymous)
     */
    @Ignore
    public void testSimpleSelectVarsPatternWithUnionInStepsTriplet() {
        String s = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
                "PREFIX foaf: <http://xmlns.com/foaf/0.1/>\n" +
                "PREFIX onto: <http://dbpedia.org/ontology/>\n" +
                "\n" +
                "SELECT  ?person ?desc ?date ?interests \n" +
                "WHERE {" +
                "      ?person rdf:type foaf:Person .\n" +
                "      ?person onto:description ?desc .\n" +
                "      ?person onto:birthDate ?date .\n" +
                "      ?person foaf:interest ?interests .\n" +
                "      ?person foaf:publications <https://patents.google.com/patent/US9264505B2/en> .\n" +
                "\n" +
                "      { ?person foaf:knows \"Novelist\"@en . }\n" +
                "UNION\n" +
                "      { ?person foaf:knows \"Author\"@en . }\n" +
                "      } ";
        final AsgQuery query = sparQLTransformer.transform(new QueryInfo<>(s, "q", TYPE_SPARQL, OntologyNameSpace.defaultNameSpace + "foaf"));
        AsgQueryUtil.replaceTagsStartingWith(query,"_anon_",eBase -> "_anon_" + eBase.geteNum());

        String expected = "Projected fields:person|desc|date\n" +
                "[└── Start, \n" +
                "    ──Typ[:http://www.w3.org/2002/07/owl#Thing person#1]──Q[2:all]:{4|6}, \n" +
                "                                                                    └─?[..][4], \n" +
                "                                                                          └─?[3]:[type<eq,http://xmlns.com/foaf/0.1/Person>], \n" +
                "                                                                          └─?[5]:[http://dbpedia.org/ontology/description<IdentityProjection>], \n" +
                "                                                                          └─?[5]:[http://dbpedia.org/ontology/birthDate<IdentityProjection>], \n" +
                "                                                                    └─?[..][6], \n" +
                "                                                                          └─?[5]:[http://dbpedia.org/ontology/description<eq,Novelist>], \n" +
                "                                                                          └─?[7]:[http://dbpedia.org/ontology/description<eq,Author>]]";
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
    @Ignore
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
        final AsgQuery query = sparQLTransformer.transform(new QueryInfo<>(s, "q", TYPE_SPARQL, OntologyNameSpace.defaultNameSpace + "foaf"));

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
    @Ignore
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
        final AsgQuery query = sparQLTransformer.transform(new QueryInfo<>(s, "q", TYPE_SPARQL, OntologyNameSpace.defaultNameSpace + "foaf"));

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
                "SELECT ?interest COUNT(*) AS ?count " +
                " WHERE \n" +
                "  {\n" +
                "    ?p foaf:interest <http://www.livejournal.com/interests.bml?int=harry+potter> .\n" +
                "    ?p foaf:interest ?interest\n" +
                "   }\n" +
                "GROUP BY ?interest ORDER BY DESC(COUNT(*)) LIMIT 10";
        final AsgQuery query = sparQLTransformer.transform(new QueryInfo<>(s, "q", TYPE_SPARQL, OntologyNameSpace.defaultNameSpace + "foaf"));

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
     *          ProjectionElem "uri"
     *          ProjectionElem "label"
     *       Order
     *          OrderElem (ASC)
     *             Var (name=label)
     *          Filter
     *             Or
     *                Compare (=)
     *                   Lang
     *                      Var (name=label)
     *                   ValueConstant (value="")
     *                Compare (=)
     *                   Lang
     *                      Var (name=label)
     *                   ValueConstant (value="en")
     *             Join
     *                Join
     *                   Join
     *                      StatementPattern
     *                         Var (name=domain)
     *                         Var (name=_const_f5e5585a_uri, value=http://www.w3.org/1999/02/22-rdf-syntax-ns#type, anonymous)
     *                         Var (name=_const_540a34f3_uri, value=http://dbpedia.org/ontology/Person, anonymous)
     *                      StatementPattern
     *                         Var (name=domain)
     *                         Var (name=_const_ceec5206_uri, value=http://dbpedia.org/ontology/birthPlace, anonymous)
     *                         Var (name=_anon_0b64a9cd_775a_4852_8502_eef4e7a49ad1, anonymous)
     *                   StatementPattern
     *                      Var (name=_anon_0b64a9cd_775a_4852_8502_eef4e7a49ad1, anonymous)
     *                      Var (name=_const_2b8b59d8_uri, value=http://dbpedia.org/ontology/country, anonymous)
     *                      Var (name=uri)
     *                StatementPattern
     *                   Var (name=uri)
     *                   Var (name=_const_9285ccfc_uri, value=http://www.w3.org/2000/01/rdf-schema#label, anonymous)
     *                   Var (name=label)
     */
    public void testFindCountries() {
        String s = "SELECT DISTINCT ?uri ?label \n" +
                " WHERE { \n" +
                "   ?domain a <http://dbpedia.org/ontology/Person> .\n" +
                "   ?domain <http://dbpedia.org/ontology/birthPlace>/<http://dbpedia.org/ontology/country> ?uri .\n" +
                "   ?uri <http://www.w3.org/2000/01/rdf-schema#label> ?label .\n" +
                "    FILTER(lang(?label) = '' || lang(?label) = 'en')\n" +
                "}\n" +
                "ORDER BY ?label\n";
        final AsgQuery query = sparQLTransformer.transform(new QueryInfo<>(s, "q", TYPE_SPARQL, OntologyNameSpace.defaultNameSpace + "foaf"));
        AsgQueryUtil.replaceTagsStartingWith(query,"_anon_",eBase -> "_anon_" + eBase.geteNum());

        String expected = "Projected fields:uri|label\n" +
                "[└── Start, \n" +
                "    ──Typ[:http://www.w3.org/2002/07/owl#Thing domain#1]──Q[2:all]:{4|5}, \n" +
                "                                                                    └─?[..][4], \n" +
                "                                                                          └─?[3]:[type<eq,http://dbpedia.org/ontology/Person>]──Typ[:http://www.w3.org/2002/07/owl#Thing _anon_6#6]──Q[7:all]:{8}, \n" +
                "                                                                    └-> Rel(:http://dbpedia.org/ontology/birthPlace _const_ceec5206_uri#5), \n" +
                "                                                                                                                                      └-> Rel(:http://dbpedia.org/ontology/country _const_2b8b59d8_uri#8)──Typ[:http://www.w3.org/2002/07/owl#Thing uri#9]──Q[10:all]:{12}, \n" +
                "                                                                                                                                                                                                                                                                      └─?[..][12], \n" +
                "                                                                                                                                                                                                                                                                             └─?[11]:[label<IdentityProjection>]]";
        assertEquals(expected, print(query));
    }

    @Test
    @Ignore
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
     *                                     Join
     *                                        Join
     *                                           Join
     *                                              StatementPattern
     *                                                 Var (name=this)
     *                                                 Var (name=_const_f5e5585a_uri, value=http://www.w3.org/1999/02/22-rdf-syntax-ns#type, anonymous)
     *                                                 Var (name=_const_2a0bf216_uri, value=http://dbpedia.org/ontology/Artwork, anonymous)
     *                                              StatementPattern
     *                                                 Var (name=this)
     *                                                 Var (name=_const_71eecf09_uri, value=http://dbpedia.org/ontology/author, anonymous)
     *                                                 Var (name=Person1)
     *                                           StatementPattern
     *                                              Var (name=Person1)
     *                                              Var (name=_const_f5e5585a_uri, value=http://www.w3.org/1999/02/22-rdf-syntax-ns#type, anonymous)
     *                                              Var (name=_const_540a34f3_uri, value=http://dbpedia.org/ontology/Person, anonymous)
     *                                        StatementPattern
     *                                           Var (name=this)
     *                                           Var (name=_const_f5e5585a_uri, value=http://www.w3.org/1999/02/22-rdf-syntax-ns#type, anonymous)
     *                                           Var (name=_const_540a34f3_uri, value=http://dbpedia.org/ontology/Person, anonymous)
     *                                     StatementPattern
     *                                        Var (name=this)
     *                                        Var (name=_const_ceec5206_uri, value=http://dbpedia.org/ontology/birthPlace, anonymous)
     *                                        Var (name=_anon_526913a7_e101_443e_952f_a5dddc2c7339, anonymous)
     *                                  StatementPattern
     *                                     Var (name=_anon_526913a7_e101_443e_952f_a5dddc2c7339, anonymous)
     *                                     Var (name=_const_2b8b59d8_uri, value=http://dbpedia.org/ontology/country, anonymous)
     *                                     Var (name=CountryNaN)
     *                               BindingSetAssignment ([[CountryNaN=http://fr.dbpedia.org/resource/Canada], [CountryNaN=http://fr.dbpedia.org/resource/Chine]])
     *                            StatementPattern
     *                               Var (name=Person1)
     *                               Var (name=_const_55de46ad_uri, value=http://dbpedia.org/ontology/movement, anonymous)
     *                               Var (name=_const_11b1aae4_uri, value=http://fr.dbpedia.org/resource/Baroque, anonymous)
     *                         StatementPattern
     *                            Var (name=this)
     *                            Var (name=_const_9285ccfc_uri, value=http://www.w3.org/2000/01/rdf-schema#label, anonymous)
     *                            Var (name=label)
     *                      StatementPattern
     *                         Var (name=this)
     *                         Var (name=_const_74b6d379_uri, value=http://xmlns.com/foaf/0.1/isPrimaryTopicOf, anonymous)
     *                         Var (name=wikipedia)
     */
    public void testFindArtworkAndPerson() {
        String s = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
                "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> \n" +
                "SELECT DISTINCT (STR(?label) AS ?nom) ?wikipedia ?this WHERE {\n" +
                "  ?this rdf:type <http://dbpedia.org/ontology/Artwork>;\n" +
                "    <http://dbpedia.org/ontology/author> ?Person1.\n" +
                "  ?Person1 rdf:type <http://dbpedia.org/ontology/Person>.\n" +
                "  ?this rdf:type <http://dbpedia.org/ontology/Person>;\n" +
                "    <http://dbpedia.org/ontology/birthPlace>/<http://dbpedia.org/ontology/country> ?CountryNaN.\n" +
                "  VALUES ?CountryNaN {\n" +
                "    <http://fr.dbpedia.org/resource/Canada>\n" +
                "    <http://fr.dbpedia.org/resource/Chine>\n" +
                "  }\n" +
                "  ?Person1 <http://dbpedia.org/ontology/movement> <http://fr.dbpedia.org/resource/Baroque>.\n" +
                "  ?this rdfs:label ?label FILTER(lang(?label) = 'fr') \n" +
                "  ?this <http://xmlns.com/foaf/0.1/isPrimaryTopicOf> ?wikipedia \n" +
                "}\n" +
                "ORDER BY ?label LIMIT 5000";
        final AsgQuery query = sparQLTransformer.transform(new QueryInfo<>(s, "q", TYPE_SPARQL, OntologyNameSpace.defaultNameSpace + "foaf"));

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
    @Ignore
    public void testSimpleSelectPersonWithSingleTriplet() {
        String s = "SELECT ?ee WHERE " +
                "{" +
                " ?ee a <Person>;<name> ?name. FILTER(?name = \"Emil\")" +
                "}";
        final AsgQuery query = sparQLTransformer.transform(new QueryInfo<>(s, "q", TYPE_SPARQL, OntologyNameSpace.defaultNameSpace + "foaf"));

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
     *       ProjectionElem "r"
     *    Order
     *       OrderElem (ASC)
     *          MathExpr (+)
     *             Var (name=_anon_794869e2_45ac_4223_93ec_e9fe6dbd2d99, anonymous)
     *             Var (name=_anon_b12143ab_647d_47bd_9764_1ea0dc4760a5, anonymous)
     *       Extension
     *          ExtensionElem (r)
     *             MathExpr (/)
     *                MathExpr (+)
     *                   Var (name=_anon_0574565c_ca7a_4372_afbe_5d7237ff1ef9, anonymous)
     *                   Var (name=_anon_5d259c83_1b37_4998_91dd_e4ab6717894b, anonymous)
     *                ValueConstant (value="2"^^<http://www.w3.org/2001/XMLSchema#integer>)
     *          Extension
     *             ExtensionElem (_anon_794869e2_45ac_4223_93ec_e9fe6dbd2d99)
     *                Count
     *                   Var (name=x)
     *             ExtensionElem (_anon_b12143ab_647d_47bd_9764_1ea0dc4760a5)
     *                Count
     *                   Var (name=y)
     *             Filter
     *                Compare (<)
     *                   MathExpr (+)
     *                      Var (name=_anon_cc8845eb_59bf_4ca5_8b69_6d7a944e22f9, anonymous)
     *                      Var (name=_anon_fab1c8fc_b72b_4fd1_9e07_33104b67b1a7, anonymous)
     *                   ValueConstant (value="5"^^<http://www.w3.org/2001/XMLSchema#integer>)
     *                Extension
     *                   ExtensionElem (_anon_cc8845eb_59bf_4ca5_8b69_6d7a944e22f9)
     *                      Sum
     *                         Var (name=x)
     *                   ExtensionElem (_anon_fab1c8fc_b72b_4fd1_9e07_33104b67b1a7)
     *                      Sum
     *                         Var (name=y)
     *                   Group (_anon_637c4631_017b_483c_95b2_407c2f29ee4c)
     *                      Extension
     *                         ExtensionElem (_anon_637c4631_017b_483c_95b2_407c2f29ee4c)
     *                            FunctionCall (http://www.w3.org/2005/xpath-functions#concat)
     *                               Var (name=n)
     *                               Var (name=id)
     *                         Join
     *                            Join
     *                               Join
     *                                  StatementPattern
     *                                     Var (name=this)
     *                                     Var (name=_const_aeffb8f2_uri, value=ex:name, anonymous)
     *                                     Var (name=n)
     *                                  StatementPattern
     *                                     Var (name=this)
     *                                     Var (name=_const_5c6b942_uri, value=ex:id, anonymous)
     *                                     Var (name=id)
     *                               StatementPattern
     *                                  Var (name=this)
     *                                  Var (name=_const_311b57a7_uri, value=ex:prop1, anonymous)
     *                                  Var (name=x)
     *                            StatementPattern
     *                               Var (name=this)
     *                               Var (name=_const_311b57a8_uri, value=ex:prop2, anonymous)
     *                               Var (name=y)
     *                      GroupElem
     *                         Sum
     *                            Var (name=x)
     *                      GroupElem
     *                         Sum
     *                            Var (name=y)
     *                      GroupElem
     *                         Count
     *                            Var (name=x)
     *                      GroupElem
     *                         Count
     *                            Var (name=y)
     *                      GroupElem
     *                         Min
     *                            MathExpr (+)
     *                               Var (name=x)
     *                               ValueConstant (value="+1"^^<http://www.w3.org/2001/XMLSchema#integer>)
     *                      GroupElem
     *                         Max
     *                            MathExpr (+)
     *                               Var (name=y)
     *                               ValueConstant (value="-1"^^<http://www.w3.org/2001/XMLSchema#integer>)
     */
    @Test
    @Ignore
    public void testSimpleSelectAggregations() {
        String s = "PREFIX ex: <ex:>\n"
                + "SELECT ((MIN(?x+1) + MAX(?y-1))/2 AS ?r) " +
                "{\n"
                + "	?this ex:name ?n . ?this ex:id ?id . ?this ex:prop1 ?x . ?this ex:prop2 ?y .\n"
                + "} " +
                "GROUP BY concat(?n, ?id) HAVING (SUM(?x) + SUM(?y) < 5) ORDER BY (COUNT(?x) + COUNT(?y))";

        final AsgQuery query = sparQLTransformer.transform(new QueryInfo<>(s, "q", TYPE_SPARQL, OntologyNameSpace.defaultNameSpace + "foaf"));

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
    @Ignore
    public void testSimpleSelectPersonWithFriendTriplet() {
        String s = "SELECT ?ee ?friends\n" +
                "    WHERE {?ee a <Person>;\n" +
                "        <name> \"Emil\";\n" +
                "        <knows> ?friends }\n";
        final AsgQuery query = sparQLTransformer.transform(new QueryInfo<>(s, "q", TYPE_SPARQL, OntologyNameSpace.defaultNameSpace + "foaf"));

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
        final AsgQuery query = sparQLTransformer.transform(new QueryInfo<>(s, "q", TYPE_SPARQL, OntologyNameSpace.defaultNameSpace + "foaf"));

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
        final AsgQuery query = sparQLTransformer.transform(new QueryInfo<>(s, "q", TYPE_SPARQL, OntologyNameSpace.defaultNameSpace + "foaf"));

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
        final AsgQuery query = sparQLTransformer.transform(new QueryInfo<>(s, "q", TYPE_SPARQL, OntologyNameSpace.defaultNameSpace + "foaf"));

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
    @Ignore
    public void testSimpleSelectMovieWithCoAutorFilterTriplet() {
        String s = "SELECT ?name\n" +
                "    WHERE {?tom a <Person>;<ACTED_IN> ?m.\n" +
                "            ?co_actor <ACTED_IN> ?m; <name> ?name.\n" +
                "        FILTER(?tom != ?co_actor)}";
        final AsgQuery query = sparQLTransformer.transform(new QueryInfo<>(s, "q", TYPE_SPARQL, OntologyNameSpace.defaultNameSpace + "foaf"));

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