package com.kayhut.fuse.generator.model.graph;

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
