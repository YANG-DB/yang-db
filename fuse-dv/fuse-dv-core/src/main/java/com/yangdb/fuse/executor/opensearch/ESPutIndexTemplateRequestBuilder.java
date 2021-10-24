
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

import org.opensearch.action.ActionFuture;
import org.opensearch.action.admin.indices.template.put.PutIndexTemplateAction;
import org.opensearch.action.admin.indices.template.put.PutIndexTemplateRequest;
import org.opensearch.action.admin.indices.template.put.PutIndexTemplateRequestBuilder;
import org.opensearch.action.support.master.AcknowledgedResponse;
import org.opensearch.client.OpenSearchClient;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;

public class ESPutIndexTemplateRequestBuilder extends PutIndexTemplateRequestBuilder {
    private String type;
    private Map<String, Object> mappings;

    public ESPutIndexTemplateRequestBuilder(OpenSearchClient client, PutIndexTemplateAction action) {
        super(client, action);
    }

    public ESPutIndexTemplateRequestBuilder(OpenSearchClient client, PutIndexTemplateAction action, String name) {
        super(client, action, name);
    }

    @Override
    public PutIndexTemplateRequestBuilder addMapping(String type, Map<String, Object> source) {
        this.type = type;
        this.mappings = source;
        return this;
    }

    public Map<String, Object> getMappings() {
        return mappings;
    }

    public String getType() {
        return type;
    }

    @Override
    public PutIndexTemplateRequest request() {
        if (!Objects.isNull(mappings)) {
            super.request.mapping(type, mappings);
        }
        return super.request();
    }

    @Override
    public ActionFuture<AcknowledgedResponse> execute() {
        request();
        return super.execute();
    }

    public Map<String, Object> getMappingsProperties(String type) {
        try {
            Map<String, Object> map = (Map<String, Object>) mappings.get(type);
            return (Map<String, Object>) map.get("properties");
        }catch (Throwable notFound) {
            return Collections.emptyMap();
        }
    }
}
