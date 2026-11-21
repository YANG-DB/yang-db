package com.yangdb.fuse.unipop.controller.search;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.Assert;

import java.io.IOException;
import java.util.*;

/**
 * Structural assertions for OpenSearch query / aggregation DSL produced by the unipop controllers.
 *
 * <p>Both sides are parsed into Jackson trees and compared semantically, so key order and formatting don't
 * matter. The expected document may be written with single quotes and unquoted field names.
 *
 * <p>Rules:
 * <ul>
 *   <li>Fields at their OpenSearch default ({@code "adjust_pure_negative": true}, {@code "boost": 1.0}) are
 *       dropped from both sides, so expectations only spell out non-default values.</li>
 *   <li>Every field in the expected document must be present and equal in the actual one. Other fields in the
 *       actual document are ignored (a new default field doesn't break the test), except for the
 *       {@link #STRICT_FIELDS}: an unexpected {@code boost}, bool clause list ({@code must}, {@code filter},
 *       {@code should}, {@code must_not}) or sub-{@code aggregations} block fails.</li>
 *   <li>Arrays must have the same size. Bool clause lists are compared as multisets (clause order has no
 *       meaning in a bool query); all other arrays are compared in order.</li>
 *   <li>Numbers compare by value ({@code 100} equals {@code 100.0}).</li>
 * </ul>
 */
public final class QueryDslAssert {
    private static final ObjectMapper MAPPER = new ObjectMapper()
            .configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true)
            .configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);

    /** Arrays whose element order carries no meaning. */
    private static final Set<String> UNORDERED_ARRAYS =
            new HashSet<>(Arrays.asList("must", "filter", "should", "must_not"));

    /** Fields that must not appear in the actual document unless the expectation names them. */
    private static final Set<String> STRICT_FIELDS =
            new HashSet<>(Arrays.asList("boost", "must", "filter", "should", "must_not", "minimum_should_match",
                    "aggregations"));

    private QueryDslAssert() {}

    /** Asserts that {@code actualJson} matches {@code expectedJson} under the rules above. */
    public static void assertDsl(String expectedJson, String actualJson) {
        JsonNode expected = normalize(parse(expectedJson));
        JsonNode actual = normalize(parse(actualJson));
        String mismatch = mismatch(expected, actual, "$", null);
        if (mismatch != null) {
            Assert.fail("DSL mismatch at " + mismatch + "\nexpected: " + expected + "\nactual:   " + actual);
        }
    }

    /** Parses a (possibly lenient) JSON string. */
    public static JsonNode parse(String json) {
        try {
            return MAPPER.readTree(json);
        } catch (IOException e) {
            throw new AssertionError("not valid JSON: " + json, e);
        }
    }

    private static JsonNode normalize(JsonNode node) {
        if (node.isObject()) {
            ObjectNode copy = MAPPER.createObjectNode();
            node.fields().forEachRemaining(e -> {
                if (isDefault(e.getKey(), e.getValue())) {
                    return;
                }
                copy.set(e.getKey(), normalize(e.getValue()));
            });
            return copy;
        }
        if (node.isArray()) {
            ArrayNode copy = MAPPER.createArrayNode();
            node.forEach(child -> copy.add(normalize(child)));
            return copy;
        }
        return node;
    }

    private static boolean isDefault(String key, JsonNode value) {
        return ("adjust_pure_negative".equals(key) && value.isBoolean() && value.booleanValue())
                || ("boost".equals(key) && value.isNumber() && value.doubleValue() == 1.0);
    }

    /** @return null when {@code actual} matches {@code expected}, otherwise a description of the first mismatch */
    private static String mismatch(JsonNode expected, JsonNode actual, String path, String fieldName) {
        if (expected.isObject()) {
            if (!actual.isObject()) {
                return path + ": expected an object, got " + actual;
            }
            Iterator<Map.Entry<String, JsonNode>> fields = expected.fields();
            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> e = fields.next();
                JsonNode actualValue = actual.get(e.getKey());
                if (actualValue == null) {
                    return path + "." + e.getKey() + ": missing";
                }
                String m = mismatch(e.getValue(), actualValue, path + "." + e.getKey(), e.getKey());
                if (m != null) {
                    return m;
                }
            }
            Iterator<String> actualNames = actual.fieldNames();
            while (actualNames.hasNext()) {
                String name = actualNames.next();
                if (STRICT_FIELDS.contains(name) && !expected.has(name)) {
                    return path + "." + name + ": unexpected " + actual.get(name);
                }
            }
            return null;
        }
        if (expected.isArray()) {
            if (!actual.isArray()) {
                return path + ": expected an array, got " + actual;
            }
            if (expected.size() != actual.size()) {
                return path + ": expected " + expected.size() + " elements, got " + actual.size() + ": " + actual;
            }
            if (UNORDERED_ARRAYS.contains(fieldName) && expected.size() > 1) {
                return matchUnordered(expected, actual, 0, new boolean[actual.size()])
                        ? null : path + ": no one-to-one match of " + expected + " in " + actual;
            }
            for (int i = 0; i < expected.size(); i++) {
                String m = mismatch(expected.get(i), actual.get(i), path + "[" + i + "]", null);
                if (m != null) {
                    return m;
                }
            }
            return null;
        }
        if (expected.isNumber()) {
            return actual.isNumber() && expected.decimalValue().compareTo(actual.decimalValue()) == 0
                    ? null : path + ": expected " + expected + ", got " + actual;
        }
        return expected.equals(actual) ? null : path + ": expected " + expected + ", got " + actual;
    }

    private static boolean matchUnordered(JsonNode expected, JsonNode actual, int index, boolean[] used) {
        if (index == expected.size()) {
            return true;
        }
        for (int i = 0; i < actual.size(); i++) {
            if (!used[i] && mismatch(expected.get(index), actual.get(i), "$", null) == null) {
                used[i] = true;
                if (matchUnordered(expected, actual, index + 1, used)) {
                    return true;
                }
                used[i] = false;
            }
        }
        return false;
    }
}
