package com.kayhut.fuse.generator.model.entity;

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
public abstract class EntityBase {

    //region Ctrs
    public EntityBase() {
    }

    public EntityBase(String id) {
        this.id = id;
    }
    //endregion

    //region Getters & Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
    //endregion

    //region Abstract Methods
    public abstract String[] getRecord();
    //endregion

    //region Fields
    private String id;
    //endregion
}
