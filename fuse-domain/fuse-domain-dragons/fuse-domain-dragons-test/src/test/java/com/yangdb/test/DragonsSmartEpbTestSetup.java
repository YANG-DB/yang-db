package com.yangdb.test;

import com.yangdb.fuse.model.GlobalConstants;
import com.yangdb.fuse.model.OntologyTestUtils;
import com.yangdb.fuse.services.test.TestRunner;
import com.yangdb.fuse.stat.StatCalculator;
import com.yangdb.fuse.stat.configuration.StatConfiguration;
import com.yangdb.test.data.DragonsOntology;
import com.yangdb.fuse.test.framework.index.MappingElasticConfigurer;
import com.yangdb.fuse.test.framework.index.MappingFileElasticConfigurer;
import com.yangdb.fuse.test.framework.index.Mappings;
import com.yangdb.fuse.test.framework.populator.ElasticDataPopulator;
import javaslang.collection.Stream;
import org.apache.commons.configuration.Configuration;
import org.apache.tinkerpop.gremlin.structure.Direction;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.admin.indices.refresh.RefreshRequest;
import org.elasticsearch.client.transport.TransportClient;

import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.function.Function;

import static com.yangdb.fuse.model.OntologyTestUtils.BIRTH_DATE;
import static com.yangdb.fuse.model.OntologyTestUtils.NAME;

public class DragonsSmartEpbTestSetup extends TestSetupBase  {
    public static void main(String[] args) throws Exception {
        DragonsSmartEpbTestSetup test = new DragonsSmartEpbTestSetup();
        DragonKingdomQueryTest dragonKingdomQueryTest = new DragonKingdomQueryTest();
        TestRunner.run(dragonKingdomQueryTest, test,"m2.smartEpb","m1.dfs.redundant");
    }

    @Override
    protected void loadData(TransportClient client) throws Exception {
        String idField = "id";
        new MappingElasticConfigurer(OntologyTestUtils.DRAGON.name.toLowerCase(), new Mappings().addMapping("pge", getDragonMapping()))
                .configure(client);
        new MappingElasticConfigurer(OntologyTestUtils.KINGDOM.name.toLowerCase(), new Mappings().addMapping("pge", getKingdomMapping()))
                .configure(client);
        new MappingElasticConfigurer(Arrays.asList(ORIGINATED_IN), new Mappings().addMapping("pge", getOriginMapping()))
                .configure(client);

        birthDateValueFunctionFactory = startingDate -> interval -> i -> startingDate + (interval * i);

        sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        int numDragons = 200000;
        int numKingdoms = 2;

        new ElasticDataPopulator(
                client,
                OntologyTestUtils.DRAGON.name.toLowerCase(),
                "pge",
                idField,
                () -> createDragons(numDragons, birthDateValueFunctionFactory.apply(sdf.parse("1980-01-01 00:00:00").getTime()).apply(2592000000L)))
                .populate(); // date interval is ~ 1 month

        new ElasticDataPopulator(client,
                OntologyTestUtils.KINGDOM.name.toLowerCase(),
                "pge",
                idField,
                ()-> createKingdoms(numKingdoms))
                .populate();

        new ElasticDataPopulator(client,
                ORIGINATED_IN,
                "pge",
                idField,
                () -> createOriginEdges(numDragons, numKingdoms)
        ).populate();
        client.admin().indices().refresh(new RefreshRequest(
                DragonsOntology.DRAGON.name.toLowerCase(),
                OntologyTestUtils.KINGDOM.name.toLowerCase(),
                ORIGINATED_IN
        )).actionGet();

        new MappingFileElasticConfigurer("stat", Paths.get("fuse-test","fuse-benchmarks-test","src","main","resources","stat","stat_mappings.json").toString()).configure(client);
        Configuration statConfig = new StatConfiguration("stat/statistics.test.properties").getInstance();
        StatCalculator.run(client, client, statConfig);
        client.admin().indices().refresh(new RefreshRequest("stat")).actionGet();
    }

    @Override
    protected void cleanData(TransportClient client) {
        client.admin().indices()
                .delete(new DeleteIndexRequest(
                        OntologyTestUtils.DRAGON.name.toLowerCase(),
                        OntologyTestUtils.KINGDOM.name.toLowerCase(),
                        "originated_in"))
                .actionGet();
        client.admin().indices().delete(new DeleteIndexRequest("stat")).actionGet();
    }

    private static Mappings.Mapping getDragonMapping() {
        return new Mappings.Mapping()
                .addProperty("type", new Mappings.Mapping.Property(Mappings.Mapping.Property.Type.keyword))
                .addProperty(NAME.name, new Mappings.Mapping.Property(Mappings.Mapping.Property.Type.keyword))
                .addProperty(BIRTH_DATE.name, new Mappings.Mapping.Property(Mappings.Mapping.Property.Type.date, "yyyy-MM-dd HH:mm:ss||date_optional_time||epoch_millis"));
    }

    private static Mappings.Mapping getKingdomMapping() {
        return new Mappings.Mapping()
                .addProperty("type", new Mappings.Mapping.Property(Mappings.Mapping.Property.Type.keyword))
                .addProperty(NAME.name, new Mappings.Mapping.Property(Mappings.Mapping.Property.Type.keyword));
    }

    private Iterable<Map<String, Object>> createDragons(int numDragons,
                                                        Function<Integer, Long> birthDateValueFunction) {
        return Stream.range(0, numDragons).map(i->{
            Map<String, Object> dragon = new HashMap<>();
            dragon.put("id", "Dragon_" + i);
            dragon.put("type", OntologyTestUtils.DRAGON.name);
            int nameChar = i%58 + 65;
            dragon.put(NAME.name, (char)nameChar);
            dragon.put(BIRTH_DATE.name, sdf.format(new Date(birthDateValueFunction.apply(i))));
            return dragon;
        });


    }

    private static Iterable<Map<String, Object>> createKingdoms(int numKingdoms) {
        return Stream.range(0,numKingdoms).map(i -> {
            Map<String, Object> kingdom = new HashMap<>();
            kingdom.put("id", "Kingdom_" + i);
            kingdom.put("type", "Kingdom");
            kingdom.put(NAME.name, "kingdom" + i);
            //kingdoms.add(kingdom);
            return kingdom;
        });
    }

    private static Mappings.Mapping getOriginMapping() {
        return new Mappings.Mapping()
                .addProperty("type", new Mappings.Mapping.Property(Mappings.Mapping.Property.Type.keyword))
                .addProperty("direction", new Mappings.Mapping.Property(Mappings.Mapping.Property.Type.keyword))
                .addProperty(GlobalConstants.EdgeSchema.SOURCE, new Mappings.Mapping.Property()
                        .addProperty("id", new Mappings.Mapping.Property(Mappings.Mapping.Property.Type.keyword))
                        .addProperty("type", new Mappings.Mapping.Property(Mappings.Mapping.Property.Type.keyword)))
                .addProperty(GlobalConstants.EdgeSchema.DEST, new Mappings.Mapping.Property()
                        .addProperty("id", new Mappings.Mapping.Property(Mappings.Mapping.Property.Type.keyword))
                        .addProperty("type", new Mappings.Mapping.Property(Mappings.Mapping.Property.Type.keyword)));
    }

    private static Iterable<Map<String, Object>> createOriginEdges(int numDragons, int numKingdoms) {
        return Stream.range(0,numDragons).flatMap(i->{
            List<Map<String, Object>> originEdges = new ArrayList<>();
            Map<String, Object> originEdgeOut = new HashMap<>();
            originEdgeOut.put("id", OntologyTestUtils.ORIGINATED_IN.getName() + i*2);
            originEdgeOut.put("type", OntologyTestUtils.ORIGINATED_IN.getName());
            originEdgeOut.put("direction", Direction.OUT.name());

            Map<String, Object> originEdgeIn = new HashMap<>();
            originEdgeIn.put("id", OntologyTestUtils.ORIGINATED_IN.getName() + (i*2+1));
            originEdgeIn.put("type", OntologyTestUtils.ORIGINATED_IN.getName());
            originEdgeIn.put("direction", Direction.IN.name());


            Map<String, Object> dragonEntity = new HashMap<>();
            dragonEntity.put("id", "Dragon_" + i);
            dragonEntity.put("type", OntologyTestUtils.DRAGON.name);

            Map<String, Object> kingdomEntity = new HashMap<>();
            kingdomEntity.put("id", "Kingdom_" + i % numKingdoms);
            kingdomEntity.put("type", OntologyTestUtils.KINGDOM.name);

            originEdgeOut.put(GlobalConstants.EdgeSchema.SOURCE, dragonEntity);
            originEdgeOut.put(GlobalConstants.EdgeSchema.DEST, kingdomEntity);

            originEdgeIn.put(GlobalConstants.EdgeSchema.SOURCE, kingdomEntity);
            originEdgeIn.put(GlobalConstants.EdgeSchema.DEST, dragonEntity);

            originEdges.add(originEdgeOut);
            originEdges.add(originEdgeIn);
            return originEdges;
        });

    }

    private SimpleDateFormat sdf;
    private Function<Long, Function<Long, Function<Integer, Long>>> birthDateValueFunctionFactory;

    public static final String ORIGINATED_IN = "originated_in";
}
