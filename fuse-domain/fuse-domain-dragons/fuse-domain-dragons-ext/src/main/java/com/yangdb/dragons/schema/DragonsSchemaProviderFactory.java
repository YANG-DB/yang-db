package com.yangdb.dragons.schema;

/*-
 * #%L
 * fuse-domain-dragons-ext
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

import com.google.inject.Inject;
import com.typesafe.config.Config;
import com.yangdb.fuse.executor.ontology.GraphElementSchemaProviderFactory;
import com.yangdb.fuse.executor.ontology.schema.RawSchema;
import com.yangdb.fuse.model.ontology.Ontology;
import com.yangdb.fuse.unipop.schemaProviders.GraphElementSchemaProvider;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by roman.margolis on 28/09/2017.
 */
public class DragonsSchemaProviderFactory implements GraphElementSchemaProviderFactory{
    //region Constructors

    @Inject
    public DragonsSchemaProviderFactory() {
        this.schemaProviders = new HashMap<>();
//        this.schemaProviders.put("Dragons", new DragonsPhysicalSchemaProvider());
        this.schemaProviders.put("Dragons", new M2DragonsPhysicalSchemaProvider());
    }
   //endregion

    //region GraphLayoutProviderFactory Implementation
    @Override
    public GraphElementSchemaProvider get(Ontology ontology) {
        return this.schemaProviders.get(ontology.getOnt());
    }
    //endregion

    //region Fields
    private Map<String, GraphElementSchemaProvider> schemaProviders;
    //endregion
}
