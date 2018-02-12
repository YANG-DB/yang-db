package com.kayhut.fuse.model.query.properties;

import com.kayhut.fuse.model.query.Constraint;

/**
 * Created by moti on 5/9/2017.
 */
public class RedundantRelProp extends SchematicRelProp {
    //region Constructors
    public RedundantRelProp() {

    }

    public RedundantRelProp(String redundantPropName) {
        this.redundantPropName = redundantPropName;
        this.setSchematicName(redundantPropName);
    }

    public RedundantRelProp(int eNum, String pType, String redundantPropName, String schematicName, Constraint con) {
        super(eNum, pType, schematicName, con);
        this.redundantPropName = redundantPropName;
        if (this.getSchematicName() == null) {
            this.setSchematicName(redundantPropName);
        }
    }
    //endregion

    //region Properties
    public String getRedundantPropName() {
        return redundantPropName;
    }

    public void setRedundantPropName(String redundantPropName) {
        this.redundantPropName = redundantPropName;
        if (this.getSchematicName() == null) {
            this.setSchematicName(redundantPropName);
        }
    }
    //endregion

    //region Fields
    private String redundantPropName;
    //endregion

    public static RedundantRelProp of(int eNum, String redundantPropName, String pType, Constraint constraint){
        return new RedundantRelProp(eNum, pType, redundantPropName, redundantPropName, constraint);
    }

    public static RedundantRelProp of(int eNum, String redundantPropName, String schematicName, String pType, Constraint constraint){
        return new RedundantRelProp(eNum, pType, redundantPropName, schematicName, constraint);
    }
}
