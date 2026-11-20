package com.yangdb.fuse.asg.translator.cypher;

import com.yangdb.fuse.model.asgQuery.AsgEBase;
import com.yangdb.fuse.model.asgQuery.AsgQuery;
import com.yangdb.fuse.model.asgQuery.AsgQueryUtil;
import com.yangdb.fuse.model.query.QueryInfo;
import com.yangdb.fuse.model.query.Rel;
import com.yangdb.fuse.model.query.entity.EEntityBase;
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
 * Q6: a relationship that ends on an already-bound node cannot be represented in the (tree-shaped) ASG: it was left
 * dangling and only rejected later by the ontology validator ("Relation with No entity Attached"). Cycles are not
 * supported by the engine, so the translator must say so explicitly.
 */
public class CypherCyclicPatternTranslatorTest {
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

    private String unsupported(String cypher) {
        try {
            translate(cypher);
        } catch (FuseError.FuseErrorException e) {
            return e.getError().getErrorDescription();
        }
        fail("expected an unsupported-pattern error for " + cypher);
        return null;
    }

    @Test
    public void tutorialCycleAcrossCommaPatternsIsRejectedExplicitly() {
        String description = unsupported("match (p1:Person)-[own1:Own]-(d1:Dragon), (d1:Dragon)-[fire:Fire]-(d2:Dragon), " +
                "(d2:Dragon)<-[own2:Own]-(p2:Person), (p1:Person)-[know:Know]-(p2:Person) " +
                "where fire.temperature > 600 return *");
        assertTrue(description, description.contains("Cyclic patterns are not supported"));
        assertTrue(description, description.contains("p2"));
    }

    @Test
    public void cycleWithinOneChainIsRejectedExplicitly() {
        String description = unsupported("match (a:Person)-[k1:Know]->(b:Person)-[k2:Know]->(a:Person) return *");
        assertTrue(description, description.contains("Cyclic patterns are not supported"));
    }

    @Test
    public void newNodeEndingOnBoundNodeIsRejectedWithRewriteHint() {
        String description = unsupported("match (p1:Person)-[own:Own]->(d1:Dragon), (p2:Person)-[know:Know]->(p1:Person) return *");
        assertFalse(description, description.contains("Cyclic"));
        assertTrue(description, description.contains("(p1)"));
    }

    @Test
    public void branchingFromBoundNodesStillTranslates() {
        AsgQuery query = translate("match (p1:Person)-[own:Own]-(d:Dragon), (p1:Person)-[member:MemberOf]-(g:Guild) return *");
        // every relation keeps an entity after it
        List<AsgEBase<Rel>> rels = AsgQueryUtil.elements(query, Rel.class);
        assertEquals(2, rels.size());
        for (AsgEBase<Rel> rel : rels) {
            assertTrue(rel.toString(), AsgQueryUtil.nextAdjacentDescendant(rel, EEntityBase.class).isPresent());
        }
    }
}
