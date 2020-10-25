package com.yangdb.fuse.asg.strategy.type;

import com.yangdb.fuse.asg.validation.AsgQueryValidator;
import com.yangdb.fuse.asg.validation.AsgValidatorStrategyRegistrarImpl;
import com.yangdb.fuse.dispatcher.ontology.OntologyProvider;
import com.yangdb.fuse.model.OntologyTestUtils;
import com.yangdb.fuse.model.Range;
import com.yangdb.fuse.model.asgQuery.AsgEBase;
import com.yangdb.fuse.model.asgQuery.AsgQuery;
import com.yangdb.fuse.model.asgQuery.AsgQueryUtil;
import com.yangdb.fuse.model.asgQuery.AsgStrategyContext;
import com.yangdb.fuse.model.execution.plan.descriptors.AsgQueryDescriptor;
import com.yangdb.fuse.model.ontology.Ontology;
import com.yangdb.fuse.model.query.EBase;
import com.yangdb.fuse.model.query.Rel;
import com.yangdb.fuse.model.query.RelPattern;
import com.yangdb.fuse.model.query.RelUntyped;
import com.yangdb.fuse.model.query.entity.ETyped;
import com.yangdb.fuse.model.query.properties.EProp;
import com.yangdb.fuse.model.query.properties.EPropGroup;
import com.yangdb.fuse.model.query.properties.RelProp;
import com.yangdb.fuse.model.query.properties.RelPropGroup;
import com.yangdb.fuse.model.query.properties.constraint.Constraint;
import com.yangdb.fuse.model.query.properties.constraint.ConstraintOp;
import com.yangdb.fuse.model.query.quant.Quant1;
import com.yangdb.fuse.model.query.quant.QuantBase;
import com.yangdb.fuse.model.query.quant.QuantType;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Optional;

import static com.yangdb.fuse.model.OntologyTestUtils.END_DATE;
import static com.yangdb.fuse.model.OntologyTestUtils.START_DATE;
import static com.yangdb.fuse.model.Tagged.tagSeq;
import static com.yangdb.fuse.model.asgQuery.AsgQuery.Builder.*;
import static com.yangdb.fuse.model.query.Rel.Direction.R;
import static com.yangdb.fuse.model.query.properties.constraint.Constraint.of;
import static com.yangdb.fuse.model.query.properties.constraint.ConstraintOp.eq;
import static com.yangdb.fuse.model.query.quant.QuantType.all;

/**
 * test untyped Rel pattern asg strategy:
 * Expand given untyped relation with multi-types to union of multiple typed relations
 * <p>
 * (:E)-[:R | type1,type2,type3]->(:E) would be transformed into:
 * <p>
 * (:E)-Quant[OR]
 * -[:R,type1]->(:E)
 * -[:R,type2]->(:E)
 * -[:R,type3]->(:E)
 */
public class UntypedRelationInferTypeStrategyTest {
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

            @Override
            public Ontology add(Ontology ontology) {
                return ontology;
            }
        });
    }

    @Test
    public void testUntypedToTypedStrategyWithoutQuantsInPath() {
        Ontology.Accessor ont = new Ontology.Accessor(ontology);
        AsgQuery query = AsgQuery.Builder.start("Q1", "Dragons")
                .next(typed(1, OntologyTestUtils.PERSON.type))
                .next(relUntyped(2, R, new String[]{OntologyTestUtils.OWN.getrType(), OntologyTestUtils.KNOW.getrType()})
                        .below(relProp(10, RelProp.of(10, START_DATE.type, of(eq, new Date())))))
                .next(unTyped(3, "end"))
                .build();

        UntypedRelationInferTypeAsgStrategy strategy = new UntypedRelationInferTypeAsgStrategy();
        strategy.apply(query, new AsgStrategyContext(ont));

        Optional<AsgEBase<Quant1>> quant = AsgQueryUtil.elements(query, Quant1.class).stream().filter(q -> q.geteBase().getqType().equals(QuantType.some)).findAny();
        Assert.assertFalse(AsgQueryUtil.element(query, RelUntyped.class).isPresent());
        Assert.assertTrue(quant.isPresent());
        Assert.assertEquals(2, quant.get().getNext().size());
        Assert.assertTrue(queryValidator.validate(query).toString(), queryValidator.validate(query).valid());
        Assert.assertNotNull(AsgQueryDescriptor.print(query));
        Assert.assertEquals("[└── Start, \n" +
                        "    ──Typ[:Person null#1]──Q[11:some]:{15|19}, \n" +
                        "                                         └-> Rel(:own null.own#15)──UnTyp[:[Horse, Dragon] end.own#13], \n" +
                        "                                         └-> Rel(:know null.know#19)──Typ[:Person end.Person#17]]",
                AsgQueryDescriptor.print(query));

    }


    @Test
    public void testUntypedToTypedWithSomePropsStrategyWithoutQuantsInPath() throws Exception {
        Ontology.Accessor ont = new Ontology.Accessor(ontology);
        AsgQuery query = AsgQuery.Builder.start("Q1", "Dragons")
                .next(typed(1, OntologyTestUtils.PERSON.type, "source"))
                .next(quant1(2, all))
                .in(
                        eProp(4, OntologyTestUtils.FIRST_NAME.type, of(eq, "abc")),
                        relUntyped(2, R, OntologyTestUtils.KNOW.getrType(), OntologyTestUtils.SUBJECT.getrType(), OntologyTestUtils.OWN.getrType())
                                .next(unTyped(3).addNext(eProp(4, OntologyTestUtils.NAME.type, of(eq, "abc")))))

                .build();

        UntypedRelationInferTypeAsgStrategy strategy = new UntypedRelationInferTypeAsgStrategy();
        strategy.apply(query, new AsgStrategyContext(ont));

        Optional<AsgEBase<Quant1>> quant = AsgQueryUtil.elements(query, Quant1.class).stream().filter(q -> q.geteBase().getqType().equals(QuantType.some)).findAny();
        Assert.assertFalse(AsgQueryUtil.element(query, RelUntyped.class).isPresent());
        Assert.assertTrue(quant.isPresent());
        Assert.assertEquals(4, quant.get().getNext().size());
        Assert.assertTrue(queryValidator.validate(query).toString(), queryValidator.validate(query).valid());
        Assert.assertNotNull(AsgQueryDescriptor.print(query));
        Assert.assertEquals("[└── Start, \n" +
                        "    ──Typ[:Person source#1]──Q[5:some]:{2|9|13|17}, \n" +
                        "                                              └─Q[2:all]:{4}, \n" +
                        "                                                        └─?[4]:[firstName<eq,abc>]──Typ[:Kingdom null.Kingdom#7]──?[8]:[name<eq,abc>], \n" +
                        "                                              └-> Rel(:subject null.subject#9)──UnTyp[:[Horse, Dragon] null.own#11]──?[12]:[name<eq,abc>], \n" +
                        "                                              └-> Rel(:own null.own#13)──Typ[:Person null.Person#15]──?[16]:[name<eq,abc>], \n" +
                        "                                              └-> Rel(:know null.know#17)]",
                AsgQueryDescriptor.print(query));
    }

    @Test
    @Ignore("Under Development")
    public void testUntypedToTypedWithQuantPropsAfterRelStrategyWithoutQuantsInPath() throws Exception {
        Ontology.Accessor ont = new Ontology.Accessor(ontology);
        AsgQuery query = AsgQuery.Builder.start("Q1", "Dragons")
                .next(unTyped(1, "P", Collections.singletonList(OntologyTestUtils.PERSON.type)))
                .next(relUntyped(2, R, OntologyTestUtils.KNOW.getrType(), OntologyTestUtils.SUBJECT.getrType(), OntologyTestUtils.OWN.getrType())
                        .below(relProp(10, RelProp.of(10, START_DATE.type, of(eq, new Date())))))
                .next(quant1(3, all))
                .in(relPropGroup(11, all, RelProp.of(11, END_DATE.type, of(eq, new Date()))),
                        unTyped(4)
                                .addNext(eProp(5, OntologyTestUtils.NAME.type, of(eq, "abc")))
                                .next(quant1(6, all)
                                        .addNext(ePropGroup(12, EProp.of(12, OntologyTestUtils.FIRST_NAME.type, Constraint.of(ConstraintOp.like, "Dormir"))))
                                )
                )
                .build();

        UntypedRelationInferTypeAsgStrategy strategy = new UntypedRelationInferTypeAsgStrategy();
        strategy.apply(query, new AsgStrategyContext(ont));

        Optional<AsgEBase<Quant1>> quant = AsgQueryUtil.elements(query, Quant1.class).stream().filter(q -> q.geteBase().getqType().equals(QuantType.some)).findAny();
        Assert.assertFalse(AsgQueryUtil.element(query, RelUntyped.class).isPresent());
        Assert.assertTrue(quant.isPresent());
        Assert.assertEquals(3, quant.get().getNext().size());
        Assert.assertTrue(queryValidator.validate(query).toString(), queryValidator.validate(query).valid());
        Assert.assertNotNull(AsgQueryDescriptor.print(query));
        Assert.assertEquals("[└── Start, \n" +
                        "    ──Typ[:Person source#1]──Q[5:some]:{2|9|13|17}, \n" +
                        "                                              └─Q[2:all]:{4}, \n" +
                        "                                                        └─?[4]:[firstName<eq,abc>]──Typ[:Kingdom null.Kingdom#7]──?[8]:[name<eq,abc>], \n" +
                        "                                              └-> Rel(:subject null.subject#9)──UnTyp[:[Horse, Dragon] null.own#11]──?[12]:[name<eq,abc>], \n" +
                        "                                              └-> Rel(:own null.own#13)──Typ[:Person null.Person#15]──?[16]:[name<eq,abc>], \n" +
                        "                                              └-> Rel(:know null.know#17)]",
                AsgQueryDescriptor.print(query));
    }

    @Test
    public void testUntypedToTypedWithPropsStrategyWithQuantsInPath() {
        Ontology.Accessor ont = new Ontology.Accessor(ontology);
        AsgQuery query = AsgQuery.Builder.start("Q1", "Dragons")
                .next(unTyped(1, "P", Collections.singletonList(OntologyTestUtils.PERSON.type)))
                .next(quant1(2, all))
                .in(ePropGroup(3,
                        EProp.of(3, OntologyTestUtils.FIRST_NAME.type, Constraint.of(ConstraintOp.like, "Dormir"))))
                .next(relPattern(2, OntologyTestUtils.KNOW.getrType(), new Range(1, 3), R)
                        .below(relProp(10, RelProp.of(10, START_DATE.type, of(eq, new Date())))))
                .next(endTypedPattern(new ETyped(3, tagSeq("end"), OntologyTestUtils.PERSON.type, 0)))
                .next(eProp(4, OntologyTestUtils.NAME.type, of(eq, "abc")))
                .build();

        RelationPatternRangeAsgStrategy strategy = new RelationPatternRangeAsgStrategy();
        strategy.apply(query, new AsgStrategyContext(ont));

        Optional<AsgEBase<Quant1>> quant = AsgQueryUtil.elements(query, Quant1.class).stream().filter(q -> q.geteBase().getqType().equals(QuantType.some)).findAny();
        Assert.assertFalse(AsgQueryUtil.element(query, RelPattern.class).isPresent());
        Assert.assertTrue(quant.isPresent());
        Assert.assertEquals(3, quant.get().getNext().size());
        Assert.assertEquals(1, AsgQueryUtil.elements(query, QuantBase.class).stream().filter(e -> e.getNext().size() == 3).count());
        Assert.assertEquals(1, AsgQueryUtil.elements(query, QuantBase.class).stream().filter(e -> e.getNext().size() == 2).count());
        Assert.assertEquals(3, AsgQueryUtil.elements(query, QuantBase.class).stream().filter(e -> e.getNext().size() == 1).count());

        Assert.assertEquals(4, AsgQueryUtil.count(quant.get().getNext().get(0), EBase.class));
        Assert.assertEquals(8, AsgQueryUtil.count(quant.get().getNext().get(1), EBase.class));
        Assert.assertEquals(12, AsgQueryUtil.count(quant.get().getNext().get(2), EBase.class));

        Assert.assertEquals(6, AsgQueryUtil.elements(query, Rel.class).stream().filter(r -> r.getB().size() == 1).count());
        Assert.assertEquals(6, AsgQueryUtil.elements(query, RelPropGroup.class).size());
        Assert.assertEquals(0, AsgQueryUtil.elements(query, RelProp.class).size());

        Assert.assertEquals(3, AsgQueryUtil.elements(query, EProp.class).stream().filter(ep -> ep.geteBase().getCon().getOp().equals(eq)).count());
        Assert.assertEquals(1, AsgQueryUtil.elements(query, EPropGroup.class).size());

        Assert.assertTrue(queryValidator.validate(query).toString(), queryValidator.validate(query).valid());
        Assert.assertNotNull(AsgQueryDescriptor.print(query));

    }


}