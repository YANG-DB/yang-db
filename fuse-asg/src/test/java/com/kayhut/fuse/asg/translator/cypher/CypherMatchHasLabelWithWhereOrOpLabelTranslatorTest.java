package com.kayhut.fuse.asg.translator.cypher;

import com.kayhut.fuse.asg.translator.AsgTranslator;
import com.kayhut.fuse.asg.translator.cypher.strategies.MatchCypherTranslatorStrategy;
import com.kayhut.fuse.model.asgQuery.AsgEBase;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.execution.plan.descriptors.AsgQueryDescriptor;
import com.kayhut.fuse.model.query.Rel;
import com.kayhut.fuse.model.query.properties.RelProp;
import com.kayhut.fuse.model.query.properties.RelPropGroup;
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
import static com.kayhut.fuse.model.execution.plan.descriptors.AsgQueryDescriptor.*;
import static com.kayhut.fuse.model.query.properties.EProp.of;
import static com.kayhut.fuse.model.query.properties.constraint.Constraint.of;
import static com.kayhut.fuse.model.query.properties.constraint.ConstraintOp.eq;
import static com.kayhut.fuse.model.query.properties.constraint.ConstraintOp.inSet;
import static com.kayhut.fuse.model.query.properties.constraint.ConstraintOp.ne;
import static com.kayhut.fuse.model.query.quant.QuantType.all;
import static com.kayhut.fuse.model.query.quant.QuantType.some;
import static org.junit.Assert.assertEquals;

/**
 * Created by lior.perry
 */
public class CypherMatchHasLabelWithWhereOrOpLabelTranslatorTest {
    //region Setup
    @Before
    public void setUp() throws Exception {
        match = new CypherTestUtils().setUp(readJsonToString("src/test/resources/Dragons_Ontology.json")).match;
    }
    //endregion

    @Test
    public void testMatch_A_where_A_OfType_OR_A_OfType_Return_A_with_containd() {
        AsgTranslator<String, AsgQuery> translator = new CypherTranslator("Dragons", Collections.singleton(match));
        final AsgQuery query = translator.translate("MATCH (a) where (a.name CONTAINS 'jh') OR a:Horse RETURN a");
        String expected = "[└── Start, \n" +
                            "    ──Q[100:some]:{2|3}, \n" +
                            "                   └─UnTyp[:[] a#2]──Q[200:all]:{201}, \n" +
                            "                                                 └─?[..][201]──Q[300:all]:{301}, \n" +
                            "                                                         └─?[201]:[name<contains,jh>], \n" +
                            "                   └─UnTyp[:[] a#3], \n" +
                            "                               └─?[..][301], \n" +
                            "                                       └─?[301]:[type<inSet,[Horse]>]]";
        assertEquals(expected, print(query));
    }
    @Test
    public void testMatch_A_where_A_OfType_OR_A_OfType_Return_A_with_startsWith() {
        AsgTranslator<String, AsgQuery> translator = new CypherTranslator("Dragons", Collections.singleton(match));
        final AsgQuery query = translator.translate("MATCH (a) where (a.name STARTS WITH 'jh*') OR a:Horse RETURN a");
        String expected = "[└── Start, \n" +
                "    ──Q[100:some]:{2|3}, \n" +
                "                   └─UnTyp[:[] a#2]──Q[200:all]:{201}, \n" +
                "                                                 └─?[..][201]──Q[300:all]:{301}, \n" +
                "                                                         └─?[201]:[type<inSet,[Horse]>], \n" +
                "                   └─UnTyp[:[] a#3], \n" +
                "                               └─?[..][301], \n" +
                "                                       └─?[301]:[name<startsWith,jh*>]]";
        assertEquals(expected, print(query));
    }
    @Test
    public void testMatch_A_where_A_OfType_OR_A_OfType_Return_A_with_endsWith() {
        AsgTranslator<String, AsgQuery> translator = new CypherTranslator("Dragons", Collections.singleton(match));
        final AsgQuery query = translator.translate("MATCH (a) where (a.name ENDS WITH 'jh*') OR a:Horse RETURN a");
        String expected = "[└── Start, \n" +
                "    ──Q[100:some]:{2|3}, \n" +
                "                   └─UnTyp[:[] a#2]──Q[200:all]:{201}, \n" +
                "                                                 └─?[..][201]──Q[300:all]:{301}, \n" +
                "                                                         └─?[201]:[type<inSet,[Horse]>], \n" +
                "                   └─UnTyp[:[] a#3], \n" +
                "                               └─?[..][301], \n" +
                "                                       └─?[301]:[name<endsWith,jh*>]]";
        assertEquals(expected, print(query));
    }

    @Test
    public void testMatch_A_where_A_OfType_OR_A_OfType_Return_A_with_in() {
        AsgTranslator<String, AsgQuery> translator = new CypherTranslator("Dragons", Collections.singleton(match));
        final AsgQuery query = translator.translate("MATCH (a) where a.name in ['jhone','jane'] OR a:Horse RETURN a");
        String expected = "[└── Start, \n" +
                            "    ──Q[100:some]:{2|3}, \n" +
                            "                   └─UnTyp[:[] a#2]──Q[200:all]:{201}, \n" +
                            "                                                 └─?[..][201]──Q[300:all]:{301}, \n" +
                            "                                                         └─?[201]:[type<inSet,[Horse]>], \n" +
                            "                   └─UnTyp[:[] a#3], \n" +
                            "                               └─?[..][301], \n" +
                            "                                       └─?[301]:[name<inSet,[jhone, jane]>]]";
        assertEquals(expected, print(query));
    }

    @Test
    public void testMatch_A_where_A_OfType_OR_A_OfType_Return_A_with_equal() {
        AsgTranslator<String, AsgQuery> translator = new CypherTranslator("Dragons", Collections.singleton(match));
        final AsgQuery query = translator.translate("MATCH (a) where a.name = 'jhone' Or a:Horse RETURN a");
        String expected = "[└── Start, \n" +
                        "    ──Q[100:some]:{2|3}, \n" +
                        "                   └─UnTyp[:[] a#2]──Q[200:all]:{201}, \n" +
                        "                                                 └─?[..][201]──Q[300:all]:{301}, \n" +
                        "                                                         └─?[201]:[type<inSet,[Horse]>], \n" +
                        "                   └─UnTyp[:[] a#3], \n" +
                        "                               └─?[..][301], \n" +
                        "                                       └─?[301]:[name<eq,jhone>]]";
        assertEquals(expected, print(query));
    }

    @Test
    public void testMatch_A_where_A_OfType_OR_A_OfType_Return_A_with_notEqual() {
        AsgTranslator<String, AsgQuery> translator = new CypherTranslator("Dragons", Collections.singleton(match));
        final AsgQuery query = translator.translate("MATCH (a) where a.name <> 'jhone' Or a:Horse RETURN a");
        String expected = "[└── Start, \n" +
                            "    ──Q[100:some]:{2|3}, \n" +
                            "                   └─UnTyp[:[] a#2]──Q[200:all]:{201}, \n" +
                            "                                                 └─?[..][201]──Q[300:all]:{301}, \n" +
                            "                                                         └─?[201]:[type<inSet,[Horse]>], \n" +
                            "                   └─UnTyp[:[] a#3], \n" +
                            "                               └─?[..][301], \n" +
                            "                                       └─?[301]:[name<ne,jhone>]]";
        assertEquals(expected, print(query));
    }


    @Test
    public void testMatch_A_where_A_OfType_OR_B_OfType_Return_() {
        AsgTranslator<String, AsgQuery> translator = new CypherTranslator("Dragons", Collections.singleton(match));
        final AsgQuery query = translator.translate("MATCH (a)--(b) where a:Dragon Or b:Person RETURN a");
        AsgQuery expected = AsgQuery.Builder
                .start("cypher_", "Dragons")
                .next(quant1(300, some))
                .in(
                        unTyped(4, "a")
                                .next(quant1(400, all)
                                        .addNext(
                                                rel(6,null,Rel.Direction.RL,"Rel_#2")
                                                    .next(
                                                            unTyped(7, "b")
                                                                    .next(quant1(700, all)
                                                                            .addNext(ePropGroup(701, all,
                                                                                    of(701, "type",
                                                                                            of(inSet, Arrays.asList("Person")))))))
                                                    )
                                        ),
                        unTyped(8, "a")
                                .addNext(
                                        quant1(800, all)
                                            .addNext(
                                                    rel(10,null,Rel.Direction.RL,"Rel_#2")
                                                            .next(
                                                                    unTyped(11, "b")))
                                            .addNext(ePropGroup(801, all,
                                                    of(801, "type",
                                                        of(inSet, Arrays.asList("Dragon"))))
                                            )
                                )
                )
                .build();
        assertEquals(print(expected), print(query));
    }

    @Test
    public void testMatch_A_where_A_OfType_And_OR_B_OfType_AND_Return_() {
        AsgTranslator<String, AsgQuery> translator = new CypherTranslator("Dragons", Collections.singleton(match));
        final AsgQuery query = translator.translate("MATCH (a)-[c]-(b) where a:Dragon Or b:Person Or c:Fire RETURN a");

        AsgQuery expected = AsgQuery.Builder
                .start("cypher_", "Dragons")
                .next(quant1(300, some))
                .in(
                        unTyped(4, "a")
                                .next(quant1(400, all)
                                        .addNext(
                                                rel(6,null,Rel.Direction.RL,"c")
                                                    .next(
                                                            unTyped(7, "b")
                                                                    .next(quant1(700, all)
                                                                            .addNext(ePropGroup(701, all,
                                                                                    of(701, "type",
                                                                                            of(inSet, Arrays.asList("Person"))))))
                                                    )
                                                )
                                        ),
                        unTyped(8, "a")
                                .addNext(
                                        quant1(800, all)
                                            .addNext(
                                                    rel(10,null,Rel.Direction.RL,"c")
                                                            .next(
                                                                    unTyped(11, "b")))
                                            .addNext(ePropGroup(801, all,
                                                    of(801, "type",
                                                        of(inSet, Arrays.asList("Dragon"))))
                                            )
                                ),
                        unTyped(12, "a")
                                .addNext(
                                        quant1(1200, all)
                                            .addNext(
                                                    rel(14,null,Rel.Direction.RL,"c")
                                                            .below(relPropGroup(1400,all,
                                                                    RelProp.of(1401, "type", of(inSet, Arrays.asList("Fire")))))
                                                            .next(
                                                                    unTyped(15, "b")))
                                )
                ).build();
        assertEquals(print(expected), print(query));
    }

    @Test
    public void testMatch_A_AND_where_A_OfType_And_OR_B_OfType_AND_Return_() {
        AsgTranslator<String, AsgQuery> translator = new CypherTranslator("Dragons", Collections.singleton(match));
        final AsgQuery query = translator.translate("MATCH (a)-[c]-(b) where (a:Dragon AND a:Hours) Or c:Fire RETURN a");

        AsgQuery expected = AsgQuery.Builder
                .start("cypher_", "Dragons")
                .next(quant1(300, some))
                .in(
                        unTyped(4, "a")
                                .addNext(
                                        quant1(400, all)
                                                .addNext(
                                                        rel(6,null,Rel.Direction.RL,"c")
                                                                .below(relPropGroup(600,all,
                                                                        RelProp.of(601, "type", of(inSet, Arrays.asList("Fire")))))
                                                                .next(
                                                                        unTyped(7, "b")))
                                ),
                        unTyped(8, "a")
                                .addNext(
                                        quant1(800, all)
                                                .addNext(
                                                        rel(10,null,Rel.Direction.RL,"c")
                                                                .next(
                                                                        unTyped(11, "b"))
                                                )
                                                .addNext(ePropGroup(801, all,
                                                        of(801, "type",
                                                                of(inSet, Arrays.asList("Dragon"))),
                                                        of(802, "type",
                                                                of(inSet, Arrays.asList("Hours"))))
                                                )
                                )
                ).build();
        assertEquals(print(expected), print(query));
    }

    @Test
    public void testMatch_A_AND_where_A_OfType_And_OR_B_OfType_Return_() {
        AsgTranslator<String, AsgQuery> translator = new CypherTranslator("Dragons", Collections.singleton(match));
        final AsgQuery query = translator.translate("MATCH (a)-[c]-(b) where (a:Dragon Or c:Fire) And b:Person RETURN a");

        AsgQuery expected = AsgQuery.Builder
                .start("cypher_", "Dragons")
                .next(quant1(300, some))
                .in(
                        unTyped(4, "a")
                                .addNext(
                                        quant1(400, all)
                                                .addNext(
                                                        rel(6,null,Rel.Direction.RL,"c")
                                                                .below(relPropGroup(600,all,
                                                                        RelProp.of(601, "type", of(inSet, Arrays.asList("Fire")))))
                                                                .next(
                                                                        unTyped(7, "b")
                                                                                .next(quant1(700, all)
                                                                                        .addNext(ePropGroup(701, all,
                                                                                                of(701, "type",
                                                                                                        of(inSet, Arrays.asList("Person"))))))
                                                                )
                                                )
                                ),
                        unTyped(8, "a")
                                .addNext(
                                        quant1(800, all)
                                                .addNext(
                                                        rel(10,null,Rel.Direction.RL,"c")
                                                                .next(
                                                                        unTyped(11, "b")
                                                                                .next(quant1(1100, all)
                                                                                        .addNext(ePropGroup(1101, all,
                                                                                                of(1101, "type",
                                                                                                        of(inSet, Arrays.asList("Person"))))))
                                                                )
                                                )
                                                .addNext(ePropGroup(801, all,
                                                        of(801, "type",
                                                                of(inSet, Arrays.asList("Dragon"))))
                                                )
                                )
                ).build();
        assertEquals(print(expected), print(query));
    }
    @Test
    public void testMatch_A_AND_where_A_OfType_And_OR_B_OfType_AND_b_Return_() {
        AsgTranslator<String, AsgQuery> translator = new CypherTranslator("Dragons", Collections.singleton(match));
        final AsgQuery query = translator.translate("MATCH (a)-[c]-(b) where ((a:Dragon And a:Hours) Or c:Fire) And b:Person RETURN a");

        AsgQuery expected = AsgQuery.Builder
                .start("cypher_", "Dragons")
                .next(quant1(300, some))
                .in(
                        unTyped(4, "a")
                                .addNext(
                                        quant1(400, all)
                                                .addNext(
                                                        rel(6,null,Rel.Direction.RL,"c")
                                                                .next(
                                                                        unTyped(7, "b")
                                                                                .next(quant1(700, all)
                                                                                        .addNext(ePropGroup(701, all,
                                                                                                of(701, "type",
                                                                                                        of(inSet, Arrays.asList("Person"))))))
                                                                )
                                                )
                                                .addNext(ePropGroup(401, all,
                                                        of(401, "type",
                                                                of(inSet, Arrays.asList("Dragon"))),
                                                        of(402, "type",
                                                                of(inSet, Arrays.asList("Hours")))
                                                        )
                                                )
                                ),
                        unTyped(8, "a")
                                .addNext(
                                        quant1(800, all)
                                                .addNext(
                                                        rel(10,null,Rel.Direction.RL,"c")
                                                                .below(relPropGroup(1000,all,
                                                                        RelProp.of(1001, "type", of(inSet, Arrays.asList("Fire")))))
                                                                .next(
                                                                        unTyped(11, "b")
                                                                                .next(quant1(1100, all)
                                                                                        .addNext(ePropGroup(1101, all,
                                                                                                of(1101, "type",
                                                                                                        of(inSet, Arrays.asList("Person"))))))
                                                                )
                                                )
                                )
                ).build();
        assertEquals(print(expected), print(query));
    }

    @Test
    public void testMatch_A_AND_where_A_OfType_And_OR_B_OfType_AND_b_OR_c_Return_() {
        AsgTranslator<String, AsgQuery> translator = new CypherTranslator("Dragons", Collections.singleton(match));
        final AsgQuery query = translator.translate("MATCH (a)-[c]-(b) where ((a:Dragon And a:Hours) Or c:Fire) And (b:Person OR b:Hours) RETURN a");
        //expected string representation
        String expected = "[└── Start, \n" +
                "    ──Q[300:some]:{4|8|12|16}, \n" +
                "                         └─UnTyp[:[] a#4]──Q[400:all]:{6|401}, \n" +
                "                                                         └<--Rel(:null c#6)──UnTyp[:[] b#7]──Q[700:all]:{701}──Q[800:all]:{10}, \n" +
                "                                                                                                         └─?[..][701]──Q[1200:all]:{14}, \n" +
                "                                                                                                                 └─?[701]:[type<inSet,[Person]>]──Q[1600:all]:{18|1601}, \n" +
                "                                                         └─?[..][401], \n" +
                "                                                                 └─?[401]:[type<inSet,[Dragon]>], \n" +
                "                                                                 └─?[402]:[type<inSet,[Hours]>], \n" +
                "                         └─UnTyp[:[] a#8], \n" +
                "                                     └<--Rel(:null c#10)──UnTyp[:[] b#11]──Q[1100:all]:{1101}, \n" +
                "                                                    └─?[..][1000], \n" +
                "                                                             └─?[1001]:[type<inSet,[Fire]>], \n" +
                "                                                                                       └─?[..][1101], \n" +
                "                                                                                                └─?[1101]:[type<inSet,[Hours]>], \n" +
                "                         └─UnTyp[:[] a#12], \n" +
                "                                      └<--Rel(:null c#14)──UnTyp[:[] b#15]──Q[1500:all]:{1501}, \n" +
                "                                                     └─?[..][1400], \n" +
                "                                                              └─?[1401]:[type<inSet,[Fire]>], \n" +
                "                                                                                        └─?[..][1501], \n" +
                "                                                                                                 └─?[1501]:[type<inSet,[Person]>], \n" +
                "                         └─UnTyp[:[] a#16], \n" +
                "                                      └<--Rel(:null c#18)──UnTyp[:[] b#19]──Q[1900:all]:{1901}, \n" +
                "                                                                                          └─?[..][1901], \n" +
                "                                                                                                   └─?[1901]:[type<inSet,[Hours]>], \n" +
                "                                      └─?[..][1601], \n" +
                "                                               └─?[1601]:[type<inSet,[Dragon]>], \n" +
                "                                               └─?[1602]:[type<inSet,[Hours]>]]";
        assertEquals(expected, print(query));
    }

    @Test
    @Ignore
    //todo add Rule for same variable with multiple operators
    public void testMatch_A_OR_where_A_OfType_And_OR_B_OfType_AND_Return_() {
        AsgTranslator<String, AsgQuery> translator = new CypherTranslator("Dragons", Collections.singleton(match));
        final AsgQuery query = translator.translate("MATCH (a)-[c]-(b) where (a:Dragon OR a:Hours) Or c:Fire RETURN a");

        AsgQuery expected = AsgQuery.Builder
                .start("cypher_", "Dragons")
                .next(quant1(300, some))
                .in(
                        unTyped(4, "a")
                                .addNext(
                                        quant1(400, all)
                                                .addNext(
                                                        rel(6,null,Rel.Direction.RL,"c")
                                                                .below(relPropGroup(600,all,
                                                                        RelProp.of(60100, "type", of(inSet, Arrays.asList("Fire")))))
                                                                .next(
                                                                        unTyped(7, "b")))
                                ),
                        unTyped(8, "a")
                                .addNext(
                                        quant1(800, all)
                                                .addNext(
                                                        rel(10,null,Rel.Direction.RL,"c")
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