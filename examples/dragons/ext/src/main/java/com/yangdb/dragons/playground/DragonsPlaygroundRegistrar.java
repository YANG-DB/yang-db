package com.yangdb.dragons.playground;

/*-
 * #%L
 * fuse-domain-dragons-ext
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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.TypeLiteral;
import com.yangdb.fuse.dispatcher.urlSupplier.AppUrlSupplier;
import com.yangdb.fuse.executor.ontology.schema.load.GraphDataLoader;
import com.yangdb.fuse.model.ontology.Ontology;
import com.yangdb.fuse.model.resourceInfo.FuseError;
import com.yangdb.fuse.model.results.LoadResponse;
import com.yangdb.fuse.model.transport.ContentResponse;
import com.yangdb.fuse.services.appRegistrars.AppControllerRegistrarBase;
import com.yangdb.fuse.services.controllers.CatalogController;
import com.yangdb.fuse.services.controllers.DataLoaderController;
import com.yangdb.fuse.services.controllers.IdGeneratorController;
import javaslang.collection.Stream;
import org.jooby.Jooby;
import org.jooby.MediaType;
import org.jooby.Results;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * The Dragons playground: a Swagger UI page at {@code /dragons/swagger} with the tutorial's queries as ready-made
 * examples, plus {@code POST /fuse/dragons/tutorial/load}, which runs the whole tutorial setup in one call.
 * <p>
 * The service registers it through the Dragons extension ({@code com.yangdb.dragons.DragonsExtension}, listed in
 * {@code META-INF/services/com.yangdb.fuse.extension.YangDbExtension}). The spec ({@code dragons/playground/openapi.json}) is also the e2e suite's list of queries to verify.
 */
public class DragonsPlaygroundRegistrar extends AppControllerRegistrarBase<DataLoaderController> {
    static final String ONTOLOGY = "Dragons";
    /** Swagger UI 5, unpacked into this jar by the pom (not a dependency: that would replace ApiTool's 3.17.1) */
    static final String SWAGGER_UI = "/dragons/playground/ui/";
    static final String SPEC = "/dragons/playground/openapi.json";
    static final String TUTORIAL = "/dragons/tutorial/";

    /** csv file, vertex|edge, ontology label: the same order as the tutorial (entities before relationships) */
    static final String[][] FILES = {
            {"Dragons.csv", "vertex", "Dragon"}, {"Person.csv", "vertex", "Person"}, {"horses.csv", "vertex", "Horse"},
            {"Kingdoms.csv", "vertex", "Kingdom"}, {"Guilds.csv", "vertex", "Guild"},
            {"Fire.csv", "edge", "Fire"}, {"Freeze.csv", "edge", "Freeze"}, {"KnowsPerson.csv", "edge", "Know"},
            {"MembersOfGuilds.csv", "edge", "MemberOf"}, {"OriginatedDragons.csv", "edge", "OriginatedIn"},
            {"OriginatedHorse.csv", "edge", "OriginatedIn"}, {"OwnsDragon.csv", "edge", "Own"},
            {"OwnsHorse.csv", "edge", "Own"}, {"RegisteredGuilds.csv", "edge", "RegisteredIn"},
            {"SubjectOfPerson.csv", "edge", "SubjectOf"}};

    public DragonsPlaygroundRegistrar() {
        super(DataLoaderController.class);
    }

    @Override
    public void register(Jooby app, AppUrlSupplier appUrlSupplier) {
        app.get("/dragons/swagger", () -> Results.ok(page()).type(MediaType.html));
        app.get("/dragons/swagger/openapi.json", () -> Results.ok(resource(SPEC)).type(MediaType.json));
        app.assets("/dragons/swagger/ui/**", SWAGGER_UI + "{0}");

        app.post("/fuse/dragons/tutorial/load", req -> Results.json(loadTutorial(app,
                !req.param("reset").isSet() || req.param("reset").booleanValue())));
    }

    /** idgen init -> register the ontology -> (drop) -> create indices -> every tutorial CSV, with a per-step summary */
    Map<String, Object> loadTutorial(Jooby app, boolean reset) throws IOException {
        DataLoaderController loader = getController(app);
        List<Map<String, Object>> steps = new ArrayList<>();

        // the id generator only needs to be initialized once per cluster: on a second run it throws "already exists"
        steps.add(step("idgen/init", () -> app.require(Key.ID_GENERATOR).init(
                Arrays.asList("Entity", "Relation", "Evalue", "Rvalue", "workerId")), true));

        Ontology ontology = new ObjectMapper().readValue(resource(TUTORIAL + "Dragons.json"), Ontology.class);
        steps.add(step("catalog/ontology", () -> app.require(CatalogController.class).addOntology(ontology), false));
        if (reset) {
            steps.add(step("drop indices", () -> loader.drop(ONTOLOGY), true));
        }
        steps.add(step("create indices", () -> loader.init(ONTOLOGY), false));

        int failed = (int) steps.stream().filter(s -> s.containsKey("error")).count();
        for (String[] file : FILES) {
            String csv = resource(TUTORIAL + "csv/" + file[0]);
            ContentResponse<LoadResponse<String, FuseError>> response =
                    loader.loadCsv(ONTOLOGY, file[1], file[2], csv, GraphDataLoader.Directive.INSERT);
            Map<String, Object> summary = loadSummary(file[0], file[2], csv, response);
            failed += ((Number) summary.get("failures")).intValue() + (summary.containsKey("error") ? 1 : 0);
            steps.add(summary);
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("ok", failed == 0);
        result.put("next", "Open GET /query/cypher/run in /dragons/swagger and pick a query from the Examples list");
        result.put("steps", steps);
        return result;
    }

    private static Map<String, Object> loadSummary(String file, String label, String csv, ContentResponse<LoadResponse<String, FuseError>> response) {
        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("file", file);
        summary.put("label", label);
        summary.put("rows", Math.max(0, csv.split("\r?\n").length - 1));
        int successes = 0, failures = 0;
        List<String> sample = new ArrayList<>();
        if (response.getData() != null) {
            for (LoadResponse.CommitResponse<String, FuseError> commit : response.getData().getResponses()) {
                successes += commit.getSuccesses().size();
                failures += commit.getFailures().size();
                Stream.ofAll(commit.getFailures()).take(3 - sample.size())
                        .forEach(f -> sample.add(f.getErrorDescription()));
            }
        }
        summary.put("successes", successes);
        summary.put("failures", failures);
        if (!sample.isEmpty()) summary.put("failureSample", sample);
        if (response.getError() != null) summary.put("error", String.valueOf(response.getError().getMessage()));
        return summary;
    }

    /**
     * Runs one setup step and records its outcome. Controllers report problems both ways: as an error in the response
     * and as an exception (e.g. idgen init when the index exists), so both are caught. With errorIsOk the problem is
     * recorded as a note (expected on a second run) instead of an error.
     */
    static Map<String, Object> step(String name, java.util.concurrent.Callable<ContentResponse<?>> action, boolean errorIsOk) {
        Map<String, Object> step = new LinkedHashMap<>();
        step.put("step", name);
        String problem;
        try {
            ContentResponse<?> response = action.call();
            step.put("status", response.status() == null ? "?" : response.status().toString());
            problem = response.getError() == null ? null : String.valueOf(response.getError().getMessage());
        } catch (Exception e) {
            step.put("status", "exception");
            problem = e.getClass().getSimpleName() + ": " + e.getMessage();
        }
        if (problem != null) {
            step.put(errorIsOk ? "note" : "error", problem);
        }
        return step;
    }

    /** the Swagger UI page, configured for the playground spec, with "Try it out" switched on */
    static String page() {
        String ui = "/dragons/swagger/ui/";
        return "<!DOCTYPE html>\n<html lang=\"en\"><head><meta charset=\"UTF-8\"><title>YANG-DB Dragons playground</title>\n"
                + "<link rel=\"stylesheet\" href=\"" + ui + "swagger-ui.css\"></head>\n<body><div id=\"swagger-ui\"></div>\n"
                + "<script src=\"" + ui + "swagger-ui-bundle.js\"></script>\n<script>\n"
                + "window.ui = SwaggerUIBundle({url: '/dragons/swagger/openapi.json', dom_id: '#swagger-ui',\n"
                + "  tryItOutEnabled: true, docExpansion: 'list', defaultModelsExpandDepth: -1, displayRequestDuration: true});\n"
                + "</script></body></html>\n";
    }

    static String resource(String path) throws IOException {
        try (InputStream in = DragonsPlaygroundRegistrar.class.getResourceAsStream(path)) {
            if (in == null) throw new IOException("missing classpath resource " + path);
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    /** Guice key of the id generator controller, which is bound with its generic type */
    private static final class Key {
        @SuppressWarnings("unchecked")
        static final com.google.inject.Key<IdGeneratorController<Object>> ID_GENERATOR =
                (com.google.inject.Key<IdGeneratorController<Object>>) com.google.inject.Key.get(
                        new TypeLiteral<IdGeneratorController<com.yangdb.fuse.model.Range>>() {}.getType());
    }
}
