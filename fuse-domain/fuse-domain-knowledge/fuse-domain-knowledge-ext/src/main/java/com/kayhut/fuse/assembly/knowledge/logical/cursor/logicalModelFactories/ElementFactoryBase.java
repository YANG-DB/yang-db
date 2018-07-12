package com.kayhut.fuse.assembly.knowledge.logical.cursor.logicalModelFactories;

class ElementFactoryBase {
    //region Constructors

    ElementFactoryBase() {
        this.metadataFactory = new MetadataFactory();
    }

    //endregion


    //endregion

    //region Private
    protected MetadataFactory metadataFactory;
}
