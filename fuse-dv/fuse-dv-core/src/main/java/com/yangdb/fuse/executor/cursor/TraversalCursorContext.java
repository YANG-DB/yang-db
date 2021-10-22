package com.yangdb.fuse.executor.cursor;

/*-
 * #%L
 * fuse-dv-core
 * %%
 * Copyright (C) 2016 - 2019 The YangDb Graph Database Project
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

import com.yangdb.fuse.dispatcher.cursor.CursorFactory;
import com.yangdb.fuse.dispatcher.ontology.OntologyProvider;
import com.yangdb.fuse.dispatcher.provision.CursorRuntimeProvision;
import com.yangdb.fuse.dispatcher.resource.QueryResource;
import com.yangdb.fuse.model.ontology.Ontology;
import com.yangdb.fuse.model.transport.cursor.CreateCursorRequest;
import com.yangdb.fuse.unipop.schemaProviders.GraphElementSchemaProvider;
import org.apache.tinkerpop.gremlin.process.traversal.Path;
import org.apache.tinkerpop.gremlin.process.traversal.Traversal;
import org.elasticsearch.client.Client;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by Roman on 05/04/2017.
 */
public class TraversalCursorContext implements CursorFactory.Context<GraphElementSchemaProvider> {
    //region Constructor
    public TraversalCursorContext(
            Client client,
            GraphElementSchemaProvider schemaProvider,
            OntologyProvider ontologyProvider,
            Ontology ontology,
            QueryResource queryResource,
            CreateCursorRequest cursorRequest,
            CursorRuntimeProvision runtimeProvision,
            Traversal<?, Path> traversal) {
        this.client = client;
        this.schemaProvider = schemaProvider;
        this.ontologyProvider = ontologyProvider;
        this.ontology = ontology;
        this.queryResource = queryResource;
        this.cursorRequest = cursorRequest;
        this.runtimeProvision = runtimeProvision;
        this.traversal = traversal;
        this.hitsCounter = new AtomicLong(0);
    }
    //endregion


    public CursorRuntimeProvision getRuntimeProvision() {
        return runtimeProvision;
    }

    public Client getClient() {
        return client;
    }

    public GraphElementSchemaProvider getSchemaProvider() {
        return schemaProvider;
    }

    //region CursorFactory.Context Implementation
    @Override
    public QueryResource getQueryResource() {
        return this.queryResource;
    }

    @Override
    public CreateCursorRequest getCursorRequest() {
        return this.cursorRequest;
    }
    //endregion

    @Override
    public OntologyProvider getOntologyProvider() {
        return ontologyProvider;
    }

    //region Properties
    public Traversal<?, Path> getTraversal() {
        return this.traversal;
    }

    public Ontology getOntology() {
        return ontology;
    }

    public void setOntology(Ontology ontology) {
        this.ontology = ontology;
    }

    public void setQueryResource(QueryResource queryResource) {
        this.queryResource = queryResource;
    }

    public void setCursorRequest(CreateCursorRequest cursorRequest) {
        this.cursorRequest = cursorRequest;
    }

    public void setTraversal(Traversal<?, Path> traversal) {
        this.traversal = traversal;
    }

//endregion

    /**
     * traverse next result on the storage
y     * @return
     */
    public List<Path> next() {
        return next(1);
    }

    /**
     * traverse next numResults on the storage
     * @param numResults
     * @return
     */
    public List<Path> next(int numResults) {
        List<Path> paths = getTraversal().next(numResults);
        incrementHit(paths.size());
        return paths;
    }

    /**
     * increment the hits consumed by the cursor
     * @return
     */
    private TraversalCursorContext incrementHit(long size) {
        hitsCounter.accumulateAndGet(size, Long::sum);
        return this;
    }

    @Override
    public TraversalCursorContext clone()  {
        return new TraversalCursorContext(client,schemaProvider,ontologyProvider,ontology,queryResource,cursorRequest,runtimeProvision,traversal);
    }

    private Client client;
    private GraphElementSchemaProvider schemaProvider;
    private OntologyProvider ontologyProvider;
    //region Fields
    private Ontology ontology;
    private QueryResource queryResource;
    private CreateCursorRequest cursorRequest;
    private AtomicLong hitsCounter;
    private CursorRuntimeProvision runtimeProvision;
    private Traversal<?, Path> traversal;
    //endregion
}
