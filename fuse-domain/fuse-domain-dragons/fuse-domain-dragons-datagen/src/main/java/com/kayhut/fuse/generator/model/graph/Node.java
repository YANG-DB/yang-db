package com.kayhut.fuse.generator.model.graph;

/**
 * Created by benishue on 15-May-17.
 */
public class Node {

    //region Ctrs
    public Node(String nodeId) {
        this.nodeId = nodeId;
    }
    //endregion

    //region Getters & Setters
    public String getNodeId() {
        return nodeId;
    }

    public void setNodeId(String nodeId) {
        this.nodeId = nodeId;
    }
    //endregion

    //region Fields
    private String nodeId;
    //endregion
}
