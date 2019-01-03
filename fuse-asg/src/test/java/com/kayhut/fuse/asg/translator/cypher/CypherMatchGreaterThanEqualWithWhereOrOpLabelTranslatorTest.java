package com.kayhut.fuse.asg.translator.cypher;

import com.kayhut.fuse.asg.translator.AsgTranslator;
import com.kayhut.fuse.asg.translator.cypher.strategies.MatchCypherTranslatorStrategy;
import com.kayhut.fuse.model.asgQuery.AsgEBase;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.query.Rel;
import com.kayhut.fuse.model.query.properties.RelProp;
import com.kayhut.fuse.model.query.quant.Quant1;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;

import static com.kayhut.fuse.model.asgQuery.AsgQuery.Builder.*;
import static com.kayhut.fuse.model.execution.plan.descriptors.AsgQueryDescriptor.print;
import static com.kayhut.fuse.model.query.properties.EProp.of;
import static com.kayhut.fuse.model.query.properties.constraint.Constraint.of;
import static com.kayhut.fuse.model.query.properties.constraint.ConstraintOp.*;
import static com.kayhut.fuse.model.query.quant.QuantType.all;
import static com.kayhut.fuse.model.query.quant.QuantType.some;
import static org.junit.Assert.assertEquals;

/**
 * Created by lior.perry
 */
public class CypherMatchGreaterThanEqualWithWhereOrOpLabelTranslatorTest {
    //region Setup
    @Before
    public void setUp() throws Exception {
        match = new CypherTestUtils().setUp(readJsonToString("src/test/resources/Dragons_Ontology.json")).match;
    }
    //endregion

    @Test
    public void testMatch_A_where_A_OfType_OR_B_OfType_Return_() {
        AsgTranslator<String, AsgQuery> translator = new CypherTranslator("Dragons", () -> Collections.singleton(match));
        final AsgQuery query = translator.translate("MATCH (a)--(b) where a.age < 100 Or a.birth >= '28/01/2001' RETURN a");
        AsgQuery expected = AsgQuery.Builder
                .start("cypher_", "Dragons")
                .next(quant1(300, some))
                .in(
                        unTyped(4, "a")
                                .next(quant1(400, all)
                                        .addNext(
                                                rel(6, null, Rel.Direction.RL, "Rel_#2")
                                                        .next(unTyped(7, "b"))
                                        )
                                        .addNext(ePropGroup(401, all,
                                                of(401, "age", of(lt, 100)))
                                        )
                                ),
                        unTyped(8, "a")
                                .addNext(
                                        quant1(800, all)
                                                .addNext(
                                                        rel(10, null, Rel.Direction.RL, "Rel_#2")
                                                                .next(
                                                                        unTyped(11, "b")))
                                                .addNext(ePropGroup(801, all,
                                                        of(801, "birth", of(ge, "28/01/2001")))
                                                )
                                )
                )
                .build();
        assertEquals(print(expected), print(query));
    }

    @Test
    public void testMatch_A_where_A_OfType_And_OR_B_OfType_AND_Return_() {
        AsgTranslator<String, AsgQuery> translator = new CypherTranslator("Dragons", () -> Collections.singleton(match));
        final AsgQuery query = translator.translate("MATCH (a)-[c]-(b) where a.age < 100 Or b.birth >= '28/01/2001' Or c:Fire RETURN a");

        AsgQuery expected = AsgQuery.Builder
                .start("cypher_", "Dragons")
                .next(quant1(300, some))
                .in(
                        unTyped(4, "a")
                                .next(quant1(400, all)
                                        .addNext(
                                                rel(6, null, Rel.Direction.RL, "c")
                                                        .next(unTyped(7, "b"))
                                        )
                                        .addNext(ePropGroup(401, all,
                                                of(401, "age", of(lt, 100)))
                                        )
                                ),
                        unTyped(8, "a")
                                .addNext(
                                        quant1(800, all)
                                                .addNext(
                                                        rel(10, null, Rel.Direction.RL, "c")
                                                                .below(relPropGroup(1000, all,
                                                                        RelProp.of(1001, "type", of(inSet, Arrays.asList("Fire")))))
                                                                .next(unTyped(11, "b"))
                                                )
                                ),
                        unTyped(12, "a")
                                .addNext(
                                        quant1(1200, all)
                                                .addNext(
                                                        rel(14, null, Rel.Direction.RL, "c")
                                                                .next(unTyped(15, "b")
                                                                        .next(quant1(1500, all)
                                                                                .addNext(ePropGroup(1501, all,
                                                                                        of(1501, "birth", of(ge, "28/01/2001")))
                                                                                ))
                                                                ))
                                )
                ).build();
        assertEquals(print(expected), print(query));
    }

    @Test
    public void testMatch_A_AND_where_A_OfType_And_OR_B_OfType_AND_Return_() {
        AsgTranslator<String, AsgQuery> translator = new CypherTranslator("Dragons", () -> Collections.singleton(match));
        final AsgQuery query = translator.translate("MATCH (a)-[c]-(b) where (a.age < 100 AND b.birth >= '28/01/2001' ) Or c:Fire RETURN a");
        String expected = "[└── Start, \n" +
                "    ──Q[300:some]:{4|8}, \n" +
                "                   └─UnTyp[:[] a#4]──Q[400:all]:{6}, \n" +
                "                                               └<--Rel(:null c#6)──UnTyp[:[] b#7]──Q[800:all]:{10|801}, \n" +
                "                                                             └─?[..][600], \n" +
                "                                                                     └─?[601]:[type<inSet,[Fire]>], \n" +
                "                   └─UnTyp[:[] a#8], \n" +
                "                               └<--Rel(:null c#10)──UnTyp[:[] b#11]──Q[1100:all]:{1101}, \n" +
                "                                                                                   └─?[..][1101], \n" +
                "                                                                                            └─?[1101]:[birth<ge,28/01/2001>], \n" +
                "                               └─?[..][801], \n" +
                "                                       └─?[801]:[age<lt,100>]]";
        assertEquals(expected, print(query));
    }

    @Test
    public void testMatch_A_AND_where_A_OfType_And_OR_B_OfType_Return_() {
        AsgTranslator<String, AsgQuery> translator = new CypherTranslator("Dragons", () -> Collections.singleton(match));
        final AsgQuery query = translator.translate("MATCH (a)-[c]-(b) where (a.age >= 100 OR b.birth < '28/01/2001' ) And b:Person RETURN a");
        String expected = "[└── Start, \n" +
                "    ──Q[300:some]:{4|8}, \n" +
                "                   └─UnTyp[:[] a#4]──Q[400:all]:{6|401}, \n" +
                "                                                   └<--Rel(:null c#6)──UnTyp[:[] b#7]──Q[700:all]:{701}──Q[800:all]:{10}, \n" +
                "                                                                                                   └─?[..][701], \n" +
                "                                                                                                           └─?[701]:[type<inSet,[Person]>], \n" +
                "                                                   └─?[..][401], \n" +
                "                                                           └─?[401]:[age<ge,100>], \n" +
                "                   └─UnTyp[:[] a#8], \n" +
                "                               └<--Rel(:null c#10)──UnTyp[:[] b#11]──Q[1100:all]:{1101}, \n" +
                "                                                                                   └─?[..][1101], \n" +
                "                                                                                            └─?[1101]:[type<inSet,[Person]>], \n" +
                "                                                                                            └─?[1102]:[birth<lt,28/01/2001>]]";
        assertEquals(expected, print(query));
    }

    @Test
    public void testMatch_A_AND_where_A_OfType_And_OR_B_OfType_AND_b_Return_() {
        AsgTranslator<String, AsgQuery> translator = new CypherTranslator("Dragons", () -> Collections.singleton(match));
        final AsgQuery query = translator.translate("MATCH (a)-[c]-(b) where ((a.age < 100 AND b.birth >= '28/01/2001' ) Or c:Fire) And b.size <= 300 RETURN a");

        AsgQuery expected = AsgQuery.Builder
                .start("cypher_", "Dragons")
                .next(quant1(300, some))
                .in(
                        unTyped(4, "a")
                                .addNext(
                                        quant1(400, all)
                                                .addNext(
                                                        rel(6, null, Rel.Direction.RL, "c")
                                                                .next(
                                                                        unTyped(7, "b")
                                                                                .next(quant1(700, all)
                                                                                        .addNext(ePropGroup(701, all,
                                                                                                of(701, "birth",of(ge, "28/01/2001")),
                                                                                                of(702, "size",of(le, 300)))
                                                                                        ))
                                                                )
                                                )
                                                .addNext(ePropGroup(401, all,
                                                        of(401, "age",
                                                                of(lt, 100))
                                                        )
                                                )
                                ),
                        unTyped(8, "a")
                                .addNext(
                                        quant1(800, all)
                                                .addNext(
                                                        rel(10, null, Rel.Direction.RL, "c")
                                                                .below(relPropGroup(1000, all,
                                                                        RelProp.of(1001, "type", of(inSet, Arrays.asList("Fire")))))
                                                                .next(
                                                                        unTyped(11, "b")
                                                                                .next(quant1(1100, all)
                                                                                        .addNext(ePropGroup(1101, all,
                                                                                                of(1101, "size",
                                                                                                        of(le, 300)))))
                                                                )
                                                )
                                )
                ).build();
        assertEquals(print(expected), print(query));
    }

    @Test
    public void testMatch_A_AND_where_A_OfType_And_OR_B_OfType_AND_b_OR_c_Return_() {
        AsgTranslator<String, AsgQuery> translator = new CypherTranslator("Dragons", () -> Collections.singleton(match));
        final AsgQuery query = translator.translate("MATCH (a)-[c]-(b) where ((a.age < 100 AND a.birth >= '28/01/2001') Or c:Fire) And (b.age < 100 OR b.birth >= '28/01/2001') RETURN a");
        //expected string representation
        String expected = "[└── Start, \n" +
                "    ──Q[300:some]:{4|8|12|16}, \n" +
                "                         └─UnTyp[:[] a#4]──Q[400:all]:{6|401}, \n" +
                "                                                         └<--Rel(:null c#6)──UnTyp[:[] b#7]──Q[700:all]:{701}──Q[800:all]:{10}, \n" +
                "                                                                                                         └─?[..][701]──Q[1200:all]:{14|1201}, \n" +
                "                                                                                                                 └─?[701]:[age<lt,100>]──Q[1600:all]:{18}, \n" +
                "                                                         └─?[..][401], \n" +
                "                                                                 └─?[401]:[age<lt,100>], \n" +
                "                                                                 └─?[402]:[birth<ge,28/01/2001>], \n" +
                "                         └─UnTyp[:[] a#8], \n" +
                "                                     └<--Rel(:null c#10)──UnTyp[:[] b#11]──Q[1100:all]:{1101}, \n" +
                "                                                    └─?[..][1000], \n" +
                "                                                             └─?[1001]:[type<inSet,[Fire]>], \n" +
                "                                                                                       └─?[..][1101], \n" +
                "                                                                                                └─?[1101]:[birth<ge,28/01/2001>], \n" +
                "                         └─UnTyp[:[] a#12], \n" +
                "                                      └<--Rel(:null c#14)──UnTyp[:[] b#15]──Q[1500:all]:{1501}, \n" +
                "                                                                                          └─?[..][1501], \n" +
                "                                                                                                   └─?[1501]:[birth<ge,28/01/2001>], \n" +
                "                                      └─?[..][1201], \n" +
                "                                               └─?[1201]:[age<lt,100>], \n" +
                "                                               └─?[1202]:[birth<ge,28/01/2001>], \n" +
                "                         └─UnTyp[:[] a#16], \n" +
                "                                      └<--Rel(:null c#18)──UnTyp[:[] b#19]──Q[1900:all]:{1901}, \n" +
                "                                                     └─?[..][1800], \n" +
                "                                                              └─?[1801]:[type<inSet,[Fire]>], \n" +
                "                                                                                        └─?[..][1901], \n" +
                "                                                                                                 └─?[1901]:[age<lt,100>]]";
        assertEquals(expected, print(query));
    }

    @Test
    @Ignore
    //todo add Rule for same variable with multiple operators
    public void testMatch_A_OR_where_A_OfType_And_OR_B_OfType_AND_Return_() {
        AsgTranslator<String, AsgQuery> translator = new CypherTranslator("Dragons", () -> Collections.singleton(match));
        final AsgQuery query = translator.translate("MATCH (a)-[c]-(b) where (a:Dragon OR a:Hours) Or c:Fire RETURN a");

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
        AsgTranslator<String, AsgQuery> translator = new CypherTranslator("Dragons", () -> Collections.singleton(match));
        final AsgQuery query = translator.translate("MATCH (a)-[c]-(b) where (a.age < 100 AND b.birth >= '28/01/2001') Or (c.size > 50) RETURN *");

        //region Test Methods

        final AsgEBase<Quant1> quantA = quant1(100, all);
        quantA.addNext(rel(2, null, Rel.Direction.RL,"c")
                .below(relPropGroup(200,all,
                        new RelProp(201,"size",of(gt, 50),0)))
                .addNext(unTyped(3, "b")
                        .next(quant1(300, all)
                                .addNext(
                                        ePropGroup(301,all,
                                                of(301, "birth", of(ge, "28/01/2001"))))
                        )

                ));
        quantA.addNext(
                ePropGroup(101,all,
                        of(101, "age", of(lt, 100))));

        String expected = "[└── Start, \n" +
                            "    ──Q[300:some]:{4|8}, \n" +
                            "                   └─UnTyp[:[] a#4]──Q[400:all]:{6|401}, \n" +
                            "                                                   └<--Rel(:null c#6)──UnTyp[:[] b#7]──Q[700:all]:{701}──Q[800:all]:{10}, \n" +
                            "                                                                                                   └─?[..][701], \n" +
                            "                                                                                                           └─?[701]:[birth<ge,28/01/2001>], \n" +
                            "                                                   └─?[..][401], \n" +
                            "                                                           └─?[401]:[age<lt,100>], \n" +
                            "                   └─UnTyp[:[] a#8], \n" +
                            "                               └<--Rel(:null c#10)──UnTyp[:[] b#11], \n" +
                            "                                              └─?[..][1000], \n" +
                            "                                                       └─?[1001]:[size<gt,50>]]";
        assertEquals(expected, print(query));
    }

    //region Private Methods
    private static String readJsonToString(String jsonRelativePath) {
        String contents = "";
        try {
            contents = new String(Files.readAllBytes(Paths.get(jsonRelativePath)));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return contents;
    }
    //endregion

    //region Fields
    private MatchCypherTranslatorStrategy match;
    //endregion

}