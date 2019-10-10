package com.yangdb.fuse.generator.configuration;

/*-
 *
 * fuse-domain-gragons-datagen
 * %%
 * Copyright (C) 2016 - 2019 yangdb   ------ www.yangdb.org ------
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
 *
 */


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



