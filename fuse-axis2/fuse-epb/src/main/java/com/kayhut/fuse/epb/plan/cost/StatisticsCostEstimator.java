package com.kayhut.fuse.epb.plan.cost;

import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.kayhut.fuse.epb.plan.statistics.StatisticsProvider;
import com.kayhut.fuse.model.execution.plan.*;
import com.kayhut.fuse.model.execution.plan.costs.Cost;
import com.kayhut.fuse.model.execution.plan.costs.PlanDetailedCost;
import javaslang.Tuple2;

import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.kayhut.fuse.epb.plan.cost.StatisticsCostEstimator.StatisticsCostEstimatorNames.*;
import static com.kayhut.fuse.model.Utils.pattern;
import static com.kayhut.fuse.model.execution.plan.Plan.contains;

/**
 * Created by moti on 01/04/2017.
 */
public class StatisticsCostEstimator implements CostEstimator<Plan, PlanDetailedCost> {
    public enum StatisticsCostEstimatorPatterns {
        //option2
        FULL_STEP("^(?<" + ENTITY_ONE.value + ">" + EntityOp.class.getSimpleName() + ")" + ":" + "(?<" + OPTIONAL_ENTITY_ONE_FILTER.value + ">" + EntityFilterOp.class.getSimpleName() + ":)?" +
                "(?<" + RELATION.value + ">" + RelationOp.class.getSimpleName() + ")" + ":" + "(?<" + OPTIONAL_REL_FILTER.value + ">" + RelationFilterOp.class.getSimpleName() + ":)?" +
                "(?<" + ENTITY_TWO.value + ">" + EntityOp.class.getSimpleName() + ")" + "(:" + "(?<" + OPTIONAL_ENTITY_TWO_FILTER.value + ">" + EntityFilterOp.class.getSimpleName() + "))?$"),
        //option 1
        SINGLE_MODE("^(?<" + ENTITY_ONLY.value + ">" + EntityOp.class.getSimpleName() + ")" + "(:" + "(?<" + OPTIONAL_ENTITY_ONLY_FILTER.value + ">" + EntityFilterOp.class.getSimpleName() + "))?$"),
        //option 3 And node
        AND_MODE("^(?<" + AND_MODE_ENTITY_ONE.value+">" + EntityOp.class.getSimpleName() + ")" + ":" + "(?<" + AND_MODE_OPTIONAL_ENTITY_ONE_FILTER.value + ">" + EntityFilterOp.class.getSimpleName() + ":)?" +
                "(?<" + AND_MODE_ENTITY_TWO.value + ">" + EntityOp.class.getSimpleName() + ")" + "(:" + "(?<" + AND_MODE_OPTIONAL_ENTITY_TWO_FILTER.value + ">" + EntityFilterOp.class.getSimpleName() + "))?$");

        private String pattern;

        StatisticsCostEstimatorPatterns(String pattern) {
            this.pattern = pattern;
        }

        public String pattern() {
            return pattern;
        }
    }

    public enum StatisticsCostEstimatorNames {
        ENTITY_ONE("entityOne"),
        OPTIONAL_ENTITY_ONE_FILTER("optionalEntityOneFilter"),
        ENTITY_TWO("entityTwo"),
        OPTIONAL_ENTITY_TWO_FILTER("optionalEntityTwoFilter"),
        RELATION("relation"),
        OPTIONAL_REL_FILTER("optionalRelFilter"),
        ENTITY_ONLY("entityOnly"),
        OPTIONAL_ENTITY_ONLY_FILTER("optionalEntityOnlyFilter"),
        AND_MODE_ENTITY_ONE("andEntityOne"),
        AND_MODE_OPTIONAL_ENTITY_ONE_FILTER("andOptionalEntityOneFilter"),
        AND_MODE_ENTITY_TWO("andEntityTwo"),
        AND_MODE_OPTIONAL_ENTITY_TWO_FILTER("andOptionalEntityTwoFilter");

        private String value;

        StatisticsCostEstimatorNames(String value) {
            this.value = value;
        }

        public String value() {
            return value;
        }

        static StatisticsCostEstimatorNames of(String name) {
            return Arrays.stream(values()).filter(v -> v.value.equals(name)).findFirst().get();
        }
    }

    private StatisticsProvider statisticsProvider;

    @Inject
    public StatisticsCostEstimator(StatisticsProvider statisticsProvider) {
        this.statisticsProvider = statisticsProvider;
    }

    @Override
    public PlanWithCost<Plan, PlanDetailedCost> estimate(Plan plan, Optional<PlanWithCost<Plan, PlanDetailedCost>> previousCost) {
        PlanWithCost<Plan, PlanDetailedCost> newPlan = null;
        List<PlanOpBase> step = plan.getOps();
        if (previousCost.isPresent()) {
            step = extractNewSteps(plan);
        }
        String opsString = pattern(step);
        StatisticsCostEstimatorPatterns[] supportedPattern = getSupportedPattern();
        for (StatisticsCostEstimatorPatterns pattern : supportedPattern) {
            Pattern compile = Pattern.compile(pattern.pattern());
            Matcher matcher = compile.matcher(opsString);
            if (matcher.find()) {
                Map<StatisticsCostEstimatorNames, PlanOpBase> map = extractStep(step, getNamedGroups(compile), matcher);
                Tuple2<Double, List<PlanOpWithCost<Cost>>> tuple2 = calculate(map, pattern, previousCost);
                newPlan = buildNewPlan(tuple2, previousCost);
                break;
            }
        }
        return newPlan;
    }

    private PlanWithCost<Plan, PlanDetailedCost> buildNewPlan(Tuple2<Double,List<PlanOpWithCost<Cost>>> tuple2, Optional<PlanWithCost<Plan, PlanDetailedCost>> previousCost) {
        AtomicReference<Cost> completePlanCost = new AtomicReference<>();
        List<PlanOpWithCost<Cost>> planOpWithCosts;
        if (previousCost.isPresent()) {
            completePlanCost.set(previousCost.get().getCost().getGlobalCost());
            planOpWithCosts = Lists.newArrayList(previousCost.get().getCost().getOpCosts());
        } else {
            completePlanCost.set(new Cost(0, 0));
            planOpWithCosts = new ArrayList<>();
        }

        double lambda = tuple2._1;
        planOpWithCosts.forEach(element-> {
            if(element.getOpBase().get(0) instanceof EntityOp) {
                element.push(element.peek()*lambda);
            }
        });

        List<PlanOpWithCost<Cost>> costs = tuple2._2;
        costs.forEach(c -> {
            //add new step into plan
            if (!previousCost.isPresent() || !contains(previousCost.get().getPlan(), c.getOpBase().get(0))) {
                planOpWithCosts.add(c);
                completePlanCost.set(new Cost(completePlanCost.get().cost + c.getCost().cost, 0));
            }
        });

        Plan newPlan = new Plan(planOpWithCosts.stream().flatMap(p -> p.getOpBase().stream()).collect(Collectors.toList()));
        PlanDetailedCost newCost = new PlanDetailedCost(completePlanCost.get(), planOpWithCosts);
        return new PlanWithCost<>(newPlan, newCost);
    }

    public Tuple2<Double,List<PlanOpWithCost<Cost>>> calculate(Map<StatisticsCostEstimatorNames, PlanOpBase> map, StatisticsCostEstimatorPatterns pattern, Optional<PlanWithCost<Plan, PlanDetailedCost>> previousCost) {
        switch (pattern) {
            case FULL_STEP:
                return calculateFullStep(map, previousCost.get());
            case SINGLE_MODE:
                return calculateSingleNodeStep(map);
            case AND_MODE:
                return calculateAndStep(map, previousCost.get());
        }
        throw new RuntimeException("No Appropriate pattern found [" + pattern + "]");
    }

    private Tuple2<Double, List<PlanOpWithCost<Cost>>> calculateAndStep(Map<StatisticsCostEstimatorNames, PlanOpBase> map, PlanWithCost<Plan, PlanDetailedCost> planPlanDetailedCostPlanWithCost) {
        EntityOp entityOp = (EntityOp) map.get(AND_MODE_ENTITY_TWO);
        if (!map.containsKey(AND_MODE_OPTIONAL_ENTITY_TWO_FILTER)) {
            map.put(AND_MODE_OPTIONAL_ENTITY_TWO_FILTER, new EntityFilterOp());
        }

        EntityFilterOp filterOp = (EntityFilterOp) map.get(AND_MODE_OPTIONAL_ENTITY_TWO_FILTER);
        filterOp.setEntity(entityOp.getEntity());

        PlanOpWithCost<Cost> entityLatestOp = planPlanDetailedCostPlanWithCost.getCost().getPlanOpByEntity(entityOp.getEntity().geteBase()).get();
        return new Tuple2<>(1d,Collections.singletonList(new PlanOpWithCost<>(new Cost(0, (long)Math.ceil(entityLatestOp.peek())),entityLatestOp.peek(), entityOp, filterOp)));

    }

    private Tuple2<Double,List<PlanOpWithCost<Cost>>> calculateFullStep(Map<StatisticsCostEstimatorNames, PlanOpBase> map, PlanWithCost<Plan, PlanDetailedCost> previousCost) {
        //entity one
        EntityOp entityOneOp = (EntityOp) map.get(ENTITY_ONE);
        if (!map.containsKey(OPTIONAL_ENTITY_ONE_FILTER)) {
            map.put(OPTIONAL_ENTITY_ONE_FILTER, new EntityFilterOp());
        }

        EntityFilterOp filterOneOp = (EntityFilterOp) map.get(OPTIONAL_ENTITY_ONE_FILTER);
        //set entity type on this kaka
        filterOneOp.setEntity(entityOneOp.getEntity());

        //relation
        RelationOp rel = (RelationOp) map.get(RELATION);

        if (!map.containsKey(OPTIONAL_REL_FILTER)) {
            map.put(OPTIONAL_REL_FILTER, new RelationFilterOp());
        }
        RelationFilterOp relFilterOp = (RelationFilterOp) map.get(OPTIONAL_REL_FILTER);
        //set entity type on this kaka
        relFilterOp.setRel(rel.getRelation());

        //entity
        EntityOp entityTwoOp = (EntityOp) map.get(ENTITY_TWO);
        if (!map.containsKey(OPTIONAL_ENTITY_TWO_FILTER)) {
            map.put(OPTIONAL_ENTITY_TWO_FILTER, new EntityFilterOp());
        }

        EntityFilterOp filterTwoOp = (EntityFilterOp) map.get(OPTIONAL_ENTITY_TWO_FILTER);
        //set entity type on this kaka
        filterTwoOp.setEntity(entityTwoOp.getEntity());

        //calculate
        //get node 1 cost from existing cost with plan
        Cost entityOneCost = previousCost.getCost().getOpCost(entityOneOp).get();

        //edge estimate =>
        Direction direction = Direction.of(rel.getRelation().geteBase().getDir());
        double edgeEstimation_N1 = entityOneCost.total * statisticsProvider.getGlobalSelectivity(rel.getRelation().geteBase(),entityOneOp.getEntity().geteBase(),direction);

        //redundant
        //C1_e
        double C1_e = statisticsProvider.getRedundantEdgeStatistics(relFilterOp.getRel().geteBase(), filterOneOp.getEntity().geteBase(), filterOneOp.getEprop().geteBase(), direction).getCardinality()._1;
        //C_2e
        double C2_e = statisticsProvider.getRedundantEdgeStatistics(relFilterOp.getRel().geteBase(), filterTwoOp.getEntity().geteBase(), filterTwoOp.getEprop().geteBase(), direction.reverse()).getCardinality()._1;
        //relation
        double C3_v = statisticsProvider.getEdgeStatistics(relFilterOp.getRel().geteBase()).getCardinality()._1;
        double C3_filter = statisticsProvider.getEdgeFilterStatistics(relFilterOp.getRel().geteBase(),relFilterOp.getRelProp().geteBase()).getCardinality()._1;
        //get min
        double edgeEstimation = Collections.min(Arrays.asList(C3_v, C3_filter, C1_e, C2_e, edgeEstimation_N1));
        //cost if zero since the real cost is residing on the adjacent filter (rel filter)
        Cost relCost = new Cost(edgeEstimation, (long) edgeEstimation);

        //get entity one statistics calculated in prior step

        double entityTwoCard = statisticsProvider.getNodeStatistics(entityTwoOp.getEntity().geteBase()).getCardinality()._1;
        double filterTowCard = entityTwoCard;
        if (filterTwoOp.getEprop() != null) {
            filterTowCard = statisticsProvider.getNodeFilterStatistics(entityTwoOp.getEntity().geteBase(),filterTwoOp.getEprop().geteBase()).getCardinality()._1;
        }

        //node redundand stats: C_2e
        double nodeEstimate_C2_e = statisticsProvider.getRedundantEdgeStatistics(rel.getRelation().geteBase(),entityTwoOp.getEntity().geteBase(), filterTwoOp.getEprop().geteBase(), direction.reverse()).getCardinality()._1;

        //node 2 cardinality estimation
        double N2 = Collections.min(Arrays.asList(entityTwoCard, filterTowCard, edgeEstimation));

        //calculate back propagation weight
        double lambdaEdge = edgeEstimation / edgeEstimation_N1;
        double lambdaNode = N2 / nodeEstimate_C2_e;
        double lambda = Math.min(lambdaEdge, lambdaNode);

        PlanOpWithCost entityOneOpCost = new PlanOpWithCost<>(entityOneCost, lambda, entityOneOp, filterOneOp);
        PlanOpWithCost relOpCost = new PlanOpWithCost<>(relCost, edgeEstimation, rel, relFilterOp);
        PlanOpWithCost entityTwoOpCost = new PlanOpWithCost<>(new Cost(N2, (long) N2), N2, entityTwoOp, filterTwoOp);

        return new Tuple2<>(lambda, Arrays.asList(entityOneOpCost, relOpCost, entityTwoOpCost));
        //return new StepEstimator(edgeEstimation,N2,lambda);
    }

    private Tuple2<Double,List<PlanOpWithCost<Cost>>> calculateSingleNodeStep(Map<StatisticsCostEstimatorNames, PlanOpBase> map) {
        EntityOp entityOp = (EntityOp) map.get(ENTITY_ONLY);
        if (!map.containsKey(OPTIONAL_ENTITY_ONLY_FILTER)) {
            map.put(OPTIONAL_ENTITY_ONLY_FILTER, new EntityFilterOp());
        }

        EntityFilterOp filterOp = (EntityFilterOp) map.get(OPTIONAL_ENTITY_ONLY_FILTER);
        //set entity type on this kaka
        filterOp.setEntity(entityOp.getEntity());
        //calculate
        double entityTotal = statisticsProvider.getNodeStatistics(entityOp.getEntity().geteBase()).getCardinality()._1;
        double filterTotal = entityTotal;
        if (filterOp.getEprop() != null) {
            filterTotal = statisticsProvider.getNodeFilterStatistics(entityOp.getEntity().geteBase(),filterOp.getEprop().geteBase()).getCardinality()._1;
        }

        double min = Math.min(entityTotal, filterTotal);
        return new Tuple2<>(1d,Collections.singletonList(new PlanOpWithCost<>(new Cost(min, min), min, entityOp, filterOp)));
    }

    private Map<StatisticsCostEstimatorNames, PlanOpBase> extractStep(List<PlanOpBase> step, Map<String, Integer> groups, Matcher matcher) {
        Map<StatisticsCostEstimatorNames, PlanOpBase> map = new HashMap<>();
        TreeSet<Map.Entry<String, Integer>> entries = new TreeSet<>(Comparator.comparingInt(Map.Entry::getValue));
        entries.addAll(groups.entrySet());
        int stepIndex = 0;
        for (Map.Entry<String, Integer> entry : entries) {
            if (matcher.group(entry.getKey()) != null) {
                map.put(StatisticsCostEstimatorNames.of(entry.getKey()), step.get(stepIndex));
                stepIndex++;
            }
        }

        return map;
    }

    private List<String> extractGroups(Matcher matcher) {
        return Arrays.stream(values()).map(v -> matcher.group(v.value())).filter(Objects::nonNull).collect(Collectors.toList());
    }

    /**
     * extract step from the end of the current plan
     *
     * @param current
     * @return
     */
    private List<PlanOpBase> extractNewSteps(Plan current) {
        List<PlanOpBase> newPlan = new ArrayList<>();
        List<PlanOpBase> ops = current.getOps();
        int entityCounter = 0;
        int i = ops.size() - 1;
        while (i >= 0 && entityCounter < 2) {
            if (ops.get(i).getClass().equals(EntityOp.class)) {
                entityCounter++;
            }
            newPlan.add(0, ops.get(i));
            i--;
        }
        if (entityCounter > 0)
            return newPlan;
        return Collections.emptyList();
    }

    public static StatisticsCostEstimatorPatterns[] getSupportedPattern() {
        return StatisticsCostEstimatorPatterns.values();
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Integer> getNamedGroups(Pattern regex) {
        try {
            Method namedGroupsMethod = Pattern.class.getDeclaredMethod("namedGroups");
            namedGroupsMethod.setAccessible(true);

            Map<String, Integer> namedGroups = null;
            namedGroups = (Map<String, Integer>) namedGroupsMethod.invoke(regex);

            if (namedGroups == null) {
                throw new InternalError();
            }

            return Collections.unmodifiableMap(namedGroups);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}