package com.kayhut.fuse.model.transport;

import com.kayhut.fuse.model.Content;
import com.kayhut.fuse.model.query.QueryMetadata;
import com.kayhut.fuse.model.process.ResultMetadata;

/**
 * Created by lior on 19/02/2017.
 */
public class ContentResponse implements BaseResponse {

    private String id;
    private QueryMetadata queryMetadata;
    private ResultMetadata resultMetadata;
    private Content content;

    public ContentResponse(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public QueryMetadata getQueryMetadata() {
        return queryMetadata;
    }

    public ResultMetadata getResultMetadata() {
        return resultMetadata;
    }

    public Content getContent() {
        return content;
    }

    public static class ResponseBuilder {
        private ContentResponse response;

        public static ResponseBuilder builder(String id) {
            ResponseBuilder builder = new ResponseBuilder(id);
            return builder;
        }

        public ResponseBuilder(String id) {
            response = new ContentResponse(id);
            response.id = id;
        }

        public ResponseBuilder queryMetadata(QueryMetadata metadata) {
            response.queryMetadata = metadata;
            return this;
        }

        public ResponseBuilder resultMetadata(ResultMetadata metadata) {
            response.resultMetadata = metadata;
            return this;
        }

        public ResponseBuilder data(Content data) {
            this.response.content = data;
            return this;
        }

        public ContentResponse compose() {
            return response;
        }

    }
}
