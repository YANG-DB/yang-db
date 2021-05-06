package com.yangdb.fuse.executor.ontology;

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

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.yangdb.fuse.model.ontology.Ontology;
import com.yangdb.fuse.unipop.schemaProviders.GraphElementSchemaProvider;

import java.util.HashMap;
import java.util.Map;

public class CachedGraphElementSchemaProviderFactory implements GraphElementSchemaProviderFactory {
    public static final String schemaProviderFactoryParameter = "CachedGraphElementSchemaProviderFactory.@schemaProviderFactory";

    //region Constructors
    @Inject
    public CachedGraphElementSchemaProviderFactory(
            @Named(schemaProviderFactoryParameter) GraphElementSchemaProviderFactory schemaProviderFactory) {
        this.schemaProviderFactory = schemaProviderFactory;
        this.schemaProviders = new HashMap<>();
    }
    //endregion

    //region GraphElementSchemaProviderFactory Implementation
    @Override
    public synchronized GraphElementSchemaProvider get(Ontology ontology) {
        return this.schemaProviders.computeIfAbsent(ontology.getOnt(),
                ont -> new GraphElementSchemaProvider.Cached(this.schemaProviderFactory.get(ontology)));
    }
    //endregion

    //region Fields
    private GraphElementSchemaProviderFactory schemaProviderFactory;

    private Map<String, GraphElementSchemaProvider> schemaProviders;
    //endregion
}
