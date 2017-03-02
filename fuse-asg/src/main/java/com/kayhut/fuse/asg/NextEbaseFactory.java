package com.kayhut.fuse.asg;

import com.kayhut.fuse.model.query.*;

import java.util.*;
import java.util.function.Function;

/**
 * Created by benishue on 01-Mar-17.
 */
public class NextEbaseFactory {
    //region Constructor
    public NextEbaseFactory() {
        this.map = new HashMap<>() ;
        this.map.put(ETyped.class, (ebase) -> ((ETyped)ebase).getNext() == 0 ? Collections.emptyList() : Arrays.asList(((ETyped) ebase).getNext()));
        this.map.put(EUntyped.class, (ebase) -> ((EUntyped)ebase).getNext() == 0 ? Collections.emptyList() : Arrays.asList(((EUntyped) ebase).getNext()));
        this.map.put(EAgg.class, (ebase) -> ((EAgg)ebase).getNext() == 0 ? Collections.emptyList() : Arrays.asList(((EAgg) ebase).getNext()));
        this.map.put(EConcrete.class, (ebase) -> ((EConcrete)ebase).getNext() == 0 ? Collections.emptyList() : Arrays.asList(((EConcrete) ebase).getNext()));
        this.map.put(ELog.class, (ebase) -> ((ELog)ebase).getNext() == 0 ? Collections.emptyList() : Arrays.asList(((ELog) ebase).getNext()));
        this.map.put(Quant1.class, (ebase) -> ((Quant1) ebase).getNext());
        this.map.put(Quant2.class, (ebase) -> ((Quant2) ebase).getNext());
        this.map.put(Rel.class, (ebase) -> ((Rel)ebase).getNext() == 0 ? Collections.emptyList() : Arrays.asList(((Rel) ebase).getNext()));
        this.map.put(Start.class, (ebase) -> ((Start)ebase).getNext() == 0 ? Collections.emptyList() : Arrays.asList(((Start) ebase).getNext()));
    }
    //endregion

    //region Public Methods
    public List<Integer> supply(EBase eBase) {
        return this.map.get(eBase.getClass()).apply(eBase);
    }
    //endregion

    //region Fields
    private Map<Class, Function<EBase, List<Integer>>> map;
    //endregion
}
