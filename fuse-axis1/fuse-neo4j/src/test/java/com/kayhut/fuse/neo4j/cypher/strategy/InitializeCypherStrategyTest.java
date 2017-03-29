package com.kayhut.fuse.neo4j.cypher.strategy;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kayhut.fuse.model.asgQuery.AsgEBase;
import com.kayhut.fuse.model.ontology.Ontology;
import com.kayhut.fuse.model.query.EBase;
import com.kayhut.fuse.model.query.Start;
import com.kayhut.fuse.neo4j.cypher.CypherStatement;
import javaslang.Tuple2;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;

import static com.kayhut.fuse.neo4j.cypher.strategy.TestUtils.loadOntology;
import static org.junit.Assert.*;

/**
 * Created by User on 26/03/2017.
 */
public class InitializeCypherStrategyTest {
    Ontology ontology;

    @Before
    public void setUp() throws Exception {
        ontology = loadOntology("dragons.json");
    }

    @Test
    public void apply() throws Exception {
        CypherStatement statement = new InitializeCypherStrategy(ontology, new HashMap<>()).apply(new AsgEBase(new Start()));
        Assert.assertNotNull(statement.toString());
    }


}