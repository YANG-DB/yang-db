package com.yangdb.fuse.services.appRegistrars;

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

import com.google.inject.ConfigurationException;
import com.yangdb.fuse.dispatcher.query.JsonQueryTransformerFactory;
import com.yangdb.fuse.dispatcher.urlSupplier.AppUrlSupplier;
import com.yangdb.fuse.services.controllers.QueryController;
import org.jooby.Jooby;
import org.jooby.MediaType;
import org.jooby.Request;
import org.jooby.Response;
import org.jooby.Status;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * The generic query routes, for every language in the {@code QueryLanguages} registry:
 * <ul>
 *     <li>{@code POST <queryStore>/:lang}: create a query ({@code CreateJsonQueryRequest} body; its {@code type} is set
 *     to the language)</li>
 *     <li>{@code GET|POST <queryStore>/:lang/run}: run a stateless query and return its first page (query text in the
 *     {@code query} parameter, a parameter named like the language, or the body)</li>
 * </ul>
 * An unknown language gets a 404 that lists the available ones.
 * <p>
 * Jooby matches routes in registration order and these patterns also match fixed paths such as
 * {@code /fuse/query/v1/run} or {@code /fuse/query/call}, so the service registers this registrar <b>after</b> all
 * other registrars (it deliberately isn't an {@code AppControllerRegistrarBase}, which the package scan would pick up
 * in no particular order).
 */
public class QueryLanguageRoutesRegistrar implements AppRegistrar {
    public static final String LANG_KEY = "lang";
    public static final String UNKNOWN_LANGUAGE = "Unknown query language";

    @Override
    public void register(Jooby app, AppUrlSupplier appUrlSupplier) {
        String store = appUrlSupplier.queryStoreUrl();
        app.post(store + "/:" + LANG_KEY, (req, rsp) -> post(app, req, rsp));
        app.get(store + "/:" + LANG_KEY + "/run", (req, rsp) -> run(app, req, rsp));
        app.post(store + "/:" + LANG_KEY + "/run", (req, rsp) -> run(app, req, rsp));
    }

    private static void post(Jooby app, Request req, Response rsp) throws Throwable {
        Optional<String> language = language(app, req, rsp);
        if (language.isPresent()) {
            rsp.send(QueryControllerRegistrar.API.postJsonQuery("postQuery", req, language.get(), controller(app)));
        }
    }

    private static void run(Jooby app, Request req, Response rsp) throws Throwable {
        Optional<String> language = language(app, req, rsp);
        if (language.isPresent()) {
            QueryController controller = controller(app);
            QueryControllerRegistrar.API.runJsonQuery("runQuery", req, rsp, req.param(LANG_KEY).value(),
                    (query, ontology, pageSize, cursorType) -> controller.run(query, language.get(), ontology, pageSize, cursorType));
        }
    }

    private static QueryController controller(Jooby app) {
        return app.require(QueryController.class);
    }

    /** the registered name of the requested language, or empty after sending a 404 that lists the available ones */
    static Optional<String> language(Jooby app, Request req, Response rsp) throws Throwable {
        String requested = req.param(LANG_KEY).value();
        Optional<Set<String>> available = languages(app);
        if (!available.isPresent()) {
            // no language registry bound (a config without M2AsgModule): let the driver report it
            return Optional.of(requested);
        }
        Optional<String> match = available.get().stream().filter(requested::equals).findFirst();
        if (!match.isPresent()) {
            match = available.get().stream().filter(requested::equalsIgnoreCase).findFirst();
        }
        if (!match.isPresent()) {
            Map<String, Object> error = new LinkedHashMap<>();
            error.put("errorCode", UNKNOWN_LANGUAGE);
            error.put("errorDescription", "No query language '" + requested + "'; available languages: " + available.get());
            error.put("languages", available.get());
            rsp.status(Status.NOT_FOUND).type(MediaType.json).send(error);
        }
        return match;
    }

    private static Optional<Set<String>> languages(Jooby app) {
        try {
            return Optional.of(app.require(JsonQueryTransformerFactory.class).languages());
        } catch (ConfigurationException notBound) {
            return Optional.empty();
        }
    }
}
