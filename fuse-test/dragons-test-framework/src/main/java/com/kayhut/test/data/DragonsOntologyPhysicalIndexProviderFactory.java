package com.kayhut.test.data;

import com.jayway.jsonpath.JsonPath;
import com.kayhut.fuse.executor.ontology.PhysicalIndexProviderFactory;
import com.kayhut.fuse.model.ontology.Ontology;
import com.kayhut.fuse.unipop.schemaProviders.PhysicalIndexProvider;
import com.kayhut.fuse.unipop.schemaProviders.indexPartitions.IndexPartition;
import com.kayhut.fuse.unipop.schemaProviders.indexPartitions.StaticIndexPartition;
import com.kayhut.fuse.unipop.schemaProviders.indexPartitions.TimeSeriesIndexPartition;
import net.minidev.json.JSONArray;

import javax.inject.Inject;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

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
        this("DragonsIndexProvider.conf");
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
                return Collections::emptyList;
            }
        });
    }

    private IndexPartition buildIndexPartition(JSONArray entity) {
        Optional<IndexPartition> partition = entity.stream().filter(p -> ((Map) p).containsKey(PARTITION)).map(v -> {
            if (((Map) v).get(PARTITION).equals(TIME)) {
                return new TimeBasedIndexPartition((Map) v);
            } else {
                return new StaticIndexPartition(indices((Map) v));
            }
        }).findFirst();
        return partition.orElse(() -> Collections.EMPTY_LIST);
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

    public static class TimeBasedIndexPartition implements TimeSeriesIndexPartition {
        private Map values;
        private SimpleDateFormat dateFormat;

        public TimeBasedIndexPartition(Map values) {
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
            return values.get("partition").toString();
        }

        @Override
        public String getIndexName(Date date) {
            String format = String.format(getIndexFormat(), dateFormat.format(date));
            return StreamSupport.stream(getIndices().spliterator(), false)
                    .filter(s -> s.equals(format)).findFirst().orElse(null);
        }

        @Override
        public Iterable<String> getIndices() {
            return StreamSupport.stream(indices(values).spliterator(), false)
                    .map(p->getIndexFormat().replace("%s",p)).collect(Collectors.toSet());
        }
    }
}
