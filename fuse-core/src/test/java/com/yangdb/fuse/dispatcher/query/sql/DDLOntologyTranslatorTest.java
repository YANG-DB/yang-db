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

        Assert.assertEquals(1,accessor.$entity$("BOOK").getMandatory().size());
        Assert.assertEquals(5,accessor.$entity$("BOOK").getProperties().size());
        Assert.assertEquals(2,accessor.relationBySideA("BOOK").size());
        Assert.assertEquals(1,accessor.relationBySideB("BOOK").size());

        Assert.assertEquals(1,accessor.$entity$("LANGUAGE").getMandatory().size());
        Assert.assertEquals(3,accessor.$entity$("LANGUAGE").getProperties().size());
        Assert.assertEquals(0,accessor.relationBySideA("LANGUAGE").size());
        Assert.assertEquals(1,accessor.relationBySideB("LANGUAGE").size());

        Assert.assertEquals(0,accessor.$entity$("BOOK_STORE").getMandatory().size());
        Assert.assertEquals(1,accessor.$entity$("BOOK_STORE").getProperties().size());
        Assert.assertEquals(0,accessor.relationBySideA("BOOK_STORE").size());
        Assert.assertEquals(1,accessor.relationBySideB("BOOK_STORE").size());

        Assert.assertEquals(1,accessor.$entity$("AUTHOR").getMandatory().size());
        Assert.assertEquals(6,accessor.$entity$("AUTHOR").getProperties().size());
        Assert.assertEquals(0,accessor.relationBySideA("AUTHOR").size());
        Assert.assertEquals(1,accessor.relationBySideB("AUTHOR").size());

        Assert.assertEquals(1,accessor.$entity$("BOOK_TO_BOOK_STORE").getMandatory().size());
        Assert.assertEquals(3,accessor.$entity$("BOOK_TO_BOOK_STORE").getProperties().size());
        Assert.assertEquals(2,accessor.relationBySideA("BOOK_TO_BOOK_STORE").size());
        Assert.assertEquals(0,accessor.relationBySideB("BOOK_TO_BOOK_STORE").size());

        Assert.assertEquals(1,accessor.$relation$("FK_BOOK_AUTHOR").getePairs().size());
        Assert.assertEquals(1,accessor.$relation$("FK_BOOK_LANGUAGE").getePairs().size());
        Assert.assertEquals(1,accessor.$relation$("FK_B2BS_BOOK_STORE").getePairs().size());
        Assert.assertEquals(1,accessor.$relation$("FK_B2BS_BOOK").getePairs().size());



    }


}
