package com.kayhut.fuse.model;

import com.kayhut.fuse.model.results.QueryResult;
import com.kayhut.fuse.model.transport.BaseContent;

/**
 * Created by lior on 19/02/2017.
 */
public class Path extends BaseContent<QueryResult> {
    private QueryResult data;

    public Path() {}

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

    public static class PathBuilder {
        private Path path;

        public PathBuilder(String id) {
            path = new Path();
            path.setId(id);
        }

        public static PathBuilder builder(String id) {
            PathBuilder builder = new PathBuilder(id);
            return builder;
        }

        public PathBuilder data(QueryResult data) {
            this.path.setData(data);
            this.path.setCompleted(true);
            return this;
        }

        public Path compose() {
            return path;
        }
    }

}
