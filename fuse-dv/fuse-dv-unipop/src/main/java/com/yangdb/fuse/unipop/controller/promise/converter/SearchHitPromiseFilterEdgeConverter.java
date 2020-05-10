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
import com.yangdb.fuse.unipop.structure.promise.PromiseFilterEdge;
import com.yangdb.fuse.unipop.structure.promise.PromiseVertex;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.elasticsearch.search.SearchHit;
import org.unipop.process.Profiler;
import org.unipop.structure.UniGraph;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;

/**
 * Created by Elad on 4/30/2017.
 */
public class SearchHitPromiseFilterEdgeConverter implements ElementConverter<SearchHit, Edge> {

    //region Constructor
    public SearchHitPromiseFilterEdgeConverter(UniGraph graph) {
        this.graph = graph;
    }
    //endregion

    @Override
    public Iterable<Edge> convert(SearchHit hit) {
        Map<String, Object> propertiesMap = hit.getSourceAsMap();
        PromiseVertex v = new PromiseVertex(
                Promise.as(hit.getId(), (String) hit.getSourceAsMap().get("type")),
                Optional.empty(),
                graph,
                propertiesMap);

        return Arrays.asList(new PromiseFilterEdge(v, graph));
    }

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
    private Profiler profiler = Profiler.Noop.instance ;
    private UniGraph graph;
    //endregion
}
