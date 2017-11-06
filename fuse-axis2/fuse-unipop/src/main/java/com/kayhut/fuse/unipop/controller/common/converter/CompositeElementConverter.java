package com.kayhut.fuse.unipop.controller.common.converter;

import javaslang.collection.Stream;

/**
 * Created by roman.margolis on 14/09/2017.
 */
public class CompositeElementConverter<TElementSource, TElementDest> implements ElementConverter<TElementSource, TElementDest> {
    //region Constructors
    public CompositeElementConverter(ElementConverter<TElementSource, TElementDest>...elementConverters) {
        this(Stream.of(elementConverters));
    }

    public CompositeElementConverter(Iterable<ElementConverter<TElementSource, TElementDest>> elementConverters) {
        this.elementConverters = Stream.ofAll(elementConverters).toJavaList();
    }
    //endregion

    //region ElementConverter Implementation
    @Override
    public TElementDest convert(TElementSource tElementSource) {
        for(ElementConverter<TElementSource, TElementDest> elementConverter : this.elementConverters) {
            TElementDest elementDest = elementConverter.convert(tElementSource);
            if (elementDest != null) {
                return elementDest;
            }
        }

        return null;
    }
    //endregion

    //region Fields
    private Iterable<ElementConverter<TElementSource, TElementDest>> elementConverters;
    //endregion
}
