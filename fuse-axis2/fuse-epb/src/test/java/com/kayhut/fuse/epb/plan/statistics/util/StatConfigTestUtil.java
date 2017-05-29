package com.kayhut.fuse.epb.plan.statistics.util;

import com.kayhut.fuse.epb.plan.statistics.configuration.StatConfig;
import com.kayhut.fuse.stat.model.configuration.StatContainer;

import java.util.Collections;

/**
 * Created by benishue on 29-May-17.
 */
public class StatConfigTestUtil {

    static final String STAT_INDEX_NAME = "stat";
    static final String STAT_TERM_TYPE_NAME = "bucketTerm";
    static final String STAT_STRING_TYPE_NAME = "bucketString";
    static final String STAT_NUMERIC_TYPE_NAME = "bucketNumeric";
    static final String STAT_COUNT_FIELD_NAME = "count";
    static final String STAT_CARDINALITY_FIELD_NAME = "cardinality";
    static final String STAT_FIELD_TERM_NAME = "term";
    static final String STAT_FIELD_NUMERIC_LOWER_NAME = "lower_bound_numeric";
    static final String STAT_FIELD_NUMERIC_UPPER_NAME = "upper_bound_numeric";
    static final String STAT_FIELD_STRING_LOWER_NAME = "lower_bound_string";
    static final String STAT_FIELD_STRING_UPPER_NAME = "upper_bound_string";

    public static StatConfig getStatConfig (StatContainer statContainer) {
        return StatConfig.Builder.statConfig().withStatClusterName("fuse.test_elastic")
                .withStatNodesHosts(Collections.singletonList("localhost"))
                .withStatTransportPort(9300)
                .withStatIndexName(STAT_INDEX_NAME)
                .withStatTermTypeName(STAT_TERM_TYPE_NAME)
                .withStatStringTypeName(STAT_STRING_TYPE_NAME)
                .withStatNumericTypeName(STAT_NUMERIC_TYPE_NAME)
                .withStatCountFieldName(STAT_COUNT_FIELD_NAME)
                .withStatCardinalityFieldName(STAT_CARDINALITY_FIELD_NAME)
                .withStatFieldTermName(STAT_FIELD_TERM_NAME)
                .withStatFieldNumericLowerName(STAT_FIELD_NUMERIC_LOWER_NAME)
                .withStatFieldNumericUpperName(STAT_FIELD_NUMERIC_UPPER_NAME)
                .withStatFieldStringLowerName(STAT_FIELD_STRING_LOWER_NAME)
                .withStatFieldStringUpperName(STAT_FIELD_STRING_UPPER_NAME)
                .withStatContainer(statContainer).build();
    }
}
