package com.yangdb.fuse.unipop.controller.promise.appender;

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

import com.yangdb.fuse.unipop.controller.common.appender.SearchAppender;
import com.yangdb.fuse.unipop.controller.common.context.VertexControllerContext;
import com.yangdb.fuse.unipop.controller.search.SearchBuilder;
import com.yangdb.fuse.unipop.promise.IdPromise;
import com.yangdb.fuse.unipop.schemaProviders.indexPartitions.IndexPartitions;
import com.yangdb.fuse.unipop.structure.promise.PromiseVertex;
import javaslang.collection.Stream;

import java.util.Collection;

/**
 * Created by Roman on 12/06/2017.
 */
public class FilterIndexSearchAppender implements SearchAppender<VertexControllerContext> {
    //region SearchAppender Implementation
    @Override
    public boolean append(SearchBuilder searchBuilder, VertexControllerContext context) {
        Collection<String> indices = Stream.ofAll(context.getBulkVertices())
                .map(vertex -> (PromiseVertex)vertex)
                .map(PromiseVertex::getPromise)
                .map(promise -> (IdPromise)promise)
                .map(promise -> promise.getLabel().get())
                .flatMap(label -> context.getSchemaProvider().getVertexSchemas(label))
                .map(schema -> schema.getIndexPartitions().get())
                .flatMap(IndexPartitions::getPartitions)
                .flatMap(IndexPartitions.Partition::getIndices)
                .distinct()
                .toJavaList();

        searchBuilder.getIndices().addAll(indices);
        return true;
    }
    //endregion
}
