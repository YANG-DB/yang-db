package com.yangdb.fuse.services;

/*-
 * #%L
 * fuse-service
 * %%
 * Copyright (C) 2016 - 2019 The Fuse Graph Database Project
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

import com.yangdb.fuse.services.embedded.ElasticEmbeddedNode;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigValue;
import com.typesafe.config.ConfigValueFactory;
import javaslang.Tuple2;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public abstract class FuseUtils {
    public static final String ELASTICSEARCH_EMBEDDED = "elasticsearch.embedded";
    public static final String ELASTICSEARCH_CLUSTER_NAME = "elasticsearch.cluster_name";
    public static final String ELASTICSEARCH_DELETE_DATA_ON_LOAD = "elasticsearch.delete_data_on_load";
    public static final String ELASTICSEARCH_PORT = "elasticsearch.port ";
    public static final String ELASTICSEARCH_WORKING_DIR = "elasticsearch.workingDir";

    private static List<AutoCloseable> closeables = new ArrayList<>();

    public static Config loadConfig(File file, String activeProfile, Tuple2<String, ConfigValue> ... values) {
        Config config = ConfigFactory.parseFile(file);
        config = config.withValue("application.profile", ConfigValueFactory.fromAnyRef(activeProfile, "FuseApp"));
        for (Tuple2<String, ConfigValue> value : values) {
            config = config.withValue(value._1, value._2);
        }
        return config;
    }

    public static boolean loadEmbedded(Config config) throws Exception {
        if(config.hasPath(ELASTICSEARCH_EMBEDDED) &&
            config.getBoolean(ELASTICSEARCH_EMBEDDED)) {
            String nodeName = config.getString(ELASTICSEARCH_CLUSTER_NAME);
            boolean deleteOnLoad = true;
            if(config.hasPath(ELASTICSEARCH_DELETE_DATA_ON_LOAD)) {
                deleteOnLoad = config.getBoolean(ELASTICSEARCH_DELETE_DATA_ON_LOAD);
            }
            int nodePort = config.getInt(ELASTICSEARCH_PORT);
            String target =  "target/es";
            if(config.hasPath(ELASTICSEARCH_WORKING_DIR))
                target = config.getString(ELASTICSEARCH_WORKING_DIR);

            System.out.println(String.format("Loading elasticsearch (embedded?%b) server %s on port %d on target %s",config.getBoolean("elasticsearch.embedded"),nodeName,nodePort,target));
            closeables.add(new ElasticEmbeddedNode(target, 9200, nodePort, nodeName, deleteOnLoad));
            return true;
        }
        return false;
    }

    public static void onStop() {
        System.out.println("Stopping all closeables");
        closeables.forEach(c-> {
            try {
                c.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public static void onStart() {

    }

    public static void onStarted() {

    }
}
