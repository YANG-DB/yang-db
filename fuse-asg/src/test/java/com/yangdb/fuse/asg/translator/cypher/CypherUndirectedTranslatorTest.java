package com.yangdb.fuse.asg.translator.cypher;

import com.yangdb.fuse.model.asgQuery.AsgEBase;
import com.yangdb.fuse.model.asgQuery.AsgQuery;
import com.yangdb.fuse.model.asgQuery.AsgQueryUtil;
import com.yangdb.fuse.model.query.QueryInfo;
import com.yangdb.fuse.model.query.Rel;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;

import java.io.StringWriter;
import java.util.Collections;
import java.util.List;

import static com.yangdb.fuse.model.transport.CreateQueryRequestMetadata.TYPE_CYPHERQL;
import static org.junit.Assert.assertEquals;

/**
 * Q1 guard: the tutorial's undirected query must reach the planner as a both-directions ({@code RL}) relation.
 * The translator gets this right; Q1 (undirected executing as {@code ->}) lives in the execution layer.
 */
public class CypherUndirectedTranslatorTest {
    private CypherTranslator translator;

    @Before
    public void setUp() throws Exception {
        StringWriter writer = new StringWriter();
        IOUtils.copy(Thread.currentThread().getContextClassLoader().getResourceAsStream("Dragons_Ontology.json"), writer);
        CypherTestUtils utils = new CypherTestUtils().setUp(writer.toString());
        translator = new CypherTranslator(() -> Collections.singleton(utils.match));
    }

    private Rel.Direction directionOf(String cypher) {
        AsgQuery query = translator.translate(new QueryInfo<>(cypher, "q", TYPE_CYPHERQL, "Dragons"));
        List<AsgEBase<Rel>> rels = AsgQueryUtil.elements(query, Rel.class);
        assertEquals(1, rels.size());
        return rels.get(0).geteBase().getDir();
    }

    @Test
    public void undirectedPatternTranslatesToBothDirections() {
        assertEquals(Rel.Direction.RL, directionOf("match (p1:Person)-[know:Know]-(p2:Person {gender: 'FEMALE'}) return *"));
    }

    @Test
    public void directedPatternsKeepTheirDirection() {
        assertEquals(Rel.Direction.R, directionOf("match (p1:Person)-[know:Know]->(p2:Person {gender: 'FEMALE'}) return *"));
        assertEquals(Rel.Direction.L, directionOf("match (p1:Person)<-[know:Know]-(p2:Person {gender: 'FEMALE'}) return *"));
    }
}
