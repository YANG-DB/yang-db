package org.kayhut.fuse.model.queryDTO;

import java.util.List;

/**
 * Created by user on 19-Feb-17.
 */
public class QueryHorizontalQuantifier extends QueryQuantifierBase {
    public int getBelow() {
        return below;
    }

    public void setBelow(int below) {
        this.below = below;
    }


    //region Fields
    private int below;
    //endregion
}
