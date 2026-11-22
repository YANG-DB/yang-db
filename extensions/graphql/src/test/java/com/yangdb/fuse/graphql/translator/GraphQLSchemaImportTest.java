package com.yangdb.fuse.graphql.translator;

import com.yangdb.fuse.model.ontology.Ontology;
import org.junit.Assert;
import org.junit.Test;

import static com.yangdb.fuse.graphql.translator.GraphQLDragonsQueryTest.resource;

/**
 * Schema import ({@code POST /fuse/graphql/ontology/{id}} runs {@link GraphQLToOntologyTransformer#transform(String, String)}
 * on the service's one shared transformer).
 */
public class GraphQLSchemaImportTest {
    private static final String STAR_WARS = resource("graphql/starWars.graphql");
    private static final String WHERE = resource("graphql/whereSchema.graphql");
    private static final String SPACE_X = resource("graphql/spaceX.graphql");

    // G2: imports keep no state across requests

    @Test
    public void importingTheSameSchemaTwiceGivesTheSameOntology() {
        GraphQLToOntologyTransformer transformer = new GraphQLToOntologyTransformer();
        Ontology first = transformer.transform("StarWars", STAR_WARS + "\n" + WHERE);
        Ontology second = transformer.transform("StarWars", STAR_WARS + "\n" + WHERE);
        Assert.assertEquals(first.getEntityTypes(), second.getEntityTypes());
        Assert.assertEquals(first.getRelationshipTypes(), second.getRelationshipTypes());
        Assert.assertEquals(first.getProperties(), second.getProperties());
    }

    @Test
    public void aSecondImportTranslatesTheSecondSchema() {
        GraphQLToOntologyTransformer transformer = new GraphQLToOntologyTransformer();
        transformer.transform("SpaceX", SPACE_X + "\n" + WHERE);
        Ontology starWars = transformer.transform("StarWars", STAR_WARS + "\n" + WHERE);
        Ontology.Accessor accessor = new Ontology.Accessor(starWars);
        Assert.assertTrue(accessor.entity("Human").isPresent());
        Assert.assertFalse("SpaceX types leaked into the StarWars ontology", accessor.entity("Capsule").isPresent());
        Assert.assertEquals(starWars.getProperties(), new GraphQLToOntologyTransformer()
                .transform("StarWars", STAR_WARS + "\n" + WHERE).getProperties());
    }

    @Test
    public void aFailedImportDoesNotBreakTheNextOne() {
        GraphQLToOntologyTransformer transformer = new GraphQLToOntologyTransformer();
        try {
            transformer.transform("Broken", "type Query { broken: Missing }");
            Assert.fail("an undefined type must fail");
        } catch (RuntimeException expected) {
            // fine
        }
        Ontology starWars = transformer.transform("StarWars", STAR_WARS + "\n" + WHERE);
        Assert.assertTrue(new Ontology.Accessor(starWars).entity("Human").isPresent());
    }

    // G3: a schema that uses the shared where-clause types

    @Test
    public void theWhereClauseTypesAreIncludedWhenMissing() {
        Ontology starWars = new GraphQLToOntologyTransformer().transform("StarWars", STAR_WARS);
        Assert.assertEquals("StarWars", starWars.getOnt());
        Assert.assertTrue(new Ontology.Accessor(starWars).entity("Human").isPresent());
        Assert.assertEquals(new GraphQLToOntologyTransformer().transform("StarWars", STAR_WARS + "\n" + WHERE).getEntityTypes(),
                starWars.getEntityTypes());
    }

    @Test
    public void concatenatedSchemasImport() {
        Ontology starWars = new GraphQLToOntologyTransformer().transform("StarWars", WHERE + "\n" + STAR_WARS);
        Ontology.Accessor accessor = new Ontology.Accessor(starWars);
        Assert.assertTrue(accessor.entity("Human").isPresent());
        Assert.assertTrue(accessor.entity("Droid").isPresent());
        Assert.assertTrue(accessor.relation("friends").isPresent());
    }
}
