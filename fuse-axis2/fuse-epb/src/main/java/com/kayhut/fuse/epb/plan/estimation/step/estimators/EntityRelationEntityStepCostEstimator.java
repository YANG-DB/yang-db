package com.kayhut.fuse.epb.plan.estimation.step.estimators;

import com.kayhut.fuse.epb.plan.estimation.CostEstimationConfig;
import com.kayhut.fuse.epb.plan.estimation.step.EntityRelationEntityStep;
import com.kayhut.fuse.epb.plan.estimation.step.Step;
import com.kayhut.fuse.epb.plan.estimation.step.StepCostEstimator;
import com.kayhut.fuse.epb.plan.estimation.step.context.M1StepCostEstimatorContext;
import com.kayhut.fuse.epb.plan.statistics.StatisticsProvider;
import com.kayhut.fuse.model.execution.plan.*;
import com.kayhut.fuse.model.execution.plan.costs.CountEstimatesCost;
import com.kayhut.fuse.model.execution.plan.costs.DoubleCost;
import com.kayhut.fuse.model.execution.plan.costs.DetailedCost;
import com.kayhut.fuse.model.execution.plan.costs.PlanDetailedCost;
import com.kayhut.fuse.model.ontology.OntologyFinalizer;
import com.kayhut.fuse.model.query.properties.*;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by moti on 29/05/2017.
 *
 */
public class EntityRelationEntityStepCostEstimator implements StepCostEstimator<Plan, CountEstimatesCost, M1StepCostEstimatorContext> {
    //region Static
    /**
     * ********************************************************
     * Calculate estimates for a full step.
     * Algorithm description:
     * Step = E1 ------- Rel + filter(+ E2 pushdown props) ------> E2 + filter( without pushdown props)
     * <p>
     * N1 = Prior estimate for E1 count
     * <p>
     * calculations:
     * R1 (relation estimate based on E1 count and global selectivity) = N1 * GS
     * R2 (relation filter estimate) = statistical_estimate(Rel + filter + E1 pushdown)
     * R (rel estimate) = min(R1, R2)
     * <p>
     * Z (E2 node pushdown props only estimate) = statistical_estimate(E1 pushdown filter only)
     * alpha - GS ratio, > 1
     * N2-1 (relation based estimate for E2) = min(R*alpha/GS, Z)
     * N2-2 (E2 complete estimate) = statistical_estimate(E1 + filter (with pushdown))
     * <p>
     * N2 = min(N2-1, N2-2)
     * <p>
     * lambda = (R/R1)*(N2/N2-1)
     * N1' = lambda*N1 (back propagate count estimate)
     * ********************************************************
     * @param config
     * @param statisticsProvider
     * @param previousPlanCost
     * @param step
     * @return
     */
    public static StepCostEstimator.Result<Plan, CountEstimatesCost> calculateFullStep(
            CostEstimationConfig config,
            StatisticsProvider statisticsProvider,
            PlanWithCost<Plan, PlanDetailedCost> previousPlanCost,
            EntityRelationEntityStep step) {

        EntityOp start = step.getStart();
        EntityFilterOp startFilter = step.getStartFilter();
        EntityOp end = step.getEnd();
        EntityFilterOp endFilter = step.getEndFilter();
        RelationOp rel = step.getRel();
        RelationFilterOp relationFilter = step.getRelFilter();

        PlanDetailedCost previousCost = previousPlanCost.getCost();
        CountEstimatesCost entityOneCost = previousCost.getPlanStepCost(start).get().getCost();

        //edge estimate =>
        Direction direction = Direction.of(rel.getAsgEBase().geteBase().getDir());
        //(relation estimate based on E1 count and global selectivity) = N1 * GS
        long selectivity = statisticsProvider.getGlobalSelectivity(rel.getAsgEBase().geteBase(),
                relationFilter.getAsgEBase().geteBase(),
                start.getAsgEBase().geteBase(), direction);
        double N1 = entityOneCost.peek();
        double R1 = N1 * selectivity;
        //(relation filter estimate) = statistical_estimate(Rel + filter + E1 pushdown)
        double R2 = statisticsProvider.getEdgeFilterStatistics(relationFilter.getRel().geteBase(), relationFilter.getAsgEBase().geteBase()).getTotal();
        //(rel estimate) = min(R1, R2)
        double R = Math.min(R1, R2);

        EPropGroup clone = endFilter.getAsgEBase().geteBase().clone();
        List<RelProp> pushdownProps = new LinkedList<>();
        List<RelProp> collect = relationFilter.getAsgEBase().geteBase().getProps().stream().filter(f -> (f instanceof PushdownRelProp) &&
                (!f.getpType().equals(OntologyFinalizer.ID_FIELD_P_TYPE) &&
                        (!f.getpType().equals(OntologyFinalizer.TYPE_FIELD_P_TYPE))))
                .collect(Collectors.toList());
        collect.forEach(p -> {
            pushdownProps.add(p);
            clone.getProps().add(EProp.of(p.getpType(), p.geteNum(), p.getCon()));
        });

        // Z (E2 node pushdown props only estimate) = statistical_estimate(E1 pushdown filter only)
        double Z = statisticsProvider.getRedundantNodeStatistics(end.getAsgEBase().geteBase(), RelPropGroup.of(pushdownProps)).getTotal();

        // N2-1 (relation based estimate for E2) = min(R*alpha/GS, Z)
        double N2_1 = Math.min(R * config.getAlpha(), Z);
        //N2_2 (E2 complete estimate) = statistical_estimate(E1 + filter (with pushdown))
        double N2_2 = statisticsProvider.getNodeFilterStatistics(end.getAsgEBase().geteBase(), clone).getTotal();

        //* N2 = min(N2-1, N2-2)
        double N2 = Math.min(N2_1, N2_2);

        //estimate back propagation weight
        double lambdaEdge = R / R2;
        double lambdaNode = N2 / N2_2;
        // lambda = (R/R1)*(N2/N2-1)
        double lambda = lambdaEdge * lambdaNode;

        //estimation if zero since the real estimation is residing on the adjacent filter (rel filter)
        DoubleCost relCost = new DetailedCost(R * config.getDelta(), lambdaNode, lambdaEdge, R, N2);

        CountEstimatesCost newEntityOneCost = new CountEstimatesCost(entityOneCost.getCost(), N1);
        newEntityOneCost.push(N1*lambda);
        PlanWithCost<Plan, CountEstimatesCost> entityOnePlanCost = new PlanWithCost<>(new Plan(start, startFilter), newEntityOneCost);

        CountEstimatesCost newRelCost = new CountEstimatesCost(relCost.getCost(), R);
        PlanWithCost<Plan, CountEstimatesCost> relPlanCost = new PlanWithCost<>(new Plan(rel, relationFilter), newRelCost);

        CountEstimatesCost entityTwoCost = new CountEstimatesCost(N2, N2);
        PlanWithCost<Plan, CountEstimatesCost> entityTwoOpCost = new PlanWithCost<>(new Plan(end, endFilter), entityTwoCost);

        return StepCostEstimator.Result.of(lambda, entityOnePlanCost, relPlanCost, entityTwoOpCost);
    }
    //endregion

    //region Constructors
    public EntityRelationEntityStepCostEstimator(CostEstimationConfig config) {
        this.config = config;
    }
    //endregion

    //region StepPatternCostEstimator Implementation
    @Override
    public StepCostEstimator.Result<Plan, CountEstimatesCost> estimate(Step step, M1StepCostEstimatorContext context) {
        if (!step.getClass().equals(EntityRelationEntityStep.class)) {
            return StepCostEstimator.EmptyResult.get();
        }

        return calculateFullStep(config, context.getStatisticsProvider(), context.getPreviousCost().get(), (EntityRelationEntityStep)step);
    }
    //endregion

    //region Fields
    private CostEstimationConfig config;
    //endregion
}