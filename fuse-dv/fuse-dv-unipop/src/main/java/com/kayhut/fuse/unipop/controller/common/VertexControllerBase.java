package com.kayhut.fuse.unipop.controller.common;

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

import com.kayhut.fuse.unipop.controller.utils.CollectionUtil;
import javaslang.collection.Stream;
import org.apache.tinkerpop.gremlin.process.traversal.step.util.HasContainer;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.T;
import org.unipop.process.Profiler;
import org.unipop.query.search.SearchVertexQuery;

import java.util.*;
import java.util.function.Predicate;

/**
 * Created by Roman on 15/05/2017.
 */
public abstract class VertexControllerBase implements SearchVertexQuery.SearchVertexController{
    //region Constructors
    public VertexControllerBase(Predicate<Iterable<String>> applicablePredicate) {
        this(applicablePredicate, Collections.emptySet());
    }

    public VertexControllerBase(Predicate<Iterable<String>> applicablePredicate, Iterable<String> supportedEdgeLabels) {
        this.applicablePredicate = applicablePredicate;
        this.supportedEdgeLabels = Stream.ofAll(supportedEdgeLabels).toJavaSet();
    }
    //endregion

    //region SearchVertexQuery.SearchVertexController Implementation
    @Override
    public Iterator<Edge> search(SearchVertexQuery searchVertexQuery) {
        if (searchVertexQuery.getVertices().size() == 0){
            throw new UnsupportedOperationException("SearchVertexQuery must receive a non-empty list of vertices getTo start with");
        }

        Iterable<String> requestedEdgeLabels = getRequestedEdgeLabels(searchVertexQuery.getPredicates().getPredicates());
        if (!this.applicablePredicate.test(requestedEdgeLabels)) {
            return Collections.emptyIterator();
        }

        return search(searchVertexQuery, getSupportedEdgeLabels(requestedEdgeLabels));
    }

    @Override
    public Profiler getProfiler() {
        return this.profiler;
    }

    @Override
    public void setProfiler(Profiler profiler) {
        this.profiler = profiler;
    }
    //endregion

    //region Protected Methods
    protected Iterable<String> getRequestedEdgeLabels(Iterable<HasContainer> hasContainers) {
        Optional<HasContainer> labelHasContainer =
                Stream.ofAll(hasContainers)
                    .filter(hasContainer -> hasContainer.getKey().equals(T.label.getAccessor()))
                    .toJavaOptional();

        if (!labelHasContainer.isPresent()) {
            return Collections.emptyList();
        }

        List<String> requestedEdgeLabels = CollectionUtil.listFromObjectValue(labelHasContainer.get().getValue());
        return requestedEdgeLabels;
    }

    protected Iterable<String> getSupportedEdgeLabels(Iterable<String> requestEdgeLabels) {
        return Stream.ofAll(requestEdgeLabels)
                .filter(label -> this.supportedEdgeLabels.contains(label))
                .toJavaSet();
    }

    protected abstract Iterator<Edge> search(SearchVertexQuery searchVertexQuery, Iterable<String> edgeLabels);

    //endregion

    //region Fields
    private Predicate<Iterable<String>> applicablePredicate;
    private Set<String> supportedEdgeLabels;

    protected Profiler profiler;
    //endregion
}
