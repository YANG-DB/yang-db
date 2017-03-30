package com.kayhut.fuse.neo4j.cypher.strategy;

import com.kayhut.fuse.model.asgQuery.AsgEBase;
import com.kayhut.fuse.model.ontology.Ontology;
import com.kayhut.fuse.model.query.Start;
import com.kayhut.fuse.neo4j.cypher.CypherCompilationState;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;

import static com.kayhut.fuse.neo4j.cypher.TestUtils.loadOntology;

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
        InitializeCypherStrategy initializeCypherStrategy = new InitializeCypherStrategy(new HashMap<>(), ontology);
        CypherCompilationState state = initializeCypherStrategy.apply(new AsgEBase(new Start()));
        Assert.assertNotNull(state.getStatement().toString());
    }


}