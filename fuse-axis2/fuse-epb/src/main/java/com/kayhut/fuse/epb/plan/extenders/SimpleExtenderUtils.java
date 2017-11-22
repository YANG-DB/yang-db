package com.kayhut.fuse.epb.plan.extenders;

import com.kayhut.fuse.dispatcher.utils.AsgQueryUtil;
import com.kayhut.fuse.dispatcher.utils.PlanUtil;
import com.kayhut.fuse.model.asgQuery.AsgEBase;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.execution.plan.*;
import com.kayhut.fuse.model.query.EBase;
import com.kayhut.fuse.model.query.Rel;
import com.kayhut.fuse.model.query.Start;
import com.kayhut.fuse.model.query.entity.EConcrete;
import com.kayhut.fuse.model.query.entity.EEntityBase;
import com.kayhut.fuse.model.query.entity.ETyped;
import com.kayhut.fuse.model.query.entity.EUntyped;
import com.kayhut.fuse.model.query.optional.OptionalComp;
import com.kayhut.fuse.model.query.properties.EProp;
import com.kayhut.fuse.model.query.properties.EPropGroup;
import com.kayhut.fuse.model.query.properties.RelProp;
import com.kayhut.fuse.model.query.properties.RelPropGroup;
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
            if (shouldAddElement(element)) {
                allElements.put(element.geteNum(), element);
            }

            if (shouldAdvanceToNext(element) && element.getNext() != null) {
                element.getNext().forEach(e -> flattenQueryRecursive((AsgEBase) e, allElements));
            }

            if (shouldAdvanceToBs(element) && element.getB() != null) {
                element.getB().forEach(e -> flattenQueryRecursive((AsgEBase) e, allElements));
            }
        }
    }

    static boolean shouldAdvanceToBs(AsgEBase element) {
        return element.getB() != null;
    }

    static boolean shouldAdvanceToNext(AsgEBase element) {
        return element.getNext() != null;
    }

    static boolean shouldAddElement(AsgEBase element) {
        return element != null &&
                !(element.geteBase() instanceof Start) &&
                !(element.geteBase() instanceof OptionalComp);
    }

    /**
     * Takes a flattened query and an execution plan, and seperates the query getTo two collections - parts that exist
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

        Stream.ofAll(PlanUtil.flat(plan).getOps()).forEach(op -> {
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

        Set<Class<?>> handledClasses = new HashSet<>(Arrays.asList(
                EConcrete.class, EUntyped.class, ETyped.class, Rel.class, EProp.class,
                EPropGroup.class, RelProp.class, RelPropGroup.class));

        return Stream.ofAll(partsTuple._2().values()).filter(asgEBase -> handledClasses.contains(asgEBase.geteBase().getClass()))
                .isEmpty();
    }

    static Set<Integer> markEntitiesAndRelations(Plan plan) {
        return Stream.ofAll(PlanUtil.flat(plan).getOps()).map(PlanOpBase::geteNum).toJavaSet();
    }


    static <T extends EBase, S extends EBase> Optional<AsgEBase<S>> getNextDescendantUnmarkedOfType(Class<? extends EBase> type,
                                                                                                    AsgEBase<T> asgEBase,
                                                                                                    Set<Integer> markedElements) {
        return AsgQueryUtil.nextDescendant(asgEBase,
                (child) -> type.isAssignableFrom(child.geteBase().getClass()) &&
                        !markedElements.contains(child.geteNum()));
    }

    static <T extends EBase, S extends EBase> Optional<AsgEBase<S>> getNextAncestorUnmarkedOfType(Class<? extends EBase> type,
                                                                                                  AsgEBase<T> asgEBase,
                                                                                                  Set<Integer> markedElements) {
        return AsgQueryUtil.ancestor(asgEBase,
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
            if (clazz.isAssignableFrom(planOp.getClass())) {
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
            Optional<AsgEBase<EEntityBase>> parentEntity = AsgQueryUtil.ancestor(lastEntityOp.getAsgEBase(), EEntityBase.class);
            while (parentEntity.isPresent()) {
                nextRelation = getNextDescendantUnmarkedOfType(type, parentEntity.get(), markedElements);
                if (nextRelation.isPresent()) {
                    break;
                }

                parentEntity = AsgQueryUtil.ancestor(parentEntity.get(), EEntityBase.class);
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

        EntityOp lastEntityOp = getLastOpOfType(plan, EntityOp.class);
        if(lastEntityOp==null)
            return Collections.emptyList();

        return AsgQueryUtil.nextDescendants(lastEntityOp.getAsgEBase(),(child) -> type.isAssignableFrom(child.geteBase().getClass()) ,
                p -> {
                    if(p.equals(lastEntityOp.getAsgEBase()))
                        return true;

                    List path = AsgQueryUtil.pathToAncestor(p, lastEntityOp.getAsgEBase().geteBase().geteNum());
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

