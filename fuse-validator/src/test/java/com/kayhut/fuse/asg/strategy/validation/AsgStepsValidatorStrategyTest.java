package com.kayhut.fuse.asg.strategy.validation;

import com.kayhut.fuse.asg.strategy.AsgStepsValidatorStrategy;
import com.kayhut.fuse.model.validation.ValidationResult;
import com.kayhut.fuse.model.OntologyTestUtils;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.asgQuery.AsgStrategyContext;
import com.kayhut.fuse.model.ontology.Ontology;
import com.kayhut.fuse.model.query.Constraint;
import com.kayhut.fuse.model.query.ConstraintOp;
import com.kayhut.fuse.model.query.Rel;
import com.kayhut.fuse.model.query.properties.EProp;
import com.kayhut.fuse.model.query.properties.RelProp;
import com.kayhut.fuse.model.query.quant.QuantType;
import javaslang.collection.Stream;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Date;

import static com.kayhut.fuse.model.OntologyTestUtils.*;
import static com.kayhut.fuse.model.asgQuery.AsgQuery.Builder.*;
import static com.kayhut.fuse.model.query.Constraint.of;
import static com.kayhut.fuse.model.query.ConstraintOp.*;
import static com.kayhut.fuse.model.query.Rel.Direction.R;
import static com.kayhut.fuse.model.query.quant.QuantType.all;

/**
 * Created by liorp on 6/6/2017.
 */
public class AsgStepsValidatorStrategyTest {
    Ontology ontology;

    AsgQuery query = AsgQuery.Builder.start("Q1", "Dragons")
            .next(unTyped(1))
            .next(rel(2, OntologyTestUtils.OWN.getrType(), R).below(relProp(10,
                    RelProp.of(START_DATE.type, 10, of(eq, new Date())))))
            .next(unTyped(3))
            .build();

    @Before
    public void setUp() throws Exception {
        ontology = OntologyTestUtils.createDragonsOntologyLong();
    }

    @Test
    public void testValidQuery() {
        AsgStepsValidatorStrategy strategy = new AsgStepsValidatorStrategy();
        ValidationResult validationResult = strategy.apply(query, new AsgStrategyContext(new Ontology.Accessor(ontology)));
        Assert.assertTrue(validationResult.valid());
    }

    @Test
    public void testStepNoRelQuery() {
        AsgQuery query = AsgQuery.Builder.start("Q1", "Dragons")
                .next(unTyped(1))
                .next(unTyped(3))
                .build();

        AsgStepsValidatorStrategy strategy = new AsgStepsValidatorStrategy();
        ValidationResult validationResult = strategy.apply(query, new AsgStrategyContext(new Ontology.Accessor(ontology)));
        Assert.assertFalse(validationResult.valid());
        Assert.assertTrue(Stream.ofAll(validationResult.errors()).toJavaArray(String.class)[0].contains(AsgStepsValidatorStrategy.ERROR_1));
    }

    @Test
    public void testStepWithPropsNoRelQuery() {
        AsgQuery query = AsgQuery.Builder.start("Q1", "Dragons")
                .next(unTyped(1))
                .next(eProp(10, EProp.of(COLOR.type, 11, of(eq, "Moshe"))))
                .next(unTyped(3))
                .next(eProp(12, EProp.of(NAME.type, 13, of(eq, "bubu"))))
                .build();

        AsgStepsValidatorStrategy strategy = new AsgStepsValidatorStrategy();
        ValidationResult validationResult = strategy.apply(query, new AsgStrategyContext(new Ontology.Accessor(ontology)));
        Assert.assertFalse(validationResult.valid());
        Assert.assertTrue(Stream.ofAll(validationResult.errors()).toJavaArray(String.class)[0].contains(AsgStepsValidatorStrategy.ERROR_1));
    }

    @Test
    public void testStepWithPropsNoEntityQuery() {
        AsgQuery query = AsgQuery.Builder.start("Q1", "Dragons")
                .next(rel(1, FREEZE.getrType(), R))
                .below(relProp(2, RelProp.of(START_DATE.type, 3, Constraint.of(ge, new Date(System.currentTimeMillis())))))
                .next(rel(4, FREEZE.getrType(), R))
                .below(relProp(5, RelProp.of(START_DATE.type, 6, Constraint.of(ge, new Date(System.currentTimeMillis())))))
                .next(eProp(7, EProp.of(NAME.type, 8, of(eq, "bubu"))))
                .build();

        AsgStepsValidatorStrategy strategy = new AsgStepsValidatorStrategy();
        ValidationResult validationResult = strategy.apply(query, new AsgStrategyContext(new Ontology.Accessor(ontology)));
        Assert.assertFalse(validationResult.valid());
        Assert.assertTrue(Stream.ofAll(validationResult.errors()).toJavaArray(String.class)[0].contains(AsgStepsValidatorStrategy.ERROR_2));
    }

    @Test
    public void testStepWithPropsNoEntityLongQuery() {
        AsgQuery query = AsgQuery.Builder.start("Q1", "Dragons")
                .next(rel(1, FREEZE.getrType(), R))
                .next(quant1(2, QuantType.all))
                .in(eProp(3, EProp.of(FIRST_NAME.type, 3, Constraint.of(ConstraintOp.eq, "abc"))),
                        rel(4, OWN.getrType(), Rel.Direction.R).below(relProp(5))
                        .next(rel(1, FREEZE.getrType(), R))
                        .next(typed(10, GUILD.type).next(eProp(11))))
                .build();

        AsgStepsValidatorStrategy strategy = new AsgStepsValidatorStrategy();
        ValidationResult validationResult = strategy.apply(query, new AsgStrategyContext(new Ontology.Accessor(ontology)));
        Assert.assertFalse(validationResult.valid());
        Assert.assertTrue(Stream.ofAll(validationResult.errors()).toJavaArray(String.class)[0].contains(AsgStepsValidatorStrategy.ERROR_2));
    }

    @Test
    public void testStepWithPropsNoRelLongQuery() {
        AsgQuery query = AsgQuery.Builder.start("Q1", "Dragons").
                next(typed(1, OntologyTestUtils.PERSON.type)).
                next(quant1(2, QuantType.all)).
                in(eProp(3, EProp.of(FIRST_NAME.type, 3, Constraint.of(ConstraintOp.eq, "abc"))),
                        rel(4, OWN.getrType(), Rel.Direction.R).below(relProp(5)).
                                next(typed(6, DRAGON.type)
                                        .next(eProp(7))).
                                next(typed(10, GUILD.type).next(eProp(11)))).
                build();

        AsgStepsValidatorStrategy strategy = new AsgStepsValidatorStrategy();
        ValidationResult validationResult = strategy.apply(query, new AsgStrategyContext(new Ontology.Accessor(ontology)));
        Assert.assertFalse(validationResult.valid());
        Assert.assertTrue(Stream.ofAll(validationResult.errors()).toJavaArray(String.class)[0].contains(AsgStepsValidatorStrategy.ERROR_1));
    }

    @Test
    public void testStepWithPropsNoRelLongerQuery() {
        AsgQuery query = AsgQuery.Builder.start("Q1", "Dragons")
                .next(typed(1, PERSON.type)
                        .next(eProp(2, EProp.of(HEIGHT.type, 3, Constraint.of(ConstraintOp.gt, 189L)))))
                .next(rel(4, OWN.getrType(), R)
                        .below(relProp(5, RelProp.of(START_DATE.type, 6, Constraint.of(ge, new Date(System.currentTimeMillis()))))))
                .next(typed(7, DRAGON.type))
                .next(quant1(8, all))
                .in(eProp(9, EProp.of(NAME.type, 10, Constraint.of(ge, "smith")))
                        , rel(12, FREEZE.getrType(), R)
                                .below(relProp(122))
                                .next(unTyped(13)
                                        .next(eProp(14, EProp.of(NAME.type, 15, Constraint.of(ConstraintOp.notContains, "bob"))))
                                )
                                .below(relProp(18, RelProp.of(START_DATE.type, 19,
                                        Constraint.of(ge, new Date(System.currentTimeMillis() - 1000 * 60))),
                                        RelProp.of(END_DATE.type, 19, Constraint.of(le, new Date(System.currentTimeMillis() + 1000 * 60)))))
                                .next(concrete(20, "smoge", DRAGON.type, "Display:smoge", "D")
                                        .next(eProp(21, EProp.of(NAME.type, 22, Constraint.of(ConstraintOp.eq, "smoge"))))
                                )
                ).build();

        AsgStepsValidatorStrategy strategy = new AsgStepsValidatorStrategy();
        ValidationResult validationResult = strategy.apply(query, new AsgStrategyContext(new Ontology.Accessor(ontology)));
        Assert.assertFalse(validationResult.valid());
        Assert.assertTrue(Stream.ofAll(validationResult.errors()).toJavaArray(String.class)[0].contains(AsgStepsValidatorStrategy.ERROR_1));
    }

}
