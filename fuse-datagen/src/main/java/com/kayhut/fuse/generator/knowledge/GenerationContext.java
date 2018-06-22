package com.kayhut.fuse.generator.knowledge;

/**
 * Created by Roman on 6/22/2018.
 */
public class GenerationContext {
    //region Constructors
    public GenerationContext(ContextGenerationConfiguration configuration, ContextStatistics contextStatistics) {
        this.configuration = configuration;
        this.contextStatistics = contextStatistics;
    }
    //endregion

    //region Properties

    public ContextGenerationConfiguration getConfiguration() {
        return configuration;
    }

    public ContextStatistics getContextStatistics() {
        return contextStatistics;
    }
    //endregion

    //region Fields
    private ContextGenerationConfiguration configuration;
    private ContextStatistics contextStatistics;
    //endregion
}
