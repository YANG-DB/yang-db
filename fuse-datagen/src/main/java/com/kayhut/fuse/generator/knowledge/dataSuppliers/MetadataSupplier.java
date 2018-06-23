package com.kayhut.fuse.generator.knowledge.dataSuppliers;

import com.kayhut.fuse.generator.knowledge.model.KnowledgeEntityBase;

import java.util.Collections;
import java.util.Date;
import java.util.function.Supplier;

/**
 * Created by Roman on 6/22/2018.
 */
public class MetadataSupplier implements Supplier<KnowledgeEntityBase.Metadata> {
    //region Constructors
    public MetadataSupplier(Supplier<Date> dateSupplier, Supplier<String> nameSupplier) {
        this(dateSupplier, nameSupplier, Collections::emptyList);
    }

    public MetadataSupplier(Supplier<Date> dateSupplier, Supplier<String> nameSupplier, Supplier<Iterable<String>> refsSupplier) {
        this.dateSupplier = dateSupplier;
        this.nameSupplier = nameSupplier;
        this.refsSupplier = refsSupplier;
    }
    //endregion

    //region Supplier<KnowledgeEntityBase.Metadata> Implementation
    @Override
    public KnowledgeEntityBase.Metadata get() {
        String name = this.nameSupplier.get();
        Date date = this.dateSupplier.get();
        Iterable<String> refs = this.refsSupplier.get();

        return new KnowledgeEntityBase.Metadata(name, date, name, date, refs);
    }
    //endregion

    //region Fields
    private Supplier<Date> dateSupplier;
    private Supplier<String> nameSupplier;
    private Supplier<Iterable<String>> refsSupplier;
    //endregion
}
