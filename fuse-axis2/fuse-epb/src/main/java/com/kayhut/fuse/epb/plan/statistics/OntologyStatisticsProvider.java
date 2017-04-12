package com.kayhut.fuse.epb.plan.statistics;

import com.kayhut.fuse.unipop.schemaProviders.GraphElementSchemaProvider;
import com.kayhut.fuse.unipop.schemaProviders.GraphVertexSchema;

import java.util.Optional;

/**
 * Created by moti on 4/12/2017.
 */
public class OntologyStatisticsProvider implements StatisticsProvider<StatisticableOntologyItemInfo> {
    private GraphElementSchemaProvider graphElementSchemaProvider;
    private StatisticsProvider<RawGraphStatisticableItemInfo> rawGraphStatisticsProvider;


    @Override
    public CardinalityStatistics getCardinalityStatistics(StatisticableOntologyItemInfo item) {
        if(item instanceof StatisticableOntologyEntityInfo){
            StatisticableOntologyEntityInfo statisticableEntityInfo = (StatisticableOntologyEntityInfo) item;
            Optional<GraphVertexSchema> vertexSchema = graphElementSchemaProvider.getVertexSchema(statisticableEntityInfo.getEntity().geteTag());
            if(vertexSchema.isPresent()){
                GraphVertexSchema graphVertexSchema = vertexSchema.get();

            }
        }


        return null;
    }

    @Override
    public <T extends Comparable<T>> HistogramStatistics<T> getHistogramStatistics(StatisticableOntologyItemInfo item) {
        return null;
    }
}
