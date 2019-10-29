package com.yangdb.fuse.unipop.controller.common;

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

import javaslang.collection.Stream;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Element;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.unipop.process.Profiler;
import org.unipop.query.search.SearchQuery;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;

/**
 * Created by lior.perry on 19/03/2017.
 *
 * g.V() OR g.E() ==> edge controller
 */
public class ElementController implements SearchQuery.SearchController {
    //region Constructors
    public ElementController(
            SearchQuery.SearchController vertexController,
            SearchQuery.SearchController edgeController) {
        this.innerControllers = new HashMap<>();
        this.innerControllers.put(Vertex.class, vertexController);
        this.innerControllers.put(Edge.class, edgeController);
    }
    //endregion

    //region SearchQuery.SearchController Implementation
    @Override
    public <E extends Element> Iterator<E> search(SearchQuery<E> searchQuery) {
        Iterator<E> result = this.innerControllers.get(searchQuery.getReturnType()).search(searchQuery);
        return result;
    }

    @Override
    public Profiler getProfiler() {
        return this.innerControllers.values().iterator().next().getProfiler();
    }

    @Override
    public void setProfiler(Profiler profiler) {
        Stream.ofAll(this.innerControllers.values())
                .filter(Objects::nonNull)
                .forEach(controller -> controller.setProfiler(profiler));
    }
    //endregion

    //region Fields
    private Map<Class, SearchQuery.SearchController> innerControllers;
    //endregion
}
