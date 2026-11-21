package com.yangdb.fuse.asg.translator.cypher;

import com.yangdb.fuse.model.asgQuery.AsgEBase;
import com.yangdb.fuse.model.asgQuery.AsgQuery;
import com.yangdb.fuse.model.asgQuery.AsgQueryUtil;
import com.yangdb.fuse.model.query.QueryInfo;
import com.yangdb.fuse.model.query.entity.ETyped;
import com.yangdb.fuse.model.query.properties.EProp;
import com.yangdb.fuse.model.query.properties.EPropGroup;
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
 * Q12: properties written in a node pattern ({@code (p:Person {k: v, ...})}) must all become constraints on the node,
 * exactly like {@code WHERE p.k = v}, for named and unnamed nodes.
 */
public class CypherNodePropertiesTranslatorTest {
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

    private static AsgEBase<ETyped> node(AsgQuery query, String eType) {
        List<AsgEBase<ETyped>> nodes = AsgQueryUtil.elements(query, ETyped.class);
        return nodes.stream().filter(n -> n.geteBase().geteType().equals(eType)).findFirst()
                .orElseThrow(() -> new AssertionError("no " + eType + " node"));
    }

    /** the constraints attached to the node (via its quant): pType -> eq value */
    private static Map<String, Object> nodeConstraints(AsgEBase<ETyped> node) {
        List<AsgEBase<EPropGroup>> groups = AsgQueryUtil.<ETyped, EPropGroup>nextDescendants(node, EPropGroup.class).stream()
                .filter(g -> !g.geteBase().getProps().isEmpty())
                .collect(java.util.stream.Collectors.toList());
        assertEquals("one non-empty EPropGroup on the node", 1, groups.size());
        Map<String, Object> constraints = new TreeMap<>();
        for (EProp prop : groups.get(0).geteBase().getProps()) {
            assertEquals(ConstraintOp.eq, prop.getCon().getOp());
            constraints.put(prop.getpType(), prop.getCon().getExpr());
        }
        return constraints;
    }

    @Test
    public void namedNodeWithOnePropertyMatchesWhereForm() {
        AsgQuery pattern = translate("MATCH (p:Person {firstName: 'Braden'}) RETURN *");
        AsgQuery where = translate("MATCH (p:Person) WHERE p.firstName = 'Braden' RETURN *");

        assertEquals(Collections.singletonMap("firstName", "Braden"), nodeConstraints(node(pattern, "Person")));
        assertEquals(print(where.withProjectedFields(Collections.emptyMap())),
                print(pattern.withProjectedFields(Collections.emptyMap())));
    }

    @Test
    public void namedNodeKeepsEveryProperty() {
        AsgQuery query = translate("MATCH (p:Person {gender: 'Female', firstName: 'Braden'}) RETURN *");
        Map<String, Object> expected = new TreeMap<>();
        expected.put("gender", "Female");
        expected.put("firstName", "Braden");
        assertEquals(expected, nodeConstraints(node(query, "Person")));
    }

    @Test
    public void unnamedNodeKeepsEveryProperty() {
        AsgQuery query = translate("MATCH (:Person {gender: 'Female', firstName: 'Braden'}) RETURN *");
        Map<String, Object> expected = new TreeMap<>();
        expected.put("gender", "Female");
        expected.put("firstName", "Braden");
        assertEquals(expected, nodeConstraints(node(query, "Person")));
    }

    @Test
    public void unnamedStartNodeWithPropertyInAPathMatchesNamedForm() {
        AsgQuery unnamed = translate("MATCH (:Person {firstName: 'Braden'})-[k:knows]->(p2:Person) RETURN *");
        AsgQuery named = translate("MATCH (`Node_#1`:Person {firstName: 'Braden'})-[k:knows]->(p2:Person) RETURN *");
        assertEquals(print(named.withProjectedFields(Collections.emptyMap())),
                print(unnamed.withProjectedFields(Collections.emptyMap())));
        assertEquals(Collections.singletonMap("firstName", "Braden"),
                nodeConstraints(AsgQueryUtil.<ETyped>elements(unnamed, ETyped.class).get(0)));
    }

    @Test
    public void unnamedEndNodeKeepsItsProperties() {
        AsgQuery query = translate("MATCH (p1:Person)-[k:knows]->(:Person {gender: 'Male', firstName: 'Braden'}) RETURN *");
        List<AsgEBase<ETyped>> nodes = AsgQueryUtil.elements(query, ETyped.class);
        assertEquals(2, nodes.size());
        Map<String, Object> expected = new TreeMap<>();
        expected.put("gender", "Male");
        expected.put("firstName", "Braden");
        assertEquals(expected, nodeConstraints(nodes.get(1)));
    }
}
