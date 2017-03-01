package com.kayhut.fuse.asg;

import com.kayhut.fuse.model.query.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * Created by benishue on 01-Mar-17.
 */
public class NextEbaseFactory {

    public NextEbaseFactory() {
        this.map = new HashMap<Class, Function<EBase, List<Integer>>>() ;
        this.map.put(ETyped.class, (ebase) -> Arrays.asList(((ETyped) ebase).getNext()));
        this.map.put(EUntyped.class, (ebase) -> Arrays.asList(((EUntyped) ebase).getNext()));
        this.map.put(EAgg.class, (ebase) -> Arrays.asList(((EAgg) ebase).getNext()));
        this.map.put(EConcrete.class, (ebase) -> Arrays.asList(((EConcrete) ebase).getNext()));
        this.map.put(ELog.class, (ebase) -> Arrays.asList(((ELog) ebase).getNext()));
        this.map.put(Quant1.class, (ebase) -> ((Quant1) ebase).getNext());
        this.map.put(Quant2.class, (ebase) -> ((Quant2) ebase).getNext());
        this.map.put(Rel.class, (ebase) -> Arrays.asList(((Rel) ebase).getNext()));
        this.map.put(Start.class, (ebase) -> Arrays.asList(((Start) ebase).getNext()));
    }

    public List<Integer> supply(EBase eBase) {
        return this.map.get(eBase.getClass()).apply(eBase);
    }

    private Map<Class, Function<EBase, List<Integer>>> map;
}
