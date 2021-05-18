package com.yangdb.fuse.model.schema.implementation.graphmetadata;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Describes how the graph data in the store database should be interpreted.
 *
 *
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class GraphMetadata {

    /**
     * Field that described how data will be interpreted in order to do temporal graph processing.
     */
    private StorageLayout storageLayout = StorageLayout.IGNORETIME;

    /***
     * Backend-system type used on current implementation level .
     */
    private BackendSystem backendSystem = BackendSystem.INDEX;

    // Optional parameters for snapshot layout:
    /***
     * [OPTIONAL] This is valid only for snapshot layouts. All events that happen inside this unit
     * are considered as happening in the same snapshot
     */
    private Integer snapshotConcatenationFactor;

    /***
     * [OPTIONAL] The unit of time used for the snapshot.
     */
    private SnapshotConcatenationUnit snapshotConcatenationUnit;

    /***
     * Default constructor.
     */
    public GraphMetadata() {}

    /***
     * Constructor that takes into consideration the storage layout.
     *
     * @param storageLayout how data is stored.
     */
    public GraphMetadata(final StorageLayout storageLayout, final BackendSystem systemType) {
        this.storageLayout = storageLayout;
        this.backendSystem = systemType;
    }

    /**
     * @return the storageLayout
     */
    public StorageLayout getStorageLayout() {
        return storageLayout;
    }

    /**
     * @param storageLayout the storageLayout to set
     */
    public void setStorageLayout(final StorageLayout storageLayout) {
        this.storageLayout = storageLayout;
    }

    /**
     * @return the backendSystem
     */
    public BackendSystem getBackendSystem() {
        return backendSystem;
    }

    /**
     * Define the backend system type.
     * @param backendtype the backend system type to set
     */
    public void setBackendSystem(final BackendSystem backendtype) {
        this.backendSystem = backendtype;
    }

    /**
     * @return the SnapshotConcatenationFactor
     */
    public Integer getSnapshotConcatenationFactor() {
        return snapshotConcatenationFactor;
    }

    /**
     * @param snapshotConcatenationFactor the snapshotConcatenationFactor to set
     */
    public void setSnapshotConcatenationFactor(final Integer snapshotConcatenationFactor) {
        this.snapshotConcatenationFactor = snapshotConcatenationFactor;
    }

    /**
     * @return the SnapshotConcatenationUnit
     */
    public SnapshotConcatenationUnit getSnapshotConcatenationUnit() {
        return snapshotConcatenationUnit;
    }

    /**
     * Define the unit used for concatenation.
     * @param snapshotConcatenationUnit the SnapshotConcatenationUnit to set
     */
    public void setSnapshotConcatenationUnit(final SnapshotConcatenationUnit snapshotConcatenationUnit) {
        this.snapshotConcatenationUnit = snapshotConcatenationUnit;
    }

    @Override
    @SuppressWarnings("checkstyle:magicnumber")
    public int hashCode() {
        int result = storageLayout != null ? storageLayout.hashCode() : 0;
        result = 31 * result + (snapshotConcatenationFactor != null ? snapshotConcatenationFactor.hashCode() : 0);
        result = 31 * result + (snapshotConcatenationUnit != null ? snapshotConcatenationUnit.hashCode() : 0);
        result = 31 * result + (backendSystem != null ? backendSystem.hashCode() : 0);
        return result;
    }

    @Override
    public boolean equals(final Object o) {
        return (this.hashCode() == o.hashCode());
    }

}
