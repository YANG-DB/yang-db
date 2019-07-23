package com.yangdb.fuse.asg.strategy.validation;

import com.yangdb.fuse.asg.validation.AsgEntityDuplicateETagValidatorStrategy;
import com.yangdb.fuse.model.OntologyTestUtils;
import com.yangdb.fuse.model.OntologyTestUtils.HORSE;
import com.yangdb.fuse.model.OntologyTestUtils.PERSON;
import com.yangdb.fuse.model.asgQuery.AsgQuery;
import com.yangdb.fuse.model.asgQuery.AsgStrategyContext;
import com.yangdb.fuse.model.ontology.Ontology;
import com.yangdb.fuse.model.query.properties.RelProp;
import com.yangdb.fuse.model.validation.ValidationResult;
import javaslang.collection.Stream;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Date;

import static com.yangdb.fuse.model.OntologyTestUtils.START_DATE;
import static com.yangdb.fuse.model.asgQuery.AsgQuery.Builder.*;
import static com.yangdb.fuse.model.query.Rel.Direction.R;
import static com.yangdb.fuse.model.query.properties.constraint.Constraint.of;
import static com.yangdb.fuse.model.query.properties.constraint.ConstraintOp.eq;

/**
 * Created by lior.perry on 6/6/2017.
 */
public class AsgEntityDuplicateETagsValidatorStrategyTest {
    Ontology ontology;

    AsgQuery query = AsgQuery.Builder.start("Q1", "Dragons")
            .next(typed(1, PERSON.type,"P"))
            .next(rel(2, OntologyTestUtils.OWN.getrType(), R).below(
                    relProp(10,
                        RelProp.of(10, START_DATE.type, of(eq, new Date())),
                        RelProp.of(11, START_DATE.type, of(eq, new Date())))))
            .next(concrete(3, "HorseWithNoName",HORSE.type,"display","H"))
            .build();

    @Before
    public void setUp() throws Exception {
        ontology = OntologyTestUtils.createDragonsOntologyLong();
    }

    @Test
    public void testValidQuery() {
        AsgEntityDuplicateETagValidatorStrategy strategy = new AsgEntityDuplicateETagValidatorStrategy();
        ValidationResult validationResult = strategy.apply(query, new AsgStrategyContext(new Ontology.Accessor(ontology)));
        Assert.assertTrue(validationResult.valid());
    }

    @Test
    public void testNotValidConcreteEntityTypeQuery() {
        AsgQuery query = AsgQuery.Builder.start("Q1", "Dragons")
                .next(concrete(1, "no", "100", "eName", "eTag"))
                .next(rel(2, OntologyTestUtils.OWN.getrType(), R).below(relProp(10,
                        RelProp.of(10, START_DATE.type, of(eq, new Date())))))
                .next(unTyped(3,"eTag"))
                .build();

        AsgEntityDuplicateETagValidatorStrategy strategy = new AsgEntityDuplicateETagValidatorStrategy();
        ValidationResult validationResult = strategy.apply(query, new AsgStrategyContext(new Ontology.Accessor(ontology)));
        Assert.assertFalse(validationResult.valid());
        Assert.assertTrue(Stream.ofAll(validationResult.errors()).toJavaArray(String.class)[0].contains(AsgEntityDuplicateETagValidatorStrategy.ERROR_1));
    }

    @Test
    public void testNotValidTypedEntityTypeQuery() {
        AsgQuery query = AsgQuery.Builder.start("Q1", "Dragons")
                .next(typed(1, PERSON.type,"P"))
                .next(rel(2, OntologyTestUtils.OWN.getrType(), R).below(relProp(10,
                        RelProp.of(10, START_DATE.type, of(eq, new Date())))))
                .next(typed(3, "100","P"))
                .next(rel(4, OntologyTestUtils.OWN.getrType(), R).below(relProp(10,
                        RelProp.of(10, START_DATE.type, of(eq, new Date())))))
                .build();

        AsgEntityDuplicateETagValidatorStrategy strategy = new AsgEntityDuplicateETagValidatorStrategy();
        ValidationResult validationResult = strategy.apply(query, new AsgStrategyContext(new Ontology.Accessor(ontology)));
        Assert.assertFalse(validationResult.valid());
        Assert.assertTrue(Stream.ofAll(validationResult.errors()).toJavaArray(String.class)[0].contains(AsgEntityDuplicateETagValidatorStrategy.ERROR_1));
    }
}
