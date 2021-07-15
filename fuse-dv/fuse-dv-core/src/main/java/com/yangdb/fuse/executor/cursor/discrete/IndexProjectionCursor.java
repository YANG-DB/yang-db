package com.yangdb.fuse.executor.cursor.discrete;

/*-
 * #%L
 * fuse-dv-core
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

import com.yangdb.fuse.dispatcher.cursor.Cursor;
import com.yangdb.fuse.dispatcher.cursor.CursorFactory;
import com.yangdb.fuse.executor.cursor.TraversalCursorContext;
import com.yangdb.fuse.executor.ontology.schema.PartitionResolver;
import com.yangdb.fuse.executor.ontology.schema.ProjectionTransformer;
import com.yangdb.fuse.executor.ontology.schema.load.*;
import com.yangdb.fuse.model.ontology.Ontology;
import com.yangdb.fuse.model.projection.ProjectionAssignment;
import com.yangdb.fuse.model.resourceInfo.FuseError;
import com.yangdb.fuse.model.results.AssignmentsQueryResult;
import javaslang.Tuple2;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.client.Client;

import java.util.List;

import static com.yangdb.fuse.model.GlobalConstants.ProjectionConfigs.PROJECTION;

/**
 * this cursor will create a new Index which is the query result projection and populate this index with the query results as the arrive
 */
public class IndexProjectionCursor extends PathsTraversalCursor {

    public static class Factory implements CursorFactory {
        //region CursorFactory Implementation
        @Override
        public Cursor createCursor(Context context) {
            return new IndexProjectionCursor((TraversalCursorContext) context);
        }
        //endregion
    }

    /**
     * This projection mapping is a single unified index containing the entire ontology wrapped into a single index so that
     * every type of query result can be indexed and queried for slice & dice type of questions
     * <p>
     * "properties": {
     *   "entityA": {
     *     "type": "nested",
     *     "properties": {
     *       "entityA_id": {
     *         "type": "integer",
     *       },
     *       "relationA": {
     *         "type": "nested",
     *         "properties": {
     *           "relationA_id": {
     *             "type": "integer",
     *           }
     *         }
     *       }
     *     }
     *   },
     *   "entityB": {
     *     "type": "nested",
     *     "properties": {
     *       "entityB_id": {
     *         "type": "integer",
     *       },
     *       "relationB": {
     *         "type": "nested",
     *         "properties": {
     *           "relationB_id": {
     *             "type": "integer",
     *           }
     *         }
     *       }
     *     }
     *   }
     *   }
     *
     */
    public IndexProjectionCursor(TraversalCursorContext context) {
        super(context);
        resolver = new PartitionResolver.StaticPartitionResolver(PROJECTION);
        transformer = new ProjectionTransformer(new Ontology.Accessor(context.getOntology()));
    }

    @Override
    public AssignmentsQueryResult getNextResults(int numResults) {
        return super.getNextResults(numResults);
    }

    protected AssignmentsQueryResult toQuery(int numResults) {
        //since a projection index exists - we need to transform an assignment to a document with the projection mapping
        AssignmentsQueryResult result = super.toQuery(numResults);
        //transform assignments results to projection document
        DataTransformerContext<List<ProjectionAssignment>> context = transformer.transform(result, GraphDataLoader.Directive.INSERT);
        LoadResponse<String, FuseError> load = load(this.context.getClient(), context);
        //todo register load results to log
        //todo add results to AssignmentsQueryResult ??
        return result;
    }

    /**
     * load data into E/S
     *
     * @param context
     * @return
     */
    private LoadResponse<String, FuseError> load(Client client, DataTransformerContext context) {
        //load bulk requests
        Tuple2<Response, BulkRequestBuilder> tuple = LoadUtils.load(resolver, client, context);
        //submit bulk request
        LoadUtils.submit(tuple._2(), tuple._1());
        return new LoadResponseImpl().response(context.getTransformationResponse()).response(tuple._1());
    }

    // private region
    private PartitionResolver.StaticPartitionResolver resolver;
    private ProjectionTransformer transformer;


}
