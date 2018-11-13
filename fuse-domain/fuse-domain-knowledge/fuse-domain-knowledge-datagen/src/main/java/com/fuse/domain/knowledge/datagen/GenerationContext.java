package com.fuse.domain.knowledge.datagen;

/*-
 * #%L
 * fuse-domain-knowledge-datagen
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
 * Created by Roman on 6/22/2018.
 */
public class GenerationContext {
    //region Constructors
    public GenerationContext(
            ElasticConfiguration elasticConfiguration,
            ContextGenerationConfiguration contextGenerationConfiguration,
            ContextStatistics contextStatistics) {
        this.elasticConfiguration = elasticConfiguration;
        this.contextGenerationConfiguration = contextGenerationConfiguration;
        this.contextStatistics = contextStatistics;
    }
    //endregion

    //region Properties
    public ElasticConfiguration getElasticConfiguration() {
        return elasticConfiguration;
    }

    public void setElasticConfiguration(ElasticConfiguration elasticConfiguration) {
        this.elasticConfiguration = elasticConfiguration;
    }

    public ContextGenerationConfiguration getContextGenerationConfiguration() {
        return contextGenerationConfiguration;
    }

    public void setContextGenerationConfiguration(ContextGenerationConfiguration contextGenerationConfiguration) {
        this.contextGenerationConfiguration = contextGenerationConfiguration;
    }

    public ContextStatistics getContextStatistics() {
        return contextStatistics;
    }

    public void setContextStatistics(ContextStatistics contextStatistics) {
        this.contextStatistics = contextStatistics;
    }
    //endregion

    //region Fields
    private ElasticConfiguration elasticConfiguration;
    private ContextGenerationConfiguration contextGenerationConfiguration;
    private ContextStatistics contextStatistics;
    //endregion
}
