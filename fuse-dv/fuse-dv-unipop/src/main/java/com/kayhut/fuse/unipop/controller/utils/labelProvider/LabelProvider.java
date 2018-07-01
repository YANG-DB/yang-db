package com.kayhut.fuse.unipop.controller.utils.labelProvider;

/**
 * Created by Roman on 22/05/2017.
 */
public interface LabelProvider<T> {
    String get(T data);
}
