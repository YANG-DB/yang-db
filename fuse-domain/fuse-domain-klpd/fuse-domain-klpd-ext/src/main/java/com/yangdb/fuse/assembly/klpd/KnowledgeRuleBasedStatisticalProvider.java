package com.yangdb.fuse.assembly.klpd;

/*-
 * #%L
 * fuse-domain-knowledge-ext
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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import com.yangdb.fuse.epb.plan.statistics.RefreshableStatisticsProviderFactory;
import com.yangdb.fuse.epb.plan.statistics.RuleBasedStatisticalProvider;
import com.yangdb.fuse.epb.plan.statistics.Statistics;
import com.yangdb.fuse.epb.plan.statistics.StatisticsProvider;
import com.yangdb.fuse.model.execution.plan.Direction;
import com.yangdb.fuse.model.ontology.Ontology;
import com.yangdb.fuse.model.query.EBase;
import com.yangdb.fuse.model.query.Rel;
import com.yangdb.fuse.model.query.entity.EConcrete;
import com.yangdb.fuse.model.query.entity.EEntityBase;
import com.yangdb.fuse.model.query.entity.ETyped;
import com.yangdb.fuse.model.query.entity.EUntyped;
import com.yangdb.fuse.model.query.properties.EProp;
import com.yangdb.fuse.model.query.properties.EPropGroup;
import com.yangdb.fuse.model.query.properties.RelPropGroup;
import com.yangdb.fuse.model.query.properties.constraint.ConstraintOp;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

/**
 * Created by lior.perry on 2/18/2018.
 */
public class KnowledgeRuleBasedStatisticalProvider implements RefreshableStatisticsProviderFactory {
    public static final String IGNORE = "ignore";
    public static final String OPERATORS = "operators";
    public static final String NODES = "nodes";
    public static final String EDGES = "edges";
    public static final String SELECTIVITY = "selectivity";
    public static final String TOTAL = "total";
    public static final String KNOWLEDGE = "Knowledge";
    public static final String DEFAULT = "default";
    public static final String DEFAULT_FILTER = "defaultFilter";
    public static final String COMBINERS = "combiners";
    public static final String COMBINER_FIELDS = "fields";


    public static final String RULES_SETUP_JSON = "./rules/setup.json";

    private Map<String, Object> map;

    @Inject
    public KnowledgeRuleBasedStatisticalProvider() throws IOException {
        init();
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
    }

    @Override
    public StatisticsProvider get(Ontology ontology) {
        if (ontology.getOnt().equals(KNOWLEDGE))
            return new RuleBasedStatisticalProvider() {
                @Override
                public Statistics.SummaryStatistics getNodeStatistics(EEntityBase item) {
                    Map node = getNode(map, item);
                    if (item instanceof EConcrete) {
                        int selectivity = 1;
                        if (!node.isEmpty()) {
                            selectivity = (Integer) node.getOrDefault(SELECTIVITY, selectivity);
                        }
                        return new Statistics.SummaryStatistics(selectivity, selectivity);
                    } else if (item instanceof ETyped)
                        return new Statistics.SummaryStatistics((Integer) node.getOrDefault(TOTAL, 1000) * (Integer) node.getOrDefault(SELECTIVITY, 1),
                                (Integer) node.getOrDefault(TOTAL, 1000) * (Integer) node.getOrDefault(SELECTIVITY, 1));
                    else if (item instanceof EUntyped)
                        return new Statistics.SummaryStatistics(Integer.MAX_VALUE, Integer.MAX_VALUE);

                    return new Statistics.SummaryStatistics(1, 1);
                }

                @Override
                public Statistics.SummaryStatistics getNodeFilterStatistics(EEntityBase item, EPropGroup entityFilter) {
                    Statistics.SummaryStatistics nodeStatistics = getNodeStatistics(item);
                    OptionalDouble max = OptionalDouble.of(1);
                    OptionalDouble fieldIdMax = OptionalDouble.of(1);
                    boolean ignoreGroup = entityFilter.getProps().stream()
                            .filter(p -> p.getCon() != null)
                            .anyMatch(p -> isFilterIgnore(p));
                    if (!ignoreGroup) {
                        int defaultFilter = getEntityDefaultFilter(map, DEFAULT);

                        //default none staged entity statistics
                        if (!((Map) map.get(NODES)).containsKey(((ETyped) item).geteType())) {
                            return new Statistics.SummaryStatistics(nodeStatistics.getTotal() / defaultFilter, nodeStatistics.getCardinality() / defaultFilter);
                        }

                        //first try getting fieldId prop estimation
                        fieldIdMax = entityFilter.getProps().stream()
                                .filter(p -> p.getCon() != null)
                                .filter(p -> !isFilterIgnore(p))
                                .filter(p -> p.getpType() != null)
                                .filter(p -> combiners(map, item).contains(p.getpType()))
                                .mapToDouble(f -> {

                                    //take the root field estimation
                                    double fieldsSum = entityFilter.getProps().stream()
                                            .filter(p -> p.getCon() != null)
                                            .filter(p -> !isFilterIgnore(p))
                                            .filter(p -> p.getpType() != null)
                                            .mapToInt(field -> (int) combinerRootOf(map, item, f.getpType()).getOrDefault(field.getCon().getExpr(),1))
                                            .sum();

                                    //take the additional combiner field estimation
                                    double combinersSum = entityFilter.getProps().stream()
                                            .filter(p -> p.getCon() != null)
                                            .filter(p -> !isFilterIgnore(p))
                                            .filter(p -> p.getpType() != null)
                                            .mapToInt(field -> (int) combinerValuesOf(map, item, f.getpType()).getOrDefault(field.getpType(),1))
                                            .sum();

                                    return combinersSum + fieldsSum;
                                }).max();
                        //get non- fieldId prop estimation
                        max = entityFilter.getProps().stream()
                                .filter(p -> p.getCon() != null)
                                .filter(p -> !isFilterIgnore(p))
                                .filter(p -> p.getpType() != null)
                                .filter(p -> !combiners(map, item).contains(p.getpType()))
                                .mapToDouble(f -> score(map, item, f)).max();
                    }
                    double maxValue = Math.max(max.orElse(1), fieldIdMax.orElse(1));
                    return new Statistics.SummaryStatistics(nodeStatistics.getTotal() / maxValue, nodeStatistics.getCardinality() / maxValue);
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
        throw new IllegalArgumentException("Ontology Not Supported " + ontology.getOnt());
    }

    private Map combinerRootOf(Map map, EEntityBase item, String pType) {
        return (Map) ((Map) getNode(map, item).get(pType)).get(COMBINER_FIELDS);
    }

    private Map combinerValuesOf(Map map, EEntityBase item, String pType) {
        return (Map) ((Map) getNode(map, item).get(pType)).get(COMBINERS);
    }

    private List<String> combiners(Map map, EEntityBase item) {
        return (List<String>) getNode(map, item).getOrDefault(COMBINERS, new ArrayList<String>());
    }

    private boolean isFilterIgnore(EProp prop) {
        //test regexp against condition expression
        Optional<String> ignoreOperator = getIgnoreOperator(map, prop.getCon().getOp());
        if (!ignoreOperator.isPresent()) return false;
        String ignoreRegExp = ignoreOperator.get();
        if (prop.getCon().getExpr() instanceof List) {
            return ((List<String>) prop.getCon().getExpr()).stream().anyMatch(p -> p.matches(ignoreRegExp));
        }
        return prop.getCon().getExpr().toString().matches(ignoreRegExp);
    }

    public static double score(Map map, EEntityBase item, EProp field) {
        String property = field.getpType();
        int propCardinality = (int) getNode(map, item).getOrDefault(property, getNode(map, item).get(DEFAULT_FILTER));
        ConstraintOp op = field.getCon().getOp();
        return getOperatorAlpha(map, op) * propCardinality;
    }

    public static Map getNode(Map map, EEntityBase item) {
        return (Map) ((Map) map.get(NODES)).getOrDefault(((ETyped) item).geteType(), ((Map) map.get(NODES)).get(DEFAULT));
    }

    public static int getEntityDefaultFilter(Map map, String type) {
        return (int) ((Map) ((Map) map.get(NODES)).get(type)).get(DEFAULT_FILTER);
    }

    public static double getOperatorAlpha(Map map, ConstraintOp op) {
        return (double) ((Map) map.get(OPERATORS)).getOrDefault(op.toString(), 1.0d);
    }

    public static Optional<String> getIgnoreOperator(Map map, ConstraintOp op) {
        return ((Map) map.get(IGNORE)).containsKey(op.toString()) ?
                Optional.of((String) ((Map) map.get(IGNORE)).get(op.toString())) :
                Optional.empty();

    }

    public static Map getEdge(Map map, Rel item) {
        return (Map) ((Map) map.get(EDGES)).getOrDefault(item.getrType(), Collections.emptyMap());
    }
}
