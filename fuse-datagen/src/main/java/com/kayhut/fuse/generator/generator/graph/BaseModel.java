package com.kayhut.fuse.generator.generator.graph;

/**
 * Created by benishue on 15-May-17.
 */
public abstract class BaseModel {

    //region Ctrs
    public BaseModel() {
    }

    public BaseModel(int numOfNodes) {
        this.numOfNodes = numOfNodes;
    }

    public BaseModel(String modelName, int numOfNodes) {
        this.numOfNodes = numOfNodes;
        this.modelName = modelName;
    }

    //endregion

    //region Getters & Setters

    public int getNumOfNodes() {
        return numOfNodes;
    }

    public void setNumOfNodes(int numOfNodes) {
        this.numOfNodes = numOfNodes;
    }

    public String getModelName() {
        return modelName;
    }

    public void setModelName(String modelName) {
        this.modelName = modelName;
    }

    //endregion

    //region Fields
    private int numOfNodes;
    private String modelName;
    //endregion
}
