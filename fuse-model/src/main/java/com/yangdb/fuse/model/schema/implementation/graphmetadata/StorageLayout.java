package com.yangdb.fuse.model.schema.implementation.graphmetadata;

/**
 * The possible formats that may be used when storing temporal data.
 *
 *
 */
public enum StorageLayout {

    /***
     * Which means that all tuples contained between the intervals that define a grain should be
     * considered has coexisting on the same graph snapshot.
     */
    SNAPSHOT,

    /***
     * Disables temporal graph evaluations.
     */
    IGNORETIME,

    /***
     * Assume delta updates on the graph data, as a continuous information stream..
     */
    DELTA
}
