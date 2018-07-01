package com.kayhut.fuse.epb.plan.statistics.configuration;


import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kayhut.fuse.model.execution.plan.Direction;
import com.typesafe.config.Config;
import javaslang.Tuple3;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class ElasticCountStatisticsConfig {

    public ElasticCountStatisticsConfig(Config config) {
        String stats_config_file = config.getString("fuse.elastic_count_stats_config");
        this.globalSelectivity = new HashMap<>();
        InputStream inputStream;
        try {
            inputStream = new FileInputStream(stats_config_file);
            Map<String, Object> rulesMap = new ObjectMapper().readValue(inputStream, Map.class);

            for (Map.Entry<String, Object> ruleGroup : rulesMap.entrySet()) {
                if(ruleGroup.getKey().equals("default")){
                    this.defaultSelectivity = Long.valueOf(ruleGroup.getValue().toString());
                }else{
                    Map<String, Object> edgeTypeRules = (Map<String, Object>) ruleGroup.getValue();
                    for (Map.Entry<String, Object> edgeTypeRule : edgeTypeRules.entrySet()) {
                        if(edgeTypeRule.getKey().equals("default")){
                            this.globalSelectivity.put(new Tuple3<>(ruleGroup.getKey(), "", ""), Long.valueOf(edgeTypeRule.getValue().toString()));
                        }else{
                            Map<String, Integer> directionGroup = (Map<String, Integer>) edgeTypeRule.getValue();
                            for (Map.Entry<String, Integer> direction : directionGroup.entrySet()) {
                                this.globalSelectivity.put(new Tuple3<>(ruleGroup.getKey(), edgeTypeRule.getKey(), direction.getKey()), Long.valueOf(direction.getValue()));

                            }
                        }
                    }
                }
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }


    }

    public long getRelationSelectivity(String rType, String sourceEType, Direction direction){
        Tuple3<String, String, String> set = new Tuple3<>(rType, sourceEType, direction.name());
        if(globalSelectivity.containsKey(set)){
            return globalSelectivity.get(set);
        }
        set = new Tuple3<>(rType, "", "");
        if(globalSelectivity.containsKey(set)){
            return globalSelectivity.get(set);
        }
        return defaultSelectivity;

    }


    private Map<Tuple3<String, String, String>, Long> globalSelectivity;

    private Long defaultSelectivity;
}
