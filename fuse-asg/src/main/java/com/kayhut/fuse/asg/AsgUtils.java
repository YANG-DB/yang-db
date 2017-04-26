package com.kayhut.fuse.asg;

import com.google.common.collect.Ordering;
import com.kayhut.fuse.model.asgQuery.AsgEBase;
import com.kayhut.fuse.model.query.EBase;
import com.kayhut.fuse.model.query.Rel;
import com.kayhut.fuse.model.query.entity.EEntityBase;
import com.kayhut.fuse.model.query.properties.EProp;
import com.kayhut.fuse.model.query.properties.RelProp;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by benishue on 19-Apr-17.
 */
public class AsgUtils {

    public static Map<Integer, AsgEBase> searchForAllEntitiesOfType(AsgEBase startElement, Class clazz){
       Map<Integer, AsgEBase> elementsResult = new HashMap<>();
       searchForAllEntitiesOfType(startElement,clazz ,elementsResult);
       return elementsResult;
   }

    public static boolean isAllNextChildrenEProps(AsgEBase<? extends EBase> parent) {
        boolean isAllChildrenEProps = true;
        for(AsgEBase asgEBase : parent.getNext())
        {
            if (!(asgEBase.geteBase() instanceof EProp)) {
                isAllChildrenEProps = false;
                break;
            }
        };
        return isAllChildrenEProps;
    }

    public static List<AsgEBase<? extends EBase>> getEPropsNextChildren(AsgEBase<? extends EBase> parent) {
        List<AsgEBase<? extends EBase>> ePropsNextChildren = new ArrayList<>();
        for(AsgEBase asgEBase : parent.getNext())
        {
            if ((asgEBase.geteBase() instanceof EProp)) {
                ePropsNextChildren.add(asgEBase);
            }
        };
        return ePropsNextChildren;
    }

    public static List<AsgEBase<? extends EBase>> getRelPropsBelowChildren(AsgEBase<? extends EBase> parent) {
        List<AsgEBase<? extends EBase>> rPropsBelowChildren = new ArrayList<>();

        while (parent.getB().size() == 1 && parent.getB().get(0).geteBase() instanceof RelProp) {
            rPropsBelowChildren.add(parent.getB().get(0));
            parent = parent.getB().get(0);
        }
        return rPropsBelowChildren;
    }

    private static void searchForAllEntitiesOfType(AsgEBase element, Class clazz, Map<Integer, AsgEBase> elementsResult){
            if (clazz.isAssignableFrom(element.geteBase().getClass())) {
                if(!elementsResult.containsKey(element.geteNum())) {
                    elementsResult.put(element.geteNum(), element)   ;
                }
            }
            if (shouldAdvanceToNext(element) && element.getNext() != null  && element.getNext().size() > 0)
                element.getNext().forEach(e -> searchForAllEntitiesOfType((AsgEBase) e ,clazz, elementsResult));
            if (shouldAdvanceToBs(element) && element.getB() != null && element.getB().size() > 0)
                element.getB().forEach(e -> searchForAllEntitiesOfType((AsgEBase) e, clazz ,elementsResult));
    }

    private static boolean shouldAdvanceToBs(AsgEBase element) {
        return element.getB() != null;
    }

    private static boolean shouldAdvanceToNext(AsgEBase element) {
        return element.getNext() != null;
    }

    public static <T extends EBase> int getMinEnumFromListOfEBase(List<T> eBaseList )
    {
        List<Integer> enumsList = eBaseList.stream().map(T::geteNum).collect(Collectors.toList());
        int min = 999;
        min = Ordering.<Integer>natural().min(enumsList);
        return min;
    }
}
