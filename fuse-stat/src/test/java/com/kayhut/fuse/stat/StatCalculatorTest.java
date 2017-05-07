package com.kayhut.fuse.stat;

import com.kayhut.fuse.stat.Util.EsUtil;
import com.kayhut.fuse.stat.Util.StatUtil;
import com.kayhut.fuse.stat.es.client.ClientProvider;
import com.kayhut.test.framework.index.ElasticInMemoryIndex;
import com.kayhut.test.framework.populator.ElasticDataPopulator;
import org.apache.commons.collections.map.HashedMap;
import org.apache.commons.configuration.Configuration;
import org.elasticsearch.client.transport.TransportClient;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.*;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

/**
 * Created by benishue on 04-May-17.
 */
public class StatCalculatorTest {


    private static TransportClient dataClient;
    private static TransportClient statClient;
    static ElasticInMemoryIndex elasticInMemoryIndex;
    static Configuration configuration;
    static String CONFIGURATION_FILE_PATH = "statistics.test.properties";

    @Test
    public void statCalculatorTest() throws Exception {
        StatCalculator.main(new String[]{CONFIGURATION_FILE_PATH});
        //Check if Stat index created
        String statIndexName = "stat";
        String statTypeName = "bucket";
        String dataIndexName1 = "index1" ;
        String dataIndexName2 = "index2" ;
        String dataTypeName1 = "dragon";
        String dataFieldName1 = "age";
        String dataFieldName2 = "address";
        assertTrue(EsUtil.checkIfEsIndexExists(statClient, statIndexName ));
        assertTrue(EsUtil.checkIfEsTypeExists(statClient,statIndexName,statTypeName));

        //Check if age stat bucket exists (bucket #1: 10.0-20.0)
        String docId1 = StatUtil.hashString(dataIndexName1 + dataTypeName1 + dataFieldName1  + "10.0" + "20.0");
        assertEquals("vc8XbeyVJ7gfdxmfHSiOCQ==", docId1);
        assertTrue(EsUtil.checkIfEsDocExists(statClient, statIndexName, statTypeName, docId1));

        //Check if address bucket exists (bucket #1 "abc" + "dzz")
        String docId2 = StatUtil.hashString(dataIndexName1 + dataTypeName1 + dataFieldName2  + "abc" + "dzz");
        assertTrue(EsUtil.checkIfEsDocExists(statClient, statIndexName, statTypeName, docId1));

    }


    @BeforeClass
    public static void setup() throws Exception {

        configuration = new StatConfiguration(CONFIGURATION_FILE_PATH).getInstance();

        dataClient = ClientProvider.getDataClient(configuration);
        statClient = ClientProvider.getDataClient(configuration);

        elasticInMemoryIndex = new ElasticInMemoryIndex();

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
        List<String> colors = Arrays.asList("red", "green", "yellow", "blue");
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
        String output = sb.toString();
        return output;
    }

}