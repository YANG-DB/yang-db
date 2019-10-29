package com.yangdb.fuse.asg.strategy.validation;

import com.yangdb.fuse.asg.validation.AsgConstraintExpressionValidatorStrategy;
import com.yangdb.fuse.model.OntologyTestUtils;
import com.yangdb.fuse.model.asgQuery.AsgQuery;
import com.yangdb.fuse.model.asgQuery.AsgStrategyContext;
import com.yangdb.fuse.model.ontology.Ontology;
import com.yangdb.fuse.model.query.aggregation.AggLOp;
import com.yangdb.fuse.model.query.properties.CalculatedEProp;
import com.yangdb.fuse.model.query.properties.EProp;
import com.yangdb.fuse.model.query.properties.RelProp;
import com.yangdb.fuse.model.query.properties.constraint.ParameterizedConstraint;
import com.yangdb.fuse.model.query.properties.projection.CalculatedFieldProjection;
import com.yangdb.fuse.model.query.properties.projection.IdentityProjection;
import com.yangdb.fuse.model.validation.ValidationResult;
import javaslang.collection.Stream;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;

import static com.yangdb.fuse.model.OntologyTestUtils.*;
import static com.yangdb.fuse.model.asgQuery.AsgQuery.Builder.*;
import static com.yangdb.fuse.model.query.Rel.Direction.R;
import static com.yangdb.fuse.model.query.properties.constraint.Constraint.of;
import static com.yangdb.fuse.model.query.properties.constraint.ConstraintOp.*;

/**
 * Created by lior.perry on 6/6/2017.
 */
public class AsgConstraintExpressionValidationStrategyTest {
    Ontology ontology;


    @Before
    public void setUp() throws Exception {
        ontology = OntologyTestUtils.createDragonsOntologyLong();
    }

    @Test
    public void testValidQuery() {
        AsgQuery query = AsgQuery.Builder.start("Q1", "Dragons")
                .next(typed(1, PERSON.type,"p"))
                .next(ePropGroup(10,
                        EProp.of(11, FIRST_NAME.type, of(eq, "Moshe")),
                        CalculatedEProp.of(101, "p->eTag", new CalculatedFieldProjection(AggLOp.count))))
                .next(rel(2, OntologyTestUtils.OWN.getrType(), R)
                        .below(relProp(10, RelProp.of(10, START_DATE.type, of(eq, new Date())))))
                .next(concrete(3, "HorseWithNoName", HORSE.type,"display","eTag"))
                .next(ePropGroup(12, EProp.of(13, NAME.type, of(eq, "bubu"))))
                .build();

        AsgConstraintExpressionValidatorStrategy strategy = new AsgConstraintExpressionValidatorStrategy();
        ValidationResult validationResult = strategy.apply(query, new AsgStrategyContext(new Ontology.Accessor(ontology)));
        Assert.assertTrue(validationResult.valid());
    }

    @Test
    public void testValidPropParameterConstraint() {
        AsgQuery query = AsgQuery.Builder.start("Q1", "Dragons")
                .next(typed(1, PERSON.type))
                .next(ePropGroup(10,
                        EProp.of(11, NAME.type, new ParameterizedConstraint())))
                .next(rel(2, KNOW.getrType(), R))
                .next(typed(1, PERSON.type))
                .build();

        AsgConstraintExpressionValidatorStrategy strategy = new AsgConstraintExpressionValidatorStrategy();
        ValidationResult validationResult = strategy.apply(query, new AsgStrategyContext(new Ontology.Accessor(ontology)));
        Assert.assertTrue(validationResult.valid());
    }
    @Test
    public void testValidPropConstraintIdentityProj() {
        AsgQuery query = AsgQuery.Builder.start("Q1", "Dragons")
                .next(typed(1, PERSON.type))
                .next(ePropGroup(10,
                        EProp.of(11, NAME.type, new IdentityProjection())))
                .next(rel(2, KNOW.getrType(), R))
                .next(typed(1, PERSON.type))
                .build();

        AsgConstraintExpressionValidatorStrategy strategy = new AsgConstraintExpressionValidatorStrategy();
        ValidationResult validationResult = strategy.apply(query, new AsgStrategyContext(new Ontology.Accessor(ontology)));
        Assert.assertTrue(validationResult.valid());
    }
    @Test
    public void testValidPropConstraintCalcField() {
        AsgQuery query = AsgQuery.Builder.start("Q1", "Dragons")
                .next(typed(1, PERSON.type))
                .next(ePropGroup(10,
                        CalculatedEProp.of(101, "p->eTag", new CalculatedFieldProjection(AggLOp.count))))
                .next(rel(2, KNOW.getrType(), R))
                .next(typed(1, PERSON.type))
                .build();

        AsgConstraintExpressionValidatorStrategy strategy = new AsgConstraintExpressionValidatorStrategy();
        ValidationResult validationResult = strategy.apply(query, new AsgStrategyContext(new Ontology.Accessor(ontology)));
        Assert.assertTrue(validationResult.valid());
    }

    @Test
    public void testNotValidPropConstraintEqNull() {
        AsgQuery query = AsgQuery.Builder.start("Q1", "Dragons")
                .next(typed(1, PERSON.type))
                .next(ePropGroup(10,
                        EProp.of(11, NAME.type, of(eq, null))))
                .next(rel(2, KNOW.getrType(), R))
                .next(typed(1, PERSON.type))
                .build();

        AsgConstraintExpressionValidatorStrategy strategy = new AsgConstraintExpressionValidatorStrategy();
        ValidationResult validationResult = strategy.apply(query, new AsgStrategyContext(new Ontology.Accessor(ontology)));
        Assert.assertFalse(validationResult.valid());
        Assert.assertEquals(Stream.ofAll(validationResult.errors()).toJavaArray(String.class)[0],
                (String.format(AsgConstraintExpressionValidatorStrategy.ERROR_1,eq.name(),NAME.type)));
    }
    @Test
    public void testValidPropConstraintNoConstraintValue() {
        AsgQuery query = AsgQuery.Builder.start("Q1", "Dragons")
                .next(typed(1, PERSON.type))
                .next(ePropGroup(10,
                        EProp.of(11, NAME.type, of(empty))))
                .next(rel(2, KNOW.getrType(), R))
                .next(typed(1, PERSON.type))
                .build();

        AsgConstraintExpressionValidatorStrategy strategy = new AsgConstraintExpressionValidatorStrategy();
        ValidationResult validationResult = strategy.apply(query, new AsgStrategyContext(new Ontology.Accessor(ontology)));
        Assert.assertTrue(validationResult.valid());
    }

    @Test
    public void testNotValidPropConstraintNoConstraintValue() {
        AsgQuery query = AsgQuery.Builder.start("Q1", "Dragons")
                .next(typed(1, PERSON.type))
                .next(ePropGroup(10,
                        EProp.of(11, NAME.type, of(empty,"StringValue"))))
                .next(rel(2, KNOW.getrType(), R))
                .next(typed(1, PERSON.type))
                .build();

        AsgConstraintExpressionValidatorStrategy strategy = new AsgConstraintExpressionValidatorStrategy();
        ValidationResult validationResult = strategy.apply(query, new AsgStrategyContext(new Ontology.Accessor(ontology)));
        Assert.assertFalse(validationResult.valid());
        Assert.assertEquals(Stream.ofAll(validationResult.errors()).toJavaArray(String.class)[0],
                (String.format(AsgConstraintExpressionValidatorStrategy.ERROR_1,empty.name(),NAME.type)));
    }

    @Test
    public void testNotValidPropConstraintSetNull() {
        AsgQuery query = AsgQuery.Builder.start("Q1", "Dragons")
                .next(typed(1, PERSON.type))
                .next(ePropGroup(10,
                        EProp.of(11, NAME.type, of(inSet, null))))
                .next(rel(2, KNOW.getrType(), R))
                .next(typed(1, PERSON.type))
                .build();

        AsgConstraintExpressionValidatorStrategy strategy = new AsgConstraintExpressionValidatorStrategy();
        ValidationResult validationResult = strategy.apply(query, new AsgStrategyContext(new Ontology.Accessor(ontology)));
        Assert.assertFalse(validationResult.valid());
        Assert.assertEquals(Stream.ofAll(validationResult.errors()).toJavaArray(String.class)[0],
                (String.format(AsgConstraintExpressionValidatorStrategy.ERROR_1,inSet.name(),NAME.type)));
    }
    @Test
    public void testNotValidPropConstraintSetNonArray() {
        AsgQuery query = AsgQuery.Builder.start("Q1", "Dragons")
                .next(typed(1, PERSON.type))
                .next(eProp(11, NAME.type, of(inSet, "SingleValue")))
                .next(rel(2, KNOW.getrType(), R))
                .next(typed(1, PERSON.type))
                .build();

        AsgConstraintExpressionValidatorStrategy strategy = new AsgConstraintExpressionValidatorStrategy();
        ValidationResult validationResult = strategy.apply(query, new AsgStrategyContext(new Ontology.Accessor(ontology)));
        Assert.assertFalse(validationResult.valid());
        Assert.assertEquals(Stream.ofAll(validationResult.errors()).toJavaArray(String.class)[0],
                (String.format(AsgConstraintExpressionValidatorStrategy.ERROR_1,inSet.name(),NAME.type)));
    }
    @Test
    public void testValidPropConstraintSetArray() {
        AsgQuery query = AsgQuery.Builder.start("Q1", "Dragons")
                .next(typed(1, PERSON.type))
                .next(eProp(11, NAME.type, of(inSet, Arrays.asList("SingleValue"))))
                .next(rel(2, KNOW.getrType(), R))
                .next(typed(1, PERSON.type))
                .build();

        AsgConstraintExpressionValidatorStrategy strategy = new AsgConstraintExpressionValidatorStrategy();
        ValidationResult validationResult = strategy.apply(query, new AsgStrategyContext(new Ontology.Accessor(ontology)));
        Assert.assertTrue(validationResult.valid());
    }

    @Test
    public void testNotValidPropConstraintRangeNull() {
        AsgQuery query = AsgQuery.Builder.start("Q1", "Dragons")
                .next(typed(1, PERSON.type))
                .next(ePropGroup(10,
                        EProp.of(11, BIRTH_DATE.type, of(inRange, null))))
                .next(rel(2, KNOW.getrType(), R))
                .next(typed(1, PERSON.type))
                .build();

        AsgConstraintExpressionValidatorStrategy strategy = new AsgConstraintExpressionValidatorStrategy();
        ValidationResult validationResult = strategy.apply(query, new AsgStrategyContext(new Ontology.Accessor(ontology)));
        Assert.assertFalse(validationResult.valid());
        Assert.assertEquals(Stream.ofAll(validationResult.errors()).toJavaArray(String.class)[0],
                (String.format(AsgConstraintExpressionValidatorStrategy.ERROR_1,inRange.name(),BIRTH_DATE.type)));
    }
    @Test
    public void testNotValidPropConstraintRangeSingleValue() {
        AsgQuery query = AsgQuery.Builder.start("Q1", "Dragons")
                .next(typed(1, PERSON.type))
                .next(ePropGroup(10,
                        EProp.of(11, BIRTH_DATE.type, of(inRange, "SingleValue"))))
                .next(rel(2, KNOW.getrType(), R))
                .next(typed(1, PERSON.type))
                .build();

        AsgConstraintExpressionValidatorStrategy strategy = new AsgConstraintExpressionValidatorStrategy();
        ValidationResult validationResult = strategy.apply(query, new AsgStrategyContext(new Ontology.Accessor(ontology)));
        Assert.assertFalse(validationResult.valid());
        Assert.assertEquals(Stream.ofAll(validationResult.errors()).toJavaArray(String.class)[0],
                (String.format(AsgConstraintExpressionValidatorStrategy.ERROR_1,inRange.name(),BIRTH_DATE.type)));
    }
    @Test
    public void testNotValidPropConstraintRangeSingleValueList() {
        AsgQuery query = AsgQuery.Builder.start("Q1", "Dragons")
                .next(typed(1, PERSON.type))
                .next(ePropGroup(10,
                        EProp.of(11, BIRTH_DATE.type, of(inRange, Collections.singletonList("SingleValue")))))
                .next(rel(2, KNOW.getrType(), R))
                .next(typed(1, PERSON.type))
                .build();

        AsgConstraintExpressionValidatorStrategy strategy = new AsgConstraintExpressionValidatorStrategy();
        ValidationResult validationResult = strategy.apply(query, new AsgStrategyContext(new Ontology.Accessor(ontology)));
        Assert.assertFalse(validationResult.valid());
        Assert.assertEquals(Stream.ofAll(validationResult.errors()).toJavaArray(String.class)[0],
                (String.format(AsgConstraintExpressionValidatorStrategy.ERROR_1,inRange.name(),BIRTH_DATE.type)));
    }

    @Test
    public void testValidPropConstraintRangeValueList() {
        AsgQuery query = AsgQuery.Builder.start("Q1", "Dragons")
                .next(typed(1, PERSON.type))
                .next(ePropGroup(10,
                        EProp.of(11, BIRTH_DATE.type, of(inRange, Arrays.asList("FirstValue","SecondValue")))))
                .next(rel(2, KNOW.getrType(), R))
                .next(typed(1, PERSON.type))
                .build();

        AsgConstraintExpressionValidatorStrategy strategy = new AsgConstraintExpressionValidatorStrategy();
        ValidationResult validationResult = strategy.apply(query, new AsgStrategyContext(new Ontology.Accessor(ontology)));
        Assert.assertTrue(validationResult.valid());
    }

    @Test
    public void testNotValidPropConstraintRangePartial() {
        AsgQuery query = AsgQuery.Builder.start("Q1", "Dragons")
                .next(typed(1, PERSON.type))
                .next(ePropGroup(10,
                        EProp.of(11, BIRTH_DATE.type, of(inRange, Arrays.asList(new Date(),new Date(),new Date())))))
                .next(rel(2, KNOW.getrType(), R))
                .next(typed(1, PERSON.type))
                .build();

        AsgConstraintExpressionValidatorStrategy strategy = new AsgConstraintExpressionValidatorStrategy();
        ValidationResult validationResult = strategy.apply(query, new AsgStrategyContext(new Ontology.Accessor(ontology)));
        Assert.assertFalse(validationResult.valid());
        Assert.assertEquals(Stream.ofAll(validationResult.errors()).toJavaArray(String.class)[0],
                (String.format(AsgConstraintExpressionValidatorStrategy.ERROR_1,inRange.name(),BIRTH_DATE.type)));
    }

}
