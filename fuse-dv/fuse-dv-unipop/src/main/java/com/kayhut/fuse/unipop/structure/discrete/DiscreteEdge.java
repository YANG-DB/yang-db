package com.kayhut.fuse.unipop.structure.discrete;

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

import com.kayhut.fuse.unipop.controller.utils.map.MapBuilder;
import org.apache.tinkerpop.gremlin.structure.T;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.unipop.structure.UniEdge;
import org.unipop.structure.UniGraph;

import java.util.Collections;
import java.util.Map;

/**
 * Created by roman.margolis on 14/09/2017.
 */
public class DiscreteEdge extends UniEdge {
    //region Constructors
    public DiscreteEdge(Object id, String label, Vertex outV, Vertex inV, Vertex otherVertex, UniGraph graph, Map<String, Object> properties) {
        super(properties, outV, inV, otherVertex, graph);
        this.id = id.toString();
        this.label = label;
    }
    //endregion
}
