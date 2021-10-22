package com.yangdb.fuse.executor;

/*-
 * #%L
 * fuse-dv-core
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

import com.yangdb.fuse.dispatcher.resource.QueryResource;
import com.yangdb.fuse.executor.cursor.TraversalCursorContext;

import java.util.List;

public class CompositeTraversalCursorContext extends TraversalCursorContext {

    private final List<QueryResource> inner;

    public CompositeTraversalCursorContext(TraversalCursorContext outer, List<QueryResource> inner) {
        super(outer.getClient(), outer.getSchemaProvider(), outer.getOntologyProvider(), outer.getOntology(), outer.getQueryResource(), outer.getCursorRequest(), outer.getRuntimeProvision(), outer.getTraversal());
        this.inner = inner;
    }

    @Override
    public CompositeTraversalCursorContext clone() {
        return new CompositeTraversalCursorContext(
                new TraversalCursorContext(getClient(), getSchemaProvider(), getOntologyProvider(), getOntology(), getQueryResource(), getCursorRequest(), getRuntimeProvision(), getTraversal()), getInner());
    }

    public List<QueryResource> getInner() {
        return inner;
    }
}
