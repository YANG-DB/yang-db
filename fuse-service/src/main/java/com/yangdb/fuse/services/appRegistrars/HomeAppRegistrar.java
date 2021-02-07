package com.yangdb.fuse.services.appRegistrars;

/*-
 * #%L
 * fuse-service
 * %%
 * Copyright (C) 2016 - 2019 The YangDb Graph Database Project
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import com.yangdb.fuse.dispatcher.urlSupplier.AppUrlSupplier;
import org.jooby.Jooby;
import org.jooby.Result;
import org.jooby.Results;

public class HomeAppRegistrar implements AppRegistrar{
    //region AppRegistrar Implementation
    @Override
    public void register(Jooby app, AppUrlSupplier appUrlSupplier) {
        app.get("/", () -> HOME);
    }
    //endregion

    //region Fields
    private static Result HOME = Results
            .ok(
                    "<!doctype html>\n" +
                            "<html lang=\"en\">\n" +
                            "<head>\n" +
                            "  <title>YangDb API</title>\n" +
                            "</head>\n" +
                            "<body>\n" +
                            "<h1>YangDb API</h1>\n" +
                            "<ul>\n" +
                            "<li>Resource Url: <a href=\"/fuse\">Fuse</a></li>\n" +
                            "<li>Swagger API: <a href=\"/swagger\">Swagger</a></li>\n" +
                            "<li>Redocly API: <a href=\"/redocly/redocly\">Redocly</a></li>\n" +
                            "<li><hr></li>"+
                            "<li>Graphql-Queries builder: <a href=\"queryBuilder/graphql\">graphql-query builder</a></li>\n" +
                            "<li>Sparql-Queries builder: <a href=\"queryBuilder/sparql\">sparql-query builder</a></li>\n" +
                            "<li>Cypher-Queries builder: <a href=\"queryBuilder/cypher\">cypher-query builder</a></li>\n" +
                            "<li><hr></li>"+
                            "<li>BigDesk E/S online Monitor: <a href=\"/bigdesk\">bigDesk</a></li>\n" +
                            "<li><hr></li>"+
                            "<li>Health Url: <a href=\"/fuse/health\">healthUrl</a></li>\n" +
                            "<li>Statistics Url: <a href=\"/sys/metrics\">statistics</a></li>\n" +
                            "<li><hr></li>"+
                            "<li>Query Store Url: <a href=\"/fuse/query\">queryStoreUrl</a></li>\n" +
                            "</ul>\n" +
                            "<p>More at <a href=\"http://yangdb.org\">" +
                            "www.yangdb.org</a>\n" +
                            "</body>\n" +
                            "</html>")
            .type("html");
    //endregion
}
