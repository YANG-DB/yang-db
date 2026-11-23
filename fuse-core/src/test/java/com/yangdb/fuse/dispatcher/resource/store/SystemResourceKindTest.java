package com.yangdb.fuse.dispatcher.resource.store;

import org.junit.Assert;
import org.junit.Test;
import org.opensearch.index.query.BoolQueryBuilder;
import org.opensearch.index.query.ExistsQueryBuilder;
import org.opensearch.index.query.TermQueryBuilder;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class SystemResourceKindTest {

    @Test
    public void matchesOwnKind() {
        Assert.assertTrue(SystemResourceKind.matches(source(SystemResourceKind.QUERY), SystemResourceKind.QUERY));
        Assert.assertTrue(SystemResourceKind.matches(source(SystemResourceKind.NODE), SystemResourceKind.NODE));
    }

    @Test
    public void rejectsOtherKind() {
        Assert.assertFalse(SystemResourceKind.matches(source(SystemResourceKind.NODE), SystemResourceKind.QUERY));
        Assert.assertFalse(SystemResourceKind.matches(source(SystemResourceKind.QUERY), SystemResourceKind.NODE));
        Assert.assertFalse(SystemResourceKind.matches(null, SystemResourceKind.QUERY));
    }

    @Test
    public void legacyDocumentWithoutKindMatchesEveryKind() {
        Map<String, Object> legacy = Collections.singletonMap("name", "q1");
        Assert.assertTrue(SystemResourceKind.matches(legacy, SystemResourceKind.QUERY));
        Assert.assertTrue(SystemResourceKind.matches(legacy, SystemResourceKind.NODE));
    }

    @Test
    public void filterKeepsKindOrMissingKind() {
        BoolQueryBuilder filter = (BoolQueryBuilder) SystemResourceKind.filter(SystemResourceKind.QUERY);
        Assert.assertEquals("1", filter.minimumShouldMatch());
        Assert.assertEquals(2, filter.should().size());
        TermQueryBuilder term = (TermQueryBuilder) filter.should().get(0);
        Assert.assertEquals(SystemResourceKind.FIELD, term.fieldName());
        Assert.assertEquals(SystemResourceKind.QUERY, term.value());
        BoolQueryBuilder missing = (BoolQueryBuilder) filter.should().get(1);
        Assert.assertEquals(SystemResourceKind.FIELD, ((ExistsQueryBuilder) missing.mustNot().get(0)).fieldName());
    }

    @Test
    public void mappingIsUntypedKeyword() {
        Map<String, Object> mapping = SystemResourceKind.mapping();
        Assert.assertEquals(Collections.singleton("properties"), mapping.keySet());
        Map<?, ?> kind = (Map<?, ?>) ((Map<?, ?>) mapping.get("properties")).get(SystemResourceKind.FIELD);
        Assert.assertEquals("keyword", kind.get("type"));
    }

    private static Map<String, Object> source(String kind) {
        Map<String, Object> source = new HashMap<>();
        source.put(SystemResourceKind.FIELD, kind);
        source.put("name", "x");
        return source;
    }
}
