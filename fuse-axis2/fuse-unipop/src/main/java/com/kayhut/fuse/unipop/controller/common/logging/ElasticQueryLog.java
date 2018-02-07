package com.kayhut.fuse.unipop.controller.common.logging;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by lior.perry on 2/7/2018.
 */
public class ElasticQueryLog {
    public static final ElasticQueryLog EMPTY = new ElasticQueryLog();
    /**
     * map containing the elastic execution query
     * query.cursor.page => query log
     */
    public static ConcurrentHashMap<String,ElasticQueryLog> logs = new ConcurrentHashMap<>();

    private String query;
    private int scrollCount;
    private long hits;
    private int scrollTime;
    private long limit;
    private int scrollSize;

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
