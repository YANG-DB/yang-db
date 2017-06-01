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
        this.statCountFieldName = config.getString("elasticsearch.stat.count.field");
        this.statCardinalityFieldName = config.getString("elasticsearch.stat.cardinality.field");
        this.statFieldTermName = config.getString("elasticsearch.stat.field.term.name");
        this.statFieldNumericLowerName = config.getString("elasticsearch.stat.field.numericLowerName.name");
        this.statFieldNumericUpperName = config.getString("elasticsearch.stat.field.numericUpperName.name");
        this.statFieldStringLowerName = config.getString("elasticsearch.stat.field.stringLowerName.name");
        this.statFieldStringUpperName = config.getString("elasticsearch.stat.field.stringUpperName.name");

        Optional<StatContainer> statJsonConfiguration = getStatJsonConfiguration(config.getString("elasticsearch.stat.configuration.file"));
        statJsonConfiguration.ifPresent(statContainer -> this.statContainer = statContainer);
    }

    //Used only in the Step Builder
    private StatConfig(String statClusterName,
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
                      String statFieldNumericLowerName,
                      String statFieldNumericUpperName,
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
        this.statFieldNumericLowerName = statFieldNumericLowerName;
        this.statFieldNumericUpperName = statFieldNumericUpperName;
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
            throw new RuntimeException("Failed to load statistics configuration");
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

    public String getStatFieldNumericLowerName() {
        return statFieldNumericLowerName;
    }

    public String getStatFieldNumericUpperName() {
        return statFieldNumericUpperName;
    }

    public String getStatFieldStringLowerName() {
        return statFieldStringLowerName;
    }

    public String getStatFieldStringUpperName() {
        return statFieldStringUpperName;
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
    private String statFieldNumericLowerName;
    private String statFieldNumericUpperName;
    private String statFieldStringLowerName;
    private String statFieldStringUpperName;
    private StatContainer statContainer;


    //endregion

    //region Step Builder
    public interface StatClusterNameStep {
        StatNodesHostsStep withStatClusterName(String statClusterName);
    }

    public interface StatNodesHostsStep {
        StatTransportPortStep withStatNodesHosts(List<String> statNodesHosts);
    }

    public interface StatTransportPortStep {
        StatIndexNameStep withStatTransportPort(int statTransportPort);
    }

    public interface StatIndexNameStep {
        StatTermTypeNameStep withStatIndexName(String statIndexName);
    }

    public interface StatTermTypeNameStep {
        StatStringTypeNameStep withStatTermTypeName(String statTermTypeName);
    }

    public interface StatStringTypeNameStep {
        StatNumericTypeNameStep withStatStringTypeName(String statStringTypeName);
    }

    public interface StatNumericTypeNameStep {
        StatGlobalTypeNameStep withStatNumericTypeName(String statNumericTypeName);
    }

    public interface StatGlobalTypeNameStep {
        StatCountFieldNameStep withStatGlobalTypeName(String statGlobalTypeName);
    }

    public interface StatCountFieldNameStep {
        StatCardinalityFieldNameStep withStatCountFieldName(String statCountFieldName);
    }

    public interface StatCardinalityFieldNameStep {
        StatFieldTermNameStep withStatCardinalityFieldName(String statCardinalityFieldName);
    }

    public interface StatFieldTermNameStep {
        StatFieldNumericLowerNameStep withStatFieldTermName(String statFieldTermName);
    }

    public interface StatFieldNumericLowerNameStep {
        StatFieldNumericUpperNameStep withStatFieldNumericLowerName(String statFieldNumericLowerName);
    }

    public interface StatFieldNumericUpperNameStep {
        StatFieldStringLowerNameStep withStatFieldNumericUpperName(String statFieldNumericUpperName);
    }

    public interface StatFieldStringLowerNameStep {
        StatFieldStringUpperNameStep withStatFieldStringLowerName(String statFieldStringLowerName);
    }

    public interface StatFieldStringUpperNameStep {
        StatContainerStep withStatFieldStringUpperName(String statFieldStringUpperName);
    }

    public interface StatContainerStep {
        BuildStep withStatContainer(StatContainer statContainer);
    }

    public interface BuildStep {
        StatConfig build();
    }

    public static class Builder implements StatClusterNameStep, StatNodesHostsStep, StatTransportPortStep, StatIndexNameStep, StatTermTypeNameStep, StatStringTypeNameStep, StatNumericTypeNameStep, StatGlobalTypeNameStep, StatCountFieldNameStep, StatCardinalityFieldNameStep, StatFieldTermNameStep, StatFieldNumericLowerNameStep, StatFieldNumericUpperNameStep, StatFieldStringLowerNameStep, StatFieldStringUpperNameStep, StatContainerStep, BuildStep {
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
        private String statFieldNumericLowerName;
        private String statFieldNumericUpperName;
        private String statFieldStringLowerName;
        private String statFieldStringUpperName;
        private StatContainer statContainer;

        private Builder() {
        }

        public static StatClusterNameStep statConfig() {
            return new Builder();
        }

        @Override
        public StatNodesHostsStep withStatClusterName(String statClusterName) {
            this.statClusterName = statClusterName;
            return this;
        }

        @Override
        public StatTransportPortStep withStatNodesHosts(List<String> statNodesHosts) {
            this.statNodesHosts = statNodesHosts;
            return this;
        }

        @Override
        public StatIndexNameStep withStatTransportPort(int statTransportPort) {
            this.statTransportPort = statTransportPort;
            return this;
        }

        @Override
        public StatTermTypeNameStep withStatIndexName(String statIndexName) {
            this.statIndexName = statIndexName;
            return this;
        }

        @Override
        public StatStringTypeNameStep withStatTermTypeName(String statTermTypeName) {
            this.statTermTypeName = statTermTypeName;
            return this;
        }

        @Override
        public StatNumericTypeNameStep withStatStringTypeName(String statStringTypeName) {
            this.statStringTypeName = statStringTypeName;
            return this;
        }

        @Override
        public StatGlobalTypeNameStep withStatNumericTypeName(String statNumericTypeName) {
            this.statNumericTypeName = statNumericTypeName;
            return this;
        }

        @Override
        public StatCountFieldNameStep withStatGlobalTypeName(String statGlobalTypeName) {
            this.statGlobalTypeName = statGlobalTypeName;
            return this;
        }

        @Override
        public StatCardinalityFieldNameStep withStatCountFieldName(String statCountFieldName) {
            this.statCountFieldName = statCountFieldName;
            return this;
        }

        @Override
        public StatFieldTermNameStep withStatCardinalityFieldName(String statCardinalityFieldName) {
            this.statCardinalityFieldName = statCardinalityFieldName;
            return this;
        }

        @Override
        public StatFieldNumericLowerNameStep withStatFieldTermName(String statFieldTermName) {
            this.statFieldTermName = statFieldTermName;
            return this;
        }

        @Override
        public StatFieldNumericUpperNameStep withStatFieldNumericLowerName(String statFieldNumericLowerName) {
            this.statFieldNumericLowerName = statFieldNumericLowerName;
            return this;
        }

        @Override
        public StatFieldStringLowerNameStep withStatFieldNumericUpperName(String statFieldNumericUpperName) {
            this.statFieldNumericUpperName = statFieldNumericUpperName;
            return this;
        }

        @Override
        public StatFieldStringUpperNameStep withStatFieldStringLowerName(String statFieldStringLowerName) {
            this.statFieldStringLowerName = statFieldStringLowerName;
            return this;
        }

        @Override
        public StatContainerStep withStatFieldStringUpperName(String statFieldStringUpperName) {
            this.statFieldStringUpperName = statFieldStringUpperName;
            return this;
        }

        @Override
        public BuildStep withStatContainer(StatContainer statContainer) {
            this.statContainer = statContainer;
            return this;
        }

        @Override
        public StatConfig build() {
            return new StatConfig(
                    this.statClusterName,
                    this.statNodesHosts,
                    this.statTransportPort,
                    this.statIndexName,
                    this.statTermTypeName,
                    this.statStringTypeName,
                    this.statNumericTypeName,
                    this.statGlobalTypeName,
                    this.statCountFieldName,
                    this.statCardinalityFieldName,
                    this.statFieldTermName,
                    this.statFieldNumericLowerName,
                    this.statFieldNumericUpperName,
                    this.statFieldStringLowerName,
                    this.statFieldStringUpperName,
                    this.statContainer
            );
        }
    }
    //endregion

}
