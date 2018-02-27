package com.kayhut.fuse.neo4j.cypher.compile;

import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.ontology.Ontology;
import com.kayhut.fuse.model.query.properties.constraint.Constraint;
import com.kayhut.fuse.model.query.properties.constraint.ConstraintOp;
import com.kayhut.fuse.model.query.properties.EProp;
import com.kayhut.fuse.model.query.quant.QuantType;
import com.kayhut.fuse.neo4j.cypher.CypherCompiler;
import com.kayhut.fuse.neo4j.cypher.TestUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

/**
 * Created by Elad on 6/28/2017.
 */
public class CompileSingleNodeTest {

    private Ontology ontology;

    @Before
    public void setup() throws IOException {
        ontology = TestUtils.loadOntology("Dragons.json");
    }

    @Test
    public void testCompileSingleNode() {

        AsgQuery query = AsgQuery.Builder.start("q1", "Dragons")
                .next(AsgQuery.Builder.unTyped(1)).build();

        String cypher = CypherCompiler.compile(query, ontology);

        Assert.assertEquals("MATCH\n" +
                "p0 = (n1)\n" +
                "RETURN n1\n", cypher);
    }

    @Test
    public void testCompileSingleNodeWithTag() {

        AsgQuery query = AsgQuery.Builder.start("q1", "Dragons")
                .next(AsgQuery.Builder.unTyped(1, "A")).build();

        String cypher = CypherCompiler.compile(query, ontology);

        Assert.assertEquals("MATCH\n" +
                "p0 = (A)\n" +
                "RETURN A\n", cypher);
    }

    @Test
    public void testCompileSingleNodeWithLabel() {

        AsgQuery query = AsgQuery.Builder.start("q1", "Dragons")
                .next(AsgQuery.Builder.typed(1, "Person")).build();

        String cypher = CypherCompiler.compile(query, ontology);

        Assert.assertEquals("MATCH\n" +
                "p0 = (n1:Person)\n" +
                "RETURN n1\n", cypher);
    }

    @Test
    public void testCompileSingleNodeWithTagAndLabel() {

        AsgQuery query = AsgQuery.Builder.start("q1", "Dragons")
                .next(AsgQuery.Builder.typed(1, "Person", "A")).build();

        String cypher = CypherCompiler.compile(query, ontology);

        Assert.assertEquals("MATCH\n" +
                "p0 = (A:Person)\n" +
                "RETURN A\n", cypher);
    }

    @Test
    public void testCompileSingleNodeWithPropertyFilter  () {

        AsgQuery query = AsgQuery.Builder.start("q1", "Dragons")
                .next(AsgQuery.Builder.typed(1, "Person", "A"))
                .next(AsgQuery.Builder.eProp(2, new EProp(2, "firstName", Constraint.of(ConstraintOp.eq, "John")))).build();

        String cypher = CypherCompiler.compile(query, ontology);

        Assert.assertEquals("MATCH\n" +
                "p0 = (A:Person)\n" +
                "WHERE A.firstName = 'John'\n" +
                "RETURN A\n", cypher);

    }

    @Test
    public void testCompileSingleNodeWithPropertiesAnd  () {

        AsgQuery query = AsgQuery.Builder.start("q1", "Dragons")
                .next(AsgQuery.Builder.typed(1, "Person", "A"))
                .next(AsgQuery.Builder.eProp(2, new EProp(2, "firstName", Constraint.of(ConstraintOp.eq, "John")),
                                                       new EProp(2, "height", Constraint.of(ConstraintOp.lt, 180)),
                                                       new EProp(2, "firstName", Constraint.of(ConstraintOp.ne, "Doe")))).build();

        String cypher = CypherCompiler.compile(query, ontology);

        Assert.assertEquals("MATCH\n" +
                "p0 = (A:Person)\n" +
                "WHERE A.firstName = 'John' AND A.height < 180 AND A.firstName  <>  'Doe'\n" +
                "RETURN A\n", cypher);

    }

    @Test
    public void testCompileSingleNodeWithPropertiesOr  () {

        AsgQuery query = AsgQuery.Builder.start("q1", "Dragons")
                .next(AsgQuery.Builder.typed(1, "Person", "A"))
                .next(AsgQuery.Builder.quant1(2, QuantType.some))
                .next(AsgQuery.Builder.eProp(2, new EProp(2, "firstName", Constraint.of(ConstraintOp.eq, "John")),
                        new EProp(2, "height", Constraint.of(ConstraintOp.lt, 180)),
                        new EProp(2, "firstName", Constraint.of(ConstraintOp.ne, "Doe")))).build();

        String cypher = CypherCompiler.compile(query, ontology);

        Assert.assertEquals("MATCH\n" +
                "p0 = (A:Person)\n" +
                "WHERE A.firstName = 'John' OR A.height < 180 OR A.firstName  <>  'Doe'\n" +
                "RETURN A\n", cypher);

    }
}
