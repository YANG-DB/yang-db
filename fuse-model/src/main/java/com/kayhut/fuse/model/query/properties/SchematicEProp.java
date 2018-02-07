package com.kayhut.fuse.model.query.properties;

import com.kayhut.fuse.model.query.Constraint;

/**
 * Created by roman.margolis on 07/02/2018.
 */
public class SchematicEProp extends EProp {
    //region Constructors
    public SchematicEProp() {

    }

    public SchematicEProp(int eNum, String pType, String schematicName, Constraint con) {
        super(eNum, pType, con);
        this.schematicName = schematicName;
    }
    //endregion

    //region Properties
    public String getSchematicName() {
        return schematicName;
    }

    public void setSchematicName(String schematicName) {
        this.schematicName = schematicName;
    }
    //enregion

    //region Fields
    private String schematicName;
    //endregion
}
