package com.kayhut.fuse.model.descriptors;

import javaslang.collection.Stream;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Created by roman.margolis on 30/11/2017.
 */
public class CompositeDescriptor<Q> implements Descriptor<Q>{
    //region Constructors
    public CompositeDescriptor(Map<Class<?>, Descriptor<? extends Q>> descriptors, Descriptor<? extends Q> defaultDescriptor) {
        this.descriptors = new HashMap<>(descriptors);
        this.defaultDescriptor = defaultDescriptor;
    }
    //endregion

    //region Descriptor Implementation
    @Override
    public String describe(Q item) {
        Optional<Descriptor<? extends Q>> descriptor = Stream.ofAll(this.descriptors.entrySet())
                .filter(entry -> entry.getKey().isAssignableFrom(item.getClass()))
                .sorted((entry1, entry2) -> entry1.getKey().isAssignableFrom(entry2.getKey()) ? 1 : -1)
                .<Descriptor<? extends Q>>map(Map.Entry::getValue).toJavaOptional();

        return descriptor.map(descriptor1 -> descriptor1.describe(wrap(item)))
                .orElseGet(() -> this.defaultDescriptor.describe(wrap(item)));

    }
    //endregion

    //region Private Methods
    private <T> T wrap(Q item) {
        return (T)item;
    }
    //endregion

    //region Fields
    private Map<Class<?>, Descriptor<? extends Q>> descriptors;
    private Descriptor<? extends Q> defaultDescriptor;
    //endregion
}
