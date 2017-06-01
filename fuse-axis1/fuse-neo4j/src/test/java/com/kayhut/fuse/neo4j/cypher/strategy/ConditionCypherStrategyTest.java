package com.kayhut.fuse.neo4j.cypher.strategy;

import com.google.common.base.Supplier;
import com.kayhut.fuse.dispatcher.asg.AsgQuerySupplier;
import com.kayhut.fuse.dispatcher.asg.builder.BNextFactory;
import com.kayhut.fuse.dispatcher.asg.builder.NextEbaseFactory;
import com.kayhut.fuse.model.asgQuery.AsgEBase;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.ontology.Ontology;
import com.kayhut.fuse.neo4j.cypher.CypherStatement;
import javaslang.Tuple2;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.util.HashMap;

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
        Supplier<AsgQuery> asgSupplier = new AsgQuerySupplier(loadQuery("Q003-1.json"),new NextEbaseFactory(), new BNextFactory() );
        asgQuery = asgSupplier.get();

    }

    @Test
    @Ignore
    public void apply() throws Exception {
        HashMap<AsgEBase, Tuple2<CypherStatement, String>> statementsMap = new HashMap<>();

        //todo: test only condition strategy or the whole compilation?
    }

}