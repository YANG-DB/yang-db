package com.kayhut.fuse.epb.plan.statistics;

import com.kayhut.fuse.model.query.entity.EEntityBase;
import com.kayhut.fuse.unipop.schemaProviders.GraphElementSchemaProvider;
import com.kayhut.fuse.unipop.schemaProviders.GraphVertexSchema;

import java.util.Optional;

/**
 * Created by moti on 4/12/2017.
 */
public class QueryItemStatisticsProvider implements StatisticsProvider<StatisticableQueryItemInfo> {
    private GraphElementSchemaProvider graphElementSchemaProvider;
    private StatisticsProvider<RawGraphStatisticableItemInfo> rawGraphStatisticsProvider;

    @Override
    public CardinalityStatistics getCardinalityStatistics(StatisticableQueryItemInfo info) {
        return rawGraphStatisticsProvider.getCardinalityStatistics(createRawItemInfo(info));
    }

    private RawGraphStatisticableItemInfo createRawItemInfo(StatisticableQueryItemInfo info){
        if(info instanceof StatisticableOntologyElementInfo){
            StatisticableOntologyElementInfo statisticableOntologyElementInfo = (StatisticableOntologyElementInfo) info;
            if(statisticableOntologyElementInfo.geteBase() instanceof EEntityBase) {
                EEntityBase eEntityBase = (EEntityBase) statisticableOntologyElementInfo.geteBase();
                Optional<GraphVertexSchema> vertexSchema = graphElementSchemaProvider.getVertexSchema(eEntityBase.geteTag());
                if (vertexSchema.isPresent()) {
                    GraphVertexSchema graphVertexSchema = vertexSchema.get();
                    return new GraphVertexItemInfo(graphVertexSchema);
                }
            }
        }
        return null;
    }


    @Override
    public <T extends Comparable<T>> HistogramStatistics<T> getHistogramStatistics(StatisticableQueryItemInfo info) {
        return rawGraphStatisticsProvider.getHistogramStatistics(createRawItemInfo(info));
    }
}
