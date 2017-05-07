package com.kayhut.fuse.unipop.schemaProviders.indexPartitions;

import java.util.Date;

public interface TimeSeriesIndexPartition extends IndexPartition{
    String getDateFormat();
    String getIndexPrefix();
    String getIndexFormat();
    String getTimeField();
    String getIndexName(Date date);
}
