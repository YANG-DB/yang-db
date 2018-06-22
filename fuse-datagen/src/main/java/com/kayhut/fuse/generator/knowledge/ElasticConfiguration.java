package com.kayhut.fuse.generator.knowledge;

/**
 * Created by Roman on 6/22/2018.
 */
public class ElasticConfiguration {
    //region Construtors
    public ElasticConfiguration() {}

    public ElasticConfiguration(Iterable<String> hosts, LightSchema readSchema, LightSchema writeSchema) {
        this.hosts = hosts;
        this.readSchema = readSchema;
        this.writeSchema = writeSchema;
    }
    //endregion

    //region Properties
    public Iterable<String> getHosts() {
        return hosts;
    }

    public void setHosts(Iterable<String> hosts) {
        this.hosts = hosts;
    }

    public LightSchema getReadSchema() {
        return readSchema;
    }

    public void setReadSchema(LightSchema readSchema) {
        this.readSchema = readSchema;
    }

    public LightSchema getWriteSchema() {
        return writeSchema;
    }

    public void setWriteSchema(LightSchema writeSchema) {
        this.writeSchema = writeSchema;
    }
    //endregion

    //region Fields
    private Iterable<String> hosts;
    private LightSchema readSchema;
    private LightSchema writeSchema;
    //endregion
}
