package com.kayhut.fuse.asg.translator.cypher;

import com.kayhut.fuse.asg.translator.AsgTranslator;
import com.kayhut.fuse.asg.translator.cypher.strategies.MatchCypherTranslatorStrategy;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.query.Rel;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;

import static com.kayhut.fuse.model.asgQuery.AsgQuery.Builder.*;
import static com.kayhut.fuse.model.execution.plan.descriptors.AsgQueryDescriptor.print;
import static com.kayhut.fuse.model.query.quant.QuantType.all;
import static org.junit.Assert.assertEquals;

/**
 * Created by lior.perry
 */
public class CypherMatchTranslatorTest {
    //region Setup
    @Before
    public void setUp() throws Exception {
        match = new CypherTestUtils().setUp(readJsonToString("src/test/resources/Dragons_Ontology.json")).match;
    }
    //endregion


    //region Test Methods
    @Test
    public void testMatch_A_Return_A() {
        AsgTranslator<String, AsgQuery> translator = new CypherTranslator("Dragons", Collections.singleton(match));
        final AsgQuery query = translator.translate("MATCH (a) RETURN a");

        AsgQuery expected = AsgQuery.Builder
                .start("cypher_", "Dragons")
                .next(unTyped(1, "a"))
                .build();

        assertEquals(print(expected), print(query));
    }

    @Test
    public void testMatch_A_ofType_Dragon_Return_A() {
        AsgTranslator<String, AsgQuery> translator = new CypherTranslator("Dragons", Collections.singleton(match));
        final AsgQuery query = translator.translate("MATCH (a:Dragon) RETURN a");

        AsgQuery expected = AsgQuery.Builder
                .start("cypher_", "Dragons")
                .next(typed(1, "Dragon", "a"))
                .build();

        assertEquals(print(expected), print(query));
    }


    @Test
    public void testMatch_NodeA_NodeB_Return_A() {
        AsgTranslator<String, AsgQuery> translator = new CypherTranslator("Dragons", Collections.singleton(match));
        final AsgQuery query = translator.translate("MATCH (a)--(b) RETURN a,b");

        AsgQuery expected = AsgQuery.Builder
                .start("cypher_", "Dragons")
                .next(unTyped(1, "a"))
                .next(quant1(100, all))
                .in(
                        rel(2, null, Rel.Direction.RL)
                                .next(unTyped(3, "b"))
                ).build();

        assertEquals(print(expected), print(query));
    }

    @Test
    public void testMatch_Directional_NodeA_NodeB_Return_A() {
        AsgTranslator<String, AsgQuery> translator = new CypherTranslator("Dragons", Collections.singleton(match));
        final AsgQuery query = translator.translate("MATCH (a)-->(b) RETURN a,b");

        AsgQuery expected = AsgQuery.Builder
                .start("cypher_", "Dragons")
                .next(unTyped(1, "a"))
                .next(quant1(100, all))
                .in(
                        rel(2, null, Rel.Direction.R)
                                .next(unTyped(3, "b"))
                ).build();

        assertEquals(print(expected), print(query));
    }


    @Test
    public void testMatch_A_ofType_Dragon_B_ofType_Person_Return_A() {
        AsgTranslator<String, AsgQuery> translator = new CypherTranslator("Dragons", Collections.singleton(match));
        final AsgQuery query = translator.translate("MATCH (a:Dragon)--(b:Person) RETURN a,b");

        AsgQuery expected = AsgQuery.Builder
                .start("cypher_", "Dragons")
                .next(typed(1, "Dragon","a"))
                .next(quant1(100, all))
                .in(
                        rel(2, null, Rel.Direction.RL)
                                .next(typed(3,"Person", "b"))
                ).build();

        assertEquals(print(expected), print(query));
    }

    @Test
    public void testMatch_NodeA_RelR_NodeB_Return_A() {
        AsgTranslator<String, AsgQuery> translator = new CypherTranslator("Dragons", Collections.singleton(match));
        final AsgQuery query = translator.translate("MATCH (a:Dragon)-[c]-(b:Person) RETURN a,b,c");
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
    public void testMatch_Directional_NodeA_RelR_NodeB_Return_A() {
        AsgTranslator<String, AsgQuery> translator = new CypherTranslator("Dragons", Collections.singleton(match));
        final AsgQuery query = translator.translate("MATCH (a)-[c]->(b) RETURN a,b,c");

        AsgQuery expected = AsgQuery.Builder
                .start("cypher_", "Dragons")
                .next(unTyped(1, "a"))
                .next(quant1(100, all))
                .in(
                        rel(2, null, Rel.Direction.R,"c")
                                .next(unTyped(3, "b"))
                ).build();

        assertEquals(print(expected), print(query));
    }


    @Test
    public void testMatch_Labeled_NodeA_RelR_NodeB_Return_A() {
        AsgTranslator<String, AsgQuery> translator = new CypherTranslator("Dragons", Collections.singleton(match));
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

    private MatchCypherTranslatorStrategy match;

}