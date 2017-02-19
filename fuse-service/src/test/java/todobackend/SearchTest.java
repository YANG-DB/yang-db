package todobackend;

import com.kayhut.fuse.services.FuseApp;
import org.jooby.test.JoobyRule;
import org.junit.ClassRule;
import org.junit.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

public class SearchTest {

    @ClassRule
    public static JoobyRule app = new JoobyRule(new FuseApp());

    @Test
    /**
     * execute query with expected plan result
     */
    public void search() {
        given()
                .contentType("application/json")
                .body("{\"id\":1," +
                        "\"name\": \"hezi\"," +
                        "\"type\": \"search\"," +
                        "\"query\": \"plan me a graph!\" " +
                        "}")
                .post("/fuse/search")
                .then()
                .assertThat()
                .body(equalTo("{\"id\":\"1\",\"name\":\"hezi\",\"content\":{" +
                        "\"completed\":true,\"url\":\"http://localhost:8080/fuse/result/1\",\"id\":\"1\",\"data\":\"Simple Graph Data\",\"results\":17}}"))
                .statusCode(201)
                .contentType("application/json;charset=UTF-8");
    }

}
