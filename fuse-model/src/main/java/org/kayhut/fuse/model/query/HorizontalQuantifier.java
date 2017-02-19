package org.kayhut.fuse.model.query;

/**
 * Created by user on 19-Feb-17.
 */
public class HorizontalQuantifier extends QuantifierBase {
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
