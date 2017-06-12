package com.kayhut.fuse.neo4j.cypher.strategy;

import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.ontology.EntityType;
import com.kayhut.fuse.model.ontology.Ontology;
import com.kayhut.fuse.neo4j.cypher.CypherCompiler;
import org.junit.Assert;
import org.junit.Test;

import java.util.Collections;

import static com.kayhut.fuse.model.asgQuery.AsgQuery.Builder.concrete;

/**
 * Created by Elad on 6/8/2017.
 */
public class ConcreteNodeCypherStrategyTest {

    @Test
    public void testConcretedNodeStrategy() {

        AsgQuery query = AsgQuery.Builder.start("concrete", "dragons")
                .next(concrete(1,"id123", 1,"name", "A"))
                .build();

        Ontology ontology = Ontology.OntologyBuilder.anOntology()
                .withOnt("dragons")
                .withEntityTypes(
                        Collections.singletonList(new EntityType(1, "person", Collections.emptyList()))
                ).build();

        String cypher = CypherCompiler.compile(query ,ontology);

        Assert.assertEquals(cypher, "MATCH\n" +
                "p0 = (A)\n" +
                "WHERE id(A) = id123\n" +
                "RETURN A\n");

    }

}