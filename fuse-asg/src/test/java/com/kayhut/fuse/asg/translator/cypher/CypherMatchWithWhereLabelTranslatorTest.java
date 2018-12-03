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
import static com.kayhut.fuse.model.query.properties.constraint.Constraint.of;
import static com.kayhut.fuse.model.query.properties.constraint.ConstraintOp.inSet;
import static com.kayhut.fuse.model.query.quant.QuantType.all;
import static com.kayhut.fuse.model.query.quant.QuantType.some;
import static org.junit.Assert.assertEquals;

/**
 * Created by lior.perry
 */
@Ignore
public class CypherMatchWithWhereLabelTranslatorTest {
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
    public void testMatch_A_where_A_OfType_AND_B_OfType_Return_A() {
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
    @Test
    public void testMatch_A_where_A_OfType_testMatch_A_where_A_OfType_AND_C_OfType_Return_A() {
        AsgTranslator<String, AsgQuery> translator = new CypherTranslator("Dragons", Collections.singleton(match));
        final AsgQuery query = translator.translate("MATCH (a)-[c]-(b) where a:Dragon AND b:Person AND c:Fire RETURN a,b");

        //region Test Methods

        final AsgEBase<Quant1> quantA = quant1(100, all);
        quantA.addNext(rel(2, null, Rel.Direction.RL,"c")
                .below(relProp(201,new RelProp(201,"type",of(inSet, Arrays.asList("Fire")),0)))
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

    @Test
    public void testMatch_A_where_OpenBracket_A_OfType_testMatch_A_where_A_OfType_CloseBracket_AND_A_OfType_Return_A() {
        AsgTranslator<String, AsgQuery> translator = new CypherTranslator("Dragons", Collections.singleton(match));
        final AsgQuery query = translator.translate("MATCH (a)-[c]-(b) where (a:Dragon OR a:Person) AND a:Hours RETURN a,b,c");

        //region Test Methods

        final AsgEBase<Quant1> quantA = quant1(100, all);
        quantA.addNext(rel(2, null, Rel.Direction.RL,"c")
                .below(relProp(201,new RelProp(201,"type",of(inSet, Arrays.asList("Fire")),0)))
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