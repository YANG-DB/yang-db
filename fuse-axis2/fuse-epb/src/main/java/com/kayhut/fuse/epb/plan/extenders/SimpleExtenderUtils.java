package com.kayhut.fuse.epb.plan.extenders;

import com.kayhut.fuse.model.asgQuery.AsgEBase;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.execution.plan.Plan;
import com.kayhut.fuse.model.query.EBase;
import com.kayhut.fuse.model.query.Start;
import javaslang.Tuple2;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Created by moti on 2/28/2017.
 */
public class SimpleExtenderUtils {
    public static Map<Integer, AsgEBase> flattenQuery(AsgQuery query) {
        Map<Integer, AsgEBase> elements = new HashMap<>();
        flattenQueryRecursive(query.getStart(), elements);
        return elements;
    }

    public static void flattenQueryRecursive(AsgEBase element, Map<Integer, AsgEBase> allElements){
        if(!allElements.containsKey(element.geteNum())) {
            if(shouldAddElement(element))
                allElements.put(element.geteNum(), element);
            if(shouldAdvanceToNext(element) && element.getNext() != null)
                element.getNext().forEach(e -> flattenQueryRecursive((AsgEBase)e, allElements));
            if(shouldAdvanceToBs(element) && element.getB() != null)
                element.getB().forEach(e -> flattenQueryRecursive((AsgEBase)e, allElements));
        }
    }

    public static boolean shouldAdvanceToBs(AsgEBase element) {
        return element.getB() != null;
    }

    public static boolean shouldAdvanceToNext(AsgEBase element) {
        return element.getNext() != null;
    }

    public static boolean shouldAddElement(AsgEBase element) {
        return element != null && !(element.geteBase() instanceof Start);
    }

    /**
     * Takes a flattened query and an execution plan, and seperates the query to two collections - parts that exist
     * in the plan and parts that do not exist
     * @param plan
     * @param queryParts
     * @return A tuple, where the first element is the query parts that exist in the plan ("handled"), and a map with the
     * "unhandled" parts
     */
    public static <C> Tuple2<List<AsgEBase>, Map<Integer, AsgEBase>> removeHandledQueryParts(Plan<C> plan, Map<Integer, AsgEBase> queryParts) {
        Map<Integer, AsgEBase> unHandledParts = new HashMap<>(queryParts);
        List<AsgEBase> handledParts = new LinkedList<>();
        plan.getOps().forEach(op -> {
            handledParts.add(queryParts.get(op.getOpBase().geteNum()));
            unHandledParts.remove(op.getOpBase().geteNum());
        });
        return new Tuple2<>(handledParts, unHandledParts);
    }

    public static boolean shouldAdvanceToParents(AsgEBase<? extends EBase> handledPartToExtend) {
        return handledPartToExtend.getParents() != null;
    }
}
