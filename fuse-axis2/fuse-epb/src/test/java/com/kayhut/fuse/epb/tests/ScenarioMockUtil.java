package com.kayhut.fuse.epb.tests;

import com.kayhut.fuse.epb.plan.statistics.GraphStatisticsProvider;
import com.kayhut.fuse.epb.plan.statistics.Statistics;
import com.kayhut.fuse.model.OntologyTestUtils;
import com.kayhut.fuse.model.ontology.Ontology;
import com.kayhut.fuse.unipop.schemaProviders.*;
import com.kayhut.fuse.unipop.schemaProviders.indexPartitions.IndexPartition;
import com.kayhut.fuse.unipop.schemaProviders.indexPartitions.TimeSeriesIndexPartition;
import com.kayhut.fuse.unipop.structure.ElementType;
import javaslang.collection.Stream;
import org.elasticsearch.common.collect.Tuple;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by moti on 5/17/2017.
 */
public class ScenarioMockUtil {
    private Ontology.Accessor ont;
    private PhysicalIndexProvider indexProvider;
    private GraphElementSchemaProvider graphElementSchemaProvider;
    private GraphLayoutProvider graphLayoutProvider = null;
    private Map<String, Map<String, String>> redundantProps = new HashMap<>();
    private Map<Tuple<String, ElementType>, IndexPartition> indexPartitionMap = new HashMap<>();
    private static String INDEX_PREFIX = "idx-";
    private static String INDEX_FORMAT = "idx-%s";
    private static String DATE_FORMAT_STRING = "yyyy-MM-dd-HH";
    private static SimpleDateFormat DATE_FORMAT = new SimpleDateFormat(DATE_FORMAT_STRING);
    private long scenarioTime = new Date().getTime();
    private GraphStatisticsProvider graphStatisticsProvider;
    private Map<String, Long> globalSelectivity = new HashMap<>();
    private Map<String, Long> cardinalityPerTypePerIndex = new HashMap<>();
    private Map<String, Statistics.HistogramStatistics> histogramPerPropPerIndex = new HashMap<>();

    private long nodeScaleFactor;
    private long edgeScaleFactor;

    public ScenarioMockUtil(long nodeScaleFactor, long edgeScaleFactor) {
        this.nodeScaleFactor = nodeScaleFactor;
        this.edgeScaleFactor = edgeScaleFactor;
        this.graphLayoutProvider = mock(GraphLayoutProvider.class);
        when(this.graphLayoutProvider.getRedundantProperty(any(), any())).thenAnswer(invocationOnMock -> {
            String edgeType = invocationOnMock.getArgumentAt(0, String.class);
            String property = invocationOnMock.getArgumentAt(1, String.class);
            if(redundantProps.containsKey(edgeType)){
                if(redundantProps.get(edgeType).containsKey(property)){
                    return Optional.of(redundantProps.get(edgeType).get(property));
                }
            }
            return Optional.empty();
        });

        this.ont = new Ontology.Accessor(OntologyTestUtils.createDragonsOntologyShort());

        this.indexProvider = mock(PhysicalIndexProvider.class);
        IndexPartition defaultPartition = () -> Arrays.asList("idx1");
        when(this.indexProvider.getIndexPartitionByLabel(any(), any())).thenAnswer(invocationOnMock -> {
            String label = invocationOnMock.getArgumentAt(0, String.class);
            ElementType elementType = invocationOnMock.getArgumentAt(1, ElementType.class);
            Tuple<String, ElementType> item = new Tuple<>(label, elementType);
            return indexPartitionMap.getOrDefault(item, defaultPartition);
        });

        this.graphElementSchemaProvider = new OntologySchemaProvider(this.ont.get(), this.indexProvider, this.graphLayoutProvider);

        this.graphStatisticsProvider = mock(GraphStatisticsProvider.class);
        when(graphStatisticsProvider.getGlobalSelectivity(any(), any(), any())).thenAnswer(invocationOnMock -> {
            GraphEdgeSchema schema = invocationOnMock.getArgumentAt(0, GraphEdgeSchema.class);
            List<String> indices = (List<String>) invocationOnMock.getArgumentAt(2, List.class);
            Long globalSelectivity = this.globalSelectivity.getOrDefault(schema.getType(), 10L);
            return globalSelectivity*indices.size();
        });

        when(graphStatisticsProvider.getVertexCardinality(any())).thenAnswer(invocationOnMock -> {
            GraphVertexSchema vertex = invocationOnMock.getArgumentAt(0, GraphVertexSchema.class);
            IndexPartition indexPartition = indexProvider.getIndexPartitionByLabel(vertex.getType(), ElementType.vertex);
            return graphStatisticsProvider.getVertexCardinality(vertex, Stream.ofAll(indexPartition.getIndices()).toJavaList());
        });

        when(graphStatisticsProvider.getVertexCardinality(any(), any())).thenAnswer(invocationOnMock -> {
            GraphVertexSchema graphVertexSchema = invocationOnMock.getArgumentAt(0, GraphVertexSchema.class);
            List<String> indices = invocationOnMock.getArgumentAt(1, List.class);
            return getCardinality(graphVertexSchema, indices,nodeScaleFactor);
        });

        when(graphStatisticsProvider.getEdgeCardinality(any())).thenAnswer(invocationOnMock -> {
            GraphEdgeSchema edge = invocationOnMock.getArgumentAt(0, GraphEdgeSchema.class);
            IndexPartition indexPartition = indexProvider.getIndexPartitionByLabel(edge.getType(), ElementType.edge);
            return graphStatisticsProvider.getEdgeCardinality(edge, Stream.ofAll(indexPartition.getIndices()).toJavaList());
        });

        when(graphStatisticsProvider.getEdgeCardinality(any(), any())).thenAnswer(invocationOnMock -> {
            GraphEdgeSchema edge = invocationOnMock.getArgumentAt(0, GraphEdgeSchema.class);
            List<String> indices = invocationOnMock.getArgumentAt(1, List.class);
            return getCardinality(edge, indices,edgeScaleFactor);
        });

        when(graphStatisticsProvider.getConditionHistogram(any(), any(), any(), any(), isA(List.class))).thenAnswer(invocationOnMock -> {
            List<String> indices = invocationOnMock.getArgumentAt(1, List.class);
            GraphElementPropertySchema propertySchema = invocationOnMock.getArgumentAt(2, GraphElementPropertySchema.class);
            if(propertySchema.getType().equals("string")) {
                List<Statistics.HistogramStatistics<String>> histograms = IntStream.range(0, indices.size()).mapToObj(i -> (Statistics.HistogramStatistics<String>)histogramPerPropPerIndex.get(propertySchema.getName())).collect(Collectors.toList());
                return Statistics.HistogramStatistics.combine(histograms);
            }

            if(propertySchema.getType().equals("date")){
                List<Statistics.HistogramStatistics<Date>> histograms = IntStream.range(0, indices.size()).mapToObj(i -> (Statistics.HistogramStatistics<Date>)histogramPerPropPerIndex.get(propertySchema.getName())).collect(Collectors.toList());
                return Statistics.HistogramStatistics.combine(histograms);
            }

            List<Statistics.HistogramStatistics<Long>> histograms = IntStream.range(0, indices.size()).mapToObj(i -> (Statistics.HistogramStatistics<Long>)histogramPerPropPerIndex.get(propertySchema.getName())).collect(Collectors.toList());
            return Statistics.HistogramStatistics.combine(histograms);
        });

        when(graphStatisticsProvider.getConditionHistogram(any(), any(), any(), any(), isA(String.class))).thenAnswer(invocationOnMock -> {
            List<String> indices = invocationOnMock.getArgumentAt(1, List.class);
            GraphElementPropertySchema propertySchema = invocationOnMock.getArgumentAt(2, GraphElementPropertySchema.class);

                List<Statistics.HistogramStatistics<String>> histograms = IntStream.range(0, indices.size()).mapToObj(i -> (Statistics.HistogramStatistics<String>)histogramPerPropPerIndex.get(propertySchema.getName())).collect(Collectors.toList());
                return Statistics.HistogramStatistics.combine(histograms);
        });

        when(graphStatisticsProvider.getConditionHistogram(any(), any(), any(), any(), isA(Date.class))).thenAnswer(invocationOnMock -> {
            List<String> indices = invocationOnMock.getArgumentAt(1, List.class);
            GraphElementPropertySchema propertySchema = invocationOnMock.getArgumentAt(2, GraphElementPropertySchema.class);

            List<Statistics.HistogramStatistics<Date>> histograms = IntStream.range(0, indices.size()).mapToObj(i -> (Statistics.HistogramStatistics<Date>)histogramPerPropPerIndex.get(propertySchema.getName())).collect(Collectors.toList());
            return Statistics.HistogramStatistics.combine(histograms);
        });

        when(graphStatisticsProvider.getConditionHistogram(any(), any(), any(), any(), isA(Long.class))).thenAnswer(invocationOnMock -> {
            List<String> indices = invocationOnMock.getArgumentAt(1, List.class);
            GraphElementPropertySchema propertySchema = invocationOnMock.getArgumentAt(2, GraphElementPropertySchema.class);

            List<Statistics.HistogramStatistics<Long>> histograms = IntStream.range(0, indices.size()).mapToObj(i -> (Statistics.HistogramStatistics<Long>)histogramPerPropPerIndex.get(propertySchema.getName())).collect(Collectors.toList());
            return Statistics.HistogramStatistics.combine(histograms);
        });
    }

    private Statistics.Cardinality getCardinality(GraphElementSchema graphElementSchema, List<String> indices,long scale) {
        return new Statistics.Cardinality(cardinalityPerTypePerIndex.getOrDefault(graphElementSchema.getType(), 1000l)*indices.size()* scale,
                cardinalityPerTypePerIndex.getOrDefault(graphElementSchema.getType(), 1000l)*indices.size());
    }

    public ScenarioMockUtil withLayoutRedundancy(String edgeType, String propertyName, String redundantPropertyName){
        if(!redundantProps.containsKey(edgeType)){
            redundantProps.put(edgeType, new HashMap<>());
        }
        redundantProps.get(edgeType).put(propertyName, redundantPropertyName);
        return this;
    }

    public ScenarioMockUtil withTimeSeriesIndex(String type, ElementType elementType, String timeField, int numIndices){
        TimeSeriesIndexPartition indexPartition = new TimeSeriesIndexPartition() {
            @Override
            public String getDateFormat() {
                return DATE_FORMAT_STRING;
            }

            @Override
            public String getIndexPrefix() {
                return type + INDEX_PREFIX;
            }

            @Override
            public String getIndexFormat() {
                return type + INDEX_FORMAT;
            }

            @Override
            public String getTimeField() {
                return timeField;
            }

            @Override
            public String getIndexName(Date date) {
                return String.format(getIndexFormat(), DATE_FORMAT.format(date));
            }

            @Override
            public Iterable<String> getIndices() {
                return IntStream.range(0, numIndices).mapToObj(i -> new Date(scenarioTime - 60*60*1000 * i)).
                        map(this::getIndexName).collect(Collectors.toList());

            }
        };
        indexPartitionMap.put(new Tuple<>(type, elementType), indexPartition);

        for(int i = 0;i<numIndices;i++){

        }

        return this;
    }

    public ScenarioMockUtil withGlobalSelectivity(String edgeType, long gs){
        globalSelectivity.put(edgeType, gs);
        return this;
    }

    public ScenarioMockUtil withElementCardinality(String type, Long cardinality){
        cardinalityPerTypePerIndex.put(type, cardinality);
        return this;
    }

    public ScenarioMockUtil withHistogram(String prop, Statistics.HistogramStatistics histogram){
        histogramPerPropPerIndex.put(prop, histogram);
        return this;
    }

    public static ScenarioMockUtil start(long nodeScaleFactor, long edgeScaleFactor){
        return new ScenarioMockUtil(nodeScaleFactor,edgeScaleFactor);
    }


    public Ontology.Accessor getOntologyAccessor() {
        return ont;
    }

    public PhysicalIndexProvider getIndexProvider() {
        return indexProvider;
    }

    public GraphElementSchemaProvider getGraphElementSchemaProvider() {
        return graphElementSchemaProvider;
    }

    public long getScenarioTime() {
        return scenarioTime;
    }

    public GraphStatisticsProvider getGraphStatisticsProvider() {
        return graphStatisticsProvider;
    }

    public Map<String, Long> getGlobalSelectivity() {
        return globalSelectivity;
    }
}
