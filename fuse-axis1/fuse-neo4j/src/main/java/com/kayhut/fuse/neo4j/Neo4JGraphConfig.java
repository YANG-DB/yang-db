package com.kayhut.fuse.neo4j;

/**
 * Created by Elad on 5/29/2017.
 */
public class Neo4JGraphConfig implements GraphConfig {

    private String boltUrl;
    private String user;
    private String pwd;

    public Neo4JGraphConfig(String boltUrl, String user, String pwd) {
        this.boltUrl = boltUrl;
        this.user = user;
        this.pwd = pwd;
    }

    @Override
    public String getboltUrl() {
        return this.boltUrl;
    }

    @Override
    public String getUser() {
        return this.user;
    }

    @Override
    public String getPwd() {
        return this.pwd;
    }
}
