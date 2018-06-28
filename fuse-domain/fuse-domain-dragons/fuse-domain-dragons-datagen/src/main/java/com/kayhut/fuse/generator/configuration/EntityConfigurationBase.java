package com.kayhut.fuse.generator.configuration;


/**
 * Created by benishue on 20/05/2017.
 */
public abstract class EntityConfigurationBase {

    //region Ctrs

    public EntityConfigurationBase() {

    }

    public EntityConfigurationBase(int numberOfNodes,
                                   int edgesPerNode,
                                   String entitiesFilePath,
                                   String relationsFilePath) {
        this.numberOfNodes = numberOfNodes;
        this.edgesPerNode = edgesPerNode;
        this.entitiesFilePath = entitiesFilePath;
        this.relationsFilePath = relationsFilePath;
    }
    //endregion

    //region Getters
    public int getNumberOfNodes() {
        return numberOfNodes;
    }

    public int getEdgesPerNode() {
        return edgesPerNode;
    }

    public String getEntitiesFilePath() {
        return entitiesFilePath;
    }

    public String getRelationsFilePath() {
        return relationsFilePath;
    }

    //endregion

    //region Fields
    private int numberOfNodes;
    private int edgesPerNode;
    private String entitiesFilePath;
    private String relationsFilePath;
    //endregion

}



