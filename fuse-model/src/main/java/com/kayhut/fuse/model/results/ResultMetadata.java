package com.kayhut.fuse.model.results;

import java.net.URL;

/**
 * Created by lior on 23/02/2017.
 */
public class ResultMetadata {
    private String id;
    private String cursor;
    private String result;

    public String getId() {
        return id;
    }

    public String getCursor() {
        return cursor;
    }

    public String getResult() {
        return result;
    }

     public static class ResultMetadataBuilder {
         private ResultMetadata metadata;

         public static ResultMetadataBuilder build(String id) {
             return new ResultMetadataBuilder(id);
         }

         ResultMetadataBuilder(String id) {
             metadata = new ResultMetadata();
             metadata.id = id;
         }

         public ResultMetadataBuilder cursorUrl(String url) {
             metadata.cursor = url;
             return this;
         }

         public ResultMetadataBuilder resultUrl(String url) {
             metadata.result = url;
             return this;
         }

         public ResultMetadata compose() {
             return metadata;
         }

     }
}
