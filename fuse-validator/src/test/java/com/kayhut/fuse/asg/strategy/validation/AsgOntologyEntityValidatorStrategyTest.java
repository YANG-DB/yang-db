package com.kayhut.fuse.asg.strategy.validation;

import com.kayhut.fuse.dispatcher.utils.ValidationContext;
import com.kayhut.fuse.model.OntologyTestUtils;
import com.kayhut.fuse.model.OntologyTestUtils.HORSE;
import com.kayhut.fuse.model.OntologyTestUtils.PERSON;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.asgQuery.AsgStrategyContext;
import com.kayhut.fuse.model.ontology.Ontology;
import com.kayhut.fuse.model.query.properties.RelProp;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Date;

import static com.kayhut.fuse.model.OntologyTestUtils.START_DATE;
import static com.kayhut.fuse.model.asgQuery.AsgQuery.Builder.*;
import static com.kayhut.fuse.model.query.Constraint.of;
import static com.kayhut.fuse.model.query.ConstraintOp.eq;
import static com.kayhut.fuse.model.query.Rel.Direction.R;

/**
 * Created by liorp on 6/6/2017.
 */
public class AsgOntologyEntityValidatorStrategyTest {
    Ontology ontology;

    AsgQuery query = AsgQuery.Builder.start("Q1", "Dragons")
            .next(typed(1, PERSON.type))
            .next(rel(2, OntologyTestUtils.OWN.getrType(), R).below(relProp(10,
                    RelProp.of(START_DATE.type, 10, of(eq, new Date())))))
            .next(concrete(3, "HorseWithNoName",HORSE.type,"display","eTag"))
            .build();

    @Before
    public void setUp() throws Exception {
        ontology = OntologyTestUtils.createDragonsOntologyLong();
    }

    @Test
    public void testValidQuery() {
        AsgOntologyEntityValidatorStrategy strategy = new AsgOntologyEntityValidatorStrategy();
        ValidationContext validationContext = strategy.apply(query, new AsgStrategyContext(new Ontology.Accessor(ontology)));
        Assert.assertTrue(validationContext.valid());
    }

    @Test
    public void testNotValidConcreteEntityTypeQuery() {
        AsgQuery query = AsgQuery.Builder.start("Q1", "Dragons")
                .next(concrete(1, "no", "100", "eName", "eTag"))
                .next(rel(2, OntologyTestUtils.OWN.getrType(), R).below(relProp(10,
                        RelProp.of(START_DATE.type, 10, of(eq, new Date())))))
                .next(unTyped(3))
                .build();

        AsgOntologyEntityValidatorStrategy strategy = new AsgOntologyEntityValidatorStrategy();
        ValidationContext validationContext = strategy.apply(query, new AsgStrategyContext(new Ontology.Accessor(ontology)));
        Assert.assertFalse(validationContext.valid());
        Assert.assertTrue(validationContext.errors()[0].contains(AsgOntologyEntityValidatorStrategy.ERROR_1));
    }

    @Test
    public void testNotValidTypedEntityTypeQuery() {
        AsgQuery query = AsgQuery.Builder.start("Q1", "Dragons")
                .next(typed(1, PERSON.type))
                .next(rel(2, OntologyTestUtils.OWN.getrType(), R).below(relProp(10,
                        RelProp.of(START_DATE.type, 10, of(eq, new Date())))))
                .next(typed(3, "100"))
                .build();

        AsgOntologyEntityValidatorStrategy strategy = new AsgOntologyEntityValidatorStrategy();
        ValidationContext validationContext = strategy.apply(query, new AsgStrategyContext(new Ontology.Accessor(ontology)));

        Assert.assertFalse(validationContext.valid());
        Assert.assertTrue(validationContext.errors()[0].contains(AsgOntologyEntityValidatorStrategy.ERROR_1));
    }

    @Test
    public void testNotValidRelEntityTypeQuery() {
        AsgQuery query = AsgQuery.Builder.start("Q1", "Dragons")
                .next(typed(1, PERSON.type))
                .next(rel(2, "1000", R).below(relProp(10,
                        RelProp.of(START_DATE.type, 10, of(eq, new Date())))))
                .next(concrete(3, "HorseWithNoName",HORSE.type,"display","eTag"))
                .build();

        AsgOntologyEntityValidatorStrategy strategy = new AsgOntologyEntityValidatorStrategy();
        ValidationContext validationContext = strategy.apply(query, new AsgStrategyContext(new Ontology.Accessor(ontology)));

        Assert.assertFalse(validationContext.valid());
        Assert.assertTrue(validationContext.errors()[0].contains(AsgOntologyEntityValidatorStrategy.ERROR_2));
    }

}
