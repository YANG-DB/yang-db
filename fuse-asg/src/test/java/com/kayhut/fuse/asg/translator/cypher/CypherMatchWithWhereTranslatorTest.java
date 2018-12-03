package com.kayhut.fuse.asg.translator.cypher;

import com.kayhut.fuse.asg.translator.AsgTranslator;
import com.kayhut.fuse.asg.translator.cypher.strategies.MatchCypherTranslatorStrategy;
import com.kayhut.fuse.model.asgQuery.AsgEBase;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.query.Rel;
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
import static com.kayhut.fuse.model.query.properties.constraint.Constraint.of;
import static com.kayhut.fuse.model.query.properties.constraint.ConstraintOp.inSet;
import static com.kayhut.fuse.model.query.quant.QuantType.all;
import static com.kayhut.fuse.model.query.quant.QuantType.some;
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

    @Test
    public void testMatch_A_where_A_OfType_Return_A() {
        AsgTranslator<String, AsgQuery> translator = new CypherTranslator("Dragons", Collections.singleton(match));
        final AsgQuery query = translator.translate("MATCH (a) where a:Dragon RETURN a");
        AsgQuery expected = AsgQuery.Builder
                .start("cypher_", "Dragons")
                .next(unTyped(1, "a"))
                .next(quant1(100, some))
                .in(
                        eProp(101, "type", of(inSet, Arrays.asList("Dragon")))
                )
                .build();
        assertEquals(print(expected), print(query));
    }

    @Test
    public void testMatch_A_where_A_OfType_OR_A_OfType_Return_A() {
        AsgTranslator<String, AsgQuery> translator = new CypherTranslator("Dragons", Collections.singleton(match));
        final AsgQuery query = translator.translate("MATCH (a) where a:Dragon OR a:Hours RETURN a");
        AsgQuery expected = AsgQuery.Builder
                .start("cypher_", "Dragons")
                .next(unTyped(1, "a"))
                .next(quant1(100, some))
                .in(
                        eProp(101, "type", of(inSet, Arrays.asList("Dragon"))),
                        eProp(102, "type", of(inSet, Arrays.asList("Hours")))
                )
                .build();
        assertEquals(print(expected), print(query));
    }

    @Test
    public void testMatch_A_where_A_OfType_AND_A_OfType_Return_A() {
        AsgTranslator<String, AsgQuery> translator = new CypherTranslator("Dragons", Collections.singleton(match));
        final AsgQuery query = translator.translate("MATCH (a) where a:Dragon AND a:Hours RETURN a");
        AsgQuery expected = AsgQuery.Builder
                .start("cypher_", "Dragons")
                .next(unTyped(1, "a"))
                .next(quant1(100, all))
                .in(
                        eProp(101, "type", of(inSet, Arrays.asList("Dragon"))),
                        eProp(102, "type", of(inSet, Arrays.asList("Hours")))
                )
                .build();
        assertEquals(print(expected), print(query));
    }

    @Test
    public void testMatch_NodeA_NodeB_Return_A() {
        AsgTranslator<String, AsgQuery> translator = new CypherTranslator("Dragons", Collections.singleton(match));
        final AsgQuery query = translator.translate("MATCH (a)--(b) where a:Dragon AND b:Person RETURN a,b");

        //region Test Methods

        final AsgEBase<Quant1> quantA = quant1(100, all);
        quantA.addNext(rel(2, null, Rel.Direction.RL)
                .addNext(unTyped(3, "b")
                        .next(quant1(300, all)
                                .addNext(eProp(301, "type", of(inSet, Arrays.asList("Person"))))
                        )

                ));
        quantA.addNext(eProp(101, "type", of(inSet, Arrays.asList("Dragon"))));

        AsgQuery expected = AsgQuery.Builder
                .start("cypher_", "Dragons")
                .next(unTyped(1, "a"))
                .next(quantA)
                .build();
        assertEquals(print(expected), print(query));
    }

    /*


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

*/
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