package org.unipop.virtual;

/*-
 * #%L
 * VirtualSourceProvider.java - unipop-core - kayhut - 2,016
 * org.codehaus.mojo-license-maven-plugin-1.16
 * $Id$
 * $HeadURL$
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

import org.apache.tinkerpop.gremlin.structure.T;
import org.json.JSONObject;
import org.unipop.query.controller.SourceProvider;
import org.unipop.query.controller.UniQueryController;
import org.unipop.schema.element.ElementSchema;
import org.unipop.structure.UniGraph;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

import static org.unipop.util.ConversionUtils.getList;

/**
 * Created by sbarzilay on 9/6/16.
 */
public class VirtualSourceProvider implements SourceProvider {
    private UniGraph graph;

    @Override
    public Set<UniQueryController> init(UniGraph graph, JSONObject configuration) throws Exception {
        this.graph = graph;
        Set<ElementSchema> schemas = getList(configuration, "vertices").stream().map(this::createVertexSchema).collect(Collectors.toSet());
        return Collections.singleton(new VirtualController(graph, schemas));
    }

    private VirtualVertexSchema createVertexSchema(JSONObject json) {
        json.accumulate("id", "@" + T.id.getAccessor().toString());
        return new VirtualVertexSchema(json, graph);
    }

    @Override
    public void close() {

    }
}
