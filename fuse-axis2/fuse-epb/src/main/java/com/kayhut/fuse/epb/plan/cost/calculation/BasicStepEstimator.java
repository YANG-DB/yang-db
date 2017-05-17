package com.kayhut.fuse.epb.plan.cost.calculation;

import com.kayhut.fuse.epb.plan.cost.StatisticsCostEstimator;
import com.kayhut.fuse.epb.plan.statistics.StatisticsProvider;
import com.kayhut.fuse.model.execution.plan.*;
import com.kayhut.fuse.model.execution.plan.costs.Cost;
import com.kayhut.fuse.model.execution.plan.costs.PlanDetailedCost;
import com.kayhut.fuse.model.query.properties.*;
import javaslang.Tuple2;

import java.util.*;
import java.util.stream.Collectors;

import static com.kayhut.fuse.epb.plan.cost.StatisticsCostEstimator.StatisticsCostEstimatorNames.*;

/**
 * Created by moti on 5/16/2017.
 */
public class BasicStepEstimator implements StepEstimator {
    private double alpha;

    public BasicStepEstimator(double alpha) {
        this.alpha = alpha;
    }

    public Tuple2<Double, List<PlanOpWithCost<Cost>>> calculate(StatisticsProvider statisticsProvider, Map<StatisticsCostEstimator.StatisticsCostEstimatorNames, PlanOpBase> map, StatisticsCostEstimator.StatisticsCostEstimatorPatterns pattern, Optional<PlanWithCost<Plan, PlanDetailedCost>> previousCost) {
        switch (pattern) {
            case FULL_STEP:
                return calculateFullStep(statisticsProvider, map, previousCost.get());
            case SINGLE_MODE:
                return calculateSingleNodeStep(statisticsProvider, map);
            case AND_MODE:
                return calculateAndStep(map, previousCost.get());
        }
        throw new RuntimeException("No Appropriate pattern found [" + pattern + "]");
    }

    private Tuple2<Double, List<PlanOpWithCost<Cost>>> calculateAndStep(Map<StatisticsCostEstimator.StatisticsCostEstimatorNames, PlanOpBase> map, PlanWithCost<Plan, PlanDetailedCost> planPlanDetailedCostPlanWithCost) {
        EntityOp entityOp = (EntityOp) map.get(AND_MODE_ENTITY_TWO);
        if (!map.containsKey(AND_MODE_OPTIONAL_ENTITY_TWO_FILTER)) {
            map.put(AND_MODE_OPTIONAL_ENTITY_TWO_FILTER, new EntityFilterOp());
        }

        EntityFilterOp filterOp = (EntityFilterOp) map.get(AND_MODE_OPTIONAL_ENTITY_TWO_FILTER);
        filterOp.setEntity(entityOp.getAsgEBase());

        PlanOpWithCost<Cost> entityLatestOp = planPlanDetailedCostPlanWithCost.getCost().getPlanOpByEntity(entityOp.getAsgEBase().geteBase()).get();
        return new Tuple2<>(1d, Collections.singletonList(new PlanOpWithCost<>(new Cost(0, (long) Math.ceil(entityLatestOp.peek())), entityLatestOp.peek(), entityOp, filterOp)));

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
    private Tuple2<Double, List<PlanOpWithCost<Cost>>> calculateFullStep(StatisticsProvider statisticsProvider, Map<StatisticsCostEstimator.StatisticsCostEstimatorNames, PlanOpBase> map, PlanWithCost<Plan, PlanDetailedCost> previousCost) {
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
        Cost entityOneCost = previousCost.getCost().getOpCost(entityOneOp).get();

        //edge estimate =>
        Direction direction = Direction.of(relationOp.getAsgEBase().geteBase().getDir());
        //(relation estimate based on E1 count and global selectivity) = N1 * GS
        long selectivity = statisticsProvider.getGlobalSelectivity(relationOp.getAsgEBase().geteBase(),
                relFilterOp.getAsgEBase().geteBase(),
                entityOneOp.getAsgEBase().geteBase(), direction);
        double R1 = entityOneCost.total * selectivity;
        //(relation filter estimate) = statistical_estimate(Rel + filter + E1 pushdown)
        double R2 = statisticsProvider.getEdgeFilterStatistics(relFilterOp.getRel().geteBase(), relFilterOp.getAsgEBase().geteBase()).getCardinality();
        //(rel estimate) = min(R1, R2)
        double R = Math.min(R1, R2);

        EPropGroup clone = filterOneOp.getAsgEBase().geteBase().clone();
        List<RelProp> pushdownProps = new LinkedList<>();
        List<RelProp> collect = relFilterOp.getAsgEBase().geteBase().getProps().stream().filter(f -> f instanceof PushdownRelProp).collect(Collectors.toList());
        collect.forEach(p -> {
            pushdownProps.add(p);
            clone.getProps().add(EProp.of(p.getpType(), p.geteNum(), p.getCon()));
        });

        // Z (E2 node pushdown props only estimate) = statistical_estimate(E1 pushdown filter only)
        double Z = statisticsProvider.getRedundantNodeStatistics(entityTwoOp.getAsgEBase().geteBase(), RelPropGroup.of(pushdownProps)).getCardinality();

        // N2-1 (relation based estimate for E2) = min(R*alpha/GS, Z)
        double N2_1 = Math.min(R/selectivity*alpha,Z);
        //N2_2 (E2 complete estimate) = statistical_estimate(E1 + filter (with pushdown))
        double N2_2 = statisticsProvider.getNodeFilterStatistics(entityTwoOp.getAsgEBase().geteBase(), clone).getCardinality();

        //* N2 = min(N2-1, N2-2)
        double N2 = Math.min(N2_1,N2_2);

        //calculate back propagation weight
        double lambdaEdge = R / R2;
        double lambdaNode = N2 / N2_2;
        // lambda = (R/R1)*(N2/N2-1)
        double lambda = Math.min(lambdaEdge, lambdaNode);

        //cost if zero since the real cost is residing on the adjacent filter (rel filter)
        Cost relCost = new Cost(R, (long) R);

        PlanOpWithCost entityOneOpCost = new PlanOpWithCost<>(entityOneCost, lambda, entityOneOp, filterOneOp);
        PlanOpWithCost relOpCost = new PlanOpWithCost<>(relCost, R, relationOp, relFilterOp);
        PlanOpWithCost entityTwoOpCost = new PlanOpWithCost<>(new Cost(N2, (long) N2), N2, entityTwoOp, filterTwoOp);

        return new Tuple2<>(lambda, Arrays.asList(entityOneOpCost, relOpCost, entityTwoOpCost));
        //return new StepEstimator(edgeEstimation,N2,lambda);
    }

    private Tuple2<Double, List<PlanOpWithCost<Cost>>> calculateSingleNodeStep(StatisticsProvider statisticsProvider, Map<StatisticsCostEstimator.StatisticsCostEstimatorNames, PlanOpBase> map) {
        EntityOp entityOp = (EntityOp) map.get(ENTITY_ONLY);
        if (!map.containsKey(OPTIONAL_ENTITY_ONLY_FILTER)) {
            map.put(OPTIONAL_ENTITY_ONLY_FILTER, new EntityFilterOp());
        }

        EntityFilterOp filterOp = (EntityFilterOp) map.get(OPTIONAL_ENTITY_ONLY_FILTER);
        //set entity type on this kaka
        filterOp.setEntity(entityOp.getAsgEBase());
        //calculate
        double entityTotal = statisticsProvider.getNodeStatistics(entityOp.getAsgEBase().geteBase()).getCardinality();
        double filterTotal = entityTotal;
        if (filterOp.getAsgEBase() != null) {
            filterTotal = statisticsProvider.getNodeFilterStatistics(entityOp.getAsgEBase().geteBase(), filterOp.getAsgEBase().geteBase()).getCardinality();
        }

        double min = Math.min(entityTotal, filterTotal);
        return new Tuple2<>(1d, Collections.singletonList(new PlanOpWithCost<>(new Cost(min, min), min, entityOp, filterOp)));
    }

}
