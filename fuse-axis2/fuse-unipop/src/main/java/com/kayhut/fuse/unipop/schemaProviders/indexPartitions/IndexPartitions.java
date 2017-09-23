package com.kayhut.fuse.unipop.schemaProviders.indexPartitions;

import javaslang.collection.Stream;

import java.util.Optional;

public interface IndexPartitions {
    Optional<String> partitionField();
    Iterable<Partition> partitions();

    interface Partition {
        Iterable<String> indices();

        interface Range<T extends Comparable<T>> extends Partition {
            T from();

            T to();

            boolean isWithin(T value);

            class Impl<T extends Comparable<T>> implements Range<T> {
                //region Constructors
                public Impl(T from, T to, String... indices) {
                    this(from, to, Stream.of(indices));
                }

                public Impl(T from, T to, Iterable<String> indices) {
                    this.from = from;
                    this.to = to;
                    this.indices = Stream.ofAll(indices).toJavaSet();
                }
                //endregion

                //region Range Implementation
                @Override
                public Iterable<String> indices() {
                    return this.indices;
                }

                @Override
                public T from() {
                    return this.from;
                }

                @Override
                public T to() {
                    return this.to;
                }

                @Override
                public boolean isWithin(T value) {
                    return value.compareTo(this.from) >= 0 && value.compareTo(this.to) < 0;
                }
                //endregion

                //region Fields
                private Iterable<String> indices;
                private T from;
                private T to;
                //endregion
            }
        }
    }
}
