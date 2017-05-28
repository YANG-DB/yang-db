package com.kayhut.fuse.epb.plan.cost;

import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.kayhut.fuse.dispatcher.ontolgy.OntologyProvider;
import com.kayhut.fuse.epb.plan.cost.calculation.StepEstimator;
import com.kayhut.fuse.epb.plan.statistics.StatisticsProvider;
import com.kayhut.fuse.epb.plan.statistics.StatisticsProviderFactory;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.execution.plan.*;
import com.kayhut.fuse.model.execution.plan.costs.Cost;
import com.kayhut.fuse.model.execution.plan.costs.PlanDetailedCost;
import com.kayhut.fuse.model.ontology.Ontology;
import com.kayhut.fuse.unipop.schemaProviders.GraphElementSchemaProvider;
import javaslang.collection.Stream;

import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.kayhut.fuse.dispatcher.utils.PlanUtil.extractNewStep;
import static com.kayhut.fuse.epb.plan.cost.StatisticsCostEstimator.StatisticsCostEstimatorNames.*;
import static com.kayhut.fuse.model.Utils.pattern;
import static com.kayhut.fuse.model.execution.plan.Plan.contains;

/**
 * Created by moti on 01/04/2017.
 */
public class StatisticsCostEstimator implements CostEstimator<Plan, PlanDetailedCost, AsgQuery> {
    public enum StatisticsCostEstimatorPatterns {
        //option2
        FULL_STEP("^(?<" + ENTITY_ONE.value + ">" + EntityOp.class.getSimpleName() + ")" + ":" + "(?<" + OPTIONAL_ENTITY_ONE_FILTER.value + ">" + EntityFilterOp.class.getSimpleName() + ":)?" +
                "(?<" + RELATION.value + ">" + RelationOp.class.getSimpleName() + ")" + ":" + "(?<" + OPTIONAL_REL_FILTER.value + ">" + RelationFilterOp.class.getSimpleName() + ":)?" +
                "(?<" + ENTITY_TWO.value + ">" + EntityOp.class.getSimpleName() + ")" + "(:" + "(?<" + OPTIONAL_ENTITY_TWO_FILTER.value + ">" + EntityFilterOp.class.getSimpleName() + "))?$"),
        //option 1
        SINGLE_MODE("^(?<" + ENTITY_ONLY.value + ">" + EntityOp.class.getSimpleName() + ")" + "(:" + "(?<" + OPTIONAL_ENTITY_ONLY_FILTER.value + ">" + EntityFilterOp.class.getSimpleName() + "))?$"),
        //option 3 And node
        GOTO_MODE("^(?<" + GOTO_ENTITY.value+">" + GoToEntityOp.class.getSimpleName() + ")" + ":" +
                "(?<" + RELATION.value + ">" + RelationOp.class.getSimpleName() + ")" + ":" + "(?<" + OPTIONAL_REL_FILTER.value + ">" + RelationFilterOp.class.getSimpleName() + ":)?" +
                "(?<" + ENTITY_TWO.value + ">" + EntityOp.class.getSimpleName() + ")" + "(:" + "(?<" + OPTIONAL_ENTITY_TWO_FILTER.value + ">" + EntityFilterOp.class.getSimpleName() + "))?$");

        private String pattern;
        private static Map<StatisticsCostEstimatorPatterns, Pattern> compiledPatterns;

        static {
            compiledPatterns = new HashMap<>();
            for(StatisticsCostEstimatorPatterns pattern : StatisticsCostEstimatorPatterns.values()){
                Pattern compile = Pattern.compile(pattern.pattern());
                compiledPatterns.put(pattern, compile);
            }
        }

        StatisticsCostEstimatorPatterns(String pattern) {
            this.pattern = pattern;
        }

        public String pattern() {
            return pattern;
        }

        public Pattern getCompiledPattern(){
            return compiledPatterns.get(this);
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
        GOTO_ENTITY("gotoEntity");

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

    private StatisticsProviderFactory statisticsProviderFactory;
    private OntologyProvider ontologyProvider;
    private StepEstimator estimator;

    @Inject
    public StatisticsCostEstimator(
            StatisticsProviderFactory statisticsProviderFactory,
            StepEstimator estimator,
            OntologyProvider ontologyProvider) {
        this.statisticsProviderFactory = statisticsProviderFactory;
        this.estimator = estimator;
        this.ontologyProvider = ontologyProvider;
    }

    @Override
    public PlanWithCost<Plan, PlanDetailedCost> estimate(
            Plan plan,
            Optional<PlanWithCost<Plan, PlanDetailedCost>> previousCost,
            AsgQuery query) {
        PlanWithCost<Plan, PlanDetailedCost> newPlan = null;
        List<PlanOpBase> step = plan.getOps();
        if (previousCost.isPresent()) {
            step = extractNewStep(plan);
        }
        String opsString = pattern(step);
        StatisticsCostEstimatorPatterns[] supportedPattern = getSupportedPattern();
        for (StatisticsCostEstimatorPatterns pattern : supportedPattern) {
            //Pattern compile = Pattern.compile(pattern.pattern());
            Pattern compile = pattern.getCompiledPattern();
            Matcher matcher = compile.matcher(opsString);
            if (matcher.find()) {
                Map<StatisticsCostEstimatorNames, PlanOpBase> map = extractStep(step, getNamedGroups(compile), matcher);
                StatisticsProvider statisticsProvider = statisticsProviderFactory.get(ontologyProvider.get(query.getOnt()).get());
                StepEstimator.StepEstimatorResult result = estimator.calculate(statisticsProvider, map, pattern, previousCost);
                newPlan = buildNewPlan(result, previousCost);
                break;
            }
        }
        return newPlan;
    }

    private PlanWithCost<Plan, PlanDetailedCost> buildNewPlan(StepEstimator.StepEstimatorResult result, Optional<PlanWithCost<Plan, PlanDetailedCost>> previousCost) {
        AtomicReference<Cost> completePlanCost = new AtomicReference<>();
        List<PlanOpWithCost<Cost>> planOpWithCosts;
        if (previousCost.isPresent()) {
            completePlanCost.set(previousCost.get().getCost().getGlobalCost());
            planOpWithCosts = Stream.ofAll(previousCost.get().getCost().getOpCosts()).map(c -> new PlanOpWithCost<>(c.getCost(), c.getCountEstimates(), c.getOpBase())).toJavaList();
        } else {
            completePlanCost.set(new Cost(0));
            planOpWithCosts = new ArrayList<>();
        }

        double lambda = result.lambda();
        planOpWithCosts.forEach(element-> {
            if(element.getOpBase().get(0).getClass().equals(EntityOp.class)) {
                element.push(element.peek()*lambda);
            }
        });

        List<PlanOpWithCost<Cost>> costs = result.planOpWithCosts();
        costs.forEach(c -> {
            //add new step into plan
            if (!previousCost.isPresent() || !contains(previousCost.get().getPlan(), c.getOpBase().get(0))) {
                planOpWithCosts.add(c);
                double cost = completePlanCost.get().cost + c.getCost().cost;
                completePlanCost.set(new Cost(cost ));
            }
        });

        Plan newPlan = new Plan(planOpWithCosts.stream().flatMap(p -> p.getOpBase().stream()).collect(Collectors.toList()));
        PlanDetailedCost newCost = new PlanDetailedCost(completePlanCost.get(), planOpWithCosts);
        return new PlanWithCost<>(newPlan, newCost);
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