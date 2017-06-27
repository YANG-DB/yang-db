package com.kayhut.fuse.neo4j.cypher.strategy;

import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.ontology.EntityType;
import com.kayhut.fuse.model.ontology.Ontology;
import com.kayhut.fuse.neo4j.cypher.CypherCompiler;
import org.junit.Assert;
import org.junit.Test;

import java.util.Collections;

import static com.kayhut.fuse.model.asgQuery.AsgQuery.Builder.typed;

/**
 * Created by Elad on 6/1/2017.
 */
public class TypedNodeCypherStrategyTest {

    @Test
    public void testTypedNodeStrategy() {

        AsgQuery query = AsgQuery.Builder.start("typed", "dragons")
                                         .next(typed(1, "1", "A"))
                                         .build();

        Ontology ontology = Ontology.OntologyBuilder.anOntology()
                                                    .withOnt("dragons")
                                                    .withEntityTypes(
                                                            Collections.singletonList(new EntityType("1", "person", Collections.emptyList()))
                                                    ).build();


        String cypher = CypherCompiler.compile(query ,ontology);

        Assert.assertEquals(cypher, "MATCH\n" +
                                           "p0 = (A:person)\n" +
                                           "RETURN A\n");

    }

}