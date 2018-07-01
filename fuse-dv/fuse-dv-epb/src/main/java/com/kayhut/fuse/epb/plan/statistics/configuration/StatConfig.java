package com.kayhut.fuse.epb.plan.statistics.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kayhut.fuse.stat.model.configuration.StatContainer;
import com.kayhut.fuse.stat.util.StatUtil;
import com.typesafe.config.Config;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

/**
 * Created by benishue on 24-May-17.
 */
public class StatConfig {

    //region Ctrs
    public StatConfig(Config config) {
        this.statClusterName = config.getString("elasticsearch.stat.cluster.name");
        this.statNodesHosts = config.getStringList("elasticsearch.stat.hosts");
        this.statTransportPort = config.getInt("elasticsearch.stat.port");
        this.statIndexName = config.getString("elasticsearch.stat.index.name");
        this.statTermTypeName = config.getString("elasticsearch.stat.type.term.name");
        this.statStringTypeName = config.getString("elasticsearch.stat.type.string.name");
        this.statNumericTypeName = config.getString("elasticsearch.stat.type.numeric.name");
        this.statGlobalTypeName = config.getString("elasticsearch.stat.type.global.name");
        this.statCountFieldName = config.getString("elasticsearch.stat.count.field");
        this.statCardinalityFieldName = config.getString("elasticsearch.stat.cardinality.field");
        //Hardcoded
        this.statFieldTermName = "term" ;

        this.statFieldNumericDoubleLowerName = "lower_bound_numericDouble";
        this.statFieldNumericDoubleUpperName = "upper_bound_numericDouble";
        this.statFieldNumericLongLowerName = "lower_bound_numericLong";
        this.statFieldNumericLongUpperName = "upper_bound_numericLong";

        this.statFieldStringLowerName = "lower_bound_string";
        this.statFieldStringUpperName = "upper_bound_string";

//        OptionalComp<StatContainer> statJsonConfiguration = getStatJsonConfiguration(config.getString("elasticsearch.stat.configuration.file"));
//        statJsonConfiguration.ifPresent(statContainer -> this.statContainer = statContainer);
    }

    //Used only in the Pattern Builder
    public StatConfig(String statClusterName,
                      List<String> statNodesHosts,
                      int statTransportPort,
                      String statIndexName,
                      String statTermTypeName,
                      String statStringTypeName,
                      String statNumericTypeName,
                      String statGlobalTypeName,
                      String statCountFieldName,
                      String statCardinalityFieldName,
                      String statFieldTermName,
                      String statFieldNumericDoubleLowerName,
                      String statFieldNumericDoubleUpperName,
                       String statFieldNumericLongLowerName,
                       String statFieldNumericLongUpperName,
                      String statFieldStringLowerName,
                      String statFieldStringUpperName,
                      StatContainer statContainer) {
        this.statClusterName = statClusterName;
        this.statNodesHosts = statNodesHosts;
        this.statTransportPort = statTransportPort;
        this.statIndexName = statIndexName;
        this.statTermTypeName = statTermTypeName;
        this.statStringTypeName = statStringTypeName;
        this.statNumericTypeName = statNumericTypeName;
        this.statGlobalTypeName = statGlobalTypeName;
        this.statCountFieldName = statCountFieldName;
        this.statCardinalityFieldName = statCardinalityFieldName;
        this.statFieldTermName = statFieldTermName;

        this.statFieldNumericDoubleLowerName = statFieldNumericDoubleLowerName;
        this.statFieldNumericDoubleUpperName = statFieldNumericDoubleUpperName;
        this.statFieldNumericLongLowerName = statFieldNumericLongLowerName;
        this.statFieldNumericLongUpperName = statFieldNumericLongUpperName;

        this.statFieldStringLowerName = statFieldStringLowerName;
        this.statFieldStringUpperName = statFieldStringUpperName;
        this.statContainer = statContainer;
    }


    //endregion

    //region Public Methods

    //endregion

    //region Private Methods
    private Optional<StatContainer> getStatJsonConfiguration(String statJsonPath) {
        String statConfigJson = StatUtil.readJsonToString(statJsonPath);
        Optional<StatContainer> statContainer;
        try {
            statContainer = Optional.ofNullable(new ObjectMapper().readValue(statConfigJson, StatContainer.class));
        } catch (IOException e) {
            throw new RuntimeException("Failed getTo load statistics configuration\n" + e.getMessage());
        }
        return statContainer;
    }
    //endregion

    //region Getters
    public String getStatClusterName() {
        return statClusterName;
    }

    public List<String> getStatNodesHosts() {
        return statNodesHosts;
    }

    public int getStatTransportPort() {
        return statTransportPort;
    }

    public String getStatIndexName() {
        return statIndexName;
    }

    public String getStatTermTypeName() {
        return statTermTypeName;
    }

    public String getStatStringTypeName() {
        return statStringTypeName;
    }

    public String getStatNumericTypeName() {
        return statNumericTypeName;
    }

    public String getStatGlobalTypeName() {
        return statGlobalTypeName;
    }

    public String getStatCountFieldName() {
        return statCountFieldName;
    }

    public String getStatCardinalityFieldName() {
        return statCardinalityFieldName;
    }

    public StatContainer getStatContainer() {
        return statContainer;
    }

    public String getStatFieldTermName() {
        return statFieldTermName;
    }

    public String getStatFieldStringLowerName() {
        return statFieldStringLowerName;
    }

    public String getStatFieldStringUpperName() {
        return statFieldStringUpperName;
    }

    public String getStatFieldNumericDoubleLowerName() {
        return statFieldNumericDoubleLowerName;
    }

    public String getStatFieldNumericDoubleUpperName() {
        return statFieldNumericDoubleUpperName;
    }

    public String getStatFieldNumericLongLowerName() {
        return statFieldNumericLongLowerName;
    }

    public String getStatFieldNumericLongUpperName() {
        return statFieldNumericLongUpperName;
    }

    //endregion

    //region Fields
    private String statClusterName;
    private List<String> statNodesHosts;
    private int statTransportPort;
    private String statIndexName;
    private String statTermTypeName;
    private String statStringTypeName;
    private String statNumericTypeName;
    private String statGlobalTypeName;
    private String statCountFieldName;
    private String statCardinalityFieldName;
    private String statFieldTermName;
    private String statFieldNumericDoubleLowerName;
    private String statFieldNumericDoubleUpperName;
    private String statFieldNumericLongLowerName;
    private String statFieldNumericLongUpperName;
    private String statFieldStringLowerName;
    private String statFieldStringUpperName;
    private StatContainer statContainer;
    //endregion
}
