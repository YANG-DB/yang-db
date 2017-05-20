package com.kayhut.fuse.generator.generator.model;

/**
 * Created by benishue on 15-May-17.
 */
public class ScaleFreeModel extends BaseModel {

    //region Ctrs

    public ScaleFreeModel(String modelName, int edgesPerNode, boolean exactlyEdgesPerNode, int numOfNodes) {
        super(modelName, numOfNodes);
        this.edgesPerNode = edgesPerNode;
        this.exactlyEdgesPerNode = exactlyEdgesPerNode;
    }

    public ScaleFreeModel(int edgesPerNode, int numOfNodes) {
        super("Barabàsi-Albert", numOfNodes);
        this.exactlyEdgesPerNode = false;
        this.edgesPerNode = edgesPerNode;
    }

    public ScaleFreeModel(String modelName, int edgesPerNode, int numOfNodes) {
        super(modelName, numOfNodes);
        this.exactlyEdgesPerNode = false;
        this.edgesPerNode = edgesPerNode;
    }
    //endregion

    //region Getters & Setters
    public int getEdgesPerNode() {
        return edgesPerNode;
    }

    public void setEdgesPerNode(int edgesPerNode) {
        this.edgesPerNode = edgesPerNode > 0 ? edgesPerNode : 1;
    }

    public boolean isExactlyEdgesPerNode() {
        return exactlyEdgesPerNode;
    }

    public void setExactlyEdgesPerNode(boolean exactlyEdgesPerNode) {
        this.exactlyEdgesPerNode = exactlyEdgesPerNode;
    }
    //endregion

    //region Fields
    private int edgesPerNode;
    private boolean exactlyEdgesPerNode = false;
    //endregion

}
