package com.kayhut.fuse.unipop.controller.promise.appender;

/*-
 * #%L
 * fuse-dv-unipop
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

import com.kayhut.fuse.unipop.controller.common.appender.SearchAppender;
import com.kayhut.fuse.unipop.controller.common.context.CompositeControllerContext;
import com.kayhut.fuse.unipop.controller.common.context.ElementControllerContext;
import com.kayhut.fuse.unipop.controller.promise.GlobalConstants;
import com.kayhut.fuse.unipop.controller.promise.context.PromiseVertexControllerContext;
import com.kayhut.fuse.unipop.controller.search.SearchBuilder;

/**
 * Created by Elad on 4/26/2017.
 */
public class PromiseEdgeAggregationAppender implements SearchAppender<ElementControllerContext> {
    @Override
    public boolean append(SearchBuilder searchBuilder, ElementControllerContext context) {
        searchBuilder.getAggregationBuilder().seekRoot()
                     .terms(GlobalConstants.EdgeSchema.SOURCE)
                        .field(GlobalConstants.EdgeSchema.SOURCE_ID)
                        .size(1000)
                        .shardSize(1000)
                        .executionHint("map")
                     .terms(GlobalConstants.EdgeSchema.DEST)
                        .field(GlobalConstants.EdgeSchema.DEST_ID)
                        .size(1000)
                        .shardSize(1000)
                        .executionHint("map");

        return true;
    }
}
