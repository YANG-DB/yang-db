package com.yangdb.fuse.model.schema.implementation.graphmetadata;

/***
 * Backend system type used in a certain Implementation level file.
 *
 *
 */
public enum BackendSystem {

    /***
     * Relational: This option indicates that the implementation level will map the abstraction layer property graph to
     * to a relational backend (e.g. RDBMS)
     */
    RELATIONAL,

    /***
     * Index: This option indicates that the implementation level will map the abstraction layer property graph to
     * to an index backend.
     */
    INDEX
}
