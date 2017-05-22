package com.kayhut.fuse.unipop.controller.utils.labelProvider;

/**
 * Created by Roman on 22/05/2017.
 */
public class PrefixedLabelProvider implements LabelProvider<String> {
    //region Constructors
    public PrefixedLabelProvider(String splitString) {
        this.splitString = splitString;
    }
    //endregion

    //region LabelProvider Implementation
    @Override
    public String get(String data) {
        return data.split(splitString)[0];
    }
    //endregion

    //region Fields
    private String splitString;
    //endregion
}
