package com.kayhut.fuse.epb.plan.extenders;

import com.kayhut.fuse.asg.util.AsgQueryUtils;
import com.kayhut.fuse.model.asgQuery.AsgEBase;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.execution.plan.AsgEBasePlanOp;
import com.kayhut.fuse.model.execution.plan.EntityOp;
import com.kayhut.fuse.model.execution.plan.Plan;
import com.kayhut.fuse.model.execution.plan.PlanOpBase;
import com.kayhut.fuse.model.query.EBase;
import com.kayhut.fuse.model.query.Start;
import com.kayhut.fuse.model.query.entity.EEntityBase;
import javaslang.Tuple2;
import javaslang.collection.Stream;

import java.util.*;

/**
 * Created by moti on 2/28/2017.
 */
public interface SimpleExtenderUtils {
    static Map<Integer, AsgEBase> flattenQuery(AsgQuery query) {
        Map<Integer, AsgEBase> elements = new HashMap<>();
        flattenQueryRecursive(query.getStart(), elements);
        return elements;
    }

    static void flattenQueryRecursive(AsgEBase element, Map<Integer, AsgEBase> allElements) {
        if (!allElements.containsKey(element.geteNum())) {
            if (shouldAddElement(element))
                allElements.put(element.geteNum(), element);
            if (shouldAdvanceToNext(element) && element.getNext() != null)
                element.getNext().forEach(e -> flattenQueryRecursive((AsgEBase) e, allElements));
            if (shouldAdvanceToBs(element) && element.getB() != null)
                element.getB().forEach(e -> flattenQueryRecursive((AsgEBase) e, allElements));
        }
    }

    static boolean shouldAdvanceToBs(AsgEBase element) {
        return element.getB() != null;
    }

    static boolean shouldAdvanceToNext(AsgEBase element) {
        return element.getNext() != null;
    }

    static boolean shouldAddElement(AsgEBase element) {
        return element != null && !(element.geteBase() instanceof Start);
    }

    /**
     * Takes a flattened query and an execution plan, and seperates the query to two collections - parts that exist
     * in the plan and parts that do not exist
     *
     * @param plan
     * @param queryParts
     * @return A tuple, where the first element is the query parts that exist in the plan ("handled"), and a map with the
     * "unhandled" parts
     */
    static <C> Tuple2<List<AsgEBase>, Map<Integer, AsgEBase>> removeHandledQueryParts(Plan plan, Map<Integer, AsgEBase> queryParts) {
        Map<Integer, AsgEBase> unHandledParts = new HashMap<>(queryParts);
        List<AsgEBase> handledParts = new LinkedList<>();
        plan.getOps().forEach(op -> {
            handledParts.add(queryParts.get(op.geteNum()));
            unHandledParts.remove(op.geteNum());
        });
        return new Tuple2<>(handledParts, unHandledParts);
    }

    static boolean shouldAdvanceToParents(AsgEBase<? extends EBase> handledPartToExtend) {
        return handledPartToExtend.getParents() != null;
    }

    static <C> boolean checkIfPlanIsComplete(Plan plan, AsgQuery query) {
        Map<Integer, AsgEBase> queryParts = SimpleExtenderUtils.flattenQuery(query);
        Tuple2<List<AsgEBase>, Map<Integer, AsgEBase>> partsTuple = SimpleExtenderUtils.removeHandledQueryParts(plan, queryParts);
        return partsTuple._2().isEmpty();
    }

    static Set<Integer> markEntitiesAndRelations(Plan plan) {
        return Stream.ofAll(plan.getOps()).map(op -> op.geteNum()).toJavaSet();
    }


    static <T extends EBase, S extends EBase> Optional<AsgEBase<S>> getNextDescendantUnmarkedOfType(Class<? extends EBase> type,
                                                                                                    AsgEBase<T> asgEBase,
                                                                                                    Set<Integer> markedElements) {
        return AsgQueryUtils.getNextDescendant(asgEBase,
                (child) -> type.isAssignableFrom(child.geteBase().getClass()) &&
                        !markedElements.contains(child.geteNum()));
    }

    static <T extends EBase, S extends EBase> Optional<AsgEBase<S>> getNextAncestorUnmarkedOfType(Class<? extends EBase> type,
                                                                                                  AsgEBase<T> asgEBase,
                                                                                                  Set<Integer> markedElements) {
        return AsgQueryUtils.getAncestor(asgEBase,
                (child) -> type.isAssignableFrom(child.geteBase().getClass()) &&
                        !markedElements.contains(child.geteNum()));
    }


    /**
     * get last entity op in given plan
     *
     * @param plan
     * @return
     */
    static <T extends AsgEBasePlanOp<EEntityBase>> T getLastOpOfType(Plan plan, Class<T> clazz) {
        T lastEntityOp = null;
        for (int i = plan.getOps().size() - 1; i >= 0; i--) {
            PlanOpBase planOp = plan.getOps().get(i);
            if (planOp.getClass().equals(clazz)) {
                lastEntityOp = (T) planOp;
                break;
            }
        }

        return lastEntityOp;
    }

    /**
     * get next Rel type element which was not visited already
     *
     * @param plan
     * @return
     */
    static <T extends EBase> Optional<AsgEBase<T>> getNextDescendantUnmarkedOfType(Plan plan, Class<T> type) {
        Set<Integer> markedElements = markEntitiesAndRelations(plan);
        EntityOp lastEntityOp = getLastOpOfType(plan, EntityOp.class);
        if(lastEntityOp==null)
            return Optional.empty();

        Optional<AsgEBase<T>> nextRelation = getNextDescendantUnmarkedOfType(type, lastEntityOp.getAsgEBase(), markedElements);
        if (!nextRelation.isPresent()) {
            Optional<AsgEBase<EEntityBase>> parentEntity = AsgQueryUtils.getAncestor(lastEntityOp.getAsgEBase(), EEntityBase.class);
            while (parentEntity.isPresent()) {
                nextRelation = getNextDescendantUnmarkedOfType(type, parentEntity.get(), markedElements);
                if (nextRelation.isPresent()) {
                    break;
                }

                parentEntity = AsgQueryUtils.getAncestor(parentEntity.get(), EEntityBase.class);
            }
        }

        return nextRelation;
    }

    /**
     * get next Rel type element which was not visited already
     *
     * @param plan
     * @return
     */
    static <T extends EBase> List<AsgEBase<T>> getNextDescendantsUnmarkedOfType(Plan plan, Class<T> type) {
        Set<Integer> markedElements = markEntitiesAndRelations(plan);

        EntityOp lastEntityOp = getLastOpOfType(plan, EntityOp.class);
        if(lastEntityOp==null)
            return Collections.emptyList();

        return AsgQueryUtils.getNextDescendants(lastEntityOp.getAsgEBase(),
                (child) -> type.isAssignableFrom(child.geteBase().getClass()) && !markedElements.contains(child.geteNum()),
                p -> {
                    if(p.equals(lastEntityOp.getAsgEBase()))
                        return true;

                    List path = AsgQueryUtils.getPathToAncestor(p, lastEntityOp.getAsgEBase().geteBase().geteNum());
                    return !path.isEmpty() && path.size()<3;
                });
    }


    /**
     * get next Rel type element which was not visited already
     *
     * @param plan
     * @return
     */
    static <T extends EBase> Optional<AsgEBase<T>> getNextAncestorUnmarkedOfType(Plan plan, Class<T> type) {
        Set<Integer> markedElements = markEntitiesAndRelations(plan);
        EntityOp lastEntityOp = getLastOpOfType(plan, EntityOp.class);
        return getNextAncestorUnmarkedOfType(type, lastEntityOp.getAsgEBase(), markedElements);
    }

    /**
     * get next Rel type element which was not visited already
     *
     * @param plan
     * @return
     */
    static <T extends EBase> Optional<AsgEBase<T>> getNextAncestorOfType(Plan plan, Class<T> type) {
        EntityOp lastEntityOp = getLastOpOfType(plan, EntityOp.class);
        return getNextAncestorUnmarkedOfType(type, lastEntityOp.getAsgEBase(), Collections.EMPTY_SET);
    }
}

