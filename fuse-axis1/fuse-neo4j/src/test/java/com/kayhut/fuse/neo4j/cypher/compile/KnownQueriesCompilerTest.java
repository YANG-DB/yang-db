package com.kayhut.fuse.neo4j.cypher.compile;

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
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by EladW on 19/02/2017.
 */
public class KnownQueriesCompilerTest {

    Ontology ontology;
    AsgQuery asgQuery;

    @Before
    public void setUp() throws Exception {
        //ontology = loadOntology("dragons.json");
        asgQuery = new AsgQuerySupplier(loadQuery("Q003-1.json"),new NextEbaseFactory(), new BNextFactory() ).get();
    }

    @Test
    @Ignore
    public void shouldCompileV1Q3() {

        String cypher = CypherCompiler.compile(asgQuery, ontology);

        assertTrue(cypher.contains("B.first_name = 'Brandon'"));

    }

}
