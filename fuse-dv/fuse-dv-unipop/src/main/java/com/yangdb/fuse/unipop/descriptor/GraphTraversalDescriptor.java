package com.yangdb.fuse.unipop.descriptor;

/*-
 *
 * fuse-dv-unipop
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

import com.yangdb.fuse.model.descriptors.Descriptor;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.DefaultGraphTraversal;

/**
 * Created by moti on 6/21/2017.
 */
public class GraphTraversalDescriptor implements Descriptor<DefaultGraphTraversal> {
    @Override
    public String describe(DefaultGraphTraversal query) {
        StringBuilder sb = new StringBuilder();
        for(Object step : query.asAdmin().getSteps()) {
            /*if (TraversalParent.class.isAssignableFrom(step.getClass())) {
                TraversalParent traversalParent = (TraversalParent)step;
                traversalParent.getGlobalChildren()
            } else {*/
                sb.append(step.toString());
            //}
            sb.append("\n");
        }
        return sb.toString();
    }
}
