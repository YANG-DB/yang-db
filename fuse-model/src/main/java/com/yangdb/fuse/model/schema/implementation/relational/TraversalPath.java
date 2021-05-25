package com.yangdb.fuse.model.schema.implementation.relational;

/*-
 * #%L
 * fuse-model
 * %%
 * Copyright (C) 2016 - 2021 The YangDb Graph Database Project
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

import java.util.List;

/***
 * A single relational edge can be represented in several distinct Relational tables with redundant
 * columns. The sequence of tables in a path represents an edge traversal.
 *
 *
 */
public class TraversalPath {

    /**
     * There may be several route implementations to traverse the same path, depending on the source
     * vertex. Also, the route may contain several table join that at the end will represent just
     * one edge. The traversal hop models this feature.
     */
    private List<TraversalHop> traversalHops;

    /***
     * Default constructor.
     */
    public TraversalPath() {}

    /***
     * Generates Traversal Paths.
     * @param traversalHops hops used in the traversal
     */
    public TraversalPath(final List<TraversalHop> traversalHops) {
        this.traversalHops = traversalHops;
    }

    /**
     * @return the traversalHops
     */
    public List<TraversalHop> getTraversalHops() {
        return traversalHops;
    }

    /**
     * @param traversalHops the traversalHops to set
     */
    public void setTraversalHops(final List<TraversalHop> traversalHops) {
        this.traversalHops = traversalHops;
    }
}
