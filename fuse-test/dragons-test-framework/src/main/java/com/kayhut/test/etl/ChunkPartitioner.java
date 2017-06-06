package com.kayhut.test.etl;

import java.util.Map;

/**
 * Created by Roman on 06/06/2017.
 */
public class ChunkPartitioner implements Partitioner {
    //region Constructors
    public ChunkPartitioner(int maxChunkSize) {
        this.maxChunkSize = maxChunkSize;
        this.currentPartition = 1;
        this.currentChunkSize = 0;
    }
    //endregion

    //region Partitioner Implementation
    @Override
    public String getPartition(Map<String, String> document) {
        this.currentChunkSize++;
        if (this.currentChunkSize == this.maxChunkSize) {
            this.currentPartition++;
            this.currentChunkSize = 0;
        }

        return Integer.toString(this.currentPartition);
    }
    //endregion

    //region Fields
    private int currentPartition;
    private int currentChunkSize;
    private int maxChunkSize;
    //endregion
}
