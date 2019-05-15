package com.kayhut.fuse.asg.strategy.validation;

import com.kayhut.fuse.asg.validation.AsgEntityPropertiesValidatorStrategy;
import com.kayhut.fuse.asg.validation.AsgRelPropertiesValidatorStrategy;
import com.kayhut.fuse.model.OntologyTestUtils;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.asgQuery.AsgStrategyContext;
import com.kayhut.fuse.model.ontology.Ontology;
import com.kayhut.fuse.model.query.properties.EProp;
import com.kayhut.fuse.model.query.properties.RelProp;
import com.kayhut.fuse.model.query.properties.constraint.Constraint;
import com.kayhut.fuse.model.query.properties.constraint.ConstraintOp;
import com.kayhut.fuse.model.query.quant.QuantType;
import com.kayhut.fuse.model.validation.ValidationResult;
import javaslang.collection.Stream;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Date;

import static com.kayhut.fuse.model.OntologyTestUtils.*;
import static com.kayhut.fuse.model.asgQuery.AsgQuery.Builder.*;
import static com.kayhut.fuse.model.query.Rel.Direction.R;
import static com.kayhut.fuse.model.query.properties.constraint.Constraint.of;
import static com.kayhut.fuse.model.query.properties.constraint.ConstraintOp.eq;

/**
 * Created by lior.perry on 6/6/2017.
 */
public class AsgRelPropertiesValidationStrategyTest {
    Ontology ontology;

    AsgQuery query = AsgQuery.Builder.start("Q1", "Dragons")
            .next(typed(1, PERSON.type))
            .next(ePropGroup(10, EProp.of(11, FIRST_NAME.type, of(eq, "Moshe"))))
            .next(rel(2, OntologyTestUtils.OWN.getrType(), R).below(relProp(10,
                    RelProp.of(10, START_DATE.type, of(eq, new Date())))))
            .next(concrete(3, "HorseWithNoName", HORSE.type,"display","eTag"))
            .next(ePropGroup(12, EProp.of(13, NAME.type, of(eq, "bubu"))))
            .build();

    @Before
    public void setUp() throws Exception {
        ontology = OntologyTestUtils.createDragonsOntologyLong();
    }

    @Test
    public void testValidQuery() {
        AsgRelPropertiesValidatorStrategy strategy = new AsgRelPropertiesValidatorStrategy();
        ValidationResult validationResult = strategy.apply(query, new AsgStrategyContext(new Ontology.Accessor(ontology)));
        Assert.assertTrue(validationResult.valid());
    }

    @Test
    public void testNotValidPropEntityMismatchQuery() {
        AsgQuery query = AsgQuery.Builder.start("Q1", "Dragons")
                .next(typed(1, PERSON.type))
                .next(ePropGroup(10, EProp.of(11, COLOR.type, of(eq, "Moshe"))))
                .next(rel(2, OntologyTestUtils.OWN.getrType(), R).below(relProp(10,
                        RelProp.of(10, FIRST_NAME.type, of(eq, new Date())))))
                .next(concrete(3, "HorseWithNoName", HORSE.type,"display","eTag"))
                .next(ePropGroup(12, EProp.of(13, NAME.type, of(eq, "bubu"))))
                .build();

        AsgRelPropertiesValidatorStrategy strategy = new AsgRelPropertiesValidatorStrategy();
        ValidationResult validationResult = strategy.apply(query, new AsgStrategyContext(new Ontology.Accessor(ontology)));
        Assert.assertFalse(validationResult.valid());
        Assert.assertTrue(Stream.ofAll(validationResult.errors()).toJavaArray(String.class)[0].contains(AsgRelPropertiesValidatorStrategy.ERROR_2));
    }

    @Test
    public void testNotValidPropRelMismatchQuery() {
        AsgQuery query = AsgQuery.Builder.start("Q1", "Dragons")
                .next(typed(1, PERSON.type))
                .next(ePropGroup(10, EProp.of(11, FIRST_NAME.type, of(eq, "Moshe"))))
                .next(rel(2, OntologyTestUtils.OWN.getrType(), R).below(relProp(10,
                        RelProp.of(10, START_DATE.type, of(eq, new Date())),
                        RelProp.of(11, NAME.type, of(eq, new Date())))))
                .next(unTyped(3, HORSE.type, DRAGON.type))
                .next(ePropGroup(12, EProp.of(13, FIRST_NAME.type, of(eq, "bubu"))))
                .build();

        AsgRelPropertiesValidatorStrategy strategy = new AsgRelPropertiesValidatorStrategy();
        ValidationResult validationResult = strategy.apply(query, new AsgStrategyContext(new Ontology.Accessor(ontology)));
        Assert.assertFalse(validationResult.valid());
        Assert.assertTrue(Stream.ofAll(validationResult.errors()).toJavaArray(String.class)[0].contains(AsgRelPropertiesValidatorStrategy.ERROR_2));
    }

    @Test
    public void testNotValidQuantPropGroupEntityMismatchQuery() {
        AsgQuery query = AsgQuery.Builder.start("Q1", "Dragons")
                .next(typed(1, PERSON.type))
                .next(rel(2, OntologyTestUtils.OWN.getrType(), R))
                .next(quant1(3, QuantType.all))
                .in(ePropGroup(4, EProp.of(5, NAME.type, Constraint.of(ConstraintOp.le,"abc"))),
                        rel(6, OntologyTestUtils.OWN.getrType(), R).below(relProp(10,RelProp.of(10, START_DATE.type, of(eq, new Date()))))
                                .next(concrete(7, "HorseWithNoName", HORSE.type,"display","eTag"))
                                .next(ePropGroup(12, EProp.of(13, NAME.type, of(eq, "bubu")))))
                .build();

        AsgEntityPropertiesValidatorStrategy strategy = new AsgEntityPropertiesValidatorStrategy();
        ValidationResult validationResult = strategy.apply(query, new AsgStrategyContext(new Ontology.Accessor(ontology)));
        Assert.assertFalse(validationResult.valid());
        Assert.assertTrue(Stream.ofAll(validationResult.errors()).toJavaArray(String.class)[0].contains(AsgEntityPropertiesValidatorStrategy.ERROR_1));
    }
    @Test
    public void testNotValidQuantPropEntityMismatchQuery() {
        AsgQuery query = AsgQuery.Builder.start("Q1", "Dragons")
                .next(typed(1, PERSON.type))
                .next(rel(2, OntologyTestUtils.OWN.getrType(), R))
                .next(quant1(3, QuantType.all))
                .in(eProp(4, NAME.type, Constraint.of(ConstraintOp.le,"abc")),
                        rel(6, OntologyTestUtils.OWN.getrType(), R).below(relProp(10,RelProp.of(10, START_DATE.type, of(eq, new Date()))))
                                .next(concrete(7, "HorseWithNoName", HORSE.type,"display","eTag"))
                                .next(ePropGroup(12, EProp.of(13, NAME.type, of(eq, "bubu")))))
                .build();

        AsgEntityPropertiesValidatorStrategy strategy = new AsgEntityPropertiesValidatorStrategy();
        ValidationResult validationResult = strategy.apply(query, new AsgStrategyContext(new Ontology.Accessor(ontology)));
        Assert.assertFalse(validationResult.valid());
        Assert.assertTrue(Stream.ofAll(validationResult.errors()).toJavaArray(String.class)[0].contains(AsgEntityPropertiesValidatorStrategy.ERROR_1));
    }

    @Test
    public void testNoIntervalTypePropRelQuery() {
        AsgQuery query = AsgQuery.Builder.start("Q1", "Dragons")
                .next(typed(1, PERSON.type))
                .next(ePropGroup(10, EProp.of(11, FIRST_NAME.type, of(eq, "Moshe"))))
                .next(rel(2, OntologyTestUtils.OWN.getrType(), R).below(relProp(10,
                        RelProp.of(10, START_DATE.type, of(eq, new Date(),null)),
                        RelProp.of(11, NAME.type, of(eq, new Date())))))
                .next(unTyped(3, HORSE.type, DRAGON.type))
                .next(ePropGroup(12, EProp.of(13, FIRST_NAME.type, of(eq, "bubu"))))
                .build();

        AsgRelPropertiesValidatorStrategy strategy = new AsgRelPropertiesValidatorStrategy();
        ValidationResult validationResult = strategy.apply(query, new AsgStrategyContext(new Ontology.Accessor(ontology)));
        Assert.assertFalse(validationResult.valid());
        Assert.assertEquals(Stream.ofAll(validationResult.errors()).toJavaArray(String.class).length, 2);
    }


}
