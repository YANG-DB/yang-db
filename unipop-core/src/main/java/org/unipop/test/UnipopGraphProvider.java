package org.unipop.test;

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
 * UnipopGraphProvider.java - unipop-core - yangdb - 2,016
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

import org.apache.commons.configuration.Configuration;
import org.apache.tinkerpop.gremlin.AbstractGraphProvider;
import org.apache.tinkerpop.gremlin.LoadGraphWith;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.unipop.structure.*;

import java.lang.annotation.*;
import java.util.*;

public abstract class UnipopGraphProvider extends AbstractGraphProvider {

    private static final Set<Class> IMPLEMENTATION = new HashSet<Class>() {{
        add(UniGraph.class);
        add(UniElement.class);
        add(UniVertex.class);
        add(UniEdge.class);
        add(UniProperty.class);
        add(UniVertexProperty.class);
    }};

    @Override
    public Map<String, Object> getBaseConfiguration(String graphName, Class<?> test, String testMethodName, LoadGraphWith.GraphData loadGraphWith) {
        return new HashMap<String, Object>() {{
            put(Graph.GRAPH, UniGraph.class.getName());
            put("graphName",graphName.toLowerCase());

        }};
    }

    @Override
    public void clear(final Graph g, final Configuration configuration) throws Exception {
        if (g != null) g.close();
    }

    @Override
    public Set<Class> getImplementations() {
        return IMPLEMENTATION;
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    @Inherited
    public @interface OptIn {

        String UnipopStructureSuite = "org.unipop.test.UnipopStructureSuite";
        String UnipopProcessSuite = "org.unipop.test.UnipopProcessSuite";

        String value();
    }
}
