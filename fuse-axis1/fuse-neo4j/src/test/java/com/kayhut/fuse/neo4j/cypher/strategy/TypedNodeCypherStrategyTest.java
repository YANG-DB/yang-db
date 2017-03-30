package com.kayhut.fuse.neo4j.cypher.strategy;

import com.kayhut.fuse.model.asgQuery.AsgEBase;
import com.kayhut.fuse.model.ontology.Ontology;
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
public class TypedNodeCypherStrategyTest {
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

        ETyped eTyped = new ETyped();
        eTyped.seteTag("test");
        eTyped.seteType(1);
        eTyped.seteNum(1);
        AsgEBase<ETyped> asgEBase = new AsgEBase<>(eTyped);
        element.addNextChild(asgEBase);

        CypherCompilationState statement = new TypedNodeCypherStrategy(state, ontology).apply(asgEBase);
        assertNotNull(statement.getStatement());
        assertEquals(statement.getStatement().toString(), "MATCH p0 = (test:Person)\n" +
                "RETURN test\n");
    }

}