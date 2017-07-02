package com.kayhut.fuse.dispatcher.asg;

import com.kayhut.fuse.model.query.EBase;

import java.util.List;

/**
 * Created by liorp on 6/1/2017.
 */
public interface BellowFactory {
    //region Public Methods
    List<Integer> supplyBellow(EBase eBase);
}
