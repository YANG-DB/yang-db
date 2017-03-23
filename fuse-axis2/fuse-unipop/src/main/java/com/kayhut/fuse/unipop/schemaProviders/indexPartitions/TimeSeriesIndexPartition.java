package com.kayhut.fuse.unipop.schemaProviders.indexPartitions;

public interface TimeSeriesIndexPartition extends IndexPartition{
    String getDateFormat();
    String getIndexPrefix();
    String getIndexFormat();
    String getTimeField();
    Iterable<String> getAllIndices();
}
