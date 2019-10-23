package com.yangdb.fuse.generator.model.graph;

/*-
 * #%L
 * fuse-domain-dragons-datagen
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



import java.util.ArrayList;
import java.util.List;

/**
 * Created by benishue on 15-May-17.
 */
public class NodesEdges {

    //region Ctrs
    public NodesEdges() {
        this.nodes = new ArrayList<>();
        this.edges = new ArrayList<>();
    }

    public NodesEdges(List<Node> node, List<Edge> edges) {
        this.nodes = node;
        this.edges = edges;
    }
    //endregion

    //region Getters & Setters
    public List<Node> getNodes() {
        return nodes;
    }

    public void setNodes(List<Node> nodes) {
        this.nodes = nodes;
    }

    public List<Edge> getEdges() {
        return edges;
    }

    public void setEdges(List<Edge> edges) {
        this.edges = edges;
    }
    //endregion

    //region Public Methods
    public void addNode(Node node){
        this.nodes.add(node);
    }

    public void addEdge(Edge edge){
        this.edges.add(edge);
    }
    //endregion

    //region Fields
    private List<Node> nodes;
    private List<Edge> edges;
    //endregion
}
