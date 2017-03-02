package com.kayhut.fuse.epb.plan.extenders;

import com.kayhut.fuse.model.execution.plan.Plan;
import com.kayhut.fuse.model.queryAsg.AsgQuery;
import com.kayhut.fuse.model.queryAsg.EBaseAsg;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Created by moti on 2/28/2017.
 */
public class SimpleExtenderUtils {
    public static Map<Integer, EBaseAsg> flattenQuery(AsgQuery query) {
        Map<Integer, EBaseAsg> elements = new HashMap<>();
        flattenQueryRecursive(query.getStart(), elements);
        return elements;
    }

    public static void flattenQueryRecursive(EBaseAsg element, Map<Integer, EBaseAsg> allElements){
        if(!allElements.containsKey(element.geteNum())) {
            if(shouldAddElement(element))
                allElements.put(element.geteNum(), element);
            if(shouldAdvanceToNext(element) && element.getNext() != null)
                element.getNext().forEach(e -> flattenQueryRecursive(e, allElements));
            if(shouldAdvanceToBs(element) && element.getB() != null)
                element.getB().forEach(e -> flattenQueryRecursive(e, allElements));
        }
    }

    public static boolean shouldAdvanceToBs(EBaseAsg element) {
        return element.getB() != null;
    }

    public static boolean shouldAdvanceToNext(EBaseAsg element) {
        return element.getNext() != null;
    }

    public static boolean shouldAddElement(EBaseAsg element) {
        return true;
    }

    public static List<EBaseAsg> removeHandledParts(Plan plan, Map<Integer, EBaseAsg> queryParts) {
        List<EBaseAsg> handledParts = new LinkedList<>();
        plan.getOps().forEach(op -> {
            handledParts.add(queryParts.get(op.geteNum()));
            queryParts.remove(op.geteNum());
        });
        return handledParts;
    }
}
