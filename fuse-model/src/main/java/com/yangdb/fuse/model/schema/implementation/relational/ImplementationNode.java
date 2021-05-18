package com.yangdb.fuse.model.schema.implementation.relational;

import java.util.ArrayList;
import java.util.List;

/***
 * Represent a node on the implementation level format. Nodes follow the property graph model.
 *
 */
public class ImplementationNode {

    /***
     * List of types used to reference that node, as used in Graph Query Languages e.g. labels in Cypher.
     */
    private List<String> types = new ArrayList<>();

    /***
     * Simple identifier for table name e.g. “person” or “ab-21-78”. This specifies the name of the
     * actual database table representing nodes. This information will be extracted from the
     * database.
     */
    private String tableName;

    /**
     * Identifier for primary key provenance. Can be a single column or multiple columns. In the
     * case of multiple columns, the data on those columns will be transformed into String and then
     * concatenated, in the exact sequence described here.
     */
    private List<NodeIdImplementation> id = new ArrayList<>();

    /***
     * List of all attributes as fields (including the idColumn) that this node contains, in the
     * property graph model.
     */
    private List<Attribute> attributes = new ArrayList<>();


    /***
     * Restriction that may be applied to this node, to reject data rows that don't represent
     * nodes..
     */
    private List<RestrictionClauses> restrictions = new ArrayList<>();

    /**
     * Creates an empty implementation node.
     */
    public ImplementationNode() {
        types = new ArrayList<>();
        tableName = "";
        id = new ArrayList<>();
        attributes = new ArrayList<>();
        restrictions = new ArrayList<>();
    }

    /***
     * Generates an implementation Node.
     * @param types types used on the abstraction level for this implementation Node
     * @param tableName table where this node information comes from
     * @param id List of id items that compose the ID of that node
     * @param attributes list of attributes in that node
     * @param restrictions restrictions to assume that a tuple is a node of this type.
     */
    public ImplementationNode(final List<String> types, final String tableName, final List<NodeIdImplementation> id,
                              final List<Attribute> attributes, final List<RestrictionClauses> restrictions) {
        this.types = types;
        this.tableName = tableName;
        this.id = id;
        this.attributes = attributes;
        this.restrictions = restrictions;
    }

    ImplementationNode getImplementationNode() {
        return this;
    }

    /**
     * @param attributes the attributes to set
     */
    public void setAttributes(final List<Attribute> attributes) {
        this.attributes = attributes;
    }

    /**
     * @param types the types to set
     */
    public void setTypes(final List<String> types) {
        this.types = types;
    }

    /**
     * @param restrictions the restrictions to set
     */
    public void setRestrictions(final List<RestrictionClauses> restrictions) {
        this.restrictions = restrictions;
    }

    /**
     * @return the attributes
     */
    public List<Attribute> getAttributes() {
        return attributes;
    }

    /**
     * @return the types
     */
    public List<String> getTypes() {
        return types;
    }

    /**
     * @return the restrictions
     */
    public List<RestrictionClauses> getRestrictions() {
        return restrictions;
    }

    /**
     * @return the tableName
     */
    public String getTableName() {
        return tableName;
    }

    /**
     * @param tableName the tableName to set
     */
    public void setTableName(final String tableName) {
        this.tableName = tableName;
    }

    /**
     * @return the id
     */
    public List<NodeIdImplementation> getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    public void setId(final List<NodeIdImplementation> id) {
        this.id = id;
    }
}
