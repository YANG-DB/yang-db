package org.unipop.query.mutation;

/*-
 * #%L
 * unipop-core
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

/*-
 *
 * AddEdgeQuery.java - unipop-core - yangdb - 2,016
 * org.codehaus.mojo-license-maven-plugin-1.16
 * $Id$
 * $HeadURL$
 * %%
 * Copyright (C) 2016 - 2019 yangdb   ------ www.yangdb.org ------
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
 *
 */

import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.unipop.query.StepDescriptor;
import org.unipop.query.UniQuery;
import org.unipop.query.controller.UniQueryController;
import org.unipop.structure.UniVertex;

import java.util.Map;

public class AddEdgeQuery extends UniQuery{
    private final Vertex outVertex;
    private final Vertex inVertex;
    private final Map<String, Object> properties;
    private final StepDescriptor stepDescriptor;

    public AddEdgeQuery(Vertex outVertex, Vertex inVertex, Map<String, Object> properties, StepDescriptor stepDescriptor) {
        super(stepDescriptor);
        this.outVertex = outVertex;
        this.inVertex = inVertex;
        this.properties = properties;
        this.stepDescriptor = stepDescriptor;
    }

    public Vertex getInVertex() {
        return inVertex;
    }

    public Vertex getOutVertex() {
        return outVertex;
    }

    @Override
    public StepDescriptor getStepDescriptor() {
        return stepDescriptor;
    }

    public Map<String, Object> getProperties() {
        return properties;
    }

    public interface AddEdgeController extends UniQueryController {
        Edge addEdge(AddEdgeQuery uniQuery);
    }

    @Override
    public String toString() {
        return "AddEdgeQuery{" +
                "outVertex=" + outVertex +
                ", inVertex=" + inVertex +
                ", properties=" + properties +
                ", stepDescriptor=" + stepDescriptor +
                '}';
    }
}
