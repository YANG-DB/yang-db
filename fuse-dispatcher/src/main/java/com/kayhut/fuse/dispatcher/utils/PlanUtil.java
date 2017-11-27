package com.kayhut.fuse.dispatcher.utils;

import com.kayhut.fuse.model.execution.plan.*;
import com.kayhut.fuse.model.execution.plan.composite.CompositePlanOp;
import com.kayhut.fuse.model.execution.plan.composite.Plan;
import javaslang.collection.Stream;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Created by Roman on 15/05/2017.
 */
public class PlanUtil {
    //region Public Methods
    public static boolean isFirst(CompositePlanOp compositePlanOp, PlanOp planOp) {
        return compositePlanOp.getOps().size() > 0 && compositePlanOp.getOps().get(0) == planOp;
    }

    public static <T extends PlanOp> Optional<T> adjacentNext(CompositePlanOp compositePlanOp, PlanOp planOp) {
        int indexOf = compositePlanOp.getOps().indexOf(planOp);
        return getPlanOp(compositePlanOp, truePredicate, nextAdjacentDirectionFunction.apply(indexOf), indexOf);
    }

    public static <T extends PlanOp> Optional<T> next(CompositePlanOp compositePlanOp, PlanOp planOp, Predicate<PlanOp> opPredicate) {
        return getPlanOp(compositePlanOp, opPredicate, nextDirection, compositePlanOp.getOps().indexOf(planOp));
    }

    public static <T extends PlanOp> Optional<T> next(CompositePlanOp compositePlanOp, PlanOp planOp, Class<T> klass) {
        return getPlanOp(compositePlanOp, classPredicateFunction.apply(klass), nextDirection, compositePlanOp.getOps().indexOf(planOp));
    }

    public static <T extends PlanOp> Optional<T> adjacentPrev(CompositePlanOp compositePlanOp, PlanOp planOp) {
        int indexOf = compositePlanOp.getOps().indexOf(planOp);
        return getPlanOp(compositePlanOp, truePredicate, prevAdjacentDirectionFunction.apply(indexOf), indexOf);
    }

    public static <T extends PlanOp> Optional<T> prev(CompositePlanOp compositePlanOp, PlanOp planOp, Predicate<PlanOp> opPredicate) {
        return getPlanOp(compositePlanOp, opPredicate, prevDirection, compositePlanOp.getOps().indexOf(planOp));
    }

    public static <T extends PlanOp> Optional<T> prev(CompositePlanOp compositePlanOp, PlanOp planOp, Class<T> klass) {
        return getPlanOp(compositePlanOp, classPredicateFunction.apply(klass), prevDirection, compositePlanOp.getOps().indexOf(planOp));
    }

    public static <T extends PlanOp> Optional<T> first(CompositePlanOp compositePlanOp, Predicate<PlanOp> opPredicate) {
        return getPlanOp(compositePlanOp, opPredicate, nextDirection, -1);
    }

    public static <T extends PlanOp> Optional<T> first(CompositePlanOp compositePlanOp, Class<T> klass) {
        return first(compositePlanOp, classPredicateFunction.apply(klass));
    }

    public static <T extends PlanOp> Optional<T> first(CompositePlanOp compositePlanOp, T planOp) {
        return first(compositePlanOp, equalsPredicateFunction.apply(planOp));
    }

    public static <T extends PlanOp> T first$(CompositePlanOp compositePlanOp, Predicate<PlanOp> predicate) {
        return PlanUtil.<T>first(compositePlanOp, predicate).get();
    }

    public static <T extends PlanOp> T first$(CompositePlanOp compositePlanOp, Class<T> klass) {
        return PlanUtil.first(compositePlanOp, klass).get();
    }

    public static <T extends PlanOp> T first$(CompositePlanOp compositePlanOp, T planOp) {
        return PlanUtil.first(compositePlanOp, planOp).get();
    }

    public static <T extends PlanOp> Optional<T> last(CompositePlanOp compositePlanOp, Predicate<PlanOp> opPredicate) {
        return getPlanOp(compositePlanOp, opPredicate, prevDirection, compositePlanOp.getOps().size());
    }

    public static <T extends PlanOp> Optional<T> last(CompositePlanOp compositePlanOp, Class<T> klass) {
        return last(compositePlanOp, classPredicateFunction.apply(klass));
    }

    public static <T extends PlanOp> Optional<T> last(CompositePlanOp compositePlanOp, T planOp) {
        return last(compositePlanOp, equalsPredicateFunction.apply(planOp));
    }

    public static <T extends PlanOp> T last$(CompositePlanOp compositePlanOp, Predicate<PlanOp> predicate) {
        return PlanUtil.<T>last(compositePlanOp, predicate).get();
    }

    public static <T extends PlanOp> T last$(CompositePlanOp compositePlanOp, Class<T> klass) {
        return PlanUtil.last(compositePlanOp, klass).get();
    }

    public static <T extends PlanOp> T last$(CompositePlanOp compositePlanOp, T planOp) {
        return PlanUtil.last(compositePlanOp, planOp).get();
    }

    public static <T extends CompositePlanOp> T flat(CompositePlanOp compositePlanOp) {
        return (T)flatten(compositePlanOp, truePredicate, truePredicate);
    }

    public static <T extends CompositePlanOp> T replace(CompositePlanOp compositePlanOp, PlanOp oldOp, PlanOp newOp) {
        Plan newPlan = new Plan(compositePlanOp.getOps());
        List<CompositePlanOp> composites = Stream.<CompositePlanOp>of(newPlan).toJavaList();

        while(!composites.isEmpty()) {
            compositePlanOp = composites.get(0);

            int indexOfOld = compositePlanOp.getOps().indexOf(oldOp);
            if (indexOfOld > 0) {
                compositePlanOp.getOps().set(indexOfOld, newOp);
                break;
            } else {
                composites.addAll(Stream.ofAll(compositePlanOp.getOps())
                        .filter(planOp -> CompositePlanOp.class.isAssignableFrom(planOp.getClass()))
                        .map(planOp -> (CompositePlanOp)planOp)
                        .toJavaList());

                composites.remove(0);
            }
        }

        return (T)newPlan;
    }
    //endregion

    //region Private Methods
    private static <T extends PlanOp> Optional<T> getPlanOp(
            CompositePlanOp compositePlanOp,
            Predicate<PlanOp> opPredicate,
            Function<Integer, Integer> direction,
            int startIndex) {

        for(int index = direction.apply(startIndex) ;
            index >= 0 && index < compositePlanOp.getOps().size() ;
            index = direction.apply(index)) {
            PlanOp planOp = compositePlanOp.getOps().get(index);
            if (opPredicate.test(planOp)) {
                return Optional.of((T)planOp);
            }
        }

        return Optional.empty();
    }

    private static Plan flatten(
            CompositePlanOp compositePlanOp,
            Predicate<PlanOp> opPredicate,
            Predicate<PlanOp> compositeOpPredicate) {
        List<PlanOp> flattenedPlanOps = new ArrayList<>(compositePlanOp.getOps());
        int index = 0;
        while(index < flattenedPlanOps.size()) {
            PlanOp planOp = flattenedPlanOps.get(index);
            if (CompositePlanOp.class.isAssignableFrom(planOp.getClass())) {
                CompositePlanOp innerCompositePlanOp = (CompositePlanOp)planOp;
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

    private static Function<Class<?>, Predicate<PlanOp>> classPredicateFunction =
            klass -> planOp -> klass.isAssignableFrom(planOp.getClass());

    private static Function<PlanOp, Predicate<PlanOp>> equalsPredicateFunction =
            planOp -> planOp2 -> planOp2.equals(planOp);

    private static Predicate<PlanOp> truePredicate = planOp -> true;
    //endregion

}
