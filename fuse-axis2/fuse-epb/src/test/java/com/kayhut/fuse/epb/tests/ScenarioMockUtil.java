package com.kayhut.fuse.epb.tests;

import com.kayhut.fuse.epb.plan.statistics.GraphStatisticsProvider;
import com.kayhut.fuse.model.OntologyTestUtils;
import com.kayhut.fuse.model.ontology.Ontology;
import com.kayhut.fuse.unipop.schemaProviders.*;
import com.kayhut.fuse.unipop.schemaProviders.indexPartitions.IndexPartition;
import com.kayhut.fuse.unipop.schemaProviders.indexPartitions.TimeSeriesIndexPartition;
import com.kayhut.fuse.unipop.structure.ElementType;
import org.elasticsearch.common.collect.Tuple;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by moti on 5/17/2017.
 */
public class ScenarioMockUtil {
    private Ontology ontology;
    private PhysicalIndexProvider indexProvider;
    private GraphElementSchemaProvider graphElementSchemaProvider;
    private OntologyGraphLayoutProvider ontologyGraphLayoutProvider = null;
    private Map<String, Map<String, String>> redundantProps = new HashMap<>();
    private Map<Tuple<String, ElementType>, IndexPartition> indexPartitionMap = new HashMap<>();
    private static String INDEX_PREFIX = "idx-";
    private static String INDEX_FORMAT = "idx-%s";
    private static String DATE_FORMAT_STRING = "yyyy-MM-dd-HH";
    private static SimpleDateFormat DATE_FORMAT = new SimpleDateFormat(DATE_FORMAT_STRING);
    private long scenarioTime = new Date().getTime();
    private GraphStatisticsProvider graphStatisticsProvider;
    private Map<String, Long> globalSelectivity = new HashMap<>();


    public ScenarioMockUtil() {
        this.ontologyGraphLayoutProvider = mock(OntologyGraphLayoutProvider.class);
        when(this.ontologyGraphLayoutProvider.getRedundantVertexProperty(any(), any())).thenAnswer(invocationOnMock -> {
            String edgeType = invocationOnMock.getArgumentAt(0, String.class);
            String property = invocationOnMock.getArgumentAt(1, String.class);
            if(redundantProps.containsKey(edgeType)){
                if(redundantProps.get(edgeType).containsKey(property)){
                    return Optional.of(redundantProps.get(edgeType).get(property));
                }
            }
            return Optional.empty();
        });

        when(this.ontologyGraphLayoutProvider.getRedundantVertexPropertyByPushdownName(any(), any())).thenAnswer(invocationOnMock -> {
            String edgeType = invocationOnMock.getArgumentAt(0, String.class);
            String property = invocationOnMock.getArgumentAt(1, String.class);
            if(redundantProps.containsKey(edgeType)){
                if(redundantProps.get(edgeType).containsValue(property)){
                    return redundantProps.get(edgeType).entrySet().stream().filter(set -> set.getValue().equals(property)).map(Map.Entry::getKey).findFirst();
                }
            }
            return Optional.empty();
        });

        this.ontology = OntologyTestUtils.createDragonsOntologyShort();

        this.indexProvider = mock(PhysicalIndexProvider.class);
        when(this.indexProvider.getIndexPartitionByLabel(any(), any())).thenAnswer(invocationOnMock -> {
            String label = invocationOnMock.getArgumentAt(0, String.class);
            ElementType elementType = invocationOnMock.getArgumentAt(1, ElementType.class);
            Tuple<String, ElementType> item = new Tuple<>(label, elementType);
            return indexPartitionMap.getOrDefault(item, null);
        });

        this.graphElementSchemaProvider = new OntologySchemaProvider(this.indexProvider, this.ontology, this.ontologyGraphLayoutProvider);

        this.graphStatisticsProvider = mock(GraphStatisticsProvider.class);
        when(graphStatisticsProvider.getGlobalSelectivity(any(), any())).thenAnswer(invocationOnMock -> {
            GraphEdgeSchema schema = invocationOnMock.getArgumentAt(0, GraphEdgeSchema.class);
            List<String> indices = (List<String>) invocationOnMock.getArgumentAt(1, List.class);
            Long globalSelectivity = this.globalSelectivity.getOrDefault(schema.getType(), 10L);
            return globalSelectivity*indices.size();
        });


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
        return this;
    }

    public ScenarioMockUtil withGlobalSelectivity(String edgeType, long gs){
        globalSelectivity.put(edgeType, gs);
        return this;
    }

    public static ScenarioMockUtil start(){
        return new ScenarioMockUtil();
    }
}
