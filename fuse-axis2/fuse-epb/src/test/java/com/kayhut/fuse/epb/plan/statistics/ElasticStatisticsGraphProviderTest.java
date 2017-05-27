package com.kayhut.fuse.epb.plan.statistics;

import com.google.common.collect.ImmutableList;
import com.kayhut.fuse.epb.plan.statistics.configuration.StatConfig;
import com.kayhut.fuse.epb.plan.statistics.provider.ElasticClientProvider;
import com.kayhut.fuse.epb.plan.statistics.provider.ElasticStatisticsGraphProvider;
import com.kayhut.fuse.model.ontology.*;
import com.kayhut.fuse.stat.es.populator.ElasticDataPopulator;
import com.kayhut.fuse.stat.model.bucket.BucketRange;
import com.kayhut.fuse.stat.model.bucket.BucketTerm;
import com.kayhut.fuse.stat.model.configuration.Field;
import com.kayhut.fuse.stat.model.configuration.Mapping;
import com.kayhut.fuse.stat.model.configuration.StatContainer;
import com.kayhut.fuse.stat.model.configuration.Type;
import com.kayhut.fuse.stat.model.enums.DataType;
import com.kayhut.fuse.stat.model.histogram.*;
import com.kayhut.fuse.stat.model.result.StatRangeResult;
import com.kayhut.fuse.stat.model.result.StatTermResult;
import com.kayhut.fuse.stat.util.EsUtil;
import com.kayhut.fuse.stat.util.StatUtil;
import com.kayhut.fuse.unipop.schemaProviders.GraphVertexSchema;
import com.kayhut.fuse.unipop.schemaProviders.OntologySchemaProvider;
import com.kayhut.fuse.unipop.structure.ElementType;
import com.kayhut.test.framework.index.ElasticEmbeddedNode;
import javaslang.Tuple2;
import org.elasticsearch.client.transport.TransportClient;
import org.junit.*;
import org.mockito.Mockito;

import java.io.IOException;
import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

/**
 * Created by benishue on 25-May-17.
 */
public class ElasticStatisticsGraphProviderTest {

    private static TransportClient statClient;
    private static ElasticEmbeddedNode elasticEmbeddedNode;
    private static StatConfig statConfig;
    static final long NUM_OF_DRAGONS_IN_INDEX_1 = 1000L;
    static final long NUM_OF_DRAGONS_IN_INDEX_2 = 555L; //HAMSA HAMSA HAMSA
    static final String STAT_INDEX_NAME = "stat";
    static final String STAT_TERM_TYPE_NAME = "bucketTerm";
    static final String STAT_STRING_TYPE_NAME = "bucketString";
    static final String STAT_NUMERIC_TYPE_NAME = "bucketNumeric";
    static final String STAT_COUNT_FIELD_NAME = "count";
    static final String STAT_CARDINALITY_FIELD_NAME = "cardinality";
    static final String DRAGON_TYPE_NAME = "Dragon";
    static final String AGE_FIELD_NAME = "age";
    static final List<String> VERTEX_INDICES = ImmutableList.of("vertexIndex1", "vertexIndex2");
    static final List<String> EDGE_INDICES = ImmutableList.of("edgeIndex1", "edgeIndex2");


    @Test
    public void getVertexCardinality() throws Exception {
        OntologySchemaProvider ontologySchemaProvider = getOntologySchemaProvider(getOntology());
        GraphVertexSchema vertexDragonSchema = ontologySchemaProvider.getVertexSchema("Dragon").get();
        ElasticStatisticsGraphProvider statisticsGraphProvider = new ElasticStatisticsGraphProvider(statConfig);

        Map<String, Tuple2<Long, Long>> termStatistics = new HashMap<>();
        //We have 1000 dragons in index1, cardinality is 1
        termStatistics.put(DRAGON_TYPE_NAME, new Tuple2<>(NUM_OF_DRAGONS_IN_INDEX_1, 1L));

        //Populating the Elastic Stat Engine index: 'stat' type: 'termBucket', buckets of statistics
        populateTermStatDocs(VERTEX_INDICES, DRAGON_TYPE_NAME,"_type", termStatistics);
        //Checking that the ELASTIC STAT TERM TYPE created
        assertTrue(EsUtil.checkIfEsTypeExists(statClient, STAT_INDEX_NAME, STAT_TERM_TYPE_NAME));

        //Sanity Checks
        //We have only 1 index ('stat') in the STAT Elastic Engine
        String[] allIndices = EsUtil.getAllIndices(statClient);
        assertEquals(1, allIndices.length);
        assertEquals(allIndices[0], STAT_INDEX_NAME);

        //We have only 1 type in the Elastic Index 'stat' = 'termBucket'
        String[] allTypesFromIndex = EsUtil.getAllTypesFromIndex(statClient, STAT_INDEX_NAME);
        Arrays.asList(allTypesFromIndex).forEach(type -> System.out.println(type));
        assertEquals(1, allTypesFromIndex.length);
        assertEquals(STAT_TERM_TYPE_NAME, allTypesFromIndex[0]);

        //Check that the bucket term exists (the bucket is calculated on the field _type which is value is 'Dragon')
        String docId = StatUtil.hashString(VERTEX_INDICES.get(0) + DRAGON_TYPE_NAME + "_type"  + DRAGON_TYPE_NAME);
        Optional<Map<String, Object>> doc6Result = EsUtil.getDocumentSourceById(statClient, STAT_INDEX_NAME, STAT_TERM_TYPE_NAME, docId);
        assertTrue(doc6Result.isPresent());

        assertEquals(1, (int)doc6Result.get().get(STAT_CARDINALITY_FIELD_NAME));
        assertEquals((int)doc6Result.get().get(STAT_COUNT_FIELD_NAME), NUM_OF_DRAGONS_IN_INDEX_1);

        Statistics.Cardinality vertexCardinality = statisticsGraphProvider.getVertexCardinality(vertexDragonSchema);
    }

    @Test
    public void getVertexCardinality1() throws Exception {

    }

    @Test
    public void getEdgeCardinality() throws Exception {

    }

    @Test
    public void getEdgeCardinality1() throws Exception {

    }

    @Test
    public void getConditionHistogram() throws Exception {
//        populateNumericStatDocs(VERTEX_INDICES,
//                DRAGON_TYPE_NAME,
//                AGE_FIELD_NAME,
//                10L,
//                100L,
//                20);
//        Thread.sleep(2000);
    }

    @Test
    public void getConditionHistogram1() throws Exception {

    }

    @Test
    public void getGlobalSelectivity() throws Exception {

    }

    /**
     * (1) Starting Elastic engine in-memory
     * (2) Loading statistics configuration
     * @throws Exception
     */
    @BeforeClass
    public static void setup() throws Exception {

        statConfig = new StatConfig("fuse.test_elastic",
                Collections.singletonList("localhost"),
                9300,
                STAT_INDEX_NAME,
                STAT_TERM_TYPE_NAME,
                STAT_STRING_TYPE_NAME,
                STAT_NUMERIC_TYPE_NAME,
                STAT_COUNT_FIELD_NAME,
                STAT_CARDINALITY_FIELD_NAME,
                buildStatContainer());

        statClient = new ElasticClientProvider(statConfig).getStatClient();
        elasticEmbeddedNode = new ElasticEmbeddedNode();

        Thread.sleep(4000);
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
        if (statClient != null) {
            statClient.close();
            statClient = null;
        }

        elasticEmbeddedNode.close();
        Thread.sleep(4000);
    }

    private static void populateNumericStatDocs(List<String> indices, String type, String field, long min, long max, int numOfBins) throws IOException {
        new ElasticDataPopulator(
                statClient,
                STAT_INDEX_NAME,
                STAT_NUMERIC_TYPE_NAME,
                "id",
                () -> prepareNumericStatisticsDocs(
                        indices,
                        type,
                        field,
                        min,
                        max,
                        numOfBins
                )).populate();
    }

    /**
     * Populating elastic statistics documents for terms
     * @param indices Elastic Indices Names
     * @param type Elastic Type Name (e.g., Person)
     * @param field Elastic Field Name (e.g., gender, _type)
     * @param termStatistics Map <Key = 'Term', Tuple<Count, Cardinality>, (e.g < 'Key = female', Value = [500, 1] >
     */
    private static void populateTermStatDocs(List<String> indices, String type, String field, Map<String, Tuple2<Long, Long>> termStatistics) throws IOException {
        new ElasticDataPopulator(
                statClient,
                STAT_INDEX_NAME,
                STAT_TERM_TYPE_NAME,
                "id",
                () -> prepareTermStatisticsDocs(
                        indices,
                        type,
                        field,
                        termStatistics
                )).populate();
    }

    //region Private Methods
    private static StatContainer buildStatContainer() {
        HistogramNumeric histogramDragonAge = HistogramNumeric.HistogramNumericBuilder.aHistogramNumeric()
                .withMin(10).withMax(100).withNumOfBins(10).build();

        HistogramString histogramDragonName = HistogramString.HistogramStringBuilder.aHistogramString()
                .withPrefixSize(3)
                .withInterval(10).withNumOfChars(26).withFirstCharCode("97").build();

        HistogramManual histogramDragonAddress = HistogramManual.HistogramManualBuilder.aHistogramManual()
                .withBuckets(Arrays.asList(
                        new BucketRange("abc", "dzz"),
                        new BucketRange("efg", "hij"),
                        new BucketRange("klm", "xyz")
                )).withDataType(DataType.string)
                .build();

        HistogramComposite histogramDragonColor = HistogramComposite.HistogramCompositeBuilder.aHistogramComposite()
                .withManualBuckets(Arrays.asList(
                        new BucketRange("00", "11"),
                        new BucketRange("22", "33"),
                        new BucketRange("44", "55")
                )).withDataType(DataType.string)
                .withAutoBuckets(HistogramString.HistogramStringBuilder.aHistogramString()
                        .withFirstCharCode("97")
                        .withInterval(10)
                        .withNumOfChars(26)
                        .withPrefixSize(3).build())
                .build();

        HistogramTerm histogramTerm = HistogramTerm.HistogramTermBuilder.aHistogramTerm()
                .withDataType(DataType.string).withBuckets(Arrays.asList(
                        new BucketTerm("male"),
                        new BucketTerm("female")
                )).build();

        HistogramTerm histogramDocType = HistogramTerm.HistogramTermBuilder.aHistogramTerm()
                .withDataType(DataType.string).withBuckets(Collections.singletonList(
                        new BucketTerm("dragon")
                )).build();


        Type typeDragon = new Type("dragon", Arrays.asList(
                new Field("age", histogramDragonAge),
                new Field("name", histogramDragonName),
                new Field("address", histogramDragonAddress),
                new Field("color", histogramDragonColor),
                new Field("gender", histogramTerm),
                new Field("_type", histogramDocType)));

        Mapping mapping = Mapping.MappingBuilder.aMapping().withIndices(Arrays.asList("index1", "index2"))
                .withTypes(Collections.singletonList("dragon")).build();

        return StatContainer.StatContainerBuilder.aStatContainer()
                .withMappings(Collections.singletonList(mapping))
                .withTypes(Collections.singletonList(typeDragon))
                .build();
    }

    private OntologySchemaProvider getOntologySchemaProvider(Ontology ontology) {
        return new OntologySchemaProvider(ontology, (label, elementType) -> {
            if (elementType == ElementType.vertex) {
                return () -> VERTEX_INDICES;
            } else if (elementType == ElementType.edge) {
                return () -> EDGE_INDICES;
            } else {
                // must fail
                Assert.assertTrue(false);
                return null;
            }
        });
    }

    private Ontology getOntology() {
        Ontology ontology = Mockito.mock(Ontology.class);
        List<EPair> ePairs = Arrays.asList(new EPair() {{
            seteTypeA(2);
            seteTypeB(1);
        }});

        RelationshipType fireRelationshipType = RelationshipType.Builder.get()
                .withRType(1).withName("Fire").withEPairs(ePairs).build();

        Property nameProp = new Property("name", 1, "string");
        Property ageProp = new Property("age", 2, "int");

        when(ontology.getProperties()).then(invocationOnMock -> Collections.singletonList(nameProp));

        when(ontology.getEntityTypes()).thenAnswer(invocationOnMock ->
                {
                    ArrayList<EntityType> entityTypes = new ArrayList<>();
                    entityTypes.add(EntityType.EntityTypeBuilder.anEntityType()
                            .withEType(1).withName("Person")
                            .withProperties(Collections.singletonList(nameProp.getpType()))
                            .build());
                    entityTypes.add(EntityType.EntityTypeBuilder.anEntityType()
                            .withEType(2).withName("Dragon")
                            .withProperties(Collections.singletonList(ageProp.getpType()))
                            .build());
                    return entityTypes;
                }
        );

        when(ontology.getRelationshipTypes()).thenAnswer(invocationOnMock ->
                {
                    ArrayList<RelationshipType> relTypes = new ArrayList<>();
                    relTypes.add(fireRelationshipType);
                    return relTypes;
                }
        );

        return ontology;
    }

    /**
     * @param indices
     * @param type
     * @param field
     * @param min
     * @param max
     * @param numOfBins
     * @return List of documents where each numeric bucket count & cardinality = index of bucket
     */
    public static Iterable<Map<String, Object>> prepareNumericStatisticsDocs(List<String> indices,
                                                                             String type,
                                                                             String field,
                                                                             long min,
                                                                             long max,
                                                                             int numOfBins) {
        List<BucketRange<Double>> numericBuckets = StatUtil.createNumericBuckets(min, max, Math.toIntExact(numOfBins));
        List<StatRangeResult> statRangeResults = new ArrayList<>();
        int j = 0;
        for (BucketRange<Double> bucketRange : numericBuckets) {
            for (String index : indices) {
                StatRangeResult<Double> statRangeResult = new StatRangeResult<>
                        (index, type, field, Integer.toString(j), DataType.numeric, bucketRange.getStart(), bucketRange.getEnd(), j, j);
                statRangeResults.add(statRangeResult);
            }
            j++;
        }
        return StatUtil.prepareStatDocs(statRangeResults);
    }

    /**
     * @param indices Elastic Indices Names
     * @param type Elastic Type Name (e.g., Person)
     * @param field Elastic Field Name (e.g., gender, _type)
     * @param termStatistics Map <Key = 'Term', Tuple<Count, Cardinality>, (e.g < 'Key = female', Value = [500, 1] >
     * @return list of documents
     */
    public static Iterable<Map<String, Object>> prepareTermStatisticsDocs(List<String> indices,
                                                                          String type,
                                                                          String field,
                                                                          Map<String, Tuple2<Long, Long>> termStatistics) {
        List<StatTermResult> statRangeResults = new ArrayList<>();
        for (Map.Entry<String, Tuple2<Long, Long>> entry : termStatistics.entrySet()) {
            String term = entry.getKey();
            Long count = entry.getValue()._1;
            Long cardinality = entry.getValue()._2;
            for (String index : indices) {
                StatTermResult<String> statTermResult =
                        new StatTermResult<>(index, type, field, term, DataType.string, term, count, cardinality);
                statRangeResults.add(statTermResult);
            }
        }
        return StatUtil.prepareStatDocs(statRangeResults);
    }
    //endregion


}
