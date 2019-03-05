package com.kayhut.fuse.services;

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
            int nodePort = config.getInt("elasticsearch.port ");
            String target =  "target/es";
            if(config.hasPath("elasticsearch.workingDir"))
                target = config.getString("elasticsearch.workingDir");

            System.out.println(String.format("Loading embedded server %s on port %d on target %s",nodeName,nodePort,target));
            closeables.add(new ElasticEmbeddedNode(target, 9200, nodePort, nodeName));
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
