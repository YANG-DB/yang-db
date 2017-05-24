package com.kayhut.fuse.epb.plan.statistics.configuration;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigValueFactory;
import org.apache.commons.configuration.Configuration;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;

/**
 * Created by benishue on 24-May-17.
 */
public class StatConfig {

    //region Ctrs
    public StatConfig(Config config) {
        //todo populate config properties
    }

    //endregion

    //region Public Methods

    //endregion

    //region Getters & Setters
    public String getStatClusterName() {
        return statClusterName;
    }

    public void setStatClusterName(String statClusterName) {
        this.statClusterName = statClusterName;
    }

    public List<String> getStatNodesHosts() {
        return statNodesHosts;
    }

    public void setStatNodesHosts(List<String> statNodesHosts) {
        this.statNodesHosts = statNodesHosts;
    }

    public int getStatTransportPort() {
        return statTransportPort;
    }

    public void setStatTransportPort(int statTransportPort) {
        this.statTransportPort = statTransportPort;
    }

    public String getStatIndexName() {
        return statIndexName;
    }

    public void setStatIndexName(String statIndexName) {
        this.statIndexName = statIndexName;
    }

    public String getStatTermTypeName() {
        return statTermTypeName;
    }

    public void setStatTermTypeName(String statTermTypeName) {
        this.statTermTypeName = statTermTypeName;
    }

    public String getStatStringTypeName() {
        return statStringTypeName;
    }

    public void setStatStringTypeName(String statStringTypeName) {
        this.statStringTypeName = statStringTypeName;
    }

    public String getStatNumericTypeName() {
        return statNumericTypeName;
    }

    public void setStatNumericTypeName(String statNumericTypeName) {
        this.statNumericTypeName = statNumericTypeName;
    }

    public String getStatCountFieldName() {
        return statCountFieldName;
    }

    public void setStatCountFieldName(String statCountFieldName) {
        this.statCountFieldName = statCountFieldName;
    }

    public String getStatCardinalityFieldName() {
        return statCardinalityFieldName;
    }

    public void setStatCardinalityFieldName(String statCardinalityFieldName) {
        this.statCardinalityFieldName = statCardinalityFieldName;
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
    private String statCountFieldName;
    private String statCardinalityFieldName;
    //endregion

}
