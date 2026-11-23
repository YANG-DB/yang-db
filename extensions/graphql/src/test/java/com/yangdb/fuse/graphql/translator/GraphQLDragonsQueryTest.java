package com.yangdb.fuse.graphql.translator;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yangdb.fuse.dispatcher.ontology.OntologyProvider;
import com.yangdb.fuse.model.execution.plan.descriptors.QueryDescriptor;
import com.yangdb.fuse.model.ontology.Ontology;
import com.yangdb.fuse.model.query.EBase;
import com.yangdb.fuse.model.query.Query;
import com.yangdb.fuse.model.query.QueryInfo;
import com.yangdb.fuse.model.query.Rel;
import com.yangdb.fuse.model.query.entity.EUntyped;
import com.yangdb.fuse.model.query.properties.EProp;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.InputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.yangdb.fuse.model.transport.CreateQueryRequestMetadata.TYPE_GRAPHQL;

/**
 * G1: GraphQL queries against an ontology registered from JSON (Dragons): the schema they run against is generated from
 * the ontology, with a root Query field per entity type ({@code dragon} for {@code Dragon}).
 */
public class GraphQLDragonsQueryTest {
    private static Ontology dragons;

    @BeforeClass
    public static void setUp() throws Exception {
        try (InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream("ontology/Dragons.json")) {
            dragons = new ObjectMapper().readValue(in, Ontology.class);
        }
    }

    public static OntologyProvider provider(Ontology ontology) {
        return new OntologyProvider() {
            @Override
            public Optional<Ontology> get(String id) {
                return Optional.of(ontology);
            }

            @Override
            public Collection<Ontology> getAll() {
                return Collections.singletonList(ontology);
            }

            @Override
            public Ontology add(Ontology ontology) {
                return ontology;
            }
        };
    }

    private static Query translate(GraphQLToOntologyTransformer schemaUtils, String q) {
        return new GraphQL2QueryTransformer(schemaUtils, provider(dragons))
                .transform(new QueryInfo<>(q, "q1", TYPE_GRAPHQL, "Dragons"));
    }

    @Test
    public void allDragonsWithNameAndColor() {
        Query query = translate(new GraphQLToOntologyTransformer(), "{ dragon { name color } }");
        String expected = "[└── Start, \n" +
                "    ──Typ[Dragon:1]──Q[2]:{3|4}, \n" +
                "                           └─?[3]:[name<IdentityProjection>], \n" +
                "                           └─?[4]:[color<IdentityProjection>]]";
        Assert.assertEquals(expected, QueryDescriptor.print(query));
    }

    @Test
    public void dragonsFilteredByColor() {
        Query query = translate(new GraphQLToOntologyTransformer(),
                "{ dragon(where: {operator: AND, constraints: [{operand: \"color\", operator: \"eq\", expression: \"BLACK\"}]}) { name color } }");
        String print = QueryDescriptor.print(query);
        Assert.assertTrue(print, print.contains("Typ[Dragon:1]"));
        Assert.assertTrue(print, print.contains("color<eq,BLACK>"));
    }

    /** G4: Own leads to dragons and horses, its field has an interface type both belong to */
    @Test
    public void aRelationshipToSeveralTypesHasAnInterfaceOfThemInTheSchema() {
        String sdl = OntologyToGraphQLSdl.sdl(dragons);
        Assert.assertTrue(sdl, sdl.contains("    Own: [Person_Own]"));
        Assert.assertTrue(sdl, sdl.contains("interface Person_Own {\n    name: String\n"));
        Assert.assertTrue(sdl, sdl.contains("type Dragon implements Person_Own {"));
        Assert.assertTrue(sdl, sdl.contains("type Horse implements Person_Own {"));
        // a field only one of them has is not in the interface
        String iface = sdl.substring(sdl.indexOf("interface Person_Own {"));
        iface = iface.substring(0, iface.indexOf('}'));
        Assert.assertFalse(sdl, iface.contains("color") || iface.contains("weight"));
        Assert.assertTrue(OntologyToGraphQLSdl.registry(dragons).getType("Person_Own").isPresent());
    }

    /** G4: without naming a type, the side B is any of the relationship's types */
    @Test
    public void personsAndEverythingTheyOwn() {
        Query query = translate(new GraphQLToOntologyTransformer(), "{ person { name Own { name } } }");
        String print = QueryDescriptor.print(query);
        Assert.assertTrue(print, print.contains("Typ[Person:1]"));
        Assert.assertTrue(print, print.contains("Rel(Own:"));
        EUntyped owned = single(query, EUntyped.class);
        Assert.assertEquals(print, new HashSet<>(Arrays.asList("Dragon", "Horse")), owned.getvTypes());
        Assert.assertEquals(print, 2, count(query, EProp.class, "name"));
    }

    /** G4: an inline fragment narrows the side B to its type, for each of the types */
    @Test
    public void personsAndTheDragonsTheyOwn() {
        Query query = translate(new GraphQLToOntologyTransformer(), "{ person { name Own { ... on Dragon { name color } } } }");
        String print = QueryDescriptor.print(query);
        Assert.assertTrue(print, print.contains("Rel(Own:"));
        Assert.assertTrue(print, print.contains("Typ[Dragon:"));
        Assert.assertEquals(print, 0, count(query, EUntyped.class, null));
        Assert.assertEquals(print, 1, count(query, EProp.class, "color"));
    }

    @Test
    public void personsAndTheHorsesTheyOwn() {
        Query query = translate(new GraphQLToOntologyTransformer(), "{ person { name Own { ... on Horse { name weight } } } }");
        String print = QueryDescriptor.print(query);
        Assert.assertTrue(print, print.contains("Rel(Own:"));
        Assert.assertTrue(print, print.contains("Typ[Horse:"));
        Assert.assertFalse(print, print.contains("Dragon"));
        Assert.assertEquals(print, 1, count(query, EProp.class, "weight"));
    }

    /** G4: common fields once, and each type's own fields, on the one side-B element */
    @Test
    public void commonAndPerTypeFieldsOfARelationshipToSeveralTypes() {
        Query query = translate(new GraphQLToOntologyTransformer(),
                "{ person { Own { name ... on Dragon { color } ... on Horse { weight } } } }");
        String print = QueryDescriptor.print(query);
        EUntyped owned = single(query, EUntyped.class);
        Assert.assertEquals(print, new HashSet<>(Arrays.asList("Dragon", "Horse")), owned.getvTypes());
        Assert.assertEquals(print, 1, count(query, EProp.class, "name"));
        Assert.assertEquals(print, 1, count(query, EProp.class, "color"));
        Assert.assertEquals(print, 1, count(query, EProp.class, "weight"));
        Assert.assertEquals(print, 1, count(query, Rel.class, null));
    }

    /** G4: a field the types share that is itself a relationship (Dragon and Horse both originate in a Kingdom) */
    @Test
    public void aRelationshipOfTheTypesOfARelationshipToSeveralTypes() {
        Query query = translate(new GraphQLToOntologyTransformer(),
                "{ person { Own { name OriginatedIn { name } } } }");
        String print = QueryDescriptor.print(query);
        Assert.assertEquals(print, 2, count(query, Rel.class, null));
        Assert.assertTrue(print, print.contains("Rel(OriginatedIn:"));
        Assert.assertTrue(print, print.contains("Typ[Kingdom:"));
        Assert.assertEquals(print, 2, count(query, EProp.class, "name"));
    }

    /** G4: types with no field in common get a union, and are queried with fragments */
    @Test
    public void aRelationshipToTypesWithNothingInCommonHasAUnion() throws Exception {
        Ontology ontology = new ObjectMapper().readValue("{\"ont\":\"Zoo\"," +
                "\"entityTypes\":[{\"eType\":\"Keeper\",\"name\":\"Keeper\",\"properties\":[\"name\"]}," +
                "{\"eType\":\"Lion\",\"name\":\"Lion\",\"properties\":[\"mane\"]}," +
                "{\"eType\":\"Snake\",\"name\":\"Snake\",\"properties\":[\"length\"]}]," +
                "\"relationshipTypes\":[{\"rType\":\"Feeds\",\"name\":\"Feeds\",\"directional\":true," +
                "\"ePairs\":[{\"eTypeA\":\"Keeper\",\"eTypeB\":\"Lion\"},{\"eTypeA\":\"Keeper\",\"eTypeB\":\"Snake\"}]}]," +
                "\"properties\":[{\"pType\":\"name\",\"name\":\"name\",\"type\":\"string\"}," +
                "{\"pType\":\"mane\",\"name\":\"mane\",\"type\":\"string\"}," +
                "{\"pType\":\"length\",\"name\":\"length\",\"type\":\"int\"}]}", Ontology.class);
        String sdl = OntologyToGraphQLSdl.sdl(ontology);
        Assert.assertTrue(sdl, sdl.contains("    Feeds: [Keeper_Feeds]"));
        Assert.assertTrue(sdl, sdl.contains("union Keeper_Feeds = Lion | Snake"));

        Query both = new GraphQL2QueryTransformer(new GraphQLToOntologyTransformer(), provider(ontology))
                .transform(new QueryInfo<>("{ keeper { name Feeds { ... on Lion { mane } ... on Snake { length } } } }",
                        "q1", TYPE_GRAPHQL, "Zoo"));
        String print = QueryDescriptor.print(both);
        Assert.assertEquals(print, new HashSet<>(Arrays.asList("Lion", "Snake")), single(both, EUntyped.class).getvTypes());
        Assert.assertEquals(print, 1, count(both, EProp.class, "mane"));
        Assert.assertEquals(print, 1, count(both, EProp.class, "length"));

        Query snakes = new GraphQL2QueryTransformer(new GraphQLToOntologyTransformer(), provider(ontology))
                .transform(new QueryInfo<>("{ keeper { Feeds { ... on Snake { length } } } }", "q1", TYPE_GRAPHQL, "Zoo"));
        Assert.assertTrue(QueryDescriptor.print(snakes), QueryDescriptor.print(snakes).contains("Typ[Snake:"));
    }

    private static <T extends EBase> T single(Query query, Class<T> type) {
        List<T> found = query.getElements().stream().filter(type::isInstance).map(type::cast).collect(Collectors.toList());
        Assert.assertEquals(QueryDescriptor.print(query), 1, found.size());
        return found.get(0);
    }

    /** the elements of a type (the properties of a type, when pType is given) */
    private static long count(Query query, Class<? extends EBase> type, String pType) {
        return query.getElements().stream().filter(type::isInstance)
                .filter(e -> pType == null || pType.equals(((EProp) e).getpType()))
                .count();
    }

    @Test
    public void aSchemaImportedEarlierDoesNotLeakIntoOtherOntologies() {
        // the service shares one transformer: an SDL import must not change what a Dragons query runs against
        GraphQLToOntologyTransformer shared = new GraphQLToOntologyTransformer();
        shared.transform("StarWars", resource("graphql/starWars.graphql") + "\n" + resource("graphql/whereSchema.graphql"));
        Query query = translate(shared, "{ dragon { name } }");
        Assert.assertTrue(QueryDescriptor.print(query), QueryDescriptor.print(query).contains("Typ[Dragon:1]"));
    }

    @Test
    public void theGeneratedSchemaHasAQueryFieldPerEntityType() {
        String sdl = OntologyToGraphQLSdl.sdl(dragons);
        for (String field : new String[]{"dragon", "person", "horse", "kingdom", "guild", "profession"}) {
            Assert.assertTrue(sdl, sdl.contains("    " + field + "(where: WhereClause): "));
        }
        // relationship fields on side A, typed by side B; an entity-typed property (Person.profession) is a scalar
        Assert.assertTrue(sdl, sdl.contains("    Fire: [Dragon]"));
        Assert.assertTrue(sdl, sdl.contains("    profession: String"));
        Assert.assertEquals(sdl, new GraphQLToOntologyTransformer().translate(dragons));
        // and it is a valid schema
        Assert.assertTrue(OntologyToGraphQLSdl.registry(dragons).getType("Dragon").isPresent());
    }

    public static String resource(String name) {
        try (InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream(name)) {
            return new String(in.readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
        } catch (java.io.IOException e) {
            throw new IllegalStateException(e);
        }
    }
}
