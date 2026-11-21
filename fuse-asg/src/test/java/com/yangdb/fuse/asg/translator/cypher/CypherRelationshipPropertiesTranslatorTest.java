package com.yangdb.fuse.asg.translator.cypher;

import com.yangdb.fuse.model.asgQuery.AsgEBase;
import com.yangdb.fuse.model.asgQuery.AsgQuery;
import com.yangdb.fuse.model.asgQuery.AsgQueryUtil;
import com.yangdb.fuse.model.query.QueryInfo;
import com.yangdb.fuse.model.query.Rel;
import com.yangdb.fuse.model.query.properties.RelProp;
import com.yangdb.fuse.model.query.properties.RelPropGroup;
import com.yangdb.fuse.model.query.properties.constraint.ConstraintOp;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;

import java.io.StringWriter;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static com.yangdb.fuse.model.execution.plan.descriptors.AsgQueryDescriptor.print;
import static com.yangdb.fuse.model.transport.CreateQueryRequestMetadata.TYPE_CYPHERQL;
import static org.junit.Assert.*;

/**
 * Q10: properties written in a relationship pattern ({@code -[f:fire {k: v}]->}) must all become constraints on the
 * relationship, exactly like {@code WHERE f.k = v}, for named and unnamed relationships.
 */
public class CypherRelationshipPropertiesTranslatorTest {
    private CypherTranslator translator;

    @Before
    public void setUp() throws Exception {
        StringWriter writer = new StringWriter();
        IOUtils.copy(Thread.currentThread().getContextClassLoader().getResourceAsStream("Dragons_Ontology.json"), writer);
        CypherTestUtils utils = new CypherTestUtils().setUp(writer.toString());
        translator = new CypherTranslator(() -> Collections.singleton(utils.match));
    }

    private AsgQuery translate(String cypher) {
        return translator.translate(new QueryInfo<>(cypher, "q", TYPE_CYPHERQL, "Dragons"));
    }

    private static AsgEBase<Rel> onlyRel(AsgQuery query) {
        List<AsgEBase<Rel>> rels = AsgQueryUtil.elements(query, Rel.class);
        assertEquals(1, rels.size());
        return rels.get(0);
    }

    /** the relationship's own property constraints: pType -> eq value */
    private static Map<String, Object> relConstraints(AsgEBase<Rel> rel) {
        List<AsgEBase<RelPropGroup>> groups = AsgQueryUtil.bDescendants(rel, RelPropGroup.class);
        assertEquals("one RelPropGroup below the relationship", 1, groups.size());
        Map<String, Object> constraints = new TreeMap<>();
        for (RelProp prop : groups.get(0).geteBase().getProps()) {
            assertEquals(ConstraintOp.eq, prop.getCon().getOp());
            constraints.put(prop.getpType(), prop.getCon().getExpr());
        }
        return constraints;
    }

    @Test
    public void namedRelationshipWithOnePropertyMatchesWhereForm() {
        AsgQuery pattern = translate("MATCH (d1:Dragon)-[f:fire {timestamp: 187}]->(d2:Dragon) RETURN *");
        AsgQuery where = translate("MATCH (d1:Dragon)-[f:fire]->(d2:Dragon) WHERE f.timestamp = 187 RETURN *");

        assertEquals(Collections.singletonMap("timestamp", "187"), relConstraints(onlyRel(pattern)));
        assertEquals(print(where.withProjectedFields(Collections.emptyMap())),
                print(pattern.withProjectedFields(Collections.emptyMap())));
    }

    @Test
    public void namedRelationshipKeepsEveryProperty() {
        AsgQuery query = translate("MATCH (d1:Dragon)-[f:freezes {timestamp: 373, duration: 1}]->(d2:Dragon) RETURN *");
        Map<String, Object> expected = new TreeMap<>();
        expected.put("timestamp", "373");
        expected.put("duration", "1");
        assertEquals(expected, relConstraints(onlyRel(query)));
    }

    @Test
    public void unnamedRelationshipKeepsEveryProperty() {
        AsgQuery query = translate("MATCH (d1:Dragon)-[:freezes {timestamp: 373, duration: 1}]->(d2:Dragon) RETURN *");
        AsgEBase<Rel> rel = onlyRel(query);
        assertEquals("freezes", rel.geteBase().getrType());
        Map<String, Object> expected = new TreeMap<>();
        expected.put("timestamp", "373");
        expected.put("duration", "1");
        assertEquals(expected, relConstraints(rel));
    }

    @Test
    public void unnamedRelationshipWithOnePropertyMatchesNamedForm() {
        AsgQuery unnamed = translate("MATCH (d1:Dragon)-[:fire {timestamp: 187}]->(d2:Dragon) RETURN *");
        AsgQuery named = translate("MATCH (d1:Dragon)-[`Rel_#2`:fire {timestamp: 187}]->(d2:Dragon) RETURN *");
        assertEquals(print(named.withProjectedFields(Collections.emptyMap())),
                print(unnamed.withProjectedFields(Collections.emptyMap())));
    }

    @Test
    public void relationshipPropertiesStayOnTheRelationshipNotTheEndNode() {
        AsgQuery query = translate("MATCH (d1:Dragon)-[f:fire {timestamp: 187}]->(d2:Dragon) RETURN *");
        AsgEBase<Rel> rel = onlyRel(query);
        // the end node hangs directly off the relationship
        assertEquals(1, rel.getNext().size());
        assertEquals("d2", ((com.yangdb.fuse.model.query.entity.ETyped) rel.getNext().get(0).geteBase()).geteTag());
        assertTrue(AsgQueryUtil.elements(query, com.yangdb.fuse.model.query.properties.EPropGroup.class).stream()
                .allMatch(g -> g.geteBase().getProps().isEmpty()));
    }

}
