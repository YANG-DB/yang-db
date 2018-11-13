package com.kayhut.fuse.dispatcher.asg.builder;

/*-
 * #%L
 * fuse-core
 * %%
 * Copyright (C) 2016 - 2018 kayhut
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

import com.kayhut.fuse.dispatcher.asg.NextFactory;
import com.kayhut.fuse.model.query.EBase;
import com.kayhut.fuse.model.query.Rel;
import com.kayhut.fuse.model.query.Start;
import com.kayhut.fuse.model.query.aggregation.*;
import com.kayhut.fuse.model.query.combiner.HComb;
import com.kayhut.fuse.model.query.entity.*;
import com.kayhut.fuse.model.query.optional.OptionalComp;
import com.kayhut.fuse.model.query.properties.EProp;
import com.kayhut.fuse.model.query.properties.EPropGroup;
import com.kayhut.fuse.model.query.properties.RelProp;
import com.kayhut.fuse.model.query.properties.RelPropGroup;
import com.kayhut.fuse.model.query.quant.HQuant;
import com.kayhut.fuse.model.query.quant.Quant1;
import com.kayhut.fuse.model.query.quant.Quant2;

import java.util.*;
import java.util.function.Function;

/**
 * Created by benishue on 01-Mar-17.
 */
public class NextEbaseFactory implements NextFactory {

    //region Constructor
    public NextEbaseFactory() {
        this.map = new HashMap<>() ;
        this.map.put(AggM5.class, (ebase) -> (Collections.emptyList()));
        this.map.put(AggM4.class, (ebase) -> (Collections.emptyList()));
        this.map.put(AggM3.class, (ebase) -> (Collections.emptyList()));
        this.map.put(AggM2.class, (ebase) -> (Collections.emptyList()));
        this.map.put(AggM1.class, (ebase) -> (Collections.emptyList()));
        this.map.put(AggL3.class, (ebase) -> (Collections.emptyList()));
        this.map.put(AggL2.class, (ebase) -> (Collections.emptyList()));
        this.map.put(AggL1.class, (ebase) -> (Collections.emptyList()));
        this.map.put(HComb.class, (ebase) -> (Collections.emptyList()));
        this.map.put(HQuant.class, (ebase) -> (Collections.emptyList()));
        this.map.put(RelProp.class, (ebase) ->  (Collections.emptyList()));
        this.map.put(RelPropGroup.class, (ebase) ->  (Collections.emptyList()));
        this.map.put(ETyped.class, (ebase) -> ((ETyped)ebase).getNext() == 0 ? Collections.emptyList() : Collections.singletonList(((ETyped) ebase).getNext()));
        this.map.put(EUntyped.class, (ebase) -> ((EUntyped)ebase).getNext() == 0 ? Collections.emptyList() : Collections.singletonList(((EUntyped) ebase).getNext()));
        this.map.put(EAgg.class, (ebase) -> ((EAgg)ebase).getNext() == 0 ? Collections.emptyList() : Collections.singletonList(((EAgg) ebase).getNext()));
        this.map.put(EConcrete.class, (ebase) -> ((EConcrete)ebase).getNext() == 0 ? Collections.emptyList() : Collections.singletonList(((EConcrete) ebase).getNext()));
        this.map.put(ELog.class, (ebase) -> ((ELog)ebase).getNext() == 0 ? Collections.emptyList() : Collections.singletonList(((ELog) ebase).getNext()));
        this.map.put(EProp.class, (ebase) -> (Collections.emptyList()));
        this.map.put(EPropGroup.class, (ebase) -> (Collections.emptyList()));
        this.map.put(Quant1.class, (ebase) -> ((Quant1) ebase).getNext());
        this.map.put(Quant2.class, (ebase) -> ((Quant2) ebase).getNext());
        this.map.put(Rel.class, (ebase) -> ((Rel)ebase).getNext() == 0 ? Collections.emptyList() : Collections.singletonList(((Rel) ebase).getNext()));
        this.map.put(Start.class, (ebase) -> ((Start)ebase).getNext() == 0 ? Collections.emptyList() : Collections.singletonList(((Start) ebase).getNext()));
        this.map.put(OptionalComp.class, (ebase) -> ((OptionalComp)ebase).getNext() == 0 ? Collections.emptyList() : Collections.singletonList(((OptionalComp)ebase).getNext()));
    }
    //endregion

    //region Public Methods
    @Override
    public List<Integer> supplyNext(EBase eBase) {
        return this.map.get(eBase.getClass()).apply(eBase);
    }
    //endregion

    //region Fields
    private Map<Class, Function<EBase, List<Integer>>> map;
    //endregion
}
