package com.yangdb.fuse.model.graphql;

import com.yangdb.fuse.model.ontology.Ontology;
import com.yangdb.fuse.model.ontology.Property;
import org.junit.Assert;
import org.junit.Test;

import java.io.InputStream;

import static com.yangdb.fuse.model.ontology.Property.equal;

public class GraphQLOntologyTranslatorTest {

    @Test
    public void testPropertiesTranslation() {
        InputStream resource = Thread.currentThread().getContextClassLoader().getResourceAsStream("graphql/StarWars.graphql");
        Ontology ontology = GraphQL2OntologyTransformer.transform(resource);
        Assert.assertNotNull(ontology);
        Assert.assertEquals(ontology.getProperties().size(),4);

        Ontology.Accessor accessor = new Ontology.Accessor(ontology);

        Assert.assertTrue(equal(accessor.property$("id"),new Property.MandatoryProperty(new Property("id","id","ID"))));
        Assert.assertTrue(equal(accessor.property$("name"),new Property.MandatoryProperty(new Property("name","name","String"))));
        Assert.assertTrue(equal(accessor.property$("appearsIn"),new Property.MandatoryProperty(new Property("appearsIn","appearsIn","Episode"))));
        Assert.assertTrue(equal(accessor.property$("description"),new Property("description","description","String")));
    }

    @Test
    public void testEntitiesTranslation() {
        InputStream resource = Thread.currentThread().getContextClassLoader().getResourceAsStream("graphql/StarWars.graphql");
        Ontology ontology = GraphQL2OntologyTransformer.transform(resource);
        Assert.assertNotNull(ontology);
        Assert.assertEquals(ontology.getEntityTypes().size(),3);
    }

    @Test
    public void testEnumsTranslation() {
        InputStream resource = Thread.currentThread().getContextClassLoader().getResourceAsStream("graphql/StarWars.graphql");
        Ontology ontology = GraphQL2OntologyTransformer.transform(resource);
        Assert.assertNotNull(ontology);
        Assert.assertEquals(ontology.getEnumeratedTypes().size(),1);
    }
}
