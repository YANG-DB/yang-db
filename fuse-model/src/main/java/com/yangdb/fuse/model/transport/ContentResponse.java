package com.yangdb.fuse.model.transport;

/*-
 * #%L
 * ContentResponse.java - fuse-model - yangdb - 2,016
 * org.codehaus.mojo-license-maven-plugin-1.16
 * $Id$
 * $HeadURL$
 * %%
 * Copyright (C) 2016 - 2018 yangdb   ------ www.yangdb.org ------
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.yangdb.fuse.model.results.TextContent;
import org.jooby.Status;

import java.util.Optional;
import java.util.function.Predicate;

/**
 * Created by lior.perry on 19/02/2017.
 */
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
@JsonPropertyOrder( {"requestId", "elapsed", "renderElapsed", "totalElapsed" } )
public class ContentResponse<T> implements Response, TextContent {
    public static <T> ContentResponse<T> notFound() {
        return Builder.<T>builder(Status.NOT_FOUND, Status.NOT_FOUND)
                .data(Optional.empty())
                .compose();
    }

    public static <T> ContentResponse<T> internalError(Exception ex) {
        return Builder.<T>builder(Status.SERVER_ERROR, Status.SERVER_ERROR)
                .data(Optional.empty())
                .error(ex)
                .compose();
    }

    //region Constructors
    public ContentResponse() {
    }
    //endregion

    //region Properties
    public String getRequestId() {
        return this.requestId;
    }

    @JsonInclude(JsonInclude.Include.ALWAYS)
    public String getElapsed() {
        return String.format("%08d", this.elapsed);
    }

    @JsonInclude(JsonInclude.Include.ALWAYS)
    public String getRenderElapsed() {
        return String.format("%08d", this.renderElapsed);
    }

    @JsonInclude(JsonInclude.Include.ALWAYS)
    public String getTotalElapsed() {
        return String.format("%08d", this.totalElapsed);
    }

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    public ExternalMetadata getExternal() {
        return this.external;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public T getData() {
        return this.data;
    }

    public Status status() {
        return this.status;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public Exception getError() {
        return this.error;
    }

    @Override
    public String content() {
        if(this.data != null && TextContent.class.isAssignableFrom(this.data.getClass())){
            return ((TextContent)this.data).content();
        }else{
            return this.toString();
        }
    }
    //endregion

    @Override
    public String toString() {
        return String.format("ContentResponse{status=%s, requestId=%s, external=%s, elapsed=%d, data=%s}",
                status,
                requestId,
                external,
                elapsed,
                data);
    }

    //region Fields
    private Status status = Status.NOT_FOUND;
    private String requestId;
    private long elapsed;
    private long renderElapsed;
    private long totalElapsed;
    private ExternalMetadata external;
    private T data;
    private Exception error;
    //endregion

    //region Builder
    public static class Builder<T> {
        public static <S> Builder<S> builder(Status success, Status fail) {
            return new Builder<>(success, fail);
        }

        public static <S> Builder<S> builder(ContentResponse<S> response) {
            return new Builder<>(response);
        }

        //region Constructors
        public Builder(Status success, Status fail) {
            this.response = new ContentResponse<>();
            this.success = success;
            this.fail = fail;

            this.successPredicate = response1 -> response1.data != null && response1.error == null;
        }

        public Builder(ContentResponse<T> response) {
            this.response = response;
            this.success = response.status;
            this.fail = response.status;

            this.successPredicate = response1 -> response1.data != null && response1.error == null;
        }
        //endregion

        //region Properties
        public Builder<T> requestId(String requestId) {
            this.response.requestId = requestId;
            return this;
        }

        public Builder<T> external(ExternalMetadata external) {
            this.response.external = external;
            return this;
        }

        public Builder<T> elapsed(long elapsed) {
            this.response.elapsed = elapsed;
            return this;
        }

        public Builder<T> renderElapsed(long renderElapsed) {
            this.response.renderElapsed = renderElapsed;
            return this;
        }

        public Builder<T> totalElapsed(long totalElapsed) {
            this.response.totalElapsed = totalElapsed;
            return this;
        }

        public Builder<T> data(Optional<T> data) {
            this.response.data = data.orElse(null);
            return this;
        }

        public Builder<T> error(Exception ex) {
            this.response.error = ex;
            return this;
        }

        public Builder<T> successPredicate(Predicate<ContentResponse<T>> successPredicate) {
            this.successPredicate = successPredicate;
            return this;
        }

        public ContentResponse<T> compose() {
            if(this.successPredicate.test(this.response)) {
                response.status = success;
            } else {
                response.status = fail;
            }

            return response;
        }
        //endregion

        //region Fields
        private Status success;
        private Status fail;
        private ContentResponse<T> response;
        private Predicate<ContentResponse<T>> successPredicate;
        //endregion
    }
    //endregion
}
