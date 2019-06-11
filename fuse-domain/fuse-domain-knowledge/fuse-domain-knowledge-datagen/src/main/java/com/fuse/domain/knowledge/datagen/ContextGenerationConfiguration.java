package com.fuse.domain.knowledge.datagen;

/*-
 * #%L
 * fuse-domain-knowledge-datagen
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
 * Created by Roman on 6/22/2018.
 */
public class ContextGenerationConfiguration {
    //region Constructors
    public ContextGenerationConfiguration() {}

    public ContextGenerationConfiguration(
            String fromContext,
            String toContext,
            double scaleFactor,
            double entityOverlapFactor,
            double entityValueOverlapFactor) {

        this.fromContext = fromContext;
        this.toContext = toContext;

        this.scaleFactor = scaleFactor;
        this.entityOverlapFactor = entityOverlapFactor;
        this.entityValueOverlapFactor = entityValueOverlapFactor;
    }
    //endregion

    //region Properties
    public String getFromContext() {
        return fromContext;
    }

    public void setFromContext(String fromContext) {
        this.fromContext = fromContext;
    }

    public String getToContext() {
        return toContext;
    }

    public void setToContext(String toContext) {
        this.toContext = toContext;
    }

    public double getScaleFactor() {
        return scaleFactor;
    }

    public void setScaleFactor(double scaleFactor) {
        this.scaleFactor = scaleFactor;
    }

    public double getEntityOverlapFactor() {
        return entityOverlapFactor;
    }

    public void setEntityOverlapFactor(double entityOverlapFactor) {
        this.entityOverlapFactor = entityOverlapFactor;
    }

    public double getEntityValueOverlapFactor() {
        return entityValueOverlapFactor;
    }

    public void setEntityValueOverlapFactor(double entityValueOverlapFactor) {
        this.entityValueOverlapFactor = entityValueOverlapFactor;
    }
    //endregion

    //region Fields
    private String fromContext;
    private String toContext;

    private double scaleFactor;
    private double entityOverlapFactor;
    private double entityValueOverlapFactor;
    //endregion
}
