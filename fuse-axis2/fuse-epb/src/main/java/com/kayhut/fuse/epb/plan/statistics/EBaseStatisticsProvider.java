package com.kayhut.fuse.epb.plan.statistics;

import com.kayhut.fuse.model.execution.plan.Direction;
import com.kayhut.fuse.model.ontology.Ontology;
import com.kayhut.fuse.model.ontology.OntologyUtil;
import com.kayhut.fuse.model.query.Constraint;
import com.kayhut.fuse.model.query.ConstraintOp;
import com.kayhut.fuse.model.query.EBase;
import com.kayhut.fuse.model.query.Rel;
import com.kayhut.fuse.model.query.entity.EConcrete;
import com.kayhut.fuse.model.query.entity.EEntityBase;
import com.kayhut.fuse.model.query.entity.ETyped;
import com.kayhut.fuse.model.query.entity.EUntyped;
import com.kayhut.fuse.model.query.properties.EProp;
import com.kayhut.fuse.model.query.properties.EPropGroup;
import com.kayhut.fuse.model.query.properties.RelPropGroup;
import com.kayhut.fuse.unipop.schemaProviders.GraphElementPropertySchema;
import com.kayhut.fuse.unipop.schemaProviders.GraphElementSchemaProvider;
import com.kayhut.fuse.unipop.schemaProviders.GraphVertexSchema;
import com.kayhut.fuse.unipop.schemaProviders.indexPartitions.IndexPartition;
import com.kayhut.fuse.unipop.schemaProviders.indexPartitions.TimeSeriesIndexPartition;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * Created by liorp on 4/26/2017.
 */
public class EBaseStatisticsProvider implements StatisticsProvider {
    private GraphElementSchemaProvider graphElementSchemaProvider;
    private Ontology ontology;
    private GraphStatisticsProvider graphStatisticsProvider;
    private static Set<ConstraintOp> supportedOps = new HashSet<>();

    static {
        supportedOps.add(ConstraintOp.eq);
        supportedOps.add(ConstraintOp.ne);
        supportedOps.add(ConstraintOp.ge);
        supportedOps.add(ConstraintOp.gt);
        supportedOps.add(ConstraintOp.le);
        supportedOps.add(ConstraintOp.lt);
    }


    public EBaseStatisticsProvider(GraphElementSchemaProvider graphElementSchemaProvider, Ontology ontology, GraphStatisticsProvider graphStatisticsProvider) {
        this.graphElementSchemaProvider = graphElementSchemaProvider;
        this.ontology = ontology;
        this.graphStatisticsProvider = graphStatisticsProvider;
    }

    @Override
    public Statistics.Cardinality getNodeStatistics(EEntityBase entity) {
        if (entity instanceof EConcrete) {
            List<Statistics.BucketInfo<String>> bucketInfos = Collections.singletonList(new Statistics.BucketInfo<String>(1L, 1L, ((EConcrete) entity).geteID(), ((EConcrete) entity).geteID()));
            return bucketInfos.get(0).getCardinalityObject();
        }

        List<String> vertexTypes = getVertexTypes(entity);
        Statistics.Cardinality entityStats = getVertexStatistics(vertexTypes.get(0));

        for (int i = 1; i < vertexTypes.size(); i++) {
            entityStats = (Statistics.Cardinality) entityStats.merge( getVertexStatistics(vertexTypes.get(i)));
        }

        return entityStats;

    }


    @Override
    public Statistics.Cardinality getNodeFilterStatistics(EEntityBase entity, EPropGroup entityFilter) {
        if (entity instanceof EConcrete) {
            List<Statistics.BucketInfo<String>> bucketInfos = Collections.singletonList(new Statistics.BucketInfo<String>(1L, 1L, ((EConcrete) entity).geteID(), ((EConcrete) entity).geteID()));
            return bucketInfos.get(0).getCardinalityObject();
        }
        List<String> vertexTypes = getVertexTypes(entity);
        Statistics.Cardinality entityStats = estimateVertexPropertyGroup(vertexTypes.get(0), entityFilter);

        for (int i = 1; i < vertexTypes.size(); i++) {
            entityStats = (Statistics.Cardinality) entityStats.merge( estimateVertexPropertyGroup(vertexTypes.get(i), entityFilter));
        }

        return entityStats;
    }

    @Override
    public Statistics.Cardinality getEdgeStatistics(Rel item) {
        return null;
    }

    @Override
    public Statistics.Cardinality getEdgeFilterStatistics(Rel item, RelPropGroup entityFilter) {
        return null;
    }

    @Override
    public Statistics.Cardinality getRedundantEdgeStatistics(Rel rel, EBase entity, EPropGroup entityFilter, Direction direction) {
        return null;
    }

    @Override
    public Statistics.Cardinality getRedundantNodeStatistics(Rel rel, EBase entity, EPropGroup entityFilter, Direction direction) {
        return null;
    }

    @Override
    public long getGlobalSelectivity(Rel rel, EBase entity, Direction direction) {
        return 0;
    }

    private Statistics.Cardinality getVertexStatistics(String vertexType) {
        return graphStatisticsProvider.getVertexCardinality(graphElementSchemaProvider.getVertexSchema(vertexType).get());
    }

    private Statistics.Cardinality getVertexStatistics(GraphVertexSchema graphVertexSchema, List<IndexPartition> relevantPartitions) {
        return graphStatisticsProvider.getVertexCardinality(graphVertexSchema, relevantPartitions);
    }

    private List<String> getVertexTypes(EEntityBase entity) {
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
        return vertexTypes;
    }

    private Statistics.Cardinality estimateVertexPropertyGroup(String vertexType, EPropGroup entityFilter) {
        GraphVertexSchema graphVertexSchema = graphElementSchemaProvider.getVertexSchema(vertexType).get();
        List<IndexPartition> indexPartitions = StreamSupport.stream(graphVertexSchema.getIndexPartitions().spliterator(),false).collect(Collectors.toList());
        List<IndexPartition> relevantPartitions = new ArrayList<>(indexPartitions);
        if(indexPartitions.size() > 0 && indexPartitions.get(0) instanceof TimeSeriesIndexPartition){
            relevantPartitions = findRelevantTimeSeriesPartitions(indexPartitions, entityFilter);
        }

        // This part assumes that all filter conditions are under an AND condition, so the estimation is the minimum.
        // When we add an OR condition (and a complex condition tree), we need to take a different approach
        Statistics.Cardinality minVertexCardinality = null;
        for(EProp eProp : entityFilter.geteProps()){
            GraphElementPropertySchema graphElementPropertySchema = graphVertexSchema.getProperty(eProp.getpType()).get();
            Optional<Statistics.Cardinality> conditionCardinality = getConditionCardinality(graphVertexSchema, graphElementPropertySchema, eProp.getCon(), relevantPartitions);
            if(minVertexCardinality == null){
                if(conditionCardinality.isPresent())
                    minVertexCardinality = conditionCardinality.get();
                else{
                    minVertexCardinality = getVertexStatistics(graphVertexSchema, relevantPartitions);
                }
            }
            else{
                if(conditionCardinality.isPresent() &&  minVertexCardinality.getTotal() > conditionCardinality.get().getTotal())
                    minVertexCardinality = conditionCardinality.get();
            }
        }
        return minVertexCardinality;
    }

    private Optional<Statistics.Cardinality> getConditionCardinality(GraphVertexSchema graphVertexSchema,
                                                                     GraphElementPropertySchema graphElementPropertySchema,
                                                                     Constraint constraint,
                                                                     List<IndexPartition> relevantPartitions) {

        if(!supportedOps.contains(constraint.getOp())){
            return Optional.empty();
        }

        switch(graphElementPropertySchema.getType()){
            case "string":
                return getValueConditionCardinality(graphVertexSchema, graphElementPropertySchema, constraint, relevantPartitions, String.class);
        }
        return Optional.empty();
    }

    private <T extends Comparable<T>> Optional<Statistics.Cardinality> getValueConditionCardinality(GraphVertexSchema graphVertexSchema, GraphElementPropertySchema graphElementPropertySchema, Constraint constraint, List<IndexPartition> relevantPartitions, Class<T> tp) {
        if(tp.isInstance(constraint.getExpr())){
            T expr = (T) constraint.getExpr();
            Statistics.HistogramStatistics<T> histogramStatistics = graphStatisticsProvider.getConditionHistogram(graphVertexSchema, relevantPartitions, graphElementPropertySchema, constraint.getOp(), expr);
            return Optional.of(estimateCardinality(histogramStatistics, expr, constraint.getOp()));
        }
        return Optional.empty();
    }

    private <T extends Comparable<T>> Statistics.Cardinality estimateCardinality(Statistics.HistogramStatistics<T> histogramStatistics, T value, ConstraintOp constraintOp){
        Statistics.Cardinality cardinality = null;
        switch(constraintOp){
            case eq:
                Optional<Statistics.BucketInfo<T>> bucketContaining = histogramStatistics.findBucketContaining(value);
                if(bucketContaining.isPresent()){
                    cardinality = new Statistics.Cardinality(bucketContaining.get().getTotal() / bucketContaining.get().getCardinality(),1);
                }else{
                    cardinality = new Statistics.Cardinality(0,0);
                }
                break;
            case ne:
                break;
            case gt:
                break;
            case ge:
                break;
            case lt:
                break;
            case le:
                break;

        }
        return cardinality;
    }

    private List<IndexPartition> findRelevantTimeSeriesPartitions(List<IndexPartition> indexPartitions, EPropGroup entityFilter) {
        //todo check if should use db prop name
        TimeSeriesIndexPartition firstPartition = (TimeSeriesIndexPartition) indexPartitions.get(0);
        EProp timeCondition = null;
        for (EProp eProp:entityFilter.geteProps()){
            if(eProp.getpType().equals(firstPartition.getTimeField())){
                timeCondition = eProp;
                break;
            }
        }

        if(timeCondition == null)
            return indexPartitions;

        List<IndexPartition> relevantPartitions = new ArrayList<>(indexPartitions);

        for(IndexPartition indexPartition : indexPartitions){
            TimeSeriesIndexPartition timeSeriesIndexPartition = (TimeSeriesIndexPartition) indexPartition;
            //todo remove non relevant partitions
        }

        return relevantPartitions;

    }


}
