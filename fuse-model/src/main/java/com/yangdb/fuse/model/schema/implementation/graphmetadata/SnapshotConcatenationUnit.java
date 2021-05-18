package com.yangdb.fuse.model.schema.implementation.graphmetadata;

/***
 * Size of the data granularity used to verify if something is coexisting on the same graph snapshot.
 *
 *
 */
public enum SnapshotConcatenationUnit {

    /***
     * All tuples that happen within a <b>second</b> are considered has coexisting on a graph snapshot.
     */
    SECONDS,

    /***
     * All tuples that happen within a <b>minute</b> are considered has coexisting on a graph snapshot.
     */
    MINUTES,

    /***
     * All tuples that happen within a <b>hour</b> are considered has coexisting on a graph snapshot.
     */
    HOURS,

    /***
     * All tuples that happen within a <b>day</b> are considered has coexisting on a graph snapshot.
     */
    DAYS,

    /***
     * All tuples that happen within a <b>week</b> are considered has coexisting on a graph snapshot.
     */
    WEEKS,

    /***
     * All tuples that happen within a <b>month</b> are considered has coexisting on a graph snapshot.
     */
    MONTHS,

    /***
     * All tuples that happen within a <b>year</b> are considered has coexisting on a graph snapshot.
     */
    YEARS
}
