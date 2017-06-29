package com.kayhut.fuse.epb.plan.estimation.step;

import com.kayhut.fuse.epb.plan.estimation.StatisticsCostEstimator;
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
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Created by moti on 29/05/2017.
 *
 */
public class FullStepPatternCostEstimator implements StepPatternCostEstimator {
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
    public static StepCostEstimator.StepEstimatorResult calculateFullStep(
            CostEstimationConfig config,
            StatisticsProvider statisticsProvider,
            PlanWithCost<Plan, PlanDetailedCost> previousPlanCost,
            Step step) {

        EntityOp start = step.start()._1;
        EntityFilterOp startFilter = step.start()._2;
        EntityOp end = step.end()._1;
        EntityFilterOp endFilter = step.end()._2;
        RelationOp rel = step.rel()._1;
        RelationFilterOp relationFilter = step.rel()._2;

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

        //calculate back propagation weight
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

        return StepCostEstimator.StepEstimatorResult.of(lambda, entityOnePlanCost, relPlanCost, entityTwoOpCost);
    }
    //endregion

    //region Constructors
    public FullStepPatternCostEstimator(CostEstimationConfig config) {
        this.config = config;
    }
    //endregion

    //region StepPatternCostEstimator Implementation
    @Override
    public StepCostEstimator.StepEstimatorResult estimate(StatisticsProvider statisticsProvider, Map<StatisticsCostEstimator.Token, PlanOpBase> patternParts, Optional<PlanWithCost<Plan, PlanDetailedCost>> previousCost) {
        return calculateFullStep(config, statisticsProvider, previousCost.get(), Step.buildFullStep(patternParts));
    }
    //endregion

    //region Fields
    private CostEstimationConfig config;
    //endregion
}
