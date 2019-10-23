package com.yangdb.fuse.assembly.knowledge;

/*-
 * #%L
 * fuse-domain-knowledge-ext
 * %%
 * Copyright (C) 2016 - 2019 The YangDb Graph Database Project
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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import com.yangdb.fuse.epb.plan.statistics.RefreshableStatisticsProviderFactory;
import com.yangdb.fuse.epb.plan.statistics.Statistics;
import com.yangdb.fuse.epb.plan.statistics.StatisticsProvider;
import com.yangdb.fuse.model.execution.plan.Direction;
import com.yangdb.fuse.model.ontology.Ontology;
import com.yangdb.fuse.model.query.EBase;
import com.yangdb.fuse.model.query.Rel;
import com.yangdb.fuse.model.query.entity.EConcrete;
import com.yangdb.fuse.model.query.entity.EEntityBase;
import com.yangdb.fuse.model.query.entity.ETyped;
import com.yangdb.fuse.model.query.properties.EProp;
import com.yangdb.fuse.model.query.properties.EPropGroup;
import com.yangdb.fuse.model.query.properties.RelPropGroup;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;


/**
 * Created by lior.perry on 2/18/2018.
 */
public class KnowledgeStaticRuleBasedStatisticalProvider implements RefreshableStatisticsProviderFactory {


    public static final String RULES_SETUP_JSON = "./rules/static_setup.json";

    public static final String RULES = "rules";
    public static final String KNOWLEDGE = "Knowledge";
    public static final String OPERATORS = "operators";
    public static final String NODE = "node";
    public static final String SCORE = "score";
    public static final String FILTER = "filter";
    public static final String COMBINER = "combined";
    public static final String MAX_SCORE = "maxScore";
    public static final int SCORE_FACTOR = 10;

    private Map<String, Object> map;
    private double maxScore;

    @Inject
    public KnowledgeStaticRuleBasedStatisticalProvider() throws IOException {
        init();
    }

    public double getMaxScore() {
        return maxScore;
    }

    @Override
    public void refresh() {
        try {
            init();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String getSetup() {
        try {
            return new ObjectMapper().writeValueAsString(map);
        } catch (JsonProcessingException e) {
            return "Not available";
        }
    }

    private void init() throws IOException {
        InputStream stream = Thread.currentThread().getContextClassLoader().getResourceAsStream(RULES_SETUP_JSON);
        if (stream != null) {
            map = new ObjectMapper().readValue(stream, Map.class);
        } else {
            File setup = new File(RULES_SETUP_JSON);
            map = new ObjectMapper().readValue(setup, Map.class);
        }
        maxScore = (double) (Integer) map.get(MAX_SCORE);
    }

    @Override
    public StatisticsProvider get(Ontology ontology) {
        if (ontology.getOnt().equals(KNOWLEDGE)) {
            return new StatisticsProvider() {
                @Override
                public Statistics.SummaryStatistics getNodeStatistics(EEntityBase item) {
                    double ruleScore = SCORE_FACTOR *getRuleScore((ETyped) item);
                    return new Statistics.SummaryStatistics(maxScore-ruleScore, maxScore-ruleScore);
                }

                @Override
                public Statistics.SummaryStatistics getNodeFilterStatistics(EEntityBase item, EPropGroup entityFilter) {
                    double ruleScore = SCORE_FACTOR *getRuleScore((ETyped) item, entityFilter);
                    return new Statistics.SummaryStatistics(maxScore-ruleScore, maxScore-ruleScore);
                }

                @Override
                public Statistics.SummaryStatistics getEdgeStatistics(Rel item, EEntityBase source) {
                    return new Statistics.SummaryStatistics(1, 1);
                }

                @Override
                public Statistics.SummaryStatistics getEdgeFilterStatistics(Rel item, RelPropGroup entityFilter, EEntityBase source) {
                    return new Statistics.SummaryStatistics(1, 1);
                }

                @Override
                public Statistics.SummaryStatistics getRedundantNodeStatistics(EEntityBase entity, RelPropGroup relPropGroup) {
                    return new Statistics.SummaryStatistics(1, 1);
                }

                @Override
                public long getGlobalSelectivity(Rel rel, RelPropGroup filter, EBase entity, Direction direction) {
                    return 1;
                }
            };
        }
        throw new IllegalArgumentException("Ontology Not Supported " + ontology.getOnt());
    }

    public int getRuleScore(ETyped item) {
        //EConcrete overrides all other rules
        if(item instanceof EConcrete)
            return (int) maxScore/ SCORE_FACTOR;

        OptionalInt max = ((List) map.get(RULES)).stream()
                .filter(rule -> ((Map) rule).get(NODE).equals(item.geteType()))
                .filter(rule -> !((Map) rule).containsKey(FILTER))
                .mapToInt(rule -> (int) ((Map) rule).get(SCORE))
                .max();
        //no rule found return 1 default
        return max.orElseGet(() -> 1);
    }

    public double getRuleScore(ETyped item, EPropGroup entityFilter) {
        //no filters rule cost
        double entityOnlyRule = getRuleScore(item);
        EPropGroup filteredEntityFilter = filterOutEmptyConstraints(entityFilter);

        //match without combiner
        OptionalDouble max = ((List) map.get(RULES)).stream()
                .filter(rule -> !((Map) rule).containsKey(COMBINER))
                .filter(rule -> ((Map) rule).get(NODE).equals(item.geteType()))
                .filter(rule -> matchAnyInFilter((Map) rule,filteredEntityFilter ))
                .mapToDouble(rule -> (double) (Integer) ((Map) rule).get(SCORE) + findOperatorAlpha(FILTER,(Map) rule,entityFilter))
                .max();

        OptionalDouble maxCombiner = ((List) map.get(RULES)).stream()
                .filter(rule -> ((Map) rule).containsKey(COMBINER))
                .filter(rule -> ((Map) rule).get(NODE).equals(item.geteType()))
                .filter(rule -> matchAnyInFilter((Map) rule, filteredEntityFilter))
                .filter(rule -> matchAnyInCombiner((Map) rule, filteredEntityFilter))
                .mapToDouble(rule ->  (double) (Integer) ((Map) rule).get(SCORE) + findOperatorAlpha(COMBINER,(Map) rule,entityFilter))
                .max();

        //no rule found return 1 default
        return (Collections.max(Arrays.asList(entityOnlyRule, max.orElse(1.0), maxCombiner.orElse(1.0))) + filteredEntityFilter.getProps().size() * 0.1);
    }

    private EPropGroup filterOutEmptyConstraints(EPropGroup entityFilter) {
        return new EPropGroup(entityFilter.geteNum(),entityFilter.getProps().stream().filter(p->p.getCon()!=null).collect(Collectors.toList()));
    }

    private boolean matchAnyInCombiner(Map rule, EPropGroup entityFilter) {
        return mathAny(COMBINER, rule, entityFilter);
    }

    private boolean matchAnyInFilter(Map rule, EPropGroup entityFilter) {
        return mathAny(FILTER, rule, entityFilter);
    }

    private boolean mathAny(String key, Map rule, EPropGroup entityFilter) {
        return ((List) rule.getOrDefault(key, Collections.emptyList())).stream()
                .anyMatch(field -> entityFilter.getProps().stream().anyMatch(prop -> prop.getpType().equals(field)));
    }

    private boolean getConst(String prop, Map rule, EPropGroup entityFilter) {
        return ((List) rule.get(prop)).stream().anyMatch(field -> entityFilter.getProps().contains(field));
    }

    public double findOperatorAlpha(String key, Map rule, EPropGroup entityFilter) {
        OptionalDouble max = entityFilter.getProps().stream()
                .filter(p -> ((List) rule.getOrDefault(key,Collections.emptyList())).contains(p.getpType()))
                .mapToDouble(p -> getOperatorAlpha(p))
                .max();


        return max.orElse(0);
    }

    public double getOperatorAlpha(EProp op) {
        if(op.getCon()!=null && op.getCon().getOp()!=null)
            return (double) ((Map) map.get(OPERATORS)).getOrDefault(op.getCon().getOp().toString(), 0.0d);
        return 0;
    }

}
