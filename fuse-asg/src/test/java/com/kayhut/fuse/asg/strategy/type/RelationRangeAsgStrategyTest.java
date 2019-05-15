package com.kayhut.fuse.asg.strategy.type;

import com.kayhut.fuse.asg.validation.AsgQueryValidator;
import com.kayhut.fuse.asg.validation.AsgValidatorStrategyRegistrarImpl;
import com.kayhut.fuse.dispatcher.ontology.OntologyProvider;
import com.kayhut.fuse.model.OntologyTestUtils;
import com.kayhut.fuse.model.Range;
import com.kayhut.fuse.model.asgQuery.AsgEBase;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.asgQuery.AsgQueryUtil;
import com.kayhut.fuse.model.asgQuery.AsgStrategyContext;
import com.kayhut.fuse.model.ontology.Ontology;
import com.kayhut.fuse.model.query.EBase;
import com.kayhut.fuse.model.query.Rel;
import com.kayhut.fuse.model.query.RelPattern;
import com.kayhut.fuse.model.query.properties.EProp;
import com.kayhut.fuse.model.query.properties.EPropGroup;
import com.kayhut.fuse.model.query.properties.RelProp;
import com.kayhut.fuse.model.query.properties.constraint.Constraint;
import com.kayhut.fuse.model.query.properties.constraint.ConstraintOp;
import com.kayhut.fuse.model.query.quant.Quant1;
import com.kayhut.fuse.model.query.quant.QuantBase;
import com.kayhut.fuse.model.query.quant.QuantType;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Optional;

import static com.kayhut.fuse.model.OntologyTestUtils.END_DATE;
import static com.kayhut.fuse.model.OntologyTestUtils.START_DATE;
import static com.kayhut.fuse.model.asgQuery.AsgQuery.Builder.*;
import static com.kayhut.fuse.model.query.Rel.Direction.R;
import static com.kayhut.fuse.model.query.properties.constraint.Constraint.of;
import static com.kayhut.fuse.model.query.properties.constraint.ConstraintOp.eq;
import static com.kayhut.fuse.model.query.quant.QuantType.all;

/**
 * test Rel pattern asg strategy:
 * Expand given relation range pattern into an Or quant with all premutations of requested path length
 * <p>
 * (:E)-[:R | 1..3]->(:E) would be transformed into:
 * <p>
 * (:E)-Quant[OR]
 * -[:R]->(:E)
 * -[:R]->(:E)-[:R]->(:E)
 * -[:R]->(:E)-[:R]->(:E)-[:R]->(:E)
 */
public class RelationRangeAsgStrategyTest {
    static Ontology ontology;
    static AsgQueryValidator queryValidator;

    @BeforeClass
    public static void setUp() throws Exception {
        ontology = OntologyTestUtils.createDragonsOntologyShort();
        queryValidator = new AsgQueryValidator(new AsgValidatorStrategyRegistrarImpl(), new OntologyProvider() {
            @Override
            public Optional<Ontology> get(String id) {
                return Optional.of(ontology);
            }

            @Override
            public Collection<Ontology> getAll() {
                return Collections.singleton(ontology);
            }
        });
    }

    @Test
    public void testUntypedToTypedStrategyWithoutQuantsInPath() throws Exception {
        Ontology.Accessor ont = new Ontology.Accessor(ontology);
        AsgQuery query = AsgQuery.Builder.start("Q1", "Dragons")
                .next(typed(1,OntologyTestUtils.PERSON.type))
                .next(new AsgEBase<>(new RelPattern(2, OntologyTestUtils.OWN.getrType(), new Range(1, 3), R))
                        .below(relProp(10, RelProp.of(10, START_DATE.type, of(eq, new Date())))))
                .next(unTyped(3))
                .build();

        RelationPatternRangeAsgStrategy strategy = new RelationPatternRangeAsgStrategy();
        strategy.apply(query, new AsgStrategyContext(ont));

        Optional<AsgEBase<Quant1>> quant = AsgQueryUtil.elements(query, Quant1.class).stream().filter(q -> q.geteBase().getqType().equals(QuantType.some)).findAny();
        Assert.assertFalse(AsgQueryUtil.element(query, RelPattern.class).isPresent());
        Assert.assertTrue(quant.isPresent());
        Assert.assertEquals(3, quant.get().getNext().size());
        Assert.assertEquals(3, AsgQueryUtil.count(quant.get().getNext().get(0), EBase.class));
        Assert.assertEquals(7, AsgQueryUtil.count(quant.get().getNext().get(1), EBase.class));
        Assert.assertEquals(11, AsgQueryUtil.count(quant.get().getNext().get(2), EBase.class));
        Assert.assertEquals(6, AsgQueryUtil.elements(query, Rel.class).stream().filter(r -> r.getB().size() == 1).count());
        Assert.assertEquals(4, (long) AsgQueryUtil.elements(query, QuantBase.class).size());
        Assert.assertTrue(queryValidator.validate(query).toString(),queryValidator.validate(query).valid());
    }

    @Test
    public void testUntypedToTypedWithPropsStrategyWithoutQuantsInPath() throws Exception {
        Ontology.Accessor ont = new Ontology.Accessor(ontology);
        AsgQuery query = AsgQuery.Builder.start("Q1", "Dragons")
                .next(typed(1,OntologyTestUtils.PERSON.type))
                .next(new AsgEBase<>(new RelPattern(2, OntologyTestUtils.KNOW.getrType(), new Range(1, 3), R))
                        .below(relProp(10, RelProp.of(10, START_DATE.type, of(eq, new Date())))))
                .next(typed(3, OntologyTestUtils.PERSON.type))
                .next(eProp(4, OntologyTestUtils.FIRST_NAME.type, of(eq, "abc")))
                .build();

        RelationPatternRangeAsgStrategy strategy = new RelationPatternRangeAsgStrategy();
        strategy.apply(query, new AsgStrategyContext(ont));

        Optional<AsgEBase<Quant1>> quant = AsgQueryUtil.elements(query, Quant1.class).stream().filter(q -> q.geteBase().getqType().equals(QuantType.some)).findAny();
        Assert.assertFalse(AsgQueryUtil.element(query, RelPattern.class).isPresent());
        Assert.assertTrue(quant.isPresent());
        Assert.assertEquals(3, quant.get().getNext().size());
        Assert.assertEquals(5, AsgQueryUtil.count(quant.get().getNext().get(0), EBase.class));
        Assert.assertEquals(11, AsgQueryUtil.count(quant.get().getNext().get(1), EBase.class));
        Assert.assertEquals(17, AsgQueryUtil.count(quant.get().getNext().get(2), EBase.class));
        Assert.assertEquals(6, AsgQueryUtil.elements(query, Rel.class).stream().filter(r -> r.getB().size() == 1).count());
        Assert.assertEquals(6, AsgQueryUtil.elements(query, EProp.class).stream().filter(ep -> ep.geteBase().getCon().getOp().equals(eq)).count());
        Assert.assertEquals(10, (long) AsgQueryUtil.elements(query, QuantBase.class).size());
        Assert.assertTrue(queryValidator.validate(query).toString(),queryValidator.validate(query).valid());
    }

    @Test
    @Ignore("fix ontology terms & path after quant in resulting query")
    public void testUntypedToTypedWithSomePropsStrategyWithoutQuantsInPath() throws Exception {
        Ontology.Accessor ont = new Ontology.Accessor(ontology);
        AsgQuery query = AsgQuery.Builder.start("Q1", "Dragons")
                .next(typed(1, "Entity", "source"))
                .next(quant1(2, all))
                .in(
                        eProp(4, "p1", of(eq, "abc")),
                        new AsgEBase<>(new RelPattern(2, OntologyTestUtils.OWN.getrType(), new Range(1, 3), R))
                                .next(
                                        typed(3, OntologyTestUtils.DRAGON.type)
                                                .addNext(eProp(4, "p1", of(eq, "abc")))))

                .build();
        RelationPatternRangeAsgStrategy strategy = new RelationPatternRangeAsgStrategy();
        strategy.apply(query, new AsgStrategyContext(ont));

        Optional<AsgEBase<Quant1>> quant = AsgQueryUtil.elements(query, Quant1.class).stream().filter(q -> q.geteBase().getqType().equals(QuantType.some)).findAny();
        Assert.assertFalse(AsgQueryUtil.element(query, RelPattern.class).isPresent());
        Assert.assertTrue(quant.isPresent());
        Assert.assertEquals(3, quant.get().getNext().size());
        Assert.assertEquals(4, AsgQueryUtil.count(quant.get().getNext().get(0), EBase.class));
        Assert.assertEquals(9, AsgQueryUtil.count(quant.get().getNext().get(1), EBase.class));
        Assert.assertEquals(14, AsgQueryUtil.count(quant.get().getNext().get(2), EBase.class));
        Assert.assertEquals(7, AsgQueryUtil.elements(query, EProp.class).stream().filter(ep -> ep.geteBase().getCon().getOp().equals(eq)).count());
        Assert.assertEquals(11, (long) AsgQueryUtil.elements(query, QuantBase.class).size());
        Assert.assertTrue(queryValidator.validate(query).toString(),queryValidator.validate(query).valid());
    }

    @Test
    @Ignore("todo fix this test")
    public void testUntypedToTypedWithQuantPropsStrategyWithoutQuantsInPath() throws Exception {
        Ontology.Accessor ont = new Ontology.Accessor(ontology);
        AsgQuery query = AsgQuery.Builder.start("Q1", "Dragons")
                .next(typed(1,OntologyTestUtils.PERSON.type))
                .next(new AsgEBase<>(new RelPattern(2, OntologyTestUtils.KNOW.getrType(), new Range(1, 3), R))
                        .below(relProp(10, RelProp.of(10, START_DATE.type, of(eq, new Date())))))
                .next(typed(3, OntologyTestUtils.PERSON.type))
                .next(quant1(4, all))
                .in(ePropGroup(5,
                        EProp.of(5, OntologyTestUtils.FIRST_NAME.type, Constraint.of(ConstraintOp.like, "Dormir"))))
                .build();

        RelationPatternRangeAsgStrategy strategy = new RelationPatternRangeAsgStrategy();
        strategy.apply(query, new AsgStrategyContext(ont));

        Optional<AsgEBase<Quant1>> quant = AsgQueryUtil.elements(query, Quant1.class).stream().filter(q -> q.geteBase().getqType().equals(QuantType.some)).findAny();
        Assert.assertFalse(AsgQueryUtil.element(query, RelPattern.class).isPresent());
        Assert.assertTrue(quant.isPresent());
        Assert.assertEquals(3, quant.get().getNext().size());
        Assert.assertEquals(6, AsgQueryUtil.count(quant.get().getNext().get(0), EBase.class));
        Assert.assertEquals(13, AsgQueryUtil.count(quant.get().getNext().get(1), EBase.class));
        Assert.assertEquals(20, AsgQueryUtil.count(quant.get().getNext().get(2), EBase.class));
        Assert.assertEquals(6, AsgQueryUtil.elements(query, Rel.class).stream().filter(r -> r.getB().size() == 1).count());
        Assert.assertEquals(6, AsgQueryUtil.elements(query, EPropGroup.class).stream().filter(ep -> ep.geteBase().getProps().size() == 1).count());
        Assert.assertEquals(16, (long) AsgQueryUtil.elements(query, QuantBase.class).size());
        Assert.assertTrue(queryValidator.validate(query).toString(),queryValidator.validate(query).valid());

    }

    @Test
    public void testUntypedToTypedWithQuantPropsAfterRelStrategyWithoutQuantsInPath() throws Exception {
        Ontology.Accessor ont = new Ontology.Accessor(ontology);
        AsgQuery query = AsgQuery.Builder.start("Q1", "Dragons")
                .next(unTyped(1))
                .next(new AsgEBase<>(new RelPattern(2, OntologyTestUtils.OWN.getrType(), new Range(1, 3), R))
                        .below(relProp(10, RelProp.of(10, START_DATE.type, of(eq, new Date())))))
                .next(quant1(3, all))
                .in(relPropGroup(11, all, RelProp.of(11, END_DATE.type, of(eq, new Date()))),
                        typed(4, OntologyTestUtils.DRAGON.type)
                                .next(quant1(5, all)
                                        .addNext(ePropGroup(12, EProp.of(12, "name", Constraint.of(ConstraintOp.like, "Dormir"))))
                                )
                )
                .build();

        RelationPatternRangeAsgStrategy strategy = new RelationPatternRangeAsgStrategy();
        strategy.apply(query, new AsgStrategyContext(ont));

        Optional<AsgEBase<Quant1>> quant = AsgQueryUtil.elements(query, Quant1.class).stream().filter(q -> q.geteBase().getqType().equals(QuantType.some)).findAny();
        Assert.assertFalse(AsgQueryUtil.element(query, RelPattern.class).isPresent());
        Assert.assertTrue(quant.isPresent());
        Assert.assertEquals(3, quant.get().getNext().size());
        Assert.assertEquals(7, AsgQueryUtil.count(quant.get().getNext().get(0), EBase.class));
        Assert.assertEquals(14, AsgQueryUtil.count(quant.get().getNext().get(1), EBase.class));
        Assert.assertEquals(21, AsgQueryUtil.count(quant.get().getNext().get(2), EBase.class));
        Assert.assertEquals(6, AsgQueryUtil.elements(query, Rel.class).stream().filter(r -> r.getB().size() == 1).count());
        Assert.assertEquals(6, AsgQueryUtil.elements(query, EPropGroup.class).stream().filter(ep -> ep.geteBase().getProps().size() == 1).count());
        Assert.assertEquals(13, (long) AsgQueryUtil.elements(query, QuantBase.class).size());

        Assert.assertTrue(queryValidator.validate(query).toString(),queryValidator.validate(query).valid());
    }

    @Test
    @Ignore("todo fix this test")
    public void testUntypedToTypedWithPropsStrategyWithQuantsInPath() {
        Ontology.Accessor ont = new Ontology.Accessor(ontology);
        AsgQuery query = AsgQuery.Builder.start("Q1", "Dragons")
                .next(unTyped(1))
                .next(quant1(2, all))
                .in(ePropGroup(3,
                        EProp.of(3, "name", Constraint.of(ConstraintOp.like, "Dormir"))))
                .next(new AsgEBase<>(new RelPattern(2, OntologyTestUtils.OWN.getrType(), new Range(1, 3), R))
                        .below(relProp(10, RelProp.of(10, START_DATE.type, of(eq, new Date())))))
                .next(typed(3, OntologyTestUtils.DRAGON.type))
                .next(eProp(4, "p1", of(eq, "abc")))
                .build();

        RelationPatternRangeAsgStrategy strategy = new RelationPatternRangeAsgStrategy();
        strategy.apply(query, new AsgStrategyContext(ont));

        Optional<AsgEBase<Quant1>> quant = AsgQueryUtil.elements(query, Quant1.class).stream().filter(q -> q.geteBase().getqType().equals(QuantType.some)).findAny();
        Assert.assertFalse(AsgQueryUtil.element(query, RelPattern.class).isPresent());
        Assert.assertTrue(quant.isPresent());
        Assert.assertEquals(3, quant.get().getNext().size());
        Assert.assertEquals(5, AsgQueryUtil.count(quant.get().getNext().get(0), EBase.class));
        Assert.assertEquals(11, AsgQueryUtil.count(quant.get().getNext().get(1), EBase.class));
        Assert.assertEquals(17, AsgQueryUtil.count(quant.get().getNext().get(2), EBase.class));
        Assert.assertEquals(6, AsgQueryUtil.elements(query, Rel.class).stream().filter(r -> r.getB().size() == 1).count());
        Assert.assertEquals(6, AsgQueryUtil.elements(query, EProp.class).stream().filter(ep -> ep.geteBase().getCon().getOp().equals(eq)).count());
        Assert.assertEquals(11, (long) AsgQueryUtil.elements(query, QuantBase.class).size());
        Assert.assertTrue(queryValidator.validate(query).toString(),queryValidator.validate(query).valid());

    }


}