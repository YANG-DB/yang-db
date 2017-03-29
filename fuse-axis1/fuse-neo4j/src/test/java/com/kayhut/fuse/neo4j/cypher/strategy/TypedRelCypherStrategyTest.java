package com.kayhut.fuse.neo4j.cypher.strategy;

import com.kayhut.fuse.model.asgQuery.AsgEBase;
import com.kayhut.fuse.model.ontology.Ontology;
import com.kayhut.fuse.model.query.Rel;
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
public class TypedRelCypherStrategyTest {
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

        Rel rel = new Rel();
        rel.setrType(1);
        rel.seteNum(1);
        AsgEBase<ETyped> asgEBase = new AsgEBase(rel);
        element.addNextChild(asgEBase);

        CypherStatement statement = new TypedRelCypherStrategy(ontology, statementsMap).apply(asgEBase);
        assertNotNull(statement);
        assertEquals(statement.toString(), "MATCH p0 = -[:Person]-\n" +
                "RETURN null\n");

    }

}