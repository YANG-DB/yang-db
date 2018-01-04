package com.kayhut.fuse.epb.plan.estimation.pattern;

import com.google.inject.Inject;
import com.kayhut.fuse.dispatcher.utils.PlanUtil;
import com.kayhut.fuse.dispatcher.epb.CostEstimator;
import com.kayhut.fuse.epb.plan.estimation.IncrementalEstimationContext;
import com.kayhut.fuse.epb.plan.estimation.pattern.estimators.PatternCostEstimator;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.execution.plan.*;
import com.kayhut.fuse.model.execution.plan.composite.Plan;
import com.kayhut.fuse.model.execution.plan.composite.descriptors.IterablePlanOpDescriptor;
import com.kayhut.fuse.model.execution.plan.costs.CountEstimatesCost;
import com.kayhut.fuse.model.execution.plan.costs.DoubleCost;
import com.kayhut.fuse.model.execution.plan.costs.JoinCost;
import com.kayhut.fuse.model.execution.plan.costs.PlanDetailedCost;
import com.kayhut.fuse.model.execution.plan.entity.EntityFilterOp;
import com.kayhut.fuse.model.execution.plan.entity.EntityJoinOp;
import com.kayhut.fuse.model.execution.plan.entity.EntityOp;
import com.kayhut.fuse.model.execution.plan.entity.GoToEntityOp;
import com.kayhut.fuse.model.execution.plan.relation.RelationFilterOp;
import com.kayhut.fuse.model.execution.plan.relation.RelationOp;
import javaslang.collection.Stream;

import java.lang.reflect.Method;
import java.util.*;
import java.util.regex.Matcher;

import static com.kayhut.fuse.epb.plan.estimation.pattern.Pattern.*;
import static com.kayhut.fuse.epb.plan.estimation.pattern.RegexPatternCostEstimator.PatternPart.*;

/**
 * Created by moti on 01/04/2017.
 */
public class RegexPatternCostEstimator implements CostEstimator<Plan, PlanDetailedCost, IncrementalEstimationContext<Plan, PlanDetailedCost, AsgQuery>> {
    //region Static
    private static Map<Pattern, java.util.regex.Pattern> compiledPatterns;

    public static Pattern[] getSupportedPattern() {
        return RegexPatternCostEstimator.Pattern.values();
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
        for(Pattern pattern : RegexPatternCostEstimator.Pattern.values()){
            java.util.regex.Pattern compile = java.util.regex.Pattern.compile(pattern.pattern());
            compiledPatterns.put(pattern, compile);
        }
    }
    //endregion

    public enum Pattern {
        //option2
        ENTITY_RELATION_ENTITY("^(?<" + ENTITY_ONE.value + ">" + EntityOp.class.getSimpleName() + ")" + ":" + "(?<" + OPTIONAL_ENTITY_ONE_FILTER.value + ">" + EntityFilterOp.class.getSimpleName() + ":)?" +
                "(?<" + RELATION.value + ">" + RelationOp.class.getSimpleName() + ")" + ":" + "(?<" + OPTIONAL_REL_FILTER.value + ">" + RelationFilterOp.class.getSimpleName() + ":)?" +
                "(?<" + ENTITY_TWO.value + ">" + EntityOp.class.getSimpleName() + ")" + "(:" + "(?<" + OPTIONAL_ENTITY_TWO_FILTER.value + ">" + EntityFilterOp.class.getSimpleName() + "))?$"),
        //option 1
        ENTITY("^(?<" + ENTITY_ONLY.value + ">" + EntityOp.class.getSimpleName() + ")" + "(:" + "(?<" + OPTIONAL_ENTITY_ONLY_FILTER.value + ">" + EntityFilterOp.class.getSimpleName() + "))?$"),
        //option 3 And node
        GOTO_ENTITY_RELATION_ENTITY("^(?<" + GOTO_ENTITY.value+">" + GoToEntityOp.class.getSimpleName() + ")" + ":" +
                "(?<" + RELATION.value + ">" + RelationOp.class.getSimpleName() + ")" + ":" + "(?<" + OPTIONAL_REL_FILTER.value + ">" + RelationFilterOp.class.getSimpleName() + ":)?" +
                "(?<" + ENTITY_TWO.value + ">" + EntityOp.class.getSimpleName() + ")" + "(:" + "(?<" + OPTIONAL_ENTITY_TWO_FILTER.value + ">" + EntityFilterOp.class.getSimpleName() + "))?$"),

        ENTITY_JOIN("^(?<" + JOIN.value + ">" + EntityJoinOp.class.getSimpleName() + ")$" ),
        ENTITY_JOIN_RELATION_ENTITY("^(?<" + JOIN.value+">" + EntityJoinOp.class.getSimpleName() + ")" + ":" +
                "(?<" + RELATION.value + ">" + RelationOp.class.getSimpleName() + ")" + ":" + "(?<" + OPTIONAL_REL_FILTER.value + ">" + RelationFilterOp.class.getSimpleName() + ":)?" +
                "(?<" + ENTITY_TWO.value + ">" + EntityOp.class.getSimpleName() + ")" + "(:" + "(?<" + OPTIONAL_ENTITY_TWO_FILTER.value + ">" + EntityFilterOp.class.getSimpleName() + "))?$"),
        GOTO("^(((?<" + ENTITY_ONE.value + ">" +EntityOp.class.getSimpleName() + ")" + "(:" + "(?<"+ OPTIONAL_ENTITY_ONE_FILTER.value + ">" + EntityFilterOp.class.getSimpleName() + "))?)|(?<" + JOIN.value + ">"+ EntityJoinOp.class.getSimpleName()+"))"+":(?<" + GOTO_ENTITY.value+">" + GoToEntityOp.class.getSimpleName() + ")$" );

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
        GOTO_ENTITY("gotoEntity"),
        JOIN("join");


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
    public RegexPatternCostEstimator(PatternCostEstimator<Plan, CountEstimatesCost, IncrementalEstimationContext<Plan, PlanDetailedCost, AsgQuery>> estimator) {
        this.estimator = estimator;
    }
    //endregion

    //region CostEstimator Implementation
    @Override
    public PlanWithCost<Plan, PlanDetailedCost> estimate(
            Plan plan,
            IncrementalEstimationContext<Plan, PlanDetailedCost, AsgQuery> context) {
        PlanWithCost<Plan, PlanDetailedCost> newPlan = null;
        Plan planStep = context.getPreviousCost().isPresent() ? extractNewPlanStep(plan) : plan;

        String opsString = IterablePlanOpDescriptor.getLight().describe(planStep.getOps());
        Pattern[] supportedPattern = getSupportedPattern();
        for (Pattern regexPattern : supportedPattern) {
            java.util.regex.Pattern compile = regexPattern.getCompiledPattern();
            Matcher matcher = compile.matcher(opsString);
            if (matcher.find()) {
                Map<PatternPart, PlanOp> patternParts = getStepPatternParts(planStep, getNamedGroups(compile), matcher);
                com.kayhut.fuse.epb.plan.estimation.pattern.Pattern pattern = buildPattern(regexPattern, patternParts, plan);
                PatternCostEstimator.Result<Plan, CountEstimatesCost> result = estimator.estimate(pattern, context);
                newPlan = pattern.buildNewPlan(result, context.getPreviousCost());
                break;
            }
        }
        return newPlan;
    }
    //endregion

    //region Private Methods
    private static Plan extractNewPlanStep(Plan plan) {
        List<PlanOp> planOps = new ArrayList<>();
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

        return new Plan();
    }

    private Map<PatternPart, PlanOp> getStepPatternParts(Plan step, Map<String, Integer> groups, Matcher matcher) {
        Map<PatternPart, PlanOp> map = new HashMap<>();
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

    public PatternCostEstimator<Plan, CountEstimatesCost, IncrementalEstimationContext<Plan, PlanDetailedCost, AsgQuery>> getEstimator() {
        return estimator;
    }

    //region Fields
    private PatternCostEstimator<Plan, CountEstimatesCost, IncrementalEstimationContext<Plan, PlanDetailedCost, AsgQuery>> estimator;
    //endregion
}