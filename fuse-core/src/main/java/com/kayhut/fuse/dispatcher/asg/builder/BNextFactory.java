package com.kayhut.fuse.dispatcher.asg.builder;

/*-
 * #%L
 * fuse-core
 * %%
 * Copyright (C) 2016 - 2018 yangdb   ------ www.yangdb.org ------
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

import com.kayhut.fuse.dispatcher.asg.BellowFactory;
import com.kayhut.fuse.model.query.EBase;
import com.kayhut.fuse.model.query.Rel;
import com.kayhut.fuse.model.query.RelPattern;
import com.kayhut.fuse.model.query.Start;
import com.kayhut.fuse.model.query.aggregation.*;
import com.kayhut.fuse.model.query.combiner.HComb;
import com.kayhut.fuse.model.query.entity.*;
import com.kayhut.fuse.model.query.optional.OptionalComp;
import com.kayhut.fuse.model.query.properties.*;
import com.kayhut.fuse.model.query.quant.HQuant;
import com.kayhut.fuse.model.query.quant.Quant1;
import com.kayhut.fuse.model.query.quant.Quant2;

import java.util.*;
import java.util.function.Function;

/**
 * Created by benishue on 01-Mar-17.
 */

public class BNextFactory implements BellowFactory{

    //region Constructor
    public BNextFactory() {
        this.map = new HashMap<>() ;
        this.map.put(AggM5.class, ebase -> ((AggM5)ebase).getB() == 0 ? Collections.emptyList() : Collections.singletonList(((AggM5) ebase).getB()));
        this.map.put(AggM4.class, ebase -> ((AggM4)ebase).getB() == 0 ? Collections.emptyList() : Collections.singletonList(((AggM4) ebase).getB()));
        this.map.put(AggM3.class, ebase -> ((AggM3)ebase).getB() == 0 ? Collections.emptyList() : Collections.singletonList(((AggM3) ebase).getB()));
        this.map.put(AggM2.class, ebase -> ((AggM2)ebase).getB() == 0 ? Collections.emptyList() : Collections.singletonList(((AggM2) ebase).getB()));
        this.map.put(AggM1.class, ebase -> ((AggM1)ebase).getB() == 0 ? Collections.emptyList() : Collections.singletonList(((AggM1) ebase).getB()));
        this.map.put(AggL3.class, ebase -> ((AggL3)ebase).getB() == 0 ? Collections.emptyList() : Collections.singletonList(((AggL3) ebase).getB()));
        this.map.put(AggL2.class, ebase -> ((AggL2)ebase).getB() == 0 ? Collections.emptyList() : Collections.singletonList(((AggL2) ebase).getB()));
        this.map.put(AggL1.class, ebase -> ((AggL1)ebase).getB() == 0 ? Collections.emptyList() : Collections.singletonList(((AggL1) ebase).getB()));
        this.map.put(HComb.class, ebase -> ((HComb)ebase).getB() == 0 ? Collections.emptyList() : Collections.singletonList(((HComb) ebase).getB()));
        this.map.put(HQuant.class, ebase -> ((HQuant)ebase).getB());
        this.map.put(Quant1.class, ebase -> ((Quant1)ebase).getB() == 0 ? Collections.emptyList() : Collections.singletonList(((Quant1) ebase).getB()));
        this.map.put(Quant2.class, ebase -> (Collections.emptyList()));
        this.map.put(Rel.class, ebase -> ((Rel)ebase).getB() == 0 ? Collections.emptyList() : Collections.singletonList(((Rel) ebase).getB()));
        this.map.put(RelPattern.class, ebase -> ((Rel)ebase).getB() == 0 ? Collections.emptyList() : Collections.singletonList(((Rel) ebase).getB()));
        this.map.put(EndPattern.class, ebase -> ((EndPattern)ebase).getB() == 0 ? Collections.emptyList() : Collections.singletonList(((EndPattern) ebase).getB()));
        this.map.put(RelProp.class, ebase -> ((RelProp)ebase).getB() == 0 ? Collections.emptyList() : Collections.singletonList(((RelProp) ebase).getB()));
        this.map.put(RelPropGroup.class, ebase -> (Collections.emptyList()));
        this.map.put(ETyped.class, ebase -> ((ETyped)ebase).getB() == 0 ? Collections.emptyList() : Collections.singletonList(((ETyped) ebase).getB()));
        this.map.put(EUntyped.class, ebase -> (Collections.emptyList()));
        this.map.put(EAgg.class, ebase -> (Collections.emptyList()));
        this.map.put(EProp.class, ebase -> (Collections.emptyList()));
        this.map.put(CalculatedEProp.class, (ebase) -> (Collections.emptyList()));
        this.map.put(EPropGroup.class, ebase -> (Collections.emptyList()));
        this.map.put(EConcrete.class, ebase -> (Collections.emptyList()));
        this.map.put(Start.class, ebase -> ((Start)ebase).getB() == 0 ? Collections.emptyList() : Collections.singletonList(((Start) ebase).getB()));
        this.map.put(OptionalComp.class, (ebase) -> Collections.emptyList());
        this.map.put(CountComp.class, (ebase) -> Collections.emptyList());
    }
    //endregion

    //region Public Methods
    public List<Integer> supplyBellow(EBase eBase) {
        return this.map.get(eBase.getClass()).apply(eBase);
    }
    //endregion

    //region Fields
    private Map<Class, Function<EBase, List<Integer>>> map;

    //endregion
}
