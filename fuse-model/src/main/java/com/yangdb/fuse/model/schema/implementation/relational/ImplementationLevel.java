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


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.yangdb.fuse.model.schema.implementation.graphmetadata.GraphMetadata;

import java.util.List;
import java.util.Optional;

/**
 * A relational implementation description of the graph declared in the AbstractionLevel.
 */
public class ImplementationLevel {

    /***
     * Set of restrictions imposed to the graph analysis.
     */
    private GraphMetadata graphMetadata = new GraphMetadata();

    /***
     * Set of rules that will interpret data has a node of a given type.
     */
    private List<ImplementationNode> implementationNodes;

    /***
     * Set of rules that will interpret data has an edge of a given type.
     */
    private List<ImplementationEdge> implementationEdges;

    /***
     * Default constructor.
     */
    public ImplementationLevel() {
    }

    /***
     * Generates an implementation level .
     * @param graphMetadata the implementation level graph metadata
     * @param nodes the implementation nodes
     * @param edges the implementation edges
     */
    public ImplementationLevel(final GraphMetadata graphMetadata, final List<ImplementationNode> nodes,
                               final List<ImplementationEdge> edges) {
        this.graphMetadata = graphMetadata;
        implementationNodes = nodes;
        implementationEdges = edges;
    }


    /**
     * @return the graphMetadata
     */
    public GraphMetadata getGraphMetadata() {
        return graphMetadata;
    }

    /**
     * @param graphMetadata the graphMetadata to set
     */
    public void setGraphMetadata(final GraphMetadata graphMetadata) {
        this.graphMetadata = graphMetadata;
    }

    @JsonIgnore
    public Optional<ImplementationNode> getImplementationNode(String name) {
        return implementationNodes.stream().filter(n->n.getTableName().equalsIgnoreCase(name)).findAny();
    }
    /**
     * @return the nodes
     */
    public List<ImplementationNode> getImplementationNodes() {
        return implementationNodes;
    }

    /**
     * @param nodes the nodes to set
     */
    public void setImplementationNodes(final List<ImplementationNode> nodes) {
        implementationNodes = nodes;
    }

    @JsonIgnore
    public Optional<ImplementationEdge> getImplementationEdge(String name) {
        return implementationEdges.stream().filter(n->n.anyMatch(name)).findAny();
    }

    /**
     * @return the edges
     */
    public List<ImplementationEdge> getImplementationEdges() {
        return implementationEdges;
    }

    /**
     * @param edges the edges to set
     */
    public void setImplementationEdges(final List<ImplementationEdge> edges) {
        implementationEdges = edges;
    }

    @Override
    @SuppressWarnings("checkstyle:magicnumber")
    public int hashCode() {
        int result = implementationNodes != null ? implementationNodes.hashCode() : 0;
        result = 31 * result + (implementationEdges != null ? implementationEdges.hashCode() : 0);
        result = 31 * result + (graphMetadata != null ? graphMetadata.hashCode() : 0);
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

        final ImplementationLevel that = (ImplementationLevel) o;

        List<ImplementationNode> thatNodes = that.getImplementationNodes();
        List<ImplementationEdge> thatEdges = that.getImplementationEdges();
        GraphMetadata thatMeta = that.getGraphMetadata();

        if (!implementationNodes.containsAll(thatNodes) || !thatNodes.containsAll(implementationNodes)) {
            return false;
        }
        if (!implementationEdges.containsAll(thatEdges) || !thatEdges.containsAll(implementationEdges)) {
            return false;
        }
        if (!graphMetadata.equals(thatMeta)) {
            return false;
        }

        return true;
    }


}
