package com.kayhut.fuse.executor.elasticsearch.logging;

import com.kayhut.fuse.dispatcher.logging.LogMessage;
import com.kayhut.fuse.dispatcher.logging.RequestIdByScope;
import org.slf4j.MDC;

public class ElasticQuery {

    public static LogMessage.MDCWriter logQuery(String query) {
        return new ElasticQueryWriter(query);
    }

    public static class ElasticQueryWriter implements LogMessage.MDCWriter {
        //region Static
        public static final String key = "elasticQuery";

        public ElasticQueryWriter(String query) {
            this.query = query;
        }
        //endregion

        //region LogMessage.MDCWriter Implementation
        @Override
        public void write() {
            String value = MDC.get(RequestIdByScope.Converter.key + "." + key)!=null ? MDC.get(RequestIdByScope.Converter.key + "." + key) : "";
            MDC.put(RequestIdByScope.Converter.key + "." + key, value+"\n"+query);
        }
            //endregion

            //region Fields
            private String query;
            //endregion
        }


    }
