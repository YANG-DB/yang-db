package com.yangdb.fuse.generator.data.generation.scale.free;

/*-
 * #%L
 * fuse-domain-gragons-datagen
 * %%
 * Copyright (C) 2016 - 2018 yangdb   ------ www.yangdb.org ------
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
