package com.kayhut.fuse.dispatcher.logging;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import org.slf4j.MDC;

import java.io.IOException;

public class ElasticQuery {

    static ObjectMapper mapper = new ObjectMapper();

    public static LogMessage.MDCWriter logQuery(String query) {
        return new ElasticQueryWriter(query);
    }

    public static JsonNode fetchQuery() {
        try {
            return mapper.readTree(MDC.get(RequestIdByScope.Converter.key + "." + ElasticQueryWriter.key) != null
                    ? MDC.get(RequestIdByScope.Converter.key + "." + ElasticQueryWriter.key) : "[]");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
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
            try {
                ArrayNode arrayNode = mapper.createArrayNode();
                String value = MDC.get(RequestIdByScope.Converter.key + "." + key);
                if (value != null) {
                    arrayNode = ((ArrayNode) mapper.readTree(value)).add(mapper.readTree(query));
                } else {
                    arrayNode.add(mapper.readTree(query));
                }
                MDC.put(RequestIdByScope.Converter.key + "." + key, mapper.writeValueAsString(arrayNode));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        //endregion

        //region Fields
        private String query;
        //endregion
    }

}
