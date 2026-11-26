package com.yangdb.fuse.services;

/*-
 * #%L
 * fuse-service
 * %%
 * Copyright (C) 2016 - 2026 The YangDb Graph Database Project
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

/**
 * The API page at {@code /swagger}: Swagger UI 5 (it renders OpenAPI 3.1) for the spec the distribution ships at
 * {@value #SPEC}. The UI's files come from the swagger-ui webjar, whose version must be {@value #VERSION}.
 */
public final class SwaggerUiPage {
    /** the swagger-ui webjar version in fuse-service's pom */
    public static final String VERSION = "5.35.1";
    /** where the webjar keeps the UI's files on the classpath */
    public static final String WEBJAR = "/META-INF/resources/webjars/swagger-ui/" + VERSION + "/";
    /** where this service serves them */
    public static final String UI = "/swagger/ui/";
    /** the OpenAPI spec, a static file of the distribution */
    public static final String SPEC = "/public/assets/swagger/openapi.json";

    private SwaggerUiPage() {
    }

    public static String html() {
        return "<!DOCTYPE html>\n<html lang=\"en\"><head><meta charset=\"UTF-8\"><title>YangDB API</title>\n"
                + "<link rel=\"stylesheet\" href=\"" + UI + "swagger-ui.css\"></head>\n<body><div id=\"swagger-ui\"></div>\n"
                + "<script src=\"" + UI + "swagger-ui-bundle.js\"></script>\n<script>\n"
                + "window.ui = SwaggerUIBundle({url: '" + SPEC + "', dom_id: '#swagger-ui',\n"
                + "  docExpansion: 'list', defaultModelsExpandDepth: -1, displayRequestDuration: true});\n"
                + "</script></body></html>\n";
    }
}
