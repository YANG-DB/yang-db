package com.kayhut.test.data;

import com.jayway.jsonpath.JsonPath;
import com.kayhut.fuse.executor.ontology.PhysicalIndexProviderFactory;
import com.kayhut.fuse.model.ontology.Ontology;
import com.kayhut.fuse.unipop.schemaProviders.PhysicalIndexProvider;
import com.kayhut.fuse.unipop.schemaProviders.indexPartitions.IndexPartitions;
import com.kayhut.fuse.unipop.schemaProviders.indexPartitions.StaticIndexPartitions;
import com.kayhut.fuse.unipop.schemaProviders.indexPartitions.TimeSeriesIndexPartitions;
import javaslang.collection.Stream;
import net.minidev.json.JSONArray;

import javax.inject.Inject;
import java.text.SimpleDateFormat;
import java.util.*;

import static com.kayhut.fuse.model.Utils.readJsonFile;

/**
 * Created by liorp on 6/4/2017.
 */
public class DragonsOntologyPhysicalIndexProviderFactory implements PhysicalIndexProviderFactory {

    public static final String PARTITION = "partition";
    public static final String TIME = "time";
    public static final String PROPS = "props";
    public static final String VALUES = "values";


    //region Constructors
    public DragonsOntologyPhysicalIndexProviderFactory() {
        this("DragonsIndexProvider.json");
    }


    @Inject
    public DragonsOntologyPhysicalIndexProviderFactory(String configFile) {
        this.physicalIndexProviders = new HashMap<>();
        String conf = readJsonFile("schema/" + configFile);
        this.physicalIndexProviders.put("Dragons", (label, elementType) -> {
            try {
                JSONArray read = JsonPath.read(conf, "$['entities'][?(@.type =='" + label + "')]");
                return buildIndexPartition(read);
            } catch (Exception e) {
                return new StaticIndexPartitions(Collections.emptyList());
            }
        });
    }

    private IndexPartitions buildIndexPartition(JSONArray entity) {
        Optional<IndexPartitions> partition = entity.stream().filter(p -> ((Map) p).containsKey(PARTITION)).map(v -> {
            if (((Map) v).get(PARTITION).equals(TIME)) {
                return new TimeBasedIndexPartitions((Map) v);
            } else {
                return new StaticIndexPartitions(indices((Map) v));
            }
        }).findFirst();
        return partition.orElse(new StaticIndexPartitions(Collections.emptyList()));
    }

    private static Iterable<String> indices(Map map) {
        JSONArray array = (JSONArray) ((Map) (map).get(PROPS)).get(VALUES);
        return Arrays.asList(array.toArray(new String[array.size()]));
    }

    //endregion

    //region PhysicalIndexProviderFactory implementation
    @Override
    public PhysicalIndexProvider get(Ontology ontology) {
        return this.physicalIndexProviders.get(ontology.getOnt());
    }
    //endregion

    //region Fields
    private Map<String, PhysicalIndexProvider> physicalIndexProviders;

    public static class TimeBasedIndexPartitions implements TimeSeriesIndexPartitions {
        private Map values;
        private SimpleDateFormat dateFormat;

        public TimeBasedIndexPartitions(Map values) {
            this.values = values;
            this.dateFormat = new SimpleDateFormat(getDateFormat());
        }


        @Override
        public String getDateFormat() {
            return ((Map) (values).get(PROPS)).get("date.format").toString();
        }

        @Override
        public String getIndexPrefix() {
            return ((Map) (values).get(PROPS)).get("prefix").toString();
        }

        @Override
        public String getIndexFormat() {
            return ((Map) (values).get(PROPS)).get("index.format").toString();
        }

        @Override
        public String getTimeField() {
            return ((Map)(values.get(PROPS))).get("partition.field").toString();
        }

        @Override
        public String getIndexName(Date date) {
            String format = String.format(getIndexFormat(), dateFormat.format(date));
            List<String> indices = Stream.ofAll(getPartitions())
                    .flatMap(Partition::getIndices)
                    .filter(index -> index.equals(format))
                    .toJavaList();

            return indices.isEmpty() ? null : indices.get(0);
        }

        @Override
        public Optional<String> getPartitionField() {
            return Optional.of(getTimeField());
        }

        @Override
        public Iterable<Partition> getPartitions() {
            return Collections.singletonList(() -> Stream.ofAll(indices(values))
                    .map(p -> String.format(getIndexFormat(), p))
                    .distinct().sorted()
                    .toJavaList());
        }
    }
}
