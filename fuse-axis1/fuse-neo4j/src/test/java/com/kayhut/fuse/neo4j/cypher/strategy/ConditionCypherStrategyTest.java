package com.kayhut.fuse.neo4j.cypher.strategy;

import com.google.common.base.Supplier;
import com.kayhut.fuse.asg.builder.RecTwoPassAsgQuerySupplier;
import com.kayhut.fuse.model.asgQuery.AsgEBase;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.ontology.Ontology;
import com.kayhut.fuse.neo4j.cypher.CypherStatement;
import javaslang.Tuple2;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.util.HashMap;

import static com.kayhut.fuse.neo4j.cypher.TestUtils.loadOntology;
import static com.kayhut.fuse.neo4j.cypher.TestUtils.loadQuery;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Created by User on 26/03/2017.
 */
public class ConditionCypherStrategyTest {
    Ontology ontology;
    AsgQuery asgQuery;

    @Before
    public void setUp() throws Exception {
        //ontology = loadOntology("dragons.json");
        Supplier<AsgQuery> asgSupplier = new RecTwoPassAsgQuerySupplier(loadQuery("Q003-1.json"));
        asgQuery = asgSupplier.get();

    }

    @Test
    @Ignore
    public void apply() throws Exception {
        HashMap<AsgEBase, Tuple2<CypherStatement, String>> statementsMap = new HashMap<>();

        //todo: test only condition strategy or the whole compilation?
    }

}