package com.kayhut.fuse.epb.plan.statistics;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.google.common.collect.ImmutableList;
import com.kayhut.fuse.epb.plan.statistics.configuration.StatConfig;
import com.kayhut.fuse.epb.plan.statistics.provider.ElasticClientProvider;
import com.kayhut.fuse.epb.plan.statistics.provider.ElasticStatDocumentProvider;
import com.kayhut.fuse.epb.plan.statistics.provider.ElasticStatProvider;
import com.kayhut.fuse.epb.plan.statistics.provider.ElasticStatisticsGraphProvider;
import com.kayhut.fuse.epb.plan.statistics.util.StatConfigTestUtil;
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
import org.elasticsearch.action.admin.indices.create.CreateIndexRequest;
import org.elasticsearch.action.admin.indices.refresh.RefreshRequest;
import org.elasticsearch.client.transport.TransportClient;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;

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

    private static final long NUM_OF_DRAGONS_IN_INDEX_1 = 1000L;

    private static final String DATA_TYPE_DRAGON = "dragon";
    private static final String DATA_TYPE_FIRE = "fire";

    private static final String DATA_INDEX_NAME_1 = "index1";
    private static final String DATA_INDEX_NAME_2 = "index2";
    private static final String DATA_INDEX_NAME_3 = "index3";
    private static final String DATA_INDEX_NAME_4 = "index4";

    private static final String DATA_FIELD_NAME_NAME = "name"; //Dragon Name
    private static final String DATA_FIELD_NAME_AGE = "age";
    private static final String DATA_FIELD_NAME_ADDRESS = "address";
    private static final String DATA_FIELD_NAME_COLOR = "color";
    private static final String DATA_FIELD_NAME_GENDER = "gender";
    private static final String DATA_FIELD_NAME_TYPE = "_type";

    private static final List<String> DRAGON_GENDERS =
            Arrays.asList("male", "female");

    private static final List<String> VERTEX_INDICES = ImmutableList.of(DATA_INDEX_NAME_1, DATA_INDEX_NAME_2);
    private static final List<String> EDGE_INDICES = ImmutableList.of(DATA_INDEX_NAME_3, DATA_INDEX_NAME_4);


    @Test
    public void getVertexCardinality() throws Exception {
        OntologySchemaProvider ontologySchemaProvider = getOntologySchemaProvider(getOntology());
        GraphVertexSchema vertexDragonSchema = ontologySchemaProvider.getVertexSchema(DATA_TYPE_DRAGON).get();

        ElasticStatisticsGraphProvider statisticsGraphProvider = new ElasticStatisticsGraphProvider(statConfig,
                new ElasticStatProvider(statConfig, new ElasticStatDocumentProvider(statClient, statConfig)),
                Caffeine.newBuilder()
                        .maximumSize(10_000)
                        .expireAfterWrite(5, TimeUnit.MINUTES)
                        .build());

        Map<String, Tuple2<Long, Long>> termStatistics = new HashMap<>();
        //We have 1000 dragons in index1, cardinality is 1
        termStatistics.put(DATA_TYPE_DRAGON, new Tuple2<>(NUM_OF_DRAGONS_IN_INDEX_1, 1L));

        //Populating the Elastic Stat Engine index: 'stat' type: 'termBucket', buckets of statistics
        populateTermStatDocs(VERTEX_INDICES, DATA_TYPE_DRAGON, DATA_FIELD_NAME_TYPE, termStatistics);
        //Checking that the ELASTIC STAT TERM TYPE created
        assertTrue(EsUtil.isTypeExists(statClient, statConfig.getStatIndexName(), statConfig.getStatTermTypeName()));

        //Sanity Checks
        //We have only 1 index ('stat') in the STAT Elastic Engine
        String[] allIndices = EsUtil.getAllIndices(statClient);
        assertEquals(1, allIndices.length);
        assertEquals(allIndices[0], statConfig.getStatIndexName());

        //We have only 1 type in the Elastic Index 'stat' = 'termBucket'
        String[] allTypesFromIndex = EsUtil.getAllTypesFromIndex(statClient, statConfig.getStatIndexName());
        Arrays.asList(allTypesFromIndex).forEach(type -> System.out.println(type));
        assertEquals(1, allTypesFromIndex.length);
        assertEquals(statConfig.getStatTermTypeName(), allTypesFromIndex[0]);

        //Check that the bucket term exists (the bucket is calculated on the field _type which is value is 'Dragon')
        String docId = StatUtil.hashString(VERTEX_INDICES.get(0) + DATA_TYPE_DRAGON + DATA_FIELD_NAME_TYPE + DATA_TYPE_DRAGON);
        Optional<Map<String, Object>> doc6Result = EsUtil.getDocumentSourceById(statClient, statConfig.getStatIndexName(), statConfig.getStatTermTypeName(), docId);
        assertTrue(doc6Result.isPresent());

        assertEquals(1, (int) doc6Result.get().get(statConfig.getStatCardinalityFieldName()));
        assertEquals((int) doc6Result.get().get(statConfig.getStatCountFieldName()), NUM_OF_DRAGONS_IN_INDEX_1);

        Statistics.SummaryStatistics vertexSummaryStatistics = statisticsGraphProvider.getVertexCardinality(vertexDragonSchema);
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
    public void getConditionHistogram1() throws Exception {

    }

    @Test
    public void getGlobalSelectivity() throws Exception {

    }

    /**
     * (1) Starting Elastic engine in-memory
     * (2) Loading statistics configuration
     *
     * @throws Exception
     */
    @BeforeClass
    public static void setup() throws Exception {
        statConfig = StatConfigTestUtil.getStatConfig(buildStatContainer());

        elasticEmbeddedNode = new ElasticEmbeddedNode();
        elasticEmbeddedNode.getClient().admin().indices().create(new CreateIndexRequest(statConfig.getStatIndexName())).actionGet();

        statClient = new ElasticClientProvider(statConfig).getStatClient();
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
        if (statClient != null) {
            statClient.close();
            statClient = null;
        }

        elasticEmbeddedNode.close();
    }

    /**
     * Populating elastic statistics documents for terms
     *
     * @param indices        Elastic Indices Names
     * @param type           Elastic Type Name (e.g., Person)
     * @param field          Elastic Field Name (e.g., gender, _type)
     * @param termStatistics Map <Key = 'Term', Tuple<Count, SummaryStatistics>, (e.g < 'Key = female', Value = [500, 1] >
     */
    private static void populateTermStatDocs(List<String> indices,
                                             String type,
                                             String field,
                                             Map<String, Tuple2<Long, Long>> termStatistics
    ) throws IOException {
        new ElasticDataPopulator(
                statClient,
                statConfig.getStatIndexName(),
                statConfig.getStatTermTypeName(),
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
        HistogramNumeric histogramDragonAge = HistogramNumeric.Builder.get()
                .withMin(10).withMax(100).withNumOfBins(10).build();

        HistogramString histogramDragonName = HistogramString.Builder.get()
                .withPrefixSize(3)
                .withInterval(10).withNumOfChars(26).withFirstCharCode("97").build();

        HistogramManual histogramDragonAddress = HistogramManual.Builder.get()
                .withBuckets(Arrays.asList(
                        new BucketRange("abc", "dzz"),
                        new BucketRange("efg", "hij"),
                        new BucketRange("klm", "xyz")
                )).withDataType(DataType.string)
                .build();

        HistogramComposite histogramDragonColor = HistogramComposite.Builder.get()
                .withManualBuckets(Arrays.asList(
                        new BucketRange("00", "11"),
                        new BucketRange("22", "33"),
                        new BucketRange("44", "55")
                )).withDataType(DataType.string)
                .withAutoBuckets(HistogramString.Builder.get()
                        .withFirstCharCode("97")
                        .withInterval(10)
                        .withNumOfChars(26)
                        .withPrefixSize(3).build())
                .build();

        HistogramTerm histogramTerm = HistogramTerm.Builder.get()
                .withDataType(DataType.string).withBuckets(Arrays.asList(
                        new BucketTerm(DRAGON_GENDERS.get(0)),
                        new BucketTerm(DRAGON_GENDERS.get(1))
                )).build();

        HistogramTerm histogramDocType = HistogramTerm.Builder.get()
                .withDataType(DataType.string).withBuckets(Collections.singletonList(
                        new BucketTerm(DATA_TYPE_DRAGON)
                )).build();


        Type typeDragon = new Type(DATA_TYPE_DRAGON, Arrays.asList(
                new Field(DATA_FIELD_NAME_AGE, histogramDragonAge),
                new Field(DATA_FIELD_NAME_NAME, histogramDragonName),
                new Field(DATA_FIELD_NAME_ADDRESS, histogramDragonAddress),
                new Field(DATA_FIELD_NAME_COLOR, histogramDragonColor),
                new Field(DATA_FIELD_NAME_GENDER, histogramTerm),
                new Field(DATA_FIELD_NAME_TYPE, histogramDocType)));

        Mapping mapping = Mapping.Builder.get().withIndices(VERTEX_INDICES)
                .withTypes(Collections.singletonList(DATA_TYPE_DRAGON)).build();

        return StatContainer.Builder.get()
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
                .withRType(1).withName(DATA_TYPE_FIRE).withEPairs(ePairs).build();

        Property nameProp = new Property(DATA_FIELD_NAME_NAME, 1, "string");
        Property ageProp = new Property(DATA_FIELD_NAME_AGE, 2, "int");

        when(ontology.getProperties()).then(invocationOnMock -> Collections.singletonList(nameProp));

        when(ontology.getEntityTypes()).thenAnswer(invocationOnMock ->
                {
                    ArrayList<EntityType> entityTypes = new ArrayList<>();
                    entityTypes.add(EntityType.Builder.get()
                            .withEType(2).withName(DATA_TYPE_DRAGON)
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
    private static Iterable<Map<String, Object>> prepareNumericStatisticsDocs(List<String> indices,
                                                                              String type,
                                                                              String field,
                                                                              long min,
                                                                              long max,
                                                                              int numOfBins) {
        List<BucketRange<Double>> numericBuckets = StatUtil.createDoubleBuckets(min, max, Math.toIntExact(numOfBins));
        List<StatRangeResult> statRangeResults = new ArrayList<>();
        int j = 0;
        for (BucketRange<Double> bucketRange : numericBuckets) {
            for (String index : indices) {
                StatRangeResult<Double> statRangeResult = new StatRangeResult<>
                        (index, type, field, Integer.toString(j), DataType.numericDouble, bucketRange.getStart(), bucketRange.getEnd(), j, j);
                statRangeResults.add(statRangeResult);
            }
            j++;
        }
        return StatUtil.prepareStatDocs(statRangeResults);
    }

    /**
     * @param indices        Elastic Indices Names
     * @param type           Elastic Type Name (e.g., Person)
     * @param field          Elastic Field Name (e.g., gender, _type)
     * @param termStatistics Map <Key = 'Term', Tuple<Count, SummaryStatistics>, (e.g < 'Key = female', Value = [500, 1] >
     * @return list of documents
     */
    private static Iterable<Map<String, Object>> prepareTermStatisticsDocs(List<String> indices,
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
