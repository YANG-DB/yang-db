package com.kayhut.fuse.model.process;

/**
 * resources
 * http://domain/fuse/query/:id
 * http://domain/fuse/query/:id/plan
 * http://domain/fuse/query/:id/cursor/:sequence
 * http://domain/fuse/query/:id/cursor/:sequence/result/:sequence
 */
public class QueryResourceResult {
    private String executionContextId;
    private String planUrl;
    private String cursorUrl;

    public String getExecutionContextId() {
        return this.executionContextId;
    }

    public String getCursorUrl() {
        return cursorUrl;
    }

    public String getPlanUrl() {
        return planUrl;
    }

    public static class ResultMetadataBuilder {
         private QueryResourceResult metadata;

         public static ResultMetadataBuilder build(String executionContextId) {
             return new ResultMetadataBuilder(executionContextId);
         }

         ResultMetadataBuilder(String executionContextId) {
             metadata = new QueryResourceResult();
             metadata.executionContextId = executionContextId;
         }

         public ResultMetadataBuilder cursorUrl(String url) {
             metadata.cursorUrl = url;
             return this;
         }

         public ResultMetadataBuilder planUrl(String plan) {
             metadata.planUrl = plan;
             return this;
         }

         public QueryResourceResult compose() {
             return metadata;
         }

     }
}
