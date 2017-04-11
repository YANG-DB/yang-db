package com.kayhut.fuse.neo4j.cypher.strategy;

import com.google.common.base.Supplier;
import com.kayhut.fuse.asg.builder.RecTwoPassAsgQuerySupplier;
import com.kayhut.fuse.model.asgQuery.AsgEBase;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.ontology.Ontology;
import com.kayhut.fuse.neo4j.cypher.CypherCompilationState;
import com.kayhut.fuse.neo4j.cypher.CypherCompiler;
import com.kayhut.fuse.neo4j.cypher.CypherStatement;
import javaslang.Tuple2;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;

import static com.kayhut.fuse.neo4j.cypher.TestUtils.loadOntology;
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
        ontology = loadOntology("dragons.json");
        asgQuery = new RecTwoPassAsgQuerySupplier(loadQuery("Q008.json")).get();
    }

    @Test
    public void apply() throws Exception {

        String cypher = CypherCompiler.compile(asgQuery, ontology);

        assertTrue(cypher.contains("UNION\nMATCH p0 = (A:Person)"));
    }
}