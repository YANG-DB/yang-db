package com.kayhut.fuse.model;

import com.kayhut.fuse.model.results.QueryResult;
import com.kayhut.fuse.model.transport.BaseContent;

/**
 * Created by lior on 19/02/2017.
 */
public class Graph extends BaseContent<QueryResult> {
    private QueryResult data;

    public Graph() {}


    public void setData(QueryResult data) {
        this.data = data;
    }

    @Override
    public long getResults() {
        return data.toString().length();
    }

    @Override
    public QueryResult getData() {
        return data;
    }

    public static class GraphBuilder {
        private Graph graph;

        private GraphBuilder(String id) {
            graph = new Graph();
            graph.setId(id);
        }

        public static GraphBuilder builder(String id) {
            GraphBuilder builder = new GraphBuilder(id);
            return builder;
        }

        public GraphBuilder data(QueryResult data) {
            this.graph.setData(data);
            this.graph.setCompleted(true);
            return this;
        }

        public Graph compose() {
            return graph;
        }
    }
}
