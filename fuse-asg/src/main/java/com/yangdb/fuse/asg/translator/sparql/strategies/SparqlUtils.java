package com.yangdb.fuse.asg.translator.sparql.strategies;

/*-
 * #%L
 * fuse-asg
 * %%
 * Copyright (C) 2016 - 2020 The YangDb Graph Database Project
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

import com.yangdb.fuse.model.asgQuery.AsgEBase;
import com.yangdb.fuse.model.asgQuery.AsgQuery;
import com.yangdb.fuse.model.asgQuery.AsgQueryUtil;
import com.yangdb.fuse.model.query.EBase;
import com.yangdb.fuse.model.query.quant.Quant1;
import com.yangdb.fuse.model.query.quant.QuantBase;
import com.yangdb.fuse.model.query.quant.QuantType;

import java.util.ArrayList;
import java.util.List;

import static com.yangdb.fuse.model.asgQuery.AsgQueryUtil.max;

public interface SparqlUtils {

    /**
     * create or get relevant quant element on query
     * @param byTag
     * @param query
     * @param context
     * @param parentQuantType
     * @return
     */
    static AsgEBase<? extends EBase> quant(AsgEBase<? extends EBase> byTag,
                                           AsgQuery query, SparqlStrategyContext context,
                                           QuantType parentQuantType) {
        //next find the quant associated with this element - if none found create one
        if (AsgQueryUtil.nextAdjacentDescendants(byTag, QuantBase.class).stream().noneMatch(g -> ((QuantBase) g.geteBase()).getqType().equals(parentQuantType))) {
            //quants will get enum according to the next formula = scopeElement.enum * 100
            final AsgEBase<Quant1> quantAsg = new AsgEBase<>(new Quant1(max(query) +1,  parentQuantType, new ArrayList<>(), 0));
            //is scope already has next - add them to the newly added quant
            if (context.getScope().hasNext()) {
                final List<AsgEBase<? extends EBase>> next = context.getScope().getNext();
                quantAsg.setNext(new ArrayList<>(next));
                context.getScope().setNext(new ArrayList<>());
            }
            context.getScope().addNext(quantAsg);
            context.scope(quantAsg);
        }
        return AsgQueryUtil.nextAdjacentDescendants(byTag, QuantBase.class).stream()
                .filter(g -> ((QuantBase) g.geteBase()).getqType().equals(parentQuantType))
                .findFirst().get();
    }

}
