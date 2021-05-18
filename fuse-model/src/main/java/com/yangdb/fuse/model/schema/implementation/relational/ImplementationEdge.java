package com.yangdb.fuse.model.schema.implementation.relational;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.ArrayList;
import java.util.List;

/***
 * Implementation Edge. A description of the relational implementation of an edge declared in the abstraction layer.
 *
 *
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ImplementationEdge {

    /***
     * Node types that this edge may be referenced on the abstraction level.
     */
    private List<String> types = new ArrayList<>();

    /***
     * The path is a list of all the tables, that at the end would model analogous property graph's edges.
     */
    private List<TraversalPath> paths = new ArrayList<>();

    /***
     * Generates an Implementation edge.
     * @param types types used to refer to that edge on the abstraction level
     * @param paths traversal paths that compose that edge
     */
    public ImplementationEdge(final List<String> types, final List<TraversalPath> paths) {
        this.types = types;
        this.paths = paths;
    }


    /**
     * Constructs an empty Implementation Edge.
     */
    public ImplementationEdge() {

        paths = new ArrayList<>();
        types = new ArrayList<>();

        TraversalPath traversalPath = new TraversalPath();
        TraversalHop traversalHop = new TraversalHop();

        List<TraversalHop> traversalList = new ArrayList<>();
        traversalList.add(traversalHop);

        traversalPath.setTraversalHops(traversalList);
        traversalPath.setTraversalHops(new ArrayList<>());

        paths.add(traversalPath);
    }
    @JsonIgnore
    public boolean anyMatch(String name) {
        return types.stream().anyMatch(t->t.equalsIgnoreCase(name));
    }
    /**
     * @return the types
     */
    public List<String> getTypes() {
        return types;
    }

    /**
     * @param types the types to set
     */
    public void setTypes(final List<String> types) {
        this.types = types;
    }

    /**
     * @return the paths
     */
    public List<TraversalPath> getPaths() {
        return paths;
    }

    /**
     * @param paths the paths to set
     */
    public void setPaths(final List<TraversalPath> paths) {
        this.paths = paths;
    }

    @Override
    @SuppressWarnings("checkstyle:magicnumber")
    public int hashCode() {
        int result = types != null ? types.hashCode() : 0;
        result = 31 * result + (paths != null ? paths.hashCode() : 0);
        return result;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final ImplementationEdge that = (ImplementationEdge) o;

        List<String> thatType = that.getTypes();
        List<TraversalPath> thatPaths = that.getPaths();

        if (!types.containsAll(thatType) || !thatType.containsAll(types)) {
            return false;
        }
        if (!paths.containsAll(thatPaths) || !thatPaths.containsAll(paths)) {
            return false;
        }

        return true;
    }
}
