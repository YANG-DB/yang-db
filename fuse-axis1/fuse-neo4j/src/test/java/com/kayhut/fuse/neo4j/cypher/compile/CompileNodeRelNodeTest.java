package com.kayhut.fuse.neo4j.cypher.compile;

import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.ontology.Ontology;
import com.kayhut.fuse.model.query.properties.constraint.Constraint;
import com.kayhut.fuse.model.query.properties.constraint.ConstraintOp;
import com.kayhut.fuse.model.query.Rel;
import com.kayhut.fuse.model.query.properties.EProp;
import com.kayhut.fuse.model.query.properties.RelProp;
import com.kayhut.fuse.model.query.quant.QuantType;
import com.kayhut.fuse.neo4j.cypher.CypherCompiler;
import com.kayhut.fuse.neo4j.cypher.TestUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

/**
 * Created by Elad on 6/29/2017.
 */
public class CompileNodeRelNodeTest {

    private Ontology ontology;

    @Before
    public void setup() throws IOException {
        ontology = TestUtils.loadOntology("Dragons.json");
    }

    @Test
    public void testCompileNodeRelNode() {

        AsgQuery query = AsgQuery.Builder.start("q1", "Dragons")
                                         .next(AsgQuery.Builder.unTyped(1))
                                         .next(AsgQuery.Builder.rel(2, "owns", Rel.Direction.R))
                                         .next(AsgQuery.Builder.unTyped(3)).build();

        String cypher = CypherCompiler.compile(query, ontology);

        Assert.assertEquals("MATCH\n" +
                "p0 = (n1)-[r1:owns]->(n2)\n" +
                "RETURN n1,r1,n2\n", cypher);
    }

    @Test
    public void testCompileNodeRelNodeWithTags() {

        AsgQuery query = AsgQuery.Builder.start("q1", "Dragons")
                .next(AsgQuery.Builder.unTyped(1, "A"))
                .next(AsgQuery.Builder.rel(2, "owns", Rel.Direction.R))
                .next(AsgQuery.Builder.unTyped(3, "B")).build();

        String cypher = CypherCompiler.compile(query, ontology);

        Assert.assertEquals("MATCH\n" +
                "p0 = (A)-[r1:owns]->(B)\n" +
                "RETURN A,r1,B\n", cypher);
    }

    @Test
    public void testCompileNodeRelNodeWithTypes() {

        AsgQuery query = AsgQuery.Builder.start("q1", "Dragons")
                .next(AsgQuery.Builder.typed(1, "Person"))
                .next(AsgQuery.Builder.rel(2, "know", Rel.Direction.R))
                .next(AsgQuery.Builder.typed(3, "Person")).build();

        String cypher = CypherCompiler.compile(query, ontology);

        Assert.assertEquals("MATCH\n" +
                "p0 = (n1:Person)-[r1:know]->(n2:Person)\n" +
                "RETURN n1,r1,n2\n", cypher);
    }

    @Test
    public void testCompileNodeRelNodeWithTypesAndTags() {

        AsgQuery query = AsgQuery.Builder.start("q1", "Dragons")
                .next(AsgQuery.Builder.typed(1, "Person", "A"))
                .next(AsgQuery.Builder.rel(2, "know", Rel.Direction.R))
                .next(AsgQuery.Builder.typed(3, "Person", "B")).build();

        String cypher = CypherCompiler.compile(query, ontology);

        Assert.assertEquals("MATCH\n" +
                "p0 = (A:Person)-[r1:know]->(B:Person)\n" +
                "RETURN A,r1,B\n", cypher);
    }

    @Test
    public void testCompileNodeRelNodeWithRelFilter() {

        AsgQuery query = AsgQuery.Builder.start("q1", "Dragons")
                .next(AsgQuery.Builder.typed(1, "Dragon", "A"))
                .next(AsgQuery.Builder.rel(2, "fire", Rel.Direction.R)
                                            .below(AsgQuery.Builder.eProp(3, new EProp(2, "temperature", Constraint.of(ConstraintOp.gt, "1000")),
                                                                                    new EProp(2, "temperature", Constraint.of(ConstraintOp.lt, "5000")))))
                .next(AsgQuery.Builder.typed(4, "Dragon", "B")).build();

        String cypher = CypherCompiler.compile(query, ontology);

        Assert.assertEquals("MATCH\n" +
                "p0 = (A:Dragon)-[r1:fire]->(B:Dragon)\n" +
                "WHERE r1.temperature > 1000 AND r1.temperature < 5000\n" +
                "RETURN A,r1,B\n", cypher);
    }

    @Test
    public void testCompileNodeRelNodeWithLastNodeFilter() {

        AsgQuery query = AsgQuery.Builder.start("q1", "Dragons")
                .next(AsgQuery.Builder.typed(1, "Dragon", "A"))
                .next(AsgQuery.Builder.rel(2, "fire", Rel.Direction.R))
                .next(AsgQuery.Builder.typed(4, "Dragon", "B"))
                .next(AsgQuery.Builder.eProp(5, new EProp(2, "firstName", Constraint.of(ConstraintOp.eq, "John")))).build();

        String cypher = CypherCompiler.compile(query, ontology);

        Assert.assertEquals("MATCH\n" +
                "p0 = (A:Dragon)-[r1:fire]->(B:Dragon)\n" +
                "WHERE B.firstName = 'John'\n" +
                "RETURN A,r1,B\n", cypher);
    }

    @Test
    public void testCompileNodeRelNodeWithFirstNodeFilter() {
        AsgQuery query = AsgQuery.Builder.start("q1", "Dragons")
                .next(AsgQuery.Builder.typed(1, "Dragon", "A"))
                .next(AsgQuery.Builder.quant1(2, QuantType.all).next(AsgQuery.Builder.eProp(5, new EProp(3, "firstName", Constraint.of(ConstraintOp.eq, "John")))))
                .next(AsgQuery.Builder.rel(4, "fire", Rel.Direction.R))
                .next(AsgQuery.Builder.typed(5, "Dragon", "B"))
                .build();

        String cypher = CypherCompiler.compile(query, ontology);

        Assert.assertEquals("MATCH\n" +
                "p1 = (A:Dragon)-[r1:fire]->(B:Dragon)\n" +
                "WHERE A.firstName = 'John'\n" +
                "RETURN A,r1,B\n", cypher);
    }

    @Test
    public void testCompileNodeRelNodeWithBothNodesFilters() {
        AsgQuery query = AsgQuery.Builder.start("q1", "Dragons")
                .next(AsgQuery.Builder.typed(1, "Dragon", "A"))
                .next(AsgQuery.Builder.quant1(2, QuantType.all).next(AsgQuery.Builder.eProp(5, new EProp(3, "firstName", Constraint.of(ConstraintOp.eq, "John")))))
                .next(AsgQuery.Builder.rel(4, "fire", Rel.Direction.R))
                .next(AsgQuery.Builder.typed(5, "Dragon", "B"))
                .next(AsgQuery.Builder.eProp(6, new EProp(2, "firstName", Constraint.of(ConstraintOp.eq, "John"))))
                .build();

        String cypher = CypherCompiler.compile(query, ontology);

        Assert.assertEquals("MATCH\n" +
                "p1 = (A:Dragon)-[r1:fire]->(B:Dragon)\n" +
                "WHERE A.firstName = 'John' AND B.firstName = 'John'\n" +
                "RETURN A,r1,B\n", cypher);
    }

    @Test
    public void testCompileNodeRelNodeWithBothNodesAndRelFilters() {
        AsgQuery query = AsgQuery.Builder.start("q1", "Dragons")
                .next(AsgQuery.Builder.typed(1, "Dragon", "A"))
                .next(AsgQuery.Builder.quant1(2, QuantType.all).next(AsgQuery.Builder.eProp(5, new EProp(3, "firstName", Constraint.of(ConstraintOp.eq, "John")))))
                .next(AsgQuery.Builder.rel(4, "fire", Rel.Direction.R).below(AsgQuery.Builder.relProp(5, RelProp.of(5, "temperature", Constraint.of(ConstraintOp.ge, 100)))))
                .next(AsgQuery.Builder.typed(6, "Dragon", "B"))
                .next(AsgQuery.Builder.eProp(7, new EProp(2, "firstName", Constraint.of(ConstraintOp.eq, "John"))))
                .build();

        String cypher = CypherCompiler.compile(query, ontology);

        Assert.assertEquals("MATCH\n" +
                "p1 = (A:Dragon)-[r1:fire]->(B:Dragon)\n" +
                "WHERE A.firstName = 'John' AND r1.temperature >= 100 AND B.firstName = 'John'\n" +
                "RETURN A,r1,B\n", cypher);
    }
}
