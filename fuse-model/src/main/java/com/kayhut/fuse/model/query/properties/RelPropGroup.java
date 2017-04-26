package com.kayhut.fuse.model.query.properties;

import com.kayhut.fuse.model.query.EBase;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by benishue on 25-Apr-17.
 */
public class RelPropGroup  extends EBase {

    public RelPropGroup() {
        this.rProps = new ArrayList<>();
    }

    public List<RelProp> getrProps() {
        return rProps;
    }

    public void setrProps(List<RelProp> rProps) {
        this.rProps = rProps;
    }

    //Region Fields
    private List<RelProp> rProps;
    //endregion
}
