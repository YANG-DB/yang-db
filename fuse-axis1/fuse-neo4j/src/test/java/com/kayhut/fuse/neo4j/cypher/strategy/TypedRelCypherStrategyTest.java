package com.kayhut.fuse.neo4j.cypher.strategy;

import com.kayhut.fuse.asg.builder.RecTwoPassAsgQuerySupplier;
import com.kayhut.fuse.model.asgQuery.AsgEBase;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.ontology.Ontology;
import com.kayhut.fuse.model.query.Rel;
import com.kayhut.fuse.model.query.Start;
import com.kayhut.fuse.model.query.entity.ETyped;
import com.kayhut.fuse.neo4j.cypher.CypherCompilationState;
import com.kayhut.fuse.neo4j.cypher.CypherCompiler;
import com.kayhut.fuse.neo4j.cypher.CypherCondition;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static com.kayhut.fuse.neo4j.cypher.TestUtils.loadOntology;
import static com.kayhut.fuse.neo4j.cypher.TestUtils.loadQuery;
import static org.junit.Assert.*;

/**
 * Created by User on 26/03/2017.
 */
public class TypedRelCypherStrategyTest {

    Ontology ontology;
    AsgQuery asgQuery;

    @Before
    public void setUp() throws Exception {
        //ontology = loadOntology("dragons.json");
        asgQuery = new RecTwoPassAsgQuerySupplier(loadQuery("Q190-1.json")).get();
    }

    @Test
    @Ignore
    public void apply() throws Exception {

        String cypher = CypherCompiler.compile(asgQuery, ontology);

        assertTrue(cypher.contains("MATCH p0 = (A:Person)-[r1:own]->(B:dragon)"));

    }

}