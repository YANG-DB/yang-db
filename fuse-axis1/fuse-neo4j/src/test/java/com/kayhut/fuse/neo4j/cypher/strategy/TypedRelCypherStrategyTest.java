package com.kayhut.fuse.neo4j.cypher.strategy;

import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.ontology.EntityType;
import com.kayhut.fuse.model.ontology.Ontology;
import com.kayhut.fuse.model.ontology.RelationshipType;
import com.kayhut.fuse.model.query.Rel;
import com.kayhut.fuse.neo4j.cypher.CypherCompiler;
import org.junit.Assert;
import org.junit.Test;

import java.util.Collections;

import static com.kayhut.fuse.model.asgQuery.AsgQuery.Builder.rel;
import static com.kayhut.fuse.model.asgQuery.AsgQuery.Builder.unTyped;

/**
 * Created by Elad on 6/8/2017.
 */
public class TypedRelCypherStrategyTest {

    @Test
    public void testTypedRelStrategy() {

        AsgQuery query = AsgQuery.Builder.start("typed_rel", "dragons")
                .next(unTyped(1,"A"))
                .next(rel(2,"1", Rel.Direction.R))
                .next(unTyped(3,"B"))
                .build();

        Ontology ontology = Ontology.OntologyBuilder.anOntology()
                .withOnt("dragons")
                .withEntityTypes(
                        Collections.singletonList(new EntityType("1", "person", Collections.emptyList()))
                ).withRelationshipTypes(
                        Collections.singletonList(new RelationshipType("knows", "1", true))
                ).build();


        String cypher = CypherCompiler.compile(query ,ontology);

        Assert.assertEquals(cypher, "MATCH\n" +
                "p0 = (A)-[r1:knows]->(B)\n" +
                "RETURN A,r1,B\n");

    }

}