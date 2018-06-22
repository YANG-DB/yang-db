package com.kayhut.fuse.generator.knowledge.dataSuppliers;

import com.kayhut.fuse.generator.knowledge.model.KnowledgeEntityBase;

import java.util.Date;
import java.util.function.Supplier;

/**
 * Created by Roman on 6/22/2018.
 */
public class MetadataSupplier implements Supplier<KnowledgeEntityBase.Metadata> {
    //region Constructors
    public MetadataSupplier(Supplier<Date> dateSupplier, Supplier<String> nameSupplier, boolean includeRefs) {
        this.dateSupplier = dateSupplier;
        this.nameSupplier = nameSupplier;
        this.includeRefs = includeRefs;
    }
    //endregion

    //region Supplier<KnowledgeEntityBase.Metadata> Implementation
    @Override
    public KnowledgeEntityBase.Metadata get() {
        //KnowledgeEntityBase.Metadata m = new KnowledgeEntityBase.Metadata()
        return null;
    }
    //endregion

    //region Fields
    private Supplier<Date> dateSupplier;
    private Supplier<String> nameSupplier;
    private boolean includeRefs;
    //endregion
}
