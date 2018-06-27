package com.kayhut.fuse.dispatcher.query;

/**
 * Created by Roman on 12/15/2017.
 */
public interface QueryTransformer<QIn, QOut> {
    QOut transform(QIn query);
}
