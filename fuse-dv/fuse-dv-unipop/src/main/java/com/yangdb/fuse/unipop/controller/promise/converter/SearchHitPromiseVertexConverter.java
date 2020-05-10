package com.yangdb.fuse.unipop.controller.promise.converter;

/*-
 * #%L
 * fuse-dv-unipop
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

import com.yangdb.fuse.unipop.controller.common.converter.ElementConverter;
import com.yangdb.fuse.unipop.promise.Promise;
import com.yangdb.fuse.unipop.structure.promise.PromiseVertex;
import org.apache.tinkerpop.gremlin.structure.Element;
import org.elasticsearch.search.SearchHit;
import org.unipop.process.Profiler;
import org.unipop.structure.UniGraph;

import java.util.Collections;
import java.util.Optional;

/**
 * Created by roman on 11/17/2015.
 */
public class SearchHitPromiseVertexConverter implements ElementConverter<SearchHit, Element> {
    //region Constructor
    public SearchHitPromiseVertexConverter(UniGraph graph) {
        this.graph = graph;
    }
    //endregion

    @Override
    public Iterable<Element> convert(SearchHit element) {
        return Collections.singletonList(
                new PromiseVertex(
                    Promise.as(element.getId(), (String) element.getSourceAsMap().get("type")),
                    Optional.empty(),
                    graph,
                    element.getSourceAsMap()));
    }
    //endregion

    //endregion
    @Override
    public Profiler getProfiler() {
        return this.profiler;
    }

    @Override
    public void setProfiler(Profiler profiler) {
        this.profiler = profiler;
    }

    //region Fields
    private Profiler profiler;
    private UniGraph graph;
    //endregion
}
