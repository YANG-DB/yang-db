package com.kayhut.fuse.neo4j.cypher.strategy;

import com.kayhut.fuse.model.asgQuery.AsgEBase;
import com.kayhut.fuse.model.ontology.Ontology;
import com.kayhut.fuse.model.query.Rel;
import com.kayhut.fuse.model.query.Start;
import com.kayhut.fuse.model.query.entity.ETyped;
import com.kayhut.fuse.neo4j.cypher.CypherCompilationState;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static com.kayhut.fuse.neo4j.cypher.TestUtils.loadOntology;
import static org.junit.Assert.*;

/**
 * Created by User on 26/03/2017.
 */
public class TypedRelCypherStrategyTest {
    Ontology ontology;

    @Before
    public void setUp() throws Exception {
        ontology = loadOntology("dragons.json");

    }

    @Test
    public void apply() throws Exception {
        AsgEBase element = new AsgEBase(new Start());
        Map<AsgEBase, CypherCompilationState> state = new HashMap<>();
        new InitializeCypherStrategy(state, ontology).apply(element);

        Rel rel = new Rel();
        rel.setrType(1);
        rel.seteNum(1);
        AsgEBase<ETyped> asgEBase = new AsgEBase(rel);
        element.addNextChild(asgEBase);

        CypherCompilationState updatedState = new TypedRelCypherStrategy(state, ontology).apply(asgEBase);
        assertNotNull(updatedState.getStatement());
        assertEquals(updatedState.getStatement().toString(), "MATCH p0 = -[:Person]-\n" +
                "RETURN null\n");

    }

}