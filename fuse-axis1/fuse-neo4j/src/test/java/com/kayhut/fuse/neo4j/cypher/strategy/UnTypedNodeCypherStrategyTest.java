package com.kayhut.fuse.neo4j.cypher.strategy;

import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.ontology.Ontology;
import com.kayhut.fuse.neo4j.cypher.CypherCompiler;
import org.junit.Assert;
import org.junit.Test;

import static com.kayhut.fuse.model.asgQuery.AsgQuery.Builder.unTyped;

/**
 * Created by Elad on 6/11/2017.
 */
public class UnTypedNodeCypherStrategyTest {

    @Test
    public void testUמTypedNodeStrategy() {

        AsgQuery query = AsgQuery.Builder.start("untyped", "dragons")
                .next(unTyped(1, "A"))
                .build();

        Ontology ontology = Ontology.OntologyBuilder.anOntology()
                .withOnt("dragons").build();

        String cypher = CypherCompiler.compile(query ,ontology);

        Assert.assertEquals(cypher, "MATCH\n" +
                "p0 = (A)\n" +
                "RETURN A\n");

    }

    @Test
    public void testUמTypedNodeStrategyNoTag() {

        AsgQuery query = AsgQuery.Builder.start("untyped", "dragons")
                .next(unTyped(1))
                .build();

        Ontology ontology = Ontology.OntologyBuilder.anOntology()
                .withOnt("dragons").build();

        String cypher = CypherCompiler.compile(query ,ontology);

        Assert.assertEquals(cypher, "MATCH\n" +
                "p0 = (n1)\n" +
                "RETURN n1\n");

    }

}