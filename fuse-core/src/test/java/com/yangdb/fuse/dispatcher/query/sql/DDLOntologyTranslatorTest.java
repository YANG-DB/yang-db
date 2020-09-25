package com.yangdb.fuse.dispatcher.query.sql;

import com.yangdb.fuse.model.ontology.Ontology;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

//@Ignore("Work in Progress")
public class DDLOntologyTranslatorTest {
    public static Ontology ontology;
    public static List<String> tables ;
    public static DDL2OntologyTransformer transformer;

    @BeforeClass
    public static void setUp() throws Exception {
        tables = new ArrayList<>();
        String sqlPath = Objects.requireNonNull(Thread.currentThread().getContextClassLoader().getResource("sql")).getPath();
        Files.newDirectoryStream(Paths.get(sqlPath),
                path -> path.toString().endsWith(".ddl")).
                forEach(file-> {
                    try {
                        tables.add(new String(Files.readAllBytes(file.toFile().toPath())));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
        transformer = new DDL2OntologyTransformer();
    }

    @Test
    public void testEnumTranslation() {
        ontology = transformer.transform(tables);
        Assert.assertNotNull(ontology);
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
