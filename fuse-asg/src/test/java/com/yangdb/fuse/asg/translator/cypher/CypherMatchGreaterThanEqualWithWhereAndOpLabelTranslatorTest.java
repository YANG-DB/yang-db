package com.yangdb.fuse.asg.translator.cypher;

import com.yangdb.fuse.asg.translator.AsgTranslator;
import com.yangdb.fuse.asg.translator.cypher.strategies.MatchCypherTranslatorStrategy;
import com.yangdb.fuse.model.asgQuery.AsgEBase;
import com.yangdb.fuse.model.asgQuery.AsgQuery;
import com.yangdb.fuse.model.asgQuery.AsgQueryUtil;
import com.yangdb.fuse.model.query.EBase;
import com.yangdb.fuse.model.query.QueryInfo;
import com.yangdb.fuse.model.query.Rel;
import com.yangdb.fuse.model.query.properties.RelProp;
import com.yangdb.fuse.model.query.quant.Quant1;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;

import java.io.InputStream;
import java.io.StringWriter;
import java.util.*;

import static com.yangdb.fuse.model.asgQuery.AsgQuery.Builder.*;
import static com.yangdb.fuse.model.execution.plan.descriptors.AsgQueryDescriptor.print;
import static com.yangdb.fuse.model.query.properties.EProp.of;
import static com.yangdb.fuse.model.query.properties.constraint.Constraint.of;
import static com.yangdb.fuse.model.query.properties.constraint.ConstraintOp.*;
import static com.yangdb.fuse.model.query.quant.QuantType.all;
import static com.yangdb.fuse.model.transport.CreateQueryRequestMetadata.TYPE_CYPHERQL;
import static org.junit.Assert.assertEquals;

/**
 * Created by lior.perry
 */
public class CypherMatchGreaterThanEqualWithWhereAndOpLabelTranslatorTest {
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
    public void testMatch_A_where_A_OfType_Return_A() {
        AsgTranslator<QueryInfo<String>, AsgQuery> translator = new CypherTranslator(() -> Collections.singleton(match));
        String q = "MATCH (a:Dragon) where a.age > 100 RETURN a";
        final AsgQuery query = translator.translate(new QueryInfo<>(q,"q", TYPE_CYPHERQL, "ont"));
        AsgQuery expected = AsgQuery.Builder
                .start("cypher_", "Dragons")
                .next(typed(1,"Dragon", "a"))
                .next(quant1(100, all))
                .in(ePropGroup(101,all,of(101, "age", of(gt, 100))))
                .build();
        expected.setProjectedFields(Collections.singletonMap("a", Collections.singletonList(AsgQueryUtil.getByTag(expected.getStart(), "a").get())));
        assertEquals(print(expected), print(query));
    }

    @Test
    public void testMatch_A_where_A_OfType_OR_A_OfType_Return_A() {
        AsgTranslator<QueryInfo<String>, AsgQuery> translator = new CypherTranslator(() -> Collections.singleton(match));
        String q = "MATCH (a:Dragon) where (a.age < 100 AND a.birth >= '28/01/2001') RETURN a";
        final AsgQuery query = translator.translate(new QueryInfo<>(q,"q", TYPE_CYPHERQL, "ont"));
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
        expected.setProjectedFields(Collections.singletonMap("a", Collections.singletonList(AsgQueryUtil.getByTag(expected.getStart(), "a").get())));
        assertEquals(print(expected), print(query));
    }

    @Test
    public void testMatch_A_where_A_OfType_AND_A_OfType_Return_A() {
        AsgTranslator<QueryInfo<String>,AsgQuery> translator = new CypherTranslator(() -> Collections.singleton(match));
        String q = "MATCH (a)--(b) where a:Dragon AND (a.age < 100 AND b.birth >= '28/01/2001')  RETURN a";
        final AsgQuery query = translator.translate(new QueryInfo<>(q,"q", TYPE_CYPHERQL, "ont"));
        final AsgEBase<Quant1> quantA = quant1(100, all);
        quantA.addNext(rel(2, "*", Rel.Direction.RL,"Rel_#2")
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
        Map<String, List<AsgEBase<EBase>>> fields = new HashMap<>();
        fields.put("a", Collections.singletonList(AsgQueryUtil.getByTag(expected.getStart(), "a").get()));
        fields.put("b", Collections.singletonList(AsgQueryUtil.getByTag(expected.getStart(), "b").get()));
        fields.put("Rel_#2", Collections.singletonList(AsgQueryUtil.getByTag(expected.getStart(), "Rel_#2").get()));
        expected.setProjectedFields(fields);
        assertEquals(print(expected), print(query));
    }

    @Test
    public void testMatch_A_where_A_OfType_AND_A_OfType_And_B_Return_All() {
        AsgTranslator<QueryInfo<String>,AsgQuery> translator = new CypherTranslator(() -> Collections.singleton(match));
        String q = "MATCH (a)--(b) where a:Dragon AND (a.age < b.age AND b.birth >= '28/01/2001')  RETURN a";
        final AsgQuery query = translator.translate(new QueryInfo<>(q,"q", TYPE_CYPHERQL, "ont"));
        final AsgEBase<Quant1> quantA = quant1(100, all);
        quantA.addNext(rel(2, "*", Rel.Direction.RL,"Rel_#2")
                .addNext(unTyped(3, "b")
                        .next(quant1(300, all)
                                .addNext(
                                        ePropGroup(301,all,
                                                of(301, "birth", of(ge, "28/01/2001")))
                                )
                        )));
        quantA.addNext(
                ePropGroup(101,all,
                        of(101, "age", of(lt, "b.age")),
                        of(102, "type", of(inSet, Arrays.asList("Dragon")))));

        AsgQuery expected = AsgQuery.Builder
                .start("cypher_", "Dragons")
                .next(unTyped(1, "a"))
                .next(quantA)
                .build();

        Map<String, List<AsgEBase<EBase>>> fields = new HashMap<>();
        fields.put("a", Collections.singletonList(AsgQueryUtil.getByTag(expected.getStart(), "a").get()));
        fields.put("b", Collections.singletonList(AsgQueryUtil.getByTag(expected.getStart(), "b").get()));
        fields.put("Rel_#2", Collections.singletonList(AsgQueryUtil.getByTag(expected.getStart(), "Rel_#2").get()));
        expected.setProjectedFields(fields);
        assertEquals(print(expected), print(query));
    }

    @Test
    public void testMatch_A_where_A_OfType_testMatch_A_where_A_OfType_AND_C_OfType_Return_A() {
        AsgTranslator<QueryInfo<String>,AsgQuery> translator = new CypherTranslator(() -> Collections.singleton(match));
        String q = "MATCH (a)-[c]-(b) where a.age < 100 AND b.birth >= '28/01/2001' AND (c:Fire AND c:Freeze) RETURN a,b";
        final AsgQuery query = translator.translate(new QueryInfo<>(q,"q", TYPE_CYPHERQL, "ont"));


        //region Test Methods

        final AsgEBase<Quant1> quantA = quant1(100, all);
        quantA.addNext(rel(2, "*", Rel.Direction.RL,"c")
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
        Map<String, List<AsgEBase<EBase>>> fields = new HashMap<>();
        fields.put("a", Collections.singletonList(AsgQueryUtil.getByTag(expected.getStart(), "a").get()));
        fields.put("b", Collections.singletonList(AsgQueryUtil.getByTag(expected.getStart(), "b").get()));
        fields.put("c", Collections.singletonList(AsgQueryUtil.getByTag(expected.getStart(), "c").get()));
        expected.setProjectedFields(fields);
        assertEquals(print(expected), print(query));
    }



    @Test
    public void testMatch_A_where_A_OfType_testMatch_A_where_A_OfType_AND_C_Return_All() {
        AsgTranslator<QueryInfo<String>,AsgQuery> translator = new CypherTranslator(() -> Collections.singleton(match));
        String q = "MATCH (a)-[c]-(b) where a.age < 100 AND b.birth >= '28/01/2001' AND (c.size > 50) RETURN *";
        final AsgQuery query = translator.translate(new QueryInfo<>(q,"q", TYPE_CYPHERQL, "ont"));

        //region Test Methods

        final AsgEBase<Quant1> quantA = quant1(100, all);
        quantA.addNext(rel(2, "*", Rel.Direction.RL,"c")
                .below(relPropGroup(200,all,
                        new RelProp(202,"size",of(gt, 50),0)))
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
        Map<String, List<AsgEBase<EBase>>> fields = new HashMap<>();
        fields.put("a", Collections.singletonList(AsgQueryUtil.getByTag(expected.getStart(), "a").get()));
        fields.put("b", Collections.singletonList(AsgQueryUtil.getByTag(expected.getStart(), "b").get()));
        fields.put("c", Collections.singletonList(AsgQueryUtil.getByTag(expected.getStart(), "c").get()));
        expected.setProjectedFields(fields);
        assertEquals(print(expected), print(query));
    }

    //endregion

    //region Fields
    private MatchCypherTranslatorStrategy match;
    //endregion

}