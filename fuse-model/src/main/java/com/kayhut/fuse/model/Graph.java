package com.kayhut.fuse.model;

import com.kayhut.fuse.model.transport.UrlContent;

/**
 * Created by lior on 19/02/2017.
 */
public class Graph extends UrlContent {
    private Object data;

    public Graph() {}


    public void setData(Object data) {
        this.data = data;
    }

    @Override
    public long getResults() {
        return data.toString().length();
    }

    @Override
    public Object getData() {
        return data;
    }

    public static class GraphBuilder {
        private Graph graph;

        public GraphBuilder(String id) {
            graph = new Graph();
            graph.setId(id);
        }

        public static GraphBuilder builder(String id) {
            GraphBuilder builder = new GraphBuilder(id);
            return builder;
        }

        public GraphBuilder data(Object data) {
            this.graph.setData(data);
            this.graph.setCompleted(true);
            return this;
        }

        public GraphBuilder url(String url) {
            //todo make this parameter
            this.graph.setUrl("http://localhost:8080/fuse"+url +"/"+graph.getId());
            return this;
        }

        public Graph compose() {
            return graph;
        }
    }
}
