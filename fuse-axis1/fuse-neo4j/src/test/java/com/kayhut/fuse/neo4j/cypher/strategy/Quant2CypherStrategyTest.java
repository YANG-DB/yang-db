package com.kayhut.fuse.neo4j.cypher.strategy;

import com.kayhut.fuse.asg.builder.RecTwoPassAsgQuerySupplier;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.ontology.Ontology;
import com.kayhut.fuse.neo4j.cypher.CypherCompiler;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import static com.kayhut.fuse.neo4j.cypher.TestUtils.loadOntology;
import static com.kayhut.fuse.neo4j.cypher.TestUtils.loadQuery;
import static org.junit.Assert.*;

/**
 * Created by Elad on 4/2/2017.
 */
public class Quant2CypherStrategyTest {

    Ontology ontology;
    AsgQuery asgQuery11;
    AsgQuery asgQuery10;

    @Before
    public void setUp() throws Exception {
        //ontology = loadOntology("DragonsOntologyWithComposite.json");
        asgQuery11 = new RecTwoPassAsgQuerySupplier(loadQuery("Q011.json")).get();
        asgQuery10 = new RecTwoPassAsgQuerySupplier(loadQuery("Q010.json")).get();
    }

    @Test
    @Ignore
    public void query11CompileTest() throws Exception {
        String cypher = CypherCompiler.compile(asgQuery11, ontology);

        assertTrue(cypher.contains("MATCH p1 = (A)-[r2:knows]->(C:Person)-[r3:member_of]->(D)\n" +
                "WHERE r1.till = null AND r2.since >= '1011-01-01T00:00:00.000'"));


    }


    @Test
    @Ignore
    public void query10CompileTest() throws Exception {

        String cypher = CypherCompiler.compile(asgQuery10, ontology);

        //TODO: composite properties are not being processed correctly yet
        assertTrue(cypher.contains("MATCH p1 = (C)-[r4:freezes]->(F:Dragon)<-[r6:own]-(G)\n" +
                "WHERE A.name = 'Brandon' AND r2.tf >= '1010-01-01T00:00:00.000'"));
    }

}