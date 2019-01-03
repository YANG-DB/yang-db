package com.kayhut.fuse.asg.translator.cypher;

import com.kayhut.fuse.asg.translator.AsgTranslator;
import com.kayhut.fuse.asg.translator.cypher.strategies.MatchCypherTranslatorStrategy;
import com.kayhut.fuse.model.asgQuery.AsgEBase;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.query.Rel;
import com.kayhut.fuse.model.query.entity.ETyped;
import com.kayhut.fuse.model.query.properties.RelProp;
import com.kayhut.fuse.model.query.quant.Quant1;
import org.junit.Before;
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
import static org.junit.Assert.assertEquals;

/**
 * Created by lior.perry
 */
public class CypherMatchGreaterThanEqualWithWhereAndOpLabelTranslatorTest {
    //region Setup
    @Before
    public void setUp() throws Exception {
        match = new CypherTestUtils().setUp(readJsonToString("src/test/resources/Dragons_Ontology.json")).match;
    }
    //endregion

    @Test
    public void testMatch_A_where_A_OfType_Return_A() {
        AsgTranslator<String, AsgQuery> translator = new CypherTranslator("Dragons", () -> Collections.singleton(match));
        final AsgQuery query = translator.translate("MATCH (a:Dragon) where a.age > 100 RETURN a");
        AsgQuery expected = AsgQuery.Builder
                .start("cypher_", "Dragons")
                .next(typed(1,"Dragon", "a"))
                .next(quant1(100, all))
                .in(ePropGroup(101,all,of(101, "age", of(gt, 100))))
                .build();
        assertEquals(print(expected), print(query));
    }

    @Test
    public void testMatch_A_where_A_OfType_OR_A_OfType_Return_A() {
        AsgTranslator<String, AsgQuery> translator = new CypherTranslator("Dragons", () -> Collections.singleton(match));
        final AsgQuery query = translator.translate("MATCH (a:Dragon) where (a.age < 100 AND a.birth >= '28/01/2001') RETURN a");
        AsgQuery expected = AsgQuery.Builder
                .start("cypher_", "Dragons")
                .next(typed(1,"Dragon", "a"))
                .next(quant1(100, all))
                .in(
                        ePropGroup(101,all,
                                of(101, "age", of(lt, 100)),
                                of(102, "birth", of(ge, "28/01/2001")))
                )
                .build();
        assertEquals(print(expected), print(query));
    }

    @Test
    public void testMatch_A_where_A_OfType_AND_A_OfType_Return_A() {
        AsgTranslator<String, AsgQuery> translator = new CypherTranslator("Dragons", () -> Collections.singleton(match));
        final AsgQuery query = translator.translate("MATCH (a)--(b) where a:Dragon AND (a.age < 100 AND b.birth >= '28/01/2001')  RETURN a");
        final AsgEBase<Quant1> quantA = quant1(100, all);
        quantA.addNext(rel(2, null, Rel.Direction.RL,"Rel_#2")
                .addNext(unTyped(3, "b")
                        .next(quant1(300, all)
                                .addNext(
                                        ePropGroup(301,all,
                                                of(301, "birth", of(ge, "28/01/2001")))
                                )
                        )));
        quantA.addNext(
                ePropGroup(101,all,
                        of(101, "age", of(lt, 100)),
                        of(102, "type", of(inSet, Arrays.asList("Dragon")))));

        AsgQuery expected = AsgQuery.Builder
                .start("cypher_", "Dragons")
                .next(unTyped(1, "a"))
                .next(quantA)
                .build();
        assertEquals(print(expected), print(query));
    }

    @Test
    public void testMatch_A_where_A_OfType_testMatch_A_where_A_OfType_AND_C_OfType_Return_A() {
        AsgTranslator<String, AsgQuery> translator = new CypherTranslator("Dragons", () -> Collections.singleton(match));
        final AsgQuery query = translator.translate("MATCH (a)-[c]-(b) where a.age < 100 AND b.birth >= '28/01/2001' AND (c:Fire AND c:Freeze) RETURN a,b");

        //region Test Methods

        final AsgEBase<Quant1> quantA = quant1(100, all);
        quantA.addNext(rel(2, null, Rel.Direction.RL,"c")
                .below(relPropGroup(200,all,
                        new RelProp(201,"type",of(inSet, Arrays.asList("Freeze")),0),
                        new RelProp(201,"type",of(inSet, Arrays.asList("Fire")),0)))
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

        AsgQuery expected = AsgQuery.Builder
                .start("cypher_", "Dragons")
                .next(unTyped(1, "a"))
                .next(quantA)
                .build();
        assertEquals(print(expected), print(query));
    }
    @Test
    public void testMatch_A_where_A_OfType_testMatch_A_where_A_OfType_AND_C_Return_All() {
        AsgTranslator<String, AsgQuery> translator = new CypherTranslator("Dragons", () -> Collections.singleton(match));
        final AsgQuery query = translator.translate("MATCH (a)-[c]-(b) where a.age < 100 AND b.birth >= '28/01/2001' AND (c.size > 50) RETURN *");

        //region Test Methods

        final AsgEBase<Quant1> quantA = quant1(100, all);
        quantA.addNext(rel(2, null, Rel.Direction.RL,"c")
                .below(relPropGroup(200,all,
                        new RelProp(201,"size",of(ge, 50),0)))
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