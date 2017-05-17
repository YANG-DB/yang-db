package com.kayhut.fuse.epb.plan.statistics;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.kayhut.fuse.model.execution.plan.Direction;
import com.kayhut.fuse.model.ontology.*;
import com.kayhut.fuse.model.query.Constraint;
import com.kayhut.fuse.model.query.ConstraintOp;
import com.kayhut.fuse.model.query.EBase;
import com.kayhut.fuse.model.query.Rel;
import com.kayhut.fuse.model.query.entity.EConcrete;
import com.kayhut.fuse.model.query.entity.EEntityBase;
import com.kayhut.fuse.model.query.properties.*;
import com.kayhut.fuse.unipop.schemaProviders.*;
import com.kayhut.fuse.unipop.schemaProviders.indexPartitions.IndexPartition;
import com.kayhut.fuse.unipop.schemaProviders.indexPartitions.TimeSeriesIndexPartition;

import java.util.*;
import java.util.stream.Collectors;

import static com.kayhut.fuse.asg.util.AsgQueryUtils.getVertexTypes;

/**
 * Created by liorp on 4/26/2017.
 */
public class EBaseStatisticsProvider implements StatisticsProvider {
    private GraphElementSchemaProvider graphElementSchemaProvider;
    private Ontology ontology;
    private GraphStatisticsProvider graphStatisticsProvider;

    // Supported operators by the cost estimator, used for validation
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

    public EBaseStatisticsProvider(GraphElementSchemaProvider graphElementSchemaProvider, Ontology ontology, GraphStatisticsProvider graphStatisticsProvider) {
        this.graphElementSchemaProvider = graphElementSchemaProvider;
        this.ontology = ontology;
        this.graphStatisticsProvider = graphStatisticsProvider;
    }

    @Override
    public Statistics.Cardinality getNodeStatistics(EEntityBase entity) {
        // EConcrete == single entity, no querying, assuming the entity exists
        if (entity instanceof EConcrete) {
            List<Statistics.BucketInfo<String>> bucketInfos = Collections.singletonList(new Statistics.BucketInfo<String>(1L, 1L, ((EConcrete) entity).geteID(), ((EConcrete) entity).geteID()));
            return bucketInfos.get(0).getCardinalityObject();
        }

        // We estimate each vertex type's statistics, and combine all statistics together
        List<String> vertexTypes = getVertexTypes(entity,ontology,graphElementSchemaProvider.getVertexTypes());
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
        List<String> vertexTypes = getVertexTypes(entity,ontology,graphElementSchemaProvider.getVertexTypes());

        Statistics.Cardinality entityStats = estimateVertexPropertyGroup(vertexTypes.get(0),entityFilter);

        for (int i = 1; i < vertexTypes.size(); i++) {
            entityStats = (Statistics.Cardinality) entityStats.merge( estimateVertexPropertyGroup(vertexTypes.get(i), entityFilter));
        }

        return entityStats;
    }

    @Override
    public Statistics.Cardinality getEdgeStatistics(Rel rel) {
        GraphEdgeSchema edgeSchema = graphElementSchemaProvider.getEdgeSchema(OntologyUtil.getRelationTypeNameById(ontology, rel.getrType())).get();
        return getEdgeStatistics(edgeSchema);
    }

    @Override
    public Statistics.Cardinality getEdgeFilterStatistics(Rel rel, RelPropGroup relFilter) {
        GraphEdgeSchema graphEdgeSchema = graphElementSchemaProvider.getEdgeSchema(OntologyUtil.getRelationTypeNameById(ontology, rel.getrType())).get();
        List<String> relevantIndices = getRelevantIndicesForEdge(relFilter, graphEdgeSchema);
        Statistics.Cardinality minEdgeCardinality = getEdgeStatistics(graphEdgeSchema, relevantIndices);

        for(RelProp relProp : relFilter.getProps()){
            Property property = OntologyUtil.getProperty(ontology, Integer.parseInt( relProp.getpType())).get();
            GraphElementPropertySchema graphElementPropertySchema = graphEdgeSchema.getProperty(property.getName()).get();
            Optional<Statistics.Cardinality> conditionCardinality = getConditionCardinality(graphEdgeSchema, graphElementPropertySchema, relProp.getCon(), relevantIndices, property.getType());
            if(conditionCardinality.isPresent() &&  minEdgeCardinality.getTotal() > conditionCardinality.get().getTotal())
                minEdgeCardinality = conditionCardinality.get();

        }
        return minEdgeCardinality;
    }

//    @Override
    /*public Statistics.Cardinality getRedundantEdgeStatistics(Rel rel, RelPropGroup relPropGroup, Direction direction) {

        List<PushdownRelProp> pushdownProps = relPropGroup.getProps().stream().filter(prop -> prop instanceof PushdownRelProp).
                                                        map(PushdownRelProp.class::cast).collect(Collectors.toList());

        GraphEdgeSchema graphEdgeSchema = graphElementSchemaProvider.getEdgeSchema(OntologyUtil.getRelationTypeNameById(ontology, rel.getrType())).get();
        List<String> relevantIndices = getRelevantIndicesForEdge(relPropGroup, graphEdgeSchema);
        GraphEdgeSchema.End destination = graphEdgeSchema.getDestination().get();
        Statistics.Cardinality minEdgeCardinality = getEdgeStatistics(graphEdgeSchema, relevantIndices);

        for(PushdownRelProp pushdownRelProp : pushdownProps){
            Optional<GraphRedundantPropertySchema> pushdownVertexProperty = destination.getRedundantVertexPropertyByPushdownName(pushdownRelProp.getPushdownPropName());
            Optional<Statistics.Cardinality> conditionCardinality = getConditionCardinality(graphEdgeSchema, pushdownVertexProperty.get(), pushdownRelProp.getCon(), relevantIndices, pushdownVertexProperty.get().getType());
            if (conditionCardinality.isPresent() && minEdgeCardinality.getTotal() > conditionCardinality.get().getTotal())
                minEdgeCardinality = conditionCardinality.get();
        }
        return minEdgeCardinality;

    }*/

    @Override
    public Statistics.Cardinality getRedundantNodeStatistics(EEntityBase entity, RelPropGroup relPropGroup) {
        List<PushdownRelProp> pushdownProps = relPropGroup.getProps().stream().filter(prop -> prop instanceof PushdownRelProp).
                map(PushdownRelProp.class::cast).collect(Collectors.toList());

        EPropGroup ePropGroup = new EPropGroup(pushdownProps.stream().map(prop -> EProp.of(prop.getpType(), prop.geteNum(), prop.getCon())).collect(Collectors.toList()));
        return getNodeFilterStatistics(entity, ePropGroup);
    }

    @Override
    public long getGlobalSelectivity(Rel rel, RelPropGroup filter, EBase entity, Direction direction) {
        return 0;
    }

    private List<String> getRelevantIndicesForEdge(RelPropGroup relPropGroup, GraphEdgeSchema graphEdgeSchema) {
        IndexPartition indexPartition = graphEdgeSchema.getIndexPartition();
        List<String> relevantIndices = Lists.newArrayList(indexPartition.getIndices());
        if(indexPartition instanceof TimeSeriesIndexPartition){
            relevantIndices = findRelevantTimeSeriesIndices((TimeSeriesIndexPartition) indexPartition ,relPropGroup);
        }
        return relevantIndices;
    }

    private Statistics.Cardinality getEdgeStatistics(GraphEdgeSchema edgeSchema) {
        return graphStatisticsProvider.getEdgeCardinality(edgeSchema);
    }

    private Statistics.Cardinality getEdgeStatistics(GraphEdgeSchema edgeSchema, List<String> relevantIndices) {
        return graphStatisticsProvider.getEdgeCardinality(edgeSchema, relevantIndices);
    }

    private Statistics.Cardinality getVertexStatistics(String vertexType) {
        return graphStatisticsProvider.getVertexCardinality(graphElementSchemaProvider.getVertexSchema(vertexType).get());
    }

    private Statistics.Cardinality getVertexStatistics(GraphVertexSchema graphVertexSchema, List<String> relevantIndices) {
        return graphStatisticsProvider.getVertexCardinality(graphVertexSchema, relevantIndices);
    }

    private Statistics.Cardinality estimateVertexPropertyGroup(String vertexType, EPropGroup entityFilter) {
        GraphVertexSchema graphVertexSchema = graphElementSchemaProvider.getVertexSchema(vertexType).get();
        List<String> relevantIndices = getVertexRelevantIndices(entityFilter, graphVertexSchema);

        // This part assumes that all filter conditions are under an AND condition, so the estimation is the minimum.
        // When we add an OR condition (and a complex condition tree), we need to take a different approach
        Statistics.Cardinality minVertexCardinality = getVertexStatistics(graphVertexSchema, relevantIndices);
        for(EProp eProp : entityFilter.getProps()){
            Property property = OntologyUtil.getProperty(ontology, Integer.parseInt( eProp.getpType())).get();
            Optional<GraphElementPropertySchema> graphElementPropertySchema = graphVertexSchema.getProperty(property.getName());
            if(graphElementPropertySchema.isPresent()) {
                Optional<Statistics.Cardinality> conditionCardinality = getConditionCardinality(graphVertexSchema, graphElementPropertySchema.get(), eProp.getCon(), relevantIndices, property.getType());
                if (conditionCardinality.isPresent() && minVertexCardinality.getTotal() > conditionCardinality.get().getTotal())
                    minVertexCardinality = conditionCardinality.get();

            }else{
                // If a property does not exist on the vertex, we return 0 cardinality (again, assuming AND behavior)
                return new Statistics.Cardinality(0,0);
            }
        }
        return minVertexCardinality;
    }

    private Statistics.Cardinality estimateVertexRedundantPropertyGroup(String vertexType, EPropGroup entityFilter, Rel rel) {
        GraphVertexSchema graphVertexSchema = graphElementSchemaProvider.getVertexSchema(vertexType).get();
        List<String> relevantIndices = getVertexRelevantIndices(entityFilter, graphVertexSchema);
        GraphEdgeSchema graphEdgeSchema = graphElementSchemaProvider.getEdgeSchema(OntologyUtil.getRelationTypeNameById(ontology, rel.getrType())).get();

        // This part assumes that all filter conditions are under an AND condition, so the estimation is the minimum.
        // When we add an OR condition (and a complex condition tree), we need to take a different approach
        Statistics.Cardinality minVertexCardinality = getVertexStatistics(graphVertexSchema, relevantIndices);
        for(EProp eProp : entityFilter.getProps()){
            Property property = OntologyUtil.getProperty(ontology, Integer.parseInt( eProp.getpType())).get();
            Optional<GraphElementPropertySchema> graphElementPropertySchema = graphVertexSchema.getProperty(property.getName());
            if(graphElementPropertySchema.isPresent() && graphEdgeSchema.getDestination().get().getRedundantVertexProperty(graphElementPropertySchema.get().getName()).isPresent()) {
                Optional<Statistics.Cardinality> conditionCardinality = getConditionCardinality(graphVertexSchema, graphElementPropertySchema.get(), eProp.getCon(), relevantIndices, property.getType());
                if (conditionCardinality.isPresent() && minVertexCardinality.getTotal() > conditionCardinality.get().getTotal())
                    minVertexCardinality = conditionCardinality.get();
            }
        }
        return minVertexCardinality == null ? new Statistics.Cardinality(0,0): minVertexCardinality;
    }

    private List<String> getVertexRelevantIndices(EPropGroup entityFilter, GraphVertexSchema graphVertexSchema) {
        IndexPartition indexPartition = graphVertexSchema.getIndexPartition();
        List<String> relevantIndices = Lists.newArrayList(indexPartition.getIndices());
        if(indexPartition instanceof TimeSeriesIndexPartition){
            relevantIndices = findRelevantTimeSeriesIndices((TimeSeriesIndexPartition)indexPartition, entityFilter);
        }
        return relevantIndices;
    }

    private Optional<Statistics.Cardinality> getConditionCardinality(GraphVertexSchema graphVertexSchema,
                                                                     GraphElementPropertySchema graphElementPropertySchema,
                                                                     Constraint constraint,
                                                                     List<String> relevantIndices,
                                                                     String pType) {

        if(!supportedOps.contains(constraint.getOp())){
            return Optional.empty();
        }

        Optional<PrimitiveType> primitiveType = OntologyUtil.getPrimitiveType(ontology, pType);
        if(primitiveType.isPresent()) {
            return getValueConditionCardinality(graphVertexSchema, graphElementPropertySchema, constraint, constraint.getExpr(), relevantIndices, primitiveType.get().getJavaType());
        }else{
            Optional<EnumeratedType> enumeratedType = OntologyUtil.getEnumeratedType(ontology, graphElementPropertySchema.getType());
            if(enumeratedType.isPresent()) {
                Value value = (Value) constraint.getExpr();
                return getValueConditionCardinality(graphVertexSchema, graphElementPropertySchema, constraint, value.getName(), relevantIndices, String.class);
            }
        }
        return Optional.empty();
    }

    private Optional<Statistics.Cardinality> getConditionCardinality(GraphEdgeSchema graphEdgeSchema,
                                                                     GraphElementPropertySchema graphElementPropertySchema,
                                                                     Constraint constraint,
                                                                     List<String> relevantIndices,
                                                                     String pType) {

        if(!supportedOps.contains(constraint.getOp())){
            return Optional.empty();
        }

        Optional<PrimitiveType> primitiveType = OntologyUtil.getPrimitiveType(ontology, pType);
        if(primitiveType.isPresent()) {
            return getValueConditionCardinality(graphEdgeSchema, graphElementPropertySchema, constraint, constraint.getExpr(), relevantIndices, primitiveType.get().getJavaType());
        }else{
            Optional<EnumeratedType> enumeratedType = OntologyUtil.getEnumeratedType(ontology, graphElementPropertySchema.getType());
            if(enumeratedType.isPresent()) {
                Value value = (Value) constraint.getExpr();
                return getValueConditionCardinality(graphEdgeSchema, graphElementPropertySchema, constraint, value.getName(), relevantIndices, String.class);
            }
        }
        return Optional.empty();
    }

    private <T extends Comparable<T>> Optional<Statistics.Cardinality> getValueConditionCardinality(GraphElementSchema graphElementSchema, GraphElementPropertySchema graphElementPropertySchema, Constraint constraintOp, Object expression, List<String> relevantIndices, Class<T> tp) {
        Statistics.HistogramStatistics<T> histogramStatistics = null;
        if(tp.isInstance(expression) ){
            T expr = (T) expression;
            histogramStatistics = graphStatisticsProvider.getConditionHistogram(graphElementSchema, relevantIndices, graphElementPropertySchema, constraintOp, expr);
        }
        else if (expression instanceof List)
        {
            List<T> values = (List<T>) expression;
            histogramStatistics = graphStatisticsProvider.getConditionHistogram(graphElementSchema, relevantIndices, graphElementPropertySchema, constraintOp, values);
        }
        if(histogramStatistics != null) {
            return Optional.of(estimateCardinality(histogramStatistics, expression, constraintOp));
        }
        return Optional.empty();
    }

    private <T extends Comparable<T>> Statistics.Cardinality estimateCardinality(Statistics.HistogramStatistics<T> histogramStatistics, Object value, Constraint constraint){
        Statistics.Cardinality cardinality = null;
        switch(constraint.getOp()){
            case eq:
                Optional<Statistics.BucketInfo<T>> bucketContaining = histogramStatistics.findBucketContaining((T)value);
                cardinality = bucketContaining.map(tBucketInfo -> new Statistics.Cardinality(((double)tBucketInfo.getTotal()) / tBucketInfo.getCardinality(), 1)).
                        orElseGet(() -> new Statistics.Cardinality(0, 0));
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
                    bucketContaining = histogramStatistics.findBucketContaining((T)v);
                    total += ((double)bucketContaining.get().getTotal()) / bucketContaining.get().getCardinality();
                    count += 1;
                }
                return new Statistics.Cardinality(total,count);
            case notInSet:
                valueList = (List<T>) value;
                bucketInfos = histogramStatistics.getBuckets();

                for(T v : valueList){
                    bucketContaining = histogramStatistics.findBucketContaining((T)v);
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
        return cardinality;
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

    private <T extends Comparable<T>> Statistics.Cardinality estimateRange(List<Statistics.BucketInfo<T>> bucketsAbove, List<Statistics.BucketInfo<T>> bucketsBelow, List<T> valueList, String iType) {
        List<Statistics.BucketInfo<T>> joinedBuckets = new LinkedList<>(bucketsAbove);
        joinedBuckets.retainAll(bucketsBelow);
        if(joinedBuckets.size() > 0){
            Statistics.Cardinality cardinality = estimateGreaterThan(joinedBuckets, valueList.get(0), iType.startsWith("["));
            Statistics.Cardinality greaterCardinality = estimateLessThan(joinedBuckets.subList(joinedBuckets.size() - 1, joinedBuckets.size()), valueList.get(1), iType.endsWith("]"));
            Statistics.BucketInfo<T> last = Iterables.getLast(joinedBuckets);
            return new Statistics.Cardinality(cardinality.getTotal() + greaterCardinality.getTotal() - last.getTotal(),  cardinality.getCardinality() + greaterCardinality.getCardinality() - last.getCardinality());

        }
        return mergeBucketsCardinality(joinedBuckets);
    }

    // Currently lt and lte have the same costs
    // Also, in case we have a non numeric/date value, we take a pessimistic estimate of the bucket containing the given value (entire bucket, not relative part)
    private <T extends Comparable<T>> Statistics.Cardinality estimateLessThan(List<Statistics.BucketInfo<T>> bucketsBelow, T value, boolean inclusive) {
        if(bucketsBelow.size() == 0)
            return new Statistics.Cardinality(0,0);

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
            Statistics.Cardinality cardinality = mergeBucketsCardinality(bucketsBelow.subList(0, bucketsBelow.size() - 1));
            return new Statistics.Cardinality(cardinality.getTotal() + lastBucket.getTotal() * partialBucket, cardinality.getCardinality() + lastBucket.getCardinality()*partialBucket);
        }
        return mergeBucketsCardinality(bucketsBelow);
    }

    private <T extends Comparable<T>> Statistics.Cardinality estimateGreaterThan(List<Statistics.BucketInfo<T>> bucketsAbove, T value, boolean inclusive) {
        if (bucketsAbove.size() == 0)
            return new Statistics.Cardinality(0,0);
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
            Statistics.Cardinality cardinality = mergeBucketsCardinality(bucketsAbove.subList(1, bucketsAbove.size()));
            return new Statistics.Cardinality(cardinality.getTotal() + firstBucket.getTotal() * partialBucket, cardinality.getCardinality() + firstBucket.getCardinality()*partialBucket);
        }
        return mergeBucketsCardinality(bucketsAbove);
    }

    private List<String> findRelevantTimeSeriesIndices(TimeSeriesIndexPartition indexPartition, EPropGroup entityFilter) {
        List<EProp> timeConditions = new ArrayList<>();
        for (EProp eProp : entityFilter.getProps()){
            Property property = OntologyUtil.getProperty(ontology, Integer.parseInt(eProp.getpType())).get();
            if(property.getName().equals(indexPartition.getTimeField())){
                switch(eProp.getCon().getOp()){
                    case inRange:
                        List<Date> values = (List<Date>)eProp.getCon().getExpr();
                        timeConditions.add(EProp.of(eProp.getpType(), 0, Constraint.of(eProp.getCon().getiType().startsWith("[")? ConstraintOp.ge: ConstraintOp.gt, values.get(0))));
                        timeConditions.add(EProp.of(eProp.getpType(), 0, Constraint.of(eProp.getCon().getiType().startsWith("]")? ConstraintOp.le: ConstraintOp.lt, values.get(1))));
                        break;
                    case inSet:
                        values = (List<Date>)eProp.getCon().getExpr();
                        for(Date value : values){
                            timeConditions.add(EProp.of(eProp.getpType(), 0, Constraint.of(ConstraintOp.eq, value)));
                        }
                        break;
                    case notInSet:
                        values = (List<Date>)eProp.getCon().getExpr();
                        for(Date value : values){
                            timeConditions.add(EProp.of(eProp.getpType(), 0, Constraint.of(ConstraintOp.ne, value)));
                        }
                        break;
                    default:
                        timeConditions.add(eProp);
                        break;
                }
            }
        }

        if(timeConditions.size() == 0)
            return Lists.newArrayList(indexPartition.getIndices());

        List<String> relevantIndices = Lists.newArrayList(indexPartition.getIndices());

        for(EProp timeCondition : timeConditions) {
            String indexName = indexPartition.getIndexName((Date) timeCondition.getCon().getExpr());
            relevantIndices.removeAll(findIndicesToRemove(timeCondition.getCon(), relevantIndices, indexName));
        }
        return relevantIndices;

    }

    private List<String> findRelevantTimeSeriesIndices(TimeSeriesIndexPartition indexPartition,RelPropGroup relPropGroup) {
        List<RelProp> timeConditions = new ArrayList<>();
        for (RelProp relProp : relPropGroup.getProps()){
            if(OntologyUtil.getProperty(ontology, Integer.parseInt(relProp.getpType())).get().getName().equals(indexPartition.getTimeField())){

                switch(relProp.getCon().getOp()){
                    case inRange:
                        List<Date> values = (List<Date>)relProp.getCon().getExpr();
                        timeConditions.add(RelProp.of(relProp.getpType(), 0, Constraint.of(relProp.getCon().getiType().startsWith("[")? ConstraintOp.ge: ConstraintOp.gt, values.get(0))));
                        timeConditions.add(RelProp.of(relProp.getpType(), 0, Constraint.of(relProp.getCon().getiType().startsWith("]")? ConstraintOp.le: ConstraintOp.lt, values.get(1))));
                        break;
                    case inSet:
                        values = (List<Date>)relProp.getCon().getExpr();
                        for(Date value : values){
                            timeConditions.add(RelProp.of(relProp.getpType(), 0, Constraint.of(ConstraintOp.eq, value)));
                        }
                        break;
                    case notInSet:
                        values = (List<Date>)relProp.getCon().getExpr();
                        for(Date value : values){
                            timeConditions.add(RelProp.of(relProp.getpType(), 0, Constraint.of(ConstraintOp.ne, value)));
                        }
                        break;
                    default:
                        timeConditions.add(relProp);
                        break;
                }
            }
        }

        if(timeConditions.size() == 0)
            return Lists.newArrayList(indexPartition.getIndices());

        List<String> relevantIndices = Lists.newArrayList(indexPartition.getIndices());

        for(RelProp timeCondition : timeConditions) {

            String indexName = indexPartition.getIndexName((Date) timeCondition.getCon().getExpr());
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

    private <T extends Comparable<T>> Statistics.Cardinality mergeBucketsCardinality(List<Statistics.BucketInfo<T>> buckets){
        Statistics.Cardinality cardinality = new Statistics.Cardinality(0,0);
        for(Statistics.BucketInfo<T> bucketInfo : buckets){
            cardinality = (Statistics.Cardinality) cardinality.merge(bucketInfo.getCardinalityObject());
        }
        return cardinality;
    }

}
