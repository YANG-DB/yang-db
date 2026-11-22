package com.yangdb.fuse.graphql.translator;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yangdb.fuse.dispatcher.ontology.OntologyProvider;
import com.yangdb.fuse.model.execution.plan.descriptors.QueryDescriptor;
import com.yangdb.fuse.model.ontology.Ontology;
import com.yangdb.fuse.model.query.Query;
import com.yangdb.fuse.model.query.QueryInfo;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.InputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

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

    @Test
    public void personsAndTheDragonsTheyOwn() {
        Query query = translate(new GraphQLToOntologyTransformer(), "{ person { name Own { name } } }");
        String print = QueryDescriptor.print(query);
        Assert.assertTrue(print, print.contains("Typ[Person:1]"));
        Assert.assertTrue(print, print.contains("Rel(Own:"));
        Assert.assertTrue(print, print.contains("Typ[Dragon:"));
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
