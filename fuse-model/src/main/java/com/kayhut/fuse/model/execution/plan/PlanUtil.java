package com.kayhut.fuse.model.execution.plan;

import com.google.common.collect.Iterables;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Created by benishue on 12-Mar-17.
 */
public class PlanUtil {
    //region Public Methods
    public static boolean isFirst(CompositePlanOpBase compositePlanOp, PlanOpBase planOpBase) {
        return compositePlanOp.getOps().size() > 0 && compositePlanOp.getOps().get(0) == planOpBase;
    }

    public static <T extends PlanOpBase> Optional<T> getAdjacentNext(CompositePlanOpBase compositePlanOp, PlanOpBase planOp) {
        int indexOf = compositePlanOp.getOps().indexOf(planOp);
        return getPlanOp(compositePlanOp, truePredicate, nextAdjacentDirectionFunction.apply(indexOf), indexOf);
    }

    public static <T extends PlanOpBase> Optional<T> getNext(CompositePlanOpBase compositePlanOp, PlanOpBase planOp, Predicate<PlanOpBase> opPredicate) {
        return getPlanOp(compositePlanOp, opPredicate, nextDirection, compositePlanOp.getOps().indexOf(planOp));
    }

    public static <T extends PlanOpBase> Optional<T> getNext(CompositePlanOpBase compositePlanOp, PlanOpBase planOp, Class<?> klass) {
        return getPlanOp(compositePlanOp, classPredicateFunction.apply(klass), nextDirection, compositePlanOp.getOps().indexOf(planOp));
    }

    public static <T extends PlanOpBase> Optional<T> getAdjacentPrev(CompositePlanOpBase compositePlanOp, PlanOpBase planOp) {
        int indexOf = compositePlanOp.getOps().indexOf(planOp);
        return getPlanOp(compositePlanOp, truePredicate, prevAdjacentDirectionFunction.apply(indexOf), indexOf);
    }

    public static <T extends PlanOpBase> Optional<T> getPrev(CompositePlanOpBase compositePlanOp, PlanOpBase planOp, Predicate<PlanOpBase> opPredicate) {
        return getPlanOp(compositePlanOp, opPredicate, prevDirection, compositePlanOp.getOps().indexOf(planOp));
    }

    public static <T extends PlanOpBase> Optional<T> getPrev(CompositePlanOpBase compositePlanOp, PlanOpBase planOp, Class<?> klass) {
        return getPlanOp(compositePlanOp, classPredicateFunction.apply(klass), prevDirection, compositePlanOp.getOps().indexOf(planOp));
    }
    //endregion

    //region Private Methods
    private static <T extends PlanOpBase> Optional<T> getPlanOp(
            CompositePlanOpBase compositePlanOp,
            Predicate<PlanOpBase> opPredicate,
            Function<Integer, Integer> direction,
            int startIndex) {

        for(int index = direction.apply(startIndex) ;
            index >= 0 && index <= compositePlanOp.getOps().size() ;
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

    private static Predicate<PlanOpBase> truePredicate = planOp -> true;

    /**
     * extract step from the end of the current plan
     *
     * @param current
     * @return
     */
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

    public static PlanOpBase getLast(Plan plan) {
        return Iterables.getLast(plan.getOps());
    }

    public static <T extends PlanOpBase> T findFirst(Plan plan,Class<T> clazz,Predicate<PlanOpBase> predicate) {
        return (T) plan.getOps().stream().filter(p -> clazz.isAssignableFrom(p.getClass()) && predicate.test(p)).findFirst().get();
    }

    public static Plan replace(Plan plan,AsgEBasePlanOp oldOp, AsgEBasePlanOp newOp) {
        plan.getOps().set(plan.getOps().indexOf(oldOp), newOp);
        return plan;
    }

    //endregion

}
