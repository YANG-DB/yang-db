package com.kayhut.fuse.model.query.properties;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.kayhut.fuse.model.query.properties.constraint.Constraint;
import com.kayhut.fuse.model.query.properties.projection.Projection;

/**
 * Created by benishue on 17/02/2017.
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class RelProp extends BaseProp {
    //region Constructors
    public RelProp() {
        super();
    }

    public RelProp(int eNum, String pType, Constraint con, int b) {
        super(eNum, pType, con);
        this.b = b;
    }

    public RelProp(int eNum, String pType, Projection proj, int b) {
        super(eNum, pType, proj);
        this.b = b;
    }
    //endregion

    //region Properties
    public int getB() {
        return b;
    }

    public void setB(int b) {
        this.b = b;
    }
    //endregion

    //region Fields
    private int b;
    //endregion

    //region Static
    public static RelProp of(int eNum, String pType, Constraint con) {
        return new RelProp(eNum, pType, con, 0);
    }

    public static RelProp of(int eNum, String pType, Projection proj) {
        return new RelProp(eNum, pType, proj, 0);
    }
    //endregion
}
