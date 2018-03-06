package com.kayhut.fuse.assembly.knowlegde;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import com.kayhut.fuse.epb.plan.statistics.RefreshableStatisticsProviderFactory;
import com.kayhut.fuse.epb.plan.statistics.RuleBasedStatisticalProvider;
import com.kayhut.fuse.epb.plan.statistics.Statistics;
import com.kayhut.fuse.epb.plan.statistics.StatisticsProvider;
import com.kayhut.fuse.model.execution.plan.Direction;
import com.kayhut.fuse.model.ontology.Ontology;
import com.kayhut.fuse.model.query.EBase;
import com.kayhut.fuse.model.query.Rel;
import com.kayhut.fuse.model.query.entity.EConcrete;
import com.kayhut.fuse.model.query.entity.EEntityBase;
import com.kayhut.fuse.model.query.entity.ETyped;
import com.kayhut.fuse.model.query.entity.EUntyped;
import com.kayhut.fuse.model.query.properties.EPropGroup;
import com.kayhut.fuse.model.query.properties.RelPropGroup;
import com.kayhut.fuse.model.query.properties.constraint.ConstraintOp;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Map;
import java.util.OptionalDouble;

/**
 * Created by lior.perry on 2/18/2018.
 */
public class KnowledgeRuleBasedStatisticalProvider implements RefreshableStatisticsProviderFactory {
    public static final String OPERATORS = "operators";
    public static final String NODES = "nodes";
    public static final String EDGES = "edges";
    public static final String SELECTIVITY = "selectivity";
    public static final String TOTAL = "total";
    public static final String KNOWLEDGE = "Knowledge";
    public static final String DEFAULT = "default";
    public static final String DEFAULT_FILTER = "defaultFilter";
    public static final String FIELD_ID = "fieldId";


    public static final String RULES_SETUP_JSON = "./rules/setup.json";

    private Map<String, Object> map;

    @Inject
    public KnowledgeRuleBasedStatisticalProvider() throws IOException {
        init();
    }

    @Override
    public void refresh()  {
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
                    OptionalDouble max = entityFilter.getProps().stream().filter(p->p.getCon()!=null).mapToDouble(f -> {
                        String property = f.getpType();
                        int propCardinality = 1;

                        if(property.equals(FIELD_ID) && getNode(map, item).containsKey(property)) {
                            propCardinality = (int) ((Map) getNode(map, item).get(property)).getOrDefault(f.getCon().getExpr(),getNode(map, item).get(DEFAULT_FILTER));
                        } else {
                            propCardinality = (int) getNode(map, item).getOrDefault(property, getNode(map, item).get(DEFAULT_FILTER));
                        }
                        ConstraintOp op = f.getCon().getOp();
                        return getOperatorAlpha(map, op) * propCardinality;
                    }).max();
                    double maxValue = Math.max(max.orElse(1), 1);
                    return new Statistics.SummaryStatistics(nodeStatistics.getTotal() / maxValue, nodeStatistics.getCardinality() / maxValue);
                }

                @Override
                public Statistics.SummaryStatistics getEdgeStatistics(Rel item) {
                    return new Statistics.SummaryStatistics(1, 1);
                }

                @Override
                public Statistics.SummaryStatistics getEdgeFilterStatistics(Rel item, RelPropGroup entityFilter) {
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


    public static Map getNode(Map map, EEntityBase item) {
        return (Map) ((Map) map.get(NODES)).getOrDefault(((ETyped) item).geteType(), ((Map) map.get(NODES)).get(DEFAULT));
    }

    public static double getOperatorAlpha(Map map, ConstraintOp op) {
        return (double) ((Map) map.get(OPERATORS)).getOrDefault(op.toString(), 1.0d);
    }

    public static Map getEdge(Map map, Rel item) {
        return (Map) ((Map) map.get(EDGES)).getOrDefault(item.getrType(), Collections.emptyMap());
    }
}
