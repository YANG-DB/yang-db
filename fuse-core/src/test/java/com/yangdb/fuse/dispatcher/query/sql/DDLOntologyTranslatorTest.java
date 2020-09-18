package com.yangdb.fuse.dispatcher.query.sql;

import com.yangdb.fuse.model.ontology.Ontology;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;
import java.net.URL;
import java.nio.file.Files;
import java.util.Arrays;

@Ignore("Work in Progress")
public class DDLOntologyTranslatorTest {
    public static Ontology ontology;

    @BeforeClass
    public static void setUp() throws Exception {
        URL schema = Thread.currentThread().getContextClassLoader().getResource("sql/schema.ddl");
        DDL2OntologyTransformer transformer = new DDL2OntologyTransformer();
        //load owl ontologies - the order of the ontologies is important in regards with the owl dependencies
        assert schema != null;

        ontology = transformer.transform(Arrays.asList(
                new String(Files.readAllBytes(new File(schema.toURI()).toPath()))));
        Assert.assertNotNull(ontology);
    }

    @Test
    public void testEnumTranslation() {
        Assert.assertEquals(ontology.getEnumeratedTypes().size(), 1);
        Ontology.Accessor accessor = new Ontology.Accessor(ontology);

    }

    @Test
    public void testPropertiesTranslation() {
        Assert.assertEquals(ontology.getProperties().size(), 18);
//        Ontology.Accessor accessor = new Ontology.Accessor(ontology);
//        Assert.assertEquals(ontology.getProperties().stream().map(Property::getpType).collect(Collectors.toList()), expected);
    }

    @Test
    public void testEntitiesTranslation() {
        Assert.assertEquals(ontology.getEntityTypes().size(), 11);
        Ontology.Accessor accessor = new Ontology.Accessor(ontology);


    }

    @Test
    public void testRelationsTranslation() {
        Assert.assertEquals(ontology.getRelationshipTypes().size(), 1);
        Ontology.Accessor accessor = new Ontology.Accessor(ontology);


    }

}
