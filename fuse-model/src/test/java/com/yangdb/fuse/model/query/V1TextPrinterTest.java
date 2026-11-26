package com.yangdb.fuse.model.query;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class V1TextPrinterTest {
    private static final ObjectMapper mapper = new ObjectMapper();
    private static final String START = "{\"ont\":\"Dragons\",\"elements\":[{\"eNum\":0,\"type\":\"Start\",\"next\":1},";

    private static String print(String elements) throws IOException {
        return V1TextPrinter.print(mapper.readValue(START + elements + "]}", Query.class));
    }

    private static String con(String op, String expr) {
        return "{\"type\":\"Constraint\",\"op\":\"" + op + "\"" + (expr == null ? "" : ",\"expr\":" + expr) + "}";
    }

    @Test
    public void pathWithTypedEntitiesAndDirections() throws IOException {
        assertEquals("start (A:Person) -[:Own]-> (B:Dragon) <-[f:Fire]- (C:Dragon) -[:Know]- (:Person)", print(
                "{\"eNum\":1,\"type\":\"ETyped\",\"eTag\":\"A\",\"eType\":\"Person\",\"next\":2},"
                        + "{\"eNum\":2,\"type\":\"Rel\",\"rType\":\"Own\",\"dir\":\"R\",\"next\":3},"
                        + "{\"eNum\":3,\"type\":\"ETyped\",\"eTag\":\"B\",\"eType\":\"Dragon\",\"next\":4},"
                        + "{\"eNum\":4,\"type\":\"Rel\",\"eTag\":\"f\",\"rType\":\"Fire\",\"dir\":\"L\",\"next\":5},"
                        + "{\"eNum\":5,\"type\":\"ETyped\",\"eTag\":\"C\",\"eType\":\"Dragon\",\"next\":6},"
                        + "{\"eNum\":6,\"type\":\"Rel\",\"rType\":\"Know\",\"dir\":\"RL\",\"next\":7},"
                        + "{\"eNum\":7,\"type\":\"ETyped\",\"eType\":\"Person\"}"));
    }

    @Test
    public void concreteUntypedPatternAndUntypedRelation() throws IOException {
        assertEquals("start (A:Person #\"p1\") -[:Know*1..3]-> (B:Person) -[:MemberOf|Own !Know]-> (C:* !Horse)", print(
                "{\"eNum\":1,\"type\":\"EConcrete\",\"eTag\":\"A\",\"eType\":\"Person\",\"eID\":\"p1\",\"eName\":\"Brandon\",\"next\":2},"
                        + "{\"eNum\":2,\"type\":\"RelPattern\",\"rType\":\"Know\",\"dir\":\"R\",\"length\":{\"lower\":1,\"upper\":3},\"next\":3},"
                        + "{\"eNum\":3,\"type\":\"ETyped\",\"eTag\":\"B\",\"eType\":\"Person\",\"next\":4},"
                        + "{\"eNum\":4,\"type\":\"RelUntyped\",\"vTypes\":[\"Own\",\"MemberOf\"],\"nvTypes\":[\"Know\"],\"dir\":\"R\",\"next\":5},"
                        + "{\"eNum\":5,\"type\":\"EUntyped\",\"eTag\":\"C\",\"nvTypes\":[\"Horse\"]}"));
    }

    @Test
    public void quantifierPropertiesAndRelationFilters() throws IOException {
        assertEquals("start (A:Person) all{ {firstName = \"Brandon\"} | -[:Own {startDate in range [\"1900\", \"2000\")}]-> (B:Dragon) }",
                print("{\"eNum\":1,\"type\":\"ETyped\",\"eTag\":\"A\",\"eType\":\"Person\",\"next\":2},"
                        + "{\"eNum\":2,\"type\":\"Quant1\",\"qType\":\"all\",\"next\":[3,4]},"
                        + "{\"eNum\":3,\"type\":\"EProp\",\"pType\":\"firstName\",\"con\":" + con("eq", "\"Brandon\"") + "},"
                        + "{\"eNum\":4,\"type\":\"Rel\",\"rType\":\"Own\",\"dir\":\"R\",\"next\":5,\"b\":6},"
                        + "{\"eNum\":5,\"type\":\"ETyped\",\"eTag\":\"B\",\"eType\":\"Dragon\"},"
                        + "{\"eNum\":6,\"type\":\"RelProp\",\"pType\":\"startDate\",\"con\":{\"type\":\"Constraint\",\"op\":\"in range\",\"expr\":[\"1900\",\"2000\"],\"iType\":\"[)\"}}"));
    }

    @Test
    public void groupsOptionalProjectionAndWordOperators() throws IOException {
        assertEquals("start (A:Person) all{ {height > 180, gender, some{name contains \"Stark\", name empty}} | optional{ -[:Own]-> (B:Horse) } }",
                print("{\"eNum\":1,\"type\":\"ETyped\",\"eTag\":\"A\",\"eType\":\"Person\",\"next\":2},"
                        + "{\"eNum\":2,\"type\":\"Quant1\",\"qType\":\"all\",\"next\":[3,4]},"
                        + "{\"eNum\":3,\"type\":\"EPropGroup\",\"props\":["
                        + "{\"eNum\":31,\"type\":\"EProp\",\"pType\":\"height\",\"con\":" + con("gt", "180") + "},"
                        + "{\"eNum\":32,\"type\":\"EProp\",\"pType\":\"gender\",\"proj\":{\"type\":\"Identity\"}}],"
                        + "\"groups\":[{\"eNum\":33,\"type\":\"EPropGroup\",\"quantType\":\"some\",\"props\":["
                        + "{\"eNum\":34,\"type\":\"EProp\",\"pType\":\"name\",\"con\":" + con("contains", "\"Stark\"") + "},"
                        + "{\"eNum\":35,\"type\":\"EProp\",\"pType\":\"name\",\"con\":" + con("empty", null) + "}]}]},"
                        + "{\"eNum\":4,\"type\":\"OptionalComp\",\"next\":5},"
                        + "{\"eNum\":5,\"type\":\"Rel\",\"rType\":\"Own\",\"dir\":\"R\",\"next\":6},"
                        + "{\"eNum\":6,\"type\":\"ETyped\",\"eTag\":\"B\",\"eType\":\"Horse\"}"));
    }

    @Test
    public void whereByPrintsItsQuantifier() throws IOException {
        String q = "{\"eNum\":1,\"type\":\"ETyped\",\"eTag\":\"A\",\"eType\":\"Person\",\"next\":2},"
                + "{\"eNum\":2,\"type\":\"Rel\",\"rType\":\"Know\",\"dir\":\"R\",\"next\":3},"
                + "{\"eNum\":3,\"type\":\"ETyped\",\"eTag\":\"B\",\"eType\":\"Person\",\"next\":4},"
                + "{\"eNum\":4,\"type\":\"EProp\",\"pType\":\"height\",\"con\":{\"type\":\"WhereByConstraint\",\"op\":\"gt\","
                + "\"tagEntity\":\"A\",\"projectedField\":\"height\",\"joinType\":\"JOIN\"}}";
        assertEquals("start (A:Person) -[:Know]-> (B:Person) {height > any(A.height)}", print(q.replace("JOIN", "FOR_EACH")));
        assertEquals("start (A:Person) -[:Know]-> (B:Person) {height > all(A.height)}", print(q.replace("JOIN", "FULL")));
    }

    @Test
    public void malformedQueriesPrintWithoutThrowing() throws IOException {
        assertEquals("start (A:Person) -[:Own]-> <missing 9>", print(
                "{\"eNum\":1,\"type\":\"ETyped\",\"eTag\":\"A\",\"eType\":\"Person\",\"next\":2},"
                        + "{\"eNum\":2,\"type\":\"Rel\",\"rType\":\"Own\",\"dir\":\"R\",\"next\":9}"));
        assertEquals("start (A:Person) -[:Know]-> <cycle 1>", print(
                "{\"eNum\":1,\"type\":\"ETyped\",\"eTag\":\"A\",\"eType\":\"Person\",\"next\":2},"
                        + "{\"eNum\":2,\"type\":\"Rel\",\"rType\":\"Know\",\"dir\":\"R\",\"next\":1}"));
        assertEquals("start (A:Person) -[:Own some{startDate < \"1950\", startDate > \"1990\"}]-> (B:Dragon)", print(
                "{\"eNum\":1,\"type\":\"ETyped\",\"eTag\":\"A\",\"eType\":\"Person\",\"next\":2},"
                        + "{\"eNum\":2,\"type\":\"Rel\",\"rType\":\"Own\",\"dir\":\"R\",\"next\":3,\"b\":4},"
                        + "{\"eNum\":3,\"type\":\"ETyped\",\"eTag\":\"B\",\"eType\":\"Dragon\"},"
                        + "{\"eNum\":4,\"type\":\"HQuant\",\"qType\":\"some\",\"b\":[5,6]},"
                        + "{\"eNum\":5,\"type\":\"RelProp\",\"pType\":\"startDate\",\"con\":" + con("lt", "\"1950\"") + "},"
                        + "{\"eNum\":6,\"type\":\"RelProp\",\"pType\":\"startDate\",\"con\":" + con("gt", "\"1990\"") + "}"));
        assertEquals("start <CountComp>", print("{\"eNum\":1,\"type\":\"CountComp\"}"));
    }

    /** every "Text form:" line in the spec docs is what the printer writes for the JSON example above it */
    @Test
    public void docTextFormsMatchThePrinter() throws IOException {
        Pattern example = Pattern.compile("```json\\s*\\n(\\{.*?)\\n```\\s*\\nText form: `([^`]*)`", Pattern.DOTALL);
        int checked = 0;
        for (String doc : new String[]{"docs/spec/v1/README.md", "docs/spec/v1/rules.md"}) {
            Path path = Paths.get("..").resolve(doc);
            Matcher m = example.matcher(new String(Files.readAllBytes(path), StandardCharsets.UTF_8));
            while (m.find()) {
                assertEquals(doc + " example #" + (++checked), m.group(2), V1TextPrinter.print(mapper.readValue(m.group(1), Query.class)));
            }
        }
        assertTrue("expected text forms in the spec docs, found " + checked, checked >= 15);
    }
}
