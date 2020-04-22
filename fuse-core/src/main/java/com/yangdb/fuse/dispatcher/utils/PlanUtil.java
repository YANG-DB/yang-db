package com.yangdb.fuse.dispatcher.utils;

/*-
 * #%L
 * fuse-core
 * %%
 * Copyright (C) 2016 - 2019 The YangDb Graph Database Project
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */



import com.yangdb.fuse.model.asgQuery.AsgQuery;
import com.yangdb.fuse.model.asgQuery.AsgQueryUtil;
import com.yangdb.fuse.model.execution.plan.*;
import com.yangdb.fuse.model.execution.plan.composite.CompositePlanOp;
import com.yangdb.fuse.model.execution.plan.composite.Plan;
import com.yangdb.fuse.model.execution.plan.composite.UnionOp;
import com.yangdb.fuse.model.execution.plan.entity.EntityFilterOp;
import com.yangdb.fuse.model.execution.plan.entity.EntityJoinOp;
import com.yangdb.fuse.model.execution.plan.entity.EntityOp;
import com.yangdb.fuse.model.execution.plan.entity.GoToEntityOp;
import com.yangdb.fuse.model.execution.plan.relation.RelationFilterOp;
import com.yangdb.fuse.model.execution.plan.relation.RelationOp;
import com.yangdb.fuse.model.query.Rel;
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

    public static boolean isLast(CompositePlanOp compositePlanOp, PlanOp planOp) {
        return compositePlanOp.getOps().size() > 0 && compositePlanOp.getOps().get(compositePlanOp.getOps().size()-1) == planOp;
    }

    public static boolean contains(CompositePlanOp compositePlanOp, int eNum) {
        return compositePlanOp.getOps().size() > 0 && compositePlanOp.getOps().stream().anyMatch(planOp -> ((AsgEBaseContainer)planOp).getAsgEbase().geteNum()==eNum);
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

    public static Optional<EntityOp> findGotoEntity(Plan plan, GoToEntityOp goToEntityOp){
        Optional<EntityOp> entityOp = first(plan, (Predicate<PlanOp>) planOp -> planOp instanceof EntityOp && ((EntityOp) planOp).getAsgEbase().geteBase().equals(goToEntityOp.getAsgEbase().geteBase()) && planOp != goToEntityOp).map(op -> (EntityOp)op);
        if(entityOp.isPresent()) {
            return entityOp;
        }
        for (EntityJoinOp entityJoinOp : Stream.ofAll(plan.getOps()).filter(op -> op instanceof EntityJoinOp).map(op -> (EntityJoinOp) op)) {
            entityOp = findGotoEntity(entityJoinOp.getLeftBranch(), goToEntityOp);
            if(entityOp.isPresent()) {
                return entityOp;
            }
            entityOp = findGotoEntity(entityJoinOp.getRightBranch(), goToEntityOp);
            if(entityOp.isPresent()) {
                return entityOp;
            }
        }
        return Optional.empty();

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
            } else if(EntityJoinOp.class.isAssignableFrom(planOp.getClass())){
                EntityJoinOp entityJoinOp = (EntityJoinOp) planOp;
                flattenedPlanOps.remove(index);
                if(compositeOpPredicate.test(entityJoinOp)){
                    flattenedPlanOps.addAll(index, entityJoinOp.getRightBranch().getOps());
                    flattenedPlanOps.addAll(index, entityJoinOp.getLeftBranch().getOps());
                }
            } else if(UnionOp.class.isAssignableFrom(planOp.getClass())){
                UnionOp unionOp = (UnionOp) planOp;
                flattenedPlanOps.remove(index);
                if(compositeOpPredicate.test(unionOp)){
                    int finalIndex = index;
                    unionOp.getPlans().forEach(p->flattenedPlanOps.addAll(finalIndex, p.getOps()));
                }
            }else{
                index++;
            }
        }

        return new Plan(Stream.ofAll(flattenedPlanOps).filter(opPredicate::test));
    }

    public static RelationFilterOp relFilterOp(AsgQuery query, int num) {
        return new RelationFilterOp(AsgQueryUtil.element$(query, num));
    }

    public static RelationOp relOp(AsgQuery query, int num) {
        return new RelationOp(AsgQueryUtil.element$(query, num));
    }

    public static RelationOp relOp(AsgQuery query, int num, Rel.Direction direction) {
        return new RelationOp(AsgQueryUtil.element$(query, num), direction);
    }

    public static EntityFilterOp filterOp(AsgQuery query, int num) {
        return new EntityFilterOp(AsgQueryUtil.element$(query, num));
    }

    public static EntityOp entityOp(AsgQuery query, int num) {
        return new EntityOp(AsgQueryUtil.element$(query, num));
    }

    public static GoToEntityOp gotoOp(AsgQuery query, int num) {
        return new GoToEntityOp(AsgQueryUtil.element$(query, num));
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
