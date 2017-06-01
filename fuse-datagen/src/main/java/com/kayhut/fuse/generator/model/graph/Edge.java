package com.kayhut.fuse.generator.model.graph;

/**
 * Created by benishue on 15-May-17.
 */
public class Edge {

    //region Ctrs
    public Edge(String source, String target) {
        this.source = source;
        this.target = target;
    }
    //endregion

    //region Getters & Setters
    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }
    //endregion

    //region Fields
    private String source;
    private String target;
    //endregion
}
