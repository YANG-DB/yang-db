package com.yangdb.fuse.asg.translator.cypher;

import com.yangdb.fuse.model.asgQuery.AsgEBase;
import com.yangdb.fuse.model.asgQuery.AsgQuery;
import com.yangdb.fuse.model.asgQuery.AsgQueryUtil;
import com.yangdb.fuse.model.query.QueryInfo;
import com.yangdb.fuse.model.query.Rel;
import com.yangdb.fuse.model.query.properties.EPropGroup;
import com.yangdb.fuse.model.query.properties.FunctionRelProp;
import com.yangdb.fuse.model.query.properties.RelProp;
import com.yangdb.fuse.model.query.properties.RelPropGroup;
import com.yangdb.fuse.model.query.properties.constraint.CountConstraintOp;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;

import java.io.StringWriter;
import java.util.Collections;
import java.util.List;

import static com.yangdb.fuse.model.execution.plan.descriptors.AsgQueryDescriptor.print;
import static com.yangdb.fuse.model.transport.CreateQueryRequestMetadata.TYPE_CYPHERQL;
import static org.junit.Assert.*;

/**
 * C1: {@code WHERE count(k) = n} and {@code count(k) <> n} must become a count filter on the relationship, the same as
 * {@code count(k) > n} but with count op {@code eq} / {@code ne}. They used to produce no constraint at all, so the
 * filter was silently dropped.
 */
public class CypherCountFilterTranslatorTest {
    private static final String PATTERN = "MATCH (p1:Person)-[k:knows]->(p2:Person) WHERE count(k) %s 16 RETURN *";

    private CypherTranslator translator;

    @Before
    public void setUp() throws Exception {
        StringWriter writer = new StringWriter();
        IOUtils.copy(Thread.currentThread().getContextClassLoader().getResourceAsStream("Dragons_Ontology.json"), writer);
        CypherTestUtils utils = new CypherTestUtils().setUp(writer.toString());
        translator = new CypherTranslator(() -> Collections.singleton(utils.match));
    }

    private AsgQuery translate(String op) {
        return translator.translate(new QueryInfo<>(String.format(PATTERN, op), "q", TYPE_CYPHERQL, "Dragons"));
    }

    /** the single count constraint on relationship k */
    private static FunctionRelProp countProp(AsgQuery query) {
        List<AsgEBase<Rel>> rels = AsgQueryUtil.elements(query, Rel.class);
        assertEquals(1, rels.size());
        List<AsgEBase<RelPropGroup>> groups = AsgQueryUtil.bDescendants(rels.get(0), RelPropGroup.class);
        assertEquals("one RelPropGroup on k", 1, groups.size());
        List<RelProp> props = groups.get(0).geteBase().getProps();
        assertEquals(props.toString(), 1, props.size());
        assertTrue(props.get(0).getClass().getName(), props.get(0) instanceof FunctionRelProp);
        return (FunctionRelProp) props.get(0);
    }

    private void assertSameShapeAsGreaterThan(String op, CountConstraintOp expected) {
        AsgQuery gt = translate(">");
        AsgQuery query = translate(op);

        FunctionRelProp reference = countProp(gt);
        FunctionRelProp prop = countProp(query);
        assertEquals(CountConstraintOp.gt, reference.getCon().getCountOp());
        assertEquals(expected, prop.getCon().getCountOp());
        assertTrue(prop.getCon().isAggregation());
        assertEquals(reference.getpType(), prop.getpType());
        assertEquals(reference.getCon().getOp(), prop.getCon().getOp());
        assertEquals("16", prop.getCon().getExpr());
        // no stray property constraint on the nodes
        assertTrue(AsgQueryUtil.elements(query, EPropGroup.class).stream().allMatch(g -> g.geteBase().getProps().isEmpty()));
        // otherwise identical: same elements, same numbering
        assertEquals(print(gt.withProjectedFields(Collections.emptyMap())).replace("gt", "?"),
                print(query.withProjectedFields(Collections.emptyMap())).replace(expected.name(), "?"));
    }

    @Test
    public void countEqualsIsACountFilter() {
        assertSameShapeAsGreaterThan("=", CountConstraintOp.eq);
    }

    @Test
    public void countNotEqualsIsACountFilter() {
        assertSameShapeAsGreaterThan("<>", CountConstraintOp.ne);
    }

    @Test
    public void countInequalitiesAreUnchanged() {
        assertEquals(CountConstraintOp.ge, countProp(translate(">=")).getCon().getCountOp());
        assertEquals(CountConstraintOp.lt, countProp(translate("<")).getCon().getCountOp());
        assertEquals(CountConstraintOp.le, countProp(translate("<=")).getCon().getCountOp());
    }
}
