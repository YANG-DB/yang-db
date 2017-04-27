package com.kayhut.fuse.epb.plan.statistics;

import com.kayhut.fuse.model.execution.plan.Direction;
import com.kayhut.fuse.model.ontology.Ontology;
import com.kayhut.fuse.model.ontology.OntologyUtil;
import com.kayhut.fuse.model.query.EBase;
import com.kayhut.fuse.model.query.Rel;
import com.kayhut.fuse.model.query.entity.EConcrete;
import com.kayhut.fuse.model.query.entity.EEntityBase;
import com.kayhut.fuse.model.query.entity.ETyped;
import com.kayhut.fuse.model.query.entity.EUntyped;
import com.kayhut.fuse.model.query.properties.EProp;
import com.kayhut.fuse.model.query.properties.RelProp;
import com.kayhut.fuse.unipop.schemaProviders.GraphElementSchemaProvider;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * Created by liorp on 4/26/2017.
 */
public class EBaseStatisticsProvider implements StatisticsProvider {
    private GraphElementSchemaProvider graphElementSchemaProvider;
    private Ontology ontology;
    private GraphStatisticsProvider graphStatisticsProvider;

    public EBaseStatisticsProvider(GraphElementSchemaProvider graphElementSchemaProvider, Ontology ontology, GraphStatisticsProvider graphStatisticsProvider) {
        this.graphElementSchemaProvider = graphElementSchemaProvider;
        this.ontology = ontology;
        this.graphStatisticsProvider = graphStatisticsProvider;
    }

    @Override
    public <T extends Comparable<T>> Statistics.HistogramStatistics<T> getNodeStatistics(EEntityBase entity) {
        if (entity instanceof EConcrete) {
            List<Statistics.BucketInfo<String>> bucketInfos = Collections.singletonList(new Statistics.BucketInfo<String>(1L, 1L, ((EConcrete) entity).geteID(), ((EConcrete) entity).geteID()));
            return (Statistics.HistogramStatistics<T>) new Statistics.HistogramStatistics<>(bucketInfos);
        }
        List<String> vertexTypes = null;
        if (entity instanceof EUntyped) {
            EUntyped eUntyped = (EUntyped) entity;
            if (eUntyped.getvTypes().size() > 0) {
                vertexTypes = eUntyped.getvTypes().stream().map(v -> OntologyUtil.getEntityTypeNameById(ontology, v)).collect(Collectors.toList());
            } else {
                vertexTypes = StreamSupport.stream(graphElementSchemaProvider.getVertexTypes().spliterator(), false).collect(Collectors.toList());
                if (eUntyped.getNvTypes().size() > 0) {
                    vertexTypes.removeAll(eUntyped.getNvTypes().stream().map(v -> OntologyUtil.getEntityTypeNameById(ontology, v)).collect(Collectors.toList()));
                }
            }
        } else if (entity instanceof ETyped) {
            vertexTypes = Collections.singletonList(OntologyUtil.getEntityTypeNameById(ontology, ((ETyped) entity).geteType()));
        }
        Statistics.HistogramStatistics<String> entityStats = getVertexStatistics(vertexTypes.get(0));
        for (int i = 1; i < vertexTypes.size(); i++) {
            entityStats = (Statistics.HistogramStatistics<String>) entityStats.merge(getVertexStatistics(vertexTypes.get(i)));
        }

        return (Statistics.HistogramStatistics<T>) entityStats;

    }

    @Override
    public <T extends Comparable<T>> Statistics.HistogramStatistics<T> getNodeFilterStatistics(EEntityBase item, EProp entityFilter) {

        return null;
    }

    @Override
    public <T extends Comparable<T>> Statistics.HistogramStatistics<T> getEdgeStatistics(Rel item) {
        return null;
    }

    @Override
    public <T extends Comparable<T>> Statistics.HistogramStatistics<T> getEdgeFilterStatistics(Rel item, RelProp entityFilter) {
        return null;
    }

    @Override
    public <T extends Comparable<T>> Statistics.HistogramStatistics<T> getRedundantEdgeStatistics(Rel rel, EBase entity, EProp entityFilter, Direction direction) {
        return null;
    }

    @Override
    public <T extends Comparable<T>> Statistics.HistogramStatistics<T> getRedundantNodeStatistics(Rel rel, EBase entity, EProp entityFilter, Direction direction) {
        return null;
    }

    @Override
    public long getGlobalSelectivity(Rel rel, EBase entity, Direction direction) {
        return 0;
    }

    private Statistics.HistogramStatistics<String> getVertexStatistics(String vertexType) {
        return graphStatisticsProvider.getVertexCardinality(graphElementSchemaProvider.getVertexSchema(vertexType).get());
    }
}
