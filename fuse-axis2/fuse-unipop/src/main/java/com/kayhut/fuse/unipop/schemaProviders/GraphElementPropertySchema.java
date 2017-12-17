package com.kayhut.fuse.unipop.schemaProviders;

import javaslang.collection.Stream;

import java.util.Collections;

/**
 * Created by moti on 4/27/2017.
 */
public interface GraphElementPropertySchema {
    String getName();
    String getType();
    Iterable<IndexingSchema> getIndexingSchemes();

    interface IndexingSchema {
        enum Type {
            none,
            exact,
            ngrams
        }

        Type getType();
        String getName();

        class Impl implements IndexingSchema {
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

    class Impl implements GraphElementPropertySchema {
        //region Constructors
        public Impl(String name) {
            this(name, null);
        }

        public Impl(String name, String type) {
            this(name, type,
                    Collections.singletonList(
                            new IndexingSchema.Impl(IndexingSchema.Type.exact, name)));

        }

        public Impl(String name, String type, Iterable<IndexingSchema> indexingSchemes) {
            this.name = name;
            this.type = type;
            this.indexingSchemes = Stream.ofAll(indexingSchemes).toJavaList();
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
            return this.indexingSchemes;
        }
        //endregion

        //region Fields
        private String name;
        private String type;
        private Iterable<IndexingSchema> indexingSchemes;
        //endregion
    }
}
