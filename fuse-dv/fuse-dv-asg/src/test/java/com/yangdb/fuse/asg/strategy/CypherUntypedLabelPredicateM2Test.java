package com.yangdb.fuse.asg.strategy;

import com.yangdb.fuse.model.asgQuery.AsgEBase;
import com.yangdb.fuse.model.asgQuery.AsgQuery;
import com.yangdb.fuse.model.asgQuery.AsgQueryUtil;
import com.yangdb.fuse.model.query.entity.EUntyped;
import com.yangdb.fuse.model.query.properties.EProp;
import com.yangdb.fuse.model.query.properties.EPropGroup;
import com.yangdb.fuse.model.query.properties.SchematicEProp;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

/**
 * Q8: {@code WHERE p:A OR p:B} on an untyped node (the documented alternative to label union) crashed the M2 rewrite
 * with "EUntyped cannot be cast to ETyped" in the schema-aware constraint strategies.
 */
public class CypherUntypedLabelPredicateM2Test {
    private static M2CypherPipelineTestSupport pipeline;

    @BeforeClass
    public static void setUp() throws Exception {
        pipeline = new M2CypherPipelineTestSupport();
    }

    private static List<EProp> props(AsgQuery query) {
        return AsgQueryUtil.elements(query, EPropGroup.class).stream()
                .flatMap(group -> allProps(group.geteBase()).stream())
                .collect(Collectors.toList());
    }

    private static List<EProp> allProps(EPropGroup group) {
        List<EProp> props = group.getProps().stream().collect(Collectors.toList());
        group.getGroups().forEach(child -> props.addAll(allProps(child)));
        return props;
    }

    @Test
    public void labelOrPredicateOnUntypedNodeSurvivesM2Rewrite() {
        AsgQuery query = pipeline.rewrite("match (p) where p:Person OR p:Dragon return *");

        // OR is translated into a some-quant with one untyped branch per label (the planner unions the branches)
        List<AsgEBase<EUntyped>> untyped = AsgQueryUtil.elements(query, EUntyped.class);
        assertEquals(2, untyped.size());

        // both label alternatives are still constrained on the node
        Set<Object> labelValues = props(query).stream()
                .filter(p -> "type".equals(p.getpType()) && p.getCon() != null)
                .flatMap(p -> p.getCon().getExpr() instanceof Collection
                        ? ((Collection<?>) p.getCon().getExpr()).stream()
                        : java.util.stream.Stream.of(p.getCon().getExpr()))
                .collect(Collectors.toSet());
        assertTrue(labelValues.toString(), labelValues.contains("Person"));
        assertTrue(labelValues.toString(), labelValues.contains("Dragon"));
    }

    @Test
    public void propertyConstraintOnUntypedNodeIsLeftForTheGenericPath() {
        // a single exact-index rewrite needs a single vertex schema; an untyped node keeps its plain constraint
        AsgQuery query = pipeline.rewrite("match (p) where (p:Person OR p:Dragon) AND p.name = 'Braden Heaney' return *");
        List<EProp> nameProps = props(query).stream().filter(p -> "name".equals(p.getpType())).collect(Collectors.toList());
        assertFalse(nameProps.isEmpty());
        assertTrue(nameProps.stream().noneMatch(p -> p instanceof SchematicEProp));
    }

    @Test
    public void propertyConstraintOnTypedNodeStillUsesTheExactIndex() {
        AsgQuery query = pipeline.rewrite("match (p:Person) where p.name = 'Braden Heaney' return *");
        List<EProp> nameProps = props(query).stream().filter(p -> "name".equals(p.getpType())).collect(Collectors.toList());
        assertEquals(1, nameProps.size());
        assertTrue(nameProps.get(0) instanceof SchematicEProp);
        assertEquals("name.keyword", ((SchematicEProp) nameProps.get(0)).getSchematicName());
    }
}
