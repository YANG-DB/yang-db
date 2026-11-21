package com.yangdb.fuse.asg.translator.cypher;

import com.yangdb.fuse.model.asgQuery.AsgEBase;
import com.yangdb.fuse.model.asgQuery.AsgQuery;
import com.yangdb.fuse.model.asgQuery.AsgQueryUtil;
import com.yangdb.fuse.model.query.QueryInfo;
import com.yangdb.fuse.model.query.Rel;
import com.yangdb.fuse.model.resourceInfo.FuseError;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;

import java.io.StringWriter;
import java.util.Collections;
import java.util.List;

import static com.yangdb.fuse.model.transport.CreateQueryRequestMetadata.TYPE_CYPHERQL;
import static org.junit.Assert.*;

/**
 * Q9: a multi-type relationship ({@code [r:A|B]}) used to keep only {@code A}. Until relationship-type union is
 * supported it is rejected with an explicit unsupported-syntax error.
 */
public class CypherRelationshipTypeUnionTranslatorTest {
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

    @Test
    public void multiTypeRelationshipIsRejected() {
        try {
            translate("MATCH (a:Dragon)-[c:fire|freezes]-(b:Dragon) RETURN *");
            fail("expected an unsupported-syntax error");
        } catch (FuseError.FuseErrorException e) {
            assertEquals("Unsupported Cypher syntax", e.getError().getErrorCode());
            String description = e.getError().getErrorDescription();
            assertTrue(description, description.contains("[c:fire|freezes]"));
            assertTrue(description, description.contains("one query per"));
        }
    }

    @Test
    public void unnamedMultiTypeRelationshipIsRejected() {
        try {
            translate("MATCH (a:Dragon)-[:fire|freezes]-(b:Dragon) RETURN *");
            fail("expected an unsupported-syntax error");
        } catch (FuseError.FuseErrorException e) {
            assertEquals("Unsupported Cypher syntax", e.getError().getErrorCode());
            assertTrue(e.getError().getErrorDescription(), e.getError().getErrorDescription().contains("[:fire|freezes]"));
        }
    }

    @Test
    public void singleTypeAndUntypedRelationshipsStillTranslate() {
        assertEquals("fire", onlyRel(translate("MATCH (a:Dragon)-[c:fire]-(b:Dragon) RETURN *")).geteBase().getrType());
        assertEquals("*", onlyRel(translate("MATCH (a:Dragon)-[c]-(b:Dragon) RETURN *")).geteBase().getrType());
    }
}
