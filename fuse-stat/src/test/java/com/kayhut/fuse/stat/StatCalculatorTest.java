package com.kayhut.fuse.stat;

import com.kayhut.fuse.stat.util.EsUtil;
import com.kayhut.fuse.stat.util.StatUtil;
import com.kayhut.fuse.stat.es.client.ClientProvider;
import com.kayhut.test.framework.index.ElasticEmbeddedNode;
import com.kayhut.test.framework.populator.ElasticDataPopulator;
import org.apache.commons.collections.map.HashedMap;
import org.apache.commons.configuration.Configuration;
import org.elasticsearch.client.transport.TransportClient;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.*;

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

    @Test
    public void statCalculatorTest() throws Exception {
        StatCalculator.main(new String[]{CONFIGURATION_FILE_PATH});

        //Check if Stat index created
        String statIndexName = "stat";
        String statTypeNumericName = "bucketNumeric";
        String statTypeStringName = "bucketString";

        String dataIndexName1 = "index1" ;
        String dataIndexName2 = "index2" ;
        String dataTypeName1 = "dragon";
        String dataFieldNameAge = "age";
        String dataFieldNameAddress = "address";
        String dataFieldNameColor = "color";

        assertTrue(EsUtil.checkIfEsIndexExists(statClient, statIndexName ));
        assertTrue(EsUtil.checkIfEsTypeExists(statClient,statIndexName,statTypeNumericName));
        assertTrue(EsUtil.checkIfEsTypeExists(statClient,statIndexName,statTypeStringName));


        //Check if age stat bucket exists (bucket #1: 10.0-20.0)
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
                () -> createDragons(1000)).populate();

        new ElasticDataPopulator(
                dataClient,
                "index2",
                "dragon",
                "id",
                () -> createDragons(555)).populate();

        Thread.sleep(2000);

    }

    private static Iterable<Map<String, Object>> createDragons(int numDragons) {
        Random r = new Random();
        List<String> colors = Arrays.asList("red", "green", "yellow", "blue", "00" ,"11" ,"22" ,"33" ,"44" ,"55");
        List<Map<String, Object>> dragons = new ArrayList<>();
        for(int i = 0 ; i < numDragons ; i++) {
            Map<String, Object> dragon = new HashedMap();
            dragon.put("id", Integer.toString(i));
            dragon.put("name", generateRandomString(10) + " dragon" + i);
            dragon.put("age", r.nextInt(100));
            dragon.put("color", colors.get(r.nextInt(colors.size())));
            dragon.put("address", generateRandomString(20));

            dragons.add(dragon);
        }
        return dragons;
    }

    private static String generateRandomString(int stringSize){
        char[] chars = "abcdefghijklmnopqrstuvwxyz".toCharArray();
        StringBuilder sb = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < stringSize; i++) {
            char c = chars[random.nextInt(chars.length)];
            sb.append(c);
        }
        return sb.toString();
    }

}