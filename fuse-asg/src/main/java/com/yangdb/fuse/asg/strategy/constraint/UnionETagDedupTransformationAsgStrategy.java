package com.yangdb.fuse.asg.strategy.constraint;

/*-
 * #%L
 * fuse-asg
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

import com.yangdb.fuse.asg.strategy.AsgStrategy;
import com.yangdb.fuse.model.Tagged;
import com.yangdb.fuse.model.asgQuery.AsgEBase;
import com.yangdb.fuse.model.asgQuery.AsgQuery;
import com.yangdb.fuse.model.asgQuery.AsgQueryUtil;
import com.yangdb.fuse.model.asgQuery.AsgStrategyContext;
import com.yangdb.fuse.model.query.EBase;
import com.yangdb.fuse.model.query.properties.EPropGroup;
import com.yangdb.fuse.model.query.properties.constraint.ConstraintOp;
import com.yangdb.fuse.model.query.quant.QuantBase;
import com.yangdb.fuse.model.query.quant.QuantType;
import javaslang.collection.Stream;

import java.util.List;
import java.util.Map;

/**
 * search for or (union) type quants and dedup etags names
 */
public class UnionETagDedupTransformationAsgStrategy implements AsgStrategy {
    //region AsgStrategy Implementation
    @Override
    public void apply(AsgQuery query, AsgStrategyContext context) {
        Map<String, List<AsgEBase<EBase>>> map = AsgQueryUtil.groupByTags(query.getStart());
        map.values()
                .stream()
                .filter(v->v.size()>1)
                .forEach(this::dedupEtags);
    }

    private void dedupEtags(List<AsgEBase<EBase>> list) {
        list
                .stream()
                .skip(1)
                .map(AsgEBase::geteBase)
                .forEach(e-> ((Tagged)e).seteTag(String.format("%s:%d",((Tagged)e).geteTag(),e.geteNum())));
    }

    //endregion
}




