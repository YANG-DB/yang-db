package com.kayhut.fuse.neo4j.cypher.strategy;

import com.kayhut.fuse.asg.builder.RecTwoPassAsgQuerySupplier;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.ontology.Ontology;
import com.kayhut.fuse.neo4j.cypher.CypherCompiler;
import org.junit.Before;
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
        ontology = loadOntology("DragonsOntologyWithComposite.json");
        asgQuery11 = new RecTwoPassAsgQuerySupplier(loadQuery("Q011.json")).get();
        asgQuery10 = new RecTwoPassAsgQuerySupplier(loadQuery("Q010.json")).get();
    }

    @Test
    public void query11CompileTest() throws Exception {
        String cypher = CypherCompiler.compile(asgQuery11, ontology);

        assertTrue(cypher.contains("MATCH p1 = (A)-[r2:knows]->(C:Person)-[r3:member_of]->(D)\n" +
                "WHERE r1.till = null AND r2.since >= '1011-01-01T00:00:00.000'"));


    }


    @Test
    public void query10CompileTest() throws Exception {

        String cypher = CypherCompiler.compile(asgQuery10, ontology);

        //todo Elad, see if the result of this compilation is the correct one.
        //assertTrue(cypher.contains("MATCH p1 = (C)-[r4:freezes]->(F:Dragon)<-[r6:own]-(G)\n" +
        //        "WHERE A.first_name = 'Brandon' AND r2.time >= '1010-01-01T00:00:00.000'"));
    }

}