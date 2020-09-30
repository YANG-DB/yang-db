package com.yangdb.fuse.asg.strategy.mapping;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yangdb.fuse.dispatcher.ontology.SimpleOntologyMappingProvider;
import com.yangdb.fuse.dispatcher.ontology.SimpleOntologyProvider;
import com.yangdb.fuse.model.OntologyTestUtils;
import com.yangdb.fuse.model.asgQuery.AsgQuery;
import com.yangdb.fuse.model.execution.plan.descriptors.AsgQueryDescriptor;
import com.yangdb.fuse.model.ontology.Ontology;
import com.yangdb.fuse.model.ontology.mapping.MappingOntologies;
import com.yangdb.fuse.model.query.Query;
import com.yangdb.fuse.model.query.Rel;
import com.yangdb.fuse.model.query.Start;
import com.yangdb.fuse.model.query.entity.ETyped;
import com.yangdb.fuse.model.query.properties.EProp;
import com.yangdb.fuse.model.query.properties.EPropGroup;
import com.yangdb.fuse.model.query.properties.RelProp;
import com.yangdb.fuse.model.query.properties.constraint.Constraint;
import com.yangdb.fuse.model.query.properties.constraint.ConstraintOp;
import com.yangdb.fuse.model.query.properties.constraint.InnerQueryConstraint;
import com.yangdb.fuse.model.query.properties.constraint.WhereByConstraint;
import com.yangdb.fuse.model.query.quant.Quant1;
import com.yangdb.fuse.model.query.quant.QuantType;
import junit.framework.TestCase;
import org.junit.Assert;
import org.junit.Test;

import java.io.InputStream;
import java.util.Arrays;
import java.util.Date;

import static com.yangdb.fuse.model.OntologyTestUtils.*;
import static com.yangdb.fuse.model.asgQuery.AsgQuery.Builder.*;
import static com.yangdb.fuse.model.query.Rel.Direction.L;
import static com.yangdb.fuse.model.query.Rel.Direction.R;
import static com.yangdb.fuse.model.query.properties.constraint.Constraint.of;
import static com.yangdb.fuse.model.query.properties.constraint.ConstraintOp.eq;
import static com.yangdb.fuse.model.query.properties.constraint.ConstraintOp.gt;
import static com.yangdb.fuse.model.query.quant.QuantType.all;

public class AsgMappingStrategyTest extends TestCase {
    private ObjectMapper mapper;
    private AsgMappingStrategy strategy;


    @Override
    public void setUp() throws Exception {
        InputStream logicalDragonsSource = Thread.currentThread().getContextClassLoader().getResourceAsStream("mapping/LogicalDragons.json");
        InputStream schemaSource = Thread.currentThread().getContextClassLoader().getResourceAsStream("mapping/Dragons.json");
        InputStream mappingSource = Thread.currentThread().getContextClassLoader().getResourceAsStream("mapping/mapping.json");

        mapper = new ObjectMapper();
        Ontology ontology = mapper.readValue(logicalDragonsSource, Ontology.class);
        Ontology schema = mapper.readValue(schemaSource, Ontology.class);
        MappingOntologies mapping = mapper.readValue(mappingSource, MappingOntologies.class);

        strategy = new AsgMappingStrategy(new SimpleOntologyProvider(ontology,schema), new SimpleOntologyMappingProvider(mapping));
    }

    @Test
    public void testQ1TransformMapping() {
        AsgQuery query = Q1();
        strategy.apply(query, null);
        Assert.assertEquals("",AsgQueryDescriptor.print(query));
    }

    @Test
    public void testQ1_1TransformMapping() {
        AsgQuery query = Q1_1();
        strategy.apply(query, null);
        Assert.assertEquals("[└── Start, \n" +
                "    ──Typ[:Entity A#1]──Q[2:all]:{3}, \n" +
                "                                └─?[..][3], \n" +
                "                                      └─?[3]:[type<eq,0>]]", AsgQueryDescriptor.print(query));
    }

    private Query Q3() {
        Query query = Query.Builder.instance().withName("Q3").withOnt("LogicalDragons")
                .withElements(Arrays.asList(
                        new Start(0, 1),
                        new ETyped(1, "P", "Person", 2, 0),
                        new EPropGroup(2,
                                new EProp(3, "name", Constraint.of(ConstraintOp.inSet, Arrays.asList("jhon", "george", "jim"))))
                )).build();
        return query;
    }

    private AsgQuery Q2() {
        return AsgQuery.Builder.start("Q1", "LogicalDragons")
                .next(typed(1, OntologyTestUtils.PERSON.type))
                .next(ePropGroup(10, EProp.of(11, FIRST_NAME.type, of(eq, "Moshe"))))
                .next(rel(2, OntologyTestUtils.OWN.getrType(), R).below(relProp(20, RelProp.of(10, START_DATE.type, of(eq, new Date())))))
                .next(concrete(3, "HorseWithNoName", OntologyTestUtils.HORSE.type, "display", "eTag"))
                .next(ePropGroup(12, EProp.of(13, NAME.type, of(eq, "bubu"))))
                .build();

    }

    private AsgQuery Q1() {
        return AsgQuery.Builder.start("Q2", "LogicalDragons")
                .next(typed(1, "Person", "A"))
                .next(quant1(2, all))
                .in(ePropGroup(3, EProp.of(3, "name", Constraint.of(ConstraintOp.like, "a")),
                        EProp.of(3, "Age", Constraint.of(gt, 13))))
                .build();
    }

    private AsgQuery Q1_1() {
        return AsgQuery.Builder.start("Q1_1", "LogicalDragons")
                .next(typed(1, "Person", "A"))
                .next(quant1(2, all))
                .in(ePropGroup(3, EProp.of(3, "type", Constraint.of(ConstraintOp.like, "a*"))))
                .build();
    }

    private AsgQuery Q4() {
        return AsgQuery.Builder.start("Q4", "LogicalDragons")
                .next(typed(1, "entity1", "A"))
                .next(rel(2, "rel1", R,"R").below(relProp(2, RelProp.of(2, "2", Constraint.of(eq, "value2")))))
                .next(typed(3, "entity2", "B"))
                .next(quant1(4, all))
                .in(ePropGroup(5, EProp.of(5, "prop1", Constraint.of(eq, "value1")), EProp.of(5, "prop2", Constraint.of(gt, "value3"))),
                        rel(6, "rel2", R,"R1").next(typed(7, "entity3", "C")),
                        optional(11).next(rel(12, "rel4", R,"R2").next(typed(13, "entity4", "E")
                                .next(optional(14).next(rel(15, "rel5", R,"R2").next(typed(16, "entity4", "F")))))))
                .build();
    }



}