package com.yangdb.fuse.asg.strategy.constraint;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yangdb.fuse.asg.strategy.AsgNamedParametersStrategy;
import com.yangdb.fuse.dispatcher.asg.QueryToAsgTransformer;
import com.yangdb.fuse.dispatcher.asg.QueryToCompositeAsgTransformer;
import com.yangdb.fuse.dispatcher.ontology.OntologyProvider;
import com.yangdb.fuse.model.OntologyTestUtils;
import com.yangdb.fuse.model.asgQuery.*;
import com.yangdb.fuse.model.execution.plan.descriptors.AsgQueryDescriptor;
import com.yangdb.fuse.model.ontology.Ontology;
import com.yangdb.fuse.model.query.EBase;
import com.yangdb.fuse.model.query.Query;
import com.yangdb.fuse.model.query.Rel;
import com.yangdb.fuse.model.query.Start;
import com.yangdb.fuse.model.query.entity.ETyped;
import com.yangdb.fuse.model.query.properties.EProp;
import com.yangdb.fuse.model.query.properties.EPropGroup;
import com.yangdb.fuse.model.query.properties.constraint.*;
import com.yangdb.fuse.model.query.quant.Quant1;
import com.yangdb.fuse.model.query.quant.QuantBase;
import com.yangdb.fuse.model.query.quant.QuantType;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

import static com.yangdb.fuse.model.OntologyTestUtils.*;
import static com.yangdb.fuse.model.query.Rel.Direction.R;
import static com.yangdb.fuse.model.query.quant.QuantType.some;

public class AsgNamedParametersStrategyTest {
    //region Setup
    @Before
    public void setUp() throws Exception {
        String ontologyExpectedJson = readJsonToString("src/test/resources/Dragons_Ontology.json");
        Ontology ontology = new ObjectMapper().readValue(ontologyExpectedJson, Ontology.class);
        ont = new Ontology.Accessor(ontology);
        asgSupplier = new QueryToCompositeAsgTransformer(new OntologyProvider() {
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
    //endregion

    private Query Q1() {
        return Query.Builder.instance().withName("q1").withOnt("Dragons")
                .withElements(Arrays.asList(
                        new Start(0, 1),
                        new ETyped(1, "P1", OntologyTestUtils.PERSON.type, 2, 0),
                        new Quant1(2, QuantType.all, Arrays.asList(3, 6), 0),
                        new Rel(3, OWN.getrType(), R, null, 4, 0),
                        new ETyped(4, "V1", OntologyTestUtils.DRAGON.name, 0, 0),
                        new Rel(6, MEMBER_OF.getrType(), R, null, 7, 0),
                        new ETyped(7, "E2", OntologyTestUtils.DRAGON.name, 9, 0),
                        new Rel(9, FIRE.getrType() , R, null, 10, 0),
                        new ETyped(10, "V2", HORSE.type, 11, 0),
                        new EProp(11,BIRTH_DATE.type, WhereByConstraint.of(ConstraintOp.gt, "P1","creationTime"))
                )).build();
    }


    private Query Q2() {
        return Query.Builder.instance().withName("q2").withOnt("Dragons")
                .withElements(Arrays.asList(
                        new Start(0, 1),
                        new ETyped(1, "P1", OntologyTestUtils.PERSON.type, 2, 0),
                        new Quant1(2, QuantType.all, Arrays.asList(20, 3, 6), 0),
                        new EProp(20, PERSON.name, WhereByConstraint.of(ConstraintOp.contains, "Jimmy")),
                        new Rel(3, OWN.getrType(), R, null, 4, 0),
                        new ETyped(4, "V1", OntologyTestUtils.DRAGON.name, 0, 0),
                        new Rel(6, MEMBER_OF.getrType(), R, null, 7, 0),
                        new ETyped(7, "E2", OntologyTestUtils.DRAGON.name, 9, 0),
                        new Rel(9, FIRE.getrType() , R, null, 10, 0),
                        new ETyped(10, "V2", HORSE.type, 11, 0),
                        new EProp(11,BIRTH_DATE.type, WhereByConstraint.of(ConstraintOp.gt, "P1","creationTime"))
                )).build();
    }


    private Query Q3() {
        Query query = Query.Builder.instance().withName("q1").withOnt("Dragons")
                .withElements(Arrays.asList(
                        new Start(0, 1),
                        new ETyped(1, "People", "Person", 2, 0),
                        new EPropGroup(2,
                                new EProp(3, "id", InnerQueryConstraint.of(ConstraintOp.contains, Q4(), "P", "creationTime")))
                )).build();
        return query;
    }

    private Query Q4() {
        Query query = Query.Builder.instance().withName("q2").withOnt("Dragons")
                .withElements(Arrays.asList(
                        new Start(0, 1),
                        new ETyped(1, "P", "Person", 2, 0),
                        new EPropGroup(2,
                                new EProp(3, "name", Constraint.of(ConstraintOp.like, "jhon*")))
                )).build();
        return query;
    }

    @Test
    public void singleNamedParameterTest() {
        AsgNamedParametersStrategy strategy = new AsgNamedParametersStrategy();
        AsgQuery query = asgSupplier.transform(Q1());
        List<String> words = Arrays.asList("Jay", "Jimmy", "Jane");
        query.setParameters(Collections.singletonList(new NamedParameter("P1.creationTime", words)));
        strategy.apply(query, new AsgStrategyContext(ont, query.getOrigin()));

        Assert.assertEquals("[└── Start, \n" +
                        "    ──Typ[:Person P1#1]]",
                AsgQueryDescriptor.print(((AsgCompositeQuery) query).getQueryChain().get(0)));

        Assert.assertTrue(AsgQueryUtil.element(query, asgEBase -> (asgEBase.geteBase() instanceof QuantBase) &&
                ((QuantBase) asgEBase.geteBase()).getqType().equals(some)).isPresent());

        AsgEBase<EBase> element = AsgQueryUtil.element(query, asgEBase -> (asgEBase.geteBase() instanceof QuantBase) &&
                ((QuantBase) asgEBase.geteBase()).getqType().equals(some)).get();
        Assert.assertEquals(element.getNext().size(),3);

        List<AsgEBase<EBase>> elements = AsgQueryUtil.elements(query, asgEBase -> (asgEBase.geteBase() instanceof EProp) &&
                ((EProp) asgEBase.geteBase()).getCon().getOp().equals(ConstraintOp.gt));
        Assert.assertEquals(elements.size(),3);
        List<String> expressions = elements.stream()
                .map(p -> ((EProp) p.geteBase()).getCon().getExpr().toString())
                .collect(Collectors.toList());
        Assert.assertTrue(expressions.containsAll(words));

    }

    @Test
    public void multipleNamedParameterTest() {
        AsgNamedParametersStrategy strategy = new AsgNamedParametersStrategy();
        AsgQuery query = asgSupplier.transform(Q2());
        List<String> words = Arrays.asList("Jay", "Jimmy", "Jane");
        query.setParameters(Collections.singletonList(new NamedParameter("P1.creationTime", words)));
        strategy.apply(query, new AsgStrategyContext(ont, query.getOrigin()));

        Assert.assertEquals("[└── Start, \n" +
                "    ──Typ[:Person P1#1]──Q[2:all]:{20}, \n" +
                "                                  └─?[20]:[Person<contains,Jimmy>]]",
                AsgQueryDescriptor.print(((AsgCompositeQuery) query).getQueryChain().get(0)));
        Assert.assertTrue(AsgQueryUtil.element(query, asgEBase -> (asgEBase.geteBase() instanceof QuantBase) &&
                ((QuantBase) asgEBase.geteBase()).getqType().equals(some)).isPresent());

        AsgEBase<EBase> element = AsgQueryUtil.element(query, asgEBase -> (asgEBase.geteBase() instanceof QuantBase) &&
                ((QuantBase) asgEBase.geteBase()).getqType().equals(some)).get();
        Assert.assertEquals(element.getNext().size(),3);

        List<AsgEBase<EBase>> elements = AsgQueryUtil.elements(query, asgEBase -> (asgEBase.geteBase() instanceof EProp) &&
                ((EProp) asgEBase.geteBase()).getCon().getOp().equals(ConstraintOp.gt));
        Assert.assertEquals(elements.size(),3);
        List<String> expressions = elements.stream()
                .map(p -> ((EProp) p.geteBase()).getCon().getExpr().toString())
                .collect(Collectors.toList());
        Assert.assertTrue(expressions.containsAll(words));
    }

    @Test
    public void innerQueryNamedParameterTest() {
        AsgNamedParametersStrategy strategy = new AsgNamedParametersStrategy();
        AsgQuery query = asgSupplier.transform(Q3());
        List<String> words = Arrays.asList("Jay", "Jimmy", "Jane");
        query.setParameters(Collections.singletonList(new NamedParameter("P.creationTime", words)));
        strategy.apply(query, new AsgStrategyContext(ont, query.getOrigin()));

        Assert.assertEquals("[└── Start, \n" +
                        "    ──Typ[:Person P#1]──?[..][2], \n" +
                        "                            └─?[3]:[name<like,jhon*>]]",
                AsgQueryDescriptor.print(((AsgCompositeQuery) query).getQueryChain().get(0)));
        Assert.assertTrue(AsgQueryUtil.element(query, asgEBase -> (asgEBase.geteBase() instanceof QuantBase) &&
                ((QuantBase) asgEBase.geteBase()).getqType().equals(some)).isPresent());

        AsgEBase<EBase> element = AsgQueryUtil.element(query, asgEBase -> (asgEBase.geteBase() instanceof QuantBase) &&
                ((QuantBase) asgEBase.geteBase()).getqType().equals(some)).get();
        Assert.assertEquals(element.getNext().size(),3);

        List<AsgEBase<EPropGroup>> elements = AsgQueryUtil.elements(query, EPropGroup.class);
        Assert.assertEquals(elements.size(),3);

        List<String> expressions = elements.stream()
                .flatMap(p -> p.geteBase().getProps().stream())
                .map(p->p.getCon().getExpr().toString())
                .collect(Collectors.toList());
        Assert.assertTrue(expressions.containsAll(words));
    }

    public static String readJsonToString(String jsonRelativePath) throws Exception {
        String contents = "";
        try {
            contents = new String(Files.readAllBytes(Paths.get(jsonRelativePath)));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return contents;
    }

    //region Fields
    private Ontology.Accessor ont;
    private QueryToAsgTransformer asgSupplier;

    //endregion

}