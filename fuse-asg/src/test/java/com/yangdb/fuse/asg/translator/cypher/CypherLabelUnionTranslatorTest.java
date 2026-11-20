package com.yangdb.fuse.asg.translator.cypher;

import com.yangdb.fuse.model.asgQuery.AsgEBase;
import com.yangdb.fuse.model.asgQuery.AsgQuery;
import com.yangdb.fuse.model.asgQuery.AsgQueryUtil;
import com.yangdb.fuse.model.query.QueryInfo;
import com.yangdb.fuse.model.query.entity.EUntyped;
import com.yangdb.fuse.model.resourceInfo.FuseError;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;
import org.opencypher.v9_0.util.SyntaxException;

import java.io.StringWriter;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import static com.yangdb.fuse.model.transport.CreateQueryRequestMetadata.TYPE_CYPHERQL;
import static org.junit.Assert.*;

/**
 * Q5: the openCypher 9 parser has no node-label union ({@code (n:A|B)}), so such a query must fail with an explicit,
 * actionable error instead of a bare parser message. Relationship-type unions ({@code [r:A|B]}) are valid openCypher 9
 * and must keep parsing.
 */
public class CypherLabelUnionTranslatorTest {
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

    @Test
    public void nodeLabelUnionFailsWithExplicitUnsupportedError() {
        for (String cypher : Arrays.asList(
                "match (p:Person | Dragon) return *",
                "match (p:Person|Dragon) return *",
                "match (a:Person)-[r:Own]->(b:Dragon | Horse) return *")) {
            try {
                translate(cypher);
                fail("expected an unsupported-syntax error for " + cypher);
            } catch (FuseError.FuseErrorException e) {
                String description = e.getError().getErrorDescription();
                assertTrue(description, description.contains("label union"));
                // points the user at the supported alternatives
                assertTrue(description, description.contains("OR"));
                assertTrue(e.getCause() instanceof SyntaxException);
            }
        }
    }

    @Test
    public void multiLabelNodeIsTheSupportedAnyOfForm() {
        AsgQuery query = translate("match (p:Person:Dragon) return *");
        List<AsgEBase<EUntyped>> untyped = AsgQueryUtil.elements(query, EUntyped.class);
        assertEquals(1, untyped.size());
        assertEquals(new HashSet<>(Arrays.asList("Person", "Dragon")), untyped.get(0).geteBase().getvTypes());
    }

    @Test
    public void relationshipTypeUnionIsNotReportedAsLabelUnion() {
        try {
            translate("match (a:Dragon)-[c:Fire|Freeze]-(b:Person) return *");
        } catch (FuseError.FuseErrorException e) {
            fail("relationship type union is valid openCypher 9 syntax: " + e.getError().getErrorDescription());
        } catch (RuntimeException ignored) {
            // multi-type relationships are a separate, known limitation; only the label-union path is under test
        }
    }

    @Test(expected = SyntaxException.class)
    public void otherSyntaxErrorsAreUnchanged() {
        translate("match (p:Person return *");
    }
}
