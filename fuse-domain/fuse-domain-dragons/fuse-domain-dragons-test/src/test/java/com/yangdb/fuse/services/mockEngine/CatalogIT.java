package com.yangdb.fuse.services.mockEngine;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yangdb.fuse.model.ontology.Ontology;
import com.yangdb.fuse.model.ontology.OntologyFinalizer;
import com.yangdb.fuse.model.transport.ContentResponse;
import com.yangdb.fuse.services.TestsConfiguration;
import com.yangdb.test.BaseITMarker;
import io.restassured.http.Header;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static io.restassured.RestAssured.given;

public class CatalogIT implements BaseITMarker {
    @Before
    public void before() throws Exception {
        Assume.assumeTrue(TestsConfiguration.instance.shouldRunTestClass(this.getClass()));
//        TestSuiteAPISuite.setup();//remark when doing IT tests
    }

    @Test
    /**
     * execute query with expected plan result
     */
    public void catalog() throws IOException {
        Ontology ontology = TestUtils.loadOntology("Dragons.json");
        given()
                .contentType("application/json")
                .header(new Header("fuse-external-id", "test"))
                .with().port(8888)
                .get("/fuse/catalog/ontology/Dragons")
                .then()
                .assertThat()
                .body(new TestUtils.ContentMatcher(o -> {
                    try {
                        String expected = new ObjectMapper().writeValueAsString(ontology);
                        ContentResponse contentResponse = new ObjectMapper().readValue(o.toString(), ContentResponse.class);
                        String result = new ObjectMapper().writeValueAsString(contentResponse.getData());
                        Assert.assertEquals(expected,result);
                        return result.equals(expected);
                    } catch (Exception e) {
                        e.printStackTrace();
                        return false;
                    }
                }))
                .statusCode(200)
                .contentType("application/json;charset=UTF-8");
    }

    @Test
    /**
     * execute query with expected plan result
     */
    public void catalogs() throws IOException {
        Ontology ontology = OntologyFinalizer.finalize(TestUtils.loadOntology("Dragons.json"));
        given()
                .contentType("application/json")
                .header(new Header("fuse-external-id", "test"))
                .with().port(8888)
                .get("/fuse/catalog/ontology")
                .then()
                .assertThat()
                .body(new TestUtils.ContentMatcher(o -> {
                    try {
                        ContentResponse contentResponse = new ObjectMapper().readValue(o.toString(), ContentResponse.class);
                        String result = new ObjectMapper().writeValueAsString(contentResponse.getData());
                        Assert.assertEquals("[{\"ont\":\"Dragons\",\"directives\":[],\"entityTypes\":[{\"eType\":\"Person\",\"name\":\"Person\",\"properties\":[\"id\",\"firstName\",\"lastName\",\"gender\",\"birthDate\",\"deathDate\",\"height\",\"name\"]},{\"eType\":\"Horse\",\"name\":\"Horse\",\"properties\":[\"id\",\"name\",\"weight\",\"maxSpeed\",\"distance\"]},{\"eType\":\"Dragon\",\"name\":\"Dragon\",\"properties\":[\"id\",\"name\",\"birthDate\",\"power\",\"gender\",\"color\"]},{\"eType\":\"Kingdom\",\"name\":\"Kingdom\",\"properties\":[\"id\",\"name\",\"king\",\"queen\",\"independenceDay\",\"funds\"]},{\"eType\":\"Guild\",\"name\":\"Guild\",\"properties\":[\"id\",\"name\",\"description\",\"iconId\",\"url\",\"establishDate\"]}],\"relationshipTypes\":[{\"rType\":\"own\",\"name\":\"own\",\"directional\":true,\"ePairs\":[{\"eTypeA\":\"Person\",\"eTypeB\":\"Dragon\"},{\"eTypeA\":\"Person\",\"eTypeB\":\"Horse\"}],\"properties\":[\"id\",\"startDate\",\"endDate\"]},{\"rType\":\"know\",\"name\":\"know\",\"directional\":true,\"ePairs\":[{\"eTypeA\":\"Person\",\"eTypeB\":\"Person\"}],\"properties\":[\"id\",\"startDate\"]},{\"rType\":\"memberOf\",\"name\":\"memberOf\",\"directional\":true,\"ePairs\":[{\"eTypeA\":\"Person\",\"eTypeB\":\"Guild\"}],\"properties\":[\"id\",\"startDate\",\"endDate\"]},{\"rType\":\"fire\",\"name\":\"fire\",\"directional\":true,\"ePairs\":[{\"eTypeA\":\"Dragon\",\"eTypeB\":\"Dragon\"}],\"properties\":[\"id\",\"date\",\"temperature\"]},{\"rType\":\"freeze\",\"name\":\"freeze\",\"directional\":true,\"ePairs\":[{\"eTypeA\":\"Dragon\",\"eTypeB\":\"Dragon\"}],\"properties\":[\"id\",\"startDate\",\"endDate\"]},{\"rType\":\"originatedIn\",\"name\":\"originatedIn\",\"directional\":true,\"ePairs\":[{\"eTypeA\":\"Dragon\",\"eTypeB\":\"Kingdom\"}],\"properties\":[\"id\",\"startDate\"]},{\"rType\":\"subjectOf\",\"name\":\"subjectOf\",\"directional\":true,\"ePairs\":[{\"eTypeA\":\"Person\",\"eTypeB\":\"Kingdom\"}],\"properties\":[\"id\",\"startDate\"]},{\"rType\":\"registeredIn\",\"name\":\"registeredIn\",\"directional\":true,\"ePairs\":[{\"eTypeA\":\"Guild\",\"eTypeB\":\"Kingdom\"},{\"eTypeA\":\"Dragon\",\"eTypeB\":\"Guild\"},{\"eTypeA\":\"Horse\",\"eTypeB\":\"Guild\"}],\"properties\":[\"id\",\"startDate\"]}],\"properties\":[{\"pType\":\"id\",\"name\":\"id\",\"type\":\"string\"},{\"pType\":\"firstName\",\"name\":\"firstName\",\"type\":\"string\"},{\"pType\":\"lastName\",\"name\":\"lastName\",\"type\":\"string\"},{\"pType\":\"gender\",\"name\":\"gender\",\"type\":\"TYPE_Gender\"},{\"pType\":\"birthDate\",\"name\":\"birthDate\",\"type\":\"date\"},{\"pType\":\"deathDate\",\"name\":\"deathDate\",\"type\":\"string\"},{\"pType\":\"name\",\"name\":\"name\",\"type\":\"string\"},{\"pType\":\"height\",\"name\":\"height\",\"type\":\"int\"},{\"pType\":\"weight\",\"name\":\"weight\",\"type\":\"int\"},{\"pType\":\"maxSpeed\",\"name\":\"maxSpeed\",\"type\":\"int\"},{\"pType\":\"distance\",\"name\":\"distance\",\"type\":\"int\"},{\"pType\":\"establishDate\",\"name\":\"establishDate\",\"type\":\"date\"},{\"pType\":\"description\",\"name\":\"description\",\"type\":\"string\"},{\"pType\":\"iconId\",\"name\":\"iconId\",\"type\":\"string\"},{\"pType\":\"url\",\"name\":\"url\",\"type\":\"string\"},{\"pType\":\"king\",\"name\":\"king\",\"type\":\"string\"},{\"pType\":\"queen\",\"name\":\"queen\",\"type\":\"string\"},{\"pType\":\"independenceDay\",\"name\":\"independenceDay\",\"type\":\"string\"},{\"pType\":\"funds\",\"name\":\"funds\",\"type\":\"float\"},{\"pType\":\"color\",\"name\":\"color\",\"type\":\"TYPE_Color\"},{\"pType\":\"date\",\"name\":\"date\",\"type\":\"date\"},{\"pType\":\"startDate\",\"name\":\"startDate\",\"type\":\"date\"},{\"pType\":\"endDate\",\"name\":\"endDate\",\"type\":\"date\"},{\"pType\":\"temperature\",\"name\":\"temperature\",\"type\":\"int\"},{\"pType\":\"timestamp\",\"name\":\"timestamp\",\"type\":\"date\"},{\"pType\":\"power\",\"name\":\"power\",\"type\":\"int\"},{\"pType\":\"id\",\"name\":\"id\",\"type\":\"string\"},{\"pType\":\"type\",\"name\":\"type\",\"type\":\"string\"}],\"enumeratedTypes\":[{\"eType\":\"TYPE_Gender\",\"values\":[{\"val\":0,\"name\":\"MALE\"},{\"val\":1,\"name\":\"FEMALE\"},{\"val\":2,\"name\":\"OTHER\"}]},{\"eType\":\"TYPE_Color\",\"values\":[{\"val\":0,\"name\":\"RED\"},{\"val\":1,\"name\":\"BLUE\"},{\"val\":2,\"name\":\"GREEN\"},{\"val\":3,\"name\":\"YELLOW\"}]}],\"compositeTypes\":[]}]",result);
                        return contentResponse.getData() != null;
                    } catch (Exception e) {
                        e.printStackTrace();
                        return false;
                    }
                }))
                .statusCode(200)
                .contentType("application/json;charset=UTF-8");
    }
    @Test
    /**
     * execute query with expected plan result
     */
    public void catalogSchemas() throws IOException {
        Ontology ontology = OntologyFinalizer.finalize(TestUtils.loadOntology("Dragons.json"));
        given()
                .contentType("application/json")
                .header(new Header("fuse-external-id", "test"))
                .with().port(8888)
                .get("/fuse/catalog/schema")
                .then()
                .assertThat()
                .body(new TestUtils.ContentMatcher(o -> {
                    try {
                        ContentResponse contentResponse = new ObjectMapper().readValue(o.toString(), ContentResponse.class);
                        String result = new ObjectMapper().writeValueAsString(contentResponse.getData());
                        Assert.assertEquals("[\"{\\\"vertexSchemas\\\":[{\\\"label\\\":\\\"Kingdom\\\",\\\"constraint\\\":{\\\"traversalConstraint\\\":{\\\"traversal\\\":\\\"[HasStep([~label.eq(Kingdom)])]\\\"}},\\\"routing\\\":{\\\"present\\\":false},\\\"indexPartitions\\\":{\\\"present\\\":true},\\\"properties\\\":[{\\\"name\\\":\\\"independenceDay\\\",\\\"type\\\":\\\"string\\\",\\\"indexingSchemes\\\":[{\\\"type\\\":\\\"exact\\\",\\\"name\\\":\\\"independenceDay\\\"}]},{\\\"name\\\":\\\"king\\\",\\\"type\\\":\\\"string\\\",\\\"indexingSchemes\\\":[{\\\"type\\\":\\\"exact\\\",\\\"name\\\":\\\"king\\\"}]},{\\\"name\\\":\\\"queen\\\",\\\"type\\\":\\\"string\\\",\\\"indexingSchemes\\\":[{\\\"type\\\":\\\"exact\\\",\\\"name\\\":\\\"queen\\\"}]},{\\\"name\\\":\\\"name\\\",\\\"type\\\":\\\"string\\\",\\\"indexingSchemes\\\":[{\\\"type\\\":\\\"exact\\\",\\\"name\\\":\\\"name\\\"}]},{\\\"name\\\":\\\"funds\\\",\\\"type\\\":\\\"float\\\",\\\"indexingSchemes\\\":[{\\\"type\\\":\\\"exact\\\",\\\"name\\\":\\\"funds\\\"}]},{\\\"name\\\":\\\"id\\\",\\\"type\\\":\\\"string\\\",\\\"indexingSchemes\\\":[{\\\"type\\\":\\\"exact\\\",\\\"name\\\":\\\"id\\\"}]}],\\\"schemaElementType\\\":\\\"org.apache.tinkerpop.gremlin.structure.Vertex\\\"},{\\\"label\\\":\\\"Horse\\\",\\\"constraint\\\":{\\\"traversalConstraint\\\":{\\\"traversal\\\":\\\"[HasStep([~label.eq(Horse)])]\\\"}},\\\"routing\\\":{\\\"present\\\":false},\\\"indexPartitions\\\":{\\\"present\\\":true},\\\"properties\\\":[{\\\"name\\\":\\\"distance\\\",\\\"type\\\":\\\"int\\\",\\\"indexingSchemes\\\":[{\\\"type\\\":\\\"exact\\\",\\\"name\\\":\\\"distance\\\"}]},{\\\"name\\\":\\\"name\\\",\\\"type\\\":\\\"string\\\",\\\"indexingSchemes\\\":[{\\\"type\\\":\\\"exact\\\",\\\"name\\\":\\\"name\\\"}]},{\\\"name\\\":\\\"weight\\\",\\\"type\\\":\\\"int\\\",\\\"indexingSchemes\\\":[{\\\"type\\\":\\\"exact\\\",\\\"name\\\":\\\"weight\\\"}]},{\\\"name\\\":\\\"id\\\",\\\"type\\\":\\\"string\\\",\\\"indexingSchemes\\\":[{\\\"type\\\":\\\"exact\\\",\\\"name\\\":\\\"id\\\"}]},{\\\"name\\\":\\\"maxSpeed\\\",\\\"type\\\":\\\"int\\\",\\\"indexingSchemes\\\":[{\\\"type\\\":\\\"exact\\\",\\\"name\\\":\\\"maxSpeed\\\"}]}],\\\"schemaElementType\\\":\\\"org.apache.tinkerpop.gremlin.structure.Vertex\\\"},{\\\"label\\\":\\\"Guild\\\",\\\"constraint\\\":{\\\"traversalConstraint\\\":{\\\"traversal\\\":\\\"[HasStep([~label.eq(Guild)])]\\\"}},\\\"routing\\\":{\\\"present\\\":false},\\\"indexPartitions\\\":{\\\"present\\\":true},\\\"properties\\\":[{\\\"name\\\":\\\"iconId\\\",\\\"type\\\":\\\"string\\\",\\\"indexingSchemes\\\":[{\\\"type\\\":\\\"exact\\\",\\\"name\\\":\\\"iconId\\\"}]},{\\\"name\\\":\\\"name\\\",\\\"type\\\":\\\"string\\\",\\\"indexingSchemes\\\":[{\\\"type\\\":\\\"exact\\\",\\\"name\\\":\\\"name\\\"}]},{\\\"name\\\":\\\"description\\\",\\\"type\\\":\\\"string\\\",\\\"indexingSchemes\\\":[{\\\"type\\\":\\\"exact\\\",\\\"name\\\":\\\"description\\\"}]},{\\\"name\\\":\\\"id\\\",\\\"type\\\":\\\"string\\\",\\\"indexingSchemes\\\":[{\\\"type\\\":\\\"exact\\\",\\\"name\\\":\\\"id\\\"}]},{\\\"name\\\":\\\"establishDate\\\",\\\"type\\\":\\\"date\\\",\\\"indexingSchemes\\\":[{\\\"type\\\":\\\"exact\\\",\\\"name\\\":\\\"establishDate\\\"}]},{\\\"name\\\":\\\"url\\\",\\\"type\\\":\\\"string\\\",\\\"indexingSchemes\\\":[{\\\"type\\\":\\\"exact\\\",\\\"name\\\":\\\"url\\\"}]}],\\\"schemaElementType\\\":\\\"org.apache.tinkerpop.gremlin.structure.Vertex\\\"},{\\\"label\\\":\\\"Person\\\",\\\"constraint\\\":{\\\"traversalConstraint\\\":{\\\"traversal\\\":\\\"[HasStep([~label.eq(Person)])]\\\"}},\\\"routing\\\":{\\\"present\\\":false},\\\"indexPartitions\\\":{\\\"present\\\":true},\\\"properties\\\":[{\\\"name\\\":\\\"firstName\\\",\\\"type\\\":\\\"string\\\",\\\"indexingSchemes\\\":[{\\\"type\\\":\\\"exact\\\",\\\"name\\\":\\\"firstName\\\"}]},{\\\"name\\\":\\\"lastName\\\",\\\"type\\\":\\\"string\\\",\\\"indexingSchemes\\\":[{\\\"type\\\":\\\"exact\\\",\\\"name\\\":\\\"lastName\\\"}]},{\\\"name\\\":\\\"gender\\\",\\\"type\\\":\\\"TYPE_Gender\\\",\\\"indexingSchemes\\\":[{\\\"type\\\":\\\"exact\\\",\\\"name\\\":\\\"gender\\\"}]},{\\\"name\\\":\\\"deathDate\\\",\\\"type\\\":\\\"string\\\",\\\"indexingSchemes\\\":[{\\\"type\\\":\\\"exact\\\",\\\"name\\\":\\\"deathDate\\\"}]},{\\\"name\\\":\\\"name\\\",\\\"type\\\":\\\"string\\\",\\\"indexingSchemes\\\":[{\\\"type\\\":\\\"exact\\\",\\\"name\\\":\\\"name\\\"}]},{\\\"name\\\":\\\"id\\\",\\\"type\\\":\\\"string\\\",\\\"indexingSchemes\\\":[{\\\"type\\\":\\\"exact\\\",\\\"name\\\":\\\"id\\\"}]},{\\\"name\\\":\\\"birthDate\\\",\\\"type\\\":\\\"date\\\",\\\"indexingSchemes\\\":[{\\\"type\\\":\\\"exact\\\",\\\"name\\\":\\\"birthDate\\\"}]},{\\\"name\\\":\\\"height\\\",\\\"type\\\":\\\"int\\\",\\\"indexingSchemes\\\":[{\\\"type\\\":\\\"exact\\\",\\\"name\\\":\\\"height\\\"}]}],\\\"schemaElementType\\\":\\\"org.apache.tinkerpop.gremlin.structure.Vertex\\\"},{\\\"label\\\":\\\"Dragon\\\",\\\"constraint\\\":{\\\"traversalConstraint\\\":{\\\"traversal\\\":\\\"[HasStep([~label.eq(Dragon)])]\\\"}},\\\"routing\\\":{\\\"present\\\":false},\\\"indexPartitions\\\":{\\\"present\\\":true},\\\"properties\\\":[{\\\"name\\\":\\\"gender\\\",\\\"type\\\":\\\"TYPE_Gender\\\",\\\"indexingSchemes\\\":[{\\\"type\\\":\\\"exact\\\",\\\"name\\\":\\\"gender\\\"}]},{\\\"name\\\":\\\"color\\\",\\\"type\\\":\\\"TYPE_Color\\\",\\\"indexingSchemes\\\":[{\\\"type\\\":\\\"exact\\\",\\\"name\\\":\\\"color\\\"}]},{\\\"name\\\":\\\"name\\\",\\\"type\\\":\\\"string\\\",\\\"indexingSchemes\\\":[{\\\"type\\\":\\\"exact\\\",\\\"name\\\":\\\"name\\\"}]},{\\\"name\\\":\\\"id\\\",\\\"type\\\":\\\"string\\\",\\\"indexingSchemes\\\":[{\\\"type\\\":\\\"exact\\\",\\\"name\\\":\\\"id\\\"}]},{\\\"name\\\":\\\"power\\\",\\\"type\\\":\\\"int\\\",\\\"indexingSchemes\\\":[{\\\"type\\\":\\\"exact\\\",\\\"name\\\":\\\"power\\\"}]},{\\\"name\\\":\\\"birthDate\\\",\\\"type\\\":\\\"date\\\",\\\"indexingSchemes\\\":[{\\\"type\\\":\\\"exact\\\",\\\"name\\\":\\\"birthDate\\\"}]}],\\\"schemaElementType\\\":\\\"org.apache.tinkerpop.gremlin.structure.Vertex\\\"}],\\\"edgeSchemas\\\":[{\\\"label\\\":\\\"fire\\\",\\\"constraint\\\":{\\\"traversalConstraint\\\":{\\\"traversal\\\":\\\"[HasStep([~label.eq(fire)])]\\\"}},\\\"routing\\\":{\\\"present\\\":false},\\\"indexPartitions\\\":{\\\"present\\\":true},\\\"properties\\\":[{\\\"name\\\":\\\"date\\\",\\\"type\\\":\\\"date\\\",\\\"indexingSchemes\\\":[{\\\"type\\\":\\\"exact\\\",\\\"name\\\":\\\"date\\\"}]},{\\\"name\\\":\\\"temperature\\\",\\\"type\\\":\\\"int\\\",\\\"indexingSchemes\\\":[{\\\"type\\\":\\\"exact\\\",\\\"name\\\":\\\"temperature\\\"}]},{\\\"name\\\":\\\"id\\\",\\\"type\\\":\\\"string\\\",\\\"indexingSchemes\\\":[{\\\"type\\\":\\\"exact\\\",\\\"name\\\":\\\"id\\\"}]}],\\\"endA\\\":{\\\"present\\\":true},\\\"endB\\\":{\\\"present\\\":true},\\\"directionSchema\\\":{\\\"present\\\":true},\\\"direction\\\":\\\"OUT\\\",\\\"applications\\\":[\\\"endA\\\"],\\\"schemaElementType\\\":\\\"org.apache.tinkerpop.gremlin.structure.Edge\\\"},{\\\"label\\\":\\\"fire\\\",\\\"constraint\\\":{\\\"traversalConstraint\\\":{\\\"traversal\\\":\\\"[HasStep([~label.eq(fire)])]\\\"}},\\\"routing\\\":{\\\"present\\\":false},\\\"indexPartitions\\\":{\\\"present\\\":true},\\\"properties\\\":[{\\\"name\\\":\\\"date\\\",\\\"type\\\":\\\"date\\\",\\\"indexingSchemes\\\":[{\\\"type\\\":\\\"exact\\\",\\\"name\\\":\\\"date\\\"}]},{\\\"name\\\":\\\"temperature\\\",\\\"type\\\":\\\"int\\\",\\\"indexingSchemes\\\":[{\\\"type\\\":\\\"exact\\\",\\\"name\\\":\\\"temperature\\\"}]},{\\\"name\\\":\\\"id\\\",\\\"type\\\":\\\"string\\\",\\\"indexingSchemes\\\":[{\\\"type\\\":\\\"exact\\\",\\\"name\\\":\\\"id\\\"}]}],\\\"endA\\\":{\\\"present\\\":true},\\\"endB\\\":{\\\"present\\\":true},\\\"directionSchema\\\":{\\\"present\\\":true},\\\"direction\\\":\\\"IN\\\",\\\"applications\\\":[\\\"endA\\\"],\\\"schemaElementType\\\":\\\"org.apache.tinkerpop.gremlin.structure.Edge\\\"},{\\\"label\\\":\\\"originatedIn\\\",\\\"constraint\\\":{\\\"traversalConstraint\\\":{\\\"traversal\\\":\\\"[HasStep([~label.eq(originatedIn)])]\\\"}},\\\"routing\\\":{\\\"present\\\":false},\\\"indexPartitions\\\":{\\\"present\\\":true},\\\"properties\\\":[{\\\"name\\\":\\\"id\\\",\\\"type\\\":\\\"string\\\",\\\"indexingSchemes\\\":[{\\\"type\\\":\\\"exact\\\",\\\"name\\\":\\\"id\\\"}]},{\\\"name\\\":\\\"startDate\\\",\\\"type\\\":\\\"date\\\",\\\"indexingSchemes\\\":[{\\\"type\\\":\\\"exact\\\",\\\"name\\\":\\\"startDate\\\"}]}],\\\"endA\\\":{\\\"present\\\":true},\\\"endB\\\":{\\\"present\\\":true},\\\"directionSchema\\\":{\\\"present\\\":true},\\\"direction\\\":\\\"OUT\\\",\\\"applications\\\":[\\\"endA\\\"],\\\"schemaElementType\\\":\\\"org.apache.tinkerpop.gremlin.structure.Edge\\\"},{\\\"label\\\":\\\"originatedIn\\\",\\\"constraint\\\":{\\\"traversalConstraint\\\":{\\\"traversal\\\":\\\"[HasStep([~label.eq(originatedIn)])]\\\"}},\\\"routing\\\":{\\\"present\\\":false},\\\"indexPartitions\\\":{\\\"present\\\":true},\\\"properties\\\":[{\\\"name\\\":\\\"id\\\",\\\"type\\\":\\\"string\\\",\\\"indexingSchemes\\\":[{\\\"type\\\":\\\"exact\\\",\\\"name\\\":\\\"id\\\"}]},{\\\"name\\\":\\\"startDate\\\",\\\"type\\\":\\\"date\\\",\\\"indexingSchemes\\\":[{\\\"type\\\":\\\"exact\\\",\\\"name\\\":\\\"startDate\\\"}]}],\\\"endA\\\":{\\\"present\\\":true},\\\"endB\\\":{\\\"present\\\":true},\\\"directionSchema\\\":{\\\"present\\\":true},\\\"direction\\\":\\\"IN\\\",\\\"applications\\\":[\\\"endA\\\"],\\\"schemaElementType\\\":\\\"org.apache.tinkerpop.gremlin.structure.Edge\\\"}],\\\"vertexLabels\\\":[\\\"Kingdom\\\",\\\"Horse\\\",\\\"Guild\\\",\\\"Person\\\",\\\"Dragon\\\"],\\\"edgeLabels\\\":[\\\"fire\\\",\\\"originatedIn\\\"],\\\"labelFieldName\\\":{\\\"present\\\":false}}\"]",result);
                        return contentResponse.getData() != null;
                    } catch (Exception e) {
                        e.printStackTrace();
                        return false;
                    }
                }))
                .statusCode(200)
                .contentType("application/json;charset=UTF-8");
    }

}

