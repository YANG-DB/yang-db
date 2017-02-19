package com.kayhut.fuse.services;

import com.kayhut.fuse.model.transport.Request;
import org.jooby.Jooby;
import org.jooby.Results;
import org.jooby.Status;
import org.jooby.json.Jackson;
import org.jooby.scanner.Scanner;


@SuppressWarnings({"unchecked", "rawtypes"})
public class FuseApp extends Jooby {

    {
        use(new Scanner());

        /** JSON: */
        use(new Jackson());

        /** CORS: */
        use("*", (req, rsp) -> {
            rsp.header("Access-Control-Allow-Origin", "*");
            rsp.header("Access-Control-Allow-Methods", "POST, GET, OPTIONS, DELETE, PATCH");
            rsp.header("Access-Control-Max-Age", "3600");
            rsp.header("Access-Control-Allow-Headers", "x-requested-with", "origin", "content-type",
                    "accept");
            if (req.method().equalsIgnoreCase("options")) {
                rsp.end();
            }
        });

        /** Fuse API: */
        use("/fuse/health")
                /** check health */
                .get(() -> "Alive And Well...");

        use("/fuse/plan")
                /** submit a plan */
                .post(req -> Results.with(queryCtrl().plan(req.body(Request.class)), Status.CREATED));

        use("/fuse/search")
                /** submit a plan */
                .post(req -> Results.with(searchCtrl().search(req.body(Request.class)), Status.CREATED));

        use("/fuse/query")
                /** submit a path query */
                .post("/path",req -> Results.with(queryCtrl().pathQuery(req.body(Request.class)), Status.CREATED))
                /** submit a graph query */
                .post("/graph",req -> Results.with(queryCtrl().graphQuery(req.body(Request.class)), Status.CREATED));

        use("/fuse/result")
                /** result by ID. */
                .get("/:getId", req -> Results.with(resultsCtrl().get(req.param("getId").value()), Status.FOUND));
    }

    private QueryController queryCtrl() {
        return require(QueryController.class);
    }

    private SearchController searchCtrl() {
        return require(SearchController.class);
    }

    private ResultsController resultsCtrl() {
        return require(ResultsController.class);
    }

    public static void main(final String[] args) {
        run(FuseApp::new, args);
    }

}
