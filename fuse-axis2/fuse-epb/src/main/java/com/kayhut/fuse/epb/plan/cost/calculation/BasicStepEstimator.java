package com.kayhut.fuse.epb.plan.cost.calculation;

import com.kayhut.fuse.dispatcher.utils.PlanUtil;
import com.kayhut.fuse.epb.plan.cost.StatisticsCostEstimator;
import com.kayhut.fuse.epb.plan.statistics.StatisticsProvider;
import com.kayhut.fuse.model.execution.plan.*;
import com.kayhut.fuse.model.execution.plan.costs.Cost;
import com.kayhut.fuse.model.execution.plan.costs.DetailedCost;
import com.kayhut.fuse.model.execution.plan.costs.PlanDetailedCost;
import com.kayhut.fuse.model.ontology.OntologyFinalizer;
import com.kayhut.fuse.model.query.properties.*;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.kayhut.fuse.epb.plan.cost.StatisticsCostEstimator.StatisticsCostEstimatorNames.*;

/**
 * Created by moti on 5/16/2017.
 */
public class BasicStepEstimator implements StepEstimator {
    private double alpha;
    private double delta;

    public BasicStepEstimator(double alpha, double delta) {
        this.alpha = alpha;
        this.delta = delta;
    }

    public StepEstimatorResult calculate(StatisticsProvider statisticsProvider, Map<StatisticsCostEstimator.StatisticsCostEstimatorNames, PlanOpBase> map, StatisticsCostEstimator.StatisticsCostEstimatorPatterns pattern, Optional<PlanWithCost<Plan, PlanDetailedCost>> previousCost) {
        switch (pattern) {
            case FULL_STEP:
                return calculateFullStep(statisticsProvider, map, previousCost.get());
            case SINGLE_MODE:
                return calculateSingleNodeStep(statisticsProvider, map);
            case GOTO_MODE:
                return calculateGoToStep(statisticsProvider,map, previousCost.get());
        }
        throw new RuntimeException("No Appropriate pattern found [" + pattern + "]");
    }

    private StepEstimatorResult calculateGoToStep(StatisticsProvider statisticsProvider,Map<StatisticsCostEstimator.StatisticsCostEstimatorNames, PlanOpBase> map, PlanWithCost<Plan, PlanDetailedCost> planPlanDetailedCostPlanWithCost) {
        GoToEntityOp gotoOp = (GoToEntityOp) map.get(GOTO_ENTITY);

        PlanOpBase entityOp = planPlanDetailedCostPlanWithCost.getPlan().getOps().stream().
                filter(op -> (op instanceof EntityOp) && ((EntityOp) op).getAsgEBase().geteBase().equals(gotoOp.getAsgEBase().geteBase())).
                findFirst().get();
        EntityFilterOp filterOp = (EntityFilterOp) PlanUtil.adjacentNext(planPlanDetailedCostPlanWithCost.getPlan(), entityOp).get();

        map.put(ENTITY_ONE, entityOp);
        map.put(OPTIONAL_ENTITY_ONE_FILTER, filterOp);

        StepEstimatorResult stepEstimatorResult = calculateFullStep(statisticsProvider, map, planPlanDetailedCostPlanWithCost);
        Cost gotoCost = new Cost(0);

        return StepEstimatorResult.of(stepEstimatorResult.lambda(), new PlanOpWithCost<Cost>(gotoCost, 0,gotoOp), stepEstimatorResult.planOpWithCosts().get(1), stepEstimatorResult.planOpWithCosts().get(2));

    }

    /**
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
     *
     * @param statisticsProvider
     * @param map
     * @param previousCost
     * @return
     */
    private StepEstimatorResult calculateFullStep(StatisticsProvider statisticsProvider, Map<StatisticsCostEstimator.StatisticsCostEstimatorNames, PlanOpBase> map, PlanWithCost<Plan, PlanDetailedCost> previousCost) {
        //entity one
        EntityOp entityOneOp = (EntityOp) map.get(ENTITY_ONE);
        if (!map.containsKey(OPTIONAL_ENTITY_ONE_FILTER)) {
            map.put(OPTIONAL_ENTITY_ONE_FILTER, new EntityFilterOp());
        }

        EntityFilterOp filterOneOp = (EntityFilterOp) map.get(OPTIONAL_ENTITY_ONE_FILTER);
        //set entity type on this kaka
        filterOneOp.setEntity(entityOneOp.getAsgEBase());

        //relation
        RelationOp relationOp = (RelationOp) map.get(RELATION);

        if (!map.containsKey(OPTIONAL_REL_FILTER)) {
            map.put(OPTIONAL_REL_FILTER, new RelationFilterOp());
        }
        RelationFilterOp relFilterOp = (RelationFilterOp) map.get(OPTIONAL_REL_FILTER);
        //set entity type on this kaka
        relFilterOp.setRel(relationOp.getAsgEBase());

        //entity
        EntityOp entityTwoOp = (EntityOp) map.get(ENTITY_TWO);
        if (!map.containsKey(OPTIONAL_ENTITY_TWO_FILTER)) {
            map.put(OPTIONAL_ENTITY_TWO_FILTER, new EntityFilterOp());
        }

        EntityFilterOp filterTwoOp = (EntityFilterOp) map.get(OPTIONAL_ENTITY_TWO_FILTER);
        //set entity type on this kaka
        filterTwoOp.setEntity(entityTwoOp.getAsgEBase());

        //calculate
        //get node 1 cost from existing cost with plan
        PlanDetailedCost cost = previousCost.getCost();
        Cost entityOneCost = cost.getOpCost(entityOneOp).get();

        //edge estimate =>
        Direction direction = Direction.of(relationOp.getAsgEBase().geteBase().getDir());
        //(relation estimate based on E1 count and global selectivity) = N1 * GS
        long selectivity = statisticsProvider.getGlobalSelectivity(relationOp.getAsgEBase().geteBase(),
                relFilterOp.getAsgEBase().geteBase(),
                entityOneOp.getAsgEBase().geteBase(), direction);
        double N1 = cost.getPlanOpCost(entityOneOp).get().peek();
        double R1 = N1 * selectivity;
        //(relation filter estimate) = statistical_estimate(Rel + filter + E1 pushdown)
        double R2 = statisticsProvider.getEdgeFilterStatistics(relFilterOp.getRel().geteBase(), relFilterOp.getAsgEBase().geteBase()).getTotal();
        //(rel estimate) = min(R1, R2)
        double R = Math.min(R1, R2);

        EPropGroup clone = filterTwoOp.getAsgEBase().geteBase().clone();
        List<RelProp> pushdownProps = new LinkedList<>();
        List<RelProp> collect = relFilterOp.getAsgEBase().geteBase().getProps().stream().filter(f -> (f instanceof PushdownRelProp) &&
                (!f.getpType().equals(Integer.toString(OntologyFinalizer.ID_FIELD_P_TYPE)) &&
                        (!f.getpType().equals(Integer.toString(OntologyFinalizer.TYPE_FIELD_P_TYPE)))))
                .collect(Collectors.toList());
        collect.forEach(p -> {
            pushdownProps.add(p);
            clone.getProps().add(EProp.of(p.getpType(), p.geteNum(), p.getCon()));
        });

        // Z (E2 node pushdown props only estimate) = statistical_estimate(E1 pushdown filter only)
        double Z = statisticsProvider.getRedundantNodeStatistics(entityTwoOp.getAsgEBase().geteBase(), RelPropGroup.of(pushdownProps)).getTotal();

        // N2-1 (relation based estimate for E2) = min(R*alpha/GS, Z)
        double N2_1 = Math.min(R * alpha, Z);
        //N2_2 (E2 complete estimate) = statistical_estimate(E1 + filter (with pushdown))
        double N2_2 = statisticsProvider.getNodeFilterStatistics(entityTwoOp.getAsgEBase().geteBase(), clone).getTotal();

        //* N2 = min(N2-1, N2-2)
        double N2 = Math.min(N2_1, N2_2);

        //calculate back propagation weight
        double lambdaEdge = R / R2;
        double lambdaNode = N2 / N2_2;
        // lambda = (R/R1)*(N2/N2-1)
        double lambda = lambdaEdge * lambdaNode;

        //cost if zero since the real cost is residing on the adjacent filter (rel filter)
        Cost relCost = new DetailedCost(R * delta, lambdaNode, lambdaEdge, R, N2);

        PlanOpWithCost<Cost> entityOneOpCost = new PlanOpWithCost<>(entityOneCost, N1, entityOneOp, filterOneOp);
        entityOneOpCost.push(N1*lambda);
        PlanOpWithCost<Cost> relOpCost = new PlanOpWithCost<>(relCost, R, relationOp, relFilterOp);
        PlanOpWithCost<Cost> entityTwoOpCost = new PlanOpWithCost<>(new DetailedCost(N2, lambdaNode, lambdaEdge, R, N2), N2, entityTwoOp, filterTwoOp);

        return StepEstimatorResult.of(lambda, entityOneOpCost, relOpCost, entityTwoOpCost);
        //return new StepEstimator(edgeEstimation,N2,lambda);
    }

    private StepEstimatorResult calculateSingleNodeStep(StatisticsProvider statisticsProvider, Map<StatisticsCostEstimator.StatisticsCostEstimatorNames, PlanOpBase> map) {
        EntityOp entityOp = (EntityOp) map.get(ENTITY_ONLY);
        if (!map.containsKey(OPTIONAL_ENTITY_ONLY_FILTER)) {
            map.put(OPTIONAL_ENTITY_ONLY_FILTER, new EntityFilterOp());
        }

        EntityFilterOp filterOp = (EntityFilterOp) map.get(OPTIONAL_ENTITY_ONLY_FILTER);
        //set entity type on this kaka
        filterOp.setEntity(entityOp.getAsgEBase());
        //calculate
        double entityTotal = statisticsProvider.getNodeStatistics(entityOp.getAsgEBase().geteBase()).getTotal();
        double filterTotal = entityTotal;
        if (filterOp.getAsgEBase() != null) {
            filterTotal = statisticsProvider.getNodeFilterStatistics(entityOp.getAsgEBase().geteBase(), filterOp.getAsgEBase().geteBase()).getTotal();
        }

        double min = Math.min(entityTotal, filterTotal);
        return StepEstimatorResult.of(1d, new PlanOpWithCost<>(new Cost(min), min, entityOp, filterOp));
    }

}
