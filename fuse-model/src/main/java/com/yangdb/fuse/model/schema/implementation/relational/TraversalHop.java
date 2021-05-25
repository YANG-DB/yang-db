package com.yangdb.fuse.model.schema.implementation.relational;

/*-
 * #%L
 * fuse-model
 * %%
 * Copyright (C) 2016 - 2021 The YangDb Graph Database Project
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

/***
 * There may be several route implementations to traverse the same path, depending on the source vertex. Also, the route
 * may contain several table join that at the end will represent just one edge. The traversal hop models this feature.
 *
 *
 */
public class TraversalHop {

    /**
     * Simple identifier for source node table name e.g. “person”.
     */
    private String sourceTableName = "";

    /**
     * Simple identifier for source node table column name e.g. “id”. This column belongs to the Node table specified
     * by: sourceTableName.
     */
    private String sourceTableColumn = "";

    /**
     * [OPTIONAL] Used when there are join tables to represent edges. The column name (Simple Identifier) for the source
     * in an intermediate table: e.g. "person_id". This column belongs to the Edge table.
     */
    private String joinTableSourceColumn = "";

    /**
     * [OPTIONAL] Used when there are join tables to represent edges.Simple identifier for destination node table name
     * e.g. “movie”
     */
    private String joinTableName = "";

    /**
     * [OPTIONAL] Used when there are join tables to represent edges. The column name (Simple Identifier) for the
     * destination in an intermediate table: e.g. "movie_id". This column belongs to the Edge table.
     */
    private String joinTableDestinationColumn = "";

    /**
     * Simple identifier for destination node table column name e.g. “id”. This column belongs to the Node table
     * specified by: destinationTableName.
     */
    private String destinationTableColumn = "";

    /**
     * Simple identifier for destination node table name e.g. “person”.
     */
    private String destinationTableName = "";

    /***
     * Set of attributes that this edge contains, in a property graph model.
     */
    private List<EdgeAttribute> attributes = new ArrayList<>();

    /***
     * Set of attributes that belong to source node and are redundant.
     */
    private List<Attribute> redundantSourceAttributes = new ArrayList<>();

    /***
     * Set of attributes that belong to target node and are redundant.
     */
    private List<Attribute> redundantTargetAttributes = new ArrayList<>();

    private int stepNumber = 1;

    private List<RestrictionClauses> restriction;

    /***
     * Default constructor.
     */
    public TraversalHop() {}

    /**
     * @return the sourceTableColumn
     */
    public String getSourceTableColumn() {
        return sourceTableColumn;
    }

    /**
     * @param source the sourceTableColumn to set
     */
    public void setSourceTableColumn(final String source) {
        sourceTableColumn = source;
    }

    /**
     * @return the destinationTableColumn
     */
    public String getDestinationTableColumn() {
        return destinationTableColumn;
    }

    /**
     * @param destination the destinationTableColumn to set
     */
    public void setDestinationTableColumn(final String destination) {
        destinationTableColumn = destination;
    }

    /***
     * Generates a TraversalHop.
     *
     * @param sourceTableName sourceTableName
     * @param sourceTableColumn sourceTableColumn
     * @param joinTableSourceColumn joinTableSourceColumn
     * @param joinTableName joinTableName
     * @param joinTableDestinationColumn joinTableDestinationColumn
     * @param destinationTableColumn destinationTableColumn
     * @param destinationTableName destinationTableName
     * @param attributes attributes
     * @param stepNumber stepNumber
     * @param restriction restriction
     */
    @SuppressWarnings("checkstyle:parameternumber")
    public TraversalHop(final String sourceTableName, final String sourceTableColumn,
            final String joinTableSourceColumn, final String joinTableName, final String joinTableDestinationColumn,
            final String destinationTableColumn, final String destinationTableName,
            final List<EdgeAttribute> attributes, final int stepNumber, final List<RestrictionClauses> restriction) {
        this.sourceTableName = sourceTableName;
        this.sourceTableColumn = sourceTableColumn;
        this.joinTableSourceColumn = joinTableSourceColumn;
        this.joinTableName = joinTableName;
        this.joinTableDestinationColumn = joinTableDestinationColumn;
        this.destinationTableColumn = destinationTableColumn;
        this.destinationTableName = destinationTableName;
        this.attributes = attributes;
        this.stepNumber = stepNumber;
        this.restriction = restriction;
    }

    /***
     * Generates a TraversalHop.
     *
     * @param sourceTableName sourceTableName
     * @param sourceTableColumn sourceTableColumn
     * @param destinationTableColumn destinationTableColumn
     * @param destinationTableName destinationTableName
     * @param attributes attributes
     * @param stepNumber stepNumber
     * @param restriction restriction
     */
    public TraversalHop(final String sourceTableName, final String sourceTableColumn,
            final String destinationTableColumn, final String destinationTableName,
            final List<EdgeAttribute> attributes, final int stepNumber, final List<RestrictionClauses> restriction) {
        this.sourceTableName = sourceTableName;
        this.sourceTableColumn = sourceTableColumn;
        this.destinationTableColumn = destinationTableColumn;
        this.destinationTableName = destinationTableName;
        this.attributes = attributes;
        this.stepNumber = stepNumber;
        this.restriction = restriction;
    }

    public List<Attribute> getRedundantSourceAttributes() {
        return redundantSourceAttributes;
    }

    public void setRedundantSourceAttributes(List<Attribute> redundantSourceAttributes) {
        this.redundantSourceAttributes = redundantSourceAttributes;
    }

    public List<Attribute> getRedundantTargetAttributes() {
        return redundantTargetAttributes;
    }

    public void setRedundantTargetAttributes(List<Attribute> redundantTargetAttributes) {
        this.redundantTargetAttributes = redundantTargetAttributes;
    }

    /**
     * @return the attributes
     */
    public List<EdgeAttribute> getAttributes() {
        return attributes;
    }

    /**
     * Sets the attributes.
     *
     * @param attributes : {@code List<EdgeAttributes>}
     */
    public void setAttributes(final List<EdgeAttribute> attributes) {
        this.attributes = attributes;
    }

    /**
     * @return the sourceTableName
     */
    public String getSourceTableName() {
        return sourceTableName;
    }

    /**
     * @param sourceTableName the sourceTableName to set
     */
    public void setSourceTableName(final String sourceTableName) {
        this.sourceTableName = sourceTableName;
    }

    /**
     * @return the joinTableSourceColumn
     */
    public String getJoinTableSourceColumn() {
        return joinTableSourceColumn;
    }

    /**
     * @param joinTableSourceColumn the joinTableSourceColumn to set
     */
    public void setJoinTableSourceColumn(final String joinTableSourceColumn) {
        this.joinTableSourceColumn = joinTableSourceColumn;
    }

    /**
     * @return the joinTableName
     */
    public String getJoinTableName() {
        return joinTableName;
    }

    /**
     * @param joinTableName the joinTableName to set
     */
    public void setJoinTableName(final String joinTableName) {
        this.joinTableName = joinTableName;
    }

    /**
     * @return the joinTableDestinationColumn
     */
    public String getJoinTableDestinationColumn() {
        return joinTableDestinationColumn;
    }

    /**
     * @param joinTableDestinationColumn the joinTableDestinationColumn to set
     */
    public void setJoinTableDestinationColumn(final String joinTableDestinationColumn) {
        this.joinTableDestinationColumn = joinTableDestinationColumn;
    }

    /**
     * @return the destinationTableName
     */
    public String getDestinationTableName() {
        return destinationTableName;
    }

    /**
     * @param destionationTableName the destinationTableName to set
     */
    public void setDestinationTableName(final String destionationTableName) {
        destinationTableName = destionationTableName;
    }

    /**
     * Returns an array of the side data - mapped column, table column and table name
     *
     * It handles the joins using a join table or ones that don't (but not self joins).
     *
     * @param node Node to check the source and dest against
     * @return mapped column, table column and table name
     */
    @SuppressWarnings("checkstyle:magicnumber")
    private String[] getMappedSide(final ImplementationNode node) {
        String[] side = new String[3];
        // if the dest, src and join are the same we have a self join to the same table
        if (this.getDestinationTableName().equals(node.getTableName())
                && this.getSourceTableName().equals(node.getTableName())
                && StringUtils.isEmpty(this.getJoinTableName())) {
            side[0] = this.getDestinationTableColumn();
            side[1] = this.getSourceTableColumn();
            side[2] = this.getDestinationTableName();
        } else if (this.getDestinationTableName().equals(node.getTableName())
                && this.getSourceTableName().equals(node.getTableName())
                && !StringUtils.isEmpty(this.getJoinTableName())) {
            side[0] = this.getJoinTableSourceColumn();
            side[1] = this.getSourceTableColumn();
            side[2] = this.getSourceTableName();
        } else if (this.getDestinationTableName().equals(node.getTableName())) {
            side[0] = this.getJoinTableDestinationColumn();
            side[1] = this.getDestinationTableColumn();
            side[2] = this.getDestinationTableName();
        } else if (this.getSourceTableName().equals(node.getTableName())) {
            side[0] = this.getJoinTableSourceColumn();
            side[1] = this.getSourceTableColumn();
            side[2] = this.getSourceTableName();

        } else {
            side = null;
        }
        return side;
    }

    /**
     * Takes to ImplementationNodes and determines the source table name, column, join dest column and the destination
     * table name, column, join source column.
     *
     * Joins can be in three different forms:
     *
     * <ul>
     * <li>Case 1: Table A - Table A : self join (Employee ReportsTo Employee)</li>
     * <li>Case 2: Table A - Table B : Join without join table (Customer to Order)</li>
     * <li>Case 3: Table A - Joining Table - Table B : Join with joining table - Products - Orders (via OrderDetails)</li>
     * </ul>
     *
     * For case 2 & 3 the order of the join isn't important, however for case 1 the join of Employee1.ReportsTo =
     * Employee2.EmployeeId is different to Employee2.ReportsTo = Employee1.EmployeeId hence it takes the join direction
     * into account.
     *
     * It returns an array of 6 values: format is side A is 0..2 and sideB is 3..5... mapped column, table column and
     * table name
     *
     * @param nodeA nodeA First implementation node
     * @param nodeB nodeB Second implementation node
     * @param direction direction the direction of the join (only required if the join is a self join)
     * @return side details of the join mapping - contains 6 values that are
     */
    @SuppressWarnings("checkstyle:magicnumber")
    public String[] getMappedSide(final ImplementationNode nodeA, final ImplementationNode nodeB, final String direction) {
        String[] side = new String[6];
        // Case 1 - join is to itself without a joining table
        if (nodeA.equals(nodeB) && !StringUtils.isEmpty(this.getJoinTableName())) {
            if (direction.equals("INCOMING")) {
                side[0] = this.getJoinTableDestinationColumn();
                side[1] = this.getSourceTableColumn();
                side[2] = this.getSourceTableName();
                side[3] = this.getJoinTableSourceColumn();
                side[4] = this.getDestinationTableColumn();
                side[5] = this.getDestinationTableName();
            } else if (direction.equals("OUTGOING") || direction.equals("BOTH")) {
                side[0] = this.getJoinTableSourceColumn();
                side[1] = this.getSourceTableColumn();
                side[2] = this.getSourceTableName();
                side[3] = this.getJoinTableDestinationColumn();
                side[4] = this.getDestinationTableColumn();
                side[5] = this.getDestinationTableName();
            }
        } else {
            // Case 2 & 3 - join is without join table or with join table (but not self join)
            // get the details of side A
            String[] sideA = getMappedSide(nodeA);
            // get the details of side B
            String[] sideB = getMappedSide(nodeB);
            side[0] = sideA[0];
            side[1] = sideA[1];
            side[2] = sideA[2];
            side[3] = sideB[0];
            side[4] = sideB[1];
            side[5] = sideB[2];
        }
        return side;
    }

}
