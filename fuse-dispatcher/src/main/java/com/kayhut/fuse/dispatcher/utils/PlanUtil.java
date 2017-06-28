package com.kayhut.fuse.dispatcher.utils;

import com.kayhut.fuse.model.execution.plan.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Created by Roman on 15/05/2017.
 */
public class PlanUtil {
    //region Public Methods
    public static boolean isFirst(CompositePlanOpBase compositePlanOp, PlanOpBase planOpBase) {
        return compositePlanOp.getOps().size() > 0 && compositePlanOp.getOps().get(0) == planOpBase;
    }

    public static <T extends PlanOpBase> Optional<T> adjacentNext(CompositePlanOpBase compositePlanOp, PlanOpBase planOp) {
        int indexOf = compositePlanOp.getOps().indexOf(planOp);
        return getPlanOp(compositePlanOp, truePredicate, nextAdjacentDirectionFunction.apply(indexOf), indexOf);
    }

    public static <T extends PlanOpBase> Optional<T> next(CompositePlanOpBase compositePlanOp, PlanOpBase planOp, Predicate<PlanOpBase> opPredicate) {
        return getPlanOp(compositePlanOp, opPredicate, nextDirection, compositePlanOp.getOps().indexOf(planOp));
    }

    public static <T extends PlanOpBase> Optional<T> next(CompositePlanOpBase compositePlanOp, PlanOpBase planOp, Class<T> klass) {
        return getPlanOp(compositePlanOp, classPredicateFunction.apply(klass), nextDirection, compositePlanOp.getOps().indexOf(planOp));
    }

    public static <T extends PlanOpBase> Optional<T> adjacentPrev(CompositePlanOpBase compositePlanOp, PlanOpBase planOp) {
        int indexOf = compositePlanOp.getOps().indexOf(planOp);
        return getPlanOp(compositePlanOp, truePredicate, prevAdjacentDirectionFunction.apply(indexOf), indexOf);
    }

    public static <T extends PlanOpBase> Optional<T> prev(CompositePlanOpBase compositePlanOp, PlanOpBase planOp, Predicate<PlanOpBase> opPredicate) {
        return getPlanOp(compositePlanOp, opPredicate, prevDirection, compositePlanOp.getOps().indexOf(planOp));
    }

    public static <T extends PlanOpBase> Optional<T> prev(CompositePlanOpBase compositePlanOp, PlanOpBase planOp, Class<T> klass) {
        return getPlanOp(compositePlanOp, classPredicateFunction.apply(klass), prevDirection, compositePlanOp.getOps().indexOf(planOp));
    }

    public static <T extends PlanOpBase> Optional<T> first(CompositePlanOpBase compositePlanOp, Predicate<PlanOpBase> opPredicate) {
        return getPlanOp(compositePlanOp, opPredicate, nextDirection, -1);
    }

    public static <T extends PlanOpBase> Optional<T> first(CompositePlanOpBase compositePlanOp, Class<T> klass) {
        return first(compositePlanOp, classPredicateFunction.apply(klass));
    }

    public static <T extends PlanOpBase> Optional<T> first(CompositePlanOpBase compositePlanOp, T planOp) {
        return first(compositePlanOp, equalsPredicateFunction.apply(planOp));
    }

    public static <T extends PlanOpBase> T first$(Plan plan, Predicate<PlanOpBase> predicate) {
        return PlanUtil.<T>first(plan, predicate).get();
    }

    public static <T extends PlanOpBase> T first$(Plan plan, Class<T> klass) {
        return PlanUtil.first(plan, klass).get();
    }

    public static <T extends PlanOpBase> T first$(Plan plan, T planOp) {
        return PlanUtil.first(plan, planOp).get();
    }

    public static <T extends PlanOpBase> Optional<T> last(CompositePlanOpBase compositePlanOp, Predicate<PlanOpBase> opPredicate) {
        return getPlanOp(compositePlanOp, opPredicate, prevDirection, compositePlanOp.getOps().size());
    }

    public static <T extends PlanOpBase> Optional<T> last(CompositePlanOpBase compositePlanOp, Class<T> klass) {
        return last(compositePlanOp, classPredicateFunction.apply(klass));
    }

    public static <T extends PlanOpBase> Optional<T> last(CompositePlanOpBase compositePlanOp, T planOp) {
        return last(compositePlanOp, equalsPredicateFunction.apply(planOp));
    }

    public static <T extends PlanOpBase> T last$(Plan plan, Predicate<PlanOpBase> predicate) {
        return PlanUtil.<T>last(plan, predicate).get();
    }

    public static <T extends PlanOpBase> T last$(Plan plan, Class<T> klass) {
        return PlanUtil.last(plan, klass).get();
    }

    public static <T extends PlanOpBase> T last$(Plan plan, T planOp) {
        return PlanUtil.last(plan, planOp).get();
    }

    public static Plan replace(Plan plan, PlanOpBase oldOp, PlanOpBase newOp) {
        Plan newPlan = new Plan(plan.getOps());
        newPlan.getOps().set(plan.getOps().indexOf(oldOp), newOp);
        return newPlan;
    }

    public static List<PlanOpBase> extractNewStep(Plan current) {
        List<PlanOpBase> newPlan = new ArrayList<>();
        List<PlanOpBase> ops = current.getOps();
        int entityCounter = 0;
        int i = ops.size() - 1;
        while (i >= 0 && entityCounter < 2) {
            if (EntityOp.class.isAssignableFrom(ops.get(i).getClass())) {
                entityCounter++;
            }
            newPlan.add(0, ops.get(i));
            i--;
        }
        if (entityCounter > 0)
            return newPlan;
        return Collections.emptyList();
    }
    //endregion

    //region Private Methods
    private static <T extends PlanOpBase> Optional<T> getPlanOp(
            CompositePlanOpBase compositePlanOp,
            Predicate<PlanOpBase> opPredicate,
            Function<Integer, Integer> direction,
            int startIndex) {

        for(int index = direction.apply(startIndex) ;
            index >= 0 && index < compositePlanOp.getOps().size() ;
            index = direction.apply(index)) {
            PlanOpBase planOp = compositePlanOp.getOps().get(index);
            if (opPredicate.test(planOp)) {
                return Optional.of((T)planOp);
            }
        }

        return Optional.empty();
    }
    //endregion

    //region Static
    private static Function<Integer, Integer> nextDirection = a -> a + 1;
    private static Function<Integer, Integer> prevDirection = a -> a - 1;

    private static Function<Integer, Function<Integer, Integer>> nextAdjacentDirectionFunction =
            a -> b -> a.equals(b) ? a + 1 : -1;

    private static Function<Integer, Function<Integer, Integer>> prevAdjacentDirectionFunction =
            a -> b -> a.equals(b) ? a - 1 : -1;

    private static Function<Class<?>, Predicate<PlanOpBase>> classPredicateFunction =
            klass -> planOp -> klass.isAssignableFrom(planOp.getClass());

    private static Function<PlanOpBase, Predicate<PlanOpBase>> equalsPredicateFunction =
            planOp -> planOp2 -> planOp2.equals(planOp);

    private static Predicate<PlanOpBase> truePredicate = planOp -> true;
    //endregion

}
