package com.kayhut.fuse.epb.plan;

import com.kayhut.fuse.dispatcher.ontology.OntologyProvider;
import com.kayhut.fuse.dispatcher.validation.QueryValidator;
import com.kayhut.fuse.epb.plan.estimation.pattern.PredicateCostEstimator;
import com.kayhut.fuse.epb.plan.estimation.pattern.RegexPatternCostEstimator;
import com.kayhut.fuse.epb.plan.extenders.M1.M1DfsRedundantPlanExtensionStrategy;
import com.kayhut.fuse.epb.plan.pruners.CheapestPlanPruneStrategy;
import com.kayhut.fuse.epb.plan.pruners.NoPruningPruneStrategy;
import com.kayhut.fuse.epb.plan.selectors.AllCompletePlanSelector;
import com.kayhut.fuse.epb.plan.validation.M1PlanValidator;
import com.kayhut.fuse.executor.ontology.GraphElementSchemaProviderFactory;
import com.kayhut.fuse.model.OntologyTestUtils;
import com.kayhut.fuse.model.asgQuery.AsgEBase;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.execution.plan.PlanWithCost;
import com.kayhut.fuse.model.execution.plan.composite.Plan;
import com.kayhut.fuse.model.execution.plan.composite.UnionOp;
import com.kayhut.fuse.model.execution.plan.costs.Cost;
import com.kayhut.fuse.model.execution.plan.costs.DoubleCost;
import com.kayhut.fuse.model.execution.plan.costs.PlanDetailedCost;
import com.kayhut.fuse.model.execution.plan.entity.EntityFilterOp;
import com.kayhut.fuse.model.execution.plan.entity.EntityOp;
import com.kayhut.fuse.model.execution.plan.relation.RelationOp;
import com.kayhut.fuse.model.ontology.Ontology;
import com.kayhut.fuse.model.query.Rel;
import com.kayhut.fuse.model.query.entity.ETyped;
import com.kayhut.fuse.model.query.entity.EUntyped;
import com.kayhut.fuse.model.query.properties.EProp;
import com.kayhut.fuse.model.query.properties.constraint.Constraint;
import com.kayhut.fuse.model.query.properties.constraint.ConstraintOp;
import com.kayhut.fuse.model.validation.ValidationResult;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Optional;

import static com.kayhut.fuse.epb.utils.DfsTestUtils.buildSchemaProvider;
import static com.kayhut.fuse.epb.utils.DfsTestUtils.ruleBaseEstimator;
import static com.kayhut.fuse.model.OntologyTestUtils.*;
import static com.kayhut.fuse.model.asgQuery.AsgQuery.Builder.*;
import static com.kayhut.fuse.model.execution.plan.composite.descriptors.IterablePlanOpDescriptor.getFull;
import static com.kayhut.fuse.model.query.Rel.Direction.R;
import static com.kayhut.fuse.model.query.quant.QuantType.some;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class UnionPlanSearcherTests {
    //region Fields
    private OntologyProvider ontologyProvider;
    private QueryValidator<AsgQuery> queryValidator;
    //ontology
    private Ontology.Accessor ont = new Ontology.Accessor(OntologyTestUtils.createDragonsOntologyShort());
    private GraphElementSchemaProviderFactory schemaProviderFactory;
    //endregion

    @Before
    public void setup() {
        this.ontologyProvider = mock(OntologyProvider.class);
        when(ontologyProvider.get(any())).thenReturn(Optional.of(OntologyTestUtils.createDragonsOntologyShort()));

        this.queryValidator = (QueryValidator<AsgQuery>) mock(QueryValidator.class);
        when(queryValidator.validate(any())).thenReturn(ValidationResult.OK);

        this.schemaProviderFactory = ontology -> buildSchemaProvider(new Ontology.Accessor(ontology));
    }


    private AsgQuery querySingleSomeQuantSingleBranch() {
        return AsgQuery.Builder.start("q", "O")
                .next(typed(1, PERSON.type))
                .next(ePropGroup(2, EProp.of(3, HEIGHT.type, Constraint.of(ConstraintOp.gt, 189))))
                .next(quant1(50, some))
                .in(rel(12, FREEZE.getrType(), R)
                        .next(unTyped(13)
                                .next(ePropGroup(14, EProp.of(15, NAME.type, Constraint.of(ConstraintOp.notContains, "bob"))))
                        )).build();
    }

    private AsgQuery queryMultiSomeQuantMultiBranch() {
        return start("q", "O")
                .next(typed(1, PERSON.type))
                .next(ePropGroup(101, EProp.of(101, HEIGHT.type, Constraint.of(ConstraintOp.gt, 189))))
                .next(quant1(1000, some))
                .in(rel(13, FREEZE.getrType(), R)
                                .next(typed(3, PERSON.type)
                                        .next(ePropGroup(103, EProp.of(103, HEIGHT.type, Constraint.of(ConstraintOp.gt, 189)))
                                                .next(quant1(2000, some)
                                                        .next(rel(16, FREEZE.getrType(), R)
                                                                .next(typed(6, PERSON.type)
                                                                        .next(ePropGroup(106, EProp.of(106, HEIGHT.type, Constraint.of(ConstraintOp.gt, 189))))
                                                                ))
                                                        .next(rel(17, FREEZE.getrType(), R)
                                                                .next(typed(7, PERSON.type)
                                                                        .next(ePropGroup(107, EProp.of(107, HEIGHT.type, Constraint.of(ConstraintOp.gt, 189))))
                                                                ))
                                                )
                                        )
                                ),
                        rel(14, FREEZE.getrType(), R)
                                .next(typed(4, PERSON.type)
                                        .next(ePropGroup(104, EProp.of(104, HEIGHT.type, Constraint.of(ConstraintOp.gt, 189))))
                                ),
                        rel(15, FREEZE.getrType(), R)
                                .next(typed(5, PERSON.type)
                                        .next(ePropGroup(105, EProp.of(105, HEIGHT.type, Constraint.of(ConstraintOp.gt, 189))))
                                )
                ).build();
    }

    private AsgQuery querySingleSomeQuantMultiBranch() {
        return start("q", "O")
                .next(typed(1, PERSON.type))
                .next(ePropGroup(101, EProp.of(101, HEIGHT.type, Constraint.of(ConstraintOp.gt, 189))))
                .next(quant1(2, some)).
                        in(rel(13, FREEZE.getrType(), R)
                                        .next(typed(3, PERSON.type)
                                                .next(ePropGroup(103, EProp.of(103, HEIGHT.type, Constraint.of(ConstraintOp.gt, 189))))
                                        ),
                                rel(14, FREEZE.getrType(), R)
                                        .next(typed(4, PERSON.type)
                                                .next(ePropGroup(104, EProp.of(104, HEIGHT.type, Constraint.of(ConstraintOp.gt, 189))))
                                        ),
                                rel(15, FREEZE.getrType(), R)
                                        .next(typed(5, PERSON.type)
                                                .next(ePropGroup(105, EProp.of(105, HEIGHT.type, Constraint.of(ConstraintOp.gt, 189))))
                                        )
                        ).build();
    }

    private AsgQuery queryNoQuant() {
        return AsgQuery.Builder.start("q", "O")
                .next(typed(1, PERSON.type)
                        .next(ePropGroup(2, EProp.of(3, HEIGHT.type, Constraint.of(ConstraintOp.gt, 189)))))
                .build();
    }

    @Test
    public void testNoQuant() {
        final BottomUpPlanSearcher<Plan, PlanDetailedCost, AsgQuery> searcher = createBottomUpPlanSearcher();
        PlanWithCost<Plan, Cost> expectedPlan = new PlanWithCost(
                new Plan(
                        new Plan(
                                new EntityOp(new AsgEBase<>(new ETyped(1, "", PERSON.type, 2, 0))),
                                new EntityFilterOp(ePropGroup(2, EProp.of(3, HEIGHT.type, Constraint.of(ConstraintOp.gt, 189))))
                        ).getOps()),
                new DoubleCost(0));

        UnionPlanSearcher planSearcher = new UnionPlanSearcher(searcher,queryValidator,ontologyProvider);
        AsgQuery query = queryNoQuant();
        final PlanWithCost<Plan, PlanDetailedCost> planWithCost = planSearcher.search(query);

        Assert.assertNotNull(planWithCost);
        Assert.assertEquals(expectedPlan.getPlan(), planWithCost.getPlan());
    }

    @Test
    public void testOneQuantSingleBranch() {
        final BottomUpPlanSearcher<Plan, PlanDetailedCost, AsgQuery> searcher = createBottomUpPlanSearcher();
        UnionPlanSearcher planSearcher = new UnionPlanSearcher(searcher,queryValidator,ontologyProvider);
        AsgQuery query = querySingleSomeQuantSingleBranch();
        final PlanWithCost<Plan, PlanDetailedCost> planWithCost = planSearcher.search(query);

        Assert.assertNotNull(planWithCost);
        Assert.assertEquals("[EntityOp(Asg(ETyped(1))):EntityFilterOp(Asg(EPropGroup(2))):RelationOp(Asg(Rel(12))):RelationFilterOp(Asg(RelPropGroup(1201))):EntityOp(Asg(EUntyped(13))):EntityFilterOp(Asg(EPropGroup(14)))]",
                getFull().describe(planWithCost.getPlan().getOps()));
    }

    @Test
    public void testOneQuantMultiBranch() {
        final BottomUpPlanSearcher<Plan, PlanDetailedCost, AsgQuery> searcher = createBottomUpPlanSearcher();

        UnionPlanSearcher planSearcher = new UnionPlanSearcher(searcher,queryValidator,ontologyProvider);
        AsgQuery query = querySingleSomeQuantMultiBranch();
        final PlanWithCost<Plan, PlanDetailedCost> planWithCost = planSearcher.search(query);

        Assert.assertNotNull(planWithCost);
        Assert.assertEquals(planWithCost.getPlan().getOps().size(), 1);
        Assert.assertTrue(planWithCost.getPlan().getOps().get(0).getClass().isAssignableFrom(UnionOp.class));
        Assert.assertEquals(3, ((UnionOp) planWithCost.getPlan().getOps().get(0)).getPlans().size(), 3);
        Assert.assertEquals(
            "[UnionOp(UnionOp())" +
                        "[[EntityOp(Asg(ETyped(4))):EntityFilterOp(Asg(EPropGroup(104))):RelationOp(Asg(Rel(14))):RelationFilterOp(Asg(RelPropGroup(1401))):EntityOp(Asg(ETyped(1))):EntityFilterOp(Asg(EPropGroup(101)))]]" +
                        "[[EntityOp(Asg(ETyped(5))):EntityFilterOp(Asg(EPropGroup(105))):RelationOp(Asg(Rel(15))):RelationFilterOp(Asg(RelPropGroup(1501))):EntityOp(Asg(ETyped(1))):EntityFilterOp(Asg(EPropGroup(101)))]]" +
                        "[[EntityOp(Asg(ETyped(3))):EntityFilterOp(Asg(EPropGroup(103))):RelationOp(Asg(Rel(13))):RelationFilterOp(Asg(RelPropGroup(1301))):EntityOp(Asg(ETyped(1))):EntityFilterOp(Asg(EPropGroup(101)))]]]",
                getFull().describe(planWithCost.getPlan().getOps()));
    }

    @Test
    public void testTwoQuantMultiBranch() {
        final BottomUpPlanSearcher<Plan, PlanDetailedCost, AsgQuery> searcher = createBottomUpPlanSearcher();

        UnionPlanSearcher planSearcher = new UnionPlanSearcher(searcher,queryValidator,ontologyProvider);
        AsgQuery query = queryMultiSomeQuantMultiBranch();
        final PlanWithCost<Plan, PlanDetailedCost> planWithCost = planSearcher.search(query);

        Assert.assertNotNull(planWithCost);
        Assert.assertEquals(planWithCost.getPlan().getOps().size(), 1);
        Assert.assertTrue(planWithCost.getPlan().getOps().get(0).getClass().isAssignableFrom(UnionOp.class));
        Assert.assertEquals(4, ((UnionOp) planWithCost.getPlan().getOps().get(0)).getPlans().size(), 0);
        Assert.assertEquals(
            "[UnionOp(UnionOp())" +
                            "[[EntityOp(Asg(ETyped(6))):EntityFilterOp(Asg(EPropGroup(106))):RelationOp(Asg(Rel(16))):RelationFilterOp(Asg(RelPropGroup(1601))):EntityOp(Asg(ETyped(3))):EntityFilterOp(Asg(EPropGroup(103))):RelationOp(Asg(Rel(13))):RelationFilterOp(Asg(RelPropGroup(1301))):EntityOp(Asg(ETyped(1))):EntityFilterOp(Asg(EPropGroup(101)))]]" +
                            "[[EntityOp(Asg(ETyped(4))):EntityFilterOp(Asg(EPropGroup(104))):RelationOp(Asg(Rel(14))):RelationFilterOp(Asg(RelPropGroup(1401))):EntityOp(Asg(ETyped(1))):EntityFilterOp(Asg(EPropGroup(101)))]]" +
                            "[[EntityOp(Asg(ETyped(7))):EntityFilterOp(Asg(EPropGroup(107))):RelationOp(Asg(Rel(17))):RelationFilterOp(Asg(RelPropGroup(1701))):EntityOp(Asg(ETyped(3))):EntityFilterOp(Asg(EPropGroup(103))):RelationOp(Asg(Rel(13))):RelationFilterOp(Asg(RelPropGroup(1301))):EntityOp(Asg(ETyped(1))):EntityFilterOp(Asg(EPropGroup(101)))]]" +
                            "[[EntityOp(Asg(ETyped(5))):EntityFilterOp(Asg(EPropGroup(105))):RelationOp(Asg(Rel(15))):RelationFilterOp(Asg(RelPropGroup(1501))):EntityOp(Asg(ETyped(1))):EntityFilterOp(Asg(EPropGroup(101)))]]]",
                getFull().describe(planWithCost.getPlan().getOps()));
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
                new PredicateCostEstimator<>(plan -> plan.getOps().size() <= 2,
                        new RegexPatternCostEstimator(ruleBaseEstimator(ont)),
                        (plan, context) -> new PlanWithCost<>(plan, context.getPreviousCost().get().getCost())));
    }


}
