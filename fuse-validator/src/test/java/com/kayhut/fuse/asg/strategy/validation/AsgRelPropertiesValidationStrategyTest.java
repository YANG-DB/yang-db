package com.kayhut.fuse.asg.strategy.validation;

import com.kayhut.fuse.dispatcher.utils.ValidationContext;
import com.kayhut.fuse.model.OntologyTestUtils;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.asgQuery.AsgStrategyContext;
import com.kayhut.fuse.model.ontology.Ontology;
import com.kayhut.fuse.model.query.properties.EProp;
import com.kayhut.fuse.model.query.properties.RelProp;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Date;

import static com.kayhut.fuse.model.OntologyTestUtils.*;
import static com.kayhut.fuse.model.asgQuery.AsgQuery.Builder.*;
import static com.kayhut.fuse.model.query.Constraint.of;
import static com.kayhut.fuse.model.query.ConstraintOp.eq;
import static com.kayhut.fuse.model.query.Rel.Direction.R;

/**
 * Created by liorp on 6/6/2017.
 */
public class AsgRelPropertiesValidationStrategyTest {
    Ontology ontology;

    AsgQuery query = AsgQuery.Builder.start("Q1", "Dragons")
            .next(typed(1, PERSON.type))
            .next(eProp(10, EProp.of(FIRST_NAME.type,11,of(eq, "Moshe"))))
            .next(rel(2, OntologyTestUtils.OWN.getrType(), R).below(relProp(10,
                    RelProp.of(START_DATE.type, 10, of(eq, new Date())))))
            .next(concrete(3, "HorseWithNoName", HORSE.type,"display","eTag"))
            .next(eProp(12, EProp.of(NAME.type,13,of(eq, "bubu"))))
            .build();

    @Before
    public void setUp() throws Exception {
        ontology = OntologyTestUtils.createDragonsOntologyLong();
    }

    @Test
    public void testValidQuery() {
        AsgRelPropertiesValidatorStrategy strategy = new AsgRelPropertiesValidatorStrategy();
        ValidationContext validationContext = strategy.apply(query, new AsgStrategyContext(new Ontology.Accessor(ontology)));
        Assert.assertTrue(validationContext.valid());
    }

    @Test
    public void testNotValidPropEntityMismatchQuery() {
        AsgQuery query = AsgQuery.Builder.start("Q1", "Dragons")
                .next(typed(1, PERSON.type))
                .next(eProp(10, EProp.of(COLOR.type,11,of(eq, "Moshe"))))
                .next(rel(2, OntologyTestUtils.OWN.getrType(), R).below(relProp(10,
                        RelProp.of(FIRST_NAME.type, 10, of(eq, new Date())))))
                .next(concrete(3, "HorseWithNoName", HORSE.type,"display","eTag"))
                .next(eProp(12, EProp.of(NAME.type,13,of(eq, "bubu"))))
                .build();

        AsgRelPropertiesValidatorStrategy strategy = new AsgRelPropertiesValidatorStrategy();
        ValidationContext validationContext = strategy.apply(query, new AsgStrategyContext(new Ontology.Accessor(ontology)));
        Assert.assertFalse(validationContext.valid());
        Assert.assertTrue(validationContext.errors()[0].contains(AsgRelPropertiesValidatorStrategy.ERROR_2));
    }

    @Test
    public void testNotValidPropRelMismatchQuery() {
        AsgQuery query = AsgQuery.Builder.start("Q1", "Dragons")
                .next(typed(1, PERSON.type))
                .next(eProp(10, EProp.of(FIRST_NAME.type,11,of(eq, "Moshe"))))
                .next(rel(2, OntologyTestUtils.OWN.getrType(), R).below(relProp(10,
                        RelProp.of(START_DATE.type, 10, of(eq, new Date())),
                        RelProp.of(NAME.type, 11, of(eq, new Date())))))
                .next(unTyped(3, HORSE.type, DRAGON.type))
                .next(eProp(12, EProp.of(FIRST_NAME.type,13,of(eq, "bubu"))))
                .build();

        AsgRelPropertiesValidatorStrategy strategy = new AsgRelPropertiesValidatorStrategy();
        ValidationContext validationContext = strategy.apply(query, new AsgStrategyContext(new Ontology.Accessor(ontology)));
        Assert.assertFalse(validationContext.valid());
        Assert.assertTrue(validationContext.errors()[0].contains(AsgRelPropertiesValidatorStrategy.ERROR_2));
    }


}
