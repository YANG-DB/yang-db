package com.kayhut.fuse.generator.knowledge;

import com.kayhut.fuse.generator.data.generation.graph.GraphGeneratorBase;
import com.kayhut.fuse.generator.knowledge.model.KnowledgeEntityBase;
import com.kayhut.fuse.generator.model.relation.RelationBase;
import javaslang.Tuple2;
import org.elasticsearch.client.Client;
import org.graphstream.graph.Graph;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class KnowledgeContextGenerator implements KnowledgeGraphGenerator<KnowledgeContextGenerator.GenerationContext> {
    public static class GenerationContext {
        //region Constructors
        public GenerationContext(
                String fromContext,
                String toContext,
                double scaleFactor,
                double entityOverlapFactor,
                double entityValueOverlapFactor,
                ContextStatistics contextStatistics) {
            this.fromContext = fromContext;
            this.toContext = toContext;

            this.scaleFactor = scaleFactor;
            this.entityOverlapFactor = entityOverlapFactor;
            this.entityValueOverlapFactor = entityValueOverlapFactor;

            this.contextStatistics = contextStatistics;
        }
        //endregion

        //region Properties
        public String getFromContext() {
            return fromContext;
        }

        public String getToContext() {
            return toContext;
        }

        public double getScaleFactor() {
            return scaleFactor;
        }

        public double getEntityOverlapFactor() {
            return entityOverlapFactor;
        }

        public double getEntityValueOverlapFactor() {
            return entityValueOverlapFactor;
        }

        public ContextStatistics getContextStatistics() {
            return contextStatistics;
        }
        //endregion

        //region Fields
        private String fromContext;
        private String toContext;

        private double scaleFactor;
        private double entityOverlapFactor;
        private double entityValueOverlapFactor;

        private ContextStatistics contextStatistics;
        //endregion
    }

    //region Constructors
    public KnowledgeContextGenerator(Client client) {

    }
    //endregion

    //region KnowledgeGraphGenerator Implementation
    @Override
    public Iterable<KnowledgeEntityBase> generate(GenerationContext generationContext) {
        return null;
    }
    //endregion

    //region Fields
    private Client client;
    //endregion
}
