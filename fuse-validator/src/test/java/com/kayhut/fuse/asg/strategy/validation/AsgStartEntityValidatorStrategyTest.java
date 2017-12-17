package com.kayhut.fuse.asg.strategy.validation;

import com.kayhut.fuse.asg.strategy.AsgStartEntityValidatorStrategy;
import com.kayhut.fuse.model.validation.ValidationResult;
import com.kayhut.fuse.dispatcher.utils.AsgQueryUtil;
import com.kayhut.fuse.model.OntologyTestUtils;
import com.kayhut.fuse.model.asgQuery.AsgEBase;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.asgQuery.AsgStrategyContext;
import com.kayhut.fuse.model.ontology.Ontology;
import com.kayhut.fuse.model.query.Start;
import com.kayhut.fuse.model.query.properties.RelProp;
import javaslang.collection.Stream;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Date;

import static com.kayhut.fuse.model.OntologyTestUtils.START_DATE;
import static com.kayhut.fuse.model.asgQuery.AsgQuery.Builder.rel;
import static com.kayhut.fuse.model.asgQuery.AsgQuery.Builder.relProp;
import static com.kayhut.fuse.model.asgQuery.AsgQuery.Builder.unTyped;
import static com.kayhut.fuse.model.query.Constraint.of;
import static com.kayhut.fuse.model.query.ConstraintOp.eq;
import static com.kayhut.fuse.model.query.Rel.Direction.R;

/**
 * Created by liorp on 6/6/2017.
 */
public class AsgStartEntityValidatorStrategyTest {
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
        AsgStartEntityValidatorStrategy strategy = new AsgStartEntityValidatorStrategy();
        ValidationResult validationResult = strategy.apply(query, new AsgStrategyContext(new Ontology.Accessor(ontology)));
        Assert.assertTrue(validationResult.valid());
    }

    @Test
    public void testNameMismatchQuery() {
        AsgStartEntityValidatorStrategy strategy = new AsgStartEntityValidatorStrategy();
        query.setOnt("bela");
        ValidationResult validationResult = strategy.apply(query, new AsgStrategyContext(new Ontology.Accessor(ontology)));
        Assert.assertFalse(validationResult.valid());
        Assert.assertEquals(Stream.ofAll(validationResult.errors()).toJavaArray(String.class)[0],AsgStartEntityValidatorStrategy.ERROR_1);
    }

    @Test
    public void testStartOnlyElementQuery() {
        AsgStartEntityValidatorStrategy strategy = new AsgStartEntityValidatorStrategy();
        ValidationResult validationResult = strategy.apply(AsgQuery.Builder.start("Q1", "Dragon").build(), new AsgStrategyContext(new Ontology.Accessor(ontology)));
        Assert.assertFalse(validationResult.valid());
        Assert.assertEquals(Stream.ofAll(validationResult.errors()).toJavaArray(String.class)[0],AsgStartEntityValidatorStrategy.ERROR_2);
    }

    @Test
    public void testWrongPositionStartElementQuery() {
        AsgQuery query = AsgQuery.Builder.start("Q1", "Dragons")
                .next(unTyped(1))
                .next(rel(2, OntologyTestUtils.OWN.getrType(), R).below(relProp(10,
                        RelProp.of(START_DATE.type, 10, of(eq, new Date())))))
                .next(unTyped(3))
                .build();

        AsgQueryUtil.get(query.getStart(),3).get().addNextChild(new AsgEBase<>(new Start()));

        AsgStartEntityValidatorStrategy strategy = new AsgStartEntityValidatorStrategy();
        ValidationResult validationResult = strategy.apply(query, new AsgStrategyContext(new Ontology.Accessor(ontology)));
        Assert.assertFalse(validationResult.valid());
        Assert.assertEquals(Stream.ofAll(validationResult.errors()).toJavaArray(String.class)[0],AsgStartEntityValidatorStrategy.ERROR_3);
    }
}
