package com.kayhut.fuse.epb.plan;

import com.kayhut.fuse.dispatcher.ontology.OntologyProvider;
import com.kayhut.fuse.epb.plan.estimation.pattern.FirstStepOnlyCostEstimator;
import com.kayhut.fuse.epb.plan.estimation.pattern.RegexPatternCostEstimator;
import com.kayhut.fuse.epb.plan.extenders.M1.M1DfsRedundantPlanExtensionStrategy;
import com.kayhut.fuse.epb.plan.pruners.CheapestPlanPruneStrategy;
import com.kayhut.fuse.epb.plan.pruners.NoPruningPruneStrategy;
import com.kayhut.fuse.epb.plan.selectors.AllCompletePlanSelector;
import com.kayhut.fuse.epb.plan.validation.M1PlanValidator;
import com.kayhut.fuse.executor.ontology.GraphElementSchemaProviderFactory;
import com.kayhut.fuse.model.OntologyTestUtils;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.execution.plan.PlanWithCost;
import com.kayhut.fuse.model.execution.plan.composite.Plan;
import com.kayhut.fuse.model.execution.plan.costs.DoubleCost;
import com.kayhut.fuse.model.execution.plan.costs.PlanDetailedCost;
import com.kayhut.fuse.model.execution.plan.entity.EntityOp;
import com.kayhut.fuse.model.ontology.Ontology;
import com.kayhut.fuse.model.query.properties.constraint.Constraint;
import com.kayhut.fuse.model.query.properties.EProp;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Date;
import java.util.Optional;

import static com.kayhut.fuse.epb.utils.DfsTestUtils.buildSchemaProvider;
import static com.kayhut.fuse.epb.utils.DfsTestUtils.ruleBaseEstimator;
import static com.kayhut.fuse.model.OntologyTestUtils.*;
import static com.kayhut.fuse.model.OntologyTestUtils.Gender.MALE;
import static com.kayhut.fuse.model.asgQuery.AsgQuery.Builder.*;
import static com.kayhut.fuse.model.query.properties.constraint.ConstraintOp.*;
import static com.kayhut.fuse.model.query.Rel.Direction.R;
import static com.kayhut.fuse.model.query.properties.RelProp.of;
import static com.kayhut.fuse.model.query.quant.QuantType.all;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class DfsRuleBasedBottomUpPlanSearcherWithQuantTests {
    //region Fields
    private OntologyProvider ontologyProvider;
    //ontology
    private Ontology.Accessor ont = new Ontology.Accessor(OntologyTestUtils.createDragonsOntologyShort());
    private GraphElementSchemaProviderFactory schemaProviderFactory;
    //endregion

    @Before
    public void setup() {
        this.ontologyProvider = mock(OntologyProvider.class);
        when(ontologyProvider.get(any())).thenReturn(Optional.of(OntologyTestUtils.createDragonsOntologyShort()));

        this.schemaProviderFactory = ontology -> buildSchemaProvider(new Ontology.Accessor(ontology));
    }

    @Test
    /**
     * Start[0]:EEntityBase[1]:EPropGroup[101]:==>Relation[2]:RelPropGroup[201]:==>EEntityBase[3]:Quant1[4]:{301|5|7}:EPropGroup[301]:==>Relation[5]:RelPropGroup[501]:==>EEntityBase[6]:EPropGroup[601]:==>Relation[7]:RelPropGroup[701]:==>EEntityBase[8]:EPropGroup[801]
     */
    public void TestBuilderShouldStartWithFirstStep() {
        AsgQuery query = AsgQuery.Builder.start("Q1", "Dragons")
                .next(concrete(1, "eId0000123", OntologyTestUtils.PERSON.type, "Jomala", "person"))
                .next(eProp(101))
                .next(rel(2, OWN.getrType(), R).below(relProp(201, of(201, START_DATE.type, Constraint.of(eq, new Date())))))
                .next(typed(3, OntologyTestUtils.DRAGON.type))
                .next(quant1(4, all))
                .in(eProp(301, EProp.of(301, NAME.type, Constraint.of(eq, "smith")), EProp.of(301, GENDER.type, Constraint.of(gt, MALE)))
                        , rel(5, FREEZE.getrType(), R).below(relProp(501))
                                .next(unTyped(6).next(eProp(601)))
                        , rel(7, FIRE.getrType(), R)
                                .below(relProp(701, of(701, START_DATE.type, Constraint.of(ge, new Date(System.currentTimeMillis() - 1000 * 60))),
                                        of(701, END_DATE.type, Constraint.of(le, new Date(System.currentTimeMillis() + 1000 * 60)))))
                                .next(typed(8, OntologyTestUtils.DRAGON.type).next(eProp(801)))
                ).build();

        BottomUpPlanSearcher<Plan, PlanDetailedCost, AsgQuery> planSearcher = createBottomUpPlanSearcher();
        //Plan[[EntityOp(Asg(EConcrete(1))):EntityFilterOp(Asg(EPropGroup(101))):RelationOp(Asg(Rel(2))):RelationFilterOp(Asg(RelPropGroup(201))):EntityOp(Asg(ETyped(3))):EntityFilterOp(Asg(EPropGroup(301))):RelationOp(Asg(Rel(5))):RelationFilterOp(Asg(RelPropGroup(501))):EntityOp(Asg(EUntyped(6))):EntityFilterOp(Asg(EPropGroup(601))):GoToEntityOp(Asg(ETyped(3))):RelationOp(Asg(Rel(7))):RelationFilterOp(Asg(RelPropGroup(701))):EntityOp(Asg(ETyped(8))):EntityFilterOp(Asg(EPropGroup(801)))]]
        PlanWithCost<Plan, PlanDetailedCost> plan = planSearcher.search(query);
        Assert.assertEquals(15, plan.getPlan().getOps().size());
        Assert.assertEquals(1, ((EntityOp) plan.getPlan().getOps().get(0)).getAsgEbase().geteNum());
        Assert.assertEquals(plan.getPlan().toString(), "Plan[[EntityOp(Asg(EConcrete(1))):EntityFilterOp(Asg(EPropGroup(101))):RelationOp(Asg(Rel(2))):RelationFilterOp(Asg(RelPropGroup(201))):EntityOp(Asg(ETyped(3))):EntityFilterOp(Asg(EPropGroup(301))):RelationOp(Asg(Rel(5))):RelationFilterOp(Asg(RelPropGroup(501))):EntityOp(Asg(EUntyped(6))):EntityFilterOp(Asg(EPropGroup(601))):GoToEntityOp(Asg(ETyped(3))):RelationOp(Asg(Rel(7))):RelationFilterOp(Asg(RelPropGroup(701))):EntityOp(Asg(ETyped(8))):EntityFilterOp(Asg(EPropGroup(801)))]]");
        Assert.assertEquals(new DoubleCost(1.0), plan.getCost().getGlobalCost());
    }

    @Test
    public void TestBuilderShouldStartWithLastStep() {
        AsgQuery query = AsgQuery.Builder.start("Q1", "Dragons")
                .next(typed(1, OntologyTestUtils.DRAGON.type))
                .next(eProp(101))
                .next(rel(2, OWN.getrType(), R).below(relProp(201, of(201, START_DATE.type, Constraint.of(eq, new Date())))))
                .next(typed(3, OntologyTestUtils.DRAGON.type))
                .next(quant1(4, all))
                .in(eProp(301, EProp.of(301, NAME.type, Constraint.of(eq, "smith")), EProp.of(301, GENDER.type, Constraint.of(gt, MALE)))
                        , rel(5, FREEZE.getrType(), R).below(relProp(501))
                                .next(unTyped(6).next(eProp(601)))
                        , rel(7, FIRE.getrType(), R)
                                .below(relProp(701, of(701, START_DATE.type, Constraint.of(ge, new Date(System.currentTimeMillis() - 1000 * 60))),
                                        of(701, END_DATE.type, Constraint.of(le, new Date(System.currentTimeMillis() + 1000 * 60)))))
                                .next(concrete(8, "eId0000123", OntologyTestUtils.PERSON.type, "Jomala", "person").next(eProp(801)))
                ).build();

        BottomUpPlanSearcher<Plan, PlanDetailedCost, AsgQuery> planSearcher = createBottomUpPlanSearcher();
        //Plan[[EntityOp(Asg(EConcrete(8))):EntityFilterOp(Asg(EPropGroup(801))):RelationOp(Asg(Rel(7))):RelationFilterOp(Asg(RelPropGroup(701))):EntityOp(Asg(ETyped(3))):EntityFilterOp(Asg(EPropGroup(301))):RelationOp(Asg(Rel(5))):RelationFilterOp(Asg(RelPropGroup(501))):EntityOp(Asg(EUntyped(6))):EntityFilterOp(Asg(EPropGroup(601))):GoToEntityOp(Asg(ETyped(3))):RelationOp(Asg(Rel(2))):RelationFilterOp(Asg(RelPropGroup(201))):EntityOp(Asg(ETyped(1))):EntityFilterOp(Asg(EPropGroup(101)))]]
        PlanWithCost<Plan, PlanDetailedCost> plan = planSearcher.search(query);
        Assert.assertEquals(15, plan.getPlan().getOps().size());
        Assert.assertEquals(8, ((EntityOp) plan.getPlan().getOps().get(0)).getAsgEbase().geteNum());
        Assert.assertEquals(plan.getPlan().toString(), "Plan[[EntityOp(Asg(EConcrete(8))):EntityFilterOp(Asg(EPropGroup(801))):RelationOp(Asg(Rel(7))):RelationFilterOp(Asg(RelPropGroup(701))):EntityOp(Asg(ETyped(3))):EntityFilterOp(Asg(EPropGroup(301))):RelationOp(Asg(Rel(5))):RelationFilterOp(Asg(RelPropGroup(501))):EntityOp(Asg(EUntyped(6))):EntityFilterOp(Asg(EPropGroup(601))):GoToEntityOp(Asg(ETyped(3))):RelationOp(Asg(Rel(2))):RelationFilterOp(Asg(RelPropGroup(201))):EntityOp(Asg(ETyped(1))):EntityFilterOp(Asg(EPropGroup(101)))]]");
        Assert.assertEquals(new DoubleCost(1.0), plan.getCost().getGlobalCost());
    }

    @Test
    public void TestBuilderShouldStartWithMiddleUnderQuantStep() {
        AsgQuery query = AsgQuery.Builder.start("Q1", "Dragons")
                .next(typed(1, OntologyTestUtils.DRAGON.type))
                .next(eProp(101))
                .next(rel(2, OWN.getrType(), R).below(relProp(201, of(201, START_DATE.type, Constraint.of(eq, new Date())))))
                .next(typed(3, OntologyTestUtils.DRAGON.type))
                .next(quant1(4, all))
                .in(eProp(301, EProp.of(301, NAME.type, Constraint.of(eq, "smith")), EProp.of(301, GENDER.type, Constraint.of(gt, MALE)))
                        , rel(5, FREEZE.getrType(), R).below(relProp(501))
                                .next(concrete(6, "eId0000123", OntologyTestUtils.PERSON.type, "Jomala", "person").next(eProp(601)))
                        , rel(7, FIRE.getrType(), R)
                                .below(relProp(701, of(701, START_DATE.type, Constraint.of(ge, new Date(System.currentTimeMillis() - 1000 * 60))),
                                        of(701, END_DATE.type, Constraint.of(le, new Date(System.currentTimeMillis() + 1000 * 60)))))
                                .next(typed(8, OntologyTestUtils.DRAGON.type).next(eProp(801)))
                ).build();

        BottomUpPlanSearcher<Plan, PlanDetailedCost, AsgQuery> planSearcher = createBottomUpPlanSearcher();
        //Plan[[EntityOp(Asg(EConcrete(6))):EntityFilterOp(Asg(EPropGroup(601))):RelationOp(Asg(Rel(5))):RelationFilterOp(Asg(RelPropGroup(501))):EntityOp(Asg(ETyped(3))):EntityFilterOp(Asg(EPropGroup(301))):RelationOp(Asg(Rel(7))):RelationFilterOp(Asg(RelPropGroup(701))):EntityOp(Asg(ETyped(8))):EntityFilterOp(Asg(EPropGroup(801))):GoToEntityOp(Asg(ETyped(3))):RelationOp(Asg(Rel(2))):RelationFilterOp(Asg(RelPropGroup(201))):EntityOp(Asg(ETyped(1))):EntityFilterOp(Asg(EPropGroup(101)))]]
        PlanWithCost<Plan, PlanDetailedCost> plan = planSearcher.search(query);
        Assert.assertEquals(15, plan.getPlan().getOps().size());
        Assert.assertEquals(6, ((EntityOp) plan.getPlan().getOps().get(0)).getAsgEbase().geteNum());
        Assert.assertEquals(plan.getPlan().toString(), "Plan[[EntityOp(Asg(EConcrete(6))):EntityFilterOp(Asg(EPropGroup(601))):RelationOp(Asg(Rel(5))):RelationFilterOp(Asg(RelPropGroup(501))):EntityOp(Asg(ETyped(3))):EntityFilterOp(Asg(EPropGroup(301))):RelationOp(Asg(Rel(7))):RelationFilterOp(Asg(RelPropGroup(701))):EntityOp(Asg(ETyped(8))):EntityFilterOp(Asg(EPropGroup(801))):GoToEntityOp(Asg(ETyped(3))):RelationOp(Asg(Rel(2))):RelationFilterOp(Asg(RelPropGroup(201))):EntityOp(Asg(ETyped(1))):EntityFilterOp(Asg(EPropGroup(101)))]]");
        Assert.assertEquals(new DoubleCost(1.0), plan.getCost().getGlobalCost());
    }


    @Test
    public void TestBuilderShouldStartWithMiddleStep() {
        AsgQuery query = AsgQuery.Builder.start("Q1", "Dragons")
                .next(typed(1, OntologyTestUtils.DRAGON.type))
                .next(eProp(101))
                .next(rel(2, OWN.getrType(), R).below(relProp(201, of(201, START_DATE.type, Constraint.of(eq, new Date())))))
                .next(concrete(3, "eId0000123", OntologyTestUtils.PERSON.type, "Jomala", "person"))
                .next(quant1(4, all))
                .in(eProp(301, EProp.of(301, NAME.type, Constraint.of(eq, "smith")), EProp.of(301, GENDER.type, Constraint.of(gt, MALE)))
                        , rel(5, FREEZE.getrType(), R).below(relProp(501))
                                .next(typed(6, OntologyTestUtils.DRAGON.type).next(eProp(601)))
                        , rel(7, FIRE.getrType(), R).below(relProp(701, of(701, START_DATE.type, Constraint.of(ge, new Date(System.currentTimeMillis() - 1000 * 60))),
                                of(701, END_DATE.type, Constraint.of(le, new Date(System.currentTimeMillis() + 1000 * 60)))))
                                .next(typed(8, OntologyTestUtils.DRAGON.type).next(eProp(801)))
                ).build();

        BottomUpPlanSearcher<Plan, PlanDetailedCost, AsgQuery> planSearcher = createBottomUpPlanSearcher();
        //Plan[[EntityOp(Asg(EConcrete(3))):EntityFilterOp(Asg(EPropGroup(301))):RelationOp(Asg(Rel(7))):RelationFilterOp(Asg(RelPropGroup(701))):EntityOp(Asg(ETyped(8))):EntityFilterOp(Asg(EPropGroup(801))):GoToEntityOp(Asg(EConcrete(3))):RelationOp(Asg(Rel(5))):RelationFilterOp(Asg(RelPropGroup(501))):EntityOp(Asg(ETyped(6))):EntityFilterOp(Asg(EPropGroup(601))):GoToEntityOp(Asg(EConcrete(3))):RelationOp(Asg(Rel(2))):RelationFilterOp(Asg(RelPropGroup(201))):EntityOp(Asg(ETyped(1))):EntityFilterOp(Asg(EPropGroup(101)))]]
        PlanWithCost<Plan, PlanDetailedCost> plan = planSearcher.search(query);
        Assert.assertEquals(16, plan.getPlan().getOps().size());
        Assert.assertEquals(3, ((EntityOp) plan.getPlan().getOps().get(0)).getAsgEbase().geteNum());
        Assert.assertEquals(plan.getPlan().toString(), "Plan[[EntityOp(Asg(EConcrete(3))):EntityFilterOp(Asg(EPropGroup(301))):RelationOp(Asg(Rel(5))):RelationFilterOp(Asg(RelPropGroup(501))):EntityOp(Asg(ETyped(6))):EntityFilterOp(Asg(EPropGroup(601))):GoToEntityOp(Asg(EConcrete(3))):RelationOp(Asg(Rel(7))):RelationFilterOp(Asg(RelPropGroup(701))):EntityOp(Asg(ETyped(8))):EntityFilterOp(Asg(EPropGroup(801))):GoToEntityOp(Asg(EConcrete(3))):RelationOp(Asg(Rel(2))):RelationFilterOp(Asg(RelPropGroup(201))):EntityOp(Asg(ETyped(1))):EntityFilterOp(Asg(EPropGroup(101)))]]");
        Assert.assertEquals(new DoubleCost(1.0), plan.getCost().getGlobalCost());
    }

    private BottomUpPlanSearcher<Plan, PlanDetailedCost, AsgQuery> createBottomUpPlanSearcher() {

        return new BottomUpPlanSearcher<>(
                new M1DfsRedundantPlanExtensionStrategy(
                        ontologyProvider,
                        schemaProviderFactory),
                new NoPruningPruneStrategy<>(),
                new CheapestPlanPruneStrategy(),
                new AllCompletePlanSelector<>(),
                new AllCompletePlanSelector<>(),
                new M1PlanValidator(),
                new FirstStepOnlyCostEstimator(new RegexPatternCostEstimator(ruleBaseEstimator(ont))));
    }
}
