package com.yangdb.fuse.model;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import com.networknt.schema.ValidationMessage;
import com.yangdb.fuse.model.query.*;
import com.yangdb.fuse.model.query.entity.EConcrete;
import com.yangdb.fuse.model.query.entity.ETyped;
import com.yangdb.fuse.model.query.entity.EUntyped;
import com.yangdb.fuse.model.query.optional.OptionalComp;
import com.yangdb.fuse.model.query.properties.*;
import com.yangdb.fuse.model.query.properties.constraint.Constraint;
import com.yangdb.fuse.model.query.properties.constraint.ConstraintOp;
import com.yangdb.fuse.model.query.properties.constraint.WhereByConstraint;
import com.yangdb.fuse.model.query.properties.projection.IdentityProjection;
import com.yangdb.fuse.model.query.quant.Quant1;
import com.yangdb.fuse.model.query.quant.QuantType;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.Assert.*;

/**
 * Keeps the published V1 query JSON Schema (docs/spec/v1/query.schema.json) in step with the engine:
 * every V1 query in the repo's test resources and docs must validate, so must what the engine's own serializer
 * writes for every core element type, and malformed queries must be rejected.
 * Runs from the fuse-model directory, like every surefire test of this module.
 */
public class V1QuerySchemaTest {
    private static final Path REPO = Paths.get("..").toAbsolutePath().normalize();
    private static final ObjectMapper mapper = new ObjectMapper();
    private static JsonSchema schema;

    @BeforeClass
    public static void loadSchema() throws IOException {
        JsonNode schemaNode = mapper.readTree(REPO.resolve("docs/spec/v1/query.schema.json").toFile());
        schema = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V202012).getSchema(schemaNode);
    }

    private static Set<ValidationMessage> validate(JsonNode query) {
        return schema.validate(query);
    }

    private static void assertValid(String what, JsonNode query) {
        Set<ValidationMessage> errors = validate(query);
        assertTrue(what + " should match the V1 schema, but: " + errors, errors.isEmpty());
    }

    private static void assertInvalid(String what, String json) throws IOException {
        assertFalse(what + " should be rejected by the V1 schema", validate(mapper.readTree(json)).isEmpty());
    }

    /**
     * Unused fixtures in pre-2018 formats, kept as they are:
     * QueryJsons/Q010.json has a "Quant2" element (never implemented, so the engine cannot read the file) and "eType"
     * on relations; queries/Q001.json has "reportProps" (a field the engine ignores) and "wrapper": null.
     */
    private static final Set<String> NOT_V1 = new HashSet<>(Arrays.asList(
            "fuse-model/src/test/resources/QueryJsons/Q010.json",
            "fuse-service/src/test/resources/queries/Q001.json"));

    /** Every *.json V1 query (has "elements") under the given directories. */
    private static List<Path> queryFiles(String... dirs) throws IOException {
        List<Path> files = new ArrayList<>();
        for (String dir : dirs) {
            try (Stream<Path> walk = Files.walk(REPO.resolve(dir))) {
                files.addAll(walk.filter(p -> p.toString().endsWith(".json"))
                        .filter(p -> !p.getFileName().toString().startsWith("._"))
                        .filter(p -> !NOT_V1.contains(REPO.relativize(p).toString()))
                        .filter(p -> read(p).contains("\"elements\""))
                        .collect(Collectors.toList()));
            }
        }
        return files;
    }

    private static String read(Path p) {
        try {
            return new String(Files.readAllBytes(p), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testResourceQueriesMatchSchemaAndEngine() throws IOException {
        List<Path> files = queryFiles("fuse-model/src/test/resources/QueryJsons", "fuse-service/src/test/resources/queries");
        assertTrue("expected the V1 query corpus, found " + files.size(), files.size() >= 8);
        for (Path file : files) {
            JsonNode query = mapper.readTree(file.toFile());
            assertValid(REPO.relativize(file).toString(), query);
            // ... and what the engine reads and writes back must match too
            Query parsed = mapper.treeToValue(query, Query.class);
            assertValid(REPO.relativize(file) + " (re-serialized)", mapper.valueToTree(parsed));
        }
    }

    @Test
    public void docExamplesMatchSchema() throws IOException {
        Pattern block = Pattern.compile("```(?:json)?\\s*\\n(\\{.*?)\\n```", Pattern.DOTALL);
        int checked = 0;
        for (String doc : Arrays.asList("docs/spec/v1/README.md", "docs/spec/v1/rules.md", "docs/info/components/query_language.md",
                "docs/tutorial/architecture/query-lifecycle.md")) {
            Matcher m = block.matcher(read(REPO.resolve(doc)));
            while (m.find()) {
                JsonNode node = mapper.readTree(m.group(1));
                if (node.has("elements") && node.has("ont")) {
                    assertValid(doc + " example #" + (++checked), node);
                }
            }
        }
        assertTrue("expected V1 examples in the docs", checked >= 15);
    }

    /** what GET /fuse/query/v1/schema serves is the docs copy, byte for byte */
    @Test
    public void schemaShippedInTheJarIsTheDocsCopy() {
        assertEquals(read(REPO.resolve("docs/spec/v1/query.schema.json")), com.yangdb.fuse.model.query.V1Spec.querySchema());
    }

    @Test
    public void openApiExamplesMatchSchema() throws IOException {
        JsonNode openapi = mapper.readTree(REPO.resolve("distribution/resources/assembly/Dragons/public/assets/swagger/openapi.json").toFile());
        assertValid("OpenAPI V1Query example", openapi.at("/components/examples/V1Query/value"));
        assertValid("OpenAPI QueryRequest example", openapi.at("/components/schemas/QueryRequest/example/query"));
        // the V1 query body is the published schema, served by the engine
        assertEquals("/fuse/query/v1/schema", openapi.at("/components/schemas/Query/$ref").asText());
    }

    /** The engine's own serializer output, for every core element type, must validate. */
    @Test
    public void engineSerializationOfEveryCoreTypeMatchesSchema() {
        Query q = Query.Builder.instance().withName("core").withOnt("Dragons").withElements(Arrays.asList(
                new Start(0, 1),
                new EUntyped(1, "A", new HashSet<>(Collections.singletonList("Person")), new HashSet<>(Collections.singletonList("Dragon")), 2, 0),
                new Quant1(2, QuantType.all, Arrays.asList(3, 4, 9), 0),
                new EPropGroup(3, QuantType.some, Arrays.asList(
                        EProp.of(31, "name", Constraint.of(ConstraintOp.inRange, Arrays.asList(1, 5), "[)")),
                        EProp.of(32, "age", new IdentityProjection())),
                        Collections.singletonList(new EPropGroup(33, EProp.of(34, "x", Constraint.of(ConstraintOp.notEmpty))))),
                new RelPattern(4, "own", new Range(1, 3), Rel.Direction.R, null, 5, 6),
                new ETyped(5, "B", "Dragon", 0),
                new RelPropGroup(6, RelProp.of(7, "since", Constraint.of(ConstraintOp.gt, 3))),
                new OptionalComp(9, 10),
                new Rel(10, "fire", Rel.Direction.RL, null, 11, 12),
                new EConcrete(11, "C", "Dragon", "d1", "Smaug", 0),
                RelProp.of(12, "temperature", Constraint.of(ConstraintOp.inSet, Arrays.asList(1, 2, 3))),
                EProp.of(13, "color", Constraint.of(ConstraintOp.eq, "red")),
                new RelUntyped(14, new HashSet<>(Arrays.asList("Own", "Know")), Rel.Direction.R, null, 15),
                new EUntyped(15, "D", 0, 0),
                EProp.of(16, "height", WhereByConstraint.of(ConstraintOp.gt, "A", "height"))
        )).build();
        assertValid("serialized core query", mapper.valueToTree(q));
    }

    private static final String HEAD = "{\"ont\":\"Dragons\",\"elements\":[{\"eNum\":0,\"type\":\"Start\",\"next\":1},"
            + "{\"eNum\":1,\"type\":\"ETyped\",\"eTag\":\"A\",\"eType\":\"Person\",\"next\":2},";

    private static String withProp(String con) {
        return HEAD + "{\"eNum\":2,\"type\":\"EProp\",\"pType\":\"name\",\"con\":" + con + "}]}";
    }

    @Test
    public void validMinimalQueries() throws IOException {
        assertValid("eq", mapper.readTree(withProp("{\"type\":\"Constraint\",\"op\":\"eq\",\"expr\":\"Brandon\"}")));
        assertValid("empty", mapper.readTree(withProp("{\"type\":\"Constraint\",\"op\":\"empty\"}")));
        assertValid("in range", mapper.readTree(withProp("{\"type\":\"Constraint\",\"op\":\"in range\",\"expr\":[1,2],\"iType\":\"(]\"}")));
        assertValid("in set", mapper.readTree(withProp("{\"type\":\"Constraint\",\"op\":\"in set\",\"expr\":[\"a\",\"b\",\"c\"]}")));
        assertValid("where-by", mapper.readTree(withProp("{\"type\":\"WhereByConstraint\",\"op\":\"gt\",\"tagEntity\":\"A\",\"projectedField\":\"height\",\"joinType\":\"FOR_EACH\"}")));
        assertValid("extension constraint", mapper.readTree(withProp("{\"type\":\"InnerQueryConstraint\",\"op\":\"eq\",\"tagEntity\":\"B\"}")));
        assertValid("extension element", mapper.readTree(HEAD + "{\"eNum\":2,\"type\":\"HQuant\",\"qType\":\"all\",\"next\":[3]}]}"));
    }

    @Test
    public void malformedQueriesAreRejected() throws IOException {
        assertInvalid("misspelled field (the engine silently drops it)",
                "{\"ont\":\"D\",\"elements\":[{\"eNum\":0,\"type\":\"Start\",\"next\":1},{\"eNum\":1,\"type\":\"ETyped\",\"eTag\":\"A\",\"eTyp\":\"P\"}]}");
        assertInvalid("constraint without type (the engine fails to read it)", withProp("{\"op\":\"eq\",\"expr\":\"x\"}"));
        assertInvalid("unknown operator", withProp("{\"type\":\"Constraint\",\"op\":\"equals\",\"expr\":\"x\"}"));
        assertInvalid("eq with a list", withProp("{\"type\":\"Constraint\",\"op\":\"eq\",\"expr\":[\"x\"]}"));
        assertInvalid("eq without a value", withProp("{\"type\":\"Constraint\",\"op\":\"eq\"}"));
        assertInvalid("empty with a value", withProp("{\"type\":\"Constraint\",\"op\":\"empty\",\"expr\":\"x\"}"));
        assertInvalid("in range with three values", withProp("{\"type\":\"Constraint\",\"op\":\"in range\",\"expr\":[1,2,3]}"));
        assertInvalid("in set with a scalar", withProp("{\"type\":\"Constraint\",\"op\":\"in set\",\"expr\":\"x\"}"));
        assertInvalid("bad interval type", withProp("{\"type\":\"Constraint\",\"op\":\"in range\",\"expr\":[1,2],\"iType\":\"[[\"}"));
        assertInvalid("property with neither con nor proj", HEAD + "{\"eNum\":2,\"type\":\"EProp\",\"pType\":\"name\"}]}");
        assertInvalid("unknown element type", HEAD + "{\"eNum\":2,\"type\":\"Bogus\"}]}");
        assertInvalid("relation without direction", HEAD + "{\"eNum\":2,\"type\":\"Rel\",\"rType\":\"own\"}]}");
        assertInvalid("bad direction", HEAD + "{\"eNum\":2,\"type\":\"Rel\",\"rType\":\"own\",\"dir\":\"X\"}]}");
        assertInvalid("concrete entity without id", HEAD + "{\"eNum\":2,\"type\":\"EConcrete\",\"eType\":\"Dragon\"}]}");
        assertInvalid("no Start", "{\"ont\":\"D\",\"elements\":[{\"eNum\":1,\"type\":\"ETyped\",\"eType\":\"P\"}]}");
        assertInvalid("two Starts", "{\"ont\":\"D\",\"elements\":[{\"eNum\":0,\"type\":\"Start\"},{\"eNum\":1,\"type\":\"Start\"}]}");
        assertInvalid("where-by without joinType (the engine fails to run it, KNOWN_ISSUES Q17)",
                withProp("{\"type\":\"WhereByConstraint\",\"op\":\"gt\",\"tagEntity\":\"A\",\"projectedField\":\"height\"}"));
        assertInvalid("untyped relation with no types (the engine drops it, KNOWN_ISSUES Q19)",
                HEAD + "{\"eNum\":2,\"type\":\"RelUntyped\",\"vTypes\":[],\"dir\":\"R\"}]}");
        assertInvalid("no ontology", "{\"elements\":[{\"eNum\":0,\"type\":\"Start\"}]}");
    }
}
