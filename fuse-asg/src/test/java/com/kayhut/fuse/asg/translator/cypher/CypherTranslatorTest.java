package com.kayhut.fuse.asg.translator.cypher;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kayhut.fuse.asg.translator.AsgTranslator;
import com.kayhut.fuse.asg.translator.cypher.strategies.CypherTranslatorStrategy;
import com.kayhut.fuse.asg.translator.cypher.strategies.MatchNodePatternCypherTranslatorStrategy;
import com.kayhut.fuse.asg.translator.cypher.strategies.MatchStepPatternCypherTranslatorStrategy;
import com.kayhut.fuse.model.ontology.Ontology;
import com.kayhut.fuse.model.query.Query;
import com.kayhut.fuse.model.query.Rel;
import com.kayhut.fuse.model.query.Start;
import com.kayhut.fuse.model.query.entity.ETyped;
import com.kayhut.fuse.model.query.entity.EUntyped;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import static com.kayhut.fuse.model.execution.plan.descriptors.QueryDescriptor.print;
import static org.junit.Assert.assertEquals;

/**
 * Created by benishue on 09-May-17.
 */
public class CypherTranslatorTest {
    //region Setup
    @Before
    public void setUp() throws Exception {
        String ontologyExpectedJson = readJsonToString("src/test/resources/Dragons_Ontology.json");
        ont = new Ontology.Accessor(new ObjectMapper().readValue(ontologyExpectedJson, Ontology.class));
        strategy = Arrays.asList(new MatchNodePatternCypherTranslatorStrategy(),
                new MatchStepPatternCypherTranslatorStrategy(
                        new MatchNodePatternCypherTranslatorStrategy()
                ));
    }
    //endregion


    //region Test Methods
    @Test
    public void testMatch_A_Return_A() {
        AsgTranslator<String, Query> translator = new CypherTranslator("Dragons", strategy);
        final Query query = translator.translate("MATCH (a) RETURN a");
        Query expected = Query.Builder.instance()
                .withName("cypher_").withOnt("Dragons")
                .withElements(Arrays.asList(
                        new Start(0, 1),
                        new EUntyped(1, "a", 3, 0)))
                .build();
        assertEquals(print(expected), print(query));
    }

    @Test
    public void testMatch_A_ofType_Dragon_Return_A() {
        AsgTranslator<String, Query> translator = new CypherTranslator("Dragons", strategy);
        final Query query = translator.translate("MATCH (a:Dragon) RETURN a");
        Query expected = Query.Builder.instance()
                .withName("cypher_").withOnt("Dragons")
                .withElements(Arrays.asList(
                        new Start(0, 1),
                        new ETyped(1, "a","Dragon", 3, 0)))
                .build();
        assertEquals(print(expected), print(query));
    }

    @Test
    public void testMatch_NodeA_NodeB_Return_A() {
        AsgTranslator<String, Query> translator = new CypherTranslator("Dragons", strategy);
        final Query query = translator.translate("MATCH (a)--(b) RETURN a,b");
        Query expected = Query.Builder.instance()
                .withName("cypher_").withOnt("Dragons")
                .withElements(Arrays.asList(
                        new Start(0, 1),
                        new EUntyped(1, "a", 2, 0),
                        new Rel(2, null,Rel.Direction.RL, null,3, 0),
                        new EUntyped(3, "b", 4, 0)))
                .build();
        assertEquals(print(expected), print(query));
    }

    @Test
    public void testMatch_A_ofType_Dragon_B_ofType_Person_Return_A() {
        AsgTranslator<String, Query> translator = new CypherTranslator("Dragons", strategy);
        final Query query = translator.translate("MATCH (a:Dragon)--(b:Person) RETURN a,b");
        Query expected = Query.Builder.instance()
                .withName("cypher_").withOnt("Dragons")
                .withElements(Arrays.asList(
                        new Start(0, 1),
                        new ETyped(1, "a","Dragon", 2, 0),
                        new Rel(2, null,Rel.Direction.RL, null,3, 0),
                        new ETyped(3, "b","Person", 4, 0)))
                .build();
        assertEquals(print(expected), print(query));
    }

    @Test
    @Ignore
    public void testMatch_Directional_NodeA_NodeB_Return_A() {
        AsgTranslator<String, Query> translator = new CypherTranslator("Dragons", strategy);
        final Query query = translator.translate("MATCH (a)-->(b) RETURN a,b");
        Query expeced = null;
        assertEquals(print(expeced), print(query));
    }

    @Test
    @Ignore
    public void testMatch_NodeA_RelR_NodeB_Return_A() {
        AsgTranslator<String, Query> translator = new CypherTranslator("Dragons", strategy);
        final Query query = translator.translate("MATCH (a)-[c]-(b) RETURN a,b,c");
        Query expeced = null;
        assertEquals(print(expeced), print(query));
    }

    @Test
    @Ignore
    public void testMatch_Directional_NodeA_RelR_NodeB_Return_A() {
        AsgTranslator<String, Query> translator = new CypherTranslator("Dragons", strategy);
        final Query query = translator.translate("MATCH (a)-[c]->(b) RETURN a,b,c");
        Query expeced = null;
        assertEquals(print(expeced), print(query));
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

    //region Fields
    private Ontology.Accessor ont;
    private List<CypherTranslatorStrategy> strategy;

    //endregion

}