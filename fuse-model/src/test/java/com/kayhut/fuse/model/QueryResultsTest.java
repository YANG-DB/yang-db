package com.kayhut.fuse.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kayhut.fuse.model.query.EBase;
import com.kayhut.fuse.model.query.Query;
import com.kayhut.fuse.model.results.*;
import org.apache.commons.io.IOUtils;
import org.json.JSONException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.kayhut.fuse.model.results.QueryResult.Builder.instance;

/**
 * Created by benishue on 21-Feb-17.
 */
public class QueryResultsTest {
    private ObjectMapper mapper = new ObjectMapper();
    private static QueryResult result1Obj = instance().build();


    @Test
    public void testResults1Serialization() throws IOException, JSONException {
        String result1ActualJSON = mapper.writeValueAsString(result1Obj);
        String result1ExpectedJSONString = "{\"pattern\":{\"ont\":\"Dragons\",\"name\":\"Q1\"},\"assignments\":[{\"entities\":[{\"eTag\":[\"A\",\"C\"],\"eID\":\"12345678\",\"eType\":1,\"properties\":[{\"pType\":\"1\",\"agg\":\"raw\",\"value\":\"a\"},{\"pType\":\"3\",\"agg\":\"raw\",\"value\":5.35}],\"attachedProperties\":[{\"pName\":\"count(relationships)\",\"value\":53}]}],\"relationships\":[{\"rID\":\"12345678\",\"agg\":true,\"rType\":2,\"directional\":true,\"eID1\":\"12345678\",\"eID2\":\"12345679\",\"properties\":[{\"pType\":\"1\",\"agg\":\"max\",\"value\":76},{\"pType\":\"1\",\"agg\":\"avg\",\"value\":34.56}],\"attachedProperties\":[{\"pName\":\"sum(duration)\",\"value\":124}]}]}]}";
        System.out.println("result1ExpectedJSONString:" + result1ExpectedJSONString);
        System.out.println("result1ActualJSON:" + result1ActualJSON);
        JSONAssert.assertEquals(result1ExpectedJSONString, result1ActualJSON,false);
    }


    @Test
    public void testDeSerialization() throws Exception {
        String result1ExpectedJson = readJsonToString("results1.json");
        QueryResult resultObj = new ObjectMapper().readValue(result1ExpectedJson, QueryResult.class);
        Assert.assertNotNull(resultObj);
        String result1ActualJSON = mapper.writeValueAsString(resultObj);
        JSONAssert.assertEquals(result1ExpectedJson, result1ActualJSON,false);
    }

    private static void createResults1()
    {
        Query pattern = new Query();
        pattern.setOnt("Dragons");
        pattern.setName("Q1");
        pattern.setElements(new ArrayList<EBase>() {});

        List<Assignment> assignments = new ArrayList<Assignment>();
        List<Entity> entities = new ArrayList<Entity>();

        Entity entity = new Entity();
        entity.seteTag(Arrays.asList("A", "C"));
        entity.seteID("12345678");
        entity.seteType(1);

        List<Property> properties = new ArrayList<Property>();
        Property property1 = new Property();
        property1.setpType("1");
        property1.setAgg("raw");
        property1.setValue("a");

        Property property2 = new Property();
        property2.setpType("3");
        property2.setAgg("raw");
        property2.setValue(5.35);

        AttachedProperty attachedProperty = new AttachedProperty();
        attachedProperty.setPName("count(relationships)");
        attachedProperty.setValue(53);

        entity.setProperties(Arrays.asList(property1,property2));
        entity.setAttachedProperties(Arrays.asList(attachedProperty));

        entities.add(entity);


        List<Relationship> relationships = new ArrayList<Relationship>();
        Relationship relationship1 = new Relationship();
        relationship1.setrID("12345678");
        relationship1.setAgg(true);
        relationship1.setrType(2);
        relationship1.setDirectional(true);
        relationship1.seteID1("12345678");
        relationship1.seteID2("12345679");

        List<Property> propertiesRelationship = new ArrayList<Property>();
        Property propertyRelationship1 =  new Property();
        propertyRelationship1.setpType("1");
        propertyRelationship1.setAgg("max");
        propertyRelationship1.setValue(76);

        Property propertyRelationship2 =  new Property();
        propertyRelationship2.setpType("1");
        propertyRelationship2.setAgg("avg");
        propertyRelationship2.setValue(34.56);

        AttachedProperty attachedPropertyRelationship1 =  new AttachedProperty();
        attachedPropertyRelationship1.setPName("sum(duration)");
        attachedPropertyRelationship1.setValue(124);

        relationship1.setProperties(Arrays.asList(propertyRelationship1,propertyRelationship2));
        relationship1.setAttachedProperties(Arrays.asList(attachedPropertyRelationship1));

        relationships.add(relationship1);

        Assignment assignment = new Assignment();
        assignment.setEntities(entities);
        assignment.setRelationships(relationships);

        assignments.add(assignment);
        result1Obj.setPattern(pattern);
        result1Obj.setAssignments(assignments);


    }


    private String readJsonToString(String jsonFileName) throws Exception {
        String result = "";
        ClassLoader classLoader = getClass().getClassLoader();
        try {
            result = IOUtils.toString(classLoader.getResourceAsStream("ResultsJsons/" + jsonFileName));
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
        createResults1();

    }

}
