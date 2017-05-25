package com.kayhut.fuse.stat;

import com.kayhut.fuse.stat.configuration.StatConfiguration;
import com.kayhut.fuse.stat.es.client.ClientProvider;
import com.kayhut.fuse.stat.util.EsUtil;
import com.kayhut.fuse.stat.util.StatUtil;
import com.kayhut.test.framework.index.ElasticEmbeddedNode;
import com.kayhut.test.framework.populator.ElasticDataPopulator;
import org.apache.commons.configuration.Configuration;
import org.elasticsearch.client.transport.TransportClient;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Map;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by benishue on 04-May-17.
 */
public class StatCalculatorTest {


    static TransportClient dataClient;
    static TransportClient statClient;
    static ElasticEmbeddedNode elasticEmbeddedNode;
    static Configuration configuration;
    static final String CONFIGURATION_FILE_PATH = "statistics.test.properties";
    static final int numOfDragonsInIndex1 = 1000;
    static final int numOfDragonsInIndex2 = 555; //HAMSA HAMSA HAMSA


    //todo - add more tests, specially small unit tests per each case
    //Full - blown test
    @Test
    public void statCalculatorTest() throws Exception {
        StatCalculator.main(new String[]{CONFIGURATION_FILE_PATH});

        //Check if Stat index created
        String statIndexName = "stat";
        String statTypeNumericName = "bucketNumeric";
        String statTypeStringName = "bucketString";
        String statTypeTermName = "bucketTerm";


        String dataIndexName1 = "index1" ;
        String dataIndexName2 = "index2" ;
        String dataTypeName1 = "dragon";
        String dataFieldNameAge = "age";
        String dataFieldNameAddress = "address";
        String dataFieldNameColor = "color";
        String dataFieldNameGender = "gender";
        String dataFieldNameType = "_type";


        assertTrue(EsUtil.checkIfEsIndexExists(statClient, statIndexName ));
        assertTrue(EsUtil.checkIfEsTypeExists(statClient,statIndexName,statTypeNumericName));
        assertTrue(EsUtil.checkIfEsTypeExists(statClient,statIndexName,statTypeStringName));
        assertTrue(EsUtil.checkIfEsTypeExists(statClient,statIndexName,statTypeTermName));


        //Check if age stat numeric bucket exists (bucket #1: 10.0-20.0)
        String docId1 = StatUtil.hashString(dataIndexName1 + dataTypeName1 + dataFieldNameAge  + "10.0" + "20.0");
        assertEquals("vc8XbeyVJ7gfdxmfHSiOCQ==", docId1);
        assertTrue(EsUtil.checkIfEsDocExists(statClient, statIndexName, statTypeNumericName, docId1));

        //Check if address bucket exists (bucket #1 "abc" + "dzz")
        String docId2 = StatUtil.hashString(dataIndexName1 + dataTypeName1 + dataFieldNameAddress  + "abc" + "dzz");
        assertTrue(EsUtil.checkIfEsDocExists(statClient, statIndexName, statTypeStringName, docId2));

        //Check if color bucket exists (bucket with lower_bound: "grc", upper_bound: "grl")
        String docId3 = StatUtil.hashString(dataIndexName1 + dataTypeName1 + dataFieldNameColor  + "grc" + "grl");
        assertTrue(EsUtil.checkIfEsDocExists(statClient, statIndexName, statTypeStringName, docId3));

        //Check that the bucket ["grc", "grl") have the cardinality of 1 (i.e. Green Color)
        Optional<Map<String, Object>> doc3Result = EsUtil.getDocumentById(statClient, statIndexName, statTypeStringName, docId3);
        assertTrue(doc3Result.isPresent());
        assertEquals(1, (int)doc3Result.get().get("cardinality"));

        //Check that the manual bucket ("00", "11"] exists for the composite histogram
        String docId4 = StatUtil.hashString(dataIndexName1 + dataTypeName1 + dataFieldNameColor  + "00" + "11");
        assertTrue(EsUtil.checkIfEsDocExists(statClient, statIndexName, statTypeStringName, docId4));

        //Check term buckets (Gender male) - cardinality should be 1
        String docId5 = StatUtil.hashString(dataIndexName1 + dataTypeName1 + dataFieldNameGender  + "male");
        Optional<Map<String, Object>> doc5Result = EsUtil.getDocumentById(statClient, statIndexName, statTypeTermName, docId5);
        assertTrue(doc5Result.isPresent());
        assertEquals(1, (int)doc5Result.get().get("cardinality"));
        //Since we have 1000 deagons (~0.5% should be males)
        assertEquals(Double.valueOf(doc5Result.get().get("count").toString()),
                numOfDragonsInIndex1/2.0, numOfDragonsInIndex1 * 0.1);

        //Check that there are 1000 dragons in Index: "index1", Type: "dragon"
        //Cardinality should be 1
        String docId6 = StatUtil.hashString(dataIndexName1 + dataTypeName1 + dataFieldNameType  + "dragon");
        Optional<Map<String, Object>> doc6Result = EsUtil.getDocumentById(statClient, statIndexName, statTypeTermName, docId6);
        assertTrue(doc6Result.isPresent());
        assertEquals(1, (int)doc6Result.get().get("cardinality"));
        assertEquals((int)doc6Result.get().get("count"), 1000);
    }

    @BeforeClass
    public static void setup() throws Exception {

        configuration = new StatConfiguration(CONFIGURATION_FILE_PATH).getInstance();

        dataClient = ClientProvider.getDataClient(configuration);
        statClient = ClientProvider.getDataClient(configuration);
        elasticEmbeddedNode = new ElasticEmbeddedNode();

        Thread.sleep(4000);

        new ElasticDataPopulator(
                dataClient,
                "index1",
                "dragon",
                "id",
                () -> StatTestUtil.createDragons(numOfDragonsInIndex1)).populate();

        new ElasticDataPopulator(
                dataClient,
                "index2",
                "dragon",
                "id",
                () -> StatTestUtil.createDragons(numOfDragonsInIndex2)).populate();

        Thread.sleep(2000);

    }





}