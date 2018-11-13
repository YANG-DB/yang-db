package com.kayhut.fuse.generator.data.generation.scale.free;

/*-
 * #%L
 * fuse-domain-gragons-datagen
 * %%
 * Copyright (C) 2016 - 2018 kayhut
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
