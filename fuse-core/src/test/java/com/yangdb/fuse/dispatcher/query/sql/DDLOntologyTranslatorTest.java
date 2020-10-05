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
    public void testTranslation() {
        ontology = transformer.transform(tables);
        Assert.assertNotNull(ontology);
        Assert.assertEquals(ontology.getEnumeratedTypes().size(), 0);
        Ontology.Accessor accessor = new Ontology.Accessor(ontology);
        Assert.assertEquals(5,accessor.get().getEntityTypes().size());
        Assert.assertEquals(4,accessor.get().getRelationshipTypes().size());
        Assert.assertEquals(18,accessor.get().getProperties().size());

        Assert.assertEquals(1,accessor.$entity$("book").getMandatory().size());
        Assert.assertEquals(5,accessor.$entity$("book").getProperties().size());
        Assert.assertEquals(2,accessor.relationBySideA("book").size());
        Assert.assertEquals(1,accessor.relationBySideB("book").size());

        Assert.assertEquals(1,accessor.$entity$("language").getMandatory().size());
        Assert.assertEquals(3,accessor.$entity$("language").getProperties().size());
        Assert.assertEquals(0,accessor.relationBySideA("language").size());
        Assert.assertEquals(1,accessor.relationBySideB("language").size());

        Assert.assertEquals(0,accessor.$entity$("book_store").getMandatory().size());
        Assert.assertEquals(1,accessor.$entity$("book_store").getProperties().size());
        Assert.assertEquals(0,accessor.relationBySideA("book_store").size());
        Assert.assertEquals(1,accessor.relationBySideB("book_store").size());

        Assert.assertEquals(1,accessor.$entity$("author").getMandatory().size());
        Assert.assertEquals(6,accessor.$entity$("author").getProperties().size());
        Assert.assertEquals(0,accessor.relationBySideA("author").size());
        Assert.assertEquals(1,accessor.relationBySideB("author").size());

        Assert.assertEquals(1,accessor.$entity$("book_to_book_store").getMandatory().size());
        Assert.assertEquals(3,accessor.$entity$("book_to_book_store").getProperties().size());
        Assert.assertEquals(2,accessor.relationBySideA("book_to_book_store").size());
        Assert.assertEquals(0,accessor.relationBySideB("book_to_book_store").size());

        Assert.assertEquals(1,accessor.$relation$("fk_book_author").getePairs().size());
        Assert.assertEquals(1,accessor.$relation$("fk_book_language").getePairs().size());
        Assert.assertEquals(1,accessor.$relation$("fk_b2bs_book_store").getePairs().size());
        Assert.assertEquals(1,accessor.$relation$("fk_b2bs_book").getePairs().size());



    }


}
