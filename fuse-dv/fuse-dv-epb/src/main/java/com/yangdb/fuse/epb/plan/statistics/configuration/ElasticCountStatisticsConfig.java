package com.yangdb.fuse.epb.plan.statistics.configuration;

/*-
 *
 * fuse-dv-epb
 * %%
 * Copyright (C) 2016 - 2019 yangdb   ------ www.yangdb.org ------
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
 *
 */


import com.fasterxml.jackson.databind.ObjectMapper;
import com.yangdb.fuse.model.execution.plan.Direction;
import com.typesafe.config.Config;
import javaslang.Tuple3;

import java.io.FileInputStream;
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
