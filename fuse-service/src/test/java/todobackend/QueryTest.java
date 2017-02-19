package todobackend;

import com.kayhut.fuse.services.FuseApp;
import org.jooby.test.JoobyRule;
import org.junit.ClassRule;
import org.junit.Test;

import static io.restassured.RestAssured.get;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

public class QueryTest {

    @ClassRule
    public static JoobyRule app = new JoobyRule(new FuseApp());

    @Test
    public void checkHealth() {
        get("/fuse/health")
                .then()
                .assertThat()
                .body(equalTo("\"Alive And Well...\""))
                .header("Access-Control-Allow-Origin", equalTo("*"))
                .header("Access-Control-Allow-Methods", equalTo("POST, GET, OPTIONS, DELETE, PATCH"))
                .header("Access-Control-Max-Age", equalTo("3600"))
                .header("Access-Control-Allow-Headers", "accept")
                .statusCode(200)
                .contentType("application/json;charset=UTF-8");
    }

    /**
     * execute query with expected path result
     */
    @Test
    public void queryWithPathResults() {
        given()
                .contentType("application/json")
                .body("{" +
                        "\"name\": \"hezi\"," +
                        "\"type\": \"path\"," +
                        "\"query\": \"build me a graph!\" " +
                        "}")
                .post("/fuse/query")
                .then()
                .assertThat()
                .body(equalTo(
                        "{\"id\":1," +
                                "\"name\":\"hezi\"," +
                                "\"type\": \"path\"," +
                                "\"completed\":true," +
                                "\"results\":0," +
                                "\"url\":\"http://localhost:8080/fuse/result/1\"}"))
                .statusCode(201)
                .contentType("application/json;charset=UTF-8");

    }

    /**
     * execute query with expected graph result
     */
    @Test
    public void queryWithGraphResults() {
        given()
                .contentType("application/json")
                .body("{" +
                        "\"name\": \"hezi\"," +
                        "\"type\": \"graph\"," +
                        "\"query\": \"build me a graph!\" " +
                        "}")
                .post("/fuse/query")
                .then()
                .assertThat()
                .body(equalTo(
                        "{\"id\":1," +
                                "\"name\":\"hezi\"," +
                                "\"type\": \"graph\"," +
                                "\"completed\":true," +
                                "\"results\":0," +
                                "\"url\":\"http://localhost:8080/fuse/result/1\"}"))
                .statusCode(201)
                .contentType("application/json;charset=UTF-8");
    }

    @Test
    /**
     * execute query with expected plan result
     */
    public void plan() {
        given()
                .contentType("application/json")
                .body("{" +
                        "\"name\": \"hezi\"," +
                        "\"type\": \"plan\"," +
                        "\"query\": \"plan me a graph!\" " +
                        "}")
                .post("/fuse/plan")
                .then()
                .assertThat()
                .body(equalTo(
                        "{\"id\":1," +
                                "\"name\":\"hezi\"," +
                                "\"type\": \"plan\"," +
                                "\"completed\":true," +
                                "\"results\":0," +
                                "\"url\":\"http://localhost:8080/fuse/result/1\"}"))
                .statusCode(201)
                .contentType("application/json;charset=UTF-8");
    }

}
