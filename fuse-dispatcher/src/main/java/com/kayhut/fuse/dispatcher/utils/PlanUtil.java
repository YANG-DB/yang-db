package com.kayhut.fuse.dispatcher.utils;

import com.kayhut.fuse.model.execution.plan.*;
import javaslang.collection.Stream;

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

    public static <T extends PlanOpBase> T first$(CompositePlanOpBase compositePlanOp, Predicate<PlanOpBase> predicate) {
        return PlanUtil.<T>first(compositePlanOp, predicate).get();
    }

    public static <T extends PlanOpBase> T first$(CompositePlanOpBase compositePlanOp, Class<T> klass) {
        return PlanUtil.first(compositePlanOp, klass).get();
    }

    public static <T extends PlanOpBase> T first$(CompositePlanOpBase compositePlanOp, T planOp) {
        return PlanUtil.first(compositePlanOp, planOp).get();
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

    public static <T extends PlanOpBase> T last$(CompositePlanOpBase compositePlanOp, Predicate<PlanOpBase> predicate) {
        return PlanUtil.<T>last(compositePlanOp, predicate).get();
    }

    public static <T extends PlanOpBase> T last$(CompositePlanOpBase compositePlanOp, Class<T> klass) {
        return PlanUtil.last(compositePlanOp, klass).get();
    }

    public static <T extends PlanOpBase> T last$(CompositePlanOpBase compositePlanOp, T planOp) {
        return PlanUtil.last(compositePlanOp, planOp).get();
    }

    public static <T extends CompositePlanOpBase> T flat(CompositePlanOpBase compositePlanOp) {
        return (T)flatten(compositePlanOp, truePredicate, truePredicate);
    }

    public static <T extends CompositePlanOpBase> T replace(CompositePlanOpBase compositePlanOp, PlanOpBase oldOp, PlanOpBase newOp) {
        Plan newPlan = new Plan(compositePlanOp.getOps());
        List<CompositePlanOpBase> composites = Stream.<CompositePlanOpBase>of(newPlan).toJavaList();

        while(!composites.isEmpty()) {
            compositePlanOp = composites.get(0);

            int indexOfOld = compositePlanOp.getOps().indexOf(oldOp);
            if (indexOfOld > 0) {
                compositePlanOp.getOps().set(indexOfOld, newOp);
                break;
            } else {
                composites.addAll(Stream.ofAll(compositePlanOp.getOps())
                        .filter(planOp -> CompositePlanOpBase.class.isAssignableFrom(planOp.getClass()))
                        .map(planOp -> (CompositePlanOpBase)planOp)
                        .toJavaList());

                composites.remove(0);
            }
        }

        return (T)newPlan;
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

    private static Plan flatten(
            CompositePlanOpBase compositePlanOp,
            Predicate<PlanOpBase> opPredicate,
            Predicate<PlanOpBase> compositeOpPredicate) {
        List<PlanOpBase> flattenedPlanOps = new ArrayList<>(compositePlanOp.getOps());
        int index = 0;
        while(index < flattenedPlanOps.size()) {
            PlanOpBase planOp = flattenedPlanOps.get(index);
            if (CompositePlanOpBase.class.isAssignableFrom(planOp.getClass())) {
                CompositePlanOpBase innerCompositePlanOp = (CompositePlanOpBase)planOp;
                flattenedPlanOps.remove(index);

                if (compositeOpPredicate.test(innerCompositePlanOp)) {
                    flattenedPlanOps.addAll(index, innerCompositePlanOp.getOps());
                }
            } else {
                index++;
            }
        }

        return new Plan(Stream.ofAll(flattenedPlanOps).filter(opPredicate::test));
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
