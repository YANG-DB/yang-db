package com.yangdb.fuse.client;

/*-
 * #%L
 * fuse-core
 * %%
 * Copyright (C) 2016 - 2019 The Fuse Graph Database Project
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

import com.fasterxml.jackson.core.type.TypeReference;
import com.yangdb.fuse.model.query.Query;
import com.yangdb.fuse.model.resourceInfo.*;
import com.yangdb.fuse.model.results.*;
import com.yangdb.fuse.model.transport.CreatePageRequest;
import com.yangdb.fuse.model.transport.CreateQueryRequest;
import com.yangdb.fuse.model.transport.cursor.CreateCursorRequest;
import com.yangdb.fuse.model.transport.cursor.CreateGraphCursorRequest;

import java.io.IOException;
import java.util.function.Predicate;

public interface FuseClientSupport {

    static long countGraphElements(QueryResultBase pageData) {
        return countGraphElements(pageData, true, true, relationship -> true, entity -> true);
    }

    static long countGraphElements(QueryResultBase pageData, boolean relationship, boolean entities,
                                   Predicate<Relationship> relPredicate, Predicate<Entity> entityPredicate) {
        if (pageData instanceof CsvQueryResult)
            throw new IllegalArgumentException("Cursor returned CsvQueryResult instead of AssignmentsQueryResult");

        if (pageData.getSize() == 0)
            return 0;

        if (pageData instanceof AssignmentsQueryResult
                && ((AssignmentsQueryResult) pageData).getAssignments().isEmpty())
            return 0;

        return ((AssignmentsQueryResult<Entity,Relationship>) pageData).getAssignments().stream()
                .mapToLong(e -> (relationship ? e.getRelationships().stream().filter(relPredicate).count() : 0)
                        + (entities ? e.getEntities().stream().filter(entityPredicate).count() : 0))
                .sum();
    }

    static QueryResourceInfo query(FuseClient fuseClient, FuseResourceInfo fuseResourceInfo, CreateQueryRequest request) throws IOException {
        return fuseClient.postQuery(fuseResourceInfo.getQueryStoreUrl(),request);
    }

    static QueryResultBase query(FuseClient fuseClient, FuseResourceInfo fuseResourceInfo, Query query)
            throws IOException, InterruptedException {
        return query(fuseClient, fuseResourceInfo, query, new CreateGraphCursorRequest());
    }

    static QueryResultBase query(FuseClient fuseClient, FuseResourceInfo fuseResourceInfo, int pageSize, Query query)
            throws IOException, InterruptedException {
        return query(fuseClient, fuseResourceInfo, query, new CreateGraphCursorRequest(new CreatePageRequest(pageSize)));
    }

    static QueryResultBase query(FuseClient fuseClient, FuseResourceInfo fuseResourceInfo, String query, String ontology)
            throws IOException, InterruptedException {
        return query(fuseClient, fuseResourceInfo, query,ontology, new CreateGraphCursorRequest());
    }

    static QueryResultBase query(FuseClient fuseClient, FuseResourceInfo fuseResourceInfo, int pageSize, String query, String ontology)
            throws IOException, InterruptedException {
        return query(fuseClient, fuseResourceInfo, query,ontology, new CreateGraphCursorRequest(new CreatePageRequest(pageSize)));
    }

    static <E,R> QueryResultBase query(FuseClient fuseClient, FuseResourceInfo fuseResourceInfo, Query query, CreateCursorRequest createCursorRequest)
            throws IOException, InterruptedException {
        // get Query URL
        QueryResourceInfo queryResourceInfo = fuseClient.postQuery(fuseResourceInfo.getQueryStoreUrl(), query);
        if(queryResourceInfo.getError()!=null) {
            return new AssignmentsQueryResult<E,R>() {
                @Override
                public int getSize() {
                    return -1;
                }

                public FuseError error() {
                    return queryResourceInfo.getError();
                }

                @Override
                public String toString() {
                    return error().getErrorDescription();
                }
            };
        }

        // Press on Cursor
        CursorResourceInfo cursorResourceInfo = fuseClient.postCursor(queryResourceInfo.getCursorStoreUrl(), createCursorRequest);
        // Press on page to get the relevant page
        PageResourceInfo pageResourceInfo = getPageResourceInfo(fuseClient, cursorResourceInfo, createCursorRequest.getCreatePageRequest() != null ? createCursorRequest.getCreatePageRequest().getPageSize() : 1000);
        // return the relevant data
        return fuseClient.getPageData(pageResourceInfo.getDataUrl());
    }

    static <E,R> QueryResultBase query(FuseClient fuseClient, FuseResourceInfo fuseResourceInfo, String query, String ontology, CreateCursorRequest createCursorRequest)
            throws IOException, InterruptedException {
        // get Query URL
        QueryResourceInfo queryResourceInfo = fuseClient.postQuery(fuseResourceInfo.getQueryStoreUrl(), query,ontology);
        if(queryResourceInfo.getError()!=null) {
            return new AssignmentsQueryResult<E,R>() {
                @Override
                public int getSize() {
                    return -1;
                }

                public FuseError error() {
                    return queryResourceInfo.getError();
                }

                @Override
                public String toString() {
                    return error().getErrorDescription();
                }
            };
        }

        // Press on Cursor
        CursorResourceInfo cursorResourceInfo = fuseClient.postCursor(queryResourceInfo.getCursorStoreUrl(), createCursorRequest);
        // Press on page to get the relevant page
        PageResourceInfo pageResourceInfo = getPageResourceInfo(fuseClient, cursorResourceInfo, createCursorRequest.getCreatePageRequest() != null ? createCursorRequest.getCreatePageRequest().getPageSize() : 1000);
        // return the relevant data
        return fuseClient.getPageData(pageResourceInfo.getDataUrl());
    }

    static QueryResultBase nextPage(FuseClient fuseClient, CursorResourceInfo cursorResourceInfo, int pageSize) throws IOException, InterruptedException {
        PageResourceInfo pageResourceInfo = getPageResourceInfo(fuseClient, cursorResourceInfo, pageSize);
        // return the relevant data
        return fuseClient.getPageData(pageResourceInfo.getDataUrl());

    }

    static QueryResultBase nextPage(FuseClient fuseClient, CursorResourceInfo cursorResourceInfo, TypeReference typeReference, int pageSize) throws IOException, InterruptedException {
        PageResourceInfo pageResourceInfo = getPageResourceInfo(fuseClient, cursorResourceInfo, pageSize);
        // return the relevant data
        return fuseClient.getPageData(pageResourceInfo.getDataUrl(),typeReference);

    }

    static PageResourceInfo getPageResourceInfo(FuseClient fuseClient, CursorResourceInfo cursorResourceInfo, int pageSize) throws IOException, InterruptedException {
        PageResourceInfo pageResourceInfo = fuseClient.postPage(cursorResourceInfo.getPageStoreUrl(),pageSize);
        // Waiting until it gets the response
        while (!pageResourceInfo.isAvailable()) {
            pageResourceInfo = fuseClient.getPage(pageResourceInfo.getResourceUrl());
            if (!pageResourceInfo.isAvailable()) {
                Thread.sleep(10);
            }
        }
        return pageResourceInfo;
    }

}
