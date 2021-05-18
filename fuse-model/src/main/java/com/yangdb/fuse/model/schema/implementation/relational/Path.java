package com.yangdb.fuse.model.schema.implementation.relational;

import java.util.List;

/***
 * A single relational edge can be represented in several distinct Relational tables with redundant
 * columns. The sequence of tables in a path represents an edge traversal.
 *
 *
 */
public class Path {

    /**
     * There may be several route implementations to traverse the same path, depending on the source
     * vertex. Also, the route may contain several table join that at the end will represent just
     * one edge. The traversal hop models this feature.
     */
    private List<TraversalHop> traversalHops;

    /***
     * Default constructor.
     */
    public Path() {}

    /***
     * Generates a path from a list of traversal hops.
     * @param traversalHops hops that compose the path.
     */
    public Path(final List<TraversalHop> traversalHops) {
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
