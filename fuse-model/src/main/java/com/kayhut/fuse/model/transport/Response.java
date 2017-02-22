package com.kayhut.fuse.model.transport;

import com.kayhut.fuse.model.Content;
import com.kayhut.fuse.model.process.QueryMetadata;

/**
 * Created by lior on 19/02/2017.
 */
public class Response {

    private String id;
    private QueryMetadata metadata;
    private Content content;

    public Response(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public QueryMetadata getMetadata() {
        return metadata;
    }

    public Content getContent() {
        return content;
    }

    public static class ResponseBuilder {
        private Response response;

        public static ResponseBuilder builder(String id) {
            ResponseBuilder builder = new ResponseBuilder(id);
            return builder;
        }

        public ResponseBuilder(String id) {
            response = new Response(id);
            response.id = id;
        }

        public ResponseBuilder metadata(QueryMetadata metadata) {
            response.metadata = metadata;
            return this;
        }

        public ResponseBuilder data(Content data) {
            this.response.content = data;
            return this;
        }

        public Response compose() {
            return response;
        }

    }
}
