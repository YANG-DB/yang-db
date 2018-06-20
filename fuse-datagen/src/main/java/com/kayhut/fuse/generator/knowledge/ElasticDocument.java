package com.kayhut.fuse.generator.knowledge;

public class ElasticDocument<T> {
    //region Constructors
    public ElasticDocument(String index, String type, T source) {
        this(index, type, null, null, source);
    }

    public ElasticDocument(String index, String type, String id, T source) {
        this(index, type, id, null, source);
    }

    public ElasticDocument(String index, String type, String id, String routing, T source) {
        this.index = index;
        this.type = type;
        this.id = id;
        this.routing = routing;
        this.source = source;
    }
    //endregion

    //region Properties
    public String getIndex() {
        return index;
    }

    public String getType() {
        return type;
    }

    public String getId() {
        return id;
    }

    public String getRouting() {
        return routing;
    }

    public T getSource() {
        return source;
    }
    //endregion

    //region Fields
    private String index;
    private String type;
    private String id;
    private String routing;

    private T source;
    //endregion
}
