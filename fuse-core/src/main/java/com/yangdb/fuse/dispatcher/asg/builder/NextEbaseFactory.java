package com.yangdb.fuse.dispatcher.asg.builder;

/*-
 * #%L
 * fuse-core
 * %%
 * Copyright (C) 2016 - 2019 The YangDb Graph Database Project
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


import com.yangdb.fuse.dispatcher.asg.NextFactory;
import com.yangdb.fuse.model.query.*;
import com.yangdb.fuse.model.query.aggregation.Agg;
import com.yangdb.fuse.model.query.aggregation.CountComp;
import com.yangdb.fuse.model.query.entity.*;
import com.yangdb.fuse.model.query.optional.OptionalComp;
import com.yangdb.fuse.model.query.properties.*;
import com.yangdb.fuse.model.query.quant.HQuant;
import com.yangdb.fuse.model.query.quant.Quant1;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * Created by benishue on 01-Mar-17.
 */
public class NextEbaseFactory implements NextFactory {

    //region Constructor
    public NextEbaseFactory() {
        this.map = new HashMap<>() ;
        this.map.put(Agg.class, (ebase) -> ((Agg)ebase).getNext() == 0 ? Collections.emptyList() : Collections.singletonList(((Agg) ebase).getNext()));
        this.map.put(HQuant.class, (ebase) -> (Collections.emptyList()));
        this.map.put(RelProp.class, (ebase) ->  (Collections.emptyList()));
        this.map.put(RelPropGroup.class, (ebase) ->  (Collections.emptyList()));
        this.map.put(ETyped.class, (ebase) -> ((ETyped)ebase).getNext() == 0 ? Collections.emptyList() : Collections.singletonList(((ETyped) ebase).getNext()));
        this.map.put(EUntyped.class, (ebase) -> ((EUntyped)ebase).getNext() == 0 ? Collections.emptyList() : Collections.singletonList(((EUntyped) ebase).getNext()));
        this.map.put(EAgg.class, (ebase) -> ((EAgg)ebase).getNext() == 0 ? Collections.emptyList() : Collections.singletonList(((EAgg) ebase).getNext()));
        this.map.put(EConcrete.class, (ebase) -> ((EConcrete)ebase).getNext() == 0 ? Collections.emptyList() : Collections.singletonList(((EConcrete) ebase).getNext()));
        this.map.put(EProp.class, (ebase) -> (Collections.emptyList()));
        this.map.put(CalculatedEProp.class, (ebase) -> (Collections.emptyList()));
        this.map.put(EPropGroup.class, (ebase) -> (Collections.emptyList()));
        this.map.put(Quant1.class, (ebase) -> ((Quant1) ebase).getNext());
        this.map.put(Rel.class, (ebase) -> ((Rel)ebase).getNext() == 0 ? Collections.emptyList() : Collections.singletonList(((Rel) ebase).getNext()));
        this.map.put(RelUntyped.class, (ebase) -> ((RelUntyped)ebase).getNext() == 0 ? Collections.emptyList() : Collections.singletonList(((RelUntyped) ebase).getNext()));
        this.map.put(RelPattern.class, (ebase) -> ((Rel)ebase).getNext() == 0 ? Collections.emptyList() : Collections.singletonList(((Rel) ebase).getNext()));
        this.map.put(TypedEndPattern.class, (ebase) -> ((TypedEndPattern)ebase).getNext() == 0 ? Collections.emptyList() : Collections.singletonList(((TypedEndPattern) ebase).getNext()));
        this.map.put(UnTypedEndPattern.class, (ebase) -> ((UnTypedEndPattern)ebase).getNext() == 0 ? Collections.emptyList() : Collections.singletonList(((UnTypedEndPattern) ebase).getNext()));
        this.map.put(Start.class, (ebase) -> ((Start)ebase).getNext() == 0 ? Collections.emptyList() : Collections.singletonList(((Start) ebase).getNext()));
        this.map.put(OptionalComp.class, (ebase) -> ((OptionalComp)ebase).getNext() == 0 ? Collections.emptyList() : Collections.singletonList(((OptionalComp)ebase).getNext()));
        this.map.put(CountComp.class, (ebase) -> ((CountComp)ebase).getNext() == 0 ? Collections.emptyList() : Collections.singletonList(((CountComp)ebase).getNext()));
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
