package com.yangdb.fuse.asg.translator.cypher;

import com.yangdb.fuse.asg.translator.AsgTranslator;
import com.yangdb.fuse.asg.translator.cypher.strategies.MatchCypherTranslatorStrategy;
import com.yangdb.fuse.model.asgQuery.AsgQuery;
import com.yangdb.fuse.model.query.QueryInfo;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;

import java.io.*;
import java.util.Collections;

import static com.yangdb.fuse.model.execution.plan.descriptors.AsgQueryDescriptor.print;
import static com.yangdb.fuse.model.query.properties.EProp.of;
import static com.yangdb.fuse.model.query.properties.constraint.Constraint.of;
import static com.yangdb.fuse.model.transport.CreateQueryRequestMetadata.TYPE_CYPHERQL;
import static org.junit.Assert.assertEquals;

/**
 * Created by lior.perry
 */
public class CypherMatchMultiHopsWithWhereTest {
    //region Setup
    @Before
    public void setUp() throws Exception {
        InputStream stream = Thread.currentThread().getContextClassLoader().getResourceAsStream("Dragons_Ontology.json");
        StringWriter writer = new StringWriter();
        IOUtils.copy(stream, writer);
        match = new CypherTestUtils().setUp(writer.toString()).match;
    }
    //endregion

    @Test
    public void testMatch_4_hops_with_contains_and() {
        AsgTranslator<QueryInfo<String>,AsgQuery> translator = new CypherTranslator(() -> Collections.singleton(match));
        String s = "MATCH (a)-[b]->(c)-[d]-(e)-[f]->(g)-[h]-(i) where (a.name CONTAINS 'jh') AND a:Horse RETURN a";
        final AsgQuery query = translator.translate(new QueryInfo<>(s,"q", TYPE_CYPHERQL, "ont"));

        String expected = "[└── Start, \n" +
                "    ──UnTyp[:[] a#1]──Q[100:all]:{2|101}, \n" +
                "                                    └-> Rel(:* b#2)──UnTyp[:[] c#3]──Q[300:all]:{4}, \n" +
                "                                                                               └<--Rel(:* d#4)──UnTyp[:[] e#5]──Q[500:all]:{6}, \n" +
                "                                                                                                                          └-> Rel(:* f#6)──UnTyp[:[] g#7]──Q[700:all]:{8}, \n" +
                "                                                                                                                                                                     └<--Rel(:* h#8)──UnTyp[:[] i#9], \n" +
                "                                    └─?[..][101], \n" +
                "                                            └─?[101]:[name<contains,jh>], \n" +
                "                                            └─?[102]:[type<inSet,[Horse]>]]";
        assertEquals(expected, print(query));
    }

    @Test
    public void testMatch_4_hops_with_contains_and_with_pattern() {
        AsgTranslator<QueryInfo<String>,AsgQuery> translator = new CypherTranslator(() -> Collections.singleton(match));
        String s = "MATCH (a {name: 'vlad'})-[b]->(c {size: 'large'})-[d]-(e {age: 100})-[f]->(g {weight: 250})-[h]-(i { height: 200}) where (a.name CONTAINS 'jh') AND a:Horse RETURN a";
        final AsgQuery query = translator.translate(new QueryInfo<>(s,"q", TYPE_CYPHERQL, "ont"));

        String expected = "[└── Start, \n" +
                "    ──UnTyp[:[] a#1]──Q[100:all]:{101|2}, \n" +
                "                                    └─?[..][101], \n" +
                "                                            └─?[101]:[name<eq,vlad>]──UnTyp[:[] c#3]──Q[300:all]:{301|4}, \n" +
                "                                            └─?[102]:[name<contains,jh>], \n" +
                "                                            └─?[102]:[type<inSet,[Horse]>], \n" +
                "                                    └-> Rel(:* b#2), \n" +
                "                                               └─?[..][301], \n" +
                "                                                       └─?[301]:[size<eq,large>]──UnTyp[:[] e#5]──Q[500:all]:{501|6}, \n" +
                "                                               └<--Rel(:* d#4), \n" +
                "                                                          └─?[..][501], \n" +
                "                                                                  └─?[501]:[age<eq,100>]──UnTyp[:[] g#7]──Q[700:all]:{701|8}, \n" +
                "                                                          └-> Rel(:* f#6), \n" +
                "                                                                     └─?[..][701], \n" +
                "                                                                             └─?[701]:[weight<eq,250>]──UnTyp[:[] i#9]──Q[900:all]:{901}, \n" +
                "                                                                     └<--Rel(:* h#8), \n" +
                "                                                                                └─?[..][901], \n" +
                "                                                                                        └─?[901]:[height<eq,200>]]";
        assertEquals(expected, print(query));
    }

    @Test
    public void testMatch_4_hops_with_contains_or() {
        AsgTranslator<QueryInfo<String>,AsgQuery> translator = new CypherTranslator(() -> Collections.singleton(match));
        String s = "MATCH (a)-[b]->(c)-[d]-(e)-[f]->(g)-[h]-(i) where (a.name CONTAINS 'jh') OR a:Horse RETURN a";
        final AsgQuery query = translator.translate(new QueryInfo<>(s,"q", TYPE_CYPHERQL, "ont"));

        String expected = "[└── Start, \n" +
                "    ──Q[900:some]:{10|19}, \n" +
                "                     └─UnTyp[:[] a#10]──Q[1000:all]:{11|1001}, \n" +
                "                                                         └-> Rel(:* b#11)──UnTyp[:[] c#12]──Q[1200:all]:{13}──Q[1900:all]:{20|1901}, \n" +
                "                                                                                                        └<--Rel(:* d#13)──UnTyp[:[] e#14]──Q[1400:all]:{15}, \n" +
                "                                                                                                                                                       └-> Rel(:* f#15)──UnTyp[:[] g#16]──Q[1600:all]:{17}, \n" +
                "                                                                                                                                                                                                      └<--Rel(:* h#17)──UnTyp[:[] i#18], \n" +
                "                                                         └─?[..][1001], \n" +
                "                                                                  └─?[1001]:[name<contains,jh>], \n" +
                "                     └─UnTyp[:[] a#19], \n" +
                "                                  └-> Rel(:* b#20)──UnTyp[:[] c#21]──Q[2100:all]:{22}, \n" +
                "                                                                                 └<--Rel(:* d#22)──UnTyp[:[] e#23]──Q[2300:all]:{24}, \n" +
                "                                                                                                                                └-> Rel(:* f#24)──UnTyp[:[] g#25]──Q[2500:all]:{26}, \n" +
                "                                                                                                                                                                               └<--Rel(:* h#26)──UnTyp[:[] i#27], \n" +
                "                                  └─?[..][1901], \n" +
                "                                           └─?[1901]:[type<inSet,[Horse]>]]";
        assertEquals(expected, print(query));
    }

    @Test
    public void testMatch_4_hops_with_contains_or_and() {
        AsgTranslator<QueryInfo<String>,AsgQuery> translator = new CypherTranslator(() -> Collections.singleton(match));
        String s = "MATCH (a)-[b]->(c)-[d]-(e)-[f]->(g)-[h]-(i) where (a.name CONTAINS 'jh' and c.age > 100) OR (e:Horse and h:Fire) RETURN a";
        final AsgQuery query = translator.translate(new QueryInfo<>(s,"q", TYPE_CYPHERQL, "ont"));

        String expected = "[└── Start, \n" +
                "    ──Q[900:some]:{10|19}, \n" +
                "                     └─UnTyp[:[] a#10]──Q[1000:all]:{11}, \n" +
                "                                                    └-> Rel(:* b#11)──UnTyp[:[] c#12]──Q[1200:all]:{13}──Q[1900:all]:{20|1901}, \n" +
                "                                                                                                   └<--Rel(:* d#13)──UnTyp[:[] e#14]──Q[1400:all]:{15|1401}, \n" +
                "                                                                                                                                                       └-> Rel(:* f#15)──UnTyp[:[] g#16]──Q[1600:all]:{17}, \n" +
                "                                                                                                                                                                                                      └<--Rel(:* h#17)──UnTyp[:[] i#18], \n" +
                "                                                                                                                                                                                                                  └─?[..][1700], \n" +
                "                                                                                                                                                                                                                           └─?[1701]:[type<inSet,[Fire]>], \n" +
                "                                                                                                                                                       └─?[..][1401], \n" +
                "                                                                                                                                                                └─?[1401]:[type<inSet,[Horse]>], \n" +
                "                     └─UnTyp[:[] a#19], \n" +
                "                                  └-> Rel(:* b#20)──UnTyp[:[] c#21]──Q[2100:all]:{22|2101}, \n" +
                "                                                                                      └<--Rel(:* d#22)──UnTyp[:[] e#23]──Q[2300:all]:{24}, \n" +
                "                                                                                                                                     └-> Rel(:* f#24)──UnTyp[:[] g#25]──Q[2500:all]:{26}, \n" +
                "                                                                                                                                                                                    └<--Rel(:* h#26)──UnTyp[:[] i#27], \n" +
                "                                                                                      └─?[..][2101], \n" +
                "                                                                                               └─?[2101]:[age<gt,100>], \n" +
                "                                  └─?[..][1901], \n" +
                "                                           └─?[1901]:[name<contains,jh>]]";
        assertEquals(expected, print(query));
    }
    //endregion

    //region Fields
    private MatchCypherTranslatorStrategy match;
    //endregion

}