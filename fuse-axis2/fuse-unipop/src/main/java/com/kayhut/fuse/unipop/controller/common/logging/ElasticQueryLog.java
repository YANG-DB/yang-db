package com.kayhut.fuse.unipop.controller.common.logging;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Created by lior.perry on 2/7/2018.
 */
public class ElasticQueryLog {
    public static final ElasticQueryLog EMPTY = new ElasticQueryLog();
    public static ObjectMapper mapper = new ObjectMapper();

    private String query;
    private int scrollCount;
    private long hits;
    private int scrollTime;
    private long limit;
    private int scrollSize;

    public static String toJson(ElasticQueryLog log) throws JsonProcessingException {
        return mapper.writeValueAsString(log);
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public int getScrollCount() {
        return scrollCount;
    }

    public void setScrollCount(int scrollCount) {
        this.scrollCount = scrollCount;
    }

    public long getHits() {
        return hits;
    }

    public void setHits(long hits) {
        this.hits = hits;
    }

    public int getScrollTime() {
        return scrollTime;
    }

    public void setScrollTime(int scrollTime) {
        this.scrollTime = scrollTime;
    }

    public long getLimit() {
        return limit;
    }

    public void setLimit(long limit) {
        this.limit = limit;
    }

    public int getScrollSize() {
        return scrollSize;
    }

    public void setScrollSize(int scrollSize) {
        this.scrollSize = scrollSize;
    }

    public static class ElasticQueryLogBuilder {
        private ElasticQueryLog log;

        private ElasticQueryLogBuilder(long limit,int scrollSize, int scrollTime) {
            log = new ElasticQueryLog();
            log.limit = limit;
            log.scrollSize = scrollSize;
            log.scrollTime = scrollTime;
        }

        public static ElasticQueryLogBuilder build(long limit,int scrollSize, int scrollTime) {
            return new ElasticQueryLogBuilder(limit,scrollSize,scrollTime);
        }

        public ElasticQueryLogBuilder query(String query) {
            log.query = query;
            return this;
        }

        public ElasticQueryLogBuilder hits(long hits) {
            log.hits = hits;
            return this;
        }

        public ElasticQueryLogBuilder scroll() {
            log.scrollCount++;
            return this;
        }

        public ElasticQueryLog complete() {
            return log;
        }
    }

}
