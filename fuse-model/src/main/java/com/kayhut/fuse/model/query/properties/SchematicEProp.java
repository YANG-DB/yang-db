package com.kayhut.fuse.model.query.properties;

import com.kayhut.fuse.model.query.properties.constraint.Constraint;

/**
 * Created by roman.margolis on 07/02/2018.
 *
 * Translates pType to a schematic name such as "stringValue.keyword"
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

    //region Override Methods
    @Override
    public boolean equals(Object o) {
        if (!super.equals(o)) {
            return false;
        }

        if (!(o instanceof SchematicEProp)) {
            return false;
        }

        SchematicEProp other = (SchematicEProp)o;
        if (!this.schematicName.equals(other.schematicName)) {
            return false;
        }

        return true;
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
