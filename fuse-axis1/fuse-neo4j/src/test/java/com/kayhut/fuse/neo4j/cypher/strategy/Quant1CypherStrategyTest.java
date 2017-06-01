package com.kayhut.fuse.neo4j.cypher.strategy;

import com.kayhut.fuse.dispatcher.asg.AsgQuerySupplier;
import com.kayhut.fuse.dispatcher.asg.builder.BNextFactory;
import com.kayhut.fuse.dispatcher.asg.builder.NextEbaseFactory;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.ontology.Ontology;
import com.kayhut.fuse.neo4j.cypher.CypherCompiler;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import static com.kayhut.fuse.neo4j.cypher.TestUtils.loadQuery;
import static org.junit.Assert.*;

/**
 * Created by Elad on 4/2/2017.
 */
public class Quant1CypherStrategyTest {

    Ontology ontology;
    AsgQuery asgQuery;

    @Before
    public void setUp() throws Exception {
        //ontology = loadOntology("dragons.json");
        asgQuery = new AsgQuerySupplier(loadQuery("Q008.json"),new NextEbaseFactory(), new BNextFactory() ).get();
    }

    @Test
    @Ignore
    public void apply() throws Exception {

        String cypher = CypherCompiler.compile(asgQuery, ontology);

        assertTrue(cypher.contains("UNION\nMATCH p0 = (A:Person)"));
    }
}