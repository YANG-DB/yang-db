package com.yangdb.fuse.dispatcher.ontology;

/*-
 * #%L
 * fuse-core
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



import com.yangdb.fuse.model.schema.IndexProvider;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Created by lior.perry on 3/16/2017.
 */
public interface IndexProviderFactory {
    Optional<IndexProvider> get(String id);

    Collection<IndexProvider> getAll();

    IndexProvider add(IndexProvider provider);

    /**
     * default empty index provider factory
     */
    class EmptyIndexProviderFactory implements IndexProviderFactory {
        private Map<String,IndexProvider> indexProviderMap = new HashMap<>();

        @Override
        public Optional<IndexProvider> get(String id) {
            return Optional.ofNullable(indexProviderMap.get(id));
        }

        @Override
        public Collection<IndexProvider> getAll() {
            return indexProviderMap.values();
        }

        @Override
        public IndexProvider add(IndexProvider provider) {
            return indexProviderMap.put(provider.getOntology(),provider);
        }
    }
}
