package com.yangdb.fuse.unipop.schemaProviders;

/*-
 *
 * fuse-dv-unipop
 * %%
 * Copyright (C) 2016 - 2019 yangdb   ------ www.yangdb.org ------
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
 *
 */

import javaslang.Tuple2;
import javaslang.collection.Stream;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

/**
 * Created by moti on 4/27/2017.
 */
public interface GraphElementPropertySchema {
    String getName();
    String getType();

    Iterable<IndexingSchema> getIndexingSchemes();
    <T extends IndexingSchema> Optional<T> getIndexingSchema(IndexingSchema.Type type);

    interface IndexingSchema {
        enum Type {
            exact,
            ngrams,
            edgeNgrams
        }

        Type getType();
        String getName();

        abstract class Impl implements IndexingSchema {
            //region Constructors
            public Impl(Type type, String name) {
                this.type = type;
                this.name = name;
            }
            //endregion

            //region IndexingSchema Implementation
            @Override
            public Type getType() {
                return this.type;
            }

            @Override
            public String getName() {
                return this.name;
            }
            //endregion

            //region Fields
            private Type type;
            private String name;
            //endregion
        }
    }

    interface ExactIndexingSchema extends IndexingSchema {
        class Impl extends IndexingSchema.Impl implements ExactIndexingSchema {
            //region Constructors
            public Impl(String name) {
                super(Type.exact, name);
            }
            //endregion
        }
    }

    interface NgramsIndexingSchema extends IndexingSchema {
        int getMaxSize();

        class Impl extends IndexingSchema.Impl implements NgramsIndexingSchema {
            //region Constructors
            public Impl(String name, int maxSize) {
                super(Type.ngrams, name);
                this.maxSize = maxSize;
            }
            //endregion

            //region NgramsIndexingSchema Implementation
            @Override
            public int getMaxSize() {
                return this.maxSize;
            }
            //endregion

            //region Fields
            private int maxSize;
            //endregion
        }
    }

    class Impl implements GraphElementPropertySchema {
        //region Constructors
        public Impl(String name) {
            this(name, null);
        }

        public Impl(String name, String type) {
            this(name, type,
                    Collections.singletonList(
                            new ExactIndexingSchema.Impl(name)));

        }

        public Impl(String name, String type, Iterable<IndexingSchema> indexingSchemes) {
            this.name = name;
            this.type = type;
            this.indexingSchemes = Stream.ofAll(indexingSchemes)
                    .toJavaMap(indexingSchema -> new Tuple2<>(indexingSchema.getType(), indexingSchema));
        }
        //endregion

        //region GraphElementPropertySchema Implementation
        @Override
        public String getName() {
            return this.name;
        }

        @Override
        public String getType() {
            return this.type;
        }

        @Override
        public Iterable<IndexingSchema> getIndexingSchemes() {
            return this.indexingSchemes.values();
        }

        @Override
        public <T extends IndexingSchema> Optional<T> getIndexingSchema(IndexingSchema.Type type) {
            return Optional.ofNullable((T)this.indexingSchemes.get(type));
        }
        //endregion

        //region Fields
        private String name;
        private String type;

        private Map<IndexingSchema.Type, IndexingSchema> indexingSchemes;
        //endregion
    }
}
