package com.kayhut.fuse.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kayhut.fuse.model.query.*;
import org.json.JSONException;
import org.junit.*;
import org.skyscreamer.jsonassert.JSONAssert;
import java.io.IOException;
import org.apache.commons.io.IOUtils;
import java.util.ArrayList;
import java.util.List;


/**
 * Created by benishue on 19-Feb-17.
 */
public class QueryTest {

    private ObjectMapper mapper = new ObjectMapper();
    private static Query q1Obj = new Query();
    private static Query q2Obj = new Query();
    private static Query q3_1Obj = new Query();


    @Test
    public void testQ1Serialization() throws IOException, JSONException {
        String q1ActualJSON = mapper.writeValueAsString(q1Obj);
        String q1ExpectedJSONString = "{\"ont\":\"Dragons\",\"name\":\"Q1\",\"elements\":[{\"eNum\":0,\"type\":\"Start\",\"next\":1},{\"eNum\":1,\"type\":\"EConcrete\",\"eTag\":\"A\",\"eID\":\"12345678\",\"eType\":1,\"eName\":\"Brandon Stark\",\"next\":2},{\"eNum\":2,\"type\":\"Rel\",\"rType\":1,\"dir\":\"R\",\"next\":3},{\"eNum\":3,\"type\":\"ETyped\",\"eTag\":\"B\",\"eType\":2}]}";

        JSONAssert.assertEquals(q1ExpectedJSONString, q1ActualJSON,false);
    }

    @Test
    public void testQ2Serialization() throws IOException, JSONException {
        String q2ActualJSON = mapper.writeValueAsString(q2Obj);
        String q2ExpectedJSONString = "{\"ont\":\"Dragons\",\"name\":\"Q2\",\"elements\":[{\"eNum\":0,\"type\":\"Start\",\"next\":1},{\"eNum\":1,\"type\":\"EConcrete\",\"eTag\":\"A\",\"eID\":\"12345678\",\"eType\":1,\"eName\":\"Brandon Stark\",\"next\":2},{\"eNum\":2,\"type\":\"Rel\",\"rType\":1,\"dir\":\"R\",\"next\":3},{\"eNum\":3,\"type\":\"ETyped\",\"eTag\":\"B\",\"eType\":2,\"next\":4},{\"eNum\":4,\"type\":\"Rel\",\"rType\":3,\"dir\":\"R\",\"next\":5},{\"eNum\":5,\"type\":\"ETyped\",\"eTag\":\"C\",\"eType\":2}]}";

        JSONAssert.assertEquals(q2ExpectedJSONString, q2ActualJSON,false);
    }

    @Test
    public void testQ3_1Serialization() throws IOException, JSONException {
        String q3_1ActualJSON = mapper.writeValueAsString(q3_1Obj);
        String q3_1ExpectedJSONString = "{\"ont\":\"Dragons\",\"name\":\"Q3-1\",\"elements\":[{\"eNum\":0,\"type\":\"Start\",\"next\":1},{\"eNum\":1,\"type\":\"ETyped\",\"eTag\":\"A\",\"eType\":2,\"next\":2},{\"eNum\":2,\"type\":\"Rel\",\"rType\":1,\"dir\":\"L\",\"next\":3},{\"eNum\":3,\"type\":\"ETyped\",\"eTag\":\"B\",\"eType\":1,\"next\":4},{\"eNum\":4,\"type\":\"EProp\",\"pType\":1,\"cond\":{\"op\":\"eq\",\"value\":\"Brandon\"}}]}";

        JSONAssert.assertEquals(q3_1ExpectedJSONString, q3_1ActualJSON,false);
    }

    @Test
    public void testDeSerialization() throws Exception {
        String q1ExpectedJson = readJsonToString("Q001.json");
        Query q1Obj = new ObjectMapper().readValue(q1ExpectedJson, Query.class);
        Assert.assertNotNull(q1Obj);
        String q1ActualJSON = mapper.writeValueAsString(q1Obj);
        JSONAssert.assertEquals(q1ExpectedJson, q1ActualJSON,false);

        String q2ExpectedJson = readJsonToString("Q002.json");
        Query q2Obj = new ObjectMapper().readValue(q2ExpectedJson, Query.class);
        Assert.assertNotNull(q1Obj);
        String q2ActualJSON = mapper.writeValueAsString(q2Obj);
        JSONAssert.assertEquals(q2ExpectedJson, q2ActualJSON,false);

        String q3_1ExpectedJson = readJsonToString("Q003-1.json");
        Query q3_1Obj = new ObjectMapper().readValue(q3_1ExpectedJson, Query.class);
        Assert.assertNotNull(q3_1Obj);
        String q3_1ActualJSON = mapper.writeValueAsString(q3_1Obj);
        JSONAssert.assertEquals(q3_1ExpectedJson, q3_1ActualJSON,false);


        String q3_2ExpectedJson = readJsonToString("Q003-2.json");
        Query q3_2Obj = new ObjectMapper().readValue(q3_2ExpectedJson, Query.class);
        Assert.assertNotNull(q3_2Obj);
        String q3_2ActualJSON = mapper.writeValueAsString(q3_2Obj);
        JSONAssert.assertEquals(q3_2ExpectedJson, q3_2ActualJSON,false);

    }

    private static void createQ1()
    {
        q1Obj.setOnt("Dragons");
        q1Obj.setName("Q1");
        List<EBase> elements = new ArrayList<EBase>();

        /*
        {
          "eNum": 0,
          "type": "Start",
          "next": 1
        }
         */

        Start start  = new Start();
        start.seteNum(0);
        start.setNext(1);
        elements.add(start);

        /*
         {
          "eNum": 1,
          "type": "EConcrete",
          "eTag": "A",
          "eID": "12345678",
          "eType": 1,
          "eName": "Brandon Stark",
          "next": 2
         }
        */
        EConcrete eConcrete = new EConcrete();
        eConcrete.seteNum(1);
        eConcrete.seteTag("A");
        eConcrete.seteID("12345678");
        eConcrete.seteType(1);
        eConcrete.seteName("Brandon Stark");
        eConcrete.setNext(2);
        elements.add(eConcrete);

        /*
        {
          "eNum": 2,
          "type": "Rel",
          "rType": 1,
          "dir": "R",
          "next": 3
        }
         */
        Rel rel = new Rel();
        rel.seteNum(2);
        rel.setrType(1);
        rel.setDir("R");
        rel.setNext(3);
        elements.add(rel);


        /*
        {
          "eNum": 3,
          "type": "ETyped",
          "eTag": "B",
          "eType": 2
        }
        */
        ETyped eTyped = new ETyped();
        eTyped.seteNum(3);
        eTyped.seteTag("B");
        eTyped.seteType(2);
        elements.add(eTyped);

        q1Obj.setElements(elements);
    }

    private static void createQ2()
    {
        q2Obj.setOnt("Dragons");
        q2Obj.setName("Q2");
        List<EBase> elements = new ArrayList<EBase>();

        /*
            {
              "eNum": 0,
              "type": "Start",
              "next": 1
            }
         */

        Start start  = new Start();
        start.seteNum(0);
        start.setNext(1);
        elements.add(start);

        /*
            {
              "eNum": 1,
              "type": "EConcrete",
              "eTag": "A",
              "eID": "12345678",
              "eType": 1,
              "eName": "Brandon Stark",
              "next": 2
            }
        */

        EConcrete eConcrete = new EConcrete();
        eConcrete.seteNum(1);
        eConcrete.seteTag("A");
        eConcrete.seteID("12345678");
        eConcrete.seteType(1);
        eConcrete.seteName("Brandon Stark");
        eConcrete.setNext(2);
        elements.add(eConcrete);

        /*
            {
              "eNum": 2,
              "type": "Rel",
              "rType": 1,
              "dir": "R",
              "next": 3
            }
         */

        Rel rel = new Rel();
        rel.seteNum(2);
        rel.setrType(1);
        rel.setDir("R");
        rel.setNext(3);
        elements.add(rel);

        /*
            {
              "eNum": 3,
              "type": "ETyped",
              "eTag": "B",
              "eType": 2,
              "next": 4
            }
        */

        ETyped eTyped = new ETyped();
        eTyped.seteNum(3);
        eTyped.seteTag("B");
        eTyped.seteType(2);
        eTyped.setNext(4);
        elements.add(eTyped);

        /*
        {
            "eNum": 4,
            "type": "Rel",
            "rType": 3,
            "dir": "R",
            "next": 5
        }
        */

        Rel rel2 = new Rel();
        rel2.seteNum(4);
        rel2.setrType(3);
        rel2.setDir("R");
        rel2.setNext(5);
        elements.add(rel2);

        /*
        {
            "eNum": 5,
            "type": "ETyped",
            "eTag": "C",
            "eType": 2
        }
        */

        ETyped eTyped2 = new ETyped();
        eTyped2.seteNum(5);
        eTyped2.seteTag("C");
        eTyped2.seteType(2);
        elements.add(eTyped2);

        q2Obj.setElements(elements);
    }

    private static void createQ3_1()
    {
        q3_1Obj.setOnt("Dragons");
        q3_1Obj.setName("Q3-1");
        List<EBase> elements = new ArrayList<EBase>();

        /*
            {
              "eNum": 0,
              "type": "Start",
              "next": 1
            }
         */

        Start start  = new Start();
        start.seteNum(0);
        start.setNext(1);
        elements.add(start);

        /*
            {
              "eNum": 1,
              "type": "ETyped",
              "eTag": "A",
              "eType": 2,
              "next": 2
            }
         */

        ETyped eTyped = new ETyped();
        eTyped.seteNum(1);
        eTyped.seteTag("A");
        eTyped.seteType(2);
        eTyped.setNext(2);
        elements.add(eTyped);

        /*
            {
              "eNum": 2,
              "type": "Rel",
              "rType": 1,
              "dir": "L",
              "next": 3
            }
         */
        Rel rel = new Rel();
        rel.seteNum(2);
        rel.setrType(1);
        rel.setDir("L");
        rel.setNext(3);
        elements.add(rel);

        /*
            {
              "eNum": 3,
              "type": "ETyped",
              "eTag": "B",
              "eType": 1,
              "next": 4
            }
         */

        ETyped eTyped2 = new ETyped();
        eTyped2.seteNum(3);
        eTyped2.seteTag("B");
        eTyped2.seteType(1);
        eTyped2.setNext(4);
        elements.add(eTyped2);

        /*
            {
              "eNum": 4,
              "type": "EProp",
              "pType": 1,
              "cond": {
              "op": "eq",
              "value": "Brandon"
             }
         */

        EProp eProp = new EProp();
        eProp.seteNum(4);
        eProp.setpType(1);
        Condition cond = new Condition();
        cond.setOp(ConditionOp.eq);
        cond.setValue("Brandon");
        eProp.setCond(cond);
        elements.add(eProp);

        q3_1Obj.setElements(elements);
    }


    private String readJsonToString(String jsonFileName) throws Exception {
        String result = "";
        ClassLoader classLoader = getClass().getClassLoader();
        try {
            result = IOUtils.toString(classLoader.getResourceAsStream("QueryJsons/" + jsonFileName));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    @Before
    public void setup() {
    }

    @BeforeClass
    public static void setUpOnce() {
        createQ1();
        createQ2();
        createQ3_1();

    }

}
