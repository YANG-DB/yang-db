package com.kayhut.fuse.gta.translation;

import com.kayhut.fuse.model.execution.plan.PlanOpBase;
import org.apache.tinkerpop.gremlin.process.traversal.Traversal;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * Created by moti on 3/7/2017.
 */
public class PlanOpTranslatorFactoryImpl {

    //region Public Methods
    public Traversal supply(PlanOpBase op, Traversal traversal) {
        return this.map.get(op.getClass()).apply(op, traversal);
    }
    //endregion

    //region Fields
    private Map<Class, BiFunction<PlanOpBase, Traversal, Traversal>> map;
    //endregion


    public PlanOpTranslatorFactoryImpl() {
        this.map = new HashMap<>() ;
        //this.map.put(Start.class, (ebase) -> ((Start)ebase).getNext() == 0 ? Collections.emptyList() : Arrays.asList(((Start) ebase).getNext()));
    }
}
