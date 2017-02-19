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
                .body("{\"id\":1," +
                        "\"name\": \"hezi\"," +
                        "\"type\": \"path\"," +
                        "\"query\": \"build me a graph!\" " +
                        "}")
                .post("/fuse/query/path")
                .then()
                .assertThat()
                .body(equalTo("{\"id\":\"1\",\"name\":\"hezi\",\"content\":{" +
                        "\"completed\":true,\"url\":\"http://localhost:8080/fuse/result/1\",\"id\":\"1\",\"data\":\"Simple Path Data\",\"results\":16}}"))
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
                .body("{\"id\":1," +
                        "\"name\": \"hezi\"," +
                        "\"type\": \"graph\"," +
                        "\"query\": \"build me a graph!\" " +
                        "}")
                .post("/fuse/query/graph")
                .then()
                .assertThat()
                .body(equalTo("{\"id\":\"1\",\"name\":\"hezi\",\"content\":{" +
                        "\"completed\":true,\"url\":\"http://localhost:8080/fuse/result/1\",\"id\":\"1\",\"data\":\"Simple Graph Data\",\"results\":17}}"))
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
                .body("{\"id\":1," +
                        "\"name\": \"hezi\"," +
                        "\"type\": \"plan\"," +
                        "\"query\": \"plan me a graph!\" " +
                        "}")
                .post("/fuse/plan")
                .then()
                .assertThat()
                .body(equalTo("{\"id\":\"1\",\"name\":\"hezi\",\"content\":{\"data\":\"Simple Plan\",\"id\":\"1\",\"completed\":true,\"results\":11}}"))
                .statusCode(201)
                .contentType("application/json;charset=UTF-8");
    }

}
