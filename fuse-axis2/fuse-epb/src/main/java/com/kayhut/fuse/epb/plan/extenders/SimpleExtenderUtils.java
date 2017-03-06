package com.kayhut.fuse.epb.plan.extenders;

import com.kayhut.fuse.model.execution.plan.Plan;
import com.kayhut.fuse.model.asgQuery.AsgEBase;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.query.Start;

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

    public static List<AsgEBase> removeHandledParts(Plan plan, Map<Integer, AsgEBase> queryParts) {
        List<AsgEBase> handledParts = new LinkedList<>();
        plan.getOps().forEach(op -> {
            handledParts.add(queryParts.get(op.geteNum()));
            queryParts.remove(op.geteNum());
        });
        return handledParts;
    }
}
