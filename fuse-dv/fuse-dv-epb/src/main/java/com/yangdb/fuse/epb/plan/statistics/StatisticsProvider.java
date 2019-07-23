package com.yangdb.fuse.epb.plan.statistics;

/*-
 * #%L
 * fuse-dv-epb
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

import com.yangdb.fuse.model.execution.plan.Direction;
import com.yangdb.fuse.model.query.EBase;
import com.yangdb.fuse.model.query.Rel;
import com.yangdb.fuse.model.query.entity.EEntityBase;
import com.yangdb.fuse.model.query.properties.EPropGroup;
import com.yangdb.fuse.model.query.properties.RelPropGroup;

/**
 * Created by moti on 31/03/2017.
 */
public interface StatisticsProvider {
    /**
     *
     * @param item
     * @return
     */
    Statistics.SummaryStatistics getNodeStatistics(EEntityBase item);

    /**
     *
     * @param item
     * @param entityFilter
     * @return
     */
    Statistics.SummaryStatistics getNodeFilterStatistics(EEntityBase item, EPropGroup entityFilter);

    /**
     *
     * @param item
     * @param source
     * @return
     */
    Statistics.SummaryStatistics getEdgeStatistics(Rel item, EEntityBase source);

    /**
     *
     * @param item
     * @param entityFilter
     * @param source
     * @return
     */
    Statistics.SummaryStatistics getEdgeFilterStatistics(Rel item, RelPropGroup entityFilter, EEntityBase source);


    //Statistics.SummaryStatistics getRedundantEdgeStatistics(Rel rel, RelPropGroup relPropGroup,DirectionSchema direction);


    Statistics.SummaryStatistics getRedundantNodeStatistics(EEntityBase entity, RelPropGroup relPropGroup);

    /**
     * get average number of edges per node (by label context)
     * @return
     */
    long getGlobalSelectivity(Rel rel, RelPropGroup filter, EBase entity, Direction direction) ;

}
