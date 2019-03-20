package com.kayhut.fuse.services;

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

import com.kayhut.fuse.services.embedded.ElasticEmbeddedNode;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigValue;
import com.typesafe.config.ConfigValueFactory;
import javaslang.Tuple2;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public abstract class FuseUtils {
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
        if(config.hasPath("elasticsearch.embedded") &&
            config.getBoolean("elasticsearch.embedded")) {
            String nodeName = config.getString("elasticsearch.cluster_name");
            boolean deleteOnLoad = true;
            if(config.hasPath("elasticsearch.delete_data_on_load")) {
                deleteOnLoad = config.getBoolean("elasticsearch.delete_data_on_load");
            }
            int nodePort = config.getInt("elasticsearch.port ");
            String target =  "target/es";
            if(config.hasPath("elasticsearch.workingDir"))
                target = config.getString("elasticsearch.workingDir");

            System.out.println(String.format("Loading embedded server %s on port %d on target %s",nodeName,nodePort,target));
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
