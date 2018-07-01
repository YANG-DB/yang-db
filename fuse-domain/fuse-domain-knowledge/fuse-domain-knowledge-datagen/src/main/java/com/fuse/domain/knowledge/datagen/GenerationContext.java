package com.fuse.domain.knowledge.datagen;

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
