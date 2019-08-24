package com.yangdb.dragons.schema;

/*-
 * #%L
 * fuse-domain-dragons-test
 * %%
 * Copyright (C) 2016 - 2018 yangdb   ------ www.yangdb.org ------
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import com.jayway.jsonpath.JsonPath;
import com.yangdb.fuse.executor.ontology.PhysicalIndexProviderFactory;
import com.yangdb.fuse.model.ontology.Ontology;
import com.yangdb.fuse.unipop.schemaProviders.PhysicalIndexProvider;
import com.yangdb.fuse.unipop.schemaProviders.indexPartitions.IndexPartitions;
import com.yangdb.fuse.unipop.schemaProviders.indexPartitions.StaticIndexPartitions;
import com.yangdb.fuse.unipop.schemaProviders.indexPartitions.TimeSeriesIndexPartitions;
import javaslang.collection.Stream;
import net.minidev.json.JSONArray;

import javax.inject.Inject;
import java.text.SimpleDateFormat;
import java.util.*;

import static com.yangdb.fuse.model.Utils.readJsonFile;

/**
 * Created by lior.perry on 6/4/2017.
 */
public class PropertyOntologyPhysicalIndexProviderFactory implements PhysicalIndexProviderFactory {

    public static final String PARTITION = "partition";
    public static final String TIME = "time";
    public static final String PROPS = "props";
    public static final String VALUES = "values";


    //region Constructors
    public PropertyOntologyPhysicalIndexProviderFactory() {
        this("PropertyIndexProvider.conf");
    }


    @Inject
    public PropertyOntologyPhysicalIndexProviderFactory(String configFile) {
        this.physicalIndexProviders = new HashMap<>();
        String conf = readJsonFile("schema/" + configFile);
        this.physicalIndexProviders.put("Property", (label, elementType) -> {
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
