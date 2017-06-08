package com.kayhut.test.scenario;

import com.jayway.jsonpath.JsonPath;
import com.kayhut.fuse.model.execution.plan.Direction;
import com.kayhut.fuse.unipop.schemaProviders.indexPartitions.IndexPartition;
import com.kayhut.fuse.unipop.schemaProviders.indexPartitions.StaticIndexPartition;
import com.kayhut.test.data.DragonsOntologyPhysicalIndexProviderFactory;
import net.minidev.json.JSONArray;
import org.elasticsearch.action.bulk.BulkProcessor;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.common.unit.ByteSizeUnit;
import org.elasticsearch.common.unit.ByteSizeValue;
import org.elasticsearch.common.unit.TimeValue;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.*;

import static com.kayhut.fuse.model.Utils.readJsonFile;
import static com.kayhut.test.data.DragonsOntologyPhysicalIndexProviderFactory.*;

/**
 * Created by moti on 6/7/2017.
 */
public abstract class ETLUtils {
    public static final String ENTITY_B_ID = "EntityB.Id";
    public static final String ENTITY_B_TYPE = "EntityB.type";
    public static final String ENTITY_A_TYPE = "EntityA.type";
    public static final String ENTITY_A_ID = "EntityA.Id";
    public static final String DURATION = "duration";
    public static final String START_DATE = "startDate";
    public static final String SINCE = "since";
    public static final String END_DATE = "endDate";
    public static final String ID = "id";
    public static final String ESTABLISH_DATE = "establishDate";

    public static final String PERSON = "Person";
    public static final String KINGDOM = "Kingdom";
    public static final String MEMBER_OF_GUILD = "MemberOf";
    public static final String OWN = "Own";
    public static final String ORIGINATED = "Origin";
    public static final String REGISTERED = "Registered";
    public static final String SUBJECT_OF_KINGDOM = "Subject";
    public static final String KNOWS = "Know";
    public static final String GUILD = "Guild";
    public static final String HORSE = "Horse";
    public static final String FIRE = "Fire";
    public static final String FREEZE = "Freez";
    public static final String DRAGON = "Dragon";

    public static SimpleDateFormat sdf;
    public static final String DIRECTION_FIELD = "dir";

    static String confGraphLayoutProviderFactory = readJsonFile("schema/" + "ETLGraphLayoutFactory.conf");
    static String confDragonsIndexProvider = readJsonFile("schema/" + "DragonsIndexProvider.conf");

    static {
        sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    static SimpleDateFormat dateFormat() {
        return sdf;
    }

    public static String id(String type, String id) {
        return type+"_"+id;
    }

    static BulkProcessor getBulkProcessor(Client client) {
        return BulkProcessor.builder(client,
                new BulkProcessor.Listener() {
                    @Override
                    public void beforeBulk(long l, BulkRequest bulkRequest) {}
                    @Override
                    public void afterBulk(long l, BulkRequest bulkRequest, BulkResponse bulkResponse) {}
                    @Override
                    public void afterBulk(long l, BulkRequest bulkRequest, Throwable throwable) {}
                })
                .setBulkActions(1000)
                .setBulkSize(new ByteSizeValue(1, ByteSizeUnit.GB))
                .setFlushInterval(TimeValue.timeValueSeconds(5))
                .setConcurrentRequests(1)
                .build();
    }

    public static TransportClient getClient() throws UnknownHostException {
        Settings settings = Settings.settingsBuilder()
                .put("cluster.name", "fuse-test")
                .build();

        return TransportClient.builder().settings(settings).build()
                .addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName("13.81.12.209"), 9300))
                .addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName("13.73.165.97"), 9300))
                .addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName("52.166.57.208"), 9300));
    }

    public static Map redundant(String edgeType, Direction direction, String side) {
        JSONArray array = JsonPath.read(confGraphLayoutProviderFactory, "$['entities'][?(@.type == '" + edgeType + "')]['redundant']['" + direction.name() + "']['fields" + side + "']");
        Map<String, String> redundantFields = new HashMap<>();
        array.stream().flatMap(v -> ((JSONArray) v).stream()).forEach(m -> {
            Map<String, String> map = (Map<String, String>) m;
            redundantFields.put(map.get("name"), map.get("redundant_name"));
        });
        return redundantFields;

    }

    public static IndexPartition indexPartition(String label) {
        JSONArray entity = JsonPath.read(confDragonsIndexProvider, "$['entities'][?(@.type =='" + label + "')]");
        Optional<IndexPartition> partition = entity.stream().filter(p -> ((Map) p).containsKey(PARTITION)).map(v -> {
            if (((Map) v).get(PARTITION).equals(TIME)) {
                return new DragonsOntologyPhysicalIndexProviderFactory.TimeBasedIndexPartition((Map) v);
            } else {
                return new StaticIndexPartition(indices((Map) v));
            }
        }).findFirst();
        return partition.orElse(() -> Collections.EMPTY_LIST);
    }

    public static Iterable<String> indices(Map map) {
        JSONArray array = (JSONArray) ((Map) (map).get(PROPS)).get(VALUES);
        return Arrays.asList(array.toArray(new String[array.size()]));
    }


}