package com.yangdb.cyber.ontology;

import com.yangdb.fuse.dispatcher.query.sql.DDL2OntologyTransformer;
import com.yangdb.fuse.model.ontology.Ontology;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class DDLToOntologyGenerator {
    public static List<String> tables ;
    public static DDL2OntologyTransformer transformer;
    public static Ontology ontology;

    @BeforeClass
    public static void setUp() throws Exception {
        tables = new ArrayList<>();
        String sqlPath = Objects.requireNonNull(Thread.currentThread().getContextClassLoader().getResource("sample")).getPath();
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
    public void testOntologyCreation() {
        ontology = transformer.transform(tables);
        Assert.assertNotNull(ontology);
        Assert.assertEquals(12,ontology.getEntityTypes().size());
        Assert.assertEquals(31,ontology.getRelationshipTypes().size());
        Assert.assertEquals(406,ontology.getProperties().size());
    }
}
