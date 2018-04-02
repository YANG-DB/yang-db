package com.kayhut.fuse.model.query.properties;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.kayhut.fuse.model.query.properties.constraint.Constraint;
import com.kayhut.fuse.model.query.properties.projection.Projection;

/**
 * Created by benishue on 17/02/2017.
 */

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class EProp extends BaseProp {
    //region Constructors
    public EProp() {
        super();
    }

    public EProp(int eNum, String pType, Constraint con) {
        super(eNum, pType, con);
    }

    public EProp(int eNum, String pType, Projection proj) {
        super(eNum, pType, proj);
    }
    //endregion

    //region Static
    public static EProp of(int eNum, String pType, Constraint con) {
        return new EProp(eNum, pType, con);
    }

    public static EProp of(int eNum, String pType, Projection proj) {
        return new EProp(eNum, pType, proj);
    }
    //endregion
}
