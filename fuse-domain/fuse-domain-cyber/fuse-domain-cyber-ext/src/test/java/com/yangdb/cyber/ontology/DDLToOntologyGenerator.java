package com.yangdb.cyber.ontology;

import com.yangdb.fuse.dispatcher.query.sql.DDLToOntologyTransformer;
import com.yangdb.fuse.executor.elasticsearch.MappingIndexType;
import com.yangdb.fuse.executor.ontology.schema.DDLToIndexProviderTranslator;
import com.yangdb.fuse.model.ontology.Ontology;
import com.yangdb.fuse.model.schema.IndexProvider;
import com.yangdb.fuse.model.schema.Relation;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static java.util.stream.Collectors.groupingBy;

public class DDLToOntologyGenerator {
    public static List<String> tables ;
    public static DDLToOntologyTransformer transformer;
    public static DDLToIndexProviderTranslator indexProviderTranslator;

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
        transformer = new DDLToOntologyTransformer();
        indexProviderTranslator =  new DDLToIndexProviderTranslator();
    }

    @Test
    /**
     * test Ontology Creation according to given list of DDL queries
     */
    public void testOntologyCreation() {
        Ontology ontology = transformer.transform("Cyber", tables);
        Assert.assertNotNull(ontology);
        Assert.assertEquals(17,ontology.getEntityTypes().size());
        Assert.assertEquals(38,ontology.getRelationshipTypes().size());
        Assert.assertEquals(602,ontology.getProperties().size());
    }

    @Test
    /**
     * test index provider creation according to given list of DDL queries + ontology
     */
    public void testIndexProviderCreation() {
        IndexProvider indexProvider = indexProviderTranslator.translate("Cyber", tables);
        Assert.assertNotNull(indexProvider);

        Map<String, List<Relation>> map = indexProvider.getRelations().stream()
                .filter(r -> r.getPartition().equals(MappingIndexType.UNIFIED.name()))
                .collect(groupingBy(r->r.getProps().getValues().get(0)));

        Assert.assertEquals(17,indexProvider.getEntities().size());
        Assert.assertEquals(10, map.size());
        Assert.assertEquals(38,indexProvider.getRelations().size());

    }
}
