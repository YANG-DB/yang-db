package com.kayhut.fuse.asg.translator.cypher;

import com.kayhut.fuse.asg.translator.AsgTranslator;
import com.kayhut.fuse.asg.translator.cypher.strategies.MatchCypherTranslatorStrategy;
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
import java.util.Collections;

import static com.kayhut.fuse.model.execution.plan.descriptors.QueryDescriptor.print;
import static org.junit.Assert.assertEquals;

/**
 * Created by lior.perry
 */
@Ignore
public class CypherMatchWithWhereTranslatorTest {
    //region Setup
    @Before
    public void setUp() throws Exception {
        match = new CypherTestUtils().setUp(readJsonToString("src/test/resources/Dragons_Ontology.json")).match;
    }
    //endregion


    //region Test Methods
    @Test
    public void testMatch_A_where_A_OfType_Return_A() {
        AsgTranslator<String, Query> translator = new CypherTranslator("Dragons", Collections.singleton(match));
        final Query query = translator.translate("MATCH (a) where a:Dragon OR a:Hours RETURN a");
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
        AsgTranslator<String, Query> translator = new CypherTranslator("Dragons", Collections.singleton(match));
        final Query query = translator.translate("MATCH (a)--(b) where a:Dragon, b:Person RETURN a,b");
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
    public void testMatch_Directional_NodeA_NodeB_Return_A() {
        AsgTranslator<String, Query> translator = new CypherTranslator("Dragons", Collections.singleton(match));
        final Query query = translator.translate("MATCH (a)-->(b) RETURN a,b");
        Query expected = Query.Builder.instance()
                .withName("cypher_").withOnt("Dragons")
                .withElements(Arrays.asList(
                        new Start(0, 1),
                        new EUntyped(1, "a", 2, 0),
                        new Rel(2, null,Rel.Direction.R, null,3, 0),
                        new EUntyped(3, "b", 4, 0)))
                .build();
        assertEquals(print(expected), print(query));
    }


    @Test
    public void testMatch_A_ofType_Dragon_B_ofType_Person_Return_A() {
        AsgTranslator<String, Query> translator = new CypherTranslator("Dragons", Collections.singleton(match));
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
    public void testMatch_NodeA_RelR_NodeB_Return_A() {
        AsgTranslator<String, Query> translator = new CypherTranslator("Dragons", Collections.singleton(match));
        final Query query = translator.translate("MATCH (a:Dragon)-[c]-(b:Person) RETURN a,b,c");
         Query expected = Query.Builder.instance()
                 .withName("cypher_").withOnt("Dragons")
                 .withElements(Arrays.asList(
                         new Start(0, 1),
                         new ETyped(1, "a","Dragon", 2, 0),
                         new Rel(2, null,Rel.Direction.RL, "c",3, 0),
                         new ETyped(3, "b","Person", 4, 0)))
                 .build();
         assertEquals(print(expected), print(query));
    }

    @Test
    public void testMatch_Directional_NodeA_RelR_NodeB_Return_A() {
        AsgTranslator<String, Query> translator = new CypherTranslator("Dragons", Collections.singleton(match));
        final Query query = translator.translate("MATCH (a)-[c]->(b) RETURN a,b,c");
        Query expected = Query.Builder.instance()
                .withName("cypher_").withOnt("Dragons")
                .withElements(Arrays.asList(
                        new Start(0, 1),
                        new EUntyped(1, "a", 2, 0),
                        new Rel(2, null,Rel.Direction.R, "c",3, 0),
                        new EUntyped(3, "b", 4, 0)))
                .build();
        assertEquals(print(expected), print(query));
    }

    @Test
    public void testMatch_Labeled_NodeA_RelR_NodeB_Return_A() {
        AsgTranslator<String, Query> translator = new CypherTranslator("Dragons", Collections.singleton(match));
        final Query query = translator.translate("MATCH (a:Dragon)-[c:Fire]-(b:Person) RETURN a,b,c");
        Query expected = Query.Builder.instance()
                .withName("cypher_").withOnt("Dragons")
                .withElements(Arrays.asList(
                        new Start(0, 1),
                        new ETyped(1, "a","Dragon", 2, 0),
                        new Rel(2, "Fire",Rel.Direction.RL, "c",3, 0),
                        new ETyped(3, "b","Person", 4, 0)))
                .build();
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

    //region Fields
    private MatchCypherTranslatorStrategy match;
     //endregion

}