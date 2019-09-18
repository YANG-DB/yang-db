package com.yangdb.fuse.asg.translator.cypher;

import com.yangdb.fuse.asg.translator.AsgTranslator;
import com.yangdb.fuse.asg.translator.cypher.strategies.MatchCypherTranslatorStrategy;
import com.yangdb.fuse.model.asgQuery.AsgQuery;
import com.yangdb.fuse.model.query.Rel;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;

import static com.yangdb.fuse.model.asgQuery.AsgQuery.Builder.*;
import static com.yangdb.fuse.model.execution.plan.descriptors.AsgQueryDescriptor.print;
import static com.yangdb.fuse.model.query.quant.QuantType.all;
import static org.junit.Assert.assertEquals;

/**
 * Created by lior.perry
 */
public class CypherMatchSimpleTranslatorTest {
    //region Setup
    @Before
    public void setUp() throws Exception {
        InputStream stream = Thread.currentThread().getContextClassLoader().getResourceAsStream("Dragons_Ontology.json");
        StringWriter writer = new StringWriter();
        IOUtils.copy(stream, writer);
        match = new CypherTestUtils().setUp(writer.toString()).match;
    }
    //endregion


    //region Test Methods
    @Test
    public void testMatch_A_Return_A() {
        AsgTranslator<String, AsgQuery> translator = new CypherTranslator("Dragons", () -> Collections.singleton(match));
        final AsgQuery query = translator.translate("MATCH (a) RETURN a");

        AsgQuery expected = AsgQuery.Builder
                .start("cypher_", "Dragons")
                .next(unTyped(1, "a"))
                .build();

        assertEquals(print(expected), print(query));
    }

    @Test
    public void testMatch_A_ofType_Dragon_Return_A() {
        AsgTranslator<String, AsgQuery> translator = new CypherTranslator("Dragons", () -> Collections.singleton(match));
        final AsgQuery query = translator.translate("MATCH (a:Dragon) RETURN a");

        AsgQuery expected = AsgQuery.Builder
                .start("cypher_", "Dragons")
                .next(typed(1, "Dragon", "a"))
                .build();

        assertEquals(print(expected), print(query));
    }

    @Test
    public void testMatch_A_ofType_Dragon_Person_Return_A() {
        AsgTranslator<String, AsgQuery> translator = new CypherTranslator("Dragons", () -> Collections.singleton(match));
        final AsgQuery query = translator.translate("MATCH (a:Dragon:Person) RETURN a");

        AsgQuery expected = AsgQuery.Builder
                .start("cypher_", "Dragons")
                .next(unTyped(1, "a",Arrays.asList("Dragon","Person")))
                .build();

        assertEquals(print(expected), print(query));
    }

    @Test
    @Ignore("Not supported multi labels on edges")
    public void testMatch_A_ofRelType_Dragon_Person_Return_A() {
        AsgTranslator<String, AsgQuery> translator = new CypherTranslator("Dragons", () -> Collections.singleton(match));
        final AsgQuery query = translator.translate("MATCH (a:Dragon)-[c:Fire|Freeze]-(b:Person) RETURN *");
        AsgQuery expected = AsgQuery.Builder
                .start("cypher_", "Dragons")
                .next(typed(1, "Dragon","a"))
                .next(quant1(100, all))
                .in(
                        rel(2, null, Rel.Direction.RL,"c")
                                .next(typed(3,"Person", "b"))
                ).build();
        assertEquals(print(expected), print(query));
    }


    @Test
    public void testMatch_NodeA_NodeB_Return_A() {
        AsgTranslator<String, AsgQuery> translator = new CypherTranslator("Dragons", () -> Collections.singleton(match));
        final AsgQuery query = translator.translate("MATCH (a)--(b) RETURN a,b");

        AsgQuery expected = AsgQuery.Builder
                .start("cypher_", "Dragons")
                .next(unTyped(1, "a"))
                .next(quant1(100, all))
                .in(
                        rel(2, "*", Rel.Direction.RL,"Rel_#2")
                                .next(unTyped(3, "b"))
                ).build();

        assertEquals(print(expected), print(query));
    }

    @Test
    public void testMatch_Directional_NodeA_NodeB_Return_A() {
        AsgTranslator<String, AsgQuery> translator = new CypherTranslator("Dragons", () -> Collections.singleton(match));
        final AsgQuery query = translator.translate("MATCH (a)-->(b) RETURN a,b");

        AsgQuery expected = AsgQuery.Builder
                .start("cypher_", "Dragons")
                .next(unTyped(1, "a"))
                .next(quant1(100, all))
                .in(
                        rel(2, "*", Rel.Direction.R,"Rel_#2")
                                .next(unTyped(3, "b"))
                ).build();

        assertEquals(print(expected), print(query));
    }


    @Test
    public void testMatch_A_ofType_Dragon_B_ofType_Person_Return_A() {
        AsgTranslator<String, AsgQuery> translator = new CypherTranslator("Dragons", () -> Collections.singleton(match));
        final AsgQuery query = translator.translate("MATCH (a:Dragon)--(b:Person) RETURN a,b");

        AsgQuery expected = AsgQuery.Builder
                .start("cypher_", "Dragons")
                .next(typed(1, "Dragon","a"))
                .next(quant1(100, all))
                .in(
                        rel(2, "*", Rel.Direction.RL,"Rel_#2")
                                .next(typed(3,"Person", "b"))
                ).build();

        assertEquals(print(expected), print(query));
    }

    @Test
    public void testMatch_NodeA_RelR_NodeB_Return_A() {
        AsgTranslator<String, AsgQuery> translator = new CypherTranslator("Dragons", () -> Collections.singleton(match));
        final AsgQuery query = translator.translate("MATCH (a:Dragon)-[c]-(b:Person) RETURN a,b,c");
        AsgQuery expected = AsgQuery.Builder
                .start("cypher_", "Dragons")
                .next(typed(1, "Dragon","a"))
                .next(quant1(100, all))
                .in(
                        rel(2, "*", Rel.Direction.RL,"c")
                                .next(typed(3,"Person", "b"))
                ).build();
        assertEquals(print(expected), print(query));
    }

    @Test
    public void testMatch_Directional_NodeA_RelR_NodeB_Return_A() {
        AsgTranslator<String, AsgQuery> translator = new CypherTranslator("Dragons", () -> Collections.singleton(match));
        final AsgQuery query = translator.translate("MATCH (a)-[c]->(b) RETURN a,b,c");

        AsgQuery expected = AsgQuery.Builder
                .start("cypher_", "Dragons")
                .next(unTyped(1, "a"))
                .next(quant1(100, all))
                .in(
                        rel(2, "*", Rel.Direction.R,"c")
                                .next(unTyped(3, "b"))
                ).build();

        assertEquals(print(expected), print(query));
    }


    @Test
    public void testMatch_Labeled_NodeA_RelR_NodeB_Return_A() {
        AsgTranslator<String, AsgQuery> translator = new CypherTranslator("Dragons", () -> Collections.singleton(match));
        final AsgQuery query = translator.translate("MATCH (a:Dragon)-[c:Fire]-(b:Person) RETURN a,b,c");

        AsgQuery expected = AsgQuery.Builder
                .start("cypher_", "Dragons")
                .next(typed(1, "Dragon","a"))
                .next(quant1(100, all))
                .in(
                        rel(2, "Fire", Rel.Direction.RL,"c")
                                .next(typed(3,"Person", "b"))
                ).build();
        assertEquals(print(expected), print(query));
    }

    //endregion

    private MatchCypherTranslatorStrategy match;

}