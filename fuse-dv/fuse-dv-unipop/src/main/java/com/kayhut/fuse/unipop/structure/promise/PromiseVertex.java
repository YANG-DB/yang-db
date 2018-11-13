package com.kayhut.fuse.unipop.structure.promise;

/*-
 * #%L
 * fuse-dv-unipop
 * %%
 * Copyright (C) 2016 - 2018 kayhut
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

import com.google.common.collect.ImmutableMap;
import com.kayhut.fuse.unipop.controller.utils.map.MapBuilder;
import com.kayhut.fuse.unipop.promise.Constraint;
import com.kayhut.fuse.unipop.promise.Promise;
import org.apache.tinkerpop.gremlin.structure.T;
import org.unipop.structure.UniGraph;
import org.unipop.structure.UniVertex;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Created by lior.perry on 19/03/2017.
 */
public class PromiseVertex extends UniVertex {
    //region Constructor
    public PromiseVertex(Promise promise, Optional<Constraint> constraint, UniGraph graph) {
        this(promise, constraint, graph, Collections.emptyMap());
    }

    public PromiseVertex(Promise promise, Optional<Constraint> constraint, UniGraph graph, Map<String, Object> properties) {
        super(new MapBuilder<>(properties).put(T.id.getAccessor(), promise.getId()).get(), graph);

        this.promise = promise;
        this.constraint = constraint;
    }
    //endregion

    //region Override Methods
    @Override
    protected String getDefaultLabel() {
        return "promise";
    }
    //endregion

    //region properties
    public Promise getPromise() {
        return this.promise;
    }

    public Optional<Constraint> getConstraint() {

        return this.constraint;
    }
    //endregion

    //region Fields
    private Promise promise;
    private Optional<Constraint> constraint;
    //endregion
}
