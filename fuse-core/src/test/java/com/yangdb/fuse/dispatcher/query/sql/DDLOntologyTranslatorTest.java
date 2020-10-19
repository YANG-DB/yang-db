package com.yangdb.fuse.dispatcher.query.sql;

import com.typesafe.config.ConfigFactory;
import com.yangdb.fuse.model.ontology.Ontology;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

import static com.yangdb.fuse.dispatcher.query.sql.DDLToOntologyTransformer.*;

public class DDLOntologyTranslatorTest {
    public static Ontology ontology;
    public static List<String> tables ;
    public static DDLToOntologyTransformer transformer;

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
        Map config = new HashMap();
        config.put("assembly","test");
        config.put(String.format("%s.%s","test",RELATIONSHIPS),Arrays.asList("book_to_book_store"));
        config.put(String.format("%s.%s","test",ENTITIES),Arrays.asList("book","book_store","author","language"));

        transformer = new DDLToOntologyTransformer(ConfigFactory.parseMap(config));
    }

    @Test
    public void testTranslation() {
        ontology = transformer.transform("Books", tables);
        Assert.assertNotNull(ontology);
        Assert.assertEquals(ontology.getEnumeratedTypes().size(), 0);
        Ontology.Accessor accessor = new Ontology.Accessor(ontology);
        Assert.assertEquals(4,accessor.get().getEntityTypes().size());
        Assert.assertEquals(1,accessor.get().getRelationshipTypes().size());
        Assert.assertEquals(15,accessor.get().getProperties().size());

        Assert.assertEquals(1,accessor.$entity$("book").getMandatory().size());
        Assert.assertEquals(5,accessor.$entity$("book").getProperties().size());

        Assert.assertEquals(1,accessor.$entity$("language").getMandatory().size());
        Assert.assertEquals(3,accessor.$entity$("language").getProperties().size());

        Assert.assertEquals(0,accessor.$entity$("book_store").getMandatory().size());
        Assert.assertEquals(1,accessor.$entity$("book_store").getProperties().size());

        Assert.assertEquals(1,accessor.$entity$("author").getMandatory().size());
        Assert.assertEquals(6,accessor.$entity$("author").getProperties().size());

        Assert.assertEquals(2,accessor.relation$("book_to_book_store").getMandatory().size());
        Assert.assertEquals(3,accessor.relation$("book_to_book_store").getProperties().size());

        Assert.assertEquals(1,accessor.$relation$("book_to_book_store").getePairs().size());
        Assert.assertEquals(1,accessor.relationBySideB("book").size());
        Assert.assertEquals(1,accessor.relationBySideA("book_store").size());



    }


}
