package com.kayhut.fuse.model.query.properties;

import com.kayhut.fuse.model.query.properties.constraint.Constraint;

/**
 *
 * Translates rType to a schematic name such as "stringValue.keyword"
 */
public class SchematicRelProp extends RelProp {
    //region Constructors
    public SchematicRelProp() {

    }

    public SchematicRelProp(int eNum, String pType, String schematicName, Constraint con) {
        this(eNum, pType, schematicName, con, 0);
    }

    public SchematicRelProp(int eNum, String pType, String schematicName, Constraint con, int b) {
        super(eNum, pType, con, b);
        this.schematicName = schematicName;
    }
    //endregion

    //region Properties
    public String getSchematicName() {
        return this.schematicName;
    }

    public void setSchematicName(String schematicName) {
        this.schematicName = schematicName;
    }
    //endregion

    //region Fields
    private String schematicName;
    //endregion
}
