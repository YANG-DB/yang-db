package com.yangdb.fuse.asg.translator.cypher;

import com.yangdb.fuse.asg.translator.AsgTranslator;
import com.yangdb.fuse.asg.translator.cypher.strategies.MatchCypherTranslatorStrategy;
import com.yangdb.fuse.model.asgQuery.AsgEBase;
import com.yangdb.fuse.model.asgQuery.AsgQuery;
import com.yangdb.fuse.model.query.Rel;
import com.yangdb.fuse.model.query.properties.RelProp;
import com.yangdb.fuse.model.query.quant.Quant1;
import com.yangdb.fuse.model.transport.CreateJsonQueryRequest;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.io.InputStream;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.Collections;

import static com.yangdb.fuse.model.asgQuery.AsgQuery.Builder.*;
import static com.yangdb.fuse.model.execution.plan.descriptors.AsgQueryDescriptor.print;
import static com.yangdb.fuse.model.query.properties.EProp.of;
import static com.yangdb.fuse.model.query.properties.constraint.Constraint.of;
import static com.yangdb.fuse.model.query.properties.constraint.ConstraintOp.*;
import static com.yangdb.fuse.model.query.quant.QuantType.all;
import static com.yangdb.fuse.model.query.quant.QuantType.some;
import static com.yangdb.fuse.model.transport.CreateQueryRequestMetadata.TYPE_CYPHER;
import static org.junit.Assert.assertEquals;

/**
 * Created by lior.perry
 */
public class CypherMatchGreaterThanEqualWithWhereOrOpLabelTranslatorTest {
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
    public void testMatch_A_where_A_OfType_OR_B_OfType_Return_() {
        AsgTranslator translator = new CypherTranslator(() -> Collections.singleton(match));
        String q = "MATCH (a)--(b) where a.age < 100 Or a.birth >= '28/01/2001' RETURN a";
        final AsgQuery query = translator.translate(new CreateJsonQueryRequest("q1","q1", TYPE_CYPHER, q,"test"));

        AsgQuery expected = AsgQuery.Builder
                .start("cypher_", "Dragons")
                .next(quant1(300, some))
                .in(
                        unTyped(4, "a")
                                .next(quant1(400, all)
                                        .addNext(
                                                rel(5, "*", Rel.Direction.RL, "Rel_#2")
                                                        .next(unTyped(6, "b"))
                                        )
                                        .addNext(ePropGroup(401, all,
                                                of(401, "age", of(lt, 100)))
                                        )
                                ),
                        unTyped(7, "a")
                                .addNext(
                                        quant1(700, all)
                                                .addNext(
                                                        rel(8, "*", Rel.Direction.RL, "Rel_#2")
                                                                .next(
                                                                        unTyped(9, "b")))
                                                .addNext(ePropGroup(701, all,
                                                        of(701, "birth", of(ge, "28/01/2001")))
                                                )
                                )
                )
                .build();
        assertEquals(print(expected), print(query));
    }

    @Test
    public void testMatch_A_where_A_OfType_And_OR_B_OfType_AND_Return_() {
        AsgTranslator translator = new CypherTranslator(() -> Collections.singleton(match));
        String q = "MATCH (a)-[c]-(b) where a.age < 100 Or b.birth >= '28/01/2001' Or c:Fire RETURN a";
        final AsgQuery query = translator.translate(new CreateJsonQueryRequest("q1","q1", TYPE_CYPHER, q,"test"));


        AsgQuery expected = AsgQuery.Builder
                .start("cypher_", "Dragons")
                .next(quant1(300, some))
                .in(
                        unTyped(4, "a")
                                .next(quant1(400, all)
                                        .addNext(
                                                rel(5, "*", Rel.Direction.RL, "c")
                                                        .next(unTyped(6, "b"))
                                        )
                                        .addNext(ePropGroup(401, all,
                                                of(401, "age", of(lt, 100)))
                                        )
                                ),
                        unTyped(7, "a")
                                .addNext(
                                        quant1(700, all)
                                                .addNext(
                                                        rel(8, "*", Rel.Direction.RL, "c")
                                                                .below(relPropGroup(800, all,
                                                                        RelProp.of(801, "type", of(inSet, Arrays.asList("Fire")))))
                                                                .next(unTyped(9, "b"))
                                                )
                                ),
                        unTyped(10, "a")
                                .addNext(
                                        quant1(1000, all)
                                                .addNext(
                                                        rel(11, "*", Rel.Direction.RL, "c")
                                                                .next(unTyped(12, "b")
                                                                        .next(quant1(1200, all)
                                                                                .addNext(ePropGroup(1201, all,
                                                                                        of(1201, "birth", of(ge, "28/01/2001")))
                                                                                ))
                                                                ))
                                )
                ).build();
        assertEquals(print(expected), print(query));
    }

    @Test
    public void testMatch_A_AND_where_A_OfType_And_OR_B_OfType_AND_Return_() {
        AsgTranslator translator = new CypherTranslator(() -> Collections.singleton(match));
        String q = "MATCH (a)-[c]-(b) where (a.age < 100 AND b.birth >= '28/01/2001' ) Or c:Fire RETURN a";
        final AsgQuery query = translator.translate(new CreateJsonQueryRequest("q1","q1", TYPE_CYPHER, q,"test"));

        String expected = "[└── Start, \n" +
                "    ──Q[300:some]:{4|7}, \n" +
                "                   └─UnTyp[:[] a#4]──Q[400:all]:{5}, \n" +
                "                                               └<--Rel(:* c#5)──UnTyp[:[] b#6]──Q[700:all]:{8|701}, \n" +
                "                                                          └─?[..][500], \n" +
                "                                                                  └─?[501]:[type<inSet,[Fire]>], \n" +
                "                   └─UnTyp[:[] a#7], \n" +
                "                               └<--Rel(:* c#8)──UnTyp[:[] b#9]──Q[900:all]:{901}, \n" +
                "                                                                            └─?[..][901], \n" +
                "                                                                                    └─?[901]:[birth<ge,28/01/2001>], \n" +
                "                               └─?[..][701], \n" +
                "                                       └─?[701]:[age<lt,100>]]";
        assertEquals(expected, print(query));
    }

    @Test
    public void testMatch_A_AND_where_A_OfType_And_OR_B_OfType_Return_() {
        AsgTranslator translator = new CypherTranslator(() -> Collections.singleton(match));
        String q = "MATCH (a)-[c]-(b) where (a.age >= 100 OR b.birth < '28/01/2001' ) And b:Person RETURN a";
        final AsgQuery query = translator.translate(new CreateJsonQueryRequest("q1","q1", TYPE_CYPHER, q,"test"));

        String expected = "[└── Start, \n" +
                "    ──Q[300:some]:{4|7}, \n" +
                "                   └─UnTyp[:[] a#4]──Q[400:all]:{5|401}, \n" +
                "                                                   └<--Rel(:* c#5)──UnTyp[:[] b#6]──Q[600:all]:{601}──Q[700:all]:{8}, \n" +
                "                                                                                                └─?[..][601], \n" +
                "                                                                                                        └─?[601]:[type<inSet,[Person]>], \n" +
                "                                                   └─?[..][401], \n" +
                "                                                           └─?[401]:[age<ge,100>], \n" +
                "                   └─UnTyp[:[] a#7], \n" +
                "                               └<--Rel(:* c#8)──UnTyp[:[] b#9]──Q[900:all]:{901}, \n" +
                "                                                                            └─?[..][901], \n" +
                "                                                                                    └─?[901]:[type<inSet,[Person]>], \n" +
                "                                                                                    └─?[902]:[birth<lt,28/01/2001>]]";
        assertEquals(expected, print(query));
    }

    @Test
    public void testMatch_A_AND_where_A_OfType_And_OR_B_OfType_AND_b_Return_() {
        AsgTranslator translator = new CypherTranslator(() -> Collections.singleton(match));
        String q = "MATCH (a)-[c]-(b) where ((a.age < 100 AND b.birth >= '28/01/2001' ) Or c:Fire) And b.size <= 300 RETURN a";
        final AsgQuery query = translator.translate(new CreateJsonQueryRequest("q1","q1", TYPE_CYPHER, q,"test"));


        AsgQuery expected = AsgQuery.Builder
                .start("cypher_", "Dragons")
                .next(quant1(300, some))
                .in(
                        unTyped(4, "a")
                                .addNext(
                                        quant1(400, all)
                                                .addNext(
                                                        rel(5, "*", Rel.Direction.RL, "c")
                                                                .next(
                                                                        unTyped(6, "b")
                                                                                .next(quant1(600, all)
                                                                                        .addNext(ePropGroup(601, all,
                                                                                                of(601, "birth", of(ge, "28/01/2001")),
                                                                                                of(602, "size", of(le, 300)))
                                                                                        ))
                                                                )
                                                )
                                                .addNext(ePropGroup(401, all,
                                                        of(401, "age",
                                                                of(lt, 100))
                                                        )
                                                )
                                ),
                        unTyped(7, "a")
                                .addNext(
                                        quant1(700, all)
                                                .addNext(
                                                        rel(8, "*", Rel.Direction.RL, "c")
                                                                .below(relPropGroup(800, all,
                                                                        RelProp.of(801, "type", of(inSet, Arrays.asList("Fire")))))
                                                                .next(
                                                                        unTyped(9, "b")
                                                                                .next(quant1(900, all)
                                                                                        .addNext(ePropGroup(901, all,
                                                                                                of(901, "size",
                                                                                                        of(le, 300)))))
                                                                )
                                                )
                                )
                ).build();
        assertEquals(print(expected), print(query));
    }

    @Test
    public void testMatch_A_AND_where_A_OfType_And_OR_B_OfType_AND_b_OR_c_Return_() {
        AsgTranslator translator = new CypherTranslator(() -> Collections.singleton(match));
        String q = "MATCH (a)-[c]-(b) where ((a.age < 100 AND a.birth >= '28/01/2001') Or c:Fire) And (b.age < 100 OR b.birth >= '28/01/2001') RETURN a";
        final AsgQuery query = translator.translate(new CreateJsonQueryRequest("q1","q1", TYPE_CYPHER, q,"test"));

        //expected string representation
        String expected = "[└── Start, \n" +
                "    ──Q[300:some]:{4|7|10|13}, \n" +
                "                         └─UnTyp[:[] a#4]──Q[400:all]:{5|401}, \n" +
                "                                                         └<--Rel(:* c#5)──UnTyp[:[] b#6]──Q[600:all]:{601}──Q[700:all]:{8}, \n" +
                "                                                                                                      └─?[..][601]──Q[1000:all]:{11|1001}, \n" +
                "                                                                                                              └─?[601]:[age<lt,100>]──Q[1300:all]:{14}, \n" +
                "                                                         └─?[..][401], \n" +
                "                                                                 └─?[401]:[age<lt,100>], \n" +
                "                                                                 └─?[402]:[birth<ge,28/01/2001>], \n" +
                "                         └─UnTyp[:[] a#7], \n" +
                "                                     └<--Rel(:* c#8)──UnTyp[:[] b#9]──Q[900:all]:{901}, \n" +
                "                                                └─?[..][800], \n" +
                "                                                        └─?[801]:[type<inSet,[Fire]>], \n" +
                "                                                                                 └─?[..][901], \n" +
                "                                                                                         └─?[901]:[birth<ge,28/01/2001>], \n" +
                "                         └─UnTyp[:[] a#10], \n" +
                "                                      └<--Rel(:* c#11)──UnTyp[:[] b#12]──Q[1200:all]:{1201}, \n" +
                "                                                                                       └─?[..][1201], \n" +
                "                                                                                                └─?[1201]:[birth<ge,28/01/2001>], \n" +
                "                                      └─?[..][1001], \n" +
                "                                               └─?[1001]:[age<lt,100>], \n" +
                "                                               └─?[1002]:[birth<ge,28/01/2001>], \n" +
                "                         └─UnTyp[:[] a#13], \n" +
                "                                      └<--Rel(:* c#14)──UnTyp[:[] b#15]──Q[1500:all]:{1501}, \n" +
                "                                                  └─?[..][1400], \n" +
                "                                                           └─?[1401]:[type<inSet,[Fire]>], \n" +
                "                                                                                     └─?[..][1501], \n" +
                "                                                                                              └─?[1501]:[age<lt,100>]]";
        assertEquals(expected, print(query));
    }

    @Test
    @Ignore
    //todo add Rule for same variable with multiple operators
    public void testMatch_A_OR_where_A_OfType_And_OR_B_OfType_AND_Return_() {
        AsgTranslator translator = new CypherTranslator(() -> Collections.singleton(match));
        String q = "MATCH (a)-[c]-(b) where (a:Dragon OR a:Hours) Or c:Fire RETURN a";
        final AsgQuery query = translator.translate(new CreateJsonQueryRequest("q1","q1", TYPE_CYPHER, q,"test"));

        AsgQuery expected = AsgQuery.Builder
                .start("cypher_", "Dragons")
                .next(quant1(300, some))
                .in(
                        unTyped(4, "a")
                                .addNext(
                                        quant1(400, all)
                                                .addNext(
                                                        rel(6, null, Rel.Direction.RL, "c")
                                                                .below(relPropGroup(600, all,
                                                                        RelProp.of(601, "type", of(inSet, Arrays.asList("Fire")))))
                                                                .next(
                                                                        unTyped(7, "b")))
                                ),
                        unTyped(8, "a")
                                .addNext(
                                        quant1(800, all)
                                                .addNext(
                                                        rel(10, null, Rel.Direction.RL, "c")
                                                                .next(
                                                                        unTyped(11, "b"))
                                                )
                                                .addNext(ePropGroup(801, some,
                                                        of(801, "type",
                                                                of(inSet, Arrays.asList("Hours"))),
                                                        of(802, "type",
                                                                of(inSet, Arrays.asList("Dragon"))))
                                                )
                                )
                ).build();
        assertEquals(print(expected), print(query));
    }

    @Test
    public void testMatch_A_where_A_OfType_testMatch_A_where_A_OfType_AND_C_Return_All() {
        AsgTranslator translator = new CypherTranslator(() -> Collections.singleton(match));
        String q = "MATCH (a)-[c]-(b) where (a.age < 100 AND b.birth >= '28/01/2001') Or (c.size > 50) RETURN *";
        final AsgQuery query = translator.translate(new CreateJsonQueryRequest("q1","q1", TYPE_CYPHER, q,"test"));


        //region Test Methods

        final AsgEBase<Quant1> quantA = quant1(100, all);
        quantA.addNext(rel(2, null, Rel.Direction.RL, "c")
                .below(relPropGroup(200, all,
                        new RelProp(201, "size", of(gt, 50), 0)))
                .addNext(unTyped(3, "b")
                        .next(quant1(300, all)
                                .addNext(
                                        ePropGroup(301, all,
                                                of(301, "birth", of(ge, "28/01/2001"))))
                        )

                ));
        quantA.addNext(
                ePropGroup(101, all,
                        of(101, "age", of(lt, 100))));

        String expected = "[└── Start, \n" +
                "    ──Q[300:some]:{4|7}, \n" +
                "                   └─UnTyp[:[] a#4]──Q[400:all]:{5|401}, \n" +
                "                                                   └<--Rel(:* c#5)──UnTyp[:[] b#6]──Q[600:all]:{601}──Q[700:all]:{8}, \n" +
                "                                                                                                └─?[..][601], \n" +
                "                                                                                                        └─?[601]:[birth<ge,28/01/2001>], \n" +
                "                                                   └─?[..][401], \n" +
                "                                                           └─?[401]:[age<lt,100>], \n" +
                "                   └─UnTyp[:[] a#7], \n" +
                "                               └<--Rel(:* c#8)──UnTyp[:[] b#9], \n" +
                "                                          └─?[..][800], \n" +
                "                                                  └─?[801]:[size<gt,50>]]";
        assertEquals(expected, print(query));
    }


    //region Fields
    private MatchCypherTranslatorStrategy match;
    //endregion

}