package com.kayhut.fuse.model.query.properties;

import com.kayhut.fuse.model.query.EBase;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by benishue on 25-Apr-17.
 */
public class EPropGroup  extends EBase {

    public EPropGroup() {
        this.eProps = new ArrayList<>();
    }

    public List<EProp> geteProps() {
        return eProps;
    }

    public void seteProps(List<EProp> eProps) {
        this.eProps = eProps;
    }

    //Region Fields
    private List<EProp> eProps;
    //endregion
}
