package com.yangdb.fuse.dispatcher.query.rdf;

import com.google.common.collect.Sets;
import com.yangdb.fuse.dispatcher.query.graphql.GraphQL2OntologyTransformer;
import com.yangdb.fuse.model.ontology.EnumeratedType;
import com.yangdb.fuse.model.ontology.Ontology;
import com.yangdb.fuse.model.ontology.Property;
import com.yangdb.fuse.model.ontology.Value;
import graphql.schema.GraphQLSchema;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;

import static com.yangdb.fuse.model.ontology.Property.equal;

public class OWLOntologyTranslatorTest {
    public static Ontology ontology;
    public static GraphQLSchema graphQLSchema;

    @BeforeClass
    public static void setUp() throws Exception {
        URL workspace = Thread.currentThread().getContextClassLoader().getResource("rdf/workspace.owl");
        URL user = Thread.currentThread().getContextClassLoader().getResource("rdf/user.owl");
        URL foaf = Thread.currentThread().getContextClassLoader().getResource("rdf/foaf.owl");
        OWL2OntologyTransformer transformer = new OWL2OntologyTransformer();
        //load owl ontologies - the order of the ontologies is important in regards with the owl dependencies
        ontology = transformer.transform(Sets.newHashSet(
                new String(Files.readAllBytes(new File(foaf.toURI()).toPath())),
                new String(Files.readAllBytes(new File(user.toURI()).toPath())),
                new String(Files.readAllBytes(new File(workspace.toURI()).toPath()))));
        Assert.assertNotNull(ontology);
    }

    @Test
    @Ignore("Todo fix")
    public void testEnumTranslation() {
        Assert.assertEquals(ontology.getEnumeratedTypes().size(), 1);
        Ontology.Accessor accessor = new Ontology.Accessor(ontology);

        Assert.assertEquals(accessor.enumeratedType$("Episode"),
                new EnumeratedType("Episode",
                        Arrays.asList(new Value(0, "NEWHOPE"),
                                new Value(1, "EMPIRE"),
                                new Value(2, "JEDI"))));


    }

    @Test
    @Ignore("Todo fix")
    public void testPropertiesTranslation() {
        Assert.assertEquals(ontology.getProperties().size(), 6);
        Ontology.Accessor accessor = new Ontology.Accessor(ontology);

        Assert.assertTrue(equal(accessor.property$("id"), new Property.MandatoryProperty(new Property("id", "id", "ID"))));
        Assert.assertTrue(equal(accessor.property$("name"), new Property.MandatoryProperty(new Property("name", "name", "String"))));
        Assert.assertTrue(equal(accessor.property$("appearsIn"), new Property.MandatoryProperty(new Property("appearsIn", "appearsIn", "Episode"))));
        Assert.assertTrue(equal(accessor.property$("description"), new Property("description", "description", "String")));
        Assert.assertTrue(equal(accessor.property$("primaryFunction"), new Property("primaryFunction", "primaryFunction", "String")));
        Assert.assertTrue(equal(accessor.property$("homePlanet"), new Property("homePlanet", "homePlanet", "String")));
    }

    @Test
    @Ignore("Todo fix")
    public void testEntitiesTranslation() {
        Assert.assertEquals(ontology.getEntityTypes().size(), 3);
        Ontology.Accessor accessor = new Ontology.Accessor(ontology);

        Assert.assertEquals(accessor.entity$("Droid").geteType(), "Droid");
        Assert.assertEquals(accessor.entity$("Droid").getProperties().size(), 5);
        Assert.assertEquals(accessor.entity$("Droid").getMandatory().size(), 3);

        Assert.assertEquals(accessor.entity$("Human").geteType(), "Human");
        Assert.assertEquals(accessor.entity$("Human").getProperties().size(), 5);
        Assert.assertEquals(accessor.entity$("Human").getMandatory().size(), 3);

        Assert.assertEquals(accessor.entity$("Character").geteType(), "Character");
        Assert.assertEquals(accessor.entity$("Character").getProperties().size(), 4);
        Assert.assertEquals(accessor.entity$("Character").getMandatory().size(), 3);

    }

    @Test
    @Ignore("Todo fix")
    public void testRelationsTranslation() {
        Assert.assertEquals(ontology.getRelationshipTypes().size(), 2);
        Ontology.Accessor accessor = new Ontology.Accessor(ontology);

        Assert.assertEquals(accessor.relation$("owns").getrType(), "owns");
        Assert.assertEquals(accessor.relation$("owns").getePairs().size(), 1);

        Assert.assertEquals(accessor.relation$("friends").getrType(), "friends");
        Assert.assertEquals(accessor.relation$("friends").getePairs().size(), 2);

    }

    @Test
    @Ignore("Todo fix")
    public void testOntology2GraphQLTransformation() {
        GraphQLSchema targetSchema = new GraphQL2OntologyTransformer().transform(ontology);
        Ontology ontologyTarget = new GraphQL2OntologyTransformer().transform(targetSchema);

        Assert.assertEquals(ontology.getEntityTypes(),ontologyTarget.getEntityTypes());
        Assert.assertEquals(ontology.getRelationshipTypes(),ontologyTarget.getRelationshipTypes());
        Assert.assertEquals(ontology.getProperties(),ontologyTarget.getProperties());
        Assert.assertEquals(ontology.getEnumeratedTypes(),ontologyTarget.getEnumeratedTypes());

        Assert.assertEquals(targetSchema.getQueryType().getFieldDefinitions().size()
                ,graphQLSchema.getQueryType().getFieldDefinitions().size());
        Assert.assertEquals(targetSchema.getAllTypesAsList().size()
                ,graphQLSchema.getAllTypesAsList().size());

    }

}
