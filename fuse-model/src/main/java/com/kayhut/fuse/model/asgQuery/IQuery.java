package com.kayhut.fuse.model.asgQuery;

import java.util.Collection;

/**
 * Created by moti on 6/21/2017.
 */
public interface IQuery<T> {
    Collection<T> getElements();
}
