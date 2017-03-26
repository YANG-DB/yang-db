package com.kayhut.fuse.neo4j.cypher.strategy;

import com.kayhut.fuse.model.asgQuery.AsgEBase;
import com.kayhut.fuse.model.ontology.Ontology;
import com.kayhut.fuse.model.query.Start;
import com.kayhut.fuse.model.query.entity.ETyped;
import com.kayhut.fuse.neo4j.cypher.CypherStatement;
import javaslang.Tuple2;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;

import static com.kayhut.fuse.neo4j.cypher.strategy.TestUtils.loadOntology;
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
        HashMap<AsgEBase, Tuple2<CypherStatement, String>> statementsMap = new HashMap<>();
        new InitializeCypherStrategy(ontology, statementsMap).apply(element);

        ETyped eTyped = new ETyped();
        eTyped.seteTag("test");
        eTyped.seteType(1);
        eTyped.seteNum(1);
        AsgEBase<ETyped> asgEBase = new AsgEBase<>(eTyped);
        element.addNextChild(asgEBase);

        CypherStatement statement = new TypedNodeCypherStrategy(ontology, statementsMap).apply(asgEBase);
        assertNotNull(statement);
        assertEquals(statement.toString(), "MATCH p0 = (test:Person)\n" +
                "RETURN test\n");
    }

}