package com.yangdb.fuse.model;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Assert;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

/**
 * Structural JSON assertion for serialized model objects.
 *
 * <p>Both sides are parsed into Jackson trees, so key order and formatting don't matter. The expected document
 * may use single quotes and unquoted field names. Matching rules:
 * <ul>
 *   <li>objects: every expected field must be present and match; extra fields in the actual document are
 *       ignored, so a new serialized default doesn't break a test;</li>
 *   <li>arrays: same size, matched in order (element order is meaningful, e.g. query elements and next lists);</li>
 *   <li>numbers compare by value; other scalars must be equal, including their JSON type.</li>
 * </ul>
 */
public final class JsonTreeAssert {
    private static final ObjectMapper MAPPER = new ObjectMapper()
            .configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true)
            .configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);

    private JsonTreeAssert() {}

    public static void assertJsonMatches(String expectedJson, String actualJson) {
        JsonNode expected = parse(expectedJson);
        JsonNode actual = parse(actualJson);
        String mismatch = mismatch(expected, actual, "$");
        if (mismatch != null) {
            Assert.fail("JSON mismatch at " + mismatch + "\nexpected: " + expected + "\nactual:   " + actual);
        }
    }

    public static JsonNode parse(String json) {
        try {
            return MAPPER.readTree(json);
        } catch (IOException e) {
            throw new AssertionError("not valid JSON: " + json, e);
        }
    }

    /** @return null when {@code actual} matches {@code expected}, otherwise a description of the first mismatch */
    private static String mismatch(JsonNode expected, JsonNode actual, String path) {
        if (expected.isObject()) {
            if (!actual.isObject()) {
                return path + ": expected an object, got " + actual;
            }
            Iterator<Map.Entry<String, JsonNode>> fields = expected.fields();
            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> e = fields.next();
                JsonNode actualValue = actual.get(e.getKey());
                if (actualValue == null) {
                    return path + "." + e.getKey() + ": missing in " + actual;
                }
                String m = mismatch(e.getValue(), actualValue, path + "." + e.getKey());
                if (m != null) {
                    return m;
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
            for (int i = 0; i < expected.size(); i++) {
                String m = mismatch(expected.get(i), actual.get(i), path + "[" + i + "]");
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
}
