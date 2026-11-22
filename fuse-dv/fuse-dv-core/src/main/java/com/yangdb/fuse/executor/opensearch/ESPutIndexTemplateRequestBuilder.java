
package com.yangdb.fuse.executor.opensearch;

/*-
 * #%L
 * fuse-dv-core
 * %%
 * Copyright (C) 2016 - 2020 The YangDb Graph Database Project
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

import org.opensearch.common.action.ActionFuture;
import org.opensearch.action.admin.indices.template.put.PutIndexTemplateAction;
import org.opensearch.action.admin.indices.template.put.PutIndexTemplateRequest;
import org.opensearch.action.admin.indices.template.put.PutIndexTemplateRequestBuilder;
import org.opensearch.action.support.master.AcknowledgedResponse;
import org.opensearch.client.OpenSearchClient;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;

/**
 * Index-template request builder that keeps the template mapping as a mutable map until the request is sent, so the
 * mapping factory can keep adding properties to a shared (unified) template.
 * <p>
 * OpenSearch 2.x has no mapping types: the mapping is untyped, {@code {"properties": {...}}}. (Up to 1.3 it was
 * wrapped in a type name, {@code {"Person": {"properties": {...}}}}.)
 */
public class ESPutIndexTemplateRequestBuilder extends PutIndexTemplateRequestBuilder {
    private Map<String, Object> mappings;

    public ESPutIndexTemplateRequestBuilder(OpenSearchClient client, PutIndexTemplateAction action) {
        super(client, action);
    }

    public ESPutIndexTemplateRequestBuilder(OpenSearchClient client, PutIndexTemplateAction action, String name) {
        super(client, action, name);
    }

    /**
     * set the untyped template mapping: {@code {"properties": {...}}}
     */
    public ESPutIndexTemplateRequestBuilder setMapping(Map<String, Object> source) {
        this.mappings = source;
        return this;
    }

    /**
     * @return the untyped template mapping, or null when none was set yet
     */
    public Map<String, Object> getMappings() {
        return mappings;
    }

    @Override
    public PutIndexTemplateRequest request() {
        if (!Objects.isNull(mappings)) {
            super.request.mapping(mappings);
        }
        return super.request();
    }

    @Override
    public ActionFuture<AcknowledgedResponse> execute() {
        request();
        return super.execute();
    }

    /**
     * @return the (mutable) root properties of the mapping, or an empty map when there are none
     */
    public Map<String, Object> getMappingsProperties() {
        try {
            Map<String, Object> properties = (Map<String, Object>) mappings.get("properties");
            return properties != null ? properties : Collections.emptyMap();
        } catch (Throwable notFound) {
            return Collections.emptyMap();
        }
    }
}
