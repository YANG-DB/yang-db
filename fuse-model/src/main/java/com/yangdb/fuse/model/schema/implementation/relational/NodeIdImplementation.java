package com.yangdb.fuse.model.schema.implementation.relational;

/**
 * Relational implementation details on a node declared in the AbstractionLevel
 *
 *
 */
public class NodeIdImplementation implements Comparable<NodeIdImplementation> {

    /***
     * Column name that will be used in order to generate a node id.
     */
    private String columnName;

    /***
     * The datatype of that column. In order to generate a multi-type key, it will be transformed to
     * String (SQL92 FC [Fixed Character] datatype).
     */
    private String datatype;

    /***
     * Used in order to position the string when concatenating several columns used to compose a
     * node id.
     */
    private Integer concatenationPosition = 1;

    /***
     * Default constructor.
     */
    public NodeIdImplementation() {}

    /***
     * Generates a node id implementation.
     * @param columnName column from where the id is extracted
     * @param datatype relational datatype used to represent the ID
     * @param concatenationPosition position of concatenate multiple information and generate an ID from it.
     */
    public NodeIdImplementation(final String columnName, final String datatype, final Integer concatenationPosition) {
        this.columnName = columnName;
        this.datatype = datatype;
        this.concatenationPosition = concatenationPosition;
    }

    @Override
    public int compareTo(final NodeIdImplementation comparedNode) {
        // ascending order
        return concatenationPosition - comparedNode.getConcatenationPosition();
    }


    /**
     * @return the columnName
     */
    public String getColumnName() {
        return columnName;
    }


    /**
     * @param columnName the columnName to set
     */
    public void setColumnName(final String columnName) {
        this.columnName = columnName;
    }


    /**
     * @return the datatype
     */
    public String getDatatype() {
        return datatype;
    }


    /**
     * @param datatype the datatype to set
     */
    public void setDatatype(final String datatype) {
        this.datatype = datatype;
    }


    /**
     * @return the concatenationPosition
     */
    public Integer getConcatenationPosition() {
        return concatenationPosition;
    }


    /**
     * @param concatenationPosition the concatenationPosition to set
     */
    public void setConcatenationPosition(final Integer concatenationPosition) {
        this.concatenationPosition = concatenationPosition;
    }
}
