package com.kayhut.fuse.epb.plan.estimation.step;

import com.codahale.metrics.Slf4jReporter;
import com.google.inject.Inject;
import com.kayhut.fuse.dispatcher.ontolgy.OntologyProvider;
import com.kayhut.fuse.dispatcher.utils.LoggerAnnotation;
import com.kayhut.fuse.dispatcher.utils.PlanUtil;
import com.kayhut.fuse.epb.plan.estimation.CostEstimator;
import com.kayhut.fuse.epb.plan.estimation.step.context.M1StepCostEstimatorContext;
import com.kayhut.fuse.epb.plan.statistics.StatisticsProviderFactory;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.execution.plan.*;
import com.kayhut.fuse.model.execution.plan.costs.CountEstimatesCost;
import com.kayhut.fuse.model.execution.plan.costs.DoubleCost;
import com.kayhut.fuse.model.execution.plan.costs.PlanDetailedCost;
import javaslang.collection.Stream;

import java.lang.reflect.Method;
import java.util.*;
import java.util.regex.Matcher;

import static com.kayhut.fuse.epb.plan.estimation.step.StatisticsCostEstimator.PatternPart.*;
import static com.kayhut.fuse.model.Utils.pattern;

/**
 * Created by moti on 01/04/2017.
 */
public class StatisticsCostEstimator implements CostEstimator<Plan, PlanDetailedCost, AsgQuery> {
    //region Static
    private static Map<Pattern, java.util.regex.Pattern> compiledPatterns;

    public static Pattern[] getSupportedPattern() {
        return Pattern.values();
    }

    private static Map<String, Integer> getNamedGroups(java.util.regex.Pattern regex) {
        try {
            Method namedGroupsMethod = java.util.regex.Pattern.class.getDeclaredMethod("namedGroups");
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

    static {
        compiledPatterns = new HashMap<>();
        for(Pattern pattern : Pattern.values()){
            java.util.regex.Pattern compile = java.util.regex.Pattern.compile(pattern.pattern());
            compiledPatterns.put(pattern, compile);
        }
    }
    //endregion

    public enum Pattern {
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

        //region Enum Constructors
        Pattern(String pattern) {
            this.pattern = pattern;
        }
        //endregion

        //region Properties
        public String pattern() {
            return pattern;
        }

        public java.util.regex.Pattern getCompiledPattern(){
            return compiledPatterns.get(this);
        }
        //endregion

        //region Fields
        private String pattern;
        //endregion
    }

    public enum PatternPart {
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

        PatternPart(String value) {
            this.value = value;
        }

        public String value() {
            return value;
        }

        static PatternPart of(String name) {
            return Arrays.stream(values()).filter(v -> v.value.equals(name)).findFirst().get();
        }
    }

    //region Constructors
    @Inject
    public StatisticsCostEstimator(
            StatisticsProviderFactory statisticsProviderFactory,
            StepCostEstimator<Plan, CountEstimatesCost, M1StepCostEstimatorContext> estimator,
            OntologyProvider ontologyProvider) {
        this.statisticsProviderFactory = statisticsProviderFactory;
        this.estimator = estimator;
        this.ontologyProvider = ontologyProvider;
    }
    //endregion

    //region CostEstimator Implementation
    @Override
    @LoggerAnnotation(name = "estimate", options = LoggerAnnotation.Options.full, logLevel = Slf4jReporter.LoggingLevel.DEBUG)
    public PlanWithCost<Plan, PlanDetailedCost> estimate(
            Plan plan,
            Optional<PlanWithCost<Plan, PlanDetailedCost>> previousCost,
            AsgQuery query) {
        PlanWithCost<Plan, PlanDetailedCost> newPlan = null;
        Plan planStep = previousCost.isPresent() ? extractNewPlanStep(plan) : plan;

        String opsString = pattern(planStep.getOps());
        Pattern[] supportedPattern = getSupportedPattern();
        for (Pattern pattern : supportedPattern) {
            java.util.regex.Pattern compile = pattern.getCompiledPattern();
            Matcher matcher = compile.matcher(opsString);
            if (matcher.find()) {
                M1StepCostEstimatorContext context = new M1StepCostEstimatorContext(
                        statisticsProviderFactory.get(ontologyProvider.get(query.getOnt()).get()),
                        getStepPatternParts(planStep, getNamedGroups(compile), matcher),
                        previousCost
                );

                Step step = pattern.equals(Pattern.SINGLE_MODE) ?  Step.buildEntityOnlyStep(context.getPatternParts()) :
                            pattern.equals(Pattern.FULL_STEP) ? Step.buildFullStep(context.getPatternParts()) :
                            pattern.equals(Pattern.GOTO_MODE) ? Step.buildGoToStep(plan, context.getPatternParts()) : null;

                StepCostEstimator.Result<Plan, CountEstimatesCost> result = estimator.estimate(step, context);

                newPlan = buildNewPlan(result, previousCost);
                break;
            }
        }
        return newPlan;
    }
    //endregion

    //region Private Methods
    private static Plan extractNewPlanStep(Plan plan) {
        List<PlanOpBase> planOps = new ArrayList<>();
        int entityCounter = 0;
        for (int i = plan.getOps().size() - 1 ; i >= 0 && entityCounter < 2; i--) {
            if (EntityOp.class.isAssignableFrom(plan.getOps().get(i).getClass())) {
                entityCounter++;
            }
            planOps.add(0, plan.getOps().get(i));
        }

        if (entityCounter > 0) {
            return new Plan(planOps);
        }

        return Plan.empty();
    }
    private PlanWithCost<Plan, PlanDetailedCost> buildNewPlan(
            StepCostEstimator.Result<Plan, CountEstimatesCost> result,
            Optional<PlanWithCost<Plan, PlanDetailedCost>> previousCost) {

        DoubleCost previousPlanGlobalCost;
        List<PlanWithCost<Plan, CountEstimatesCost>> previousPlanStepCosts;
        if (previousCost.isPresent()) {
            previousPlanGlobalCost = previousCost.get().getCost().getGlobalCost();
            previousPlanStepCosts = Stream.ofAll(previousCost.get().getCost().getPlanStepCosts())
                    .map(planStepCost -> new PlanWithCost<>(
                            planStepCost.getPlan(),
                            new CountEstimatesCost(planStepCost.getCost().getCost(), planStepCost.getCost().getCountEstimates())))
                    .toJavaList();
        } else {
            previousPlanGlobalCost = new DoubleCost(0);
            previousPlanStepCosts = new ArrayList<>();
        }

        double lambda = result.lambda();
        previousPlanStepCosts.forEach(planStepCost -> {
            if(planStepCost.getPlan().getOps().get(0).getClass().equals(EntityOp.class)) {
                planStepCost.getCost().push(planStepCost.getCost().peek() * lambda);
            }
        });

        List<PlanWithCost<Plan, CountEstimatesCost>> planStepCosts =
                Stream.ofAll(result.getPlanStepCosts())
                .filter(planStepCost -> !previousCost.isPresent() ||
                        !PlanUtil.first(previousCost.get().getPlan(), planStepCost.getPlan().getOps().get(0)).isPresent())
                .toJavaList();

        double sumOfPlanStepCosts = Stream.ofAll(planStepCosts).map(planStepCost -> planStepCost.getCost().getCost()).sum().doubleValue();
        double newCost = previousPlanGlobalCost.getCost() + sumOfPlanStepCosts;
        List<PlanWithCost<Plan, CountEstimatesCost>> newPlanStepCosts = Stream.ofAll(previousPlanStepCosts).appendAll(planStepCosts).toJavaList();

        Plan newPlan = new Plan(Stream.ofAll(newPlanStepCosts).flatMap(planStepCost -> Stream.ofAll(planStepCost.getPlan().getOps())).toJavaList());
        PlanDetailedCost newDetailedCost = new PlanDetailedCost(new DoubleCost(newCost), newPlanStepCosts);
        return new PlanWithCost<>(newPlan, newDetailedCost);
    }

    private Map<PatternPart, PlanOpBase> getStepPatternParts(Plan step, Map<String, Integer> groups, Matcher matcher) {
        Map<PatternPart, PlanOpBase> map = new HashMap<>();
        TreeSet<Map.Entry<String, Integer>> entries = new TreeSet<>(Comparator.comparingInt(Map.Entry::getValue));
        entries.addAll(groups.entrySet());
        int stepIndex = 0;
        for (Map.Entry<String, Integer> entry : entries) {
            if (matcher.group(entry.getKey()) != null) {
                map.put(PatternPart.of(entry.getKey()), step.getOps().get(stepIndex));
                stepIndex++;
            }
        }

        return map;
    }
    //endregion

    //region Fields
    private StatisticsProviderFactory statisticsProviderFactory;
    private OntologyProvider ontologyProvider;
    private StepCostEstimator<Plan, CountEstimatesCost, M1StepCostEstimatorContext> estimator;
    //endregion
}