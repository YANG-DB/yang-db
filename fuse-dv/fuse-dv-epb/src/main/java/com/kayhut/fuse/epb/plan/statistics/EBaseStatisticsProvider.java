package com.kayhut.fuse.epb.plan.statistics;

/*-
 * #%L
 * fuse-dv-epb
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

import com.google.common.collect.Iterables;
import com.kayhut.fuse.model.execution.plan.Direction;
import com.kayhut.fuse.model.ontology.*;
import com.kayhut.fuse.model.query.properties.constraint.Constraint;
import com.kayhut.fuse.model.query.properties.constraint.ConstraintOp;
import com.kayhut.fuse.model.query.EBase;
import com.kayhut.fuse.model.query.Rel;
import com.kayhut.fuse.model.query.entity.EConcrete;
import com.kayhut.fuse.model.query.entity.EEntityBase;
import com.kayhut.fuse.model.query.entity.ETyped;
import com.kayhut.fuse.model.query.entity.EUntyped;
import com.kayhut.fuse.model.query.properties.*;
import com.kayhut.fuse.unipop.schemaProviders.*;
import com.kayhut.fuse.unipop.schemaProviders.indexPartitions.IndexPartitions;
import com.kayhut.fuse.unipop.schemaProviders.indexPartitions.TimeSeriesIndexPartitions;
import javaslang.collection.Stream;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by lior.perry on 4/26/2017.
 */
public class EBaseStatisticsProvider implements StatisticsProvider {
    private GraphElementSchemaProvider graphElementSchemaProvider;
    private Ontology.Accessor ont;
    private GraphStatisticsProvider graphStatisticsProvider;

    // Supported operators by the estimation estimator, used for validation
    private static Set<ConstraintOp> supportedOps = new HashSet<>();

    static {
        supportedOps.add(ConstraintOp.eq);
        supportedOps.add(ConstraintOp.ge);
        supportedOps.add(ConstraintOp.gt);
        supportedOps.add(ConstraintOp.le);
        supportedOps.add(ConstraintOp.lt);
        supportedOps.add(ConstraintOp.ne);
        supportedOps.add(ConstraintOp.inSet);
        supportedOps.add(ConstraintOp.notInSet);
        supportedOps.add(ConstraintOp.inRange);
        supportedOps.add(ConstraintOp.startsWith);
        supportedOps.add(ConstraintOp.notStartsWith);
    }

    public EBaseStatisticsProvider(
            GraphElementSchemaProvider graphElementSchemaProvider,
            Ontology.Accessor ont,
            GraphStatisticsProvider graphStatisticsProvider) {
        this.graphElementSchemaProvider = graphElementSchemaProvider;
        this.ont = ont;
        this.graphStatisticsProvider = graphStatisticsProvider;
    }

    @Override
    public Statistics.SummaryStatistics getNodeStatistics(EEntityBase entity) {
        // EConcrete == single entity, no querying, assuming the entity exists
        if (entity instanceof EConcrete) {
            List<Statistics.BucketInfo<String>> bucketInfos = Collections.singletonList(new Statistics.BucketInfo<String>(1L, 1L, ((EConcrete) entity).geteID(), ((EConcrete) entity).geteID()));
            return bucketInfos.get(0).getCardinalityObject();
        }

        // We estimate each vertex type's statistics, and combine all statistics together
        List<String> vertexTypes = getVertexTypes(entity, ont, graphElementSchemaProvider.getVertexLabels());
        Statistics.SummaryStatistics entityStats = getVertexStatistics(vertexTypes.get(0));

        for (int i = 1; i < vertexTypes.size(); i++) {
            entityStats = (Statistics.SummaryStatistics) entityStats.merge( getVertexStatistics(vertexTypes.get(i)));
        }

        return entityStats;

    }

    @Override
    public Statistics.SummaryStatistics getNodeFilterStatistics(EEntityBase entity, EPropGroup entityFilter) {
        if (entity instanceof EConcrete) {
            List<Statistics.BucketInfo<String>> bucketInfos = Collections.singletonList(new Statistics.BucketInfo<String>(1L, 1L, ((EConcrete) entity).geteID(), ((EConcrete) entity).geteID()));
            return bucketInfos.get(0).getCardinalityObject();
        }
        List<String> vertexTypes = getVertexTypes(entity,ont,graphElementSchemaProvider.getVertexLabels());

        Statistics.SummaryStatistics entityStats = estimateVertexPropertyGroup(vertexTypes.get(0),entityFilter);

        for (int i = 1; i < vertexTypes.size(); i++) {
            entityStats = (Statistics.SummaryStatistics) entityStats.merge( estimateVertexPropertyGroup(vertexTypes.get(i), entityFilter));
        }

        return entityStats;
    }

    @Override
    public Statistics.SummaryStatistics getEdgeStatistics(Rel rel, EEntityBase source) {
        Iterable<GraphEdgeSchema> edgeSchemas = graphElementSchemaProvider.getEdgeSchemas(ont.$relation$(rel.getrType()).getName());
        if (Stream.ofAll(edgeSchemas).isEmpty()) {
            // what to do what to do
        }

        //currently supports a single edge schema
        GraphEdgeSchema edgeSchema = Stream.ofAll(edgeSchemas).get(0);

        return getEdgeStatistics(edgeSchema);
    }

    @Override
    public Statistics.SummaryStatistics getEdgeFilterStatistics(Rel rel, RelPropGroup relFilter, EEntityBase source) {
        Iterable<GraphEdgeSchema> graphEdgeSchemas = graphElementSchemaProvider.getEdgeSchemas(ont.$relation$(rel.getrType()).getName());
        if (Stream.ofAll(graphEdgeSchemas).isEmpty()) {
            // what to do what to do
        }

        //currently supports a single edge schema
        GraphEdgeSchema graphEdgeSchema = Stream.ofAll(graphEdgeSchemas).get(0);

        List<String> relevantIndices = getRelevantIndicesForEdge(relFilter, graphEdgeSchema);
        Statistics.SummaryStatistics minEdgeSummaryStatistics = getEdgeStatistics(graphEdgeSchema, relevantIndices);
        for(RelProp relProp : Stream.ofAll(relFilter.getProps()).filter(relProp -> relProp.getCon() != null)){
            Property property = ont.$property$( relProp.getpType() );

            GraphElementPropertySchema graphElementPropertySchema;
            if (relProp instanceof RedundantRelProp){
                graphElementPropertySchema = graphEdgeSchema.getEndB().get().getRedundantProperty(graphElementSchemaProvider.getPropertySchema(property.getName()).get()).get();
            }else {
                graphElementPropertySchema = graphEdgeSchema.getProperty(property.getName()).get();
            }

            Optional<Statistics.SummaryStatistics> conditionCardinality = getConditionCardinality(graphEdgeSchema, graphElementPropertySchema, relProp.getCon(), relevantIndices, property.getType());
            if(conditionCardinality.isPresent() &&  minEdgeSummaryStatistics.getTotal() > conditionCardinality.get().getTotal())
                minEdgeSummaryStatistics = conditionCardinality.get();

        }
        return minEdgeSummaryStatistics;
    }

    @Override
    public Statistics.SummaryStatistics getRedundantNodeStatistics(EEntityBase entity, RelPropGroup relPropGroup) {
        List<RedundantRelProp> pushdownProps = relPropGroup.getProps().stream().filter(prop -> prop instanceof RedundantRelProp).
                map(RedundantRelProp.class::cast).collect(Collectors.toList());

        EPropGroup ePropGroup = new EPropGroup(pushdownProps.stream().map(prop -> EProp.of(prop.geteNum(), prop.getpType(), prop.getCon())).collect(Collectors.toList()));
        return getNodeFilterStatistics(entity, ePropGroup);
    }

    @Override
    public long getGlobalSelectivity(Rel rel, RelPropGroup filter, EBase entity, Direction direction) {
        Iterable<GraphEdgeSchema> graphEdgeSchemas = graphElementSchemaProvider.getEdgeSchemas(ont.$relation$(rel.getrType()).getName());
        if (Stream.ofAll(graphEdgeSchemas).isEmpty()) {
            // what to do what to do
        }

        //currently supports a single edge schema
        GraphEdgeSchema graphEdgeSchema = Stream.ofAll(graphEdgeSchemas).get(0);

        List<String> relevantIndices = getRelevantIndicesForEdge(filter, graphEdgeSchema);
        return graphStatisticsProvider.getGlobalSelectivity(graphEdgeSchema, rel.getDir(), relevantIndices);
    }

    private List<String> getRelevantIndicesForEdge(RelPropGroup relPropGroup, GraphEdgeSchema graphEdgeSchema) {
        IndexPartitions indexPartitions = graphEdgeSchema.getIndexPartitions().get();
        List<String> relevantIndices = Stream.ofAll(indexPartitions.getPartitions()).flatMap(IndexPartitions.Partition::getIndices).toJavaList();
        if(indexPartitions instanceof TimeSeriesIndexPartitions){
            relevantIndices = findRelevantTimeSeriesIndices((TimeSeriesIndexPartitions) indexPartitions,relPropGroup);
        }
        return relevantIndices;
    }

    private Statistics.SummaryStatistics getEdgeStatistics(GraphEdgeSchema edgeSchema) {
        return graphStatisticsProvider.getEdgeCardinality(edgeSchema);
    }

    private Statistics.SummaryStatistics getEdgeStatistics(GraphEdgeSchema edgeSchema, List<String> relevantIndices) {
        return graphStatisticsProvider.getEdgeCardinality(edgeSchema, relevantIndices);
    }

    private Statistics.SummaryStatistics getVertexStatistics(String vertexType) {
        Iterable<GraphVertexSchema> vertexSchemas = graphElementSchemaProvider.getVertexSchemas(vertexType);
        if (Stream.of(vertexSchemas).isEmpty()) {
            // what to do what to do
        }

        //currently supports a single vertex schema
        GraphVertexSchema vertexSchema = Stream.ofAll(vertexSchemas).get(0);

        return graphStatisticsProvider.getVertexCardinality(vertexSchema);
    }

    private Statistics.SummaryStatistics getVertexStatistics(GraphVertexSchema graphVertexSchema, List<String> relevantIndices) {
        return graphStatisticsProvider.getVertexCardinality(graphVertexSchema, relevantIndices);
    }

    private Statistics.SummaryStatistics estimateVertexPropertyGroup(String vertexType, EPropGroup entityFilter) {
        Iterable<GraphVertexSchema> graphVertexSchemas = graphElementSchemaProvider.getVertexSchemas(vertexType);
        if (Stream.of(graphVertexSchemas).isEmpty()) {
            // what to do what to do
        }

        //currently supports a single vertex schema
        GraphVertexSchema graphVertexSchema = Stream.ofAll(graphVertexSchemas).get(0);


        List<String> relevantIndices = getVertexRelevantIndices(entityFilter, graphVertexSchema);

        // This part assumes that all filter conditions are under an AND condition, so the estimation is the minimum.
        // When we add an OR condition (and a complex condition tree), we need getTo take a different approach
        Statistics.SummaryStatistics minVertexSummaryStatistics = getVertexStatistics(graphVertexSchema, relevantIndices);
        for(EProp eProp : Stream.ofAll(entityFilter.getProps()).filter(eProp -> eProp.getCon() != null)){
            Property property = ont.$property$( eProp.getpType() );
            Optional<GraphElementPropertySchema> graphElementPropertySchema = graphVertexSchema.getProperty(property.getName());
            if(graphElementPropertySchema.isPresent()) {
                Optional<Statistics.SummaryStatistics> conditionCardinality = getConditionCardinality(graphVertexSchema, graphElementPropertySchema.get(), eProp.getCon(), relevantIndices, property.getType());
                if (conditionCardinality.isPresent() && minVertexSummaryStatistics.getTotal() > conditionCardinality.get().getTotal())
                    minVertexSummaryStatistics = conditionCardinality.get();

            }else{
                // If a property does not exist on the vertex, we return 0 cardinality (again, assuming AND behavior)
                return new Statistics.SummaryStatistics(0,0);
            }
        }
        return minVertexSummaryStatistics;
    }

    private List<String> getVertexRelevantIndices(EPropGroup entityFilter, GraphVertexSchema graphVertexSchema) {
        IndexPartitions indexPartitions = graphVertexSchema.getIndexPartitions().get();
        List<String> relevantIndices = Stream.ofAll(indexPartitions.getPartitions()).flatMap(IndexPartitions.Partition::getIndices).toJavaList();
        if(indexPartitions instanceof TimeSeriesIndexPartitions){
            relevantIndices = findRelevantTimeSeriesIndices((TimeSeriesIndexPartitions) indexPartitions, entityFilter);
        }
        return relevantIndices;
    }

    private Optional<Statistics.SummaryStatistics> getConditionCardinality(GraphElementSchema graphElementSchema,
                                                                           GraphElementPropertySchema graphElementPropertySchema,
                                                                           Constraint constraint,
                                                                           List<String> relevantIndices,
                                                                           String pType) {

        if(!supportedOps.contains(constraint.getOp())){
            return Optional.empty();
        }

        Optional<PrimitiveType> primitiveType = ont.primitiveType(pType);
        if(primitiveType.isPresent()) {
            return getValueConditionCardinality(graphElementSchema, graphElementPropertySchema, constraint, constraint.getExpr(), relevantIndices, primitiveType.get().getJavaType());
        }else{
            Optional<EnumeratedType> enumeratedType = ont.enumeratedType(graphElementPropertySchema.getType());
            if(enumeratedType.isPresent()) {
                //todo: Hack for present time, remove!!!
                //Value value = (Value) constraint.getExpr();
                GraphElementPropertySchema tmp  = new GraphElementPropertySchema.Impl( graphElementPropertySchema.getName(), "enum");
                return getValueConditionCardinality(graphElementSchema, tmp, constraint, constraint.getExpr(), relevantIndices, String.class);
            }
        }
        return Optional.empty();
    }

    private <T extends Comparable<T>> Optional<Statistics.SummaryStatistics> getValueConditionCardinality(GraphElementSchema graphElementSchema, GraphElementPropertySchema graphElementPropertySchema, Constraint constraintOp, Object expression, List<String> relevantIndices, Class<T> tp) {
        Statistics.HistogramStatistics<T> histogramStatistics = null;

        histogramStatistics = graphStatisticsProvider.getConditionHistogram(graphElementSchema, relevantIndices, graphElementPropertySchema, constraintOp, tp);
        if(histogramStatistics != null) {
            return Optional.of(estimateCardinality(histogramStatistics, expression, constraintOp));
        }
        return Optional.empty();
    }

    private <T extends Comparable<T>> Statistics.SummaryStatistics estimateCardinality(Statistics.HistogramStatistics<T> histogramStatistics, Object value, Constraint constraint){
        Statistics.SummaryStatistics summaryStatistics = null;
        switch(constraint.getOp()){
            case eq:
                Optional<Statistics.BucketInfo<T>> bucketContaining = histogramStatistics.findBucketContaining((T)value);
                summaryStatistics = bucketContaining.map(tBucketInfo -> new Statistics.SummaryStatistics(((double)tBucketInfo.getTotal()) / tBucketInfo.getCardinality(), 1)).
                        orElseGet(() -> new Statistics.SummaryStatistics(0, 0));
                break;
            case gt:
                List<Statistics.BucketInfo<T>> bucketsAbove = histogramStatistics.findBucketsAbove((T)value, false);
                return estimateGreaterThan(bucketsAbove, (T)value, false);
            case ge:
                bucketsAbove = histogramStatistics.findBucketsAbove((T)value, true);
                return estimateGreaterThan(bucketsAbove, (T)value, true);
            case lt:
                List<Statistics.BucketInfo<T>> bucketsBelow = histogramStatistics.findBucketsBelow((T)value, false);
                return estimateLessThan(bucketsBelow, (T)value, false);
            case le:
                bucketsBelow = histogramStatistics.findBucketsBelow((T)value, true);
                return estimateLessThan(bucketsBelow, (T)value, true);
            case ne:
                // Pessimistic estimate that a not equals condition is almost the same as the entire distribution
                // given that we throw a single value
                bucketContaining = histogramStatistics.findBucketContaining((T) value);
                List<Statistics.BucketInfo<T>> bucketInfos = histogramStatistics.getBuckets();
                // If the bucket that contains the value is a single value bucket - we throw it
                if(bucketContaining.isPresent() && bucketContaining.get().isSingleValue()){
                    bucketInfos.remove(bucketContaining.get());
                }
                return mergeBucketsCardinality(bucketInfos);
            case inSet:
                List<T> valueList = (List<T>) value;
                double total = 0;
                double count = 0;
                for(T v : valueList){
                    bucketContaining = histogramStatistics.findBucketContaining(v);
                    if(bucketContaining.isPresent()) {
                        total += ((double) bucketContaining.get().getTotal()) / bucketContaining.get().getCardinality();
                        count += 1;
                    } else {
                        System.out.println("Bucket not found for "+v);
                    }
                }
                return new Statistics.SummaryStatistics(total,count);
            case notInSet:
                valueList = (List<T>) value;
                bucketInfos = histogramStatistics.getBuckets();

                for(T v : valueList){
                    bucketContaining = histogramStatistics.findBucketContaining(v);
                    if(bucketContaining.isPresent() && bucketContaining.get().isSingleValue()){
                        bucketInfos.remove(bucketContaining.get());
                    }
                }
                return mergeBucketsCardinality(bucketInfos);
            case inRange:
                valueList = (List<T>) value;
                bucketsAbove = histogramStatistics.findBucketsAbove(valueList.get(0), constraint.getiType().startsWith("["));
                bucketsBelow = histogramStatistics.findBucketsBelow(valueList.get(1), constraint.getiType().endsWith("]"));
                return estimateRange(bucketsAbove, bucketsBelow, valueList, constraint.getiType());
            case startsWith:
                String stringValue = (String) value;
                List<Statistics.BucketInfo<String>> startsWithBuckets = findStartsWithBuckets(stringValue, (Statistics.HistogramStatistics<String>) histogramStatistics);
                return mergeBucketsCardinality(startsWithBuckets);
            case notStartsWith:
                stringValue = (String) value;
                List<Statistics.BucketInfo<String>> notStartsWithBuckets = findNotStartsWithBuckets(stringValue, (Statistics.HistogramStatistics<String>) histogramStatistics);
                return mergeBucketsCardinality(notStartsWithBuckets);
        }
        return summaryStatistics;
    }

    private List<Statistics.BucketInfo<String>> findNotStartsWithBuckets(String stringValue, Statistics.HistogramStatistics<String> histogramStatistics) {
        List<Statistics.BucketInfo<String>> notStartsWithBuckets = new ArrayList<>();
        for(Statistics.BucketInfo<String> bucket : histogramStatistics.getBuckets()){
            if(!bucket.isValueInRange(stringValue) && !bucket.getLowerBound().startsWith(stringValue)){
                notStartsWithBuckets.add(bucket);
            }
        }
        return notStartsWithBuckets;
    }

    private List<Statistics.BucketInfo<String>> findStartsWithBuckets(String stringValue, Statistics.HistogramStatistics<String> histogramStatistics) {
        List<Statistics.BucketInfo<String>> startsWithBuckets = new ArrayList<>();
        int i = 0;
        for(;i<histogramStatistics.getBuckets().size();i++){
            Statistics.BucketInfo<String> currentBucket = histogramStatistics.getBuckets().get(i);
            if(currentBucket.getLowerBound().compareTo(stringValue) <= 0 ){
                if(currentBucket.getHigherBound().compareTo(stringValue) > 0) {
                    startsWithBuckets.add(currentBucket);
                    break;
                }
                if(currentBucket.getLowerBound().equals(currentBucket.getHigherBound()) && currentBucket.getLowerBound().equals(stringValue)){
                    startsWithBuckets.add(currentBucket);
                    break;
                }
            }

            if(currentBucket.getLowerBound().compareTo(stringValue) > 0){
                if(currentBucket.getLowerBound().startsWith(stringValue)){
                    startsWithBuckets.add(currentBucket);
                }
                break;
            }
        }

        for(i++;i<histogramStatistics.getBuckets().size();i++){
            Statistics.BucketInfo<String> currentBucket = histogramStatistics.getBuckets().get(i);
            if(currentBucket.getLowerBound().startsWith(stringValue))
                startsWithBuckets.add(currentBucket);
            else
                break;
        }

        return startsWithBuckets;
    }

    private <T extends Comparable<T>> Statistics.SummaryStatistics estimateRange(List<Statistics.BucketInfo<T>> bucketsAbove, List<Statistics.BucketInfo<T>> bucketsBelow, List<T> valueList, String iType) {
        List<Statistics.BucketInfo<T>> joinedBuckets = new LinkedList<>(bucketsAbove);
        joinedBuckets.retainAll(bucketsBelow);
        if(joinedBuckets.size() > 0){
            Statistics.SummaryStatistics summaryStatistics = estimateGreaterThan(joinedBuckets, valueList.get(0), iType.startsWith("["));
            Statistics.SummaryStatistics greaterSummaryStatistics = estimateLessThan(joinedBuckets.subList(joinedBuckets.size() - 1, joinedBuckets.size()), valueList.get(1), iType.endsWith("]"));
            Statistics.BucketInfo<T> last = Iterables.getLast(joinedBuckets);
            return new Statistics.SummaryStatistics(summaryStatistics.getTotal() + greaterSummaryStatistics.getTotal() - last.getTotal(),  summaryStatistics.getCardinality() + greaterSummaryStatistics.getCardinality() - last.getCardinality());

        }
        return mergeBucketsCardinality(joinedBuckets);
    }

    // Currently lt and lte have the same costs
    // Also, in case we have a non numeric/date value, we take a pessimistic estimate of the bucket containing the given value (entire bucket, not relative part)
    private <T extends Comparable<T>> Statistics.SummaryStatistics estimateLessThan(List<Statistics.BucketInfo<T>> bucketsBelow, T value, boolean inclusive) {
        if(bucketsBelow.size() == 0)
            return new Statistics.SummaryStatistics(0,0);

        Statistics.BucketInfo<T> lastBucket = Iterables.getLast(bucketsBelow);
        if(lastBucket.isValueInRange(value)){
            double partialBucket = 1.0;
            if(value instanceof Long){
                Long start = (Long)lastBucket.getLowerBound();
                Long end = (Long) lastBucket.getHigherBound();
                Long v = (Long) value;
                partialBucket = ((double) (v - start)) / (end - start);
            }
            if(value instanceof Double){
                Double start = (Double) lastBucket.getLowerBound();
                Double end = (Double) lastBucket.getHigherBound();
                Double v = (Double) value;
                partialBucket = ((v - start)) / (end - start);
            }
            if(value instanceof Date){
                Date start = (Date)lastBucket.getLowerBound();
                Date end = (Date) lastBucket.getHigherBound();
                Date v = (Date) value;
                partialBucket = ((double)(v.getTime() - start.getTime())) / (end.getTime() - start.getTime());

            }
            Statistics.SummaryStatistics summaryStatistics = mergeBucketsCardinality(bucketsBelow.subList(0, bucketsBelow.size() - 1));
            return new Statistics.SummaryStatistics(summaryStatistics.getTotal() + lastBucket.getTotal() * partialBucket, summaryStatistics.getCardinality() + lastBucket.getCardinality()*partialBucket);
        }
        return mergeBucketsCardinality(bucketsBelow);
    }

    private <T extends Comparable<T>> Statistics.SummaryStatistics estimateGreaterThan(List<Statistics.BucketInfo<T>> bucketsAbove, T value, boolean inclusive) {
        if (bucketsAbove.size() == 0)
            return new Statistics.SummaryStatistics(0,0);
        Statistics.BucketInfo<T> firstBucket = bucketsAbove.get(0);
        if(firstBucket.isValueInRange(value)){
            double partialBucket = 1.0;
            if(value instanceof Long){
                Long start = (Long)firstBucket.getLowerBound();
                Long end = (Long) firstBucket.getHigherBound();
                Long v = (Long) value;
                partialBucket = ((double) (end - v)) / (end - start);
            }
            if(value instanceof Double){
                Double start = (Double) firstBucket.getLowerBound();
                Double end = (Double) firstBucket.getHigherBound();
                Double v = (Double) value;
                partialBucket = ((end - v)) / (end - start);
            }
            if(value instanceof Date){
                Date start = (Date)firstBucket.getLowerBound();
                Date end = (Date) firstBucket.getHigherBound();
                Date v = (Date) value;
                partialBucket = ((double)(end.getTime() - v.getTime())) / (end.getTime() - start.getTime());

            }
            Statistics.SummaryStatistics summaryStatistics = mergeBucketsCardinality(bucketsAbove.subList(1, bucketsAbove.size()));
            return new Statistics.SummaryStatistics(summaryStatistics.getTotal() + firstBucket.getTotal() * partialBucket, summaryStatistics.getCardinality() + firstBucket.getCardinality()*partialBucket);
        }
        return mergeBucketsCardinality(bucketsAbove);
    }

    private List<String> findRelevantTimeSeriesIndices(TimeSeriesIndexPartitions indexPartitions, EPropGroup entityFilter) {
        List<EProp> timeConditions = new ArrayList<>();
        for (EProp eProp : entityFilter.getProps()){
            Property property =  ont.$property$(eProp.getpType());
            if(property.getName().equals(indexPartitions.getTimeField())){
                switch(eProp.getCon().getOp()){
                    case inRange:
                        List<Date> values = (List<Date>)eProp.getCon().getExpr();
                        timeConditions.add(EProp.of(0, eProp.getpType(), Constraint.of(eProp.getCon().getiType().startsWith("[")? ConstraintOp.ge: ConstraintOp.gt, values.get(0))));
                        timeConditions.add(EProp.of(0, eProp.getpType(), Constraint.of(eProp.getCon().getiType().startsWith("]")? ConstraintOp.le: ConstraintOp.lt, values.get(1))));
                        break;
                    case inSet:
                        values = (List<Date>)eProp.getCon().getExpr();
                        for(Date value : values){
                            timeConditions.add(EProp.of(0, eProp.getpType(), Constraint.of(ConstraintOp.eq, value)));
                        }
                        break;
                    case notInSet:
                        values = (List<Date>)eProp.getCon().getExpr();
                        for(Date value : values){
                            timeConditions.add(EProp.of(0, eProp.getpType(), Constraint.of(ConstraintOp.ne, value)));
                        }
                        break;
                    default:
                        timeConditions.add(eProp);
                        break;
                }
            }
        }

        List<String> relevantIndices = Stream.ofAll(indexPartitions.getPartitions()).flatMap(IndexPartitions.Partition::getIndices).toJavaList();
        if(timeConditions.size() == 0) {
            return relevantIndices;
        }

        for(EProp timeCondition : timeConditions) {
            String indexName = indexPartitions.getIndexName((Date) timeCondition.getCon().getExpr());
            relevantIndices.removeAll(findIndicesToRemove(timeCondition.getCon(), relevantIndices, indexName));
        }
        return relevantIndices;

    }

    private List<String> findRelevantTimeSeriesIndices(TimeSeriesIndexPartitions indexPartitions, RelPropGroup relPropGroup) {
        List<RelProp> timeConditions = new ArrayList<>();
        for (RelProp relProp : relPropGroup.getProps()){
            if (ont.$property$(relProp.getpType()).getName().equals(indexPartitions.getTimeField())) {
                switch(relProp.getCon().getOp()){
                    case inRange:
                        List<Date> values = (List<Date>)relProp.getCon().getExpr();
                        if(!values.isEmpty()) {
                            timeConditions.add(RelProp.of(0, relProp.getpType(), Constraint.of(relProp.getCon().getiType().startsWith("[") ? ConstraintOp.ge : ConstraintOp.gt, values.get(0))));
                            timeConditions.add(RelProp.of(0, relProp.getpType(), Constraint.of(relProp.getCon().getiType().startsWith("]") ? ConstraintOp.le : ConstraintOp.lt, values.get(1))));
                        }
                        break;
                    case inSet:
                        values = (List<Date>)relProp.getCon().getExpr();
                        for(Date value : values){
                            timeConditions.add(RelProp.of(0, relProp.getpType(), Constraint.of(ConstraintOp.eq, value)));
                        }
                        break;
                    case notInSet:
                        values = (List<Date>)relProp.getCon().getExpr();
                        for(Date value : values){
                            timeConditions.add(RelProp.of(0, relProp.getpType(), Constraint.of(ConstraintOp.ne, value)));
                        }
                        break;
                    default:
                        timeConditions.add(relProp);
                        break;
                }
            }
        }

        List<String> relevantIndices = Stream.ofAll(indexPartitions.getPartitions()).flatMap(IndexPartitions.Partition::getIndices).toJavaList();

        if(timeConditions.size() == 0) {
            return relevantIndices;
        }

        for(RelProp timeCondition : timeConditions) {

            String indexName = indexPartitions.getIndexName((Date) timeCondition.getCon().getExpr());
            relevantIndices.removeAll(findIndicesToRemove(timeCondition.getCon(), relevantIndices, indexName));
        }
        return relevantIndices;

    }

    private List<String> findIndicesToRemove(Constraint timeCondition, List<String> relevantIndices, String indexName){
        List<String> indicesToRemove = new ArrayList<>();
        switch(timeCondition.getOp()){
            case eq:
                indicesToRemove.addAll(relevantIndices.stream().filter(idx -> !idx.equals(indexName)).collect(Collectors.toList()));
                break;
            case ne:
                // Do nothing, under the assumption that an index contains more than one value (range)
                break;
            case gt:
            case ge:
                indicesToRemove.addAll(relevantIndices.stream().filter(idx -> idx.compareTo(indexName) < 0).collect(Collectors.toList()));
                break;
            case lt:
            case le:
                indicesToRemove.addAll(relevantIndices.stream().filter(idx -> idx.compareTo(indexName) > 0).collect(Collectors.toList()));
                break;

        }
        return indicesToRemove;
    }

    private <T extends Comparable<T>> Statistics.SummaryStatistics mergeBucketsCardinality(List<Statistics.BucketInfo<T>> buckets){
        Statistics.SummaryStatistics summaryStatistics = new Statistics.SummaryStatistics(0,0);
        for(Statistics.BucketInfo<T> bucketInfo : buckets){
            summaryStatistics = (Statistics.SummaryStatistics) summaryStatistics.merge(bucketInfo.getCardinalityObject());
        }
        return summaryStatistics;
    }

    private List<String> getVertexTypes(EEntityBase entity, Ontology.Accessor ont, Iterable<String> vertexTypes) {
        List<String> _vertexTypes = Stream.ofAll(vertexTypes).toJavaList();
        if (entity instanceof EUntyped) {
            EUntyped eUntyped = (EUntyped) entity;
            if (eUntyped.getvTypes().size() > 0) {
                _vertexTypes = Stream.ofAll(eUntyped.getvTypes()).map(v -> ont.$entity$(v).getName()).toJavaList();
            } else {
                _vertexTypes = Stream.ofAll(vertexTypes).toJavaList();
                if (eUntyped.getNvTypes().size() > 0) {
                    _vertexTypes.removeAll(Stream.ofAll(eUntyped.getNvTypes()).map(v -> ont.$entity$(v).getName()).toJavaList());
                }
            }
        } else if (entity instanceof ETyped) {
            _vertexTypes = Collections.singletonList(ont.$entity$(((ETyped) entity).geteType()).getName());
        }
        return _vertexTypes;
    }
}
