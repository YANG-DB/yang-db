
package com.yangdb.fuse.executor.opensearch;

import com.fasterxml.jackson.databind.JsonNode;
import com.yangdb.fuse.executor.BaseModuleInjectionTest;
import com.yangdb.fuse.executor.ontology.schema.RawSchema;
import com.yangdb.fuse.model.ontology.BaseElement;
import com.yangdb.fuse.model.ontology.EntityType;
import com.yangdb.fuse.model.ontology.Ontology;
import com.yangdb.fuse.model.ontology.RelationshipType;
import com.yangdb.fuse.model.schema.Entity;
import com.yangdb.fuse.model.schema.IndexProvider;
import com.yangdb.fuse.model.schema.MappingIndexType;
import com.yangdb.fuse.model.schema.Relation;
import com.yangdb.test.BaseITMarker;
import javaslang.Tuple2;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.opensearch.action.admin.indices.delete.DeleteIndexRequest;
import org.opensearch.action.admin.indices.exists.indices.IndicesExistsRequest;
import org.opensearch.action.admin.indices.mapping.get.GetMappingsRequest;
import org.opensearch.action.admin.indices.settings.get.GetSettingsRequest;
import org.opensearch.action.admin.indices.settings.get.GetSettingsResponse;
import org.opensearch.action.admin.indices.template.delete.DeleteIndexTemplateRequest;
import org.opensearch.action.admin.indices.template.get.GetIndexTemplatesRequest;
import org.opensearch.cluster.metadata.IndexTemplateMetadata;
import org.opensearch.common.regex.Regex;
import org.opensearch.common.settings.Settings;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static com.yangdb.fuse.executor.TestSuiteIndexProviderSuite.*;

/**
 * Structural checks of the index templates and indices that {@link ElasticIndexProviderMappingFactory} creates on the
 * embedded cluster.
 * <p>
 * Every expectation is derived from the test ontology ({@code schema/Dragons.json}, the shipped one) and from the
 * index-provider config of each case ({@code schema/DragonsIndexProvider{Nested,Embedded}.conf},
 * {@code schema/DragonsSingleIndexProvider.conf}). The only hand-written knowledge is the ontology-primitive to
 * OpenSearch field type table in {@link #esType}.
 * <p>
 * Shape facts (OpenSearch 2.x, no mapping types):
 * <ul>
 *     <li>the factory sends untyped template mappings ({@code {"properties":...}}); a legacy index template stores
 *     them under OpenSearch's single internal mapping name {@code _doc} ({@code {"_doc":{"properties":...}}}), never
 *     under a YANG-DB type or label name (up to 1.3 it was {@code {"Person":{"properties":...}}});</li>
 *     <li>concrete indices created from those templates have no type wrapper ({@code {"mappings":{"properties":...}}}).</li>
 * </ul>
 * The per-type templates this class creates match bare type names ({@code dragon}, {@code horse}, {@code own}, ...)
 * and would merge into indices created later in the suite, so every test starts from, and leaves behind, a cluster
 * without them or the indices they create.
 */
public class OpensearchIndexProviderMappingFactoryIT extends BaseModuleInjectionTest implements BaseITMarker {

    private static final String PROJECTION = "projection";
    /**
     * the one mapping name OpenSearch 2.x uses internally (MapperService.SINGLE_MAPPING_NAME)
     */
    private static final String SINGLE_MAPPING_NAME = "_doc";
    private static final String DATE_FORMAT = "epoch_millis||strict_date_optional_time||yyyy-MM-dd HH:mm:ss.SSS";

    //region cleanup

    @Before
    public void cleanBefore() {
        cleanup();
    }

    @After
    public void cleanAfter() {
        cleanup();
    }

    private static void cleanup() {
        Set<String> templates = new HashSet<>();
        Set<String> indices = new HashSet<>();
        for (IndexProvider provider : Arrays.asList(nestedProvider, embeddedProvider, singleIndexProvider)) {
            templates.addAll(expectedTemplates(provider).keySet());
        }
        for (RawSchema schema : Arrays.asList(nestedSchema, embeddedSchema, singleIndexSchema)) {
            schema.indices().forEach(indices::add);
        }
        templates.stream()
                .filter(name -> !templates(name).isEmpty())
                .forEach(name -> client.admin().indices().deleteTemplate(new DeleteIndexTemplateRequest(name)).actionGet());
        indices.stream()
                .filter(index -> client.admin().indices().exists(new IndicesExistsRequest(index)).actionGet().isExists())
                .forEach(index -> client.admin().indices().delete(new DeleteIndexRequest(index)).actionGet());
    }
    //endregion

    //region tests

    @Test
    public void testGenerateNestedMapping() throws Exception {
        assertGeneratedTemplates(nestedSchema, nestedProvider);
        //the nested provider maps Person.Profession as a "child" -> a nested-type field
        JsonNode people = templateProperties(templates("people").get(0));
        Assert.assertEquals("nested", people.at("/Profession/type").asText());
    }

    @Test
    public void testGenerateEmbeddedMapping() throws Exception {
        assertGeneratedTemplates(embeddedSchema, embeddedProvider);
        //the embedded provider maps Person.Profession as "embedded" -> a plain object field
        JsonNode profession = templateProperties(templates("people").get(0)).get("Profession");
        Assert.assertNotNull(profession);
        Assert.assertFalse("embedded Profession must not be a nested field", profession.has("type"));
        Assert.assertTrue(profession.path("properties").size() > 0);
    }

    @Test
    public void testGenerateSingleMapping() throws Exception {
        assertGeneratedTemplates(singleIndexSchema, singleIndexProvider);
    }

    @Test
    public void createNestedIndicesTest() throws Exception {
        assertCreatedIndices(nestedSchema, nestedProvider);
        Assert.assertEquals("nested", indexProperties("people").at("/Profession/type").asText());
    }

    @Test
    public void createEmbeddedIndicesTest() throws Exception {
        assertCreatedIndices(embeddedSchema, embeddedProvider);
        JsonNode profession = indexProperties("people").get("Profession");
        Assert.assertNotNull(profession);
        Assert.assertFalse("embedded Profession must not be a nested field", profession.has("type"));
    }

    @Test
    public void createSingleIndicesTest() throws Exception {
        assertCreatedIndices(singleIndexSchema, singleIndexProvider);
    }

    /**
     * KNOWN_ISSUES M1: in a unified (single-index) provider the entities create the shared mapping first, so the
     * relations are merged into it; their relation-only fields ({@code direction}, {@code entityA}, {@code entityB}
     * with the redundant side properties of every relation) must be mapped too, not left to dynamic mapping.
     */
    @Test
    public void testGenerateSingleMappingMapsRelationSides() throws Exception {
        new ElasticIndexProviderMappingFactory(client, singleIndexSchema, ontology, singleIndexProvider).generateMappings();
        JsonNode properties = templateProperties(templates("ontology").get(0));
        Assert.assertEquals("keyword", properties.at("/direction/type").asText());
        Assert.assertEquals("keyword", properties.at("/entityA/properties/id/type").asText());
        Assert.assertEquals("keyword", properties.at("/entityB/properties/id/type").asText());
    }

    /**
     * KNOWN_ISSUES M2: the single-index "ontology" template only matches the unified index, not every type's bare name
     * (person, dragon, horse, own, fire, ...): otherwise any index with one of those names created on the same cluster
     * gets the whole ontology mapping merged in.
     */
    @Ignore("Product bug: UNIFIED template patterns match unrelated per-type indices - see javadoc")
    @Test
    public void testGenerateSingleMappingPatternsOnlyMatchUnifiedIndex() {
        new ElasticIndexProviderMappingFactory(client, singleIndexSchema, ontology, singleIndexProvider).generateMappings();
        IndexTemplateMetadata template = templates("ontology").get(0);
        for (String other : Arrays.asList("dragon", "horse", "own", "person", "fire")) {
            Assert.assertFalse("ontology template must not match index " + other,
                    Regex.simpleMatch(template.patterns(), other));
        }
    }
    //endregion

    //region scenario assertions

    /**
     * generate the templates and check each one against the expectation derived from ontology + provider
     */
    private void assertGeneratedTemplates(RawSchema schema, IndexProvider provider) throws Exception {
        Map<String, Expected> expected = expectedTemplates(provider);
        ElasticIndexProviderMappingFactory factory = new ElasticIndexProviderMappingFactory(client, schema, ontology, provider);
        List<Tuple2<String, Boolean>> generated = factory.generateMappings();

        Assert.assertEquals(expected.keySet(), generated.stream().map(t -> t._1).collect(Collectors.toSet()));
        Assert.assertEquals("one request per template", expected.size(), generated.size());
        generated.forEach(t -> Assert.assertTrue("template " + t._1 + " acknowledged", t._2));

        for (Map.Entry<String, Expected> entry : expected.entrySet()) {
            String name = entry.getKey();
            Expected exp = entry.getValue();
            List<IndexTemplateMetadata> found = templates(name);
            Assert.assertEquals("template " + name, 1, found.size());
            IndexTemplateMetadata template = found.get(0);
            Assert.assertEquals(name, template.name());
            Assert.assertEquals("patterns of " + name, exp.patterns, new HashSet<>(template.patterns()));
            Assert.assertEquals("patterns of " + name + " are distinct", template.patterns().size(), new HashSet<>(template.patterns()).size());
            assertSettings(name, template.settings(), exp);
            assertProperties(name, templateProperties(template), exp.mapping.props);
        }

        //every concrete (partition) index of the schema is covered by exactly the one template it belongs to
        Map<String, ExpectedIndex> indices = expectedIndices(provider);
        Assert.assertEquals("schema indices", indices.keySet(), toSet(schema.indices()));
        List<IndexTemplateMetadata> all = expected.keySet().stream()
                .filter(n -> !n.equals(PROJECTION))
                .map(n -> templates(n).get(0))
                .collect(Collectors.toList());
        for (String index : schema.indices()) {
            List<String> matching = all.stream()
                    .filter(t -> Regex.simpleMatch(t.patterns(), index))
                    .map(IndexTemplateMetadata::name)
                    .collect(Collectors.toList());
            Assert.assertEquals("templates matching index " + index, Collections.singletonList(indices.get(index).template), matching);
        }
    }

    /**
     * generate the templates, create the (partition) indices and check the mapping/settings each index ends up with
     */
    private void assertCreatedIndices(RawSchema schema, IndexProvider provider) throws Exception {
        ElasticIndexProviderMappingFactory factory = new ElasticIndexProviderMappingFactory(client, schema, ontology, provider);
        Map<String, Expected> templates = expectedTemplates(provider);
        List<Tuple2<String, Boolean>> generated = factory.generateMappings();
        Assert.assertEquals(templates.keySet(), generated.stream().map(t -> t._1).collect(Collectors.toSet()));

        Map<String, ExpectedIndex> expected = expectedIndices(provider);
        Assert.assertEquals("schema indices", expected.keySet(), toSet(schema.indices()));

        List<Tuple2<Boolean, String>> created = factory.createIndices();
        Assert.assertEquals(expected.keySet(), created.stream().map(t -> t._2).collect(Collectors.toSet()));
        Assert.assertEquals("each index created once", expected.size(), created.size());
        created.forEach(t -> Assert.assertTrue("index " + t._2 + " newly created", t._1));

        for (Map.Entry<String, ExpectedIndex> entry : expected.entrySet()) {
            String index = entry.getKey();
            Expected exp = templates.get(entry.getValue().template);
            assertProperties(index, indexProperties(index), exp.mapping.props);

            GetSettingsResponse settings = client.admin().indices().getSettings(new GetSettingsRequest().indices(index)).actionGet();
            Assert.assertEquals(index + " shards", "3", settings.getSetting(index, "index.number_of_shards"));
            Assert.assertEquals(index + " replicas", "1", settings.getSetting(index, "index.number_of_replicas"));
            Assert.assertEquals(index + " sort field", exp.sortField, settings.getSetting(index, "index.sort.field"));
        }
    }

    private static void assertSettings(String name, Settings settings, Expected exp) {
        Assert.assertEquals(name + " shards", "3", settings.get("index.number_of_shards"));
        Assert.assertEquals(name + " replicas", "1", settings.get("index.number_of_replicas"));
        Assert.assertEquals(name + " sort field", exp.sortField, settings.get("index.sort.field"));
        Assert.assertEquals(name + " sort order", exp.sortField == null ? null : "asc", settings.get("index.sort.order"));
    }

    /**
     * recursive structural comparison: exact property-name set at every level, leaf field types, object vs nested
     */
    private static void assertProperties(String path, JsonNode actual, Map<String, Object> expected) {
        Assert.assertNotNull(path + " has properties", actual);
        Assert.assertEquals("property names of " + path, new TreeSet<>(expected.keySet()), toSet(actual.fieldNames()));
        expected.forEach((name, exp) -> {
            JsonNode field = actual.get(name);
            String fieldPath = path + "." + name;
            if (exp instanceof String) {
                Assert.assertEquals("type of " + fieldPath, exp, field.path("type").asText(null));
                Assert.assertFalse(fieldPath + " is a leaf", field.has("properties"));
                if (exp.equals("text")) {
                    Assert.assertEquals(fieldPath + ".keyword", "keyword", field.at("/fields/keyword/type").asText(null));
                } else if (exp.equals("date")) {
                    Assert.assertEquals(fieldPath + " format", DATE_FORMAT, field.path("format").asText(null));
                }
            } else {
                Obj obj = (Obj) exp;
                Assert.assertEquals("type of " + fieldPath, obj.type, field.has("type") ? field.get("type").asText() : null);
                assertProperties(fieldPath, field.get("properties"), obj.props);
            }
        });
    }
    //endregion

    //region cluster access

    private static List<IndexTemplateMetadata> templates(String name) {
        return client.admin().indices().getTemplates(new GetIndexTemplatesRequest(name)).actionGet().getIndexTemplates();
    }

    /**
     * templates are untyped: {properties: ...}, stored by OpenSearch under its single internal mapping name
     * {_doc: {properties: ...}} - never under a type/label name of the ontology
     */
    private static JsonNode templateProperties(IndexTemplateMetadata template) throws Exception {
        Assert.assertNotNull("template " + template.name() + " has a mapping", template.mappings());
        JsonNode root = mapper.readTree(template.mappings().string());
        if (toSet(root.fieldNames()).equals(Collections.singleton(SINGLE_MAPPING_NAME))) {
            root = root.get(SINGLE_MAPPING_NAME);
        }
        Assert.assertEquals("template " + template.name() + " mapping has no type wrapper", Collections.singleton("properties"), toSet(root.fieldNames()));
        return root.get("properties");
    }

    /**
     * concrete indices are typeless: {index: {mappings: {properties: ...}}}
     */
    private static JsonNode indexProperties(String index) throws Exception {
        GetMappingsRequest request = new GetMappingsRequest();
        request.indices(index);
        JsonNode root = mapper.readTree(client.admin().indices().getMappings(request).actionGet().toString());
        Assert.assertEquals(Collections.singleton(index), toSet(root.fieldNames()));
        JsonNode mappings = root.get(index).get("mappings");
        Assert.assertEquals("index " + index + " mapping has no type wrapper", Collections.singleton("properties"), toSet(mappings.fieldNames()));
        return mappings.get("properties");
    }
    //endregion

    //region expectations derived from ontology + index provider

    /**
     * expected object field: type null = plain object, "nested" = nested; props values are a field type (String) or Obj
     */
    private static final class Obj {
        final String type;
        final Map<String, Object> props = new TreeMap<>();

        Obj(String type) {
            this.type = type;
        }
    }

    private static final class Expected {
        final Set<String> patterns = new HashSet<>();
        final String sortField;
        final Obj mapping = new Obj(null);

        Expected(String sortField) {
            this.sortField = sortField;
        }
    }

    private static final class ExpectedIndex {
        final String template;

        ExpectedIndex(String template) {
            this.template = template;
        }
    }

    private static MappingIndexType partition(String partition) {
        return MappingIndexType.valueOf(partition.toUpperCase());
    }

    /**
     * template name -> expectation. Static/time elements without nested documents are sorted by their id field;
     * unified and projection templates are not sorted (they hold several types).
     */
    private static Map<String, Expected> expectedTemplates(IndexProvider provider) {
        Ontology.Accessor acc = new Ontology.Accessor(ontology);
        Map<String, Expected> result = new TreeMap<>();

        for (Entity entity : provider.getEntities()) {
            EntityType type = acc.entity$(entity.getType());
            switch (partition(entity.getPartition())) {
                case STATIC:
                    for (String value : entity.getProps().getValues()) {
                        Expected exp = new Expected(entity.getNested().isEmpty() ? BaseElement.idFieldName(type.getIdField()) : null);
                        exp.patterns.addAll(Arrays.asList(type.getName().toLowerCase(), type.getName(), value + "*"));
                        exp.mapping.props.putAll(entityProps(acc, entity));
                        result.put(value.toLowerCase(), exp);
                    }
                    break;
                case UNIFIED:
                    for (String value : entity.getProps().getValues()) {
                        Expected exp = result.computeIfAbsent(value, v -> new Expected(null));
                        exp.patterns.addAll(Arrays.asList(type.getName().toLowerCase(), type.getName(), value + "*"));
                        exp.mapping.props.putAll(entityProps(acc, entity));
                    }
                    break;
                case NESTED:
                    //mapped inside its parent element
                    break;
                default:
                    Assert.fail("test does not model entity partition " + entity.getPartition());
            }
        }

        for (Relation relation : provider.getRelations()) {
            RelationshipType type = acc.relation$(relation.getType());
            switch (partition(relation.getPartition())) {
                case STATIC:
                    for (String value : relation.getProps().getValues()) {
                        Expected exp = new Expected(relation.getNested().isEmpty() ? BaseElement.idFieldName(type.getIdField()) : null);
                        exp.patterns.addAll(Arrays.asList(type.getName().toLowerCase(), type.getName(), value + "*"));
                        exp.mapping.props.putAll(relationProps(acc, relation));
                        result.put(relation.getType().toLowerCase(), exp);
                    }
                    break;
                case TIME: {
                    Expected exp = new Expected(relation.getNested().isEmpty() ? BaseElement.idFieldName(type.getIdField()) : null);
                    exp.patterns.addAll(Arrays.asList(type.getName().toLowerCase(), type.getName(),
                            String.format(relation.getProps().getIndexFormat(), "*")));
                    exp.mapping.props.putAll(relationProps(acc, relation));
                    result.put(relation.getType().toLowerCase(), exp);
                    break;
                }
                case UNIFIED:
                    for (String value : relation.getProps().getValues()) {
                        Expected exp = result.computeIfAbsent(value, v -> new Expected(null));
                        exp.patterns.addAll(Arrays.asList(type.getName().toLowerCase(), type.getName(), value + "*"));
                        //the relation documents share the sides: their redundant fields accumulate
                        merge(exp.mapping.props, relationProps(acc, relation));
                    }
                    break;
                case NESTED:
                    //mapped inside its parent element
                    break;
                default:
                    Assert.fail("test does not model relation partition " + relation.getPartition());
            }
        }

        result.put(PROJECTION, expectedProjection(acc, provider));
        return result;
    }

    /**
     * concrete index name -> name of the template it is created from
     */
    private static Map<String, ExpectedIndex> expectedIndices(IndexProvider provider) {
        Map<String, ExpectedIndex> result = new TreeMap<>();
        for (Entity entity : provider.getEntities()) {
            switch (partition(entity.getPartition())) {
                case STATIC:
                case UNIFIED:
                    entity.getProps().getValues().forEach(v -> result.put(v, new ExpectedIndex(v.toLowerCase())));
                    break;
                default:
                    break;
            }
        }
        for (Relation relation : provider.getRelations()) {
            switch (partition(relation.getPartition())) {
                case STATIC:
                    relation.getProps().getValues().forEach(v -> result.put(v, new ExpectedIndex(relation.getType().toLowerCase())));
                    break;
                case UNIFIED:
                    relation.getProps().getValues().forEach(v -> result.put(v, new ExpectedIndex(v)));
                    break;
                case TIME:
                    relation.getProps().getValues().forEach(v -> result.put(
                            String.format(relation.getProps().getIndexFormat(), v), new ExpectedIndex(relation.getType().toLowerCase())));
                    break;
                default:
                    break;
            }
        }
        return result;
    }

    /**
     * entity document: its ontology fields plus one object per nested element of the provider config
     * ("child" = nested type, "embedded" = plain object)
     */
    private static Map<String, Object> entityProps(Ontology.Accessor acc, Entity entity) {
        Map<String, Object> props = fields(acc, acc.entity$(entity.getType()));
        for (Entity nested : entity.getNested()) {
            Obj obj = new Obj(ElasticIndexProviderMappingFactory.CHILD.equals(nested.getMapping()) ? "nested" : null);
            obj.props.putAll(fields(acc, acc.entity$(nested.getType())));
            props.put(nested.getType(), obj);
        }
        return props;
    }

    /**
     * relation document: its ontology fields, direction, and both sides (id, type and the configured redundant fields)
     */
    private static Map<String, Object> relationProps(Ontology.Accessor acc, Relation relation) {
        Assert.assertTrue("test does not model nested relations", relation.getNested().isEmpty());
        Map<String, Object> props = fields(acc, acc.relation$(relation.getType()));
        props.put("direction", "keyword");
        for (String side : Arrays.asList("entityA", "entityB")) {
            Obj obj = new Obj(null);
            obj.props.put("id", "keyword");
            obj.props.put("type", "keyword");
            relation.getRedundant(side).forEach(r -> obj.props.put(r.getName(), esType(acc, acc.property$(r.getName()).getType())));
            props.put(side, obj);
        }
        return props;
    }

    /**
     * merge an element's properties into a shared (unified) mapping: objects are merged field by field
     */
    private static void merge(Map<String, Object> into, Map<String, Object> props) {
        props.forEach((name, value) -> {
            Object existing = into.get(name);
            if (existing instanceof Obj && value instanceof Obj) {
                merge(((Obj) existing).props, ((Obj) value).props);
            } else {
                into.put(name, value);
            }
        });
    }

    /**
     * projection: query metadata + one nested object per entity (nested ones raised to the root), each holding one
     * nested object per relation the entity is side A of; every element carries a "tag", relations also the
     * flattened "entityB.id"/"entityB.type"
     */
    private static Expected expectedProjection(Ontology.Accessor acc, IndexProvider provider) {
        Expected exp = new Expected(null);
        exp.patterns.add(PROJECTION + "*");
        exp.mapping.props.put("queryId", "keyword");
        exp.mapping.props.put("cursorId", "keyword");
        exp.mapping.props.put("timestamp", "date");
        for (Entity entity : provider.getEntities()) {
            Obj e = new Obj("nested");
            e.props.putAll(fields(acc, acc.entity$(entity.getType())));
            e.props.put("tag", "keyword");
            for (RelationshipType rel : acc.relationBySideA(entity.getType())) {
                Obj r = new Obj("nested");
                r.props.putAll(fields(acc, rel));
                r.props.put("tag", "keyword");
                r.props.put("entityB.id", "keyword");
                r.props.put("entityB.type", "keyword");
                e.props.put(rel.getName(), r);
            }
            exp.mapping.props.put(entity.getType(), e);
        }
        return exp;
    }

    /**
     * ontology metadata + properties of an element, minus those with no field type (entity-typed properties)
     */
    private static Map<String, Object> fields(Ontology.Accessor acc, BaseElement element) {
        Map<String, Object> props = new TreeMap<>();
        for (String name : concat(element.getMetadata(), element.getProperties())) {
            String type = esType(acc, acc.property$(name).getType());
            if (type != null) props.put(name, type);
        }
        return props;
    }

    /**
     * ontology property type -> OpenSearch field type; null = not mapped as a field
     */
    private static String esType(Ontology.Accessor acc, String ontologyType) {
        switch (ontologyType.toLowerCase()) {
            case "string":
                return "keyword";
            case "text":
                return "text";
            case "date":
                return "date";
            case "int":
                return "integer";
            case "long":
                return "long";
            case "float":
                return "float";
            case "double":
                return "double";
            case "geo":
                return "geo_point";
        }
        if (acc.entity(ontologyType).isPresent() || acc.relation(ontologyType).isPresent()) {
            return null;
        }
        if (ontology.getEnumeratedTypes() != null && ontology.getEnumeratedTypes().stream().anyMatch(e -> e.geteType().equals(ontologyType))) {
            //enums are stored as their integer value
            return "integer";
        }
        return "text";
    }

    private static List<String> concat(List<String> a, List<String> b) {
        List<String> result = new ArrayList<>();
        if (a != null) result.addAll(a);
        if (b != null) result.addAll(b);
        return result;
    }

    private static Set<String> toSet(Iterable<String> iterable) {
        return StreamSupport.stream(iterable.spliterator(), false).collect(Collectors.toCollection(TreeSet::new));
    }

    private static Set<String> toSet(Iterator<String> iterator) {
        Set<String> result = new TreeSet<>();
        iterator.forEachRemaining(result::add);
        return result;
    }
    //endregion
}
